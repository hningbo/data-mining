package edu.rylynn.datamining;

import java.util.HashMap;
import java.util.Map;

public class Test {
    class ItemPair{
        public ItemPair(int i, int j){
            System.out.println(i+j);
        }
    }
    public void test() {
        Map<ItemPair ,Integer> map = new HashMap<>();
        ItemPair itemPair1 = new ItemPair(1,2);
        ItemPair itemPair2 = new ItemPair(1,2);
        map.put(itemPair1, 1);
        System.out.println(map.containsKey(itemPair1));
        System.out.println(map.containsKey(itemPair2));
    }

    public static void main(String[] args) {
        new Test().test();
    }
}
