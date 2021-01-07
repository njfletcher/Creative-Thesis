package com.StockPrediction;

import org.apache.log4j.BasicConfigurator;
import scala.collection.concurrent.Debug;

import java.io.File;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Scanner;



/* Made by Nicholas Fletcher
---------------------------------------------------------------------
 This program uses a Neural Network to predict the movement of a selected stock over a daily period.
 It is trained using a large time series dataset of a stock, which has two variables involved: %change and volume.
 In the future news sentiment index will be a third variable
 */


public class Main {


    public static void main(String[] args) throws Exception {

        BasicConfigurator.configure();
        Scanner sc = new Scanner(System.in);
        boolean train= true;
        System.out.println("Enter Company name: ");
        String compName = sc.next();
        System.out.println("Enter Company ticker: ");
        String ticker = sc.next();
        LocalDate dateTime =LocalDate.now();

        //train model if certain day of the month
        if(dateTime.getDayOfMonth() == 1){
            train =true;
        }else{
            train = false;
        }


        NewsSentiment news = new NewsSentiment(compName, ticker);
        news.getCompanyInfo();

        StockData stockD = new StockData(compName, ticker);
        Neural nn = new Neural();

        stockD.createTrain();
        stockD.createPredData();

        if(FileSystemConfig.trainFile.exists() && train == false){
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
