package com.github.huksley.app;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.seleniumhq.selenium.fluent.FluentWebDriver;
import org.seleniumhq.selenium.fluent.Period;
import org.seleniumhq.selenium.fluent.monitors.ScreenShotOnError;
import org.seleniumhq.selenium.fluent.monitors.ScreenShotOnError.WithUnitTestFrameWorkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Base64;
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
    public void testApplication() throws IOException, InterruptedException {
        driver.get("http://localhost:" + port);

        saveScreenshot("screenshot-landing.png");

        fwd.element(By.id("linkLogin")).click();
        fwd.within(Period.secs(2)).element(By.id("loginUsername")).sendKeys("test");
        fwd.element(By.id("loginPassword")).sendKeys("123");
        saveScreenshot("screenshot-login.png");

        fwd.element(By.id("buttonLogin")).click();
        // Should dissappear
        fwd.without(Period.secs(5)).element(By.id("buttonLogin"));
        // Should appear link to logout
        fwd.within(Period.secs(5)).element(By.id("linkLogout"));
        saveScreenshot("screenshot-loggedin.png");

        driver.get("http://localhost:" + port + "/index.html#/todo");
        fwd.within(Period.secs(5)).element(By.id("inputTodoNewDescription")).sendKeys("Hello, world! This is new todo from selenium! " + System.currentTimeMillis()).sendKeys(Keys.ENTER);

        // Should be at least one entry
        fwd.within(Period.secs(5)).element(By.id("rowTodo0"));

        // Check it as done
        fwd.input(By.id("checkboxTodo0")).click();

        // Should be no errors
        fwd.without(Period.secs(5)).element(By.id("labelTodoError"));

        // New todo prompt should be empty
        fwd.within(Period.secs(5)).element(By.id("inputTodoNewDescription")).getText().shouldBe("");
        saveScreenshot("screenshot-todo.png");

        driver.get("http://localhost:" + port + "/management/info");
        saveScreenshot("screenshot-management-info.png");

        driver.get("http://localhost:" + port + "/management/health");
        saveScreenshot("screenshot-management-health.png");

        driver.get("http://localhost:" + port + "/api/openapi.json");
        saveScreenshot("screenshot-openapi-json.png");

        driver.get("http://localhost:" + port + "/swagger-ui.html");
        // Wait for spec to load
        fwd.within(Period.secs(10)).element(By.className("base-url"));
        saveScreenshot("screenshot-swagger-ui.png");

        driver.get("http://localhost:" + port);
        fwd.within(Period.secs(5)).element(By.id("linkLogout")).click();
        fwd.within(Period.secs(5)).element(By.id("linkLogin"));
        saveScreenshot("screenshot-loggedout.png");

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

    private void saveScreenshot(String screenshotFile) throws IOException {
        String screenshotAs = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        byte[] decode = Base64.getDecoder().decode(screenshotAs);
        try (FileOutputStream fos = new FileOutputStream(screenshotFile)) {
            StreamUtils.copy(decode, fos);
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
