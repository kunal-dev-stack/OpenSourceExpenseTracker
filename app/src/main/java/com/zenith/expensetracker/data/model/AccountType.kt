package com.zenith.expensetracker.data.model

enum class AccountType(val displayName: String) {
    CASH("Cash"),
    BANK_CHECKING("Bank Checking"),
    SAVINGS("Savings Account"),
    CREDIT_CARD("Credit Card"),
    DIGITAL_WALLET("Digital Wallet")
}
