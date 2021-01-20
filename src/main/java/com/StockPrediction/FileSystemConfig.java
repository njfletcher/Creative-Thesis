package com.StockPrediction;

import org.encog.util.csv.CSVFormat;

import java.io.File;

public class FileSystemConfig {

    /*
    For right now the training and testing/prediction datasets are routed through my local files.
    The serialized models and normalizers are also stored in my folder for use on new data.
     */

    public static File trainFile = new File("C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\stockReports_train.CSV");
    public static String numberedTrainFile = "C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\stockReports_train_";
    public static CSVFormat format = new CSVFormat('.' , ',');
    public static String modelFilePath = "C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\model.EG";
    public static String baseDir = "C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\";
    public static File testFile = new File("C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\stockReports_test.CSV");
    public static File exampleFile = new File("C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\test.CSV");
    public static String normFile = "C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\norm.TXT";

}
