package edu.rylynn.datamining.core;

import edu.rylynn.datamining.ds.ItemSet;
import edu.rylynn.datamining.ds.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Apriori {
    List<Transaction> transactions;

    private List<ItemSet> frequentItemSet;

    private int minSupport;

    private int minConfidence;

    public Apriori(List<Transaction> transactions, int minSupport, int minConfidence) {
        this.transactions = transactions;
        this.minConfidence = minConfidence;
        this.minSupport = minSupport;
    }

    public void setMinSupport(int minSupport) {
        this.minSupport = minSupport;
    }

    public void setMinConfidence(int minConfidence) {
        this.minConfidence = minConfidence;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public int countInTransactoins(ItemSet itemSet) {
        Set<String> items = itemSet.getItems();
        int count = 0;
        for (Transaction t : transactions) {
            ItemSet transactionSet = t.toItemSet();
            if (itemSet.isSubSetOf(transactionSet)) {
                count++;
            }
        }
        return count;
    }

    public List<ItemSet> getFrequentItemSet(List<ItemSet> itemSets) {
        List<ItemSet> frequentItemSet = new ArrayList<>();
        for(ItemSet itemSet: itemSets){
            if(countInTransactoins(itemSet) >= minSupport){
                frequentItemSet.add(itemSet);
            }
        }
        return frequentItemSet;
    }

    public boolean hasInfrequentSubSet(ItemSet itemSet) {
        return false;
    }


}
