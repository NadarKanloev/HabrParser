package org.example;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class ArticlePageSaverThroughCSV {
    public static void downloadHubPages(String hubsFolderPath) {
        File hubsFolder = new File(hubsFolderPath);

        // Проверяем, является ли hubsFolder директорией
        if (hubsFolder.isDirectory()) {
            // Получаем список поддиректорий (хабов)
            File[] hubFolders = hubsFolder.listFiles(File::isDirectory);
            if (hubFolders != null) {
                for (File hubFolder : hubFolders) {
                    saveArticles(new File(hubFolder, "articles" + ".csv"), hubFolder);
                }
            }
        } else {
            System.out.println("Указанный путь не является директорией.");
        }
    }
    private static void downloadHub(File hubFolder) {
        // Получаем путь к CSV файлу в текущей папке хаба
        File csvFile = new File(hubFolder, "articles.csv");

        // Проверяем, существует ли CSV файл
        if (csvFile.exists()) {
            saveArticles(csvFile, hubFolder);
        } else {
            System.out.println("Файл articles.csv не найден в папке: " + hubFolder.getName());
        }
    }

    private static void saveArticles(File csvFile, File hubFolder) {
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile)).build()) {
            List<String[]> lines = reader.readAll();

            // Перебираем строки в CSV файле
            for (String[] line : lines) {
                if (line.length >= 3) { // Проверяем, что у нас есть хотя бы 3 колонки
                    String articleId = line[2].trim();
                    if (!articleId.isEmpty()) {
                        String articleUrl = "https://habr.com/ru/articles/" + articleId;
                        saveArticle(articleUrl, hubFolder);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvException e) {
            throw new RuntimeException(e);
        }
    }
    private static void saveArticle(String articleUrl, File hubFolder) {
        try {
            Document articleDocument = Jsoup.connect(articleUrl).get();
            Element titleElement = articleDocument.selectFirst("meta[property=og:title]");
            String title = titleElement != null ? titleElement.attr("content") : null;

            if (title != null && !title.isEmpty()) {
                String fileName = title.replaceAll("[^a-zA-Z0-9а-яА-Я.-]", "_").replaceAll("[<>:\"/\\|?*]", "") + ".html";
                File articlesFolder = new File(hubFolder, "articles_" + hubFolder.getName());
                articlesFolder.mkdirs();
                File destinationFile = new File(articlesFolder, fileName);

                // Сохраняем страницу статьи в новую папку
                Files.write(destinationFile.toPath(), articleDocument.outerHtml().getBytes());

                System.out.println("Скачана статья: " + fileName);
            } else {
                System.out.println("Заголовок статьи не найден для URL: " + articleUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
