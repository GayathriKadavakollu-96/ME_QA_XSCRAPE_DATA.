package demo.wrappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

public class Wrappers {
    /*
     * Write your selenium wrappers here
     */

    public static void goToURLAndWait(WebDriver driver, String url) {
        driver.get(url);
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(WebDriver -> ((JavascriptExecutor) WebDriver)
                .executeScript("return document.readyState").equals("complete"));
    }

    public static boolean clickMethod(WebDriver driver, WebElement locator) {
        if (locator.isDisplayed()) {
            try {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].scrollIntoView(true);", locator);
                locator.click();
                Thread.sleep(2000);
                return true;
            } catch (Exception e) {
                System.out.println("Exception occured!: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    public static boolean enterTextMethod(WebDriver driver, WebElement element, String text) {
        try {
            element.click();
            element.clear();
            element.sendKeys(text);
            Thread.sleep(1000);
            return true;
        } catch (Exception e) {
            System.out.println("Exception oocured! " + e.getMessage());
            return false;
        }
    }

    public static void scrape(String year, WebDriver driver) {
        try {
            WebElement yearLink = driver.findElement(By.id(year));
            String yearText = yearLink.getText();

            clickMethod(driver, yearLink);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table")));

            ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();
            int count = 1;
            List<WebElement> filmRows = driver.findElements(By.xpath("//tr[@class='film']"));
            for (int movie = 0; movie < 5; movie++) {
                    String movieTitle = filmRows.get(movie).findElement(By.xpath(".//td[@class='film-title']")).getText();
                    int nominations = Integer
                            .parseInt(filmRows.get(movie).findElement(By.xpath(".//td[@class='film-nominations']")).getText());
                    int awards = Integer
                            .parseInt(filmRows.get(movie).findElement(By.xpath( ".//td[@class='film-awards']")).getText());
                    boolean isWinnerFlag = count == 1;
                    String isWinner = String.valueOf(isWinnerFlag);

                    long epoch = System.currentTimeMillis() / 1000;
                    
                    HashMap<String, Object> movieMap = new HashMap<>();
                    movieMap.put("epochTime", epoch);
                    movieMap.put("Year", yearText);
                    movieMap.put("movieTitle", movieTitle);
                    movieMap.put("Nominations", nominations);
                    movieMap.put("Awards", awards);
                    movieMap.put("isWinner", isWinner);

                    dataList.add(movieMap);
                    count++;
            }
            for (HashMap<String, Object> movieData : dataList) {
                System.out.println("Epoch Time of Scrape: " + movieData.get("epochTime") + ", Year: "
                        + movieData.get("Year") + ", MovieTitle: " + movieData.get("movieTitle") + ", Nomination: "
                        + movieData.get("Nominations") + ", Awards: " + movieData.get("Awards") + ", isWinner: "
                        + movieData.get("isWinner"));
            }

            ObjectMapper mapper = new ObjectMapper();
            try {
                String userDir = System.getProperty("user.dir");
                File jsonFile = new File(userDir + "/src/test/resources/" + year + "-oscar-winner-data.json");
                mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, dataList);
                System.out.println("JSON data written to: " + jsonFile.getAbsolutePath());
                Assert.assertTrue(jsonFile.length() != 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("Web Scrap for movies is failed..");
            e.printStackTrace();
        }
    }

}
