package com.StockPrediction;
import java.io.IOException;
import java.util.Scanner;
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


}
