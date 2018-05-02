package edu.rylynn.datamining;

import edu.rylynn.datamining.ds.ItemSet;

public class Test {
    public static void main(String[] args) {
        ItemSet itemSet = new ItemSet("a,b,c,d,e");
        ItemSet subSet = new ItemSet("a,b,c,d");
        for (ItemSet subItemSet: subSet.getSubItemSet()){
            System.out.println(subItemSet);
        }
    }
}
