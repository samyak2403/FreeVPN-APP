package com.samyak2403.freevpnapp.Activitys;

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
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.os.RemoteException;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.samyak2403.freevpnapp.R;
import com.samyak2403.freevpnapp.serverdata.Servers;

import java.util.ArrayList;
import java.util.HashMap;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNThread;

public class HomeActivity extends AppCompatActivity {


    // Drawer Setup
    DrawerLayout drawerLayout;
    LinearLayout navView;

    ImageView drawerButton, vpnButton;

    //Views

    LinearLayout serverButton;

    RecyclerView recyclerView;

    ImageView countryFlag;

    TextView countryName, ipText, statusText, ipText2, connectButtonText;

    // Ver
    ArrayList<HashMap<String, Object>> serverArrayList = new ArrayList<>();

    HashMap<String, Object> selectedServer = new HashMap<>();

    Dialog dialog;

    boolean isVpnConnected = false;
    SharedPreferences sharedPreferences;


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

    }

    private void loadSavedServer() {

        String serverData = sharedPreferences.getString("savedServer", "");
        if (serverData.equals("") || serverData.length() < 10) {
            return;
        }
        HashMap<String, Object> hashMap = new Gson().fromJson(serverData, new TypeToken<HashMap<String, Object>>() {
        }.getType());

        selectServer(hashMap);

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
                intent.putExtra(Intent.EXTRA_TEXT, "https://github.com/samyak2403/IPTVmine");
                startActivity(Intent.createChooser(intent, "share"));
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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(HomeActivity.this, "Server not loaded because" + message, Toast.LENGTH_SHORT).show();
                    }
                });
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

                if (!intent.getStringExtra("state").equals("null")) {
                    statusText.setText("STATUS : " + intent.getStringExtra("state"));
                    if (!intent.getStringExtra("state").equals("CONNECTED")) {
                        onVpnConnected();
                    }
                }

            } catch (Exception e) {

            }
        }
    };

    private void onVpnConnected() {

        connectButtonText.setText("Disconnect");
        vpnButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_exit));
        isVpnConnected = true;

        vpnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnectVpn();
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
               if (selectedServer != null && selectedServer.size() != 0){
                   prepareVpn(selectedServer);
               }else {
                   Toast.makeText(HomeActivity.this, "Please Select a Server", Toast.LENGTH_SHORT).show();
               }
            }
        });

    }

    private void disconnectVpn() {

        OpenVPNThread.stop();

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
            speedText.setText(_data.get(_position).get("speed").toString());
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
}