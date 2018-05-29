package edu.rylynn.datamining.mapreduce.associations.common;

import java.util.ArrayList;
import java.util.List;

public class ItemSetUtil {
    public static ItemSet generateSuperSet(ItemSet cn1, ItemSet cn2) {
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

    public static List<ItemSet> generateSubSet(ItemSet itemSet) {
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
}
