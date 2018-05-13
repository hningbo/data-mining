package edu.rylynn.datamining.core.associations;

import edu.rylynn.datamining.core.associations.common.ItemSet;

import java.util.*;

public class FPGrowth {
    private double minSupport;
    private double minConfidence;
    private List<Map.Entry<String, Integer>> itemCount;
    private Map<String, Integer> itemIndex;
    private List<String[]> transaction;
    private Map<ItemSet, Integer> frequentItemSet;
    private TreeNode fpTree;

    public FPGrowth(double minSupport, double minConfidence, List<String> data) {

        this.minSupport = minSupport;
        this.minConfidence = minConfidence;
        this.frequentItemSet = new HashMap<>();
        transaction = new ArrayList<>();
        itemCount = new ArrayList<>();
        itemIndex = new HashMap<>();
        fpTree = new TreeNode(-1, 0, null);

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
        new FPGrowth(0.5, 0.7, list).firstScan();


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
            TreeNode node = fpTree;
            for (int i = 0; i < line.length; i++) {
                int index = itemIndex.get(line[i]);
                node = node.addNode(index);
            }
        }
    }

    public void getFrequentItemSetFromTree() {

    }

    private class TreeNode {
        int index;
        int count;
        List<TreeNode> childs;
        TreeNode parent;
        TreeNode previousNode;

        public TreeNode(int index, int count, TreeNode parent) {

            this.index = index;
            this.count = count;
            this.childs = new ArrayList<>();
            this.parent = parent;
            this.previousNode = null;
        }

        public TreeNode addNode(int index){
            for(TreeNode child: childs){
                if(child.index == index){
                    child.count++;
                    return child;
                }
            }
            TreeNode newNode = new TreeNode(index, 1, this.parent);
            this.parent.childs.add(newNode);
            return newNode;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(index+" : " + count);
            return sb.toString();
        }
    }

}


