package edu.rylynn.datamining.core.associations.common;

import java.util.Arrays;

public class ItemSet {
    private int size;
    private int[] item;
    private int hash = 0;

    public ItemSet(int size, int[] item) {
        this.size = size;
        this.item = item;
        Arrays.sort(item);
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
        sb.append("ItemSet:{");
        for (int i : item) {
            sb.append(i + ",");
        }
        sb.append("}");
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
