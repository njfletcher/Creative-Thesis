package com.StockPrediction;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.*;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import java.io.File;
import java.io.IOException;


/*uses SequenceRecordReaders to create DataSetIterators for both the train and test sets, which are used as inputs
for Dl4j neural networks.
-This network does not give good results, use Encog instead.
 */
public class Dl4jModel {

    private File outputPath;
    private int labelIndex = 0;
    private int miniBatchSize = 50;
    private static final Logger log = LoggerFactory.getLogger(Dl4jModel.class);

    public void train() throws IOException, InterruptedException {

        SequenceRecordReader trainReader = new CSVSequenceRecordReader(0, ",");
        trainReader.initialize(new FileSplit(new File("C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\stockReports_train.CSV")));

        //numPossible labels not used since regression.
        DataSetIterator trainIter = new SequenceRecordReaderDataSetIterator(trainReader, miniBatchSize, -1, 0, true);

        SequenceRecordReader testReader = new CSVSequenceRecordReader(0, ",");
        testReader.initialize(new FileSplit(new File("C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\stockReports_test.CSV")));
        DataSetIterator testIter = new SequenceRecordReaderDataSetIterator(testReader, miniBatchSize, -1, 0, true);

        final DataNormalization dataNormalization = new NormalizerMinMaxScaler(-1,1);
        dataNormalization.fitLabel(true);
        dataNormalization.fit(trainIter);

        testIter.setPreProcessor(dataNormalization);
        trainIter.setPreProcessor(dataNormalization);

        DataSet trainData = trainIter.next();
        DataSet testData = testIter.next();

        trainIter.reset();
        testIter.reset();

        System.out.println(trainData.sample(1));
        System.out.println(" ");
        System.out.println(testData.sample(1));

        //Initialize the user interface backend
        UIServer uiServer = UIServer.getInstance();
        //Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
        StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later
        //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
        uiServer.attach(statsStorage);
        //Then add the StatsListener to collect this information from the network, as it trains

        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .miniBatch(true)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(.001))
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(0, new LSTM.Builder().activation(Activation.TANH).nIn(1).nOut(3).build())
                .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.TANH)
                        .nOut(1).build())
                .backpropType(BackpropType.TruncatedBPTT)
                .tBPTTLength(100)
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();

        //http://localhost:9000/train/overview
        model.setListeners(new StatsListener(statsStorage));

        int numEpochs = 500;
        for (int i = 0; i < numEpochs; i++) {
            model.fit(trainIter);

            log.info("Epoch " + i + " complete. Time series evaluation:");

            //Run regression evaluation on our single column input
            RegressionEvaluation evaluation = new RegressionEvaluation(1);
            INDArray features = testData.getFeatures();

            INDArray lables = testData.getLabels();
            INDArray predicted = model.output(testData.getFeatures());

            evaluation.evalTimeSeries(lables, predicted);

            //Just do sout here since the logger will shift the shift the columns of the stats
            System.out.println(evaluation.stats());
        }

        //revert this

        INDArray timeSeriesFeatures = testData.getFeatures();
        INDArray timeSeriesOutput = model.output(timeSeriesFeatures);
        dataNormalization.revertLabels(timeSeriesOutput);
        dataNormalization.revert(testData);
        System.out.println(testData.getLabels());
        System.out.println(timeSeriesOutput);

        //dataNormalization.revertFeatures(lastTimeStepProbabilities);



        /*INDArray predictedTrain = model.rnnTimeStep(trainData.getFeatures());
        INDArray predictedTest = model.rnnTimeStep(testData.getFeatures());
        dataNormalization.revert(trainData);
        dataNormalization.revert(testData);
        dataNormalization.revertLabels(predictedTest);
        dataNormalization.revertLabels(predictedTrain);
        System.out.println("RealTrain: " + trainData.getLabels());
        System.out.println("PredictedTrain: " + predictedTrain);
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("RealTest: " + testData.getLabels());
        System.out.println("PredictedTest: " + predictedTest);

         */
        ModelSerializer.writeModel(model, new File("C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\model.txt"),true );
    }


    //makes prediction using test dataset.
    public void makePrediction() throws IOException, InterruptedException {

        MultiLayerNetwork net2 = MultiLayerNetwork.load(new File("C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\model.txt"), true);

        SequenceRecordReader testReader = new CSVSequenceRecordReader(1, ",");
        testReader.initialize(new FileSplit(new File("C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\stockReports_test.CSV")));
        DataSetIterator testIter = new SequenceRecordReaderDataSetIterator(testReader, miniBatchSize, -1, 0, true);


        final DataNormalization dataNormalization = new NormalizerMinMaxScaler();
        dataNormalization.fitLabel(true);
        dataNormalization.fit(testIter);
        testIter.setPreProcessor(dataNormalization);

        DataSet testData = testIter.next();

        INDArray timeSeriesFeatures = testData.getFeatures();
        INDArray timeSeriesOutput = net2.output(timeSeriesFeatures);
        dataNormalization.revertLabels(timeSeriesOutput);
        long timeSeriesLength = timeSeriesOutput.size(2);        //Size of time dimension
        INDArray lastTimeStepProbabilities = timeSeriesOutput.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength-1));

        System.out.println(lastTimeStepProbabilities);

    }

}