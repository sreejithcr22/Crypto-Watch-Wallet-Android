package com.codit.cryptowatchwallet.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by Sreejith on 22-Nov-17.
 */

public class Coin {

    private static final double SMALLEST_UNIT=Math.pow(10,8);
    public static final String BTC="BTC";
    public static final String BCC="BCC";
    public static final String ETH="ETH";
    public static final String BCH="BCH";


    public static double convertToBase(long units)
    {
        return units/SMALLEST_UNIT;
    }
    public static String calculateCoinWorth(double coinCount,double coinRate,String currencyCode){
        BigDecimal rate =  BigDecimal.valueOf(coinCount*coinRate);
        BigDecimal roundOff = rate.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        return currencyCode+" "+roundOff.toPlainString();}

}
