package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ArticleBodySaver {
    private static String hubsDirectory;
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/SpringSceurity";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "admin";
    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS articles_bodies (" +
            "article_id VARCHAR(255) PRIMARY KEY," +
            "article_body TEXT)";

    private static void saveToDatabase(String articleId, String articleBody) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            try (Statement statement = connection.createStatement()) {
                // Создаем таблицу, если ее нет
                statement.executeUpdate(CREATE_TABLE_SQL);

                // Вставляем данные, если они не пусты
                if (!articleId.isEmpty() && !articleBody.isEmpty()) {
                    String insertSql = "INSERT INTO articles_bodies (article_id, article_body) VALUES (?, ?)";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
                        preparedStatement.setString(1, articleId);
                        preparedStatement.setString(2, articleBody);
                        preparedStatement.executeUpdate();
                        System.out.println("Сохранено в базу данных");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void processHubs(String directoryPath) {
        String hubsDirectory = directoryPath;
        File hubsFolder = new File(hubsDirectory);

        if (hubsFolder.exists() && hubsFolder.isDirectory()) {
            File[] hubFolders = hubsFolder.listFiles();

            if (hubFolders != null) {
                for (File hubFolder : hubFolders) {
                    if (hubFolder.isDirectory()) {
                        System.out.println("Выполняется " + hubFolder);
                        processHub(hubFolder);
                    }
                }
            }
        } else {
            System.out.println("Invalid hubs directory path.");
        }
    }

    private static void processHub(File hubFolder) {
        String hubName = hubFolder.getName();
        File articlesFolder = new File(hubFolder, "articles_" + hubName);

        if (articlesFolder.exists() && articlesFolder.isDirectory()) {
            File[] articleFiles = articlesFolder.listFiles();

            if (articleFiles != null) {
                for (File articleFile : articleFiles) {
                    if (articleFile.isFile()) {
                        processArticle(articleFile);
                    }
                }
            }
        }
    }

    private static void processArticle(File articleFile) {
        String articleId = getArticleId(articleFile);
        String articleBody = getArticleBody(articleFile);

        if (articleId != null && articleBody != null) {
            saveToDatabase(articleId, articleBody);
        }
    }

    private static String getArticleId(File articleFile) {
        try {
            Document doc = Jsoup.parse(articleFile, "UTF-8");
            Element scriptTag = doc.selectFirst("script[data-vmid=ldjson-schema]");

            if (scriptTag != null) {
                String jsonData = scriptTag.html();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(jsonData);
                String fullId = jsonNode.at("/mainEntityOfPage/@id").textValue();
                String numericId = fullId.replaceAll("\\D", "");
                return numericId;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getArticleBody(File articleFile) {
        try {
            Document doc = Jsoup.parse(articleFile, "UTF-8");
            Element bodyTag = doc.selectFirst("div.tm-article-presenter__body");
            return bodyTag != null ? bodyTag.html() : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
