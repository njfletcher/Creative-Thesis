package com.StockPrediction;

import com.clearspring.analytics.util.Pair;
import org.apache.log4j.BasicConfigurator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
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


    public static void main(String[] args) throws Exception {

        /*BasicConfigurator.configure();
        boolean train= true;

        Scanner sc = new Scanner(System.in);

        System.out.println("Enter Company name: ");
        String compName = sc.next();

        System.out.println("Enter Company ticker: ");
        String ticker = sc.next();

        LocalDate dateTime =LocalDate.now();
        //train model if certain day of the month
        if(dateTime.getDayOfMonth() == 6){
            train =true;
        }else{
            train = false;
        }

        NewsSentiment news = new NewsSentiment(compName, ticker);
        news.getCompanyInfo();
        news.displayInfo();

        StockData stockD = new StockData(compName, ticker);
        EncogImplementation nn = new EncogImplementation();


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

         */

        BasicConfigurator.configure();


        Scanner sc = new Scanner(System.in);

        System.out.println("Enter Company name: ");
        String compName = sc.next();

        System.out.println("Enter Company ticker: ");
        String ticker = sc.next();
        NewsSentiment news = new NewsSentiment(compName, ticker);
        news.getCompanyInfo();
        news.displayInfo();

        news.getData();

        FileCreator stockD = new FileCreator(compName, ticker);



        stockD.createTrain(FileSystemConfig.trainFile, ticker);

        stockD.createPredData();

        Transform transform = new Transform();
        transform.analyze(new File(""));

        String file = "files\\Processedtrain.CSV";
        String symbol = "GOOG"; // stock name
        int batchSize = 64; // mini-batch size
        double splitRatio = 0.9; // 90% for training, 10% for testing
        int epochs = 100; // training epochs


        PriceCategory category = PriceCategory.CHANGE; // CLOSE: predict close price
        StockIterator iterator = new StockIterator(file, symbol, batchSize, 22, splitRatio, category);

        List<Pair<INDArray, INDArray>> test = iterator.getTestDataSet();


        MultiLayerNetwork net = NetworkModel.buildNetwork(iterator.inputColumns(), iterator.totalOutcomes());


        for (int i = 0; i < epochs; i++) {
            while (iterator.hasNext()) net.fit(iterator.next()); // fit model using mini-batch data
            iterator.reset(); // reset iterator
            net.rnnClearPreviousState(); // clear previous state
        }

    }


}
