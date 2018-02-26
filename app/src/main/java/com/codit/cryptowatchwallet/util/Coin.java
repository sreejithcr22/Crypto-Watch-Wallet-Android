package com.codit.cryptowatchwallet.util;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

/**
 * Created by Sreejith on 22-Nov-17.
 */

public class Coin {

    public static final String BTC = "BTC";
    public static final String ETH = "ETH";
    public static final String BCH = "BCH";
    public static final String DASH = "DASH";
    public static final String LTC = "LTC";
    public static final String DOGE = "DOGE";
    public static final String XRP = "XRP";
    public static final String PRICE_NOT_AVAILABLE="Not available";
    private static final double SATOSHI = Math.pow(10, 8);
    private static final double WEI = Math.pow(10, 18);
    private static LinkedHashMap<String,String> coinsData =new LinkedHashMap<>();

    static {
        coinsData.put("BTC","Bitcoin");
        coinsData.put("ETH","Ethereum");
        coinsData.put("XRP","Ripple");
        coinsData.put("BCH","Bitcoin Cash");
        coinsData.put("LTC","Litecoin");
        coinsData.put("ADA","Cardano");
        coinsData.put("XLM","Stellar");
        coinsData.put("NEO","NEO");
        coinsData.put("EOS","EOS");
        coinsData.put("IOTA","IOTA");
        coinsData.put("DASH","DASH");
        coinsData.put("XEM","NEM");
        coinsData.put("XMR","Monero");
        coinsData.put("LSK","LISK");
        coinsData.put("ETC","Ethereum Classic");
        coinsData.put("VEN","Ve Chain");
        coinsData.put("TRX","TRON");
        coinsData.put("BTG","Bitcoin Gold");
        coinsData.put("USDT","Tether");
        coinsData.put("OMG","OmiseGO");
        coinsData.put("ICX","ICON");
        coinsData.put("ZEC","Zcash");
        coinsData.put("XVG","Verge");
        coinsData.put("BCN","Bytecoin");
        coinsData.put("PPT","Populous");
        coinsData.put("STRAT","Stratis");
        coinsData.put("RHOC","RChain");
        coinsData.put("SC","Siacoin");
        coinsData.put("WAVES","Waves");
        coinsData.put("SNT","Status");
        coinsData.put("MKR","Maker");
        coinsData.put("BTS","BitShares");
        coinsData.put("VERI","Veritaseum");
        coinsData.put("AE","Aeternity");
        coinsData.put("WTC","Waltonchain");
        coinsData.put("ZCL","ZClassic");
        coinsData.put("DCR","Decred");
        coinsData.put("REP","Augur");
        coinsData.put("DGD","DigixDAO");
        coinsData.put("ARDR","Ardor");
        coinsData.put("HSR","Hshare");
        coinsData.put("ETN","Electroneum");
        coinsData.put("KMD","Komodo");
        coinsData.put("GAS","Gas");
        coinsData.put("ARK","Ark");
        coinsData.put("BTX","Bitcore");
        coinsData.put("VTC","Vertcoin");
        coinsData.put("DOGE","DogeCoin");









    }

    public static LinkedHashMap<String,String> getCoinsData()
    {
        return coinsData;
    }

    public static String convertToBase(String units, String coinCode) {
        double smallestUnit;
        switch (coinCode) {
            case BTC:
                smallestUnit = SATOSHI;
                break;
            case BCH:
                smallestUnit = SATOSHI;
                break;
            case ETH:
                smallestUnit = WEI;
                break;
            case DASH:
                smallestUnit = SATOSHI;
                break;
            case LTC:
                smallestUnit = SATOSHI;
                break;
            case DOGE:
                smallestUnit = SATOSHI;
                break;
            case XRP:
                smallestUnit = SATOSHI;
                break;


            default:
                smallestUnit = 1;


        }

        BigDecimal smallUnit = new BigDecimal(String.valueOf(smallestUnit));
        BigDecimal unitsDecimal = new BigDecimal(units);
        BigDecimal result = unitsDecimal.divide(smallUnit);
        if (result.compareTo(new BigDecimal(0)) == 0) return "0";
        return result.stripTrailingZeros().toPlainString();
    }

    public static String calculateCoinWorth(String coinCount, double coinRate, String currencyCode) {
        if(coinRate==-1) return PRICE_NOT_AVAILABLE;
        BigDecimal rate = new BigDecimal(String.valueOf(coinRate)).multiply(new BigDecimal(coinCount));
        BigDecimal roundOff = rate.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        return roundOff.toPlainString() + " " + currencyCode;
    }


    public static String getCoinName(String coinCode) {
        /*switch (coinCode) {
            case BTC:
                return "Bitcoin";
            case ETH:
                return "Ethereum";
            case BCH:
                return "Bitcoin Cash";
            case DASH:
                return "Dash";
            case LTC:
                return "Litecoin";
            case DOGE:
                return "Dogecoin";
            case XRP:
                return "Ripple";
            default:
                return "";
        }*/
        return coinsData.get(coinCode);
    }
}

