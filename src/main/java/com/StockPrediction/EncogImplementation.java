package com.StockPrediction;

import org.encog.ConsoleStatusReportable;
import org.encog.Encog;
import org.encog.mathutil.error.ErrorCalculation;
import org.encog.mathutil.error.ErrorCalculationMode;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.model.EncogModel;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.arrayutil.VectorWindow;
import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;
import org.encog.util.obj.SerializeObject;

import java.io.*;
import java.util.Arrays;

/*
Much simpler feed forward network using Encog, but gives much better results.
 */

public class EncogImplementation {


    private int windowSize = 18;

    public void train() throws IOException {

        ErrorCalculation.setMode(ErrorCalculationMode.RMS);
        VersatileDataSource dataSource = new CSVDataSource(FileSystemConfig.trainFile, false, FileSystemConfig.format );
        VersatileMLDataSet trainData = new VersatileMLDataSet(dataSource);
        trainData.getNormHelper().setFormat(FileSystemConfig.format);
        ColumnDefinition columnPerChange = trainData.defineSourceColumn("%Change", 1, ColumnType.continuous);
        ColumnDefinition columnVol = trainData.defineSourceColumn("Volume", 2, ColumnType.continuous);

        trainData.analyze();

        trainData.defineInput(columnPerChange);
        trainData.defineInput(columnVol);
        trainData.defineOutput(columnPerChange);
        org.encog.ml.model.EncogModel model = new org.encog.ml.model.EncogModel(trainData);
        model.selectMethod(trainData, MLMethodFactory.TYPE_FEEDFORWARD);

        model.setReport(new ConsoleStatusReportable());
        trainData.normalize();

        trainData.setLeadWindowSize(1);
        trainData.setLagWindowSize(windowSize);

        model.holdBackValidation(0.25, false, 1001);
        model.selectTrainingType(trainData);

        MLRegression bestMethod = (MLRegression) model.crossvalidate(5, false);

        //final stats
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("Final Training Error: " + model.calculateError(bestMethod, model.getTrainingDataset()));
        System.out.println("Final Validation Error: " + model.calculateError(bestMethod, model.getValidationDataset()));

        NormalizationHelper norm = trainData.getNormHelper();
        System.out.println(norm.toString());
        System.out.println("Best model: " + bestMethod);
        System.out.println("-------------------------------------------------------------------------------");

        //save to files to be used with predictions
        EncogDirectoryPersistence.saveObject(new File(FileSystemConfig.baseDir + "model.EG"), bestMethod);
        SerializeObject.save(new File(FileSystemConfig.baseDir + "norm.txt"), norm);

        Encog.getInstance().shutdown();

    }

    /*
    This method can make a single prediction. It also makes a prediction foreach step in a dataset, while
    evaluating how many predictions which the network makes on a dataset are correct, meaning if they
    matched the direction of the actual timesteps.
     */
    public void predict() throws IOException, ClassNotFoundException {


        BasicNetwork network = (BasicNetwork) EncogDirectoryPersistence.loadObject(new File(FileSystemConfig.baseDir+ "model.EG"));
        FileInputStream fileIn = new FileInputStream(FileSystemConfig.baseDir + "norm.txt");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        NormalizationHelper normLoad = (NormalizationHelper) in.readObject();


        ReadCSV csvReader = new ReadCSV(FileSystemConfig.testFile, false, FileSystemConfig.format);

        String[] currentPair = new String[2];
        double[] slice = new double[2];
        VectorWindow window = new VectorWindow(windowSize+1);
        MLData input = normLoad.allocateInputVector(windowSize+1);
        int numRight =0;
        int numWrong = 0;
        double magWrong =0;
        double magRight= 0;

        while(csvReader.next() ) {

            StringBuilder result = new StringBuilder();
            currentPair[0] = csvReader.get(1);
            currentPair[1] = csvReader.get(2);
            normLoad.normalizeInputVector(currentPair, slice, true);

            //creates data window, then makes prediction on it.
            if (window.isReady()) {

                window.copyWindow(input.getData(), 0);
                String correct = csvReader.get(1);
                MLData output = network.compute(input);
                String predicted = normLoad.denormalizeOutputVectorToString(output)[0];
                result.append(Arrays.toString(currentPair));
                result.append(" −> predicted: ");
                result.append(predicted);
                result.append("(correct: ");
                result.append(correct);
                result.append(")");
                System.out.println(result.toString());
                double pred = Double.parseDouble(predicted);
                double corr = Double.parseDouble(correct);

                if(Math.signum(pred) == Math.signum(corr)){
                    numRight++;
                }
                else{
                    numWrong++;
                }
            }
            window.add(slice);

        }

        System.out.println("Prediction done! ");
        System.out.println("Number of predictions correct(matching same direction as actual): " + numRight);
        System.out.println("Number of predictions incorrect: " + numWrong);


        //Predicts a single timestep into the future. WHat is printed out is what is predicted to happen the next day.
        //Need to double check this uses the correct input window.
        if(!csvReader.next()){
            currentPair[0] = csvReader.get(1);
            currentPair[1] = csvReader.get(2);
            normLoad.normalizeInputVector(currentPair, slice, true);
            window.copyWindow(input.getData(), 0);
            MLData output = network.compute(input);
            String predicted = normLoad.denormalizeOutputVectorToString(output)[0];
            System.out.println("Next close Prediction: " + predicted);
        }

        Encog.getInstance().shutdown();

    }


    public void altTrain(){
        //research other models
    }

    public void altPrediction(){
        //research other models
    }

}
