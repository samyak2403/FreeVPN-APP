package com.samyak2403.freevpnapp.Activitys;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.samyak2403.freevpnapp.R;
import com.samyak2403.freevpnapp.Util.SharePrefs;
import com.samyak2403.freevpnapp.serverdata.Servers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;

public class HomeActivity extends AppCompatActivity {


    // Drawer Setup
    DrawerLayout drawerLayout;
    LinearLayout navView;

    ImageView drawerButton, vpnButton;

    //Views

    LinearLayout serverButton;

    RecyclerView recyclerView;

    ImageView countryFlag, settingButton;

    TextView countryName, ipText, statusText, ipText2, connectButtonText, dSpeedText, uSpeedText, app_Version;

    // Ver
    ArrayList<HashMap<String, Object>> serverArrayList = new ArrayList<>();

    HashMap<String, Object> selectedServer = new HashMap<>();

    Dialog dialog;

    boolean isVpnConnected = false;
    SharedPreferences sharedPreferences;

    SharePrefs sharePrefs;
    int adDelay = 0;

    InterstitialAd minterstitialAd;


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
        serverButton = findViewById(R.id.serverButton);
        countryFlag = findViewById(R.id.countryFlag);
        countryName = findViewById(R.id.countryName);
        ipText = findViewById(R.id.ipText);
        vpnButton = findViewById(R.id.vpnButton);
        statusText = findViewById(R.id.statusText);
        ipText2 = findViewById(R.id.ipText2);
        sharedPreferences = this.getSharedPreferences("ServerDetails", Activity.MODE_PRIVATE);
        connectButtonText = findViewById(R.id.connectButtonText);
        dSpeedText = findViewById(R.id.dSpeedText);
        uSpeedText = findViewById(R.id.uSpeedText);
        settingButton = findViewById(R.id.settingButton);
        app_Version = findViewById(R.id.app_Version);

        //setupDrawer
        setupDrawer();

        //loadServers
        loadServers();

        //loadSavedServer
        loadSavedServer();

        //drawerButton
        drawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        //serverButton
        serverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showServerLayout();
            }
        });

        //VPN Button
        vpnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedServer != null && selectedServer.size() != 0) {
                    prepareVpn(selectedServer);
                } else {
                    Toast.makeText(HomeActivity.this, "Please Select a Server", Toast.LENGTH_SHORT).show();
                }
            }
        });

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        checkVpnConnection();

        loadInterstitialAd();
        loadBannerAd();

        // Display app version
        String appVersion = getAppVersion();
        app_Version.setText(appVersion);

    }

    private String getAppVersion() {

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return "Version: " + packageInfo.versionName + " (" + packageInfo.versionCode + ")";
        } catch (PackageManager.NameNotFoundException e) {
            return "Version info not available";
        }
    }

    private void checkVpnConnection() {

        if (OpenVPNService.getStatus().equals("CONNECTED")) {
            onVpnConnected();
            statusText.setText("STATUS : CONNECTED");
        }

    }

    private void loadSavedServer() {

        String serverData = sharedPreferences.getString("savedServer", "");
        if (serverData.equals("") || serverData.length() < 10) {
            return;
        }
        HashMap<String, Object> hashMap = new Gson().fromJson(serverData, new TypeToken<HashMap<String, Object>>() {
        }.getType());

        selectServer(hashMap);
        ipText2.setText("IP : " + hashMap.get("ipAddress").toString());

    }

    private void showServerLayout() {

        dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar);

        View view = getLayoutInflater().inflate(R.layout.server_layout, null);
        dialog.setContentView(view);

        recyclerView = view.findViewById(R.id.recyclerView);
        ImageView backButton = view.findViewById(R.id.backButton);

        ImageView infoButton = view.findViewById(R.id.infoButton);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemViewCacheSize(1000);
        recyclerView.setAdapter(new ServerRecyclerAdapter(serverArrayList));


        AdView adView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);


        //back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


        //Credits
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Credits")
                        .setMessage("Credit goes to https://vpngate.net VpnGate website which providing the server for free. You visit their website by clicking in the visit website button.")
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton("Visit Website", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("https://vpngate.net"));
                                startActivity(intent);

                            }
                        }).show();
            }
        });


        dialog.show();

    }

    private void setupDrawer() {

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
                intent.setData(Uri.parse(getString(R.string.youttube_link)));
                startActivity(intent);
            }
        });

        drawerTelegram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.telegram_user_id)));
                startActivity(intent);
            }
        });

        drawerInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.instagram_id)));
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
                // Create an Intent to share the message
                Intent shareIntent = new Intent(Intent.ACTION_SEND);

                // Set the type of content to share
                shareIntent.setType("text/plain");

                // Get the package name dynamically
                String packageName = getPackageName();

                // Add an attractive message
                String message = "🚀 Discover amazing features with our app! \n" +
                        "📱 Download now and enjoy: Free VPN App \n\n" +
                        "👉 Click here to download: https://play.google.com/store/apps/details?id=" + packageName + "\n\n" +
                        "💡 Share this with your friends and family!";

                // Add the message to the Intent
                shareIntent.putExtra(Intent.EXTRA_TEXT, message);

                // Start the sharing activity
                startActivity(Intent.createChooser(shareIntent, "Share our app via"));
            }
        });


        drawerPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.Privacy_Policy)));
                startActivity(intent);
            }
        });

        drawerTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.Terms_Conditions)));
                startActivity(intent);
            }
        });

        drawerAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(HomeActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

    }


    private void loadServers() {
        Servers servers = new Servers();

        servers.loadServers(new Servers.ServerLoadListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onServerLoaded(HashMap<String, Object> hashMaps) {
                if (!serverArrayList.contains(hashMaps)) {
                    serverArrayList.add(hashMaps);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (recyclerView != null) {
                                recyclerView.getAdapter().notifyDataSetChanged();
                            }
                            if (serverArrayList.size() == 1) {
                                selectServer(hashMaps);
                            }
                        }
                    });

                }
            }

            @Override
            public void onServerLoadFailed(String message) {


            }
        });
    }

    private void selectServer(HashMap<String, Object> hashMap) {


        selectedServer = hashMap;

        countryName.setText(hashMap.get("countryLong").toString());

        ipText.setText(hashMap.get("ipAddress").toString());


        Glide.with(HomeActivity.this)
                .load("https://www.vpngate.net/images//flags/24/" + hashMap.get("countryShort").toString() + ".png")
                .into(countryFlag);

        sharedPreferences.edit().putString("savedServer", new Gson().toJson(hashMap)).apply();


        if (dialog != null) {
            dialog.dismiss();
        }


    }

    private void prepareVpn(HashMap<String, Object> hashMap) {

        //ads showing
        showInterstitialAd();

        Intent intent = VpnService.prepare(HomeActivity.this);
        if (intent != null) {
            vpnResult.launch(intent);

        } else {

            connectVpn(hashMap);

        }

    }

    private void connectVpn(HashMap<String, Object> hashMap) {


        try {

            OpenVpnApi.startVpn(this, hashMap.get("oConfigData").toString(), hashMap.get("countryShort").toString(), "vpn", "vpn");
            ipText2.setText("IP : " + hashMap.get("ipAddress").toString());

        } catch (RemoteException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private ActivityResultLauncher<Intent> vpnResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            if (result.getResultCode() == Activity.RESULT_OK) {

                connectVpn(selectedServer);

            } else {
                Toast.makeText(HomeActivity.this, "Permission must be granted", Toast.LENGTH_SHORT).show();
            }
        }
    });


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {

//                if (!intent.getStringExtra("state").equals("null")) {
//                    statusText.setText("STATUS : " + intent.getStringExtra("state"));
//                    if (intent.getStringExtra("state").equals("CONNECTED")) {
//                        onVpnConnected();
//                    }
//                    if (intent.getStringExtra("state").equals("DISCONNECTED")) {
//                        onVpnDisconnected();
//                    }
//
//                }

                String state = intent.getStringExtra("state");

                if (state != null && !state.equals("null")) { // Safely check for null
                    statusText.setText("STATUS : " + state);

                    if (state.equals("CONNECTED")) {
                        onVpnConnected();
                    } else if (state.equals("DISCONNECTED")) {
                        onVpnDisconnected();
                    }
                }


            } catch (Exception e) {

            }
            try {
                updateSpeed(intent.getStringExtra("byteOut"), intent.getStringExtra("byteIn"));
            } catch (Exception e) {
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(HomeActivity.this).unregisterReceiver(broadcastReceiver);
    }

    private void onVpnConnected() {

        connectButtonText.setText("Disconnect");
        vpnButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_exit));
        isVpnConnected = true;

        vpnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnectVpn();
                //ads
                showInterstitialAd();
            }
        });

    }

    private void onVpnDisconnected() {
        connectButtonText.setText("Connect");
        vpnButton.setImageDrawable(getResources().getDrawable(R.drawable.power_btn));
        isVpnConnected = false;

        vpnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedServer != null && selectedServer.size() != 0) {
                    prepareVpn(selectedServer);
                } else {
                    Toast.makeText(HomeActivity.this, "Please Select a Server", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void disconnectVpn() {

        OpenVPNThread.stop();
        updateSpeed("00.0 Mbps", "00.0 Mbps");

    }

    private void updateSpeed(String uploadSpeed, String downloadSpeed) {

        if (uploadSpeed == null || downloadSpeed == null) {
            return;
        }

        uSpeedText.setText(uploadSpeed);
        dSpeedText.setText(downloadSpeed);

    }


    public class ServerRecyclerAdapter extends RecyclerView.Adapter<ServerRecyclerAdapter.ViewHolder> {

        ArrayList<HashMap<String, Object>> _data;

        public ServerRecyclerAdapter(ArrayList<HashMap<String, Object>> arrayList) {
            _data = arrayList;


        }

        @Override
        public ServerRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater _inflate = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            View _v = _inflate.inflate(R.layout.server_adpter, null);
            View _v = LayoutInflater.from(parent.getContext()).inflate(R.layout.server_adpter, parent, false);

            RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            _v.setLayoutParams(_lp);


            return new ServerRecyclerAdapter.ViewHolder(_v);

        }


        @Override
        public void onBindViewHolder(ServerRecyclerAdapter.ViewHolder _holder, @SuppressLint("RecyclerView") final int _position) {
            View view = _holder.itemView;

            ImageView countryFlag = view.findViewById(R.id.countryFlag);
            TextView speedText = view.findViewById(R.id.speedText);
            TextView countryName = view.findViewById(R.id.countryName);
            TextView ipText = view.findViewById(R.id.ipText);
            LinearLayout serverLayout = view.findViewById(R.id.serverLayout);

            countryName.setText(_data.get(_position).get("countryLong").toString());
//            speedText.setText(_data.get(_position).get("speed").toString());


            // Ensure speed is formatted as 00.0 Mbps
            String speed = _data.get(_position).get("speed").toString();

            try {
                // Convert the speed string to a double
                double speedValue = Double.parseDouble(speed);

                // Adjust the value to display in Mbps
                double displaySpeed = speedValue / 1000.0; // Assuming the original speed is in Kbps

                // Format the speed to show as "2.0 Mbps" or similar
                String formattedSpeed = String.format("%.1f Mbps", displaySpeed);

                // Set the formatted text
                speedText.setText(formattedSpeed);
            } catch (NumberFormatException | NullPointerException e) {
                // If parsing fails, show a default value
                speedText.setText("0.0 Mbps");
            }


            ipText.setText(_data.get(_position).get("ipAddress").toString());


//            String imageUrl = ;

            Glide.with(HomeActivity.this)
                    .load("https://www.vpngate.net/images//flags/24/" + _data.get(_position).get("countryShort").toString() + ".png")
                    .into(countryFlag);


            serverLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectServer(_data.get(_position));
                }
            });


        }

        @Override
        public int getItemCount() {

            if (_data != null) {
                return _data.size();
            } else {
                return 0;
            }

        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View v) {
                super(v);

            }
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("connectionState");
        LocalBroadcastManager.getInstance(HomeActivity.this).registerReceiver(broadcastReceiver, intentFilter);

    }


    //ads
    private void loadBannerAd() {


    }

    private void loadInterstitialAd() {
        // Your interstitial ad loading logic here
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, getString(R.string.interstitial_ad), adRequest,
                new InterstitialAdLoadCallback() {

                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        minterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        minterstitialAd = null;
                    }


                });
    }

    private void showInterstitialAd() {
        if (minterstitialAd != null) {
            minterstitialAd.show(this);

            minterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    Log.d(TAG, "showInterstitial: fail");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    sharePrefs.putLong("lastAd", new Date().getTime() + (long) adDelay * 60 * 1000);
                    loadInterstitialAd();
                    Log.d(TAG, "showInterstitial: dismiss");

                }
            });
        } else {
            loadInterstitialAd();

        }
    }


}