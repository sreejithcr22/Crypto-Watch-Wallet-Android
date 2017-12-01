package com.codit.cryptowatchwallet.model;

import android.util.Log;

import com.codit.cryptowatchwallet.util.BalanceInterface;
import com.codit.cryptowatchwallet.util.Coin;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sreejith on 21-Nov-17.
 */

public class CypherAddressBalance implements BalanceInterface{

    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("total_received")
    @Expose
    private String totalReceived;
    @SerializedName("total_sent")
    @Expose
    private String totalSent;
    @SerializedName("balance")
    @Expose
    private String balance;
    @SerializedName("unconfirmed_balance")
    @Expose
    private String unconfirmedBalance;
    @SerializedName("final_balance")
    @Expose
    private String finalBalance;
    @SerializedName("n_tx")
    @Expose
    private Long nTx;
    @SerializedName("unconfirmed_n_tx")
    @Expose
    private Long unconfirmedNTx;
    @SerializedName("final_n_tx")
    @Expose
    private Long finalNTx;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotalReceived() {
        return totalReceived;
    }

    public void setTotalReceived(String totalReceived) {
        this.totalReceived = totalReceived;
    }

    public String getTotalSent() {
        return totalSent;
    }

    public void setTotalSent(String totalSent) {
        this.totalSent = totalSent;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getUnconfirmedBalance() {
        return unconfirmedBalance;
    }

    public void setUnconfirmedBalance(String unconfirmedBalance) {
        this.unconfirmedBalance = unconfirmedBalance;
    }

    public String getFinalBalance() {
        return finalBalance;
    }

    public void setFinalBalance(String finalBalance) {
        this.finalBalance = finalBalance;
    }

    public Long getNTx() {
        return nTx;
    }

    public void setNTx(Long nTx) {
        this.nTx = nTx;
    }

    public Long getUnconfirmedNTx() {
        return unconfirmedNTx;
    }

    public void setUnconfirmedNTx(Long unconfirmedNTx) {
        this.unconfirmedNTx = unconfirmedNTx;
    }

    public Long getFinalNTx() {
        return finalNTx;
    }

    public void setFinalNTx(Long finalNTx) {
        this.finalNTx = finalNTx;
    }

    @Override
    public Balance getWalletBalance(String coinCode) {

        String coinBalance= Coin.convertToBase(getBalance(),coinCode);
        String totalRecieved=Coin.convertToBase(getTotalReceived(),coinCode);
        String totalSent=Coin.convertToBase(getTotalSent(),coinCode);
        String unconfirmedBalance=Coin.convertToBase(getUnconfirmedBalance(),coinCode);


        return new Balance(coinBalance,totalRecieved,totalSent,unconfirmedBalance,getNTx(),getUnconfirmedNTx());
    }
}
