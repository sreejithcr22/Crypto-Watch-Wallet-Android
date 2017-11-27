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
    private String balance;
    @SerializedName("balanceSat")
    @Expose
    private String balanceSat;
    @SerializedName("totalReceived")
    @Expose
    private String totalReceived;
    @SerializedName("totalReceivedSat")
    @Expose
    private String totalReceivedSat;
    @SerializedName("totalSent")
    @Expose
    private String totalSent;
    @SerializedName("totalSentSat")
    @Expose
    private String totalSentSat;
    @SerializedName("unconfirmedBalance")
    @Expose
    private String unconfirmedBalance;
    @SerializedName("unconfirmedBalanceSat")
    @Expose
    private String unconfirmedBalanceSat;
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

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getBalanceSat() {
        return balanceSat;
    }

    public void setBalanceSat(String balanceSat) {
        this.balanceSat = balanceSat;
    }

    public String getTotalReceived() {
        return totalReceived;
    }

    public void setTotalReceived(String totalReceived) {
        this.totalReceived = totalReceived;
    }

    public String getTotalReceivedSat() {
        return totalReceivedSat;
    }

    public void setTotalReceivedSat(String totalReceivedSat) {
        this.totalReceivedSat = totalReceivedSat;
    }

    public String getTotalSent() {
        return totalSent;
    }

    public void setTotalSent(String totalSent) {
        this.totalSent = totalSent;
    }

    public String getTotalSentSat() {
        return totalSentSat;
    }

    public void setTotalSentSat(String totalSentSat) {
        this.totalSentSat = totalSentSat;
    }

    public String getUnconfirmedBalance() {
        return unconfirmedBalance;
    }

    public void setUnconfirmedBalance(String unconfirmedBalance) {
        this.unconfirmedBalance = unconfirmedBalance;
    }

    public String getUnconfirmedBalanceSat() {
        return unconfirmedBalanceSat;
    }

    public void setUnconfirmedBalanceSat(String unconfirmedBalanceSat) {
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
    public Balance getWalletBalance(String coinCode) {
        return new Balance(getBalance(),getTotalReceived(),getTotalSent(),getUnconfirmedBalance(),getTxApperances(),getUnconfirmedTxApperances());
    }


}


