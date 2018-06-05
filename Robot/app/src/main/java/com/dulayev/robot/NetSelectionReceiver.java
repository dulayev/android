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

        StringBuilder sb = new StringBuilder();
        sb.append(intent.getAction());
        sb.append('\n');

        if (intent.hasExtra(WifiManager.EXTRA_NETWORK_INFO)) {
            NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            sb.append("type=");
            sb.append(networkInfo.getTypeName());
            sb.append(";extra=");
            sb.append(networkInfo.getExtraInfo());
            //Utils.Log("Reason", networkInfo.getReason());
            sb.append(";state=");
            sb.append(networkInfo.getState());
            sb.append(":");
            sb.append(networkInfo.getDetailedState());

            sb.append("\nflags: ");

            if (networkInfo.isAvailable()) sb.append(",Available");
            if (networkInfo.isConnected()) sb.append(",Connected");
            if (networkInfo.isConnectedOrConnecting()) sb.append(",ConnectedOrConnecting");
            if (networkInfo.isFailover()) sb.append(",Failover");
            if (networkInfo.isRoaming()) sb.append(",Roaming");
        }

        if (intent.hasExtra(WifiManager.EXTRA_WIFI_INFO)) {
            WifiInfo wifiInfo = (WifiInfo)intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
            sb.append("\nSSID="); sb.append(wifiInfo.getSSID());
            sb.append(";BSSID="); sb.append(wifiInfo.getBSSID());
        }
        Utils.Log(sb.toString());

        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

            NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                WifiInfo wifiInfo = (WifiInfo)intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                Set<String> muted = prefs.getStringSet("muted", new TreeSet<String>());

                Log.d(Utils.TAG, "Connected network: " + wifiInfo.getSSID());
                Log.d(Utils.TAG, "Muted networks: " + Utils.formatStringSet(muted));

                if(wifiInfo != null && muted.contains(wifiInfo.getSSID())) {

                    _connectedNetwork = wifiInfo.getBSSID();

                    AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

                    Log.d(Utils.TAG, "Connected network caused vibrate mode: " + wifiInfo.getSSID() + ";bin=" + _connectedNetwork);
                }
            } else if(networkInfo.getState() == NetworkInfo.State.DISCONNECTED) {

                Utils.Log("Disconnected: storedNetwork is:", _connectedNetwork);
                if (Objects.equals(_connectedNetwork, intent.getStringExtra(WifiManager.EXTRA_BSSID))) {

                    Log.d(Utils.TAG, "Network caused silence is disconnected, turn audio on");

                    _connectedNetwork = null;

                    AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                }
            }
        }
    }
}
