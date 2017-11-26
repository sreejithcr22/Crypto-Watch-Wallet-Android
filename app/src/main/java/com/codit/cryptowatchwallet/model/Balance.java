package com.codit.cryptowatchwallet.model;

import android.util.Log;

/**
 * Created by Sreejith on 24-Nov-17.
 */

public class Balance {

    public double getCoinBalance() {
        return coinBalance;
    }

    public void setCoinBalance(double coinBalance) {
        this.coinBalance = coinBalance;
    }

    public double getTotalReceived() {
        return totalReceived;
    }

    public void setTotalReceived(double totalReceived) {
        this.totalReceived = totalReceived;
    }

    public double getTotalSent() {
        return totalSent;
    }

    public void setTotalSent(double totalSent) {
        this.totalSent = totalSent;
    }

    public double getUnconfirmedBalance() {
        return unconfirmedBalance;
    }

    public void setUnconfirmedBalance(double unconfirmedBalance) {
        this.unconfirmedBalance = unconfirmedBalance;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public Balance( double coinBalance, double totalReceived, double totalSent, double unconfirmedBalance, long transactionCount,long unConfirmedTransactionCount) {

        this.coinBalance = coinBalance;
        this.totalReceived = totalReceived;
        this.totalSent = totalSent;
        this.unconfirmedBalance = unconfirmedBalance;
        this.transactionCount = transactionCount;
        this.unConfirmedTransactionCount=unConfirmedTransactionCount;
    }

    public Balance()
    {

    }


    double coinBalance,totalReceived,totalSent,unconfirmedBalance;
    long transactionCount;

    public long getUnConfirmedTransactionCount() {
        return unConfirmedTransactionCount;
    }

    public void setUnConfirmedTransactionCount(long unConfirmedTransactionCount) {
        this.unConfirmedTransactionCount = unConfirmedTransactionCount;
    }

    long unConfirmedTransactionCount;


    public static boolean isEqual(Balance balance1,Balance balance2)
    {
        if     (balance1.getCoinBalance()!=balance2.getCoinBalance()||
                balance1.getTotalReceived()!=balance2.getTotalReceived()||
                balance1.getTotalSent()!=balance2.getTotalSent()||
                balance1.getUnconfirmedBalance()!=balance2.getUnconfirmedBalance()||
                balance1.getUnConfirmedTransactionCount()!=balance2.getUnConfirmedTransactionCount()||
                balance1.getTransactionCount()!=balance2.getTransactionCount()){
            Log.d("wallet", "isEqual: false");return false;}

        return true;
    }

}
