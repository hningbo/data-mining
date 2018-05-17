package edu.rylynn.datamining.core.associations;

import edu.rylynn.datamining.core.associations.common.ItemSet;

import java.util.*;

public class Apriori {
    private double minSupport;
    private double minConfidence;
    private Map<String, Integer> transaction;
    private List<String> itemIndex;
    private List<String[]> itemData;
    private Map<ItemSet, Double> frequentItemSet;

    public Apriori(double minSupport, double minConfidence, List<String> data) {
        this.minSupport = minSupport;
        this.minConfidence = minConfidence;
        this.frequentItemSet = new HashMap<>();
        itemData = new ArrayList<>();
        transaction = new HashMap<>();
        itemIndex = new ArrayList<>();

        for (String line : data) {
            itemData.add(line.split(","));
        }

    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        list.add("健康麦香包,皮蛋瘦肉粥,养颜红枣糕");
        list.add("健康麦香包,香煎葱油饼,皮蛋瘦肉粥,八宝粥");
        list.add("香煎葱油饼,皮蛋瘦肉粥,八宝粥");
        list.add("香煎葱油饼,八宝粥");
        new Apriori(0.5, 0.7, list).generateRules();
    }

    public List<String[]> getitemData() {
        return itemData;
    }

    public double getMinSupport() {
        return minSupport;
    }

    public void setMinSupport(double minSupport) {
        this.minSupport = minSupport;
    }

    public double getMinConfidence() {
        return minConfidence;
    }

    public void setMinConfidence(double minConfidence) {
        this.minConfidence = minConfidence;
    }

    private int[] firstScan() {
        int binSize = 100;
        int index = 1;
        int[] hashBin = new int[binSize];
        for (String[] line : itemData) {
            for (int i = 0; i < line.length; i++) {
                if (transaction.containsKey(line[i])) {
                    transaction.put(line[i], transaction.get(line[i]) + 1);
                } else {
                    transaction.put(line[i], 1);
                    itemIndex.add(line[i]);
                }

            }
            for (int i = 0; i < line.length; i++) {
                for (int j = i + 1; j < line.length; j++) {

                    ItemPair itemPair = new ItemPair(itemIndex.indexOf(line[i]), itemIndex.indexOf(line[j]));
                    //System.out.println(itemIndex.get(line[i]) + "," + itemIndex.get(line[j]));
                    //System.out.println(itemPair.hashCode());
                    int binNum = itemPair.hashCode() % 100;
                    hashBin[binNum] += 1;
                }
            }
        }
//        Iterator<Map.Entry<String, Integer>> it = transaction.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry<String, Integer> entry = it.next();
//            System.out.println("key=" + entry.getKey() + "," + "value=" + entry.getValue());
//        }
//        for (int i = 0; i < 100; i++) {
//            System.out.println(hashBin[i]);
//        }
        return hashBin;
    }

    private Set<ItemPair> secondScan(int[] hashBin) {
        //cpy algorithm

        Set<ItemPair> c2 = new HashSet<>();
        for (String[] line : itemData) {
            for (int i = 0; i < line.length; i++) {
                double supporti = (double) transaction.get(line[i]) / (double) itemData.size();
                if (supporti >= minSupport) {
                    int[] seti = new int[1];
                    seti[0] = itemIndex.indexOf(line[i]);
                    ItemSet itemSeti = new ItemSet(1, seti);
                    if (!this.frequentItemSet.containsKey(itemSeti)) {

                        this.frequentItemSet.put(itemSeti, supporti);
                    }
                    for (int j = i + 1; j < line.length; j++) {
                        double supportj = (double) transaction.get(line[j]) / (double) itemData.size();
                        if (supportj >= minSupport) {
                            int[] setj = new int[1];
                            setj[0] = itemIndex.indexOf(line[j]);
                            ItemSet itemSetj = new ItemSet(1, setj);
                            if (!this.frequentItemSet.containsKey(itemSetj)) {
                                this.frequentItemSet.put(itemSetj, supportj);
                            }
                            ItemPair candicateItemPair = new ItemPair(itemIndex.indexOf(line[i]), itemIndex.indexOf(line[j]));
                            int binIndex = candicateItemPair.hashCode() % 100;
                            if (hashBin[binIndex] >= minSupport) {
                                c2.add(candicateItemPair);
                            }
                        }
                    }
                }
            }
        }

        return c2;
    }

    private ItemSet generateSuperSet(ItemSet cn1, ItemSet cn2) {
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


    private double countItemSet(ItemSet itemSet) {
        int[] items = itemSet.getItem();
        int count = 0;
        for (String[] line : itemData) {
            int flag = 1;
            for (int i = 0; i < items.length; i++) {
                flag = 0;
                for (int j = 0; j < line.length; j++) {
                    if (itemIndex.indexOf(line[j]) == items[i]) {
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
        return (double) count / (double) itemData.size();
    }

    private List<ItemSet> generateSubSet(ItemSet itemSet) {
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

    public Map<ItemSet, Double> getFrequentItemSet() {
        int[] hashBin = firstScan();
        Set<ItemPair> c2 = secondScan(hashBin);
        if (c2.size() == 0) {
            System.out.println("No frequent item set...");
        }


        Set<ItemSet> lastFrequentSet = new HashSet<>();       //C_(n-1)

        for (ItemPair c2i : c2) {
            int[] a = new int[2];
            a[0] = c2i.i;
            a[1] = c2i.j;
            ItemSet c2seti = new ItemSet(2, a);
            double support = countItemSet(c2seti);
            if (support >= minSupport) {
                if (!this.frequentItemSet.containsKey(c2seti)) {
                    this.frequentItemSet.put(c2seti, support);
                }
                lastFrequentSet.add(c2seti);
            }
        }

        while (lastFrequentSet.size() != 0) {//from C_(n-1) get C_n and the lastFrequentSet is the C_(n-1)
            Set<ItemSet> newFrequentSet = new HashSet<>();

            for (ItemSet cni : lastFrequentSet) {
                for (ItemSet cnj : lastFrequentSet) {
                    //get the candicate set
                    ItemSet superSet = generateSuperSet(cni, cnj);
                    if (superSet != null) {
                        //get the subset of the candicate set
                        //and judge if all the subset of the candicateset is in the frequentItemSet
                        List<ItemSet> subSets = generateSubSet(superSet);
                        int flag = 1;
                        for (ItemSet subSet : subSets) {
                            if (!this.frequentItemSet.containsKey(subSet)) {
                                flag = 0;
                                break;
                            }
                        }
                        if (flag == 1) {
                            if (!this.frequentItemSet.containsKey(superSet)) {
                                frequentItemSet.put(superSet, countItemSet(superSet));
                            }
                            newFrequentSet.add(superSet);
                        }
                    }
                }
            }
            lastFrequentSet.clear();
            lastFrequentSet = newFrequentSet;
        }
        return frequentItemSet;
    }

    public void generateRules() {
        getFrequentItemSet();
        for (Map.Entry<ItemSet, Double> entry : frequentItemSet.entrySet()) {
            if (entry.getKey().getSize() > 1) {
                ItemSet frequentItems = entry.getKey();
                int[] items = frequentItems.getItem();
                List<Integer> items1 = new ArrayList<>();
                List<Integer> items2 = new ArrayList<>();
                double supportAB = frequentItemSet.get(frequentItems);
                System.out.print("{");
                for (int q : frequentItems.getItem()) {
                    System.out.print(itemIndex.get(q) + ",");
                }

                System.out.print("} ");
                System.out.println(", support: " + supportAB);

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
                        System.out.print("{");
                        for (int q : A.getItem()) {
                            System.out.print(itemIndex.get(q) + ",");
                        }
                        System.out.print("} ");
                        System.out.print(", support: " + supportA);

                        System.out.print(" ==> ");

                        System.out.print("{");
                        for (int q : B.getItem()) {
                            System.out.print(itemIndex.get(q) + ",");
                        }
                        System.out.print("} ");
                        System.out.print(", support " + supportB);

                        System.out.println("...   Condidence: " + supportAB / supportA);
                    }
                }
            }
            System.out.println();
        }
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

}
