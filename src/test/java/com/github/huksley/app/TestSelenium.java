package com.github.huksley.app;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.seleniumhq.selenium.fluent.FluentWebDriver;
import org.seleniumhq.selenium.fluent.monitors.ScreenShotOnError;
import org.seleniumhq.selenium.fluent.monitors.ScreenShotOnError.WithUnitTestFrameWorkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import static org.seleniumhq.selenium.fluent.Period.secs;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "security.auth.type = test",
    "security.insecure = false",
    "security.auth.test.user = test",
    "security.auth.test.password = 123",
    "security.auth.test.roles = USER,ADMIN",
    "forward.login.success=/auth/info",
    "forward.logout.finish=/auth/info"
})
public class TestSelenium {
	Logger log = LoggerFactory.getLogger(getClass());

    private WebDriver driver;
    private FluentWebDriver fwd;
    private JavascriptExecutor js;
    @Value("${local.server.port}")
    int port;

    @Value("${browser.name:-firefox}")    
    String browser;
    
    @Value("${browser.driver.path}")
    String browserDriverPath;

    @Value("${browser.executable}")
    String browserExecutable;

    @Before
    public void init() throws Exception {
    	if (driver == null) {
	        if (browser.equals("firefox")) {
	            DesiredCapabilities capabilities = DesiredCapabilities.firefox();
	            capabilities.setCapability("marionette", true);
	            System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
	            if (browserExecutable != null) {
	            	capabilities.setCapability(FirefoxDriver.BINARY, browserExecutable);
	            }
	            if (browserDriverPath != null) {
	            	System.setProperty("webdriver.gecko.driver", browserDriverPath);
	            }
	            driver = new FirefoxDriver(new FirefoxOptions(capabilities));
	        } else
	        if (browser.equals("chrome")) {
	            if (browserDriverPath != null) {
	            	System.setProperty("webdriver.chrome.driver", browserDriverPath);
	            }
	        	ChromeOptions options = new ChromeOptions();
	        	if (browserExecutable != null) {
	        		options.setBinary(browserExecutable);
	        	}
				driver = new ChromeDriver(options);
	        } else {
	            throw new IllegalArgumentException("Invalid browser: " + browser);
            }

	        js = (JavascriptExecutor) driver;
	        driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS); //изменил 10 на 30
	        WithUnitTestFrameWorkContext scr = new ScreenShotOnError.WithUnitTestFrameWorkContext((TakesScreenshot) driver, TestSelenium.class, "test-classes", "surefire-reports/");
	        fwd = new FluentWebDriver(driver, scr);
    	}
    }

    @Test
    public void testApplcation() {
        driver.get("http://localhost:" + port);

        // Do logic here
        if (true) {
            return;
        }

        try {
            driver.close();
        } catch (Exception e) {
            // don`t care
        }
        try {
            driver.quit();
        } catch (Exception e) {
            // don`t care
        }
    }

    public void loadScript(JavascriptExecutor js, String url, String expr) throws IOException {
        String scriptLoader = readFile(getClass().getResourceAsStream("/ScriptInjectorTemplate.js"));
        scriptLoader = scriptLoader.replace("#EXPRESSION#", expr);
        scriptLoader = scriptLoader.replace("#URL#", url);
        js.executeScript(scriptLoader);
        long maxTime = 10 * 1000;
        long started = System.currentTimeMillis();
        Object l = null;
        do {
            try {
                l = js.executeScript("return window.__" + expr + "_loaded");
            } catch (Exception e) {
                System.err.println(e.toString());
            }

            try {
                Thread.sleep(100);
            } catch (Exception e) {
                throw new IOException("Interrupted");
            }

            if (System.currentTimeMillis() - started > maxTime) {
                throw new IOException("Timeout loading script: " + url);
            }
        } while (l == null);
    }


    public void selectSelect2Value(FluentWebDriver fwd, JavascriptExecutor js, String id, String value) {
        fwd.within(secs(10)).element(By.id("select2-" + id + "-container")).click();
        js.executeScript("$('#" + id + "').select2('open')");
        js.executeScript("$('#" + id + "').val('" + value + "')");
        js.executeScript("$('#" + id + "').trigger('change')");
        js.executeScript("$('#" + id + "').select2('close')");
    }

    public void selectSelect2Text(FluentWebDriver fwd, JavascriptExecutor js, String id, String title) {
        fwd.within(secs(10)).element(By.id("select2-" + id + "-container")).click();
        js.executeScript("$('#" + id + "').select2('open')");
        String var = "__sel" + System.currentTimeMillis();
        js.executeScript("var " + var + " = document.getElementById('" + id + "'); for (var i = 0; i < " + var + ".options.length; i++) " +
                        "if (" + var + ".options[i].text == '" + title + "') { " + var + ".selectedIndex = i; break; }");
        js.executeScript("$('#" + id + "').trigger('change')");
        js.executeScript("$('#" + id + "').select2('close')");
    }

    public String readFile(InputStream stream) throws IOException {
        Charset cs = Charset.forName("UTF-8");
        Reader reader = new BufferedReader(new InputStreamReader(stream, cs));
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[8192];
        int read;
        while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
            builder.append(buffer, 0, read);
        }
        return builder.toString();
    }
}
