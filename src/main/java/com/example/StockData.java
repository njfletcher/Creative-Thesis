package com.example;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;

import org.knowm.xchart.*;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import org.knowm.xchart.style.Styler.LegendPosition;
import java.awt.Color;
import java.text.SimpleDateFormat;


public class StockData {

    private String tickerName;
    private String companyName;
    Scanner sc = new Scanner(System.in);
    private List<Double> closings = new ArrayList<Double>();
    private List<Long> volume = new ArrayList<Long>();
    private List<Date> dates = new ArrayList<Date>();




    public StockData() throws IOException {
        //do later
    }

    //gets historical stock data for a given stock over a certain interval
    //will be used as part of the input when the model is completed
    public void fetchData() throws IOException {
        System.out.print("Enter a Company name: ");
        companyName = sc.next();
        System.out.print("Enter a ticker symbol: ");
        tickerName = sc.next().toUpperCase();
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -5); //5 years ago
        //amount of items per row: 252 x 4 = 1260
        to.add(Calendar.YEAR,-1);

        Stock chosenStock = YahooFinance.get(tickerName, from, to, Interval.DAILY);
        List<HistoricalQuote> stocksList = chosenStock.getHistory();

        from.add(Calendar.DAY_OF_YEAR, -1);



        Stock chosenStockOffset = YahooFinance.get(tickerName, from, to, Interval.DAILY);
        List<HistoricalQuote> stocksListOffset = chosenStockOffset.getHistory();



        //location of file that will house the input stock data. This uses a placeholder file for now.
        String fileName = "C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\stockReports_train.CSV";
        FileWriter fileWriter = new FileWriter(fileName);
        PrintWriter printWriter = new PrintWriter(fileWriter);


        //This line causes an error with the transform process. Need to find a method to remove it with the transform process.
        //printWriter.println(companyName+ "("+ tickerName + ")");


        for(int i =0; i<stocksList.size()-1; i++){

            System.out.println("date: " + stocksList.get(i).getDate().getTime() + "Offset Date: " + stocksListOffset.get(i).getDate().getTime());
            printWriter.println(stocksList.get(i).getDate().getTime() + "," + calcPercentChange(i, stocksList, stocksListOffset) + "," + stocksList.get(i).getVolume().doubleValue());
        }



        /*int count = 0;
        for (HistoricalQuote obj : stocksList ) {
            //System.out.println(obj.getClose());
            closings.add(obj.getClose().doubleValue());
            volume.add(obj.getVolume());
            dates.add(obj.getDate().getTime());
            //System.out.println(obj.getDate().getTime().toString());
            printWriter.println(obj.getClose().floatValue()+ "," + obj.getHigh().floatValue()+ "," + obj.getLow().floatValue() + "," + obj.getVolume());
            //obj.getDate().getTime() + ","
            //+ "," + obj.getVolume()


            System.out.println(obj.getDate().getTime() + "," + obj.getClose().doubleValue() + "," + obj.getVolume() + "," + obj.getHigh()+ "," + obj.getLow());

            count++;

        }

         */

        printWriter.close();
        System.out.println(" ");


        Calendar from1 = Calendar.getInstance();
        Calendar to1 = Calendar.getInstance();
        from1.add(Calendar.YEAR, -1);


        Stock chosenStock1 = YahooFinance.get(tickerName, from1, to1, Interval.DAILY);
        List<HistoricalQuote> stocksList1 = chosenStock1.getHistory();
        from1.add(Calendar.DAY_OF_YEAR, -3);

        Stock chosenStock1Offset = YahooFinance.get(tickerName, from1, to1, Interval.DAILY);
        List<HistoricalQuote> stocksList1Offset = chosenStock1Offset.getHistory();

        String fileName1 = "C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\stockReports_test.CSV";
        FileWriter fileWriter1 = new FileWriter(fileName1);
        PrintWriter printWriter1 = new PrintWriter(fileWriter1);

        //This line causes an error with the transform process. Need to find a method to remove it with the transform process.
        //printWriter1.println(companyName+ "("+ tickerName + ")");

        for(int i =0; i<stocksList1.size()-1; i++){

            System.out.println("date: " + stocksList1.get(i).getDate().getTime() + "Offset Date: " + stocksList1Offset.get(i).getDate().getTime());
            printWriter1.println(stocksList1.get(i).getDate().getTime() + "," + calcPercentChange(i, stocksList1, stocksList1Offset) + "," + stocksList1.get(i).getVolume().doubleValue());

        }

        /*int count1 = 0;
        for (HistoricalQuote obj : stocksList1 ) {
            //System.out.println(obj.getClose());
            closings.add(obj.getClose().doubleValue());
            volume.add(obj.getVolume());
            dates.add(obj.getDate().getTime());
            //System.out.println(obj.getDate().getTime().toString());
            printWriter1.println(obj.getClose().floatValue()+ "," + obj.getHigh().floatValue()+ "," + obj.getLow().floatValue() + "," + obj.getVolume());
            System.out.println(obj.getClose().doubleValue() + "," + obj.getVolume() + "," + obj.getHigh()+ "," + obj.getLow());
            //+ obj.getVolume() + ","
            //obj.getDate().getTime() + "," +

            count1++;

        }
        */
        printWriter1.close();

    }
    private double calcPercentChange(int i, List<HistoricalQuote> norm, List<HistoricalQuote> offset){

        return (norm.get(i).getClose().doubleValue()- offset.get(i).getClose().doubleValue()) / offset.get(i).getClose().doubleValue() * 100;
    }

}
