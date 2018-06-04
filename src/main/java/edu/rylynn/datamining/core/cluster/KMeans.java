package edu.rylynn.datamining.core.cluster;

import java.util.*;

public class KMeans {
    private List<List<Double>> data;
    private List<List<Double>> centers;
    private List<Set<Integer>> clusters;
    private int maxIteration;
    private int k;

    public KMeans(List<List<Double>> data, int k, int maxIteration) {
        this.data = data;
        this.k = k;
        this.maxIteration = maxIteration;
        this.centers = new ArrayList<>();
        this.clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            this.clusters.add(new HashSet<Integer>());
        }
    }

    public static void main(String[] args) {
        List<List<Double>> data = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            List<Double> doubles = new ArrayList<>();
            doubles.add(1.0+i);
            doubles.add(2.0+i);
            doubles.add(3.0+i);
            data.add(doubles);
        }
        new KMeans(data, 2, 100).kmeans();
    }

    private double distance(List<Double> a, List<Double> b) {
        if (a.size() == 0 || a.size() != b.size()) {
            return 0.0;
        }
        double distance = 0.0;
        for (int i = 0; i < a.size(); i++) {
            distance += Math.pow(a.get(i) - b.get(i), 2);
        }
        return Math.sqrt(distance / a.size());
    }

    private List<Double> add(List<Double> a, List<Double> b) {
        if (a.size() == 0 || a.size() != b.size()) {
            return a;
        }
        for (int i = 0; i < a.size(); i++) {
            a.set(i, a.get(i) + b.get(i));

        }
        return a;
    }

    //k-means++
    private void init() {
        Random random = new Random();
        int firstPoint = random.nextInt(this.data.size() - 1);
        centers.add(data.get(firstPoint));
        int index = 1;
        while (index++ < k) {
            double furtherDistance = -1.0;
            List<Double> nextPoint = new ArrayList<>();
            for (List<Double> d : data) {
                double tempDistance = 0.0;
                for (List<Double> c : centers) {
                    tempDistance += distance(c, d);
                }
                if (tempDistance >= furtherDistance) {
                    furtherDistance = tempDistance;
                    nextPoint = d;
                }
            }
            centers.add(nextPoint);
        }


        for (int j = 0; j < data.size(); j++) {
            List<Double> d = data.get(j);
            int minIndex = 0;
            double minDistance = 9999999;
            for (int i = 1; i < this.k; i++) {
                double thisDistance = distance(centers.get(i), d);
                if (thisDistance <= minDistance) {
                    minIndex = i;
                    minDistance = thisDistance;
                }
            }
            clusters.get(minIndex).add(j);
        }
    }

    //if the center does not change, algorithm finish
    private boolean clusterFinish(List<Set<Integer>> oldClusters, List<Set<Integer>> newClusters) {
        for (Set<Integer> oldCluster : oldClusters) {
            for (Set<Integer> newCluster : newClusters) {
                Iterator iter2 = newCluster.iterator();

                boolean isFullEqual = true;

                while (iter2.hasNext()) {
                    if (!oldCluster.contains(iter2.next())) {
                        isFullEqual = false;
                    }
                }
                if (!isFullEqual) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<Set<Integer>> kmeans() {
        init();
        while (maxIteration-- != 0) {
            List<List<Double>> newCenters = new ArrayList<>();
            List<Set<Integer>> newClusters = new ArrayList<>();
            for (int i = 0; i < this.k; i++) {
                newClusters.add(new HashSet<Integer>());
            }

            for (Set<Integer> oldCluster : clusters) {
                List<Double> tempCenter = new ArrayList<>();
                for (int i = 0; i < centers.get(0).size(); i++) {
                    tempCenter.add(0.0);
                }
                for (Integer oldIndex : oldCluster) {
                    List<Double> oldPoint = data.get(oldIndex);
                    tempCenter = add(tempCenter, oldPoint);
                }
                for (int i = 0; i < tempCenter.size(); i++) {
                    tempCenter.set(i, tempCenter.get(i) / (double) tempCenter.size());
                }
                newCenters.add(tempCenter);
            }

            for (int j = 0; j < data.size(); j++) {
                List<Double> d = data.get(j);
                int minIndex = 0;
                double minDistance = distance(d, newCenters.get(0));
                for (int i = 1; i < this.k; i++) {
                    double thisDistance = distance(newCenters.get(i), d);
                    if (thisDistance <= minDistance) {
                        minIndex = i;
                        minDistance = thisDistance;
                    }
                }
                newClusters.get(minIndex).add(j);
            }

            if (clusterFinish(newClusters, clusters)) {
                clusters = newClusters;
                break;
            } else {
                clusters = newClusters;
            }
        }
        printClusters();
        return clusters;

    }

    public void printClusters() {
        for (int i = 0; i < this.k; i++) {
            System.out.print("Cluster " + i + " : ");
            for (Integer index : clusters.get(i)) {
                System.out.print(index + " ");
            }
            System.out.println();
            System.out.print("Center Point : ");
            for (double value: centers.get(i)){
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }
}
