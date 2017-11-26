package com.codit.cryptowatchwallet.model;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.math.BigDecimal;

/**
 * Created by Sreejith on 24-Nov-17.
 */
@Entity(indices={@Index(value="walletAddress", unique=true)})
public class Wallet {

    @Ignore
    public static final String WALLET_NAME="wallet_name";
    @Ignore
    public static final String WALLET_ADDRESS="wallet_address";
    @Ignore
    public static final String WALLET_COIN_CODE="wallet_coin_code";

    public Wallet()
    {

    }
    public Wallet(@NonNull String displayName, String coinCode,String walletAddress,Balance balance,String coinWorth) {
        this.displayName = displayName;
        this.balance = balance;
        this.coinCode=coinCode;
        this.walletAddress=walletAddress;
        this.coinWorth=coinWorth;
    }


    @NonNull
    @PrimaryKey
    String displayName;

    public String getCoinCode() {
        return coinCode;
    }

    public void setCoinCode(String coinCode) {
        this.coinCode = coinCode;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    String  coinCode,walletAddress;

    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@NonNull String displayName) {
        this.displayName = displayName;
    }

    public Balance getBalance() {
        return balance;
    }

    public void setBalance(Balance balance) {
        this.balance = balance;
    }

    @Embedded Balance balance;

    public String getCoinWorth() {
        return coinWorth;
    }

    public void setCoinWorth(String coinWorth) {
        this.coinWorth = coinWorth;
    }

    String coinWorth;

}
