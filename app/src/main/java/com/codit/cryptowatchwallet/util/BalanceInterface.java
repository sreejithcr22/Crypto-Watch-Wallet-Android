package com.codit.cryptowatchwallet.util;

import com.codit.cryptowatchwallet.model.Balance;

/**
 * Created by Sreejith on 24-Nov-17.
 */

public interface BalanceInterface {

    public Balance getWalletBalance(String coinCode);
}
