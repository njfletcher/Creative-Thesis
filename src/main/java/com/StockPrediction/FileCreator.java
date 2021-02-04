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

public class FileCreator {

    private String tickerName;
    private String companyName;
    private List<Long> volume = new ArrayList<Long>();
    private List<String> datesTrain = new ArrayList<String>();
    private List<String> datesTest = new ArrayList<String>();
    private double[] changes;
    SAXReader reader = new SAXReader();
    Document document = reader.read(new File(FileSystemConfig.fedFile));


    public FileCreator(String cName, String cTicker) throws IOException, DocumentException {
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

        //printWriter.println("Close5,Close4,Close3,Close2,Close1,Sentiment,Volume,Label");


        for (int i = 4; i < stocksList.size() - 2; i++) {

            //System.out.println("date: " + stocksList.get(i).getDate().getTime() + "Offset Date: " + stocksListOffset.get(i).getDate().getTime());
            printWriter.println(
                    f.format(stocksList.get(i).getDate().getTime())//0
                    + "," + stocksList.get(i-2).getClose().doubleValue()//1
                    + "," + stocksList.get(i-1).getClose().doubleValue()//2
                    + "," + stocksList.get(i).getClose().doubleValue()//3
                    + "," + parseXML(FileSystemConfig.fedFile, f.format(stocksList.get(i).getDate().getTime()), document)//4
                    //+ "," + stocksList.get(i).getVolume().doubleValue()
                    + "," + stocksList.get(i+1).getClose().doubleValue());//5(label)
            //f.format(stocksList.get(i).getDate().getTime())
            //f.format(stocksList.get(i).getDate().getTime())+ "," +
            // + "," + stocksList.get(i).getVolume().doubleValue()
            datesTrain.add(f.format(stocksList.get(i).getDate().getTime()));
            changes[i] = percentChang(i, stocksList);
        }

        printWriter.close();
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

        for (int i = 4; i < stocksList1.size() - 1; i++) {

            //System.out.println("date: " + stocksList1.get(i).getDate().getTime() + "Offset Date: " + stocksList1Offset.get(i).getDate().getTime());
            printWriter1.println(
                    f.format(stocksList1.get(i).getDate().getTime())//0
                    + "," + stocksList1.get(i-2).getClose().doubleValue()//1
                    + "," + stocksList1.get(i-1).getClose().doubleValue()//2
                    + "," + stocksList1.get(i).getClose().doubleValue()//3
                    + "," + parseXML(FileSystemConfig.fedFile, f.format(stocksList1.get(i).getDate().getTime()), document)//4
                    //+ "," + stocksList1.get(i).getVolume().doubleValue()
                    + "," + stocksList1.get(i+1).getClose().doubleValue());//5(LABEL)
            //percentChang(i, stocksList1)
            //f.format(stocksList1.get(i).getDate().getTime())+ ","
            // + "," + stocksList1.get(i).getVolume().doubleValue()
            datesTest.add(f.format(stocksList1.get(i).getDate().getTime()));

        }

        printWriter1.close();
    }

    //Calculates percent change. Not used right now.
    private double percentChang(int i, List<HistoricalQuote> norm) {

        return (norm.get(i).getClose().doubleValue() - norm.get(i - 1).getClose().doubleValue()) / norm.get(i - 1).getClose().doubleValue() * 100;
    }

    //provided a date, will parse the Federal Reserve of STL XML file and output the NEWS SENTIMENT value.
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

