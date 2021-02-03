package com.StockPrediction;


import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.records.reader.impl.regex.RegexLineRecordReader;
import org.datavec.api.records.reader.impl.transform.TransformProcessRecordReader;
import org.datavec.api.records.reader.impl.transform.TransformProcessSequenceRecordReader;
import org.datavec.api.records.writer.RecordWriter;
import org.datavec.api.records.writer.impl.csv.CSVRecordWriter;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.partition.NumberOfRecordsPartitioner;
import org.datavec.api.split.partition.Partitioner;
import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.analysis.DataAnalysis;
import org.datavec.api.transform.condition.ConditionOp;
import org.datavec.api.transform.condition.column.DoubleColumnCondition;
import org.datavec.api.transform.quality.DataQualityAnalysis;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.transform.transform.normalize.Normalize;
import org.datavec.api.transform.ui.HtmlAnalysis;
import org.datavec.api.writable.DoubleWritable;
import org.datavec.api.writable.Writable;
import org.datavec.local.transforms.AnalyzeLocal;
import org.datavec.local.transforms.LocalTransformExecutor;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Transform {

    public void analyze(File file, String data) throws Exception {


        // the String to label conversion. Define schema and transform:
        Schema schema = new Schema.Builder()
                .addColumnString("date")
                .addColumnsDouble("close1","close2","close3","sentiment", "label")
                //"volume",

                .build();

        System.out.println("*****************Starting Schema*****************");
        System.out.println(schema.toString());
        System.out.println("*************************************************");


        RecordReader trainReader = new CSVRecordReader();
        trainReader.initialize(new FileSplit(file));

        DataAnalysis analysis = AnalyzeLocal.analyze(schema, trainReader);
        HtmlAnalysis.createHtmlAnalysisFile(analysis, new File("Analysis" + data + ".HTML"));


        TransformProcess tp = new TransformProcess.Builder(schema)
                .removeColumns("date")
                //.normalize("close1", Normalize.MinMax,analysis)
                //.normalize("close2", Normalize.MinMax,analysis)
                //.normalize("sentiment", Normalize.MinMax,analysis)
                //.normalize("volume", Normalize.MinMax,analysis)
                //.normalize("label", Normalize.MinMax,analysis)

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
        PrintWriter printWriter = new PrintWriter(fileWriter);

        //Define input reader and output writer:
        RecordReader rr = new CSVRecordReader(1, ',');
        rr.initialize(new FileSplit(inputFile));

        RecordWriter rw = new CSVRecordWriter();
        Partitioner p = new NumberOfRecordsPartitioner();
        rw.initialize(new FileSplit(outputFile), p);



        //Process the data:
        List<List<Writable>> originalData = new ArrayList<>();

        while(rr.hasNext()){
            originalData.add(rr.next());
        }

        List<List<Writable>> processedData = LocalTransformExecutor.execute(originalData, tp);
        rw.writeBatch(processedData);
        rw.close();

        
    }

}
