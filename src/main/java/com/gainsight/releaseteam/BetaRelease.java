package com.gainsight.releaseteam;

import com.gainsight.pageobject.core.fluent.FluentDriver;
import com.gainsight.releaseteam.utils.CoreUtils;
import com.gainsight.sfdc.SalesforceConnector;
import com.gainsight.sfdc.beans.SFDCInfo;
import com.gainsight.testdriver.Log;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;

import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.xpath;

/**
 * Created by Abhilash Thaduka on 3/13/2017.
 */
public class BetaRelease {

    private static WebDriver driver;
    private static final String UPLOAD_BUTTON = "//input[@value='Upload']";
    private static final String FINAL_BETA_UPLOAD_BUTTON = "//input[contains(@id,'PackageDetailsPageBlockButtons:upload')]";
    private static final String UPLOAD_VERSION_NUMBER = "//span[contains(@id,'UploadVersionNumber')]";
    private static final String VERSION_NAME_INPUT = "//input[contains(@id,'VersionInfoSectionItem:VersionText')]";
    private static String confDir = ".";
    private static String basedir = ".";
    private static Properties properties;
    private static String sfdcendpoint;
    private static FluentDriver fluentDriver;


    public static void main(String[] args) throws Exception {

        loadPropertiesfile();
        getSalesforceConnection();
        //driver = CoreUtils.getChromeDriver();
        driver = CoreUtils.getFirefoxDriver();
        getfd().get(properties.getProperty("sfdcurl"));
        getfd().element(id("username")).waitUntil(30).ifElementIsNotDisplayed().clear().sendKeys(properties.getProperty("sfdcusername"));
        getfd().element(id("password")).clear().sendKeys(properties.getProperty("sfdcpassword"));
        getfd().element(id("Login")).click();
        Thread.sleep(5000); // waiting for things to get settled
        Log.info("Opening url for beta upload " + sfdcendpoint);
        getfd().get(sfdcendpoint);
        CoreUtils.getscreenshot("Image-After-Opening-Url", driver);
        getfd().input(xpath(UPLOAD_BUTTON)).waitUntil(45).ifElementIsNotDisplayed().click();
        String versionNumber = getfd().element(xpath(UPLOAD_VERSION_NUMBER)).waitUntil(600).ifElementIsNotDisplayed().getText().trim();
        Calendar calendar = Calendar.getInstance();
        String monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH);
        int year = calendar.get(Calendar.YEAR);
        String finalVersion = "V " + versionNumber + " (" + monthName + " " + year + ")";
        Log.info("=============Final version to upload is =========== " + finalVersion);
        getfd().element(xpath(VERSION_NAME_INPUT)).waitUntil(30).ifElementIsNotDisplayed().clear().sendKeys(finalVersion);
        //final upload page
        getfd().input(xpath(FINAL_BETA_UPLOAD_BUTTON)).waitUntil(45).ifElementIsNotDisplayed().click();
        CoreUtils.getscreenshot("Image-After-Beta-Upload", driver);

    }


    private static FluentDriver getfd() {
        if (fluentDriver == null)
            initFluentDriver();
        return fluentDriver;
    }

    private static void initFluentDriver() {
        fluentDriver = new FluentDriver(driver);
    }

    private static void getSalesforceConnection() {
        SalesforceConnector salesforceConnector = new SalesforceConnector(properties.getProperty("sfdcusername"),
                properties.getProperty("sfdcpassword") + properties.getProperty("sfdcstoken"),
                properties.getProperty("sfdcpartnerUrl"),
                properties.getProperty("sfdcapiVersion"));
        Assert.assertTrue(salesforceConnector.connect(), "Error while connecting to salesforce");
        SFDCInfo sfdcInfo = salesforceConnector.fetchSFDCinfo();
        sfdcendpoint = "https://" + sfdcInfo.getEndpoint().substring(8, sfdcInfo.getEndpoint().indexOf(".")) + ".salesforce.com/033U0000000CdVi?tab=PackageComponents";
    }

    public static void loadPropertiesfile() throws IOException {
        properties = new Properties();
        String propFileName = "betarelease.properties";
        InputStream inputStream = MethodHandles.lookup().lookupClass().getClassLoader().getResourceAsStream(propFileName);
        if (inputStream != null) {
            properties.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }

        File configFolder = new File(basedir + File.separator + "conf");
        if (!configFolder.exists()) {
            confDir = new File(new File("").getAbsolutePath()).toPath().getParent().toString();
        } else
            confDir = basedir;
        PropertyConfigurator.configure(confDir + "/conf/log4j.properties");
    }
}

