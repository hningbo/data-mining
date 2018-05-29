package edu.rylynn.datamining.core.cluster;

import java.util.List;

public class KMeans {
    private List<List<Double>> data;
    private int k;
    public KMeans(List<List<Double>> data, int k){
        this.data = data;
        this.k = k;
    }

    private class Point{
        private List<Double> data;
        Point(List<Double> data){
            this.data = data;
        }

        double distanceTo(Point anotherPoint){
            double distance = 0.0;
            for(int i = 0; i<data.size(); i++)
            {
                distance += Math.pow(this.data.get(i) - anotherPoint.data.get(i),2);
            }
            return Math.sqrt(distance);
        }
    }
}
