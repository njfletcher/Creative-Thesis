package com.StockPrediction;
import java.io.IOException;
import java.util.Scanner;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;



/*
This class will takes whatever company you are making a prediction on, and grabs some supplemental information about it(sector, industry, etc.)
In the future, this will be one of the columns for the data. Daily news sentiment.
 */


public class NewsSentiment {

String companyName;
String tickerName;
String compSector;
String compIndustry;

    public NewsSentiment(String cName, String cTicker){
        companyName = cName;
        tickerName = cTicker;
    }

    public  void fetchNews(){
        
    }

    /*relies on webscraping, so may be unreliable in the future.
    For now it gets the sector of a selected company and the Industry name.
     */
    public void getCompanyInfo() throws IOException, InterruptedException {
        //https://www.tradingview.com/symbols/(TICKER SYMBOL GOES HERE)
        Document doc = Jsoup.connect("https://www.tradingview.com/symbols/" + tickerName.toUpperCase()).get();
        Elements e = doc.getElementsByClass("tv-widget-description__company-info");
        String resultText = e.first().text();
        System.out.println(resultText);

        int firstInd = resultText.indexOf(":");
        int nextInd = resultText.indexOf("Industry");
        int lastInd = resultText.lastIndexOf(":");
        compSector = resultText.substring(firstInd + 1, nextInd);
        compIndustry = resultText.substring(lastInd + 1);
        //System.out.println(compSector + " ; " + compIndustry);

    }
    public void twitter(){
        
    }


}
