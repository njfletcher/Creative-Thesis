package com.example;

import org.encog.ConsoleStatusReportable;
import org.encog.Encog;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
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
import org.encog.util.simple.TrainingSetUtil;

import java.io.*;
import java.util.Arrays;

public class NEURAL {

    static File filename = new File("C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\stockReports_train.CSV");
    CSVFormat format = new CSVFormat('.' , ',');
    String modelFilePath = "C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\model.EG";
    String baseDir = "C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\";

    int windowSize = 10;

    public void train() throws IOException {


        VersatileDataSource dataSource = new CSVDataSource(filename, false, format );
        VersatileMLDataSet trainData = new VersatileMLDataSet(dataSource);
        trainData.getNormHelper().setFormat(format);
        ColumnDefinition columnPerChange = trainData.defineSourceColumn("%Change", 1, ColumnType.continuous);
        ColumnDefinition columnVol = trainData.defineSourceColumn("Volume", 2, ColumnType.continuous);

        trainData.analyze();

        trainData.defineInput(columnPerChange);
        trainData.defineInput(columnVol);
        trainData.defineOutput(columnPerChange);
        EncogModel model = new EncogModel(trainData);
        model.selectMethod(trainData, MLMethodFactory.TYPE_FEEDFORWARD);

        model.setReport(new ConsoleStatusReportable());
        trainData.normalize();


        trainData.setLeadWindowSize(1);
        trainData.setLagWindowSize(windowSize);

        model.holdBackValidation(.2,false,1001);
        model.selectTrainingType(trainData);

        MLRegression bestMethod = (MLRegression) model.crossvalidate(10,false);


       System.out.println("Training Error: " + model.calculateError(bestMethod, model.getTrainingDataset()));
       System.out.println("Validation Error: " + model.calculateError(bestMethod, model.getValidationDataset()));

        NormalizationHelper norm = trainData.getNormHelper();
        System.out.println(norm.toString());
        System.out.println("Final Model: " + bestMethod);

        EncogDirectoryPersistence.saveObject(new File(baseDir + "model.EG"), bestMethod);
        SerializeObject.save(new File(baseDir + "norm.txt"), norm);

        Encog.getInstance().shutdown();

    }
    public void test() throws IOException, ClassNotFoundException {


        BasicNetwork network = (BasicNetwork)EncogDirectoryPersistence.loadObject(new File(baseDir+ "model.EG"));
        FileInputStream fileIn = new FileInputStream(baseDir + "norm.txt");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        NormalizationHelper normLoad = (NormalizationHelper) in.readObject();

        File fileNameTest = new File("C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\stockReports_test.CSV");


        ReadCSV csv = new ReadCSV(fileNameTest, false, format);

        String[] line = new String[2];
        double[] slice = new double[2];
        VectorWindow window = new VectorWindow(windowSize+1);
        MLData input = normLoad.allocateInputVector(windowSize+1);
        int stopAfter = 302;
        int numRight =0;
        int numWrong = 0;


        while(csv.next() && stopAfter>0) {

            StringBuilder result = new StringBuilder();
            line[0] = csv.get(1);
            line[1] = csv.get(2);
            normLoad.normalizeInputVector(line, slice, true);
            if (window.isReady()) {

                window.copyWindow(input.getData(), 0);
                String correct = csv.get(1);
                MLData output = network.compute(input);
                String predicted = normLoad.denormalizeOutputVectorToString(output)[0];
                result.append(Arrays.toString(line));
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
            stopAfter--;


        }
        Encog.getInstance().shutdown();
        System.out.println("Prediction done! ");
        System.out.println("Number of predictions correct(matching same direction as actual): " + numRight);
        System.out.println("Number of predictions incorrect: " + numWrong);









    }

}
