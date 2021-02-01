package com.StockPrediction;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.records.reader.impl.transform.TransformProcessRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.NumberedFileInputSplit;
import org.datavec.api.transform.transform.doubletransform.MinMaxNormalizer;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.jfree.data.general.Dataset;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.*;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.io.*;



public class Dl4jModel {

    private File outputPath;
    private int labelIndex = 0;
    private int miniBatchSize = 52;
    private static final Logger log = LoggerFactory.getLogger(Dl4jModel.class);


    public void train() throws Exception {
        RecordReader trainReader = new CSVRecordReader(0, ",");
        trainReader.initialize(new FileSplit(new File("files\\Processedtrain.CSV")));

        //numPossible labels not used since regression.
        DataSetIterator trainIter = new RecordReaderDataSetIterator(trainReader, miniBatchSize, 7, 7, true);

        /*while(trainIter.hasNext()){
            System.out.println(trainIter.next());
            System.out.println(" ");
        }

         */

        /*SequenceRecordReader testReader = new CSVSequenceRecordReader(0, ",");
        testReader.initialize(new FileSplit(FileSystemConfig.testFile));
        DataSetIterator testIter = new SequenceRecordReaderDataSetIterator(testReader, miniBatchSize, -1, 0, true);
        */



        DataNormalization dataNormalization = new NormalizerMinMaxScaler(-1,1);

        dataNormalization.fitLabel(true);
        dataNormalization.fit(trainIter);

        //testIter.setPreProcessor(dataNormalization);
        trainIter.setPreProcessor(dataNormalization);



        DataSet trainData = trainIter.next();
        //DataSet testData = testIter.next();

        trainIter.reset();
        //testIter.reset();


        NetworkModel modelBuilder = new NetworkModel();
        MultiLayerNetwork model = modelBuilder.buildNetwork(7,1);


        int numEpochs = 200;
        model.fit(trainIter, numEpochs);

        //model.output();

        /*INDArray timeSeriesFeatures = trainData.getFeatures();
        INDArray timeSeriesOutput = model.output(timeSeriesFeatures);

        dataNormalization.revertLabels(timeSeriesOutput);
        dataNormalization.revert(trainData);

        compareResults(timeSeriesOutput, trainData);

         */

        /*ModelSerializer.writeModel(model, new File("C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\model.txt"),true );
        FileOutputStream fos = new FileOutputStream(new File(FileSystemConfig.normFile));
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(dataNormalization);
        oos.close();

         */
    }

    //makes batch of predictions using test dataset.
    public void makePredictionSet() throws IOException, InterruptedException, ClassNotFoundException {

        MultiLayerNetwork net2 = MultiLayerNetwork.load(new File("C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\model.txt"), true);

        SequenceRecordReader testReader = new CSVSequenceRecordReader(1, ",");
        testReader.initialize(new FileSplit(new File("C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\stockReports_test.CSV")));
        DataSetIterator testIter = new SequenceRecordReaderDataSetIterator(testReader, miniBatchSize, -1, 0, true);

        FileInputStream fi = new FileInputStream(new File(FileSystemConfig.normFile));
        ObjectInputStream oi = new ObjectInputStream(fi);

        NormalizerMinMaxScaler standardize = (NormalizerMinMaxScaler) oi.readObject();



        testIter.setPreProcessor(standardize);

        DataSet testData = testIter.next();


        INDArray timeSeriesFeatures1 = testData.getFeatures();
        INDArray timeSeriesOutput1 = net2.output(timeSeriesFeatures1);
        standardize.revertLabels(timeSeriesOutput1);
        standardize.revert(testData);
        System.out.println(testData.getLabels());
        System.out.println(timeSeriesOutput1);

        INDArray timeSeriesFeatures = testData.getFeatures();
        INDArray timeSeriesOutput = net2.output(timeSeriesFeatures);
        standardize.revertLabels(timeSeriesOutput);
        long timeSeriesLength = timeSeriesOutput.size(2);        //Size of time dimension
        INDArray lastTimeStepProbabilities = timeSeriesOutput.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength-1));

        compareResults(timeSeriesOutput1, testData);

        System.out.println("Prediction for next day: " + lastTimeStepProbabilities);

    }

    public void compareResults(INDArray predictedArray, DataSet dataset){
        int count =0;

        double[] timesteps = new double[(int)predictedArray.size(2)];
        for(int i =0; i < (int)predictedArray.size(2); i++){
            timesteps[i] = (double)i;
        }

        double[] predicted = new double[(int)predictedArray.size(2)];
        double[] actual = new double[(int)predictedArray.size(2)];

        for(int i =0; i<predictedArray.size(2);i++){

            System.out.println(predictedArray.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(i)).getDouble() + ", " + dataset.getLabels().get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(i)).getDouble());
            if(Math.signum(predictedArray.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(i)).getDouble()) == Math.signum(dataset.getLabels().get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(i)).getDouble())){
                count++;
            }
            predicted[i] = predictedArray.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(i)).getDouble();
            actual[i] = dataset.getLabels().get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(i)).getDouble();

        }
        System.out.println(" ");
        System.out.println(count);
        System.out.println("total: " + predictedArray.size(2));
        XYChart chart = QuickChart.getChart("Daily percent change", "time-steps", "%change", "y(x)", timesteps, predicted);
        chart.addSeries("actual", timesteps, actual);

        // Show it
        new SwingWrapper(chart).displayChart();

    }

}