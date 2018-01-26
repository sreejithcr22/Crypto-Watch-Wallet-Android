package com.codit.cryptowatchwallet.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.codit.cryptowatchwallet.R;
import com.codit.cryptowatchwallet.model.Balance;
import com.codit.cryptowatchwallet.model.Wallet;
import com.codit.cryptowatchwallet.orm.AppDatabase;
import com.codit.cryptowatchwallet.orm.WalletDao;
import com.codit.cryptowatchwallet.util.Coin;

public class WalletDetailsActivity extends AppCompatActivity {

    private TextView coinWorthText,coinBalanceText,unconfBalanceText,totalSentText,totalReceivedText,transactionsText,unconfTransactionsText;
    private Wallet wallet;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_details);

        Intent intent=getIntent();
        wallet=intent.getParcelableExtra(Wallet.EXTRA_WALLET_OBJECT);
        if (wallet==null)
        {
            Toast.makeText(this,"Sorry , something went wrong !",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //identify views
        toolbar=findViewById(R.id.toolbar);
        coinWorthText=findViewById(R.id.wallet_details_coin_worth_text);
        coinBalanceText=findViewById(R.id.wallet_details_balance_text);
        unconfBalanceText=findViewById(R.id.wallet_details_unconf_balance_text);
        totalSentText=findViewById(R.id.wallet_details_total_sent_text);
        totalReceivedText=findViewById(R.id.wallet_details_total_received_text);
        transactionsText=findViewById(R.id.wallet_details_transactions_text);
        unconfTransactionsText=findViewById(R.id.wallet_details_unconf_transactions_text);

        toolbar.setTitle(wallet.getDisplayName());
        toolbar.setSubtitle(Coin.getCoinName(wallet.getCoinCode())+" ("+wallet.getCoinCode()+")");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        updateUI();


    }


    private void shareAddress()
    {
        String shareText= "Currency: "+Coin.getCoinName(wallet.getCoinCode())+"("+wallet.getCoinCode()+")\n"+"Address: "+wallet.getWalletAddress();
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,shareText);

        if(intent.resolveActivity(getPackageManager())!=null)
        {
            startActivityForResult(intent,100);
        }


    }

    private void deleteWallet()
    {
        final Handler handler=new Handler(Looper.getMainLooper());
        final Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                WalletDao walletDao= AppDatabase.getDatabase(getApplicationContext()).walletDao();
                int deletedCount=walletDao.deleteWallet(wallet);
                if(deletedCount>0)handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Wallet deleted !",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        AlertDialog.Builder builder=new AlertDialog.Builder(WalletDetailsActivity.this);
        builder.setMessage("Delete wallet '"+wallet.getDisplayName()+"' ?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        thread.start();
                        finish();
                    }
                })
                .setNegativeButton("Cancel",null)
                .create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wallet_details_toolbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id=item.getItemId();

        switch (id)
        {
            case R.id.share_address:shareAddress();return true;
            case R.id.delete:deleteWallet();return true;
        }
        return false;
    }

    private void updateUI() {

        Balance balance=wallet.getBalance();
        coinWorthText.setText(wallet.getCoinWorth());
        coinBalanceText.setText(balance.getCoinBalance());
        unconfBalanceText.setText(balance.getUnconfirmedBalance());
        totalSentText.setText(balance.getTotalSent());
        totalReceivedText.setText(balance.getTotalReceived());
        transactionsText.setText(String.valueOf(balance.getTransactionCount()));
        unconfTransactionsText.setText(String.valueOf(balance.getUnConfirmedTransactionCount()));
    }
}
