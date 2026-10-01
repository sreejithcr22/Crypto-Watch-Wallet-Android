package com.codit.cryptowatchwallet.model

/**
 * Embedded balance value object. Stored as columns inside [Wallet] via @Embedded.
 */
data class Balance(
    var coinBalance: String = "0",
    var totalReceived: String = "0",
    var totalSent: String = "0",
    var unconfirmedBalance: String = "0",
    var transactionCount: Long = 0L,
    var unConfirmedTransactionCount: Long = 0L
)
