package com.StockPrediction;

import java.io.File;
import java.util.Scanner;




/* Made by Nicholas Fletcher
---------------------------------------------------------------------
 This program uses machine learning to predict the movement of a selected stock over a daily period.
 It is trained using a large time series dataset of a stock, which has 4 variables involved: closing price(t-2), closing price(t-1),
 closing price(t), news sentiment score(t). Prediction represents closing price of (t+1).
 */


public class Main {


    public static void main(String[] args) throws Exception {


        Scanner input = new Scanner(System.in);
        boolean running = true;

        //Reads title block from file. Contains directions and some ASCII art.
        File title = new File(FileSystemConfig.baseDir + "TITLE.TXT");
        Scanner fileRead = new Scanner(title);
        int lineNumber = 1;
        while(fileRead.hasNextLine()){
            String line = fileRead.nextLine();
            System.out.println(line);
            lineNumber++;
        }

        while(running){

            System.out.println(" ");
            System.out.println("Enter Company name: ");
            String compName = input.next();

            if(compName.equals("exit")){
                System.exit(0);
            }


            System.out.println("Enter Company ticker: ");
            String ticker = input.next();
            if(ticker.equals("exit")){
                System.exit(0);
            }

            NewsSentiment news = new NewsSentiment(compName, ticker);
            news.getCompanyInfo();
            news.displayInfo();
            news.getData();

            FileCreator stockD = new FileCreator(compName, ticker);
            stockD.createTrain(FileSystemConfig.trainFile, ticker);
            stockD.createPredData();

            Transform t = new Transform();
            t.analyze(FileSystemConfig.trainFile, "train");

            Transform t2 = new Transform();
            t.analyze(FileSystemConfig.testFile, "test");

            WekaPredictor weka = new WekaPredictor();
            weka.train();
        }

    }
}
