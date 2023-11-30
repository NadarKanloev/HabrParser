package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static org.example.HubPageSaver.saveHubData;

public class HubParser {
    public static String getRatingDescription(Element article){
        String ratingDescription = article.select("span[data-test-id=votes-meter-value]").attr("title");
        return (ratingDescription.equals("0")) ? "" : ratingDescription;
    }
    private static void saveToCSV(List<String[]> data, String filePath) throws IOException{
        File directory = new File(filePath).getParentFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            for (String[] line : data) {
                writer.write(String.join(",", line) + "\n");
            }
        }
    }
    public static void writeCsv(Path hubDir, List<String[]> data, String[] header) throws IOException {
        // Имя файла по умолчанию
        String fileName = "articles.csv";

        // Создаем полный путь к файлу
        Path filePath = hubDir.resolve(fileName);

        // Создаем директорию, если она не существует
        Files.createDirectories(filePath.getParent());

        // Записываем в файл
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            // Записываем заголовок
            writer.write(String.join(",", header));
            writer.newLine();

            // Записываем данные
            for (String[] row : data) {
                writer.write(String.join(",", row));
                writer.newLine();
            }
        }
    }
    public static void parseHubsPages(){
        List<String[]> hubsList = new ArrayList<>();
        try {
            for(int i = 1; i < 11; i++){
                File input = new File(String.format("./hubs_pages/page_%d.html", i));
                Document doc = Jsoup.parse(input, "UTF-8");

                Elements hubs = doc.select("div.tm-hub__info");
                for(Element hub : hubs){
                    Element name = hub.selectFirst("a.tm-hub__title");
                    String hubName = name.text().toLowerCase().strip().replace(" ", "_");
                    String link = name.attr("href");
                    int startSubstr = link.lastIndexOf('/', link.length() - 2);
                    String hubDir = link.substring(startSubstr + 1, link.length() - 1).toLowerCase().strip();

                    Element description = hub.selectFirst("div.tm-hub__description");
                    String hubLink = "https://habr.com" + link + "articles/";

                    saveHubData(hubLink, hubDir);
                    System.out.println(hubLink);
                    hubsList.add(new String[]{hubName, hubDir, description.text(), hubLink});
                }
                saveToCSV(hubsList, "data/hubs.csv");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
