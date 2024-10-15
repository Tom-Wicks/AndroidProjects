package com.example.cw2_geotracker.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cw2_geotracker.R;
import com.example.cw2_geotracker.ReminderDB.Reminder;
import com.example.cw2_geotracker.ReminderDB.ReminderDao;
import com.example.cw2_geotracker.ReminderDB.ReminderDatabase;
import com.example.cw2_geotracker.Utilities.DialogUtility;
import com.example.cw2_geotracker.Utilities.TagUtility;

import java.util.ArrayList;
import java.util.List;

//Screen for displaying all reminders in a list
public class ReminderListActivity extends AppCompatActivity {

    ReminderDatabase db;
    ReminderDao dao;
    ReminderAdapter adapter;
    TagUtility tagUtility;
    DialogUtility dialogUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_list);

        db = ReminderDatabase.getDatabase(getApplicationContext());
        dao = db.reminderDao();
        adapter = new ReminderAdapter(getApplicationContext());
        tagUtility = new TagUtility(this);
        dialogUtility = new DialogUtility(this);

        ReminderDatabase.databaseExecutor.execute(() -> {
            List<Reminder> list = dao.getReminderList();
            adapter.setData(list);
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.reminderRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    //Help button
    public void onReminderHelpClick(View v) {
        dialogUtility.showDialog(R.string.reminder_help_message);
    }

    public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
        private List<Reminder> data;
        private Context context;
        private LayoutInflater layoutInflater;
        private View.OnClickListener onClickListener;
        public ReminderAdapter(Context context) {
            this.data = new ArrayList<>();
            this.context = context;
            this.layoutInflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.reminder_list_item, parent, false);
            return new ReminderViewHolder(itemView);
        }
        @Override
        public void onBindViewHolder(ReminderViewHolder holder, int position) {
            holder.bind(data.get(position));

            //Locate button, passes position of reminder to main activity as a result
            holder.locateView.setOnClickListener(v -> {
                Intent intent = new Intent(ReminderListActivity.this, MainActivity.class);
                intent.putExtra("latitude", holder.latitude);
                intent.putExtra("longitude", holder.longitude);
                setResult(Activity.RESULT_OK, intent);
                finish();
            });

            //Delete button, removes the reminder from the database
            holder.deleteView.setOnClickListener(v -> {
                //Confirmation message
                new AlertDialog.Builder(ReminderListActivity.this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.delete_message)
                        .setPositiveButton(R.string.delete_yes, (dialog, id) -> {
                            // If yes, delete reminder and remove from list
                            ReminderDatabase.databaseExecutor.execute(() -> {
                                dao.delete(holder.id);
                            });
                            adapter.data.remove(holder.getAdapterPosition());
                            adapter.notifyItemRemoved(holder.getAdapterPosition());
                            dialog.dismiss();
                        })
                        //If no, just close dialog
                        .setNegativeButton(R.string.delete_no, (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }
        @Override
        public int getItemCount() {
            return data.size();
        }
        public void setData(List<Reminder> newData) {
            if (data != null) {
                data.clear();
                data.addAll(newData);
                notifyDataSetChanged();
            } else {
                data = newData;
            }
        }
        class ReminderViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ImageView tagView;
            ImageButton locateView;
            ImageButton deleteView;
            double latitude;
            double longitude;
            String tag;
            int id;
            ReminderViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.reminderText);
                tagView = itemView.findViewById(R.id.reminderTag);
                locateView = itemView.findViewById(R.id.reminderLocateButton);
                deleteView = itemView.findViewById(R.id.reminderDeleteButton);
            }
            void bind(final Reminder reminder) {
                if (reminder != null) {
                    textView.setText(reminder.getText());
                    tag = reminder.getTag();
                    latitude = reminder.getLatitude();
                    longitude = reminder.getLongitude();
                    id = reminder.getId();
                    //Get tag image
                    tagView.setImageDrawable(tagUtility.getTagIcon(reminder.getTag()));
                }
            }
        }
    }
}