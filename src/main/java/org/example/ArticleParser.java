package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.example.FlowParser.getFlows;
import static org.example.HubParser.getRatingDescription;
import static org.example.HubParser.writeCsv;

public class ArticleParser {
    public static String parseHub(Path hubDir, String flow){
        List<String[]> allArticles = new ArrayList<>();
        for(int i = 1; i < 11; i++){
            try{
                File file = hubDir.resolve(String.format("page_%d.html", i)).toFile();
                String src = new String(Files.readAllBytes(file.toPath()), "UTF-8");
                Document doc = Jsoup.parse(src);
                Elements articles = doc.select("article.tm-articles-list__item");
                for(Element article : articles){
                    String author =  article.selectFirst("a.tm-user-info__username").text();
                    Element companyLinkElement = article.selectFirst("a.tm-article-snippet__hubs-item-link");
                    String companyLink = (companyLinkElement != null) ? companyLinkElement.attr("href") : "without_company";
                    String rating = article.select("span.tm-icon-counter__value").text();
                    String views = article.select("span.tm-icon-counter__value").text();
                    String bookmarks = article.select("span.bookmarks-button__counter").text();
                    String comments = article.select("span.tm-article-comments-counter-link__value").text();
                    String pubDatetime = article.select("span.tm-article-snippet__datetime-published").attr("data-time_published");
                    if(views.isEmpty()){
                        continue;
                    }
                    if(pubDatetime.isEmpty()){
                        pubDatetime = article.select("time").attr("datetime");
                    }else {
                        Element pubDatetimeElement = article.selectFirst("span.tm-article-snippet__datetime-published");
                        pubDatetime = (pubDatetimeElement != null) ? pubDatetimeElement.nextElementSibling().attr("datetime") : "";
                    }
                    if(companyLink == null){
                        companyLinkElement = article.selectFirst("a");
                        companyLink = (companyLinkElement != null) ? companyLinkElement.attr("href") : "without_company";
                    }
                    String[] articleData = {
                            hubDir.toString(), flow, article.id(), author, pubDatetime, rating, getRatingDescription(article),
                            views, bookmarks, comments, companyLink
                    };
                    allArticles.add(articleData);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        if (allArticles.size() == 200) {
            String[] header = {"hub_id", "flow", "article_id", "author", "pub_datetime", "rating", "rating_des",
                    "views", "bookmarks", "comments", "company_link"};
            try {
                writeCsv(hubDir, allArticles, header);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return hubDir.toString();
    }
    public static void parse_all_posts() {
        Map<String, String> flows = getFlows();

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            File hubsDirectory = new File("./hubs");
            File[] hubDirectories = hubsDirectory.listFiles(File::isDirectory);

            if (hubDirectories != null) {
                for (File directory : hubDirectories) {
                    String flow = flows.get(directory.getName());
                    Path directoryPath = Paths.get(directory.getAbsolutePath());
                    executorService.submit(() -> {
                        System.out.println("Parsing hub: " + directory.getName() + ", Flow: " + flow);
                        parseHub(directoryPath, flow);
                        System.out.println("Parsing completed for hub: " + directory.getName());
                    });
                }
            }
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
