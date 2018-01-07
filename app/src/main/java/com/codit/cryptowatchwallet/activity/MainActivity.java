package com.codit.cryptowatchwallet.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.codit.cryptowatchwallet.R;
import com.codit.cryptowatchwallet.fragment.MarketFragment;
import com.codit.cryptowatchwallet.fragment.SettingsFragment;
import com.codit.cryptowatchwallet.fragment.WalletFragment;
import com.codit.cryptowatchwallet.helper.PreferenceHelper;
import com.codit.cryptowatchwallet.model.Wallet;
import com.codit.cryptowatchwallet.service.AddWalletService;
import com.codit.cryptowatchwallet.service.FetchMarketDataService;
import com.codit.cryptowatchwallet.service.NotificationService;
import com.codit.cryptowatchwallet.service.RefreshWalletService;
import com.codit.cryptowatchwallet.service.UpdateWalletsWorthService;
import com.codit.cryptowatchwallet.util.Coin;
import com.codit.cryptowatchwallet.util.Currency;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity implements SettingsFragment.onCurrencyPreferenceClickListener{

     private static final String FRAGMENT_MARKET="market_fragment";
     private static final String FRAGMENT_WALLET="wallet_fragment";
     private static final String FRAGMENT_SETTINGS="settings_fragment";
     private Toolbar toolbar;

    MarketFragment marketFragment;
    WalletFragment walletFragment;
    PreferenceHelper preferenceHelper;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_wallets:

                    if(getSupportFragmentManager().findFragmentByTag(FRAGMENT_WALLET)==null)
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new WalletFragment(), FRAGMENT_WALLET).commit();

                    return true;
                case R.id.navigation_market:

                    if(getSupportFragmentManager().findFragmentByTag(FRAGMENT_MARKET)==null)
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new MarketFragment(),FRAGMENT_MARKET).commit();

                    return true;
                case R.id.navigation_settings:

                    if(getSupportFragmentManager().findFragmentByTag(FRAGMENT_SETTINGS)==null)
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new SettingsFragment(),FRAGMENT_SETTINGS).commit();

                    return true;
            }
            return false;
        }
    };

    void setUpAlarm()
    {
        Intent intent = new Intent(this, RefreshWalletService.class);
        PendingIntent pintent = PendingIntent.getService(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5*60*1000, pintent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        preferenceHelper=new PreferenceHelper(getApplicationContext());
        //check session count and do initial app setup
        initSession();


        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        toolbar=findViewById(R.id.toolbar);
        toolbar.setTitle("Wallets");
        setSupportActionBar(toolbar);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new WalletFragment(),FRAGMENT_WALLET).commit();











    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.change_currency:showChangeCurrencyDialog();return true;
        }

        return false;
    }

    void initSession()
    {
        if(preferenceHelper.getSessionCount()==0)
        {
            setUpAlarm();
        }
        if(!PreferenceHelper.SESSION_COUNT_UPDATED)
        {
            preferenceHelper.setSessionCount(preferenceHelper.getSessionCount()+1);
            PreferenceHelper.SESSION_COUNT_UPDATED=true;
            startService(new Intent(this,FetchMarketDataService.class));
        }
        Log.d("session", "count="+preferenceHelper.getSessionCount());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);


        SearchView searchView= (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {


                marketFragment= (MarketFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_MARKET);
                if(marketFragment!=null&&marketFragment.isVisible()) {
                    marketFragment.onSearch(newText);
                    return true;
                }

                walletFragment= (WalletFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_WALLET);
                if (walletFragment!=null&&walletFragment.isVisible())
                    walletFragment.onSearch(newText);



                return true;
            }
        });

        return true;
    }






    void showChangeCurrencyDialog()
    {
        final AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Change currency")
                .setSingleChoiceItems(Currency.currencyArray, preferenceHelper.getDefaultCurrency(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        preferenceHelper.setDefaultCurrency(i);
                        dialogInterface.dismiss();
                        Intent intent=new Intent(MainActivity.this, UpdateWalletsWorthService.class);
                        intent.putExtra(Currency.EXTRA_DATA_CURRENCY_CODE, Currency.currencyArray[i]);
                        startService(intent);
                        marketFragment= (MarketFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_MARKET);
                        if(marketFragment!=null&&marketFragment.isVisible()) {
                            marketFragment.refreshList();
                        }
                    }
                });
        builder.setNegativeButton("Cancel",null)
                .create().show();
    }

    @Override
    public void onCurrencyPreferenceClick() {
        showChangeCurrencyDialog();
    }
}
