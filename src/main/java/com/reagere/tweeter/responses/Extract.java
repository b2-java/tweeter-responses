package com.reagere.tweeter.responses;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Extract {

    public static void main(String[] args) throws Exception {
        //System.setProperty("webdriver.chrome.driver", "c:/bin/chromedriver.exe");

        WebDriver driver = new ChromeDriver();
        // "https://mobile.twitter.com/EmmanuelMacron/status/1068943621947367424"
        driver.get(args[0]);

        JavascriptExecutor jse = (JavascriptExecutor)driver;
        Thread.sleep(1000);
        jse.executeScript("window.main = document.getElementsByTagName(\"main\")[0]", "");
        long last, offset = 0;
        int count = 0;

        List<String> merge = new ArrayList<>();
        do {
            last = offset;
            Thread.sleep(1000);

            if (count == 0) {
                WebElement main = driver.findElement(By.tagName("main"));
                WebElement section = main.findElement(By.tagName("section"));
                String[] split = section.getText().split("\\.");

                int positionValid = 0;
                for (int pos = 0; pos < Math.min(split.length, merge.size()); pos++) {
                    boolean found = false;
                    String el = split[pos];
                    for (int j = 0; j <= pos; j++) {
                        if (merge.get(merge.size()-1-j).equals(el)) {
                            boolean valid = true;
                            for (int k = 0; k <= j; k++) {
                                if (!merge.get(merge.size()-1-j+k).equals(split[pos-k])) {
                                    valid = false;
                                }
                            }
                            if (valid) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (found) {
                        positionValid = pos;
                        break;
                    }
                }
                for (int j = positionValid; j < split.length; j++) {
                    merge.add(split[j]);
                }
            }

            jse.executeScript("window.scrollBy(0, 250)", "");
            offset = (Long) jse.executeScript("return window.main.scrollHeight", "");

            if (offset == last) {
                count++;
            } else {
                count = 0;
            }
        } while(count < 5);
        System.out.println("END");
        StringBuilder sb = new StringBuilder();
        for (String line : merge) {
            sb.append(line);
        }
        Path path = Paths.get("output.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(sb.toString());
        } finally {
            driver.close();
            driver.quit();
        }
    }
}
