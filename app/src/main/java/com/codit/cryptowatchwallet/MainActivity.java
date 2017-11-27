package com.codit.cryptowatchwallet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.codit.cryptowatchwallet.api.MarketApi;
import com.codit.cryptowatchwallet.fragment.MarketFragment;
import com.codit.cryptowatchwallet.fragment.WalletFragment;
import com.codit.cryptowatchwallet.model.Balance;
import com.codit.cryptowatchwallet.model.Wallet;
import com.codit.cryptowatchwallet.orm.AppDatabase;
import com.codit.cryptowatchwallet.orm.WalletDao;
import com.codit.cryptowatchwallet.service.AddWalletService;
import com.codit.cryptowatchwallet.service.FetchMarketDataService;
import com.codit.cryptowatchwallet.service.RefreshWalletService;
import com.codit.cryptowatchwallet.util.Coin;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

     static final String FRAGMENT_MARKET="market_fragment";
     static final String FRAGMENT_WALLET="wallet_fragment";
    private Toolbar toolbar;
    private TextView mTextMessage;
    private IntentIntegrator qrScan;
    MarketFragment marketFragment;
    WalletFragment walletFragment;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new WalletFragment(),FRAGMENT_WALLET).commit();
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                   // qrScan.initiateScan();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new MarketFragment(),FRAGMENT_MARKET).commit();

                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);

                   Intent intent=new Intent(MainActivity.this, AddWalletService.class);
                   intent.putExtra(Wallet.WALLET_NAME,"zero btc1");
                   intent.putExtra(Wallet.WALLET_COIN_CODE,Coin.BTC);
                   intent.putExtra(Wallet.WALLET_ADDRESS,"1FK1rNLV69C85ut7XfQVsyVgVWoesQNN8f");
                   startService(intent);

                    Intent intent1=new Intent(MainActivity.this, AddWalletService.class);
                    intent1.putExtra(Wallet.WALLET_NAME,"shopping");
                    intent1.putExtra(Wallet.WALLET_COIN_CODE,Coin.BCH);
                    intent1.putExtra(Wallet.WALLET_ADDRESS,"17Wk4GPKw9nZ9PbspzaxN3fv1L2m9NA9dg");
                    startService(intent1);

                    Intent intent2=new Intent(MainActivity.this, AddWalletService.class);
                    intent2.putExtra(Wallet.WALLET_NAME,"zero");
                    intent2.putExtra(Wallet.WALLET_COIN_CODE,Coin.ETH);
                    intent2.putExtra(Wallet.WALLET_ADDRESS,"0x6C2adC2073994fb2CCC5032cC2906Fa221e9B391");
                    startService(intent2);

                    return true;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mTextMessage = findViewById(R.id.message);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        toolbar=findViewById(R.id.toolbar);
        toolbar.setTitle("Wallets");
        setSupportActionBar(toolbar);



        qrScan = new IntentIntegrator(this);
        qrScan.setPrompt("Scan public address")
                .setOrientationLocked(false);






    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan failed, Please retry !", Toast.LENGTH_LONG).show();
            } else {
                mTextMessage.setText( result.getContents());

                }

            }
        else {
            Toast.makeText(this, "Scanning failed, Please retry !", Toast.LENGTH_LONG).show();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    BroadcastReceiver addWalletErrorReciever =new  BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("wallet", "onReceive: "+intent.getStringExtra(AddWalletService.ERROR_DATA));
            Toast.makeText(context,intent.getStringExtra(AddWalletService.ERROR_DATA),Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(addWalletErrorReciever,new IntentFilter(AddWalletService.ACTION_REPORT_ERROR));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(addWalletErrorReciever);
    }
}
