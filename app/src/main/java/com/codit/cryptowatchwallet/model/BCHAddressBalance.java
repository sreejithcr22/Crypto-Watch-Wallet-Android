package com.codit.cryptowatchwallet.model;

/**
 * Created by Sreejith on 26-Nov-17.
 */

import java.util.List;

import com.codit.cryptowatchwallet.util.BalanceInterface;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BCHAddressBalance implements BalanceInterface{

    @SerializedName("addrStr")
    @Expose
    private String addrStr;
    @SerializedName("balance")
    @Expose
    private Double balance;
    @SerializedName("balanceSat")
    @Expose
    private Long balanceSat;
    @SerializedName("totalReceived")
    @Expose
    private Double totalReceived;
    @SerializedName("totalReceivedSat")
    @Expose
    private Long totalReceivedSat;
    @SerializedName("totalSent")
    @Expose
    private double totalSent;
    @SerializedName("totalSentSat")
    @Expose
    private Long totalSentSat;
    @SerializedName("unconfirmedBalance")
    @Expose
    private double unconfirmedBalance;
    @SerializedName("unconfirmedBalanceSat")
    @Expose
    private Long unconfirmedBalanceSat;
    @SerializedName("unconfirmedTxApperances")
    @Expose
    private Long unconfirmedTxApperances;
    @SerializedName("txApperances")
    @Expose
    private Long txApperances;
    @SerializedName("transactions")
    @Expose
    private List<String> transactions = null;

    public String getAddrStr() {
        return addrStr;
    }

    public void setAddrStr(String addrStr) {
        this.addrStr = addrStr;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Long getBalanceSat() {
        return balanceSat;
    }

    public void setBalanceSat(Long balanceSat) {
        this.balanceSat = balanceSat;
    }

    public Double getTotalReceived() {
        return totalReceived;
    }

    public void setTotalReceived(Double totalReceived) {
        this.totalReceived = totalReceived;
    }

    public Long getTotalReceivedSat() {
        return totalReceivedSat;
    }

    public void setTotalReceivedSat(Long totalReceivedSat) {
        this.totalReceivedSat = totalReceivedSat;
    }

    public double getTotalSent() {
        return totalSent;
    }

    public void setTotalSent(Long totalSent) {
        this.totalSent = totalSent;
    }

    public Long getTotalSentSat() {
        return totalSentSat;
    }

    public void setTotalSentSat(Long totalSentSat) {
        this.totalSentSat = totalSentSat;
    }

    public double getUnconfirmedBalance() {
        return unconfirmedBalance;
    }

    public void setUnconfirmedBalance(Long unconfirmedBalance) {
        this.unconfirmedBalance = unconfirmedBalance;
    }

    public Long getUnconfirmedBalanceSat() {
        return unconfirmedBalanceSat;
    }

    public void setUnconfirmedBalanceSat(Long unconfirmedBalanceSat) {
        this.unconfirmedBalanceSat = unconfirmedBalanceSat;
    }

    public Long getUnconfirmedTxApperances() {
        return unconfirmedTxApperances;
    }

    public void setUnconfirmedTxApperances(Long unconfirmedTxApperances) {
        this.unconfirmedTxApperances = unconfirmedTxApperances;
    }

    public Long getTxApperances() {
        return txApperances;
    }

    public void setTxApperances(Long txApperances) {
        this.txApperances = txApperances;
    }

    public List<String> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<String> transactions) {
        this.transactions = transactions;
    }


    @Override
    public Balance getWalletBalance() {
        return new Balance(getBalance(),getTotalReceived(),getTotalSent(),getUnconfirmedBalance(),getTxApperances(),getUnconfirmedTxApperances());
    }
}


