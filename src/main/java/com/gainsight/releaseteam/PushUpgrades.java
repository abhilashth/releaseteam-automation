package com.gainsight.releaseteam;

import com.gainsight.pageobject.core.fluent.FluentDriver;
import com.gainsight.releaseteam.utils.CoreUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.openqa.selenium.WebDriver;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.xpath;

/**
 * Created by Abhilash Thaduka on 2/22/2017.
 */
public class PushUpgrades {

    private static FluentDriver fluentDriver;
    private static WebDriver driver;
    private static Properties properties;
    private static final String SELECT_SFDC_VERSION = "//select[contains(@id,'SchedulePushUpgradePage')]";
    private static final String ORGS_TYPE = "//select[contains(@id,'orgsType')]";
    private static final String CURRENT_SFDC_VERSION = "//select[contains(@id,'eligibleVersions')]";
    private static final String ORG_SEARCH_BOX = "//input[contains(@name,'SchedulePushUpgradePage:schedulePushUpgrade') and contains(@name,'searchBox')]";
    private static final String SEARCH_RESULTS = "//table[contains(@id,'SchedulePushUpgradePage')]/tbody/tr";
    private static final String LOADING_ICON = "//div[@class='loading']";
    private static final String SELECT_ORG_ID = "//input[contains(@id,'selectedSingleOrg')]";
    private static final String ORG_ID = "//span[contains(@id,'orgId')]";


    public static void main(String[] args) throws IOException, InterruptedException {

        List<String> orgs = getOrgDetails().stream().distinct().collect(Collectors.toList());
        System.out.println("Total Org Id's list is " + orgs);
        loadPropertiesfile();
        driver = CoreUtils.getChromeDriver();
        getfd().get(properties.getProperty("sfdcurl"));
        getfd().element(id("username")).waitUntil(30).ifElementIsNotDisplayed().clear().sendKeys(properties.getProperty("sfdcusername"));
        getfd().element(id("password")).clear().sendKeys(properties.getProperty("sfdcpassword"));
        getfd().element(id("Login")).click();
        Thread.sleep(5000); // waiting for things to get settled
        getfd().get(properties.getProperty("pushupgradeurl"));
        getfd().select(xpath(SELECT_SFDC_VERSION)).waitUntil(30).ifElementIsNotDisplayed().selectByText(properties.getProperty("sfdcversion"));
        getfd().select(xpath(ORGS_TYPE)).waitUntil(30).ifElementIsNotDisplayed().selectByText(properties.getProperty("orgType"));
        getfd().select(xpath(CURRENT_SFDC_VERSION)).waitUntil(30).ifElementIsNotDisplayed().selectByText(properties.getProperty("currentsfdcversion"));
        Map<String, Object> status = Maps.newHashMap();
        FileWriter passedFile = new FileWriter(properties.getProperty("passedlist"));
        FileWriter failedFile = new FileWriter(properties.getProperty("failedlist"));
        try {
            for (String org : orgs) {
                getfd().element(xpath(ORG_SEARCH_BOX)).waitUntil(30).ifElementIsNotDisplayed().clear().sendKeys(org);
                getfd().element(xpath(LOADING_ICON)).waitUntil(10).ignoringError().ifElementIsNotDisplayed().waitUntil(45).ifElementIsDisplayed();
                Thread.sleep(1000); // waiting for things to get settled
                if (getfd().element(xpath(SEARCH_RESULTS)).waitUntil(2).ignoringError().ifElementIsNotDisplayed().getElements().size() == 1) {
                    String resultOrg = getfd().element(xpath(ORG_ID)).getText().trim();
                    if (resultOrg.equalsIgnoreCase(org) || org.contains(resultOrg)) {
                        getfd().checkbox(xpath(SELECT_ORG_ID)).check(() -> {
                            boolean selected;
                            selected = !getfd().checkbox(xpath(SELECT_ORG_ID)).isSelected();
                            return selected;
                        });
                        status.put(org, "PASSED");
                        System.out.println("Successfully selected OrgId " + org);
                        passedFile.write(org + "\n");
                    }
                } else {
                    status.put(org, "FAILED");
                    System.out.println("Failed to select OrgId, Since search returned multiple results or org not found " + org);
                    failedFile.write(org + "\n");
                }
            }
            System.out.println("Final result is " + status.toString());
        } finally {
            failedFile.close();
            passedFile.close();
        }
    }


    private static void loadPropertiesfile() throws IOException {
        properties = new Properties();
        String propFileName = "app.properties";
        InputStream inputStream = MethodHandles.lookup().lookupClass().getClassLoader().getResourceAsStream(propFileName);
        if (inputStream != null) {
            properties.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }
    }

    private static List<String> getOrgDetails() throws IOException {
        ClassLoader classLoader = MethodHandles.lookup().lookupClass().getClassLoader();
        File file = new File(classLoader.getResource("orgs.csv").getFile());
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        List<String> orgs = Lists.newArrayList();
        for (String line : lines) {
            String[] array = line.split(",");
            orgs.add(array[0].trim());
        }
        return orgs;
    }

    private static FluentDriver getfd() {
        if (fluentDriver == null)
            initFluentDriver();
        return fluentDriver;
    }

    private static void initFluentDriver() {
        fluentDriver = new FluentDriver(driver);
    }
}