package com.StockPrediction;

import org.apache.log4j.BasicConfigurator;



/* Made by Nicholas Fletcher; Start Date: Oct 10, 2020
---------------------------------------------------------------------
 This program uses a Neural Network , to predict the closing price of a selected stock over
 a certain time period: either daily or weekly.
 It is trained using a large dataset of multiple stocks, all of which have the same variables involved: volume, closing price, and news sentiment.
 */


public class Program {


    public static void main(String[] args) throws Exception {

        BasicConfigurator.configure();

        System.out.println("Enter Company name: ");

        System.out.println("Enter Company ticker: ");

        //StockData stockPredict = new StockData();
        //stockPredict.fetchData();

        //NewsSentiment news = new NewsSentiment();
        //news.fetchNews();
        //news.getCompanyNews();

        //make a way to pass in the company name and ticker
        //reader = new StockRecordReader("TESLA", "TSLA");
        //reader.readReport();

        NEURAL nn = new NEURAL();
        //nn.train();
        nn.test();

        //NnModel network = new NnModel();
        //network.train();

        //network.makePrediction();


        //stockPredict.graph();
    }
}
