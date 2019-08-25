package com.github.huksley.app;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.*;
import java.net.URI;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Based on code from https://github.com/fcamblor/simpledl and https://github.com/dgageot/simplelenium/
 */
public class PhantomJSDownloader extends Downloader {
    public static String DEFAULT_BASE_URL = "https://bitbucket.org/ariya/phantomjs/downloads";
    public static String DEFAULT_VERSION = "2.1.1";
    private String baseUrl = DEFAULT_BASE_URL;
    private String version = DEFAULT_VERSION;

    public static void main(String[] args) {
        PhantomJSDownloader d = new PhantomJSDownloader();
        System.out.println(d.downloadAndExtract());
    }

    public PhantomJSDownloader() {
        super(3, 3);
    }

    public PhantomJSDownloader(int retryDownload, int retryConnect) {
        super(retryConnect, retryDownload);
    }

    public PhantomJSDownloader(int retryDownload, int retryConnect, String baseUrl, String version) {
        super(retryConnect, retryDownload);
        this.version = version;
        this.baseUrl = baseUrl;
    }

    public File downloadAndExtract() {
        PlatformInstall platformInstall;
        if (isWindows()) {
            platformInstall = new PlatformInstall(
                baseUrl + "/phantomjs-" + version + "-windows.zip",
                "phantomjs-" + version + "-windows/phantomjs.exe");
        } else if (isMac()) {
            platformInstall = new PlatformInstall(
                baseUrl + "/phantomjs-" + version + "-macosx.zip",
                "phantomjs-" + version + "-macosx/bin/phantomjs");
        } else if (isLinux32()) {
            platformInstall = new PlatformInstall(
                baseUrl + "/phantomjs-" + version + "-linux-i686.tar.bz2",
                "phantomjs-" + version + "-linux-i686/bin/phantomjs");
        } else {
            platformInstall = new PlatformInstall(
                baseUrl + "/phantomjs-" + version + "-linux-x86_64.tar.bz2",
                "phantomjs-" + version + "-linux-x86_64/bin/phantomjs");
        }

        return downloadAndExtract(new File(Configuration.USER_HOME.get(), ".phantomjs"), platformInstall);
    }
}

class LockFile {
    private static final List<LockFile> LOCKS_TAKEN = new CopyOnWriteArrayList<>();
    private final File lockFile;
    private FileLock lock;

    public LockFile(File lockFile) {
        this.lockFile = lockFile;
    }

    @SuppressWarnings("resource")
    public void waitLock() {
        while (true) {
            try {
                lockFile.getParentFile().mkdirs();
                lock = new FileOutputStream(lockFile).getChannel().tryLock();
                if (lock != null) {
                    LOCKS_TAKEN.add(this); // This way the lock cannot be reclaimed by the GC
                    return;
                }
            } catch (Exception e) {
                // Ignore
            }

            waitBeforeRetry();
        }
    }

    public void release() {
        requireNonNull(lock, "Lock before unlock");

        try {
            LOCKS_TAKEN.remove(this);
            lock.release();
            lockFile.delete();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to release lock");
        }
    }

    private static void waitBeforeRetry() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Ignore
        }
    }
}

enum Configuration {
    // Standard system properties
    USER_HOME("user.home", null, true),
    OS_NAME("os.name", null, true);

    private final String key;
    private final String defaultValue;
    private final boolean required;

    Configuration(String key, String defaultValue, boolean required) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    public int getInt() {
        return Integer.parseInt(get());
    }

    public String get() {
        return get(key, defaultValue, required);
    }

    public static String get(String key, String defaultValue, boolean required) {
        String value = System.getProperty(key);

        if ((value == null) || value.trim().isEmpty()) {
            if (required) {
                throw new IllegalArgumentException("System property [" + key + "] cannot be null nor empty");
            }
            return defaultValue;
        }

        return value;
    }
}

abstract class Downloader {
    protected static final int DEFAULT_RETRY_DOWNLOAD = 4;
    protected static final int DEFAULT_RETRY_CONNECT = 4;
    protected final int retryDownload;
    protected final int retryConnect;

    public Downloader(int retryConnect, int retryDownload) {
        this.retryConnect = retryConnect;
        this.retryDownload = retryDownload;
    }

    protected void pause(long timeout) {
        try {
            SECONDS.sleep(timeout);
        } catch (InterruptedException ie) {
            // Ignore
        }
    }

    public static class PlatformInstall {
        String url;
        String executablePath;

        public PlatformInstall(String url, String executablePath) {
            this.url = url;
            this.executablePath = executablePath;
        }

        public String getUrl() {
            return url;
        }

        public String getExecutablePath() {
            return executablePath;
        }
    }

    protected synchronized File downloadAndExtract(File installDir, PlatformInstall platformInstall) {
        installDir.mkdirs();

        LockFile lock = new LockFile(new File(installDir, "lock"));
        lock.waitLock();
        try {
            return extractExe("phantomJs", platformInstall, installDir);
        } finally {
            lock.release();
        }
    }

    protected File extractExe(String libName, PlatformInstall platformInstall, File installDirectory) {
        String url = platformInstall.getUrl();
        File executable = new File(installDirectory, platformInstall.getExecutablePath());
        if (executable.exists()) {
            return executable;
        }

        String zipName = url.substring(url.lastIndexOf('/') + 1);
        File targetZip = new File(installDirectory, zipName);
        downloadZip(libName, url, targetZip);

        System.out.println("Extracting " + libName);
        try {
            if (url.endsWith(".zip")) {
                unzip(targetZip, installDirectory);
            } else {
                untarbz2(targetZip, installDirectory);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to uncompress " + libName + " from " + targetZip.getAbsolutePath(), e);
        }

        executable.setExecutable(true);
        return executable;
    }

    protected void downloadZip(String driverName, String url, File targetZip) {
        if (targetZip.exists()) {
            if (targetZip.length() > 0) {
                return;
            }
            targetZip.delete();
        }

        System.out.printf("Downloading %s from %s...%n", driverName, url);

        File zipTemp = new File(targetZip.getAbsolutePath() + ".temp");
        zipTemp.getParentFile().mkdirs();

        try (InputStream input = URI.create(url).toURL().openStream()) {
            Files.copy(input, zipTemp.toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to download " + driverName + " from " + url + " to " + targetZip, e);
        }

        if (!zipTemp.renameTo(targetZip)) {
            throw new IllegalStateException(String.format("Unable to rename %s to %s", zipTemp.getAbsolutePath(), targetZip.getAbsolutePath()));
        }
    }

    protected void untarbz2(File zip, File toDir) throws IOException {
        File tar = new File(zip.getAbsolutePath().replace(".tar.bz2", ".tar"));

        try (FileInputStream fin = new FileInputStream(zip);
             BufferedInputStream bin = new BufferedInputStream(fin);
             BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bin)
        ) {
            Files.copy(bzIn, tar.toPath(), REPLACE_EXISTING);
        }

        untar(tar, toDir);
    }

    protected void unzip(File zip, File toDir) throws IOException {
        try (ZipFile zipFile = new ZipFile(zip)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                File to = new File(toDir, entry.getName());

                File parent = to.getParentFile();
                if (!parent.exists()) {
                    if (!parent.mkdirs()) {
                        throw new IOException("Unable to create folder " + parent);
                    }
                }

                try (InputStream input = zipFile.getInputStream(entry)) {
                    Files.copy(input, to.toPath(), REPLACE_EXISTING);
                }
            }
        }
    }

    protected void untar(File tar, File toDir) throws IOException {
        try (FileInputStream fin = new FileInputStream(tar);
             BufferedInputStream bin = new BufferedInputStream(fin);
             TarArchiveInputStream tarInput = new TarArchiveInputStream(bin)
        ) {
            ArchiveEntry entry;
            while (null != (entry = tarInput.getNextTarEntry())) {
                if (entry.isDirectory()) {
                    continue;
                }

                File to = new File(toDir, entry.getName());

                File parent = to.getParentFile();
                if (!parent.exists()) {
                    if (!parent.mkdirs()) {
                        throw new IOException("Unable to create folder " + parent);
                    }
                }

                Files.copy(tarInput, to.toPath(), REPLACE_EXISTING);
            }
        }
    }

    protected boolean isWindows() {
        return Configuration.OS_NAME.get().startsWith("Windows");
    }

    protected boolean isMac() {
        return Configuration.OS_NAME.get().startsWith("Mac OS X");
    }

    protected boolean isLinux32() {
        return Configuration.OS_NAME.get().contains("x86");
    }
}
