package edu.rylynn.datamining.core.associations;

import edu.rylynn.datamining.core.associations.common.ItemSet;

import java.util.*;

public class Eclat {
    private List<String[]> transaction;
    private double minSupport;
    private double minConfidence;
    private Map<ItemSet, Integer> frequentItemSet;
    private Map<ItemSet, Set<Integer>> tidSets;
    private Map<String, Integer> itemIndex;

    public Eclat(double minSupport, double minConfidence, List<String> data) {
        this.minSupport = minSupport*data.size();
        this.minConfidence = minConfidence;
        this.itemIndex = new HashMap<>();
        this.tidSets = new HashMap<>();
        this.frequentItemSet = new HashMap<>();
        transaction = new ArrayList<>();

        for (String line : data) {
            transaction.add(line.split(","));
        }
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        list.add("健康麦香包,皮蛋瘦肉粥,养颜红枣糕");
        list.add("健康麦香包,香煎葱油饼,皮蛋瘦肉粥,八宝粥");
        list.add("香煎葱油饼,皮蛋瘦肉粥,八宝粥");
        list.add("香煎葱油饼,八宝粥");
        new Eclat(0.5, 0.6, list).generateRules();

    }

    public void generateTIDSet() {
        int index = 0;
        for (int i = 0; i < transaction.size(); i++) {
            String[] line = transaction.get(i);
            for (int j = 0; j < line.length; j++) {
                if (itemIndex.containsKey(line[j])) {
                    int thisIndex = itemIndex.get(line[j]);
                    int[] item = new int[1];
                    item[0] = thisIndex;
                    ItemSet thisItemSet = new ItemSet(1, item);
                    tidSets.get(thisItemSet).add(i);
                } else {
                    itemIndex.put(line[j], index++);
                    Set<Integer> set = new HashSet<>();
                    set.add(i);
                    int[] item = new int[1];
                    item[0] = index - 1;
                    ItemSet thisItemSet = new ItemSet(1, item);
                    tidSets.put(thisItemSet, set);

                }
            }
        }
        System.out.println(this.tidSets);
    }

    private ItemSet generateSuperSet(ItemSet cn1, ItemSet cn2) {
        int[] cn1Item = cn1.getItem();
        int[] cn2Item = cn2.getItem();
        Arrays.sort(cn1Item);
        Arrays.sort(cn2Item);
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
                if (cn1Item[len1 - 1] < cn2Item[len1 - 1]) {
                    superSet[len1 - 1] = cn1Item[len1 - 1];
                    superSet[len1] = cn2Item[len1 - 1];
                } else {
                    superSet[len1 - 1] = cn2Item[len1 - 1];
                    superSet[len1] = cn1Item[len1 - 1];
                }

                return new ItemSet(len1 + 1, superSet);
            }
        }
        return null;
    }

    public Map generateFrequentItemSet() {
        generateTIDSet();
        Set<ItemSet> lastFrequentItemSet = new HashSet<>();
        int index = 0;


        for(Map.Entry<ItemSet, Set<Integer>> entry: this.tidSets.entrySet()) {
            Set<Integer> tidSet= entry.getValue();
            if (tidSet.size() > this.minSupport) {
                int[] item = new int[1];
                item[0] = index;
                ItemSet frequentItemSet = new ItemSet(1, item);
                lastFrequentItemSet.add(frequentItemSet);
                this.frequentItemSet.put(frequentItemSet, tidSet.size());
            }
            index++;
        }

        while (lastFrequentItemSet.size() != 0) {
            Set<ItemSet> newFrequentItemSet = new HashSet<>();
            for (ItemSet cni : lastFrequentItemSet) {
                for (ItemSet cnj : lastFrequentItemSet) {
                    ItemSet superSet = generateSuperSet(cni, cnj);
                    if (superSet != null) {
                        Set<Integer> tidSuperSets = new HashSet<>();
                        tidSuperSets.addAll(tidSets.get(cni));
                        tidSuperSets.retainAll(tidSets.get(cnj));
                        if (tidSuperSets.size() >= this.minSupport) {
                            newFrequentItemSet.add(superSet);
                            tidSets.put(superSet, tidSuperSets);
                            frequentItemSet.put(superSet, tidSuperSets.size());
                        }
                    }
                }
            }
            lastFrequentItemSet.clear();
            lastFrequentItemSet = newFrequentItemSet;
        }
        return this.frequentItemSet;
    }
    public void generateRules() {
        generateFrequentItemSet();
        for (Map.Entry<ItemSet, Integer> entry : frequentItemSet.entrySet()) {
            if (entry.getKey().getSize() > 1) {
                ItemSet frequentItems = entry.getKey();
                int[] items = frequentItems.getItem();
                List<Integer> items1 = new ArrayList<>();
                List<Integer> items2 = new ArrayList<>();
                double supportAB = frequentItemSet.get(frequentItems);
                System.out.println(frequentItems+", support: " + supportAB);

                for (int i = 1; i < (1 << items.length); i++) {
                    items1.clear();
                    items2.clear();
                    int index = 0;
                    int j = i;
                    while (index < items.length) {
                        if (j % 2 == 1) {
                            items1.add(items[index]);
                        } else {
                            items2.add(items[index]);
                        }
                        index++;
                        j /= 2;
                    }
                    if (items1.size() == 0 || items2.size() == 0) {
                        continue;
                    }
                    int[] item1Array = new int[items1.size()];
                    for (int ii = 0; ii < items1.size(); ii++) {
                        item1Array[ii] = items1.get(ii);
                    }
                    int[] item2Array = new int[items2.size()];
                    for (int ii = 0; ii < items2.size(); ii++) {
                        item2Array[ii] = items2.get(ii);
                    }
                    ItemSet A = new ItemSet(item1Array.length, item1Array);
                    ItemSet B = new ItemSet(item2Array.length, item2Array);

                    double supportA = frequentItemSet.get(A);
                    double supportB = frequentItemSet.get(B);
                    if (supportAB / supportA >= minConfidence) {

                        System.out.print(A + ", support: " + supportA);

                        System.out.print(" ==> ");

                        System.out.print(B + ", support " + supportB);

                        System.out.println("...   Condidence: " + supportAB / supportA);
                    }
                }
            }
            System.out.println();
        }
    }
}
