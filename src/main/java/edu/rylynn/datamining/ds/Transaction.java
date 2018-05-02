package edu.rylynn.datamining.ds;

public class Transaction {
    private String tranction;

    public Transaction(String tranction) {
        this.tranction = tranction;
    }

    public String getTranction() {

        return tranction;
    }

    public void setTranction(String tranction) {
        this.tranction = tranction;
    }

    public ItemSet toItemSet() {
        String[] itemset = tranction.split(",");
        return new ItemSet(itemset);
    }

}
