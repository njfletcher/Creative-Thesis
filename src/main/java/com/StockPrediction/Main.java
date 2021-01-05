package com.StockPrediction;

import org.apache.log4j.BasicConfigurator;
import scala.collection.concurrent.Debug;

import java.io.File;
import java.util.Scanner;



/* Made by Nicholas Fletcher; Start Date: Oct 10, 2020
---------------------------------------------------------------------
 This program uses a Neural Network , to predict the closing price of a selected stock over
 a certain time period: either daily or weekly.
 It is trained using a large dataset of multiple stocks, all of which have the same variables involved: volume, closing price, and news sentiment.
 */


public class Main {


    public static void main(String[] args) throws Exception {

        BasicConfigurator.configure();
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter Company name: ");
        String compName = sc.next();
        System.out.println("Enter Company ticker: ");
        String ticker = sc.next();

        NewsSentiment news = new NewsSentiment(compName, ticker);
        news.getCompanyInfo();

        StockData stockD = new StockData(compName, ticker);
        Neural nn = new Neural();

        boolean train = true;

        if(new File("C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\model.txt").isFile() && train ==false){
            System.out.println("Model exists, using previously trained model to make prediction..");
            stockD.createPredData();
            nn.predict();
        }else{
            System.out.println("Model not found, training new one...");
            stockD.createTrain();
            stockD.createPredData();
            nn.train();
            nn.predict();
        }

    }
}
