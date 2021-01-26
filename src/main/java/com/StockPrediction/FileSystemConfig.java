package com.StockPrediction;

import org.encog.util.csv.CSVFormat;

import java.io.File;

public class FileSystemConfig {

    /*
    For right now the training and testing/prediction datasets are routed through my local files.
    The serialized models and normalizers are also stored in my folder for use on new data.
     */
    public static String baseDir = "C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\";
    public static File trainFile = new File(baseDir + "stockReports_train.CSV");
    public static CSVFormat format = new CSVFormat('.' , ',');
    public static File testFile = new File(baseDir + "stockReports_test.CSV");
    public static String normFile = baseDir + "norm.TXT";
    public static String fedFile = baseDir + "FED.XML";
    public static String analysisFile = baseDir + "Analysis.HTML";

}
