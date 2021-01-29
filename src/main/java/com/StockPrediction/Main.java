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

        //Transform transform = new Transform();
        //transform.analyze(new File(""));

        String file = "files\\stockReports_train.CSV";
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


        File locationToSave = new File("files\\model".concat(String.valueOf(category)).concat(".zip"));
        // saveUpdater: i.e., the state for Momentum, RMSProp, Adagrad etc. Save this to train your network more in the future
        ModelSerializer.writeModel(net, locationToSave, true);


        net = ModelSerializer.restoreMultiLayerNetwork(locationToSave);

        double max = iterator.getMaxNum(category);
        double min = iterator.getMinNum(category);
        predictPriceOneAhead(net, test, max, min, category);


    }

    private static void predictPriceOneAhead (MultiLayerNetwork net, List<Pair<INDArray, INDArray>> testData, double max, double min, PriceCategory category) {
        double[] predicts = new double[testData.size()];
        double[] actuals = new double[testData.size()];
        for (int i = 0; i < testData.size(); i++) {
            predicts[i] = net.rnnTimeStep(testData.get(i).getKey()).getDouble(exampleLength - 1) * (max - min) + min;
            actuals[i] = testData.get(i).getValue().getDouble(0);
        }

        for (int i = 0; i < predicts.length; i++){
            System.out.println(predicts[i] + "," + actuals[i]);
        }

    }




}
