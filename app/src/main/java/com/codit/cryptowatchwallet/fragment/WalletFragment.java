package com.codit.cryptowatchwallet.fragment;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codit.cryptowatchwallet.R;
import com.codit.cryptowatchwallet.activity.AddWalletActivity;
import com.codit.cryptowatchwallet.adapter.WalletRecyclerAdappter;
import com.codit.cryptowatchwallet.model.Wallet;
import com.codit.cryptowatchwallet.service.FetchMarketDataService;
import com.codit.cryptowatchwallet.util.RecyclerviewSearchListener;
import com.codit.cryptowatchwallet.viewmodel.WalletViewModel;

import java.util.ArrayList;
import java.util.List;

public class WalletFragment extends Fragment implements RecyclerviewSearchListener{

    RecyclerView walletRecyclerview;
    WalletRecyclerAdappter walletRecyclerAdappter;
    List<Wallet>walletList=new ArrayList<>();
    FloatingActionButton fab;


    public WalletFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView= inflater.inflate(R.layout.fragment_wallet, container, false);
        walletRecyclerview=rootView.findViewById(R.id.wallet_recyclerview);
        fab=rootView.findViewById(R.id.add_wallet_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(), AddWalletActivity.class);
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        walletRecyclerview.setLayoutManager(layoutManager);
         walletRecyclerAdappter=new WalletRecyclerAdappter(getContext(),walletList);
        walletRecyclerview.setAdapter(walletRecyclerAdappter);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(
                walletRecyclerview.getContext(),
                layoutManager.getOrientation()
        );
        walletRecyclerview.addItemDecoration(mDividerItemDecoration);

        WalletViewModel walletViewModel= ViewModelProviders.of(this).get(WalletViewModel.class);
        walletViewModel.getAllWalletsLive().observe(WalletFragment.this, new Observer<List<Wallet>>() {
            @Override
            public void onChanged(@Nullable List<Wallet> wallets) {

                walletRecyclerAdappter.updateData(wallets);
            }
        });
    }

    public void onSearch(String searchString) {
        walletRecyclerAdappter.getFilter().filter(searchString);

    }
}
