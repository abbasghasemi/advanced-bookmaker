/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.general;

import android.content.Intent;

import ghasemi.abbas.book.BuildConfig;

public enum MarketConfiguration {

    CAFE_BAZAAR("com.farsitel.bazaar", " کافه بازار ", "bazaar://details?id=" + BuildConfig.APPLICATION_ID, Intent.ACTION_EDIT),
    MYKET("ir.mservices.market"," مایکت ", "myket://comment?id=" + BuildConfig.APPLICATION_ID, Intent.ACTION_VIEW);

    private final String pkg,name,rate,ri;

    MarketConfiguration(String pkg, String name, String rate, String RI) {
        this.pkg = pkg;
        this.name = name;
        this.rate = rate;
        this.ri = RI;
    }

    public String getPackageName() {
        return pkg;
    }

    public String getMarketName() {
        return name;
    }

    public String getAddressRate() {
        return rate;
    }

    public String getIntentAction() {
        return ri;
    }

}

