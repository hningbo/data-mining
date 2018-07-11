package edu.rylynn.datamining.core.classifier;

import java.io.BufferedReader;
import java.io.FileReader;
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
    private List<String> trainY;
    private Map<String, List<NormalDistribution>> propertyDistribution;
    private Map<String, Integer> propertyProportion;    //占比
    private int propertiesNum = 0;

    public NaiveBayesian(List<List<Double>> trainX, List<String> trainY) {
        this.trainX = trainX;
        this.trainY = trainY;
        propertyDistribution = new HashMap<>();
        propertyProportion = new HashMap<>();
    }

    public static void main(String[] args) {
        List<List<Double>> trainX = new ArrayList<>();
        List<String> trainY = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("segment-challenge.arff"));
            String line = br.readLine();
            while (line != null) {
                List<Double> lineX = new ArrayList<>();
                String[] items = line.split(",");
                trainY.add(items[items.length - 1]);
                for (String item : items) {
                    if (item != items[items.length - 1]) {
                        lineX.add(Double.parseDouble(item));
                    }
                }
                trainX.add(lineX);
                line = br.readLine();
            }
            NaiveBayesian bayesian = new NaiveBayesian(trainX, trainY);
            bayesian.train();
            int count = 0;
            for (int k = 0; k < trainX.size(); k++) {
                if (bayesian.predict(trainX.get(k)).equals(trainY.get(k))) {
                    count++;
                }
            }
            System.out.println((double) count / 1500.0);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public List<List<Double>> getTrainX() {
        return trainX;
    }

    public void setTrainX(List<List<Double>> trainX) {
        this.trainX = trainX;
    }

    public List<String> getTrainY() {
        return trainY;
    }

    public void setTrainY(List<String> trainY) {
        this.trainY = trainY;
    }

    public void train() {
        int i = 0;
        Map<String, List<Double>> tempSum = new HashMap<>();
        Map<String, List<Double>> tempSquareSum = new HashMap<>();
        for (List<Double> line : trainX) {
            String clazz = trainY.get(i);
            if (!tempSum.containsKey(clazz)) {

                List<Double> m = new ArrayList<>();
                List<Double> s = new ArrayList<>();

                for (Double aLine : line) {
                    m.add(aLine);
                    s.add(aLine * aLine);
                }

                tempSum.put(clazz, m);
                tempSquareSum.put(clazz, s);
                propertyProportion.put(clazz, 1);

            } else {
                for (int j = 0; j < line.size(); j++) {
                    List<Double> m = new ArrayList<>();
                    List<Double> s = new ArrayList<>();

                    m.add(line.get(j));
                    s.add(line.get(j) * line.get(j));
                    tempSum.get(trainY.get(i)).
                            set(j, tempSum.get(clazz).get(j) + line.get(j));
                    tempSquareSum.get(trainY.get(i)).
                            set(j, tempSquareSum.get(clazz).get(j) + line.get(j) * line.get(j));

                }
                propertyProportion.put(clazz, 1 + propertyProportion.get(clazz));

            }
            i = i + 1;
        }


        for (Map.Entry<String, List<Double>> entry : tempSum.entrySet()) {
            List<Double> sum = entry.getValue();
            List<Double> squareSum = tempSquareSum.get(entry.getKey());
            List<NormalDistribution> nd = new ArrayList<>();
            int thisCount = propertyProportion.get(entry.getKey());
            for (int j = 0; j < sum.size(); j++) {
                double thisMean = sum.get(j) / thisCount;
                double thisVar = squareSum.get(j) / (thisCount) - ((sum.get(j) / thisCount) * (sum.get(j)) / thisCount);
                if(Math.abs(thisVar - 0) <= 0.00001){
                    thisVar = 0.001;
                }
                nd.add(new NormalDistribution(thisMean, thisVar));
                System.out.println(entry.getKey());
                System.out.println("mean: " + thisMean);
                System.out.println("var: " + thisVar);
                System.out.println("=================");
            }
            propertyDistribution.put(entry.getKey(), nd);
        }
    }

    public String predict(List<Double> predictX) {
        double prob = -1.00;
        String clazz = "";
        for (Map.Entry<String, List<NormalDistribution>> entry : propertyDistribution.entrySet()) {
            double thisClazzProb = probrobilityBelongThis(predictX, entry.getValue());
            //thisClazzProb *= (double) propertyProportion.get(entry.getKey());
            if (prob < thisClazzProb) {
                prob = thisClazzProb;
                clazz = entry.getKey();
            }
        }
        return clazz;
    }

    private double probrobilityBelongThis(List<Double> predictX, List<NormalDistribution> distributions) {
        double prob = 1.0;
        for (int i = 0; i < predictX.size(); i++) {
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

            return (1 / Math.sqrt(2 * Math.PI * variance)) * Math.exp(-((x - mean) * (x - mean) / (2 * variance)));
        }
    }
}
