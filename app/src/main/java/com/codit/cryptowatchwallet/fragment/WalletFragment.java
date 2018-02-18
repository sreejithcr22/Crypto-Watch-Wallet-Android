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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codit.cryptowatchwallet.R;
import com.codit.cryptowatchwallet.activity.AddWalletActivity;
import com.codit.cryptowatchwallet.adapter.WalletRecyclerAdapter;
import com.codit.cryptowatchwallet.model.Wallet;
import com.codit.cryptowatchwallet.service.BaseService;
import com.codit.cryptowatchwallet.service.FetchMarketDataService;
import com.codit.cryptowatchwallet.util.Connectivity;
import com.codit.cryptowatchwallet.util.RecyclerviewSearchListener;
import com.codit.cryptowatchwallet.viewmodel.WalletViewModel;

import java.util.ArrayList;
import java.util.List;

public class WalletFragment extends Fragment implements RecyclerviewSearchListener {

    RecyclerView walletRecyclerview;
    WalletRecyclerAdapter walletRecyclerAdapter;
    List<Wallet> walletList = new ArrayList<>();
    FloatingActionButton fab;
    private SwipeRefreshLayout swipeRefreshLayout;


    public WalletFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_wallet, container, false);
        walletRecyclerview = rootView.findViewById(R.id.wallet_recyclerview);
        swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
        fab = rootView.findViewById(R.id.add_wallet_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddWalletActivity.class);
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        walletRecyclerview.setLayoutManager(layoutManager);
        walletRecyclerAdapter = new WalletRecyclerAdapter(getContext(), walletList);
        walletRecyclerview.setAdapter(walletRecyclerAdapter);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(
                walletRecyclerview.getContext(),
                layoutManager.getOrientation()
        );
        walletRecyclerview.addItemDecoration(mDividerItemDecoration);

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Connectivity connectivity = new Connectivity(getContext());
                        if (!connectivity.isConnected()) {
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getContext(), "No internet !", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Intent intent = new Intent(getContext(), FetchMarketDataService.class);
                        intent.putExtra(BaseService.EXTRA_SHOULD_IGNORE_WALLET_REFRESH, true);
                        getContext().startService(intent);
                    }
                }
        );

        WalletViewModel walletViewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        walletViewModel.getAllWalletsLive().observe(WalletFragment.this, new Observer<List<Wallet>>() {
            @Override
            public void onChanged(@Nullable List<Wallet> wallets) {
                swipeRefreshLayout.setRefreshing(false);
                walletRecyclerAdapter.updateData(wallets);
            }
        });
    }

    public void onSearch(String searchString) {
        walletRecyclerAdapter.getFilter().filter(searchString);

    }
}
