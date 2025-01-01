package com.samyak2403.freevpnapp.Activitys;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.samyak2403.freevpnapp.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Back button functionality
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Info button functionality
        ImageView infoButton = findViewById(R.id.infoButton);
        infoButton.setOnClickListener(v ->
                Toast.makeText(AboutActivity.this, "🌐 Free VPN: Protect your data and browse securely!", Toast.LENGTH_LONG).show()
        );

        // Load Banner Ad
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
}
