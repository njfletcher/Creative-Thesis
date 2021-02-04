package com.StockPrediction;

import org.encog.util.csv.CSVFormat;
import java.io.File;

public class FileSystemConfig {

    /*
    For right now the training and testing/prediction datasets are routed through the project files. Only
    the title block is routed through personal files.
     */
    public static String baseDir = "C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\";
    public static File trainFile = new File("files\\stockReports_train.CSV");
    public static CSVFormat format = new CSVFormat('.' , ',');
    public static File testFile = new File("files\\stockReports_test.CSV");
    public static String fedFile = "files\\FED.XML";
    public static String analysisFile = "files\\Analysis.HTML";

}
