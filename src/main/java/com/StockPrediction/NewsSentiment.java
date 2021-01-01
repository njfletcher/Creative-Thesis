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

    public NewsSentiment(){

    }

    public  void fetchNews(){
        // this whole part is temporary
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a company name: ");
        companyName = sc.next();
        System.out.print("Enter a ticker symbol: ");
        tickerName = sc.next();
    }

    public void getCompanyNews() throws IOException, InterruptedException {
        //https://www.tradingview.com/symbols/(TICKER SYMBOL GOES HERE)
        Document doc = Jsoup.connect("https://www.tradingview.com/symbols/" + tickerName.toUpperCase()).get();
        Elements e = doc.getElementsByClass("tv-widget-description__company-info");
        compSector = e.first().text();
        System.out.println(compSector);
        
    }


}
