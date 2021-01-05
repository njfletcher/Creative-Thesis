package com.StockPrediction;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;



public class StockData {

    private String tickerName;
    private String companyName;
    private List<Double> closings = new ArrayList<Double>();
    private List<Long> volume = new ArrayList<Long>();
    private List<Date> datesTrain = new ArrayList<Date>();
    private List<Date> datesTest = new ArrayList<Date>();


    public StockData(String cName, String cTicker) throws IOException {
        companyName = cName;
        tickerName =cTicker;
    }

    //creates a training dataset and writes it to a CSV file. Contains 4 years of data.
    public void createTrain() throws IOException {
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -5); //5 years ago
        //amount of items per row: 252 x 4 = 1260
        to.add(Calendar.YEAR,-1);
        from.add(Calendar.DAY_OF_YEAR, -1);

        Stock chosenStock = YahooFinance.get(tickerName, from, to, Interval.DAILY);
        List<HistoricalQuote> stocksList = chosenStock.getHistory();

        //location of file that will house the input stock data. This uses a placeholder file for now.
        String fileName = "C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\stockReports_train.CSV";
        FileWriter fileWriter = new FileWriter(fileName);
        PrintWriter printWriter = new PrintWriter(fileWriter);


        //This line causes an error with the transform process. Need to find a method to remove it with the transform process.
        //printWriter.println(companyName+ "("+ tickerName + ")");


        for(int i =1; i<stocksList.size()-1; i++){

            //System.out.println("date: " + stocksList.get(i).getDate().getTime() + "Offset Date: " + stocksListOffset.get(i).getDate().getTime());
            printWriter.println(stocksList.get(i).getDate().getTime() + "," + percentChang(i, stocksList) + "," + stocksList.get(i).getVolume().doubleValue());
            datesTrain.add(stocksList.get(i).getDate().getTime());
        }

        printWriter.close();
    }

    /*Creates test data or data that the network will make predictions on. Will contain the the last year of a stock's data,
    up until the point of calling this method. This dataset will be used to make a prediction for the next day.
    */
    public void createPredData() throws IOException {

        Calendar from1 = Calendar.getInstance();
        Calendar to1 = Calendar.getInstance();
        //Causes error depending on what the last day was. Say end date was Jan 1st, offset then doesnt work becuase wont have off day in its database
        from1.add(Calendar.YEAR, -1);
        from1.add(Calendar.DAY_OF_YEAR, -1);


        Stock chosenStock1 = YahooFinance.get(tickerName, from1, to1, Interval.DAILY);
        List<HistoricalQuote> stocksList1 = chosenStock1.getHistory();


        String fileName1 = "C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\stockReports_test.CSV";
        FileWriter fileWriter1 = new FileWriter(fileName1);
        PrintWriter printWriter1 = new PrintWriter(fileWriter1);

        //This line causes an error with the transform process. Need to find a method to remove it with the transform process.
        //printWriter1.println(companyName+ "("+ tickerName + ")");

        for(int i =1; i<stocksList1.size()-1; i++){

            //System.out.println("date: " + stocksList1.get(i).getDate().getTime() + "Offset Date: " + stocksList1Offset.get(i).getDate().getTime());
            printWriter1.println(stocksList1.get(i).getDate().getTime() + "," + percentChang(i, stocksList1) + "," + stocksList1.get(i).getVolume().doubleValue());
            datesTest.add(stocksList1.get(i).getDate().getTime());

        }

        printWriter1.close();
    }

    private double percentChang(int i, List<HistoricalQuote> norm){

        return (norm.get(i).getClose().doubleValue()- norm.get(i-1).getClose().doubleValue()) / norm.get(i-1).getClose().doubleValue() * 100;
    }

    private double getFundData(){


        return 0.0;
    }

}
