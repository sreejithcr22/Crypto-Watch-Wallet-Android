package com.codit.cryptowatchwallet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.codit.cryptowatchwallet.R;
import com.codit.cryptowatchwallet.manager.SharedPreferenceManager;
import com.codit.cryptowatchwallet.model.CoinPrices;
import com.codit.cryptowatchwallet.util.Coin;
import com.codit.cryptowatchwallet.util.Currency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by Sreejith on 23-Nov-17.
 */

public class MarketRecyclerAdapter extends RecyclerView.Adapter<MarketRecyclerAdapter.ViewHolder> implements Filterable, Comparator<CoinPrices> {

    List<CoinPrices> coinPricesList=new ArrayList<>();
    List<CoinPrices> coinPricesListCopy=new ArrayList<>();
    SharedPreferenceManager sharedPreferenceManager;


    public MarketRecyclerAdapter(List<CoinPrices> coinPricesList,Context context) {
        this.coinPricesList = coinPricesList;
        this.coinPricesListCopy=coinPricesList;
        sharedPreferenceManager =new SharedPreferenceManager(context.getApplicationContext());

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.market_list_item, parent, false));

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String coinCode=coinPricesList.get(position).getCoinCode();
        holder.coinCode.setText(Coin.getCoinName(coinCode)+" ("+coinCode+")");
        String priceText=sharedPreferenceManager.getDefaultCurrency()+" "+String.valueOf(coinPricesList.get(position).getPrices().get(sharedPreferenceManager.getDefaultCurrency()));
        holder.coinPrice.setText(priceText.contains("null") ? Coin.PRICE_NOT_AVAILABLE : priceText);

    }

    @Override
    public int getItemCount() {
        return coinPricesList.size();
    }

    public void updateData(List<CoinPrices> list)
    {
        coinPricesList.clear();
        coinPricesListCopy=list;
        for (CoinPrices prices:list) {
            coinPricesList.add(prices);
        }
        Collections.sort(coinPricesList,this);
        this.notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                Log.d("search", "onQueryTextChange: "+charSequence.toString());
                String searchString=charSequence.toString();
                if (searchString.isEmpty()){
                    coinPricesList=coinPricesListCopy;
                }
                else {
                    List<CoinPrices> filteredList=new ArrayList<>();
                    for (CoinPrices coinPrices:coinPricesListCopy) {
                        if(coinPrices.getCoinCode().toLowerCase().contains(searchString.toLowerCase())||Coin.getCoinName(coinPrices.getCoinCode()).toLowerCase().contains(searchString.toLowerCase()))
                        {
                            filteredList.add(coinPrices);
                        }
                    }

                    coinPricesList=filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = coinPricesList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                //coinPricesList = (ArrayList<CoinPrices>) filterResults.values;
                notifyDataSetChanged();

            }
        };
    }

    @Override
    public int compare(CoinPrices prices, CoinPrices t1) {
        String currency=sharedPreferenceManager.getDefaultCurrency();
        try
        {
            return Double.compare(t1.getPrices().get(currency) , prices.getPrices().get(currency));
        }
        catch (Exception e) {return 0;}
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView coinCode,coinPrice;
        public ViewHolder(View itemView) {
            super(itemView);

            coinCode= itemView.findViewById(R.id.coin_code);
            coinPrice=itemView.findViewById(R.id.coin_price);

        }
    }
}
