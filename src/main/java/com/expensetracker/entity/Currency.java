package com.expensetracker.entity;

public enum Currency {
    USD("US Dollar", "$"),
    EUR("Euro", "€"),
    GBP("British Pound", "£"),
    JPY("Japanese Yen", "¥"),
    CNY("Chinese Yuan", "¥"),
    RUB("Russian Ruble", "₽"),
    UAH("Ukrainian Hryvnia", "₴"),
    PLN("Polish Zloty", "zł"),
    CHF("Swiss Franc", "Fr"),
    CAD("Canadian Dollar", "C$"),
    AUD("Australian Dollar", "A$"),
    BRL("Brazilian Real", "R$"),
    INR("Indian Rupee", "₹"),
    KRW("South Korean Won", "₩"),
    MXN("Mexican Peso", "$"),
    SEK("Swedish Krona", "kr"),
    NOK("Norwegian Krone", "kr"),
    DKK("Danish Krone", "kr"),
    TRY("Turkish Lira", "₺"),
    ZAR("South African Rand", "R");

    private final String displayName;
    private final String symbol;

    Currency(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }
}
