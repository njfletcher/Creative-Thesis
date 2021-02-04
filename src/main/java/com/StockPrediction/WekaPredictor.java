package com.StockPrediction;

import org.apache.spark.sql.sources.In;
import weka.classifiers.functions.*;
import weka.classifiers.functions.supportVector.*;
import weka.classifiers.lazy.IBk;
import weka.classifiers.pmml.consumer.Regression;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.M5P;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
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

        /*for(int i =0; i<trainDataSet.numInstances();i++){
            System.out.println(trainDataSet.get(i).classValue());
        }
        */

        SMOreg smo = new SMOreg();
        smo.buildClassifier(trainDataSet);

        System.out.println(smo);

        ConverterUtils.DataSource testSource = new ConverterUtils.DataSource("files\\wekatest.ARFF");

        Instances testDataSet = testSource.getDataSet();

        testDataSet.setClassIndex(testDataSet.numAttributes()-1);

        int numRight =0;
        double magRight= 0;
        double magWrong =0;


        for(int i =0; i<testDataSet.numInstances()-1;i++){
            double actual = testDataSet.get(i).classValue();


            Instance newInst = testDataSet.get(i);


            double pred = smo.classifyInstance(newInst);

            double stepBackOne = newInst.value(2);
            double actualChange = actual - stepBackOne;
            double predChange = pred - stepBackOne;


            if(Math.signum(actualChange) == Math.signum(predChange)){
                numRight++;
                if(actualChange>predChange){
                    magRight += actualChange -predChange;
                }else{
                    magRight += predChange - actualChange;
                }
            }else{
                if(actualChange>predChange){
                    magWrong += actualChange -predChange;
                }
                else{
                    magWrong += predChange - actualChange;
                }
            }
        }
        System.out.println("Predictions right vs wrong: " + numRight + "/" + testDataSet.numInstances());
        System.out.println("AVG MAG OF RIGHT: " + magRight / numRight);
        System.out.println("AVG MAG OF WRONG: " + magWrong /(testDataSet.numInstances() - numRight));


        //PREDICTION ONE DAY AHEAD, USING LAST KNOWN DATAPOINT(USUALLY PREVIOUS DAY)
        System.out.println("Prediction for next day: " + predictAhead(smo, testDataSet));
        


    }
    public void writeARFF(File file, String dataType) throws IOException {
        CSVLoader loader = new CSVLoader();
        loader.setSource(file);
        Instances data = loader.getDataSet();//get instances object

        // saves ARFF(Datatype Weka can understand)
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);//set the dataset we want to convert
        //and save as ARFF
        saver.setFile(new File("files\\weka" + dataType + ".ARFF"));
        saver.writeBatch();
    }



    //makes a prediction one step ahead
    public double predictAhead(SMOreg smo, Instances testData) throws Exception {
        Instance testInstance = testData.lastInstance();

        return smo.classifyInstance(testInstance);
    }
}
