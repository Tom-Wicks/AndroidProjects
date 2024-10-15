package com.example.cw2_geotracker.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.example.cw2_geotracker.MetricDB.MetricDay;
import com.example.cw2_geotracker.MetricDB.MetricDayDao;
import com.example.cw2_geotracker.MetricDB.MetricDayDatabase;
import com.example.cw2_geotracker.MyLocation.MyLocationService;
import com.example.cw2_geotracker.R;
import com.example.cw2_geotracker.Utilities.DialogUtility;

import java.util.ArrayList;
import java.util.List;

//Screen for viewing metrics, as well as turning off the service if so desired
public class MetricListActivity extends AppCompatActivity {

    MetricDayDatabase db;
    MetricDayDao dao;
    MetricAdapter adapter;

    private Switch serviceSwitch;
    private SharedPreferences sharedPreferences;
    private String preFile = "com.example.cw2_geotracker";
    DialogUtility dialogUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metric_list);

        db = MetricDayDatabase.getDatabase(getApplicationContext());
        dao = db.metricDayDao();
        adapter = new MetricAdapter(getApplicationContext());
        dialogUtility = new DialogUtility(this);

        MetricDayDatabase.databaseExecutor.execute(() -> {
            List<MetricDay> list = dao.getDayList();
            adapter.setData(list);
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.metricRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        //Switch represents whether the service is enabled or not
        serviceSwitch = findViewById(R.id.metricBgSwitch);
        sharedPreferences = getSharedPreferences(preFile, MODE_PRIVATE);
        //Set to current preference on activity start
        serviceSwitch.setChecked(sharedPreferences.getBoolean("serviceOn",true));

        //Switch handling based on https://www.geeksforgeeks.org/switch-in-android/
        //Available under CCBY-SA license
        serviceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!sharedPreferences.getBoolean("exerciseOn", false)) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                //If toggled on, start service
                if (isChecked) {
                    editor.putBoolean("serviceOn", true);
                    Intent intent = new Intent(MetricListActivity.this, MyLocationService.class);
                    startForegroundService(intent);
                } else {
                    //If toggled off, stop service
                    editor.putBoolean("serviceOn", false);
                    Intent intent = new Intent(MetricListActivity.this, MyLocationService.class);
                    stopService(intent);
                }
                editor.apply();
            } else {
                //Do not allow switching during exercise (since it requires the service to function)
                dialogUtility.showDialog(R.string.metric_error_switch);
            }
        });
    }

    //Adapter for list of metrics by day
    public class MetricAdapter extends RecyclerView.Adapter<MetricAdapter.MetricViewHolder> {
        private List<MetricDay> data;
        private Context context;
        private LayoutInflater layoutInflater;
        private View.OnClickListener onClickListener;
        public MetricAdapter(Context context) {
            this.data = new ArrayList<>();
            this.context = context;
            this.layoutInflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public MetricViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.metric_list_item, parent, false);
            return new MetricViewHolder(itemView);
        }
        @Override
        public void onBindViewHolder(MetricViewHolder holder, int position) {
            holder.bind(data.get(position));
        }
        @Override
        public int getItemCount() {
            return data.size();
        }
        public void setData(List<MetricDay> newData) {
            if (data != null) {
                data.clear();
                data.addAll(newData);
                notifyDataSetChanged();
            } else {
                data = newData;
            }
        }
        class MetricViewHolder extends RecyclerView.ViewHolder {
            TextView dateView;
            TextView distanceView;
            TextView timeView;
            TextView reminderView;
            TextView speedView;
            MetricViewHolder(View itemView) {
                super(itemView);
                dateView = itemView.findViewById(R.id.metricDateText);
                distanceView = itemView.findViewById(R.id.metricDistanceText);
                timeView = itemView.findViewById(R.id.metricTimeText);
                reminderView = itemView.findViewById(R.id.metricReminderText);
                speedView = itemView.findViewById(R.id.metricSpeedText);
            }
            void bind(final MetricDay metric) {
                if (metric != null) {
                    //Display metrics in text
                    dateView.setText(metric.getDate());
                    distanceView.setText(getString(R.string.metric_distance, metric.getDistance()/1000));
                    //Formatting time
                    int seconds = (int) metric.getTime();
                    int ss = seconds % 60;
                    int mm = (seconds % 3600) / 60;
                    int hh = seconds / 3600;
                    timeView.setText(getString(R.string.metric_time, hh,mm,ss));
                    reminderView.setText(getString(R.string.metric_reminders, metric.getReminders()));
                    speedView.setText(getString(R.string.metric_speed, metric.getSpeed()));
                }
            }
        }
    }
}