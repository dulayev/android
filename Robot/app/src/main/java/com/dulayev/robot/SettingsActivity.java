package com.dulayev.robot;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.ResourceCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.ArraySet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SettingsActivity extends ListActivity {

    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final MatrixCursor matrix_cursor = new MatrixCursor(new String[] { "_id", "Enabled", "Name" });
        matrix_cursor.addRow(new Object[] { 1, 1, "Flat" });
        matrix_cursor.addRow(new Object[] { 2, 0, "Priest" });

        // http://www.mysamplecode.com/2012/07/android-listview-checkbox-example.html
        /*
        ListAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.layout,
                matrix_cursor,
                new String[] { "Enabled", "Name" },
                new int[] { R.id.check1, R.id.text1 },
                0
        );
        */
        class Item {
            public boolean Enabled;
            public String Name;

            public Item(
                boolean enabled,
                String name)
            {
                this.Enabled = enabled;
                this.Name = name;
            }

            @Override
            public String toString() {
                return Name;
            }
        };

        List<Item> list = new ArrayList<Item>();

        String[] networks = { "N1", "N2", "N3" };

        //list.add(new Item(true, "Flat"));
        //list.add(new Item(false, "Priest"));

        final Set<String> muted = prefs.getStringSet("muted", new TreeSet<String>());

        for(String network : networks) {

        }

        ListAdapter adapter = new ArrayAdapter<String>(this, R.layout.layout, R.id.text1, networks) {
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                CheckBox check_box = (CheckBox)view.findViewById(R.id.check1);

                //final Item item = getItem(position);

                check_box.setChecked(muted.contains(getItem(position)));

                return view;
            }
        };

        /*
        ListAdapter adapter = new ResourceCursorAdapter(this, R.layout.layout, matrix_cursor, 0) {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {

                CheckBox check_box = (CheckBox)view.findViewById(R.id.check1);
                check_box.setChecked(cursor.getInt(1) != 0);
                check_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        matrix_cursor.
                    }
                });
                ((TextView)view.findViewById(R.id.text1)).setText(cursor.getString(2));
            }
        };
        */
        setListAdapter(adapter);
    }
}
