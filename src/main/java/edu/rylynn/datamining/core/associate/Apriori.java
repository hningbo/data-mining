package edu.rylynn.datamining.core.associate;

import java.util.*;

public class Apriori {
    private int minSupport;
    private int minConfidence;
    private Map<String, Integer> itemCount;
    private Map<String, Integer> itemIndex;
    private List<String> data;

    public Apriori(int minSupport, int minConfidence, List<String> data) {
        this.minSupport = minSupport;
        this.minConfidence = minConfidence;
        this.data = data;
        itemCount = new HashMap<>();
        itemIndex = new HashMap<>();

    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        list.add("a,b,c,d");
        list.add("c,d,e,f");

        new Apriori(2, 2, list).firstScan();
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public int getMinSupport() {
        return minSupport;
    }

    public void setMinSupport(int minSupport) {
        this.minSupport = minSupport;
    }

    public int getMinConfidence() {
        return minConfidence;
    }

    public void setMinConfidence(int minConfidence) {
        this.minConfidence = minConfidence;
    }

    public void firstScan() {
        int binSize = 100;
        int index = 1;
        int[] hashBin = new int[binSize];
        for (String line : data) {
            String[] itemdata = line.split(",");
            for (int i = 0; i < itemdata.length; i++) {
                if (itemCount.containsKey(itemdata[i])) {
                    itemCount.put(itemdata[i], itemCount.get(itemdata[i]) + 1);
                } else {
                    itemCount.put(itemdata[i], 1);
                    itemIndex.put(itemdata[i], index++);
                }

            }
            for (int i = 0; i < itemdata.length; i++) {
                for (int j = i + 1; j < itemdata.length; j++) {
                    ItemPair itemPair = new ItemPair(itemIndex.get(itemdata[i]), itemIndex.get(itemdata[j]));
                    System.out.println(itemIndex.get(itemdata[i]) + "," + itemIndex.get(itemdata[j]));
                    System.out.println(itemPair.hashCode());
                    int binNum = itemPair.hashCode() % 100;
                    hashBin[binNum] += 1;
                }
            }
        }
        Iterator<Map.Entry<String, Integer>> it = itemCount.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            System.out.println("key=" + entry.getKey() + "," + "value=" + entry.getValue());
        }
        for (int i = 0; i < 100; i++) {
            System.out.println(hashBin[i]);
        }
    }

    public void secondScan() {

    }

    public void getFrequentItemPair() {

    }

    class ItemPair {
        private int i;
        private int j;
        private int hash;
        private int count;

        public ItemPair(int i, int j) {
            this.i = i;
            this.j = j;
            this.count = 0;
            this.hash = 0;
        }

        @Override
        public int hashCode() {
            int h = hash;
            if (hash == 0) {
                h = (i * 5342 + j * 2345);
                hash = h;
            }
            return h;
        }
    }

}
