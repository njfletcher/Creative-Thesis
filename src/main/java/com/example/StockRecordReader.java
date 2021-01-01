package com.example;


import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.records.reader.impl.transform.TransformProcessRecordReader;
import org.datavec.api.records.reader.impl.transform.TransformProcessSequenceRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.NumberedFileInputSplit;
import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.schema.Schema;
import org.nd4j.linalg.dataset.DataSet;

import java.io.File;


public class StockRecordReader {

    int numLinesSkip = 0;
    private String cName;
    private String cTick;
    public SequenceRecordReader transformTrainProcessRecordReader;
    public SequenceRecordReader transformTestProcessRecordReader;


    /*
Example of pulled data:

Tesla(TSLA)
Tue Dec 01 00:00:00 CST 2015,47.438,18670000

datetime, closing price, volume
     */

    public StockRecordReader(String companyName, String Tick){
        cName = companyName;
        cTick = Tick;
    }

    public void readReport() throws Exception {

        //Builds schema and transform procees, removes dateTime column
        Schema inputSchema = new Schema.Builder()
                .addColumnsString("datetime")
                .addColumnDouble("close")
                .addColumnDouble("Volume")
                .addColumnsDouble( "high", "low")//add news data later.
                .build();
        TransformProcess tp = new TransformProcess.Builder(inputSchema)
                .removeColumns("datetime")
                .build();

        //Prints column layout after transform procees
        int numActions = tp.getActionList().size();
        for(int i = 0; i <numActions; i++){
            System.out.println("\n\n==================");
            System.out.println("------------Schema after step" + i + "(" + tp.getActionList().get(i) + ")");
            System.out.println(tp.getSchemaAfterStep(i));
            System.out.println(" ");
        }

        File directoryToLook = new File("C:\\Users\\Nicholas\\Desktop\\STOCKPRACTICE\\");
        File inputTrainFile = new File(directoryToLook, "stockReports_train.csv");

        File inputTestFile = new File(directoryToLook, "stockReports_test.csv");

        //Define input reader and output writer:
        SequenceRecordReader rrTrain = new CSVSequenceRecordReader(1, ",");
        rrTrain.initialize(new FileSplit(inputTrainFile));
        transformTrainProcessRecordReader = new TransformProcessSequenceRecordReader(rrTrain,tp);



        SequenceRecordReader rrTest = new CSVSequenceRecordReader(1, ",");
        rrTest.initialize(new FileSplit(inputTestFile));
        transformTestProcessRecordReader = new TransformProcessSequenceRecordReader(rrTest,tp);


    }

    public SequenceRecordReader getTrainReader(){
        return transformTrainProcessRecordReader;
    }
    public SequenceRecordReader getTestReader(){
        return transformTestProcessRecordReader;
    }
}

