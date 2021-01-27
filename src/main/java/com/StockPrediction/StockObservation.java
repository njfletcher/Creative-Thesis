package com.StockPrediction;

public class StockObservation {


    private double perChange;
    private double sentiment;

    public  StockObservation(){};

    public  StockObservation(double percentChange, double sentiment){
        //add volume?????
        perChange = percentChange;
        this.sentiment = sentiment;
    }


    public double getChange() { return perChange; }
    public void setChange(double percent) { this.perChange = percent; }

    public double getSentiment() { return sentiment; }
    public void setSentiment(double sent) { this.sentiment = sent; }

}
