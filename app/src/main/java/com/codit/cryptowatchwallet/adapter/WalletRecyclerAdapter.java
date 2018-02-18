package com.codit.cryptowatchwallet.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.codit.cryptowatchwallet.R;
import com.codit.cryptowatchwallet.activity.WalletDetailsActivity;
import com.codit.cryptowatchwallet.model.Wallet;
import com.codit.cryptowatchwallet.util.Coin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Sreejith on 26-Nov-17.
 */

public class WalletRecyclerAdapter extends RecyclerView.Adapter<WalletRecyclerAdapter.ViewHolder> implements Filterable, Comparator<Wallet> {

    List<Wallet> walletList = new ArrayList<>();
    List<Wallet> walletListCopy = new ArrayList<>();
    Context context;

    public WalletRecyclerAdapter(Context context, List<Wallet> walletList) {
        this.walletList = walletList;
        this.walletListCopy = walletList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.wallet_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Wallet wallet = walletList.get(position);
        holder.walletName.setText(wallet.getDisplayName());
        holder.walletCoinCode.setText(Coin.getCoinName(wallet.getCoinCode()));
        holder.walletBalance.setText(String.valueOf(wallet.getBalance().getCoinBalance()) + " " + wallet.getCoinCode());
        holder.walletWorth.setText(wallet.getCoinWorth());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, WalletDetailsActivity.class);
                intent.putExtra(Wallet.EXTRA_WALLET_NAME, wallet.getDisplayName());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return walletList.size();
    }

    public void updateData(List<Wallet> updatedWallets) {
        walletList.clear();
        walletListCopy = updatedWallets;
        for (Wallet wallet : updatedWallets) {
            walletList.add(wallet);
        }
        Collections.sort(walletList,this);
        this.notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchString = charSequence.toString().toLowerCase();
                if (searchString.isEmpty()) {
                    walletList = walletListCopy;
                } else {
                    List<Wallet> filteredList = new ArrayList<>();
                    for (Wallet wallet : walletListCopy) {
                        if (wallet.getDisplayName().toLowerCase().contains(searchString) || wallet.getCoinCode().toLowerCase().contains(searchString)) {
                            filteredList.add(wallet);
                        }
                    }

                    walletList = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = walletList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int compare(Wallet wallet, Wallet t1) {
        double value1 = Double.parseDouble(wallet.getCoinWorth().substring(0,wallet.getCoinWorth().length()-4));
        double value2 = Double.parseDouble(t1.getCoinWorth().substring(0,t1.getCoinWorth().length()-4));

        return Double.compare(value2,value1);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView walletName, walletCoinCode, walletBalance, walletWorth;

        public ViewHolder(View itemView) {
            super(itemView);

            walletName = itemView.findViewById(R.id.wallet_name);
            walletCoinCode = itemView.findViewById(R.id.wallet_coin_code);
            walletBalance = itemView.findViewById(R.id.wallet_balance);
            walletWorth = itemView.findViewById(R.id.wallet_worth);

        }
    }
}



