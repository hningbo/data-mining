package edu.rylynn.datamining.core.classifier;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Only implement the process in numerical property and does not suit in discrete property

Firstly calculate the mean and the variance of all the property in each class and we assume that all of
the property follow the **Normal Distribution**

Use max likelyhood to estimate which class the example belong to.

 */
public class NaiveBayesian {

    private List<List<Double>> trainX;
    private List<Double> trainY;
    private Map<String, List<NormalDistribution>> propertyDistribution;
    private int propertiesNum = 0;

    public NaiveBayesian(List<List<Double>> trainX, List<Double> trainY) {
        this.trainX = trainX;
        this.trainY = trainY;
        propertyDistribution = new HashMap<>();
    }

    public List<List<Double>> getTrainX() {
        return trainX;
    }

    public void setTrainX(List<List<Double>> trainX) {
        this.trainX = trainX;
    }

    public List<Double> getTrainY() {
        return trainY;
    }

    public void setTrainY(List<Double> trainY) {
        this.trainY = trainY;
    }


    public void train(List<List<Double>> trainX, List<String> trainY) {
        int i = 0;
        Map<String, List<Double>> tempSum = new HashMap<>();
        Map<String, List<Double>> tempSquareSum = new HashMap<>();

        for (List<Double> line : trainX) {

            for (int j = 0; j < line.size(); j++)
                if (i == 0) {
                    List<Double> m = new ArrayList<>();
                    List<Double> s = new ArrayList<>();
                    propertiesNum = line.size();
                    m.add(line.get(j));
                    s.add(line.get(j) * line.get(j));
                    tempSum.put(trainY.get(i), m);
                    tempSquareSum.put(trainY.get(i), s);
                } else {
                    if (!tempSum.containsKey(trainY.get(i)) && j == 0) {
                        List<Double> m = new ArrayList<>();
                        List<Double> s = new ArrayList<>();
                        m.add(line.get(j));
                        s.add(line.get(j) * line.get(j));
                        tempSum.put(trainY.get(i), m);
                        tempSquareSum.put(trainY.get(i), s);
                    } else {
                        tempSum.get(trainY.get(i)).
                                set(j, tempSum.get(trainY.get(i)).get(j) + line.get(j));
                        tempSquareSum.get(trainY.get(i)).
                                set(j, tempSquareSum.get(i).get(j) + line.get(j) * line.get(j));
                    }
                }
            i = i + 1;
        }
        for (Map.Entry<String, List<Double>> entry : tempSum.entrySet()) {
            List<Double> sum = entry.getValue();
            List<Double> squareSum = tempSquareSum.get(entry.getKey());
            List<NormalDistribution> nd = new ArrayList<>();
            for (int j = 0; j < sum.size(); j++) {
                nd.add(new NormalDistribution(sum.get(j) / i,
                        (squareSum.get(j) - sum.get(j) * sum.get(j)) / i));
            }
            propertyDistribution.put(entry.getKey(), nd);
        }
    }

    public String predict(List<Double> predictX) {
        double prob = -1.00;
        String clazz = "";
        for(Map.Entry<String, List<NormalDistribution>> entry: propertyDistribution.entrySet()){
            double thisClazzProb = probrobilityBelongThis(predictX, entry.getValue());
            if(prob < thisClazzProb)
            {
                prob = thisClazzProb;
                clazz = entry.getKey();
            }
        }
        return clazz;
    }

    private double probrobilityBelongThis(List<Double> predictX, List<NormalDistribution> distributions) {
        double prob = 1.0;
        for(int i = 0; i<predictX.size(); i++){
            prob *= distributions.get(i).getProbability(predictX.get(i));
        }
        return prob;
    }

    private class NormalDistribution {
        private double mean;
        private double variance;

        NormalDistribution(double mean, double variance) {
            this.mean = mean;
            this.variance = variance;
        }

        double getProbability(double x) {
            return (1 / Math.sqrt(2 * Math.PI)) * Math.exp(-((x - mean) * (x - mean) / (2 * variance)));
        }
    }
}
