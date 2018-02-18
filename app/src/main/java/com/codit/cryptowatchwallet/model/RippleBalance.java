package com.codit.cryptowatchwallet.model;

import com.codit.cryptowatchwallet.util.BalanceInterface;
import com.codit.cryptowatchwallet.util.Coin;
import com.codit.cryptowatchwallet.util.Currency;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Sreejith on 04-Feb-18.
 */

public class RippleBalance implements BalanceInterface {

    @SerializedName("result")
    @Expose
    private String result;
    @SerializedName("ledger_index")
    @Expose
    private Integer ledgerIndex;
    @SerializedName("limit")
    @Expose
    private Integer limit;
    @SerializedName("balances")
    @Expose
    private List<WalletBalance> walletBalances = null;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getLedgerIndex() {
        return ledgerIndex;
    }

    public void setLedgerIndex(Integer ledgerIndex) {
        this.ledgerIndex = ledgerIndex;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public List<WalletBalance> getWalletBalances() {
        return walletBalances;
    }

    public void setWalletBalances(List<WalletBalance> walletBalances) {
        this.walletBalances = walletBalances;
    }

    @Override
    public Balance getWalletBalance(String coinCode) {

        List<WalletBalance> walletBalances=getWalletBalances();
        String balance="0";

        for (WalletBalance walletBalance:walletBalances) {

            if(walletBalance.getCurrency().equals(Coin.XRP))
            {
                balance= walletBalance.getValue();
                break;
            }
        }

        return new Balance(balance,"-","-","-",-1,-1);
    }

    private class WalletBalance {

        @SerializedName("currency")
        @Expose
        private String currency;
        @SerializedName("value")
        @Expose
        private String value;
        @SerializedName("counterparty")
        @Expose
        private String counterparty;

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getCounterparty() {
            return counterparty;
        }

        public void setCounterparty(String counterparty) {
            this.counterparty = counterparty;
        }

    }
}
