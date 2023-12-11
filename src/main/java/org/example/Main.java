package org.example;

import java.util.*;

import static org.example.ArticleBodySaver.*;

public class Main {
    Map<String, String> headers = new HashMap<String, String>() {{
        put("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
        put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
    }};
    public static void main(String[] args){
        //save_hubs_pages_data("https://habr.com/ru/hubs/");
        //saveAllFlowsData();
        //parseHubsPages();
        //String hubPathString = "D:\\untitled3\\hubs\\cubrid";
        //String flow = "develop";
        //Path hubPath = Paths.get(hubPathString);
        //ArticleParser.parseHub(hubPath, flow);
        //parse_all_posts()
        //String hubsFolderPath = "D:\\untitled3\\hubs";
        //ArticlePageSaver.downloadHubPages(hubsFolderPath);
        String hubsFolder = "D:\\untitled3\\hubs";
        //downloadHubPages(hubsFolder);
        ArticleBodySaver articleBodySaverr = new ArticleBodySaver();
        processHubs(hubsFolder);
    }
}