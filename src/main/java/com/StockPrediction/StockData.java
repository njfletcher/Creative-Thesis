package com.StockPrediction;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.dom4j.io.SAXReader;
import org.json.JSONException;
import org.json.simple.parser.ParseException;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.xml.sax.SAXException;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import org.dom4j.*;

import javax.xml.parsers.ParserConfigurationException;



/*
    This class uses YahooFinance api to create custom datasets which are used for training, testing, and predicting.
 */

public class StockData {

    private String tickerName;
    private String companyName;
    private List<Long> volume = new ArrayList<Long>();
    private List<String> datesTrain = new ArrayList<String>();
    private List<String> datesTest = new ArrayList<String>();
    private double[] changes;
    SAXReader reader = new SAXReader();
    Document document = reader.read(new File(FileSystemConfig.fedFile));


    public StockData(String cName, String cTicker) throws IOException, DocumentException {
        companyName = cName;
        tickerName = cTicker;
    }

    //creates a training dataset and writes it to a CSV file. Contains 4 years of data.
    public void createTrain(File file, String tick) throws IOException, JSONException, ParserConfigurationException, SAXException, ParseException, DocumentException {
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -5); //5 years ago
        //amount of items per row: 252 x 4 = 1260
        to.add(Calendar.YEAR, -1);
        from.add(Calendar.DAY_OF_YEAR, -1);

        Stock chosenStock = YahooFinance.get(tick, from, to, Interval.DAILY);
        List<HistoricalQuote> stocksList = chosenStock.getHistory();

        //location of file that will house the input stock data. This uses a placeholder file for now.

        FileWriter fileWriter = new FileWriter(file);
        PrintWriter printWriter = new PrintWriter(fileWriter);


        //This line causes an error with the transform process. Need to find a method to remove it with the transform process.
        //printWriter.println(companyName+ "("+ tickerName + ")");
        //printWriter.println("Date,Change,Volume");
        changes = new double[stocksList.size()];
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");



        for (int i = 1; i < stocksList.size() - 1; i++) {

            //System.out.println("date: " + stocksList.get(i).getDate().getTime() + "Offset Date: " + stocksListOffset.get(i).getDate().getTime());
            printWriter.println(f.format(stocksList.get(i).getDate().getTime()) + "," + percentChang(i, stocksList) + "," + parseXML(FileSystemConfig.fedFile, f.format(stocksList.get(i).getDate().getTime()), document));
            //f.format(stocksList.get(i).getDate().getTime())+ "," +
            // + "," + stocksList.get(i).getVolume().doubleValue()
            datesTrain.add(f.format(stocksList.get(i).getDate().getTime()));
            changes[i] = percentChang(i, stocksList);
        }

        printWriter.close();
        //graph();
    }

    /*Creates test data or data that the network will make predictions on. Will contain the the last year of a stock's data,
    up until the point of calling this method. This dataset will be used to make a prediction for the next day.
    */
    public void createPredData() throws IOException, JSONException, ParserConfigurationException, DocumentException, SAXException, ParseException {

        Calendar from1 = Calendar.getInstance();
        Calendar to1 = Calendar.getInstance();
        //Causes error depending on what the last day was. Say end date was Jan 1st, offset then doesnt work becuase wont have off day in its database
        from1.add(Calendar.YEAR, -1);
        from1.add(Calendar.DAY_OF_YEAR, -1);


        Stock chosenStock1 = YahooFinance.get(tickerName, from1, to1, Interval.DAILY);
        List<HistoricalQuote> stocksList1 = chosenStock1.getHistory();

        FileWriter fileWriter1 = new FileWriter(FileSystemConfig.testFile);
        PrintWriter printWriter1 = new PrintWriter(fileWriter1);

        //This line causes an error with the transform process. Need to find a method to remove it with the transform process.
        //printWriter1.println(companyName+ "("+ tickerName + ")");
        //printWriter1.println("Date,Change,Volume");
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = 1; i < stocksList1.size() - 1; i++) {

            //System.out.println("date: " + stocksList1.get(i).getDate().getTime() + "Offset Date: " + stocksList1Offset.get(i).getDate().getTime());
            printWriter1.println(f.format(stocksList1.get(i).getDate().getTime()) + "," + percentChang(i, stocksList1)+ "," + parseXML(FileSystemConfig.fedFile, f.format(stocksList1.get(i).getDate().getTime()), document));
            //f.format(stocksList1.get(i).getDate().getTime())+ ","
            // + "," + stocksList1.get(i).getVolume().doubleValue()
            datesTest.add(f.format(stocksList1.get(i).getDate().getTime()));

        }

        printWriter1.close();
    }

    private double percentChang(int i, List<HistoricalQuote> norm) {

        return (norm.get(i).getClose().doubleValue() - norm.get(i - 1).getClose().doubleValue()) / norm.get(i - 1).getClose().doubleValue() * 100;
    }

    public void graph() {
        int seriesLength = changes.length;
        double[] timesteps = new double[seriesLength];
        for (int i = 0; i < seriesLength; i++) {
            timesteps[i] = (double) i;
        }
        // Create Chart
        XYChart chart = QuickChart.getChart("Sample Chart", "X", "Y", "y(x)", timesteps, changes);
        // Show it
        new SwingWrapper(chart).displayChart();
    }

    //provided a date, will parse the Federal Reserve of STL json file and output the Treasury value.
    public String parseXML(String fileName, String date, Document doc) throws ParserConfigurationException, IOException, SAXException, ParseException, JSONException, DocumentException {

        String value = null;

        Element root = doc.getRootElement();

        for(Iterator I = root.elementIterator(); I.hasNext();){
            Element element = (Element)I.next();
            if(element.attributeValue("date").equals(date)){
                 value =element.attributeValue("value");
            }
        }

        return value;
    }
}

