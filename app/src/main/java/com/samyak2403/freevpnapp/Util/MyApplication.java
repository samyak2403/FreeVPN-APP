package com.samyak2403.freevpnapp.Util;



import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.samyak2403.freevpnapp.R;

import arrowwould.in.admobopenads.AppOpenManager;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this);

        new AppOpenManager(this, getString(R.string.open_ads));

    }

}
