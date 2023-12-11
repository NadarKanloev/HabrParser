package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ArticleBodySaver {
    private static String hubsDirectory;
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
            saveToCsv(articleId, articleBody);
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

    private static void saveToCsv(String articleId, String articleBody) {
        String csvFilePath = "D://untitled3//hubs//articles_body.csv";

        try (FileWriter writer = new FileWriter(csvFilePath, true)) {
            CSVUtils.writeLine(writer, Arrays.asList(articleId, articleBody));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class CSVUtils {
        private static final char DEFAULT_SEPARATOR = ',';

        public static void writeLine(FileWriter writer, List<String> values) throws IOException {
            boolean first = true;

            for (String value : values) {
                if (!first) {
                    writer.append(DEFAULT_SEPARATOR);
                }
                writer.append(escapeSpecialCharacters(value));
                first = false;
            }
            writer.append(System.lineSeparator());
        }

        private static String escapeSpecialCharacters(String value) {
            if (value.contains(String.valueOf(DEFAULT_SEPARATOR)) || value.contains("\"") || value.contains("\n")) {
                value = "\"" + value.replace("\"", "\"\"") + "\"";
            }
            return value;
        }
    }
}
