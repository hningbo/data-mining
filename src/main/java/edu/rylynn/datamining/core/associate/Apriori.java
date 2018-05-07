package edu.rylynn.datamining.core.associate;

import edu.rylynn.datamining.core.associate.common.ItemSet;

import java.util.*;

public class Apriori {
    private int minSupport;
    private int minConfidence;
    private Map<String, Integer> itemCount;
    private Map<String, Integer> itemIndex;
    private List<String[]> itemData;
    private Map<ItemSet, Integer> frequentItemSet;

    public Apriori(int minSupport, int minConfidence, List<String> data) {
        this.minSupport = minSupport;
        this.minConfidence = minConfidence;
        this.frequentItemSet = new HashMap<>();
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
        list.add("c,d,e,f");
        list.add("c,d,e,f");
        list.add("c,d,e,f");
        Map<ItemSet, Integer> hashMap = new Apriori(2, 2, list).getFrequentItemSet();
        Iterator iter = hashMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            ItemSet key = (ItemSet) entry.getKey();
            Integer val = (int) entry.getValue();
            System.out.println(key + "count:" + val);
        }

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

    private int[] firstScan() {
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
                    //System.out.println(itemIndex.get(line[i]) + "," + itemIndex.get(line[j]));
                    //System.out.println(itemPair.hashCode());
                    int binNum = itemPair.hashCode() % 100;
                    hashBin[binNum] += 1;
                }
            }
        }
//        Iterator<Map.Entry<String, Integer>> it = itemCount.entrySet().iterator();
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
                int counti = itemCount.get(line[i]);
                if (counti >= minSupport) {
                    int[] seti = new int[1];
                    seti[0] = itemIndex.get(line[i]);
                    ItemSet itemSeti = new ItemSet(1, seti);
                    if (!this.frequentItemSet.containsKey(itemSeti)) {

                        this.frequentItemSet.put(itemSeti, counti);
                    }
                    for (int j = i + 1; j < line.length; j++) {
                        int countj = itemCount.get(line[j]);
                        if (countj >= minSupport) {
                            int[] setj = new int[1];
                            setj[0] = itemIndex.get(line[j]);
                            ItemSet itemSetj = new ItemSet(1, setj);
                            if (!this.frequentItemSet.containsKey(itemSetj)) {
                                this.frequentItemSet.put(itemSetj, countj);
                            }
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
        return c2;
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


    private int countItemSet(ItemSet itemSet) {
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

    public Map<ItemSet, Integer> getFrequentItemSet() {
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
            int count = countItemSet(c2seti);
            if (count >= minSupport) {
                if (!this.frequentItemSet.containsKey(c2seti)) {
                    this.frequentItemSet.put(c2seti, count);
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
