package edu.rylynn.datamining.core.associate;

import java.util.*;

public class Apriori {
    private int minSupport;
    private int minConfidence;
    private Map<String, Integer> itemCount;
    private Map<String, Integer> itemIndex;
    private List<String[]> itemData;

    public Apriori(int minSupport, int minConfidence, List<String> data) {
        this.minSupport = minSupport;
        this.minConfidence = minConfidence;

        itemData = new ArrayList<>();
        itemCount = new HashMap<>();
        itemIndex = new HashMap<>();

        for (String line : data) {
            itemData.add(line.split(","));
        }

    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        list.add("a,b,c,d");
        list.add("c,d,e,f");

        new Apriori(2, 2, list).firstScan();
    }

    public List<String[]> getitemData() {
        return itemData;
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

    public int[] firstScan() {
        int binSize = 100;
        int index = 1;
        int[] hashBin = new int[binSize];
        for (String[] line : itemData) {

            for (int i = 0; i < line.length; i++) {
                if (itemCount.containsKey(line[i])) {
                    itemCount.put(line[i], itemCount.get(line[i]) + 1);
                } else {
                    itemCount.put(line[i], 1);
                    itemIndex.put(line[i], index++);
                }

            }
            for (int i = 0; i < line.length; i++) {
                for (int j = i + 1; j < line.length; j++) {
                    ItemPair itemPair = new ItemPair(itemIndex.get(line[i]), itemIndex.get(line[j]));
                    System.out.println(itemIndex.get(line[i]) + "," + itemIndex.get(line[j]));
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
        return hashBin;
    }

    public ItemSet generateSuperSet(ItemSet cn1, ItemSet cn2) {
        int[] cn1Item = cn1.getItem();
        int[] cn2Item = cn2.getItem();
        int len1 = cn1Item.length;
        int len2 = cn2Item.length;
        int[] superSet = new int[len1 + 1];
        if (len1 == len2) {
            int flag = 1;
            for (int i = 0; i < len1 - 1; i++) {
                if (cn1Item[i] != cn2Item[i]) {
                    return null;
                } else {
                    superSet[i] = cn1Item[i];
                }
            }
            if (cn1Item[len1 - 1] != cn2Item[len1 - 1]) {
                superSet[len1] = cn2Item[len1 - 1];
                return new ItemSet(len1 + 1, superSet);
            }
        }
        return null;
    }

    public void secondScan(int[] hashBin) {
        List<ItemPair> c2 = new ArrayList<>();

        for (String[] line : itemData) {
            for (int i = 0; i < line.length; i++) {
                for (int j = i; j < line.length; j++) {
                    if (itemCount.get(line[i]) >= minSupport && itemCount.get(line[j]) >= minSupport) {
                        ItemPair candicateItemPair = new ItemPair(itemIndex.get(line[i]), itemIndex.get(line[j]));
                        int binIndex = candicateItemPair.hashCode() % 100;
                        if (hashBin[binIndex] >= minSupport) {
                            c2.add(candicateItemPair);
                        }
                    }
                }
            }
        }
    }

    public int countItemSet(ItemSet itemSet) {
        int[] items = itemSet.getItem();
        int count = 0;
        for (String[] line : itemData) {
            int flag = 1;
            for (int i = 0; i < items.length; i++) {
                flag = 0;
                for (int j = 0; j < line.length; j++) {
                    if (itemIndex.get(line[j]) == items[i]) {
                        flag = 1;
                        break;
                    }
                }
                if (flag == 0) {
                    break;
                }
            }
            if (flag == 1) {
                count++;
            }
        }
        return count;
    }

    public List<ItemSet> generateSubSet(ItemSet itemSet) {
        List<ItemSet> subSets = new ArrayList<>();
        int[] items = itemSet.getItem();
        int superSetLen = items.length;
        for (int i = 0; i < items.length; i++) {
            int k = 0;
            int[] subSetItems = new int[superSetLen - 1];
            for (int j = 0; j < items.length; j++) {
                if (i != j) {
                    subSetItems[k++] = items[j];
                }
            }
            subSets.add(new ItemSet(superSetLen - 1, subSetItems));
        }
        return subSets;
    }

    public List<ItemSet> getFrequentItemSet() {
        int[] hashBin = firstScan();

        secondScan(hashBin);

        List<ItemSet> frequentSet = new ArrayList<>();
        List<ItemSet> newFrequentSet = null;

        while (newFrequentSet != null) {
            newFrequentSet.clear();
            List<ItemSet> superSets = new ArrayList<>();
            for (int i = 0; i < frequentSet.size(); i++) {
                for (int j = i + 1; j < frequentSet.size(); j++) {
                    ItemSet cni = frequentSet.get(i);
                    ItemSet cnj = frequentSet.get(j);
                    ItemSet superSet = generateSuperSet(cni, cnj);
                    if (superSet != null) {
                        List<ItemSet> subSets = generateSubSet(superSet);
                        int flag = 1;
                        for (ItemSet subSet : subSets) {
                            if (countItemSet(subSet) < minSupport) {
                                flag = 0;
                                break;
                            }
                            if (flag == 1) {
                                frequentSet.add(superSet);
                                newFrequentSet.add(superSet);
                            }
                        }
                    }
                }
            }

        }
        return frequentSet;
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
                h = (i + j) * 432143 + (i * j) * 298017;
                hash = h;
            }
            return h;
        }
    }

    class ItemSet {
        private int size;
        private int[] item;

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
            for(int i: item){
                sb.append(i+" ");
            }
            return sb.toString();
        }
    }

}
