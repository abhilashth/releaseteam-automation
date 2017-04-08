package com.gainsight.releaseteam.utils;

import com.gainsight.exceptions.AutomationException;
import com.gainsight.testdriver.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.Collections;

/**
 * Created by Abhilash Thaduka on 4/8/2017.
 */
public class CoreUtils {

    private static WebDriver driver;

    public static WebDriver getFirefoxDriver() {
        Log.info("Driver is set to Firefox.");
        driver = new FirefoxDriver();
        return driver;
    }

    public static WebDriver getChromeDriver() {
        Log.info("Driver is set to Chrome.");
        String chromeDriverLocation = System.getProperty("webdriver.chrome.driver");
        if (StringUtils.isEmpty(chromeDriverLocation)) {
            throw new IllegalArgumentException("webdriver.chrome.driver property should not be null");
        }
        File chromeDriverExecutable = new File(chromeDriverLocation);
        if (!chromeDriverExecutable.exists()) {
            throw new AutomationException("webdriver.chrome.driver not found: " + chromeDriverLocation);
        }
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability("chrome.switches", Collections.singletonList("--start-maximized"));
        driver = new ChromeDriver(capabilities);
        driver.manage().window().maximize();
        return driver;
    }


    public static void getscreenshot(String screenshotName, WebDriver driver) throws Exception {
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File("target/" + screenshotName + ".png"));
    }


}


