package com.StockPrediction;

import au.com.bytecode.opencsv.CSVReader;
import avro.shaded.com.google.common.collect.ImmutableMap;


import org.apache.commons.math3.util.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class StockIterator implements DataSetIterator {

    /** category and its index */
    private final Map<PriceCategory, Integer> featureMapIndex = ImmutableMap.of(PriceCategory.CHANGE, 0, PriceCategory.SENTIMENT, 1);

    private final int VECTOR_SIZE = 2; // number of features for a stock data
    private int miniBatchSize; // mini-batch size
    private int exampleLength = 22; // default 22, say, 22 working days per month
    private int predictLength = 1; // default 1, say, one day ahead prediction

    /** minimal values of each feature in stock dataset */
    private double[] minArray = new double[VECTOR_SIZE];
    /** maximal values of each feature in stock dataset */
    private double[] maxArray = new double[VECTOR_SIZE];

    /** feature to be selected as a training target */
    private PriceCategory category;

    /** mini-batch offset */
    private LinkedList<Integer> exampleStartOffsets = new LinkedList<>();

    /** stock dataset for training */
    private List<StockObservation> train;
    /** adjusted stock dataset for testing */
    private List<Pair<INDArray, INDArray>> test;

    public StockIterator (String filename, String symbol, int miniBatchSize, int exampleLength, double splitRatio, PriceCategory category) {
        List<StockObservation> stockDataList = readStockDataFromFile(filename, symbol);
        this.miniBatchSize = miniBatchSize;
        this.exampleLength = exampleLength;
        this.category = category;
        int split = (int) Math.round(stockDataList.size() * splitRatio);
        train = stockDataList.subList(0, split);
        test = generateTestDataSet(stockDataList.subList(split, stockDataList.size()));
        initializeOffsets();
    }

    /** initialize the mini-batch offsets */
    private void initializeOffsets () {
        exampleStartOffsets.clear();
        int window = exampleLength + predictLength;
        for (int i = 0; i < train.size() - window; i++) { exampleStartOffsets.add(i); }
    }

    public List<Pair<INDArray, INDArray>> getTestDataSet() { return test; }

    public double[] getMaxArray() { return maxArray; }

    public double[] getMinArray() { return minArray; }

    public double getMaxNum (PriceCategory category) { return maxArray[featureMapIndex.get(category)]; }

    public double getMinNum (PriceCategory category) { return minArray[featureMapIndex.get(category)]; }

    @Override
    public DataSet next(int num) {
        if (exampleStartOffsets.size() == 0) throw new NoSuchElementException();
        int actualMiniBatchSize = Math.min(num, exampleStartOffsets.size());
        INDArray input = Nd4j.create(new int[] {actualMiniBatchSize, VECTOR_SIZE, exampleLength}, 'f');
        INDArray label;
        if (category.equals(PriceCategory.ALL)) label = Nd4j.create(new int[] {actualMiniBatchSize, VECTOR_SIZE, exampleLength}, 'f');
        else label = Nd4j.create(new int[] {actualMiniBatchSize, predictLength, exampleLength}, 'f');
        for (int index = 0; index < actualMiniBatchSize; index++) {
            int startIdx = exampleStartOffsets.removeFirst();
            int endIdx = startIdx + exampleLength;
            StockObservation curData = train.get(startIdx);
            StockObservation nextData;
            for (int i = startIdx; i < endIdx; i++) {
                int c = i - startIdx;
                input.putScalar(new int[] {index, 0, c}, (curData.getChange() - minArray[1]) / (maxArray[1] - minArray[1]));
                input.putScalar(new int[] {index, 1, c}, (curData.getSentiment() - minArray[1]) / (maxArray[1] - minArray[1]));

                nextData = train.get(i + 1);
                if (category.equals(PriceCategory.ALL)) {
                    label.putScalar(new int[] {index, 0, c}, (nextData.getChange()- minArray[1]) / (maxArray[1] - minArray[1]));
                    label.putScalar(new int[] {index, 1, c}, (nextData.getSentiment()- minArray[1]) / (maxArray[1] - minArray[1]));
                } else {
                    label.putScalar(new int[]{index, 0, c}, feedLabel(nextData));
                }
                curData = nextData;
            }
            if (exampleStartOffsets.size() == 0) break;
        }
        return new DataSet(input, label);
    }

    private double feedLabel(StockObservation data) {
        double value;
        switch (category) {
            case CHANGE: value = (data.getChange() - minArray[0]) / (maxArray[0] - minArray[0]); break;
            case SENTIMENT: value = (data.getSentiment() - minArray[1]) / (maxArray[1] - minArray[1]); break;

            default: throw new NoSuchElementException();
        }
        return value;
    }

    public int totalExamples() { return train.size() - exampleLength - predictLength; }

    @Override public int inputColumns() { return VECTOR_SIZE; }

    @Override public int totalOutcomes() {
        if (this.category.equals(PriceCategory.ALL)) return VECTOR_SIZE;
        else return predictLength;
    }

    @Override public boolean resetSupported() { return false; }

    @Override public boolean asyncSupported() { return false; }

    @Override public void reset() { initializeOffsets(); }

    @Override public int batch() { return miniBatchSize; }

    public int cursor() { return totalExamples() - exampleStartOffsets.size(); }

    public int numExamples() { return totalExamples(); }

    public void setPreProcessor(DataSetPreProcessor dataSetPreProcessor) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public DataSetPreProcessor getPreProcessor() { throw new UnsupportedOperationException("Not Implemented"); }

    public List<String> getLabels() { throw new UnsupportedOperationException("Not Implemented"); }

    @Override public boolean hasNext() { return exampleStartOffsets.size() > 0; }

    public DataSet next() { return next(miniBatchSize); }

    private List<Pair<INDArray, INDArray>> generateTestDataSet (List<StockObservation> stockDataList) {
        int window = exampleLength + predictLength;
        List<Pair<INDArray, INDArray>> test = new ArrayList<>();
        for (int i = 0; i < stockDataList.size() - window; i++) {
            INDArray input = Nd4j.create(new int[] {exampleLength, VECTOR_SIZE}, 'f');
            for (int j = i; j < i + exampleLength; j++) {
                StockObservation stock = stockDataList.get(j);
                input.putScalar(new int[] {j - i, 0}, (stock.getChange() - minArray[0]) / (maxArray[0] - minArray[0]));
                input.putScalar(new int[] {j - i, 1}, (stock.getSentiment() - minArray[1]) / (maxArray[1] - minArray[1]));

            }
            StockObservation stock = stockDataList.get(i + exampleLength);
            INDArray label;
            if (category.equals(PriceCategory.ALL)) {
                label = Nd4j.create(new int[]{VECTOR_SIZE}, 'f'); // ordering is set as 'f', faster construct
                label.putScalar(new int[] {0}, stock.getChange());
                label.putScalar(new int[] {1}, stock.getSentiment());

            } else {
                label = Nd4j.create(new int[] {1}, 'f');
                switch (category) {
                    case CHANGE: label.putScalar(new int[] {0}, stock.getChange()); break;
                    case SENTIMENT: label.putScalar(new int[] {0}, stock.getSentiment()); break;

                    default: throw new NoSuchElementException();
                }
            }
            test.add(new Pair<>(input, label));
        }
        return test;
    }

    //CHECKED: loads into Stockarraylist correctly
    private List<StockObservation> readStockDataFromFile (String filename, String symbol) {
        List<StockObservation> stockDataList = new ArrayList<>();
        try {
            for (int i = 0; i < maxArray.length; i++) { // initialize max and min arrays
                maxArray[i] = Double.MIN_VALUE;
                minArray[i] = Double.MAX_VALUE;
            }
            List<String[]> list = new CSVReader(new FileReader(filename)).readAll(); // load all elements in a list
            for (String[] arr : list) {
                double[] nums = new double[VECTOR_SIZE];
                for (int i = 0; i < arr.length-1; i++) {
                    nums[i] = Double.valueOf(arr[i+1]);
                    if (nums[i] > maxArray[i]) maxArray[i] = nums[i];
                    if (nums[i] < minArray[i]) minArray[i] = nums[i];
                }
                stockDataList.add(new StockObservation(arr[0],nums[0],nums[1]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return stockDataList;
    }
}
