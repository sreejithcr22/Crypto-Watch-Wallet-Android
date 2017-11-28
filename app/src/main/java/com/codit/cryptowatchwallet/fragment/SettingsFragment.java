package com.codit.cryptowatchwallet.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.codit.cryptowatchwallet.R;
import com.codit.cryptowatchwallet.helper.PreferenceHelper;
import com.codit.cryptowatchwallet.util.Currency;

/**
 * Created by Sreejith on 28-Nov-17.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {
    PreferenceHelper helper;
    Preference defaultCurrency;
    onCurrencyPreferenceClickListener listener;

    @Override
    public boolean onPreferenceClick(Preference preference) {

        switch (preference.getKey())
        {
            case PreferenceHelper.DEFAULT_CURRENCY:listener.onCurrencyPreferenceClick();return true;
        }
        return false;
    }

    public interface onCurrencyPreferenceClickListener
    {
         void onCurrencyPreferenceClick();
    }
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Log.d("preference", "onCreatePreferences: ");
        addPreferencesFromResource(R.xml.settings);
        Log.d("preference", "oncreate: ");
        helper=new PreferenceHelper(getActivity().getApplicationContext());
        Log.d("preference", "onCreatePreferences: "+getPreferenceScreen().getPreference(0).getKey());

        defaultCurrency= getPreferenceManager().findPreference(PreferenceHelper.DEFAULT_CURRENCY);
        defaultCurrency.setOnPreferenceClickListener(this);

        intiSummaries();


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener= (onCurrencyPreferenceClickListener) context;
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



    void intiSummaries()
    {
        defaultCurrency.setSummary(Currency.currencyArray[helper.getDefaultCurrency()]);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s)
        {
            case PreferenceHelper.DEFAULT_CURRENCY:defaultCurrency.setSummary(Currency.currencyArray[helper.getDefaultCurrency()]);
            break;
        }
    }
}
