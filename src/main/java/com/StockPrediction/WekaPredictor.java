package com.StockPrediction;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SMOreg;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.M5P;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.io.IOException;

public class WekaPredictor {

    public void train() throws Exception {

        writeARFF(new File("files\\Processedtrain.CSV"), "train");
        writeARFF(new File("files\\Processedtest.CSV"), "test");

        ConverterUtils.DataSource source = new ConverterUtils.DataSource("files\\wekatrain.ARFF");
        Instances trainDataSet = source.getDataSet();

        trainDataSet.setClassIndex(trainDataSet.numAttributes()-1);

        for(int i =0; i<trainDataSet.numInstances();i++){
            System.out.println(trainDataSet.get(i).classValue());
        }

        SMOreg smo = new SMOreg();
        smo.buildClassifier(trainDataSet);

        M5P m5p = new M5P();
        m5p.buildClassifier(trainDataSet);

        System.out.println(smo);

        ConverterUtils.DataSource testSource = new ConverterUtils.DataSource("files\\wekatest.ARFF");

        Instances testDataSet = testSource.getDataSet();

        testDataSet.setClassIndex(testDataSet.numAttributes()-1);

        int numRight =0;

        for(int i =0; i<testDataSet.numInstances()-1;i++){
            double actual = testDataSet.get(i).classValue();

            Instance newInst = testDataSet.get(i);

            double pred = smo.classifyInstance(newInst);
            //(smo.classifyInstance(newInst) + m5p.classifyInstance(newInst)) /2

            numRight += calcAccuracy(actual, pred, newInst);
            System.out.println(actual + ", " + pred);

        }
        System.out.println(numRight + "/ " + testDataSet.numInstances());
    }
    public void writeARFF(File file, String dataType) throws IOException {
        CSVLoader loader = new CSVLoader();
        loader.setSource(file);
        Instances data = loader.getDataSet();//get instances object

        // save ARFF
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);//set the dataset we want to convert
        //and save as ARFF
        saver.setFile(new File("files\\weka" + dataType + ".ARFF"));
        saver.writeBatch();
    }
    public int calcAccuracy(double actual, double pred, Instance dataPoint){

        double stepBackOne = dataPoint.value(1);
        double actualChange = actual - stepBackOne;
        double predChange = pred - stepBackOne;

        //CALCULATE PERCENT CHANGE NOT JUST DOLLAR AMOUNT

        System.out.println(actualChange + ", " + predChange);
        //actualChange + ", " + predChange

        if(Math.signum(actualChange) == Math.signum(predChange)){
            return 1;
        }
        else{
            return 0;
        }

    }
}
