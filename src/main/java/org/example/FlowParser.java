package org.example;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class FlowParser {
    public static Map<String, String> getFlows() {
        Map<String, String> flows = new HashMap<>();

        File flowsDirectory = new File("./flows");
        File[] flowsDirectories = flowsDirectory.listFiles(File::isDirectory);

        if (flowsDirectories != null) {
            for (File flowDirectory : flowsDirectories) {
                File hubFile = new File(flowDirectory, "hubs.csv");
                try {
                    List<String> lines = Files.readAllLines(hubFile.toPath());
                    for (String line : lines) {
                        String[] values = line.trim().split(",");
                        if (values.length >= 1) {
                            String hubId = values[0].trim().toLowerCase().replaceAll("[_-]", "");
                            flows.put(hubId, flowDirectory.getName());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Add debugging output
        System.out.println("Flows: " + flows);
        return flows;
    }
    public static List<String> parseFlowPage(String text) {
        List<String> res = new ArrayList<>();
        Document doc = Jsoup.parse(text);

        Elements hubs = doc.select("div.tm-hub");
        for (Element hub : hubs) {
            Element name = hub.selectFirst("a.tm-hub__title");
            if (name != null) {
                String hubName = name.text().toLowerCase().trim().replace(" ", "_");
                res.add(hubName);
            }
        }
        return res;
    }
    public static void saveAllFlowsData() {
        final String BASE_URL = "https://habr.com/ru/flows/";
        Map<String, Integer> flows = new HashMap<>();
        flows.put("develop", 7);
        flows.put("admin", 1);
        flows.put("design", 1);
        flows.put("management", 1);
        flows.put("marketing", 1);
        flows.put("popsci", 2);

        HttpClient httpClient = HttpClients.createDefault();

        for (Map.Entry<String, Integer> entry : flows.entrySet()) {
            String flow = entry.getKey();
            int pages = entry.getValue();

            File flowDirectory = new File("flows/" + flow);
            if (!flowDirectory.exists()) {
                List<String> hubs = new ArrayList<>();
                flowDirectory.mkdirs();

                for (int i = 1; i <= pages; i++) {
                    String url = BASE_URL + flow + "/hubs/page" + i + "/";
                    HttpGet request = new HttpGet(url);
                    request.setHeader("User-Agent", "Mozilla/5.0");
                    try {
                        HttpResponse response = httpClient.execute(request);
                        String htmlContent = EntityUtils.toString(response.getEntity(), "UTF-8"); // Преобразование ответа в строку
                        List<String> pageHubs = parseFlowPage(htmlContent);
                        hubs.addAll(pageHubs);

                        try (FileWriter fileWriter = new FileWriter("flows/" + flow + "/flow_page_" + i + ".html")) {
                            fileWriter.write(htmlContent);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                String[] hubsArray = hubs.toArray(new String[0]);
                writeHubsToCsv("flows/" + flow + "/hubs.csv", hubsArray);
            }
        }
    }
    private static void writeHubsToCsv(String filePath, String[] hubs) {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write("hub_id\n");
            for (String hub : hubs) {
                fileWriter.write(hub + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
