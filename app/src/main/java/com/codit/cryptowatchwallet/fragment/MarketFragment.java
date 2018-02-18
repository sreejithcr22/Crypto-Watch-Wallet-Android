package com.codit.cryptowatchwallet.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codit.cryptowatchwallet.R;
import com.codit.cryptowatchwallet.adapter.MarketRecyclerAdapter;
import com.codit.cryptowatchwallet.model.CoinPrices;
import com.codit.cryptowatchwallet.service.BaseService;
import com.codit.cryptowatchwallet.service.FetchMarketDataService;
import com.codit.cryptowatchwallet.util.Connectivity;
import com.codit.cryptowatchwallet.util.RecyclerviewSearchListener;
import com.codit.cryptowatchwallet.viewmodel.MarketViewModel;

import java.util.ArrayList;
import java.util.List;

public class MarketFragment extends Fragment  implements RecyclerviewSearchListener{

    RecyclerView marketRecyclerView;
    MarketRecyclerAdapter marketRecyclerAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    List<CoinPrices> coinPricesList;
    public MarketFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        coinPricesList=new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView=inflater.inflate(R.layout.fragment_market, container, false);
        marketRecyclerView= rootView.findViewById(R.id.marketRecyclerView);
        swipeRefreshLayout=rootView.findViewById(R.id.swiperefresh);
        return rootView;

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        marketRecyclerView.setLayoutManager(layoutManager);
        marketRecyclerAdapter=new MarketRecyclerAdapter(coinPricesList,getContext());
        marketRecyclerView.setAdapter(marketRecyclerAdapter);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(
                marketRecyclerView.getContext(),
                layoutManager.getOrientation()
        );
        marketRecyclerView.addItemDecoration(mDividerItemDecoration);

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Connectivity connectivity=new Connectivity(getContext());
                        if(! connectivity.isConnected())
                        {
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getContext(),"No internet !",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Intent intent=new Intent(getContext(),FetchMarketDataService.class);
                        intent.putExtra(BaseService.EXTRA_SHOULD_IGNORE_WALLET_REFRESH,true);
                        getContext().startService(intent);
                    }
                }
        );


        MarketViewModel marketViewModel= ViewModelProviders.of(this).get(MarketViewModel.class);
        marketViewModel.getAllCoinPrices().observe(MarketFragment.this, new Observer<List<CoinPrices>>() {
            @Override
            public void onChanged(@Nullable List<CoinPrices> updatedPrices) {

                for (CoinPrices p:coinPricesList) {
                    Log.d("viewmodel", "onChanged: "+p.getCoinCode());
                }

                swipeRefreshLayout.setRefreshing(false);
                marketRecyclerAdapter.updateData(updatedPrices);


            }
        });
    }


    public void onSearch(String searchString) {
        marketRecyclerAdapter.getFilter().filter(searchString);

    }

    public void refreshList()
    {
        marketRecyclerAdapter.notifyDataSetChanged();
    }



}
