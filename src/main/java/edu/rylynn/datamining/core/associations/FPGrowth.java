package edu.rylynn.datamining.core.associations;

import edu.rylynn.datamining.core.associations.common.ItemSet;

import java.util.*;

public class FPGrowth {
    private double minSupport;
    private double minConfidence;
    private int size;
    private List<Integer> sortedIndex;
    private List<TreeNode> itemTable;
    private Map<String, Integer> itemIndex;
    private List<String[]> transaction;
    private Map<ItemSet, Integer> frequentItemSet;
    private TreeNode fpTree;

    public FPGrowth(double minSupport, double minConfidence, List<String> data) {
        this.minSupport = minSupport;
        this.minConfidence = minConfidence;
        this.frequentItemSet = new HashMap<>();
        this.sortedIndex = new ArrayList<>();
        this.transaction = new ArrayList<>();
        this.itemTable = new ArrayList<>();
        this.itemIndex = new HashMap<>();
        this.size = data.size();
        fpTree = new TreeNode(-1, 0, null);
        for (String line : data) {
            transaction.add(line.split(","));
        }
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("a,b,c,d");
        list.add("c,d,e,f");
        list.add("c,d,e,f");
        list.add("c,d,e,f");
        list.add("c,d,e,f");
        new FPGrowth(0.5, 0.7, list).buildTree();


    }

    public void firstScan() {
        int index = 1;
        for (String[] line : transaction) {
            for (int i = 0; i < line.length; i++) {
                if (itemIndex.containsKey(line[i])) {
                    int thisIndex = itemIndex.get(line[i]);
                    TreeNode node = itemTable.get(thisIndex - 1);
                    node.count++;
                } else {
                    itemTable.add(new TreeNode(index, 1, null));
                    itemIndex.put(line[i], index++);
                }
            }
        }
        Collections.sort(itemTable, new Comparator<TreeNode>() {
            public int compare(TreeNode t1, TreeNode t2) {
                return t2.count - t1.count;
            }
        });
        for (TreeNode tableNode : itemTable) {
            if (tableNode.count < this.size * this.minSupport) {
                itemTable.remove(tableNode);
            }
        }
    }

    public void buildTree() {
        firstScan();
        System.out.println(itemTable);
        for (String[] line : transaction) {
            TreeNode node = fpTree;
            List<Integer> thisLineIndex = new ArrayList<>();
            for (int i = 0; i < line.length; i++) {
                thisLineIndex.add(itemIndex.get(line[i]));
            }
            for (TreeNode tableNode : itemTable) {
                if (thisLineIndex.contains(tableNode.index)) {
                    TreeNode newNode = node.addNode(tableNode.index);
                    newNode.previousNode = node.previousNode;
                    node.previousNode = newNode;
                    node = newNode;   //TODO: there may be something wrong...
                }
            }
        }


    }

    public void getFrequentItemSetFromTree() {
        buildTree();
        for (int i = itemTable.size(); i >= 1; i--) {
            int index = itemTable.get(i).index;
            TreeNode thisNode = itemTable.get(index);
            Map<List<Integer>, Integer> fpPattern = new HashMap<>();
            while (thisNode.previousNode != null) {
                TreeNode treeTail = thisNode.previousNode;
                List<Integer> fpItems = new ArrayList<>();
                int count = treeTail.count;
                treeTail = treeTail.parent;
                while (treeTail.parent.index != -1) {
                    fpItems.add(treeTail.index);
                    treeTail = treeTail.parent;
                }

                fpPattern.put(fpItems, count);
            }

        }
    }

    private class TreeNode {
        int index;
        int count;
        List<TreeNode> childs;
        TreeNode parent;
        TreeNode previousNode;

        TreeNode(int index, int count, TreeNode parent) {
            this.index = index;
            this.count = count;
            this.childs = new ArrayList<>();
            this.parent = parent;
            this.previousNode = null;
        }

        TreeNode addNode(int index) {
            for (TreeNode child : childs) {
                if (child.index == index) {
                    child.count++;
                    return child;
                }
            }
            TreeNode newNode = new TreeNode(index, 1, this);
            this.childs.add(newNode);
            return newNode;
        }

        @Override
        public String toString() {
            return (index + " : " + count);
        }
    }
}