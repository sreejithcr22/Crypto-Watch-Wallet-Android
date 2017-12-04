package com.codit.cryptowatchwallet.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.codit.cryptowatchwallet.service.BaseService;
import com.codit.cryptowatchwallet.service.NotificationService;

import java.math.BigDecimal;

/**
 * Created by Sreejith on 24-Nov-17.
 */

public class Balance {

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


    public static  void isEqual(Balance oldBalance, Balance newBalance, String walletName, String coinCode, Context context)
    {
            BigDecimal oldCoinBalance=new BigDecimal(oldBalance.getCoinBalance());
            BigDecimal newCoinBalance=new BigDecimal(newBalance.getCoinBalance());
            String coinBalanceDifference=null;
            long newTransactionsCount=0;

            if((newTransactionsCount=newBalance.getTransactionCount()-oldBalance.getTransactionCount())>0) {

                if (oldCoinBalance.compareTo(newCoinBalance) != 0)
                    coinBalanceDifference = newCoinBalance.subtract(oldCoinBalance).toPlainString();

                Intent intent=new Intent(context, NotificationService.class);
                intent.putExtra(NotificationService.WALLET_TITLE,walletName+"-"+coinCode);
                intent.putExtra(NotificationService.NEW_TRANS_COUNT,newTransactionsCount);
                intent.putExtra(NotificationService.WALLET_BALANCE_DIFF,coinBalanceDifference);
                context.startService(intent);

            }


      /*  if     (balance1.getCoinBalance()!=balance2.getCoinBalance()||
                balance1.getTotalReceived()!=balance2.getTotalReceived()||
                balance1.getTotalSent()!=balance2.getTotalSent()||
                balance1.getUnconfirmedBalance()!=balance2.getUnconfirmedBalance()||
                balance1.getUnConfirmedTransactionCount()!=balance2.getUnConfirmedTransactionCount()||
                balance1.getTransactionCount()!=balance2.getTransactionCount()){
            Log.d("wallet", "isEqual: false");

*/

        }


    }



