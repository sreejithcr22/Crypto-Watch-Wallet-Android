package com.codit.cryptowatchwallet.model;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.codit.cryptowatchwallet.service.BaseService;
import com.codit.cryptowatchwallet.service.NotificationService;

import java.math.BigDecimal;

/**
 * Created by Sreejith on 24-Nov-17.
 */

public class Balance implements Parcelable {

    public String getCoinBalance() {
        return coinBalance;
    }

    public void setCoinBalance(String coinBalance) {
        this.coinBalance = coinBalance;
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

    public String getUnconfirmedBalance() {
        return unconfirmedBalance;
    }

    public void setUnconfirmedBalance(String unconfirmedBalance) {
        this.unconfirmedBalance = unconfirmedBalance;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public Balance( String coinBalance, String totalReceived, String totalSent, String unconfirmedBalance, long transactionCount,long unConfirmedTransactionCount) {

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


    String coinBalance,totalReceived,totalSent,unconfirmedBalance;
    long transactionCount;

    public long getUnConfirmedTransactionCount() {
        return unConfirmedTransactionCount;
    }

    public void setUnConfirmedTransactionCount(long unConfirmedTransactionCount) {
        this.unConfirmedTransactionCount = unConfirmedTransactionCount;
    }

    long unConfirmedTransactionCount;




    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.coinBalance);
        dest.writeString(this.totalReceived);
        dest.writeString(this.totalSent);
        dest.writeString(this.unconfirmedBalance);
        dest.writeLong(this.transactionCount);
        dest.writeLong(this.unConfirmedTransactionCount);
    }

    protected Balance(Parcel in) {
        this.coinBalance = in.readString();
        this.totalReceived = in.readString();
        this.totalSent = in.readString();
        this.unconfirmedBalance = in.readString();
        this.transactionCount = in.readLong();
        this.unConfirmedTransactionCount = in.readLong();
    }

    public static final Parcelable.Creator<Balance> CREATOR = new Parcelable.Creator<Balance>() {
        @Override
        public Balance createFromParcel(Parcel source) {
            return new Balance(source);
        }

        @Override
        public Balance[] newArray(int size) {
            return new Balance[size];
        }
    };
}



