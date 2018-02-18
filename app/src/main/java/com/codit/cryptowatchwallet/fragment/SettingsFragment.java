package com.codit.cryptowatchwallet.fragment;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.widget.Toast;

import com.codit.cryptowatchwallet.R;
import com.codit.cryptowatchwallet.manager.SharedPreferenceManager;
import com.codit.cryptowatchwallet.util.Currency;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Sreejith on 28-Nov-17.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {
    SharedPreferenceManager helper;
    Preference defaultCurrency, rateUs, shareApp, contactUs, donate;
    onCurrencyPreferenceClickListener listener;

    @Override
    public boolean onPreferenceClick(Preference preference) {

        switch (preference.getKey()) {
            case SharedPreferenceManager.DEFAULT_CURRENCY:
                listener.onCurrencyPreferenceClick();
                return true;
            case SharedPreferenceManager.RATE_APP:
                launchPlayStore();
                return true;
            case SharedPreferenceManager.SHARE_APP:
                shareApp();
                return true;
            case SharedPreferenceManager.CONTACT_US:
                sendFeedbackMail();
                return true;
            case SharedPreferenceManager.DONATE:
                showDonationDialog();
                return true;
        }
        return false;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Log.d("preference", "onCreatePreferences: ");
        addPreferencesFromResource(R.xml.settings);
        Log.d("preference", "oncreate: ");
        helper = new SharedPreferenceManager(getActivity().getApplicationContext());
        Log.d("preference", "onCreatePreferences: " + getPreferenceScreen().getPreference(0).getKey());

        defaultCurrency = getPreferenceManager().findPreference(SharedPreferenceManager.DEFAULT_CURRENCY);
        rateUs = getPreferenceManager().findPreference(SharedPreferenceManager.RATE_APP);
        shareApp = getPreferenceManager().findPreference(SharedPreferenceManager.SHARE_APP);
        contactUs = getPreferenceManager().findPreference(SharedPreferenceManager.CONTACT_US);
        donate = getPreferenceManager().findPreference(SharedPreferenceManager.DONATE);


        defaultCurrency.setOnPreferenceClickListener(this);
        rateUs.setOnPreferenceClickListener(this);
        shareApp.setOnPreferenceClickListener(this);
        contactUs.setOnPreferenceClickListener(this);
        donate.setOnPreferenceClickListener(this);

        intiSummaries();


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (onCurrencyPreferenceClickListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void intiSummaries() {
        defaultCurrency.setSummary(Currency.currencyArray[helper.getDefaultCurrency()]);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case SharedPreferenceManager.DEFAULT_CURRENCY:
                defaultCurrency.setSummary(Currency.currencyArray[helper.getDefaultCurrency()]);
                break;
        }
    }

    private void launchPlayStore() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getContext().getPackageName()));
        startActivity(intent);
    }

    private void shareApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + getContext().getPackageName());
        startActivity(intent);

    }

    private void sendFeedbackMail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "codit.apps@gmail.com", null));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
        startActivity(Intent.createChooser(intent, "Send feedback"));

    }

    private void showDonationDialog() {
        //TODO : change addresses
        final LinkedHashMap<String, String> addressesMap = new LinkedHashMap<>();
        addressesMap.put("Bitcoin", "12dfsdngiudngiwungiwur56457468568kdjnkdjvnksdjnvzdjvnsjdvnet");
        addressesMap.put("Litecoin", "335235sjfnjdsngksjdngksjdgnksjdnsjdgnfsdhdtherh");
        addressesMap.put("Ripple", "24634263735858sdgdfhery5735735846dgh");
        addressesMap.put("Ethereum", "sfdgsdhsrheretjdgjdgkfryk");
        final String[] currencies = new String[addressesMap.size()], addresses = new String[addressesMap.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : addressesMap.entrySet()) {
            currencies[i] = entry.getKey();
            addresses[i] = entry.getValue();
            i++;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Copy address")
                .setItems(currencies, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        copyToClipboard(addresses[i]);
                        Toast.makeText(getActivity(), "Address copied to clipboard", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Dismiss", null)
                .create().show();
    }

    private void copyToClipboard(String address) {
        try {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            //clipboard.setText(address);
            ClipData clipData = ClipData.newPlainText("address", address);
            clipboard.setPrimaryClip(clipData);
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), "Address could not be copied !", Toast.LENGTH_SHORT).show();
        }

    }

    public interface onCurrencyPreferenceClickListener {
        void onCurrencyPreferenceClick();
    }
}
