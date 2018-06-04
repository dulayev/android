package com.dulayev.robot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Map;
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

    private String _connectedNetwork; // which caused to enter vibro mode

    public NetSelectionReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Utils.TAG, "onReceive");

        Utils.Log("action", intent.getAction());
        if (intent.hasExtra(WifiManager.EXTRA_NETWORK_INFO)) {
            NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            Utils.Log("TypeName", networkInfo.getTypeName());
            Utils.Log("SubtypeName", networkInfo.getSubtypeName());
            Utils.Log("ExtraInfo", networkInfo.getExtraInfo());
            Utils.Log("Reason", networkInfo.getReason());
            Utils.Log("State", networkInfo.getState());
            Utils.Log("DetailedState", networkInfo.getDetailedState());
            Utils.Log("Available", networkInfo.isAvailable());
            Utils.Log("Connected", networkInfo.isConnected());
            Utils.Log("ConnectedOrConnecting", networkInfo.isConnectedOrConnecting());
            Utils.Log("Failover", networkInfo.isFailover());
            Utils.Log("Roaming", networkInfo.isRoaming());
        }

        if (intent.hasExtra(WifiManager.EXTRA_WIFI_INFO)) {
            WifiInfo wifiInfo = (WifiInfo)intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
            Utils.Log("SSID", wifiInfo.getSSID());
            Utils.Log("BSSID", wifiInfo.getBSSID());
            Utils.Log("NetworkId", wifiInfo.getNetworkId());
        }

        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

            Log.d(Utils.TAG, "NETWORK_STATE_CHANGED_ACTION");

            NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected()) {
                WifiInfo wifiInfo = (WifiInfo)intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                Set<String> muted = prefs.getStringSet("muted", new TreeSet<String>());

                Log.d(Utils.TAG, "Connected network: " + wifiInfo.getSSID());
                Log.d(Utils.TAG, "Muted networks: " + Utils.formatStringSet(muted));

                if(wifiInfo != null && muted.contains(wifiInfo.getSSID())) {

                    _connectedNetwork = intent.getStringExtra(WifiManager.EXTRA_BSSID);

                    AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

                    Log.d(Utils.TAG, "Connected network caused vibrate mode: " + wifiInfo.getSSID() + ";bin=" + _connectedNetwork);
                }
            }
            else if(Objects.equals(_connectedNetwork, intent.getStringExtra(WifiManager.EXTRA_BSSID)) &&
                            networkInfo.getState() == NetworkInfo.State.DISCONNECTED) {

                Log.d(Utils.TAG, "Network caused silince is disconnected, turn audio on");

                _connectedNetwork = null;

                AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        }
    }
}
