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
import com.codit.cryptowatchwallet.helper.PreferenceHelper;
import com.codit.cryptowatchwallet.model.CoinPrices;
import com.codit.cryptowatchwallet.util.Coin;
import com.codit.cryptowatchwallet.util.Currency;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Sreejith on 23-Nov-17.
 */

public class MarketRecyclerAdapter extends RecyclerView.Adapter<MarketRecyclerAdapter.ViewHolder> implements Filterable{

    List<CoinPrices> coinPricesList=new ArrayList<>();
    List<CoinPrices> coinPricesListCopy=new ArrayList<>();
    static final String TAG="recyclerview";
    PreferenceHelper preferenceHelper;


    public MarketRecyclerAdapter(List<CoinPrices> coinPricesList,Context context) {
        this.coinPricesList = coinPricesList;
        this.coinPricesListCopy=coinPricesList;
        preferenceHelper=new PreferenceHelper(context.getApplicationContext());

        Log.d("search", "constructor: ");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Log.d(TAG, "onCreateViewHolder: ");

        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.market_list_item, parent, false));

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String coinCode=coinPricesList.get(position).getCoinCode();
        Log.d(TAG, "onBindViewHolder: "+String.valueOf(position));
        holder.coinCode.setText(Coin.getCoinName(coinCode)+" ("+coinCode+")");
        holder.coinPrice.setText(Currency.currencyArray[preferenceHelper.getDefaultCurrency()]+" "+String.valueOf(coinPricesList.get(position).getPrices().get(Currency.currencyArray[preferenceHelper.getDefaultCurrency()])));

    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: "+coinPricesList.size());
        return coinPricesList.size();
    }

    public void updateData(List<CoinPrices> list)
    {
        coinPricesList.clear();
        coinPricesListCopy=list;
        for (CoinPrices prices:list) {
            coinPricesList.add(prices);
        }
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
                        if(coinPrices.getCoinCode().toLowerCase().contains(searchString.toLowerCase()))
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView coinCode,coinPrice;
        public ViewHolder(View itemView) {
            super(itemView);

            coinCode= itemView.findViewById(R.id.coin_code);
            coinPrice=itemView.findViewById(R.id.coin_price);

        }
    }
}
