package com.StockPrediction;


import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.records.writer.RecordWriter;
import org.datavec.api.records.writer.impl.csv.CSVRecordWriter;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.partition.NumberOfRecordsPartitioner;
import org.datavec.api.split.partition.Partitioner;
import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.writable.Writable;
import org.datavec.local.transforms.LocalTransformExecutor;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class Transform {

    public void analyze(File file, String data) throws Exception {


        //defines structure of the input file
        Schema inSchema = new Schema.Builder()
                .addColumnString("date")
                .addColumnsDouble("close1","close2","close3","sentiment", "label")
                .build();

        System.out.println("*****************Starting Schema*****************");
        System.out.println(inSchema.toString());
        System.out.println("*************************************************");


        RecordReader trainReader = new CSVRecordReader();
        trainReader.initialize(new FileSplit(file));




        //removes date column from datasets.
        TransformProcess tp = new TransformProcess.Builder(inSchema)
                .removeColumns("date")
                .build();

        System.out.println("*****************Final Schema*****************");
        System.out.println(tp.getFinalSchema().toString());
        System.out.println("**********************************************");

        File inputFile = file;
        File outputFile = new File("files\\Processed" + data + ".CSV");
        if(outputFile.exists()){
            outputFile.delete();
        }
        outputFile.createNewFile();
        FileWriter fileWriter = new FileWriter(outputFile);


        RecordReader rr = new CSVRecordReader(1, ',');
        rr.initialize(new FileSplit(inputFile));

        RecordWriter rw = new CSVRecordWriter();
        Partitioner p = new NumberOfRecordsPartitioner();
        rw.initialize(new FileSplit(outputFile), p);



        //Processes data
        List<List<Writable>> originalData = new ArrayList<>();

        while(rr.hasNext()){
            originalData.add(rr.next());
        }

        List<List<Writable>> processedData = LocalTransformExecutor.execute(originalData, tp);
        rw.writeBatch(processedData);
        rw.close();

        
    }

}
