package edu.rylynn.datamining.core.associations;

import edu.rylynn.datamining.core.associations.common.ItemSet;

import java.util.*;

public class FPGrowth {
    private int minSupport;
    private int minConfidence;
    private List<Map.Entry<String, Integer>> itemCount;
    private Map<String, Integer> itemIndex;
    private List<String[]> transaction;
    private Map<ItemSet, Integer> frequentItemSet;
    private TreeNode fpTree;

    public FPGrowth(int minSupport, int minConfidence, List<String> data) {

        this.minSupport = minSupport;
        this.minConfidence = minConfidence;
        this.frequentItemSet = new HashMap<>();
        transaction = new ArrayList<>();
        itemCount = new ArrayList<>();
        itemIndex = new HashMap<>();

        for (String line : data) {
            transaction.add(line.split(","));
        }

        //fpTree = new TreeNode(-1, 0, null, null);
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("a,b,c,d");
        list.add("c,d,e,f");
        list.add("c,d,e,f");
        list.add("c,d,e,f");
        list.add("c,d,e,f");
        new FPGrowth(2, 2, list).firstScan();


    }

    public void firstScan() {
        int index = 1;
        Map<String, Integer> tempItemCount = new HashMap<>();
        for (String[] line : transaction) {
            for (int i = 0; i < line.length; i++) {
                if (tempItemCount.containsKey(line[i])) {
                    tempItemCount.put(line[i], tempItemCount.get(line[i]) + 1);
                } else {
                    tempItemCount.put(line[i], 1);
                    itemIndex.put(line[i], index++);
                }
            }
        }
        itemCount = new ArrayList<Map.Entry<String, Integer>>(tempItemCount.entrySet());
        Collections.sort(itemCount, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return -o1.getValue().compareTo(o2.getValue());
            }
        });


    }

    public void buildTree() {
        for (String[] line : transaction) {
            for (int i = 0; i < line.length; i++) {
                int index = itemIndex.get(line[i]);
            }
        }
    }

    public void getFrequentItemSetFromTree() {

    }

    private class TreeNode {
        int index;
        int count;
        List<TreeNode> next;
        TreeNode previousNode;

        public TreeNode(int index, int count, TreeNode previousNode) {
            this.index = index;
            this.count = count;
            this.next = new ArrayList<>();
            this.previousNode = null;
        }

        public void addNode() {

        }
    }

}

