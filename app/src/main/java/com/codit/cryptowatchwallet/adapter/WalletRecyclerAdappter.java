package com.codit.cryptowatchwallet.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.codit.cryptowatchwallet.R;
import com.codit.cryptowatchwallet.model.Wallet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sreejith on 26-Nov-17.
 */

public class WalletRecyclerAdappter  extends RecyclerView.Adapter<WalletRecyclerAdappter.ViewHolder> implements Filterable{

    List<Wallet> walletList=new ArrayList<>();
    List<Wallet>walletListCopy=new ArrayList<>();

    public WalletRecyclerAdappter(List<Wallet> walletList) {
        this.walletList = walletList;
        this.walletListCopy=walletList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.wallet_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Wallet wallet=walletList.get(position);
        holder.walletName.setText(wallet.getDisplayName());
        holder.walletCoinCode.setText(wallet.getCoinCode());
        holder.walletBalance.setText(String.valueOf(wallet.getBalance().getCoinBalance()));
        holder.walletWorth.setText(wallet.getCoinWorth());

    }

    @Override
    public int getItemCount() {
        return walletList.size();
    }

    public void updateData(List<Wallet> updatedWallets)
    {
        walletList.clear();
        walletListCopy=updatedWallets;
        for (Wallet wallet:updatedWallets) {
            walletList.add(wallet);
        }
        this.notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchString=charSequence.toString().toLowerCase();
                if (searchString.isEmpty()){
                    walletList=walletListCopy;
                }
                else {
                    List<Wallet> filteredList=new ArrayList<>();
                    for (Wallet wallet:walletListCopy) {
                        if(wallet.getDisplayName().toLowerCase().contains(searchString)||wallet.getCoinCode().toLowerCase().contains(searchString))
                        {
                            filteredList.add(wallet);
                        }
                    }

                    walletList=filteredList;
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView walletName,walletCoinCode,walletBalance,walletWorth;
        public ViewHolder(View itemView) {
            super(itemView);

            walletName= itemView.findViewById(R.id.wallet_name);
            walletCoinCode=itemView.findViewById(R.id.wallet_coin_code);
            walletBalance=itemView.findViewById(R.id.wallet_balance);
            walletWorth=itemView.findViewById(R.id.wallet_worth);

        }
    }
}



