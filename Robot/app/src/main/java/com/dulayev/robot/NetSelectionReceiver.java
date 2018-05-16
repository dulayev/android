package com.dulayev.robot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by dulayev on 20.01.17.
 */

/* if connected to some predefined SSID wifi network:
    store its BSSID,
    activate vibro mode;

    if disconnected from network and its BSSID is stored
    activate loud mode
 */

public class NetSelectionReceiver extends BroadcastReceiver {
    private static final String TAG = NetSelectionReceiver.class.getSimpleName();

    final static private ArrayList<String> _silentNetworks =
            new ArrayList<>(Arrays.asList("Tango-EMP", "SPB1-GUEST")); //"NAPALM", 
    private String _connectedNetwork; // which caused to enter vibro mode

    public NetSelectionReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive");

        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

            Log.d(TAG, "NETWORK_STATE_CHANGED_ACTION");

            NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected()) {
                WifiInfo wifiInfo = (WifiInfo)intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);

                Log.d(TAG, "Connected network: " + wifiInfo.getSSID());
                Log.d(TAG, new Integer(wifiInfo.getSSID().length()).toString());
                Log.d(TAG, new Boolean(_silentNetworks.contains(wifiInfo.getSSID())).toString());
                Log.d(TAG, wifiInfo.getSSID().equals("NAPALM") ? "T" : "F");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                Set<String> muted = prefs.getStringSet("muted", new TreeSet<String>());

                for(int i = 0; i < _silentNetworks.size(); i++) {
                    Log.d(TAG, String.format("list[%d] is %s ---  %d", i, _silentNetworks.get(i), _silentNetworks.get(i).equals(wifiInfo.getSSID().toString()) ? 1 : 0));
                }

                if(wifiInfo != null && muted.contains(wifiInfo.getSSID()/*.replace("\"", "")*/)) {

                    Log.d(TAG, "111 Connected network caused vibrate mode: " + wifiInfo.getSSID() + ";bin=" + _connectedNetwork);

                    _connectedNetwork = intent.getStringExtra(WifiManager.EXTRA_BSSID);

                    AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

                    Log.d(TAG, "Connected network caused vibrate mode: " + wifiInfo.getSSID() + ";bin=" + _connectedNetwork);
                }
            }
            else if(Objects.equals(_connectedNetwork, intent.getStringExtra(WifiManager.EXTRA_BSSID)) &&
                            networkInfo.getState() == NetworkInfo.State.DISCONNECTED) {

                Log.d(TAG, "Network disconnected:");

                _connectedNetwork = null;

                AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        }
    }

    @Override
    public IBinder peekService(Context myContext, Intent service) {
        return super.peekService(myContext, service);
    }
}
