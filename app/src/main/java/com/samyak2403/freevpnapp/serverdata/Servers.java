package com.samyak2403.freevpnapp.serverdata;

import android.util.Base64;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Servers {


    Call mCall;


    public Servers() {

    }

    public void loadServers(ServerLoadListener serverLoadListener) {

        Request request = new Request.Builder().url("http://vpngate.net/api/iphone/").build();
        mCall = new OkHttpClient().newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                serverLoadListener.onServerFailed(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });


    }

    public void parseResponse(Response response, ServerLoadListener serverLoadListener) {

        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        try {
            inputStream = response.body().byteStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String data;


            while ((data = bufferedReader.readLine()) != null) {
                if (data.startsWith("*") && !data.startsWith("#")) {

                    loadServerData(serverLoadListener, data);

                }
            }

            inputStream.close();
            bufferedReader.close();
        } catch (IOException e) {
            serverLoadListener.onServerFailed(e.getMessage());
        }

    }

    public void loadServerData(ServerLoadListener serverLoadListener, String data) {

        HashMap<String, Object> hashMap = new HashMap<>();
        String[] serverData = data.split(",");

        hashMap.put("hostName", serverData[0]);
        hashMap.put("ipAddress", serverData[1]);
        hashMap.put("score", serverData[2]);
        hashMap.put("ping", serverData[3]);
        hashMap.put("speed", serverData[4]);
        hashMap.put("countryLong", serverData[5]);
        hashMap.put("countryShort", serverData[6]);
        hashMap.put("sessions", serverData[7]);
        hashMap.put("uptime", serverData[8]);
        hashMap.put("totalUsers", serverData[9]);
        hashMap.put("totalTraffic", serverData[10]);
        hashMap.put("logType", serverData[11]);
        hashMap.put("operator", serverData[12]);
        hashMap.put("message", serverData[13]);
        hashMap.put("oConfigData", new String(Base64.decode(serverData[14], Base64.DEFAULT)));


        String[] lines = hashMap.get("oConfigData").toString().split("[\\r\\n]+");
        hashMap.put("port", getPort(lines));
        hashMap.put("protocol", getProtocol(lines));


    }


    public static String getProtocol(String[] lines) {

        String protocol = "";
        for (String line : lines) {
            if (!line.startsWith("#")) {
                if (line.startsWith("proto")) {
                    protocol = line.split(" ")[1];
                }
            }
        }
        return protocol;

    }

    public static int getPort(String[] lines) {

        int port = 0;
        for (String line : lines) {
            if (!line.startsWith("#")) {
                if (line.startsWith("remote")) {
                    port = Integer.parseInt(line.split("")[2]);
                }
            }
        }
        return port;

    }

    public interface ServerLoadListener {

        void onServerLoaded(HashMap<String, Object> hashMap);

        void onServerFailed(String message);


    }

}
