package com.StockPrediction;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

import com.google.gson.GsonBuilder;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



/*
This class will takes whatever company you are making a prediction on, and grabs some supplemental information about it(sector, industry, etc.)
In the future, this will be one of the columns for the data. Daily news sentiment.
 */


public class NewsSentiment {


    private String companyName;
    private String tickerName;
    private String compSector;
    private String compIndustry;
    private String companyDescription;


    public NewsSentiment(String cName, String cTicker){
        companyName = cName;
        tickerName = cTicker;
    }

    public  void fetchNews(){
        
    }

    /*relies on webscraping, so may be unreliable in the future.
    For now it gets the  sector of a selected company and the Industry name.
     */
    public void getCompanyInfo() throws IOException, InterruptedException {
        //https://www.tradingview.com/symbols/(TICKER SYMBOL GOES HERE)
        Document doc = Jsoup.connect("https://www.tradingview.com/symbols/" + tickerName.toUpperCase()).get();
        Elements e = doc.getElementsByClass("tv-widget-description__company-info");
        String resultText = e.first().text();
        Elements description = doc.getElementsByClass("tv-widget-description__text");
        companyDescription = description.text();

        int firstInd = resultText.indexOf(":");
        int nextInd = resultText.indexOf("Industry");
        int lastInd = resultText.lastIndexOf(":");
        compSector = resultText.substring(firstInd + 1, nextInd);
        compIndustry = resultText.substring(lastInd + 1);
        //System.out.println(compSector + " ; " + compIndustry);

    }
    public void displayInfo(){

        System.out.println("--------------------------------------------------------");
        System.out.println(companyName.toUpperCase() + "(" + tickerName.toUpperCase()+ ")");
        System.out.println(" ");
        System.out.println("Sector: " + compSector + " " + "Industry: " + compIndustry);
        System.out.println(" ");
        System.out.println(companyDescription);
        System.out.println("--------------------------------------------------------");
    }

    public void getData() throws IOException {

        String url = "https://api.stlouisfed.org/fred/series/observations?series_id=USEPUINDXD&api_key=" + System.getenv("API_KEY");
        URL urlObj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        System.out.println("Send 'HTTP GET' request to : " + url);

        Integer responseCode = connection.getResponseCode();
        System.out.println("Response Code : " + responseCode);
        new FileOutputStream(new File(FileSystemConfig.fedFile)).close();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader inputReader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = inputReader.readLine()) != null) {
                response.append(inputLine);
            }

            inputReader.close();

            Path pathXMLFile = Paths.get(FileSystemConfig.fedFile);
            Files.write(pathXMLFile, response.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }

    }


}
