package com.codit.cryptowatchwallet.http;

import com.codit.cryptowatchwallet.model.BCHAddressBalance;
import com.codit.cryptowatchwallet.model.CypherAddressBalance;
import com.codit.cryptowatchwallet.model.RippleBalance;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by Sreejith on 24-Nov-17.
 */

public interface WalletApi {

    @GET
    Call<CypherAddressBalance> getCypherAddressBalance(@Url String url);

    @GET
    Call<BCHAddressBalance> getBCHAddressBalance(@Url String url);

    @GET
    Call<RippleBalance> getRippleBalance(@Url String url);


}
