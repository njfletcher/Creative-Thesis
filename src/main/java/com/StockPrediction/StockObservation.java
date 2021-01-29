package com.StockPrediction;

public class StockObservation {


    private double perChange;
    private double sentiment;
    private String date;

    public  StockObservation(){};

    public  StockObservation(String date, double percentChange, double sentiment){
        //add volume?????
        this.date = date;
        perChange = percentChange;
        this.sentiment = sentiment;
    }


    public double getChange() { return perChange; }
    public void setChange(double percent) { this.perChange = percent; }

    public double getSentiment() { return sentiment; }
    public void setSentiment(double sent) { this.sentiment = sent; }

    public String getDate() { return date; }
    public void setDate(String Date) { this.date = Date; }

}
