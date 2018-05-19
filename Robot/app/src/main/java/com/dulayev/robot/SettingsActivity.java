package com.dulayev.robot;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SettingsActivity extends ListActivity {

    SharedPreferences prefs;

    Set<String> muted = null;

    ArrayAdapter<String> adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        List<String> networks = new ArrayList<String>();

        WifiManager wifi_manager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> configs = wifi_manager.getConfiguredNetworks();

        if (configs != null) {
            for (WifiConfiguration config : configs) {
                networks.add(config.SSID);
            }
        }

        muted = prefs.getStringSet("muted", new TreeSet<String>());
        Log.d(Utils.TAG, "onCreate:" + Utils.formatStringSet(muted));

        adapter = new ArrayAdapter<String>(this, R.layout.layout, R.id.text1, networks) {
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final View view = super.getView(position, convertView, parent);

                CheckBox check_box = (CheckBox)view.findViewById(R.id.check1);

                check_box.setChecked(muted.contains(getItem(position)));

                check_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        TextView text_view = (TextView)view.findViewById(R.id.text1);
                        if (isChecked) {
                            muted.add((String)text_view.getText());
                        } else {
                            muted.remove((String)text_view.getText());
                        }
                        notifyDataSetChanged();
                    }
                });

                return view;
            }
        };

        setListAdapter(adapter);
    }

    @Override
    protected void onPause() {

        Log.d(Utils.TAG, "onPause:" + Utils.formatStringSet(muted));

        SharedPreferences.Editor editor = prefs.edit();

        editor.remove("muted");
        editor.apply(); // workaround for Google bug of not saving string set
        editor.putStringSet("muted", muted);
        editor.apply();

        super.onPause();
    }

    @Override
    protected void onResume() {
        muted = prefs.getStringSet("muted", new TreeSet<String>());
        adapter.notifyDataSetChanged();

        Log.d(Utils.TAG, "onResume:" + Utils.formatStringSet(muted));

        super.onResume();
    }
}
