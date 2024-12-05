package demo;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import com.fasterxml.jackson.databind.ObjectMapper;
// import io.github.bonigarcia.wdm.WebDriverManager;
import demo.wrappers.Wrappers;

public class TestCases {
    ChromeDriver driver;

    /*
     * TODO: Write your tests here with testng @Test annotation. 
     * Follow `testCase01` `testCase02`... format or what is provided in instructions
     */

     @BeforeMethod
     public void openURL(){
        Wrappers.goToURLAndWait(driver,"https://www.scrapethissite.com/pages/");
     }

     @Test (enabled = true)
     public void testCase01() throws InterruptedException{

        Assert.assertTrue(driver.getCurrentUrl().equals("https://www.scrapethissite.com/pages/"), "Unverified the URl");
        System.out.println("Verified URL is: https://www.scrapethissite.com/pages/");

        WebElement hockeyTeam=driver.findElement(By.partialLinkText("Forms, Searching and Pagination"));
        Wrappers.clickMethod(driver, hockeyTeam);

        ArrayList<HashMap<String, Object>> dataList=new ArrayList<>();
        WebElement clickOnPage=driver.findElement(By.xpath("(//ul[contains(@class, 'pagination')]//li//a)[1]"));
        Wrappers.clickMethod(driver, clickOnPage);

        for(int page=1;page<=4;page++){
            List<WebElement> rows=driver.findElements(By.xpath("//tr[@class='team']"));
            for(WebElement row: rows){
                String teamName=row.findElement(By.xpath(".//td[@class='name']")).getText();
                int year=Integer.parseInt(row.findElement(By.xpath(".//td[@class='year']")).getText());
                double winPer=Double.parseDouble(row.findElement(By.xpath(".//td[contains(@class, 'pct')]")).getText());

                long epochTime=System.currentTimeMillis()/1000;
                if(winPer<0.4){
                    HashMap<String, Object> dataMap=new HashMap<>();

                    dataMap.put("epochTime", epochTime);
                    dataMap.put("teamName", teamName);
                    dataMap.put("Year", year);
                    dataMap.put("winPercentage", winPer);

                    dataList.add(dataMap);
                }
            }
            if(page<4){
                WebElement nextPagElement=driver.findElement(By.xpath("//a[@aria-label='Next']"));
                nextPagElement.click();
                Thread.sleep(5000);
            }
        }
        for(HashMap<String, Object> data: dataList){
            System.out.println("Epoch time of scrape: "+data.get("epochTime")+", Team Name: "+data.get("teamName")+", Year: "+data.get("Year")+", winPercentage: "+data.get("winPercentage"));
        }

        //Store the HashMap data in a json File
        ObjectMapper mapper=new ObjectMapper();
        try{
            String userDir=System.getProperty("user.dir");
            File jsonFile=new File(userDir+"/src/test/resources/hockey-team-data.json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, dataList);
            System.out.println("JSON data written to: "+jsonFile.getAbsolutePath());
            Assert.assertTrue(jsonFile.length() !=0);
        }catch(IOException e){
            e.printStackTrace();
        }
     }

     @Test (enabled = true)
     public void testCase02(){

        WebElement oscarWinning=driver.findElement(By.partialLinkText("Oscar Winning Films"));
        Wrappers.clickMethod(driver, oscarWinning);

        Wrappers.scrape("2015", driver);
        Wrappers.scrape("2014", driver);
        Wrappers.scrape("2013", driver);
        Wrappers.scrape("2012", driver);
        Wrappers.scrape("2011", driver);
        Wrappers.scrape("2010", driver);
     }
     
    /*
     * Do not change the provided methods unless necessary, they will help in automation and assessment
     */
    @BeforeTest
    public void startBrowser()
    {
        System.setProperty("java.util.logging.config.file", "logging.properties");

        // NOT NEEDED FOR SELENIUM MANAGER
        // WebDriverManager.chromedriver().timeout(30).setup();

        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logs = new LoggingPreferences();

        logs.enable(LogType.BROWSER, Level.ALL);
        logs.enable(LogType.DRIVER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logs);
        options.addArguments("--remote-allow-origins=*");

        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "build/chromedriver.log"); 

        driver = new ChromeDriver(options);

        driver.manage().window().maximize();
    }

    @AfterTest
    public void endTest()
    {
        driver.close();
        driver.quit();

    }
}