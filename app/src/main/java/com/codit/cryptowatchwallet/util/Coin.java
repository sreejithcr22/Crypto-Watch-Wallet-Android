package com.codit.cryptowatchwallet.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by Sreejith on 22-Nov-17.
 */

public class Coin {

    private static final double SATOSHI=Math.pow(10,8);
    private static final double WEI=Math.pow(10,18);
    public static final String BTC="BTC";
    public static final String ETH="ETH";
    public static final String BCH="BCH";
    public static final String DASH="DASH";
    public static final String LTC="LTC";
    public static final String DOGE="DOGE";



    public static String convertToBase(String units,String coinCode)
    {
        double smallestUnit;
        switch (coinCode)
        {
            case BTC:smallestUnit=SATOSHI;break;
            case BCH:smallestUnit=SATOSHI;break;
            case ETH:smallestUnit=WEI;break;
            case DASH:smallestUnit=SATOSHI;break;
            case LTC:smallestUnit=SATOSHI;break;
            case DOGE:smallestUnit=SATOSHI;break;


            default:smallestUnit=0;


        }

        BigDecimal smallUnit=new BigDecimal(String.valueOf(smallestUnit));
        BigDecimal unitsDecimal=new BigDecimal(units);
        BigDecimal result=unitsDecimal.divide(smallUnit);
        if(result.compareTo(new BigDecimal(0))==0) return "0";
        return result.stripTrailingZeros().toPlainString();
    }
    public static String calculateCoinWorth(String coinCount,double coinRate,String currencyCode){
        BigDecimal rate = new BigDecimal(String.valueOf(coinRate)).multiply(new BigDecimal(coinCount));
        BigDecimal roundOff = rate.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        return currencyCode+" "+roundOff.toPlainString();}

}
