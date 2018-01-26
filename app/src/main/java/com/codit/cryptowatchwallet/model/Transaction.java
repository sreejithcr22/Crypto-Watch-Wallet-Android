package com.codit.cryptowatchwallet.model;

/**
 * Created by Sreejith on 21-Jan-18.
 */

public class Transaction {

    private long tnxCount;

    public long getTnxCount() {
        return tnxCount;
    }

    public String getBalanceDiff() {
        return balanceDiff;
    }

    private String balanceDiff;

    public Transaction(long tnxCount, String balanceDiff) {
        this.tnxCount = tnxCount;
        this.balanceDiff = balanceDiff;
    }
}
