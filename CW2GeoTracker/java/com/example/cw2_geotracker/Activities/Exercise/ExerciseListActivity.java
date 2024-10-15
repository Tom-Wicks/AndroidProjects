package com.example.cw2_geotracker.Activities.Exercise;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cw2_geotracker.ExerciseDB.Exercise;
import com.example.cw2_geotracker.ExerciseDB.ExerciseDao;
import com.example.cw2_geotracker.ExerciseDB.ExerciseDatabase;
import com.example.cw2_geotracker.R;
import com.example.cw2_geotracker.Utilities.TagUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExerciseListActivity extends AppCompatActivity {

    ExerciseDatabase db;
    ExerciseDao dao;
    ExerciseAdapter adapter;
    TagUtility tagUtility;

    //Used to track list filtering, to avoid unnecessary data changes
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_list);

        db = ExerciseDatabase.getDatabase(getApplicationContext());
        dao = db.exerciseDao();
        adapter = new ExerciseAdapter(getApplicationContext());
        tagUtility = new TagUtility(this);

        //Get all exercises by default
        type = "all";
        ExerciseDatabase.databaseExecutor.execute(() -> {
            List<Exercise> list = dao.getExerciseList();
            runOnUiThread(() -> adapter.setData(list));
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.exerciseRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    //Filter selections
    public void onAllTabClick(View v) {
        checkAndSet("all");
    }

    public void onWalkTabClick(View v) {
        checkAndSet("walk");
    }

    public void onRunTabClick(View v) {
        checkAndSet("run");
    }

    public void onCycleTabClick(View v) {
        checkAndSet("cycle");
    }

    //Set list filter
    private void checkAndSet(String thisType) {
        //If current list filter is not the selected type
        if (!Objects.equals(type, thisType)) {
            type = thisType;
            //Get all exercises of this type (or all), and reset adapter
            ExerciseDatabase.databaseExecutor.execute(() -> {
                List<Exercise> list;
                if (type == "all") {
                    list = dao.getExerciseList();
                } else {
                    list = dao.getExercisesByType(thisType);
                }
                runOnUiThread(() -> adapter.setData(list));
            });
        }
    }

    //Adapter to place exercises in recycler
    public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {
        private List<Exercise> data;
        private Context context;
        private LayoutInflater layoutInflater;
        private View.OnClickListener onClickListener;
        public ExerciseAdapter(Context context) {
            this.data = new ArrayList<>();
            this.context = context;
            this.layoutInflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public ExerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.exercise_list_item, parent, false);
            return new ExerciseViewHolder(itemView);
        }
        @Override
        public void onBindViewHolder(ExerciseViewHolder holder, int position) {
            holder.bind(data.get(position));

            //Each exercise on list has a button, this sets it
            holder.viewButton.setOnClickListener(v -> {
                //Go to page
                Intent intent = new Intent(ExerciseListActivity.this, ExerciseInfoActivity.class);
                //Pass this exercise to the info screen, so it displays this exercise's info
                intent.putExtra("exercise", holder.ex);
                startActivity(intent);
            });
        }
        @Override
        public int getItemCount() {
            return data.size();
        }
        public void setData(List<Exercise> newData) {
            if (data != null) {
                data.clear();
                data.addAll(newData);
                notifyDataSetChanged();
            } else {
                data = newData;
            }
        }

        class ExerciseViewHolder extends RecyclerView.ViewHolder {
            TextView titleText;
            TextView dateText;
            ImageView typeImage;
            ImageButton viewButton;
            Exercise ex;

            ExerciseViewHolder(View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.exerciseListTitleText);
                dateText = itemView.findViewById(R.id.exerciseListDateText);
                typeImage = itemView.findViewById(R.id.exerciseListTypeImage);
                viewButton = itemView.findViewById(R.id.exerciseListViewButton);
            }
            void bind(final Exercise exercise) {
                if (exercise != null) {
                    titleText.setText(exercise.getName());
                    dateText.setText(exercise.getDate());
                    //Use this exercise's type string to get the icon and display it
                    typeImage.setImageDrawable(tagUtility.getTypeIcon(exercise.getType()));
                    ex = exercise;
                }
            }
        }
    }
}