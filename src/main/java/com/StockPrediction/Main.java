package com.StockPrediction;



import org.apache.commons.math3.util.Pair;
import org.apache.log4j.BasicConfigurator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.common.io.ClassPathResource;

import org.nd4j.linalg.api.ndarray.INDArray;



import java.io.File;
import java.util.List;
import java.util.Scanner;




/* Made by Nicholas Fletcher
---------------------------------------------------------------------
 This program uses a Neural Network to predict the movement of a selected stock over a daily period.
 It is trained using a large time series dataset of a stock, which has two variables involved: %change and volume.
 In the future news sentiment index will be a third variable
 */


public class Main {

    private static int exampleLength = 22;


    public static void main(String[] args) throws Exception {

        //BasicConfigurator.configure();


        Scanner input = new Scanner(System.in);
        boolean running = true;

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
