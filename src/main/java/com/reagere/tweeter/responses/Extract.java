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
        //driver.get("https://mobile.twitter.com/EmmanuelMacron/status/1068943621947367424");
        driver.get(args[0]);

        JavascriptExecutor jse = (JavascriptExecutor)driver;
        Thread.sleep(1000);
        jse.executeScript("window.main = document.getElementsByTagName(\"main\")[0]", "");
        long last, offset = 0;
        int count = 0;

        List<String> merge = new ArrayList<>();
        boolean once = true;
        do {
            last = offset;
            Thread.sleep(500);
            if (once) {
                List<WebElement> buttons = driver.findElements(By.xpath("//div[@role='button']"));
                for (WebElement b : buttons) {
                    if (b.isDisplayed()) {
                        String text = b.getText().trim();
                        if (text.equals("Fermer") || text.equals("Close"))
                        try {
                            b.click();
                            once = false;
                            break;
                        } catch (Exception e) {
                            //
                        }
                    }
                }
            }
            if (count == 0) {
                List<WebElement> dirs = driver.findElements(By.xpath("//div[@aria-label='Back']"));
                for (WebElement dir : dirs) {
                    if (dir.isDisplayed()) {
                        try {
                            dir.click();
                            break;
                        } catch (Exception e) {
                            //
                        }
                    }
                }
                dirs = driver.findElements(By.xpath("//div[@aria-label='Retour']"));
                for (WebElement dir : dirs) {
                    if (dir.isDisplayed()) {
                        try {
                            dir.click();
                            break;
                        } catch (Exception e) {
                            //
                        }
                    }
                }

                WebElement main = driver.findElement(By.tagName("main"));
                WebElement section = main.findElement(By.tagName("section"));

                // find all "* more repl*" and click on it
                boolean hasClicked;
                do {
                    hasClicked = false;
                    Thread.sleep(500);
                    try {
                        List<WebElement> mores = section.findElements(By.xpath("//div[@dir='auto']"));
                        for (WebElement more : mores) {
                            if (more.isDisplayed()) {
                                String content = more.getText();
                                if (content.contains("more repl") || content.contains(" réponses de plus") || content.contains(" réponse de plus")) {
                                    try {
                                        more.click();
                                        hasClicked = true;
                                    } catch (Exception e) {
                                        System.out.println(content);
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (hasClicked);

                String[] split = section.getText().split("\\."); // export the text
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

            jse.executeScript("setTimeout(function() {window.scrollTo(0, "+last+"+200);},1)", "");
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
