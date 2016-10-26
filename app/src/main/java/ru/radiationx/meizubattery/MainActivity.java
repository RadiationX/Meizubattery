package ru.radiationx.meizubattery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    //BibleChapterView bibleChapterView;
    private GraphView graphVolt, graphCur;
    private TextView textView;
    private BatteryInfo info = new BatteryInfo();
    private final static Pattern p = Pattern.compile("POWER_SUPPLY_([^=]*?)=(.*)");
    private Handler handler = new Handler();
    private SharedPreferences preferences;
    private int i = 1;
    private Matcher matcher;
    private Set<String> thisData;
    private boolean started = false;
    private String stringData = "";
    private List<String> dataList = new ArrayList<>();

    private LineGraphSeries<DataPoint> voltNowSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> voltAvgSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> curNowSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> curAvgSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> curFullSeries = new LineGraphSeries<>();

    private LineGraphSeries<DataPoint> capSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> tempSeries = new LineGraphSeries<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text);
        //bibleChapterView = (BibleChapterView) findViewById(R.id.static_lay);
        graphVolt = (GraphView) findViewById(R.id.graphVolt);
        graphCur = (GraphView) findViewById(R.id.graphCur);

        graphVolt.getViewport().setXAxisBoundsManual(true);
        graphVolt.getViewport().setMinX(0);
        graphVolt.getViewport().setMaxX(50);
        graphVolt.getGridLabelRenderer().setLabelVerticalWidth(100);
        graphVolt.getSecondScale().setMinY(0);
        graphVolt.getSecondScale().setMaxY(100);
        graphVolt.getLegendRenderer().setVisible(true);
        graphVolt.getLegendRenderer().setFixedPosition(0, 0);
        graphVolt.getViewport().setScrollable(true);
        graphVolt.getViewport().setScalable(true);

        graphCur.getViewport().setXAxisBoundsManual(true);
        graphCur.getViewport().setMinX(0);
        graphCur.getViewport().setMaxX(50);
        graphCur.getGridLabelRenderer().setLabelVerticalWidth(100);
        graphCur.getSecondScale().setMinY(0);
        graphCur.getSecondScale().setMaxY(100);
        graphCur.getLegendRenderer().setVisible(true);
        graphCur.getLegendRenderer().setFixedPosition(0, 0);
        graphCur.getViewport().setScrollable(true);
        graphCur.getViewport().setScalable(true);

        graphVolt.addSeries(voltNowSeries);
        graphVolt.addSeries(voltAvgSeries);
        graphCur.addSeries(curNowSeries);
        graphCur.addSeries(curAvgSeries);
        graphCur.addSeries(curFullSeries);
        //graphVolt.getSecondScale().addSeries(capSeries);
        //graphCur.getSecondScale().addSeries(tempSeries);

        voltNowSeries.setTitle("VoltNow");
        voltAvgSeries.setTitle("VoltAvg");
        curNowSeries.setTitle("CurNow");
        curAvgSeries.setTitle("CurAvg");
        curFullSeries.setTitle("CurChrg");
        //capSeries.setTitle("Capacity");
        //tempSeries.setTitle("Temp");

        voltNowSeries.setColor(Color.argb(255, 255, 128, 128));
        curNowSeries.setColor(Color.argb(255, 255, 128, 128));
        curFullSeries.setColor(Color.MAGENTA);
        //capSeries.setColor(Color.GREEN);
        //tempSeries.setColor(Color.GREEN);

        stringData = getPreferences().getString("DATALIST", "");
        if (!stringData.isEmpty()) {
            String[] items = stringData.split(";");
            for (String item : items) {
                String[] fields = item.split(":");
                //capSeries.appendData(new DataPoint(i, (double) Math.abs(Float.parseFloat(fields[0]))), true, i);
                //tempSeries.appendData(new DataPoint(i, (double) Math.abs(Float.parseFloat(fields[1]))), true, i);
                voltNowSeries.appendData(new DataPoint(i, (double) Math.abs(Float.parseFloat(fields[2]))), true, i);
                voltAvgSeries.appendData(new DataPoint(i, (double) Math.abs(Float.parseFloat(fields[3]))), true, i);
                curNowSeries.appendData(new DataPoint(i, (double) Math.abs(Float.parseFloat(fields[4]))), true, i);
                curAvgSeries.appendData(new DataPoint(i, (double) Math.abs(Float.parseFloat(fields[5]))), true, i);
                curFullSeries.appendData(new DataPoint(i, (double) Math.abs(Float.parseFloat(fields[6]))), true, i);
                dataList.add(item);
                i++;
            }
        }

        handler.post(repeat);
        started = true;
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Action").setIcon(android.R.drawable.ic_media_pause).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (started) {
                    handler.removeCallbacks(repeat);
                    started = false;
                    item.setIcon(android.R.drawable.ic_media_play);
                } else {
                    handler.post(repeat);
                    started = true;
                    item.setIcon(android.R.drawable.ic_media_pause);
                }
                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    private File file = new File("/sys/class/power_supply/fuelgauge/uevent");
    private BufferedReader reader;
    private Runnable repeat = new Runnable() {
        @Override
        public void run() {
            repeat();
        }
    };
    Scanner inFile;

    private void repeat() {
        try {
            inFile = new Scanner(file);
            while (inFile.hasNextLine()) {
                matcher = p.matcher(inFile.nextLine());
                if (matcher.find()) {
                    setData(matcher.group(1), matcher.group(2));
                }
            }
            //capSeries.appendData(new DataPoint(i, (double) Math.abs(info.getCapacity())), true, i);
            //tempSeries.appendData(new DataPoint(i, (double) Math.abs(info.getTemp())), true, i);
            voltNowSeries.appendData(new DataPoint(i, (double) Math.abs(info.getVoltageNow())), true, i);
            voltAvgSeries.appendData(new DataPoint(i, (double) Math.abs(info.getVoltageAvg())), true, i);
            curNowSeries.appendData(new DataPoint(i, (double) Math.abs(info.getCurrentNow())), true, i);
            curAvgSeries.appendData(new DataPoint(i, (double) Math.abs(info.getCurrentAvg())), true, i);
            curFullSeries.appendData(new DataPoint(i, (double) Math.abs(info.getCurrentCharge())), true, i);
            String data = String.valueOf(info.getCapacity()).concat(":")
                    .concat(String.valueOf(info.getTemp())).concat(":")
                    .concat(String.valueOf(info.getVoltageNow())).concat(":")
                    .concat(String.valueOf(info.getVoltageAvg())).concat(":")
                    .concat(String.valueOf(info.getCurrentNow())).concat(":")
                    .concat(String.valueOf(info.getCurrentAvg())).concat(":")
                    .concat(String.valueOf(info.getCurrentCharge()));
            dataList.add(data);

            if (!stringData.isEmpty()) stringData = stringData.concat(";");
            stringData = stringData.concat(data);

            if (dataList.size() >= 200) freePrefsData();

            if (i % 5 == 0)
                setAppString(getPreferences(), stringData);

            i++;
            handler.postDelayed(repeat, 1000);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void setAppString(SharedPreferences prefs, String val) {
        boolean b = prefs.edit().putString("DATALIST", val).commit();
        Log.d("kek", "result commit data : " + b);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setAppString(getPreferences(), stringData);
    }

    private void freePrefsData() {
        List<String> data = new ArrayList<>();
        int j = 0;
        for (String s : dataList) {
            data.add(s);
            if (j > 25)
                break;
            j++;
        }
        //Log.d("kek", "data for delete size " + data.size());
        for (String s : data) {
            stringData = stringData.replace(s.concat(";"), "");
            int index = dataList.indexOf(s);
            if (index != -1)
                dataList.remove(index);
        }
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            Log.d("kek", "BroadcastReceiver RECEIVE! ");
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
        }
    };
    boolean charging = false;


    public SharedPreferences getPreferences() {
        if (preferences == null)
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences;
    }

    private void setData(String name, String argValue) {
        float value;
        float coef;
        switch (name) {
            case BatteryInfo.PRESENT:
                info.setPresent(Integer.parseInt(argValue));
                break;
            case BatteryInfo.CAPACITY:
                info.setCapacity(Integer.parseInt(argValue));
                break;
            case BatteryInfo.TEMP:
                info.setTemp(Float.parseFloat(argValue) / 10);
                break;
            case BatteryInfo.VOLTAGE_NOW:
                info.setVoltageNow(Float.parseFloat(argValue) / 1000000);
                break;
            case BatteryInfo.VOLTAGE_AVG:
                info.setVoltageAvg(Float.parseFloat(argValue) / 1000000);
                break;
            case BatteryInfo.CURRENT_NOW:
                value = Float.parseFloat(argValue) / 1000;
                float chrg = getChargeCur(info.getCapacity(), info.getVoltageNow());
                info.setCurrentCharge(charging ? Math.max(getChargeCur(info.getCapacity(), info.getVoltageNow()), Math.abs(value)) : 0);
                Log.d("kek", "compare currents " + value + " : " + info.getCurrentCharge()+ " : "+chrg);
                info.setCurrentNow(charging ? Math.max(Math.abs(value) + info.getCurrentCharge(), chrg) : value);
                break;
            case BatteryInfo.CURRENT_AVG:
                value = Float.parseFloat(argValue) / 1000;
                info.setCurrentAvg(charging ? value - 1000 : value);
                break;
            case BatteryInfo.HEALTH:
                info.setHealth(argValue);
                break;
            case BatteryInfo.MANUFACTURER:
                info.setManufacturer(argValue);
                break;
        }
    }

    private float getChargeCur(int cap, float volt) {
        float f = 1000;
        if (volt > 3.9) {
            f = (float) (-(9 / (105 - 1.03f * (cap - 4))) * Math.sqrt(volt - 3.9f) + 1.0f);
        }
        f = Math.max(f, 0);
        f = Math.min(f, 1000);
        Log.d("kek", "charge cur [" + cap + ", " + volt + "] " + f);
        return f;
    }
}
