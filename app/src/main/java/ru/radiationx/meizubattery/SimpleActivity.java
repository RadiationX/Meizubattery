package ru.radiationx.meizubattery;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Scanner;

/**
 * Created by radiationx on 26.10.16.
 */

public class SimpleActivity extends Activity {
    private Handler handler = new Handler();
    Scanner inFile;
    TextView textView;
    int i = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sact);
        textView = (TextView) findViewById(R.id.counter);
        cap = Double.parseDouble(getPreferences().getString("CAPACITY", "0"));

        time = Float.parseFloat(getPreferences().getString("CAP_TIME", "0"));
        setTitle(String.format(Locale.ENGLISH, "%(.2f mAh : %(.2f sec", cap, time));
        handler.post(repeat);
    }

    private File file = new File("/sys/class/power_supply/fuelgauge/current_now");
    private Runnable repeat = new Runnable() {
        @Override
        public void run() {
            repeat();
        }
    };
    double current_now = 0;
    double cap = 0;
    long last_time = System.currentTimeMillis();
    float time = 0;

    private void repeat() {
        try {
            inFile = new Scanner(file);
            while (inFile.hasNextLine())
                current_now = Math.abs(Integer.parseInt(inFile.nextLine()) / 1000.0d);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            inFile.close();
        }

        current_now /= 3600;
        cap += current_now;
        time += (System.currentTimeMillis() - last_time) / 1000.0f;
        last_time = System.currentTimeMillis();
        textView.setText(String.format(Locale.ENGLISH, "%(.2f mAh : %(.2f sec", cap, time));
        if (i % 5 == 0)
            setAppString();
        i++;
        handler.postDelayed(repeat, 1000);
    }

    public void setAppString() {
        boolean b = getPreferences().edit().putString("CAPACITY", Double.toString(cap)).putString("CAP_TIME", Float.toString(time)).commit();
        Log.d("kek", "result commit data : " + b);
    }

    private SharedPreferences preferences;

    public SharedPreferences getPreferences() {
        if (preferences == null)
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Clear").setIcon(android.R.drawable.ic_menu_close_clear_cancel).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                current_now = 0;
                cap = 0;
                last_time = System.currentTimeMillis();
                time = 0;
                setAppString();
                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }
}
