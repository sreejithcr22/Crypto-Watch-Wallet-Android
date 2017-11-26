package com.codit.cryptowatchwallet.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.codit.cryptowatchwallet.R;
import com.codit.cryptowatchwallet.adapter.MarketRecyclerAdapter;
import com.codit.cryptowatchwallet.model.CoinPrices;
import com.codit.cryptowatchwallet.service.FetchMarketDataService;
import com.codit.cryptowatchwallet.util.RecyclerviewSearchListener;
import com.codit.cryptowatchwallet.viewmodel.MarketViewModel;

import java.util.ArrayList;
import java.util.List;

public class MarketFragment extends Fragment  implements RecyclerviewSearchListener{

    private OnFragmentInteractionListener mListener;

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


        marketRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        marketRecyclerAdapter=new MarketRecyclerAdapter(coinPricesList);
        marketRecyclerView.setAdapter(marketRecyclerAdapter);

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        getContext().startService(new Intent(getContext(), FetchMarketDataService.class));
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public void onSearch(String searchString) {
        marketRecyclerAdapter.getFilter().filter(searchString);

    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}