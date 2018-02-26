package com.codit.cryptowatchwallet.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.codit.cryptowatchwallet.R;
import com.codit.cryptowatchwallet.fragment.MarketFragment;
import com.codit.cryptowatchwallet.fragment.SettingsFragment;
import com.codit.cryptowatchwallet.fragment.WalletFragment;
import com.codit.cryptowatchwallet.manager.SharedPreferenceManager;
import com.codit.cryptowatchwallet.service.UpdateWalletsWorthService;
import com.codit.cryptowatchwallet.util.Currency;
import com.codit.cryptowatchwallet.util.UrlBuilder;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements SettingsFragment.onCurrencyPreferenceClickListener {

    private static final String FRAGMENT_MARKET = "market_fragment";
    private static final String FRAGMENT_WALLET = "wallet_fragment";
    private static final String FRAGMENT_SETTINGS = "settings_fragment";
    MarketFragment marketFragment;
    WalletFragment walletFragment;
    SharedPreferenceManager sharedPreferenceManager;
    private MenuItem currency;
    private Toolbar toolbar;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_wallets:

                    getSupportActionBar().setTitle("Wallets");
                    if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_WALLET) == null)
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new WalletFragment(), FRAGMENT_WALLET).commit();

                    return true;
                case R.id.navigation_market:

                    getSupportActionBar().setTitle("Market");
                    if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_MARKET) == null)
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MarketFragment(), FRAGMENT_MARKET).commit();

                    return true;
                case R.id.navigation_settings:
                    getSupportActionBar().setTitle("Settings");
                    if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_SETTINGS) == null)
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment(), FRAGMENT_SETTINGS).commit();

                    return true;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferenceManager = new SharedPreferenceManager(getApplicationContext());

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Wallets");
        setSupportActionBar(toolbar);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new WalletFragment(), FRAGMENT_WALLET).commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.change_currency:
                showChangeCurrencyDialog();
                return true;
        }

        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);


        currency=menu.findItem(R.id.change_currency);
        currency.setTitle(sharedPreferenceManager.getDefaultCurrency());

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {


                marketFragment = (MarketFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_MARKET);
                if (marketFragment != null && marketFragment.isVisible()) {
                    marketFragment.onSearch(newText);
                    return true;
                }

                walletFragment = (WalletFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_WALLET);
                if (walletFragment != null && walletFragment.isVisible())
                    walletFragment.onSearch(newText);


                return true;
            }
        });

        return true;
    }


    void showChangeCurrencyDialog() {
        final String currencies[]=getApplicationContext().getResources().getStringArray(R.array.currencies);
        Arrays.sort(currencies);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Change currency")
                .setSingleChoiceItems(currencies, Arrays.binarySearch(currencies,sharedPreferenceManager.getDefaultCurrency()), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sharedPreferenceManager.setDefaultCurrency(currencies[i]);
                        currency.setTitle(sharedPreferenceManager.getDefaultCurrency());
                        dialogInterface.dismiss();
                        Intent intent = new Intent(MainActivity.this, UpdateWalletsWorthService.class);
                        startService(intent);
                        marketFragment = (MarketFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_MARKET);
                        if (marketFragment != null && marketFragment.isVisible()) {
                            marketFragment.refreshList();
                        }
                    }
                });

        builder.setNegativeButton("Cancel", null)
                .create().show();
    }

    @Override
    public void onCurrencyPreferenceClick() {
        showChangeCurrencyDialog();
    }
}
