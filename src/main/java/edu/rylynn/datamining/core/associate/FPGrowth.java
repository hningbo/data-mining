package edu.rylynn.datamining.core.associate;

import java.util.*;

public class FPGrowth {

    private int minSupport;
    private int minConfidence;
    private List<Map.Entry<String, Integer>> itemCount;
    private Map<String, Integer> itemIndex;
    private List<String[]> itemData;
    private Map<ItemSet, Integer> frequentItemSet;
    private FPTree fpTree;

    public FPGrowth(int minSupport, int minConfidence, List<String> data) {

        this.minSupport = minSupport;
        this.minConfidence = minConfidence;
        this.frequentItemSet = new HashMap<>();
        itemData = new ArrayList<>();
        itemCount = new ArrayList<>();
        itemIndex = new HashMap<>();

        for (String line : data) {
            itemData.add(line.split(","));
        }

        fpTree = new FPTree();
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
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
        for (String[] line : itemData) {
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
        for (String[] line : itemData) {
            for (int i = 0; i < line.length; i++) {
                int index = itemIndex.get(line[i]);

            }
        }
    }

    public void getFrequentItemSetFromTree() {

    }

    private class Node {
        int index;
        int count;
        Node next;
        Node previousNode;

        public Node(int index, int count, Node next, Node previousNode) {
            this.index = index;
            this.count = count;
            this.next = null;
            this.previousNode = previousNode;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        public void setPreviousNode(Node previousNode) {
            this.previousNode = previousNode;
        }
    }

    private class FPTree {
        Node thisNode = null;
        List<FPTree> fpnodes;

        public FPTree() {
            fpnodes = new ArrayList<>();
        }

        public void addNode(FPTree tree, int index) {

        }

    }

}

class ItemSet {
    private int size;
    private int[] item;
    private int hash = 0;

    public ItemSet(int size, int[] item) {
        this.size = size;
        this.item = item;
    }

    public int getSize() {
        return size;
    }

    public int[] getItem() {
        return item;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ItemSet:");
        for (int i : item) {
            sb.append(i + " ");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && size > 0) {
            int val[] = item;

            for (int i = 0; i < size; i++) {
                h = 31 * h + val[i];
            }
            hash = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        ItemSet newset = (ItemSet) obj;
        for (int i = 0; i < this.size; i++) {
            if (newset.item[i] != this.item[i]) {
                return false;
            }
        }
        return true;
    }
}

