package com.codit.cryptowatchwallet.model;

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
    private Long totalReceived;
    @SerializedName("total_sent")
    @Expose
    private Long totalSent;
    @SerializedName("balance")
    @Expose
    private Long balance;
    @SerializedName("unconfirmed_balance")
    @Expose
    private Long unconfirmedBalance;
    @SerializedName("final_balance")
    @Expose
    private Long finalBalance;
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

    public Long getTotalReceived() {
        return totalReceived;
    }

    public void setTotalReceived(Long totalReceived) {
        this.totalReceived = totalReceived;
    }

    public Long getTotalSent() {
        return totalSent;
    }

    public void setTotalSent(Long totalSent) {
        this.totalSent = totalSent;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Long getUnconfirmedBalance() {
        return unconfirmedBalance;
    }

    public void setUnconfirmedBalance(Long unconfirmedBalance) {
        this.unconfirmedBalance = unconfirmedBalance;
    }

    public Long getFinalBalance() {
        return finalBalance;
    }

    public void setFinalBalance(Long finalBalance) {
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
    public Balance getWalletBalance() {

        double coinBalance= Coin.convertToBase(getBalance());
        double totalRecieved=Coin.convertToBase(getTotalReceived());
        double totalSent=Coin.convertToBase(getTotalSent());
        double unconfirmedBalance=Coin.convertToBase(getUnconfirmedBalance());

        return new Balance(coinBalance,totalRecieved,totalSent,unconfirmedBalance,getNTx(),getUnconfirmedNTx());
    }
}
