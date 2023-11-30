package org.example;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.Doc;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.example.ArticleParser.parse_all_posts;
import static org.example.FlowParser.saveAllFlowsData;
import static org.example.HubParser.parseHubsPages;

public class Main {
    Map<String, String> headers = new HashMap<String, String>() {{
        put("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
        put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
    }};

    public static void main(String[] args){
        //save_hubs_pages_data("https://habr.com/ru/hubs/");
        saveAllFlowsData();
        //parseHubsPages();
        String hubPathString = "D:\\untitled3\\hubs\\1c";
        String flow = "develop";

        Path hubPath = Paths.get(hubPathString);

       ArticleParser.parseHub(hubPath, flow);
        parse_all_posts();
    }
}