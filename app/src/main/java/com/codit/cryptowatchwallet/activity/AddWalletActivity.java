package com.codit.cryptowatchwallet.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.codit.cryptowatchwallet.R;
import com.codit.cryptowatchwallet.model.Wallet;
import com.codit.cryptowatchwallet.service.AddWalletService;
import com.codit.cryptowatchwallet.service.BaseService;
import com.codit.cryptowatchwallet.util.Coin;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddWalletActivity extends AppCompatActivity {

    EditText addressInput,coinInput,nameInput;
    android.support.v7.widget.Toolbar toolbar;
    private IntentIntegrator qrScan;
    AppCompatButton copyClip,scanQR;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wallet);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        addressInput=findViewById(R.id.add_wallet_address_edittext);
        coinInput=findViewById(R.id.add_wallet_coin_edittext);
        nameInput=findViewById(R.id.add_wallet_name_edittext);
        copyClip=findViewById(R.id.button_copy_clip);
        scanQR=findViewById(R.id.button_scan_qr);
        dialog=new ProgressDialog(AddWalletActivity.this);
        dialog.setMessage("Adding wallet..");
        dialog.setCancelable(false);
        coinInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCoinSelectDialog();
            }
        });
        copyClip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pasteFromClipboard();
            }
        });
        scanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qrScan.initiateScan();
            }
        });
        qrScan = new IntentIntegrator(this);
        qrScan.setPrompt("Scan address")
                .setOrientationLocked(false);

    }

    void showCoinSelectDialog()
    {
        final String[] coinSelectItems=getResources().getStringArray(R.array.add_wallet_spinner_items);
        for (String s:coinSelectItems) {
            Log.d("code", "showCoinSelectDialog: "+getSelectedCoinCode(s));
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(AddWalletActivity.this);
        builder.setTitle("Select coin")
                .setSingleChoiceItems(coinSelectItems, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        coinInput.setText(coinSelectItems[i]);
                    }
                }).create().show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_wallet_toolbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId()==R.id.add_wallet_submit_menu_item)
        {
            submit();
            return true;
        }
        else if (item.getItemId()==R.id.add_wallet_clear_menu_item)
        {
            coinInput.setText("");
            addressInput.setText("");
            nameInput.setText("");
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan failed, Please try again !", Toast.LENGTH_LONG).show();
            } else {
                addressInput.setText(result.getContents());
            }

        }
        else {
            Toast.makeText(this, "Scanning failed, Please try again !", Toast.LENGTH_LONG).show();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void pasteFromClipboard()
    {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            if(clipboard.hasPrimaryClip())
            {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                String pasteText = item.getText().toString();
                addressInput.setText(pasteText);
            }
            else{Toast.makeText(getApplicationContext(), "Clipboard empty", Toast.LENGTH_SHORT).show();}
        }catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "Clipboard empty", Toast.LENGTH_SHORT).show();
        }
    }

    String getSelectedCoinCode(String selectedValue)
    {
        return selectedValue.substring(selectedValue.indexOf("(")+1,selectedValue.indexOf(")"));

    }

    boolean validateFields()
    {
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(addressInput.getText().toString().trim());

        if(TextUtils.isEmpty(nameInput.getText().toString().trim()))
        {
            nameInput.setError("Please enter a name");return false;
        }
        else if (TextUtils.isEmpty(coinInput.getText().toString()))
        {
            coinInput.setError("Please select a coin");return false;
        }
        else if(TextUtils.isEmpty(addressInput.getText().toString().trim()))
        {
            addressInput.setError("Please enter an address");return false;
        }
        else if(addressInput.getText().toString().trim().contains(" "))
        {
            addressInput.setError("Address cannot contain whitespaces");return false;
        }
        else if(m.find())
        {
            addressInput.setError("Address cannot contain special characters");return false;
        }
        return true;
    }

    void submit()
    {
        if(!validateFields())return;

        Intent intent=new Intent(AddWalletActivity.this, AddWalletService.class);
        intent.putExtra(Wallet.WALLET_NAME,nameInput.getText().toString().trim());
        intent.putExtra(Wallet.WALLET_COIN_CODE, getSelectedCoinCode(coinInput.getText().toString()));
        intent.putExtra(Wallet.WALLET_ADDRESS,addressInput.getText().toString().trim());
        startService(intent);
        dialog.show();
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder=new AlertDialog.Builder(AddWalletActivity.this);
        builder.setMessage("Are you sure you want to go back?")
                .setPositiveButton("BACK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("CANCEL",null)
                .create().show();
    }

    BroadcastReceiver reportReceiver =new  BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("wallet", "onReceive: "+intent.getStringExtra(AddWalletService.REPORT_DATA));
            if (dialog.isShowing())dialog.cancel();

            showReport(intent.getStringExtra(BaseService.REPORT_DATA),intent.getBooleanExtra(BaseService.IS_ERROR,false));
        }
    };

    void showReport(String message,boolean isError)
    {
        String title;
        if(!isError)title="Success";
        else title="Failure";

        AlertDialog.Builder builder=new AlertDialog.Builder(AddWalletActivity.this);
        builder.setTitle(title)
                .setMessage(message);
        if(!isError)
        {
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        }
        else {
            builder.setPositiveButton("OK",null);
        }
        builder.create().show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(reportReceiver,new IntentFilter(AddWalletService.REPORT_STATUS));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(reportReceiver);
        if (dialog.isShowing())dialog.cancel();
    }

}
