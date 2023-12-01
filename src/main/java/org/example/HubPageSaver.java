package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HubPageSaver {
    public static void save_hubs_pages_data(String pageUrl){
        try{
            File directory = new File("hubs_pages");
            if(!directory.exists()){
                directory.mkdir();
            }
            for(int i = 1; i < 11; i++){
                String url = pageUrl + "page" + i;
                Document document = Jsoup.connect(url).get();
                String fileName = "./hubs_pages/page_" + i + ".html";

                try (FileWriter writer = new FileWriter(fileName, false)){
                    writer.write(document.html());
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }
    public static void saveHubData(String pageUrl, String hubDir){
        String folderName = "hubs/" + hubDir;
        File directory = new File(folderName);
        if(!directory.exists()){
            directory.mkdirs();
        }
        for(int i = 1; i < 11; i++){
            String url = pageUrl + "page" + i;
            try {
                Document document = Jsoup.connect(url).get();
                String fileName = folderName + "/page_" + i + ".html";
                try (FileWriter writer = new FileWriter(fileName, false)) {
                    writer.write(document.html());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
