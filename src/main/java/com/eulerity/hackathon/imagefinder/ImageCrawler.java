package com.eulerity.hackathon.imagefinder;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ImageCrawler implements Runnable {


    public static ArrayList<String> linkList;

    //contains the number of images scraped in each link, with index corresponding to linkList
    public static ArrayList<Integer> imagesInLink;

    //number of links crawled / number of threads created
    public static int linkNumber = 0;
    
    //links of each scraped image
    public static ArrayList<String> imageList;

    //domain of input URL
    public static String domain;
    
    //max number of links that can be crawled
    public static final int MAX_CRAWLS = 25;

    private Document doc;

    @Override
    public void run() {
    }

    public void stop() {

    }

    public ImageCrawler(String url) {

        if (linkNumber >= MAX_CRAWLS) {
            Thread.currentThread().interrupt();
        } else {
            try {

                doc = Jsoup.connect(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.48 Safari/537.36")
                .get();

                //if connection was successful, increment linkNumber
                linkNumber += 1;
        
                //for initial thread
                if (domain == null) {

                    domain = url.split("//")[1].split("/")[0];

                    //initialize variables
                    imageList = new ArrayList<String>();
                    linkList = new ArrayList<String>();
                    imagesInLink = new ArrayList<Integer>();

                    //add initial URL to list
                    linkList.add(url);
                }

                //add new link, currently with 0 images
                imagesInLink.add(0);

                //get all icons
                Elements icons = doc.select("link[rel=shortcut icon]");
                for (Element icon : icons) {
                    String iconSource = icon.attr("href");
                    
                    if (!imageList.contains(iconSource)) {

                        imageList.add(iconSource);
                        imagesInLink.set(linkNumber-1, imagesInLink.get(linkNumber-1)+1);  
                    } 
                }

                //get all images
                Elements images = doc.select("img");
                for (Element image : images) {
                    String imageSource = image.attr("abs:src");
                    if (!imageSource.isEmpty()) {
                        if (!imageList.contains(imageSource)) {
                            if (imageSource.indexOf("blur=") != -1) {
                                imageSource = imageSource.split("blur=")[0];
                            }
                            imageList.add(imageSource);
                            imagesInLink.set(linkNumber-1, imagesInLink.get(linkNumber-1)+1);  
                        } 
                    }
                }

                //get all new links until MAX_CRAWLS threshold is met
                if (!(linkNumber >= MAX_CRAWLS)) {  
                    Elements links = doc.select("a[href]");
                    for (Element link : links) {
                        if (!(linkNumber >= MAX_CRAWLS)) {
                            String linkURL = link.attr("abs:href");
                            String linkDomain = linkURL.split("//")[1].split("/")[0].replace("www.", "");
                            if (linkDomain.equalsIgnoreCase(domain) && !linkList.contains(linkURL)) { 
                                Thread.sleep(100);
                                linkList.add(linkURL);
                                ImageCrawler newThread = new ImageCrawler(linkURL);
                            }
                        }
                    }
                }

            } catch (IllegalArgumentException e) {
                System.out.println(e + ", Link Number: " + linkNumber); 
                if (linkNumber == 0) {
                    linkList.add("Error Detected: " + "Malformed URL: " + url + ", Please enter a correct URL");
                }
            } catch (Exception e) {
                System.out.println(e);
                if (linkNumber == 0) {
                    linkList.add("Error Detected: " + e.toString());
                }
            }
        }
    }
}