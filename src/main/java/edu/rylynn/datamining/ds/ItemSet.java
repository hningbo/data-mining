package edu.rylynn.datamining.ds;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemSet {
    private Set<String> items;

    public ItemSet() {
        items = new HashSet<>();
    }

    public ItemSet(Set<String> items) {
        this.items = items;
    }

    public ItemSet(String[] strings) {
        this.items = new HashSet<String>();
        for (String item : strings) {
            this.items.add(item);
        }
    }

    public ItemSet(Transaction t) {
        this.items = new HashSet<String>();
        String[] itemArray = t.getTranction().split(",");
        for (String item : itemArray) {
            this.items.add(item);
        }
    }

    public ItemSet(String s) {
        this.items = new HashSet<String>();
        String[] itemArray = s.split(",");
        for (String item : itemArray) {
            this.items.add(item);
        }
    }

    public Set<String> getItems() {
        return items;
    }

    public void setItems(Set<String> items) {
        this.items = items;
    }

    public int getLength() {
        return items.size();
    }

    public boolean isSubSetOf(ItemSet itemSet) {
        if (this.getLength() > itemSet.getLength()) {
            return false;
        }
        if (!itemSet.getItems().containsAll(this.getItems())) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (String item : this.items) {
            sb.append(item + " ");
        }
        return sb.toString();
    }

    // get the C_n-1 from C_n
    public List<ItemSet> getSubItemSet() {
        List<ItemSet> subItemSets = new ArrayList<ItemSet>();
        for (String item : this.getItems()) {
            ItemSet subItemSet = new ItemSet();
            subItemSet.getItems().addAll(this.getItems());
            subItemSet.getItems().remove(item);
            subItemSets.add(subItemSet);
        }
        return subItemSets;
    }
}
