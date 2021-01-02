package com.StockPrediction;

import org.apache.log4j.BasicConfigurator;

import java.util.Scanner;



/* Made by Nicholas Fletcher; Start Date: Oct 10, 2020
---------------------------------------------------------------------
 This program uses a Neural Network , to predict the closing price of a selected stock over
 a certain time period: either daily or weekly.
 It is trained using a large dataset of multiple stocks, all of which have the same variables involved: volume, closing price, and news sentiment.
 */


public class Program {


    public static void main(String[] args) throws Exception {

        BasicConfigurator.configure();
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter Company name: ");
        String compName = sc.next();
        System.out.println("Enter Company ticker: ");
        String ticker = sc.next();

        StockData stockD = new StockData(compName, ticker);
        //stockD.createTrain();
        stockD.createPredData();

        NewsSentiment news = new NewsSentiment(compName, ticker);
        news.getCompanyInfo();

        NEURAL nn = new NEURAL();
        //nn.train();
        nn.predict();

    }
}
