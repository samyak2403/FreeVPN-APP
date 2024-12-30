package com.samyak2403.freevpnapp.Activitys;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.samyak2403.freevpnapp.R;

public class HomeActivity extends AppCompatActivity {


    // Drawer Setup
    DrawerLayout drawerLayout;
    LinearLayout navView;

    ImageView drawerButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        drawerLayout = findViewById(R.id.drawerLayout);
        navView = findViewById(R.id.navView);
        drawerButton = findViewById(R.id.drawerButton);

        setupDrawer();

        drawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }

     private void setupDrawer(){

        LinearLayout drawerYoutube = navView.findViewById(R.id.drawerYoutube);
        LinearLayout drawerTelegram = navView.findViewById(R.id.drawerTelegram);
        LinearLayout drawerInstagram = navView.findViewById(R.id.drawerInstagram);
        LinearLayout drawerTutorial = navView.findViewById(R.id.drawerTutorial);
        LinearLayout drawerShare = navView.findViewById(R.id.drawerShare);
        LinearLayout drawerPrivacy = navView.findViewById(R.id.drawerPrivacy);
        LinearLayout drawerTerms = navView.findViewById(R.id.drawerTerms);
        LinearLayout drawerAbout = navView.findViewById(R.id.drawerAbout);



        drawerYoutube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(""));
                startActivity(intent);
            }
        });

         drawerTelegram.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(Intent.ACTION_VIEW);
                 intent.setData(Uri.parse(""));
                 startActivity(intent);
             }
         });

         drawerInstagram.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(Intent.ACTION_VIEW);
                 intent.setData(Uri.parse(""));
                 startActivity(intent);
             }
         });

         drawerTutorial.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(Intent.ACTION_VIEW);
                 intent.setData(Uri.parse(""));
                 startActivity(intent);
             }
         });

         drawerShare.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(Intent.ACTION_SEND);
                 intent.setType("text/plain");
                 intent.putExtra(Intent.EXTRA_TEXT,"https://github.com/samyak2403/IPTVmine");
                 startActivity(Intent.createChooser(intent,"share"));
             }
         });

         drawerPrivacy.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(Intent.ACTION_VIEW);
                 intent.setData(Uri.parse(""));
                 startActivity(intent);
             }
         });

         drawerTerms.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(Intent.ACTION_VIEW);
                 intent.setData(Uri.parse(""));
                 startActivity(intent);
             }
         });

         drawerAbout.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(Intent.ACTION_VIEW);
                 intent.setData(Uri.parse("https://github.com/samyak2403/IPTVmine"));
                 startActivity(intent);
             }
         });

     }
}