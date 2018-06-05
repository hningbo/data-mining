package edu.rylynn.datamining.core.cluster;

import scala.Int;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DBScan {
    private double minPts;
    private double epsilon;
    private List<List<Double>> data;
    private List<Set<Integer>> clusters;
    private Set<Integer> noises;
    private boolean[] visited;


    public DBScan(double minPts, double epsilon, List<List<Double>> data) {
        this.minPts = minPts;
        this.epsilon = epsilon;
        this.data = data;
        visited = new boolean[data.size()];
        for (int i = 0; i < data.size(); i++) {
            visited[i] = false;
        }
        noises = new HashSet<>();
        clusters = new ArrayList<>();
    }

    public static void main(String[] args) {
        List<List<Double>> data = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            List<Double> doubles = new ArrayList<>();
            doubles.add(1.0 + i);
            doubles.add(2.0 + i);
            doubles.add(3.0 + i);
            data.add(doubles);
        }
        new DBScan(2, 2, data).dbScan();
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

    private void dbScanSearch(Set<Integer> cluster, Queue<Integer> candicate) {

        while(candicate.size() != 0){
            List<Integer> candicateCluster = new ArrayList<>();
            int nowIndex = candicate.element();
            visited[nowIndex] = true;
            for (List<Double> line : data) {
                int thisIndex = data.indexOf(line);
                if (visited[thisIndex] || nowIndex == thisIndex) {
                    continue;
                }
                double dis = distance(line, data.get(nowIndex));
                if (dis <= epsilon) {
                    candicateCluster.add(thisIndex);
                }
            }
            if(candicateCluster.size() < minPts){
                noises.add(nowIndex);
            }
            else {
                candicate.addAll(candicateCluster);
                cluster.add(nowIndex);
            }
            candicate.remove();
        }
    }

    private boolean allVisited() {
        for (boolean aVisited : visited) {
            if (!aVisited) {
                return false;
            }
        }
        return true;
    }

    public void dbScan() {
        while (!allVisited()) {
            Random random = new Random();
            int randomIndex = random.nextInt(data.size() - 1);
            while (visited[randomIndex]) {
                randomIndex = random.nextInt(data.size() - 1);
            }
            Set<Integer> cluster = new HashSet<>();
            Queue<Integer> candicate = new ConcurrentLinkedQueue<>();
            candicate.add(randomIndex);
            dbScanSearch(cluster, candicate);
            clusters.add(cluster);
        }
        printClusters();
        printNoises();
    }

    private void printClusters() {
        for (int i = 0; i < this.clusters.size(); i++) {
            System.out.print("Cluster " + i + " : ");
            for (Integer index : clusters.get(i)) {
                System.out.print(index + " ");
            }
            System.out.println();
        }
    }

    private void printNoises() {
        System.out.print("Noises: ");
        for (Integer index : noises) {
            System.out.print(index + " ");
        }
        System.out.println();
    }
}
