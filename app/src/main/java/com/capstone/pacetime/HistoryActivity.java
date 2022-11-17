package com.capstone.pacetime;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.pacetime.data.Breath;
import com.capstone.pacetime.data.Step;
import com.capstone.pacetime.databinding.ActivityHistoryBinding;
import com.capstone.pacetime.databinding.LayoutHistoryViewBinding;
import com.capstone.pacetime.viewmodel.RunDetailInfoViewModel;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    ActivityHistoryBinding binding;
    RunDataManager runDataManager;

    public interface OnItemClickListener {
        void onItemClicked(View view, RunInfo item, int position);
    }

    public class BindListViewAdapter extends RecyclerView.Adapter<BindListViewAdapter.RecyclerViewHolder> {

        private final ArrayList<RunInfo> itemList;

        //나중에 item 클릭 구현할 때

        private OnItemClickListener mListener;
        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */

        public BindListViewAdapter(ArrayList<RunInfo> items) {
            itemList = items;
        }


        public void setOnItemClickListener(OnItemClickListener listener){
            mListener = listener;
        }


        public class RecyclerViewHolder extends RecyclerView.ViewHolder {
            LayoutHistoryViewBinding itemBinding;

            public RecyclerViewHolder(LayoutHistoryViewBinding binding, final OnItemClickListener itemClickListener) {
                super(binding.getRoot());
                // Define click listener for the ViewHolder's View
                itemBinding = binding;
                itemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            itemClickListener.onItemClicked(v, itemList.get(position), position);
                        }
                    }
                });
            }

            public LayoutHistoryViewBinding getItemBinding() {
                return itemBinding;
            }

            void bindItem(RunDetailInfoViewModel item){
                itemBinding.setModel(item);
//                itemBinding.textviewRunDateTime.setText(item.getRunDateTime());
//                itemBinding.textviewRunStartPlace.setText(item.getRunStartPlace());
//                itemBinding.textviewRunDistance.setText(item.getRunDistance() + "km");
//                itemBinding.textviewRunPace.setText(item.getRunPace() + "m");
//                itemBinding.textviewRunHour.setText(item.getRunHour() + "s");
//                itemBinding.textviewIsBreathUsed.setText(item.getIsBreathUsed());

            }
        }

        // Create new views (invoked by the layout manager)
        @NonNull
        public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

            // Create a new view, which defines the UI of the list item
            LayoutHistoryViewBinding viewBinding = LayoutHistoryViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
            return new RecyclerViewHolder(viewBinding, mListener);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(@NonNull RecyclerViewHolder viewHolder, final int position) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.bindItem(new RunDetailInfoViewModel(itemList.get(position)));
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return itemList.size();
        }
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        runDataManager = RunDataManager.getInstance();
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //test용
        RunInfo runInfo1 = new RunInfo(30, true, null, OffsetDateTime.now(), OffsetDateTime.now(), null, new ArrayList<Step>(), 1.2f, 153, 332);
        RunInfo runInfo2 = new RunInfo(30, false, null, OffsetDateTime.now(), OffsetDateTime.now(), null, new ArrayList<Step>(), 2.4f, 163, 351);

        runDataManager.runInfoToFirebase(runInfo1);
        runDataManager.runInfoToFirebase(runInfo2);

//        ArrayList<LayoutHistoryViewItem> itemList = new ArrayList<LayoutHistoryViewItem>();
        ArrayList<RunInfo> runInfos = runDataManager.getRunInfos();
        Log.d("HISTORY_ACTIVITY", "runInfos size: " + runInfos.size());
        Log.v("HISTORY_ACTIVITY", "runInfos size: " + runInfos.size());
//        for (int i = 0; i < runInfos.size(); i++) {
//            LayoutHistoryViewItem item = new LayoutHistoryViewItem();
//            Log.i("HISTORY_ACTIVITY", "i = " + i);
//            item.setItem(runInfos.get(i));
//            item.setIndex(i);
//            itemList.add(item);


//            item.setRunDateTime("2022-11-10");
//            item.setRunStartPlace("Dongjakgu");
//            item.setRunDistance(Integer.toString(i));
//            item.setRunPace("4   " + i);
//            item.setRunHour("123:" + i);
//            item.setIsBreathUsed("0");
//            item.setIndex(i);
//            itemList.add(item);
//        }


        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        BindListViewAdapter adapter = new BindListViewAdapter(runInfos);
        adapter.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClicked(View view, RunInfo item, int position) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(HistoryActivity.this, ResultActivity.class);
                        intent.putExtra("index", runInfos.indexOf(item));
                        startActivity(intent);
//                        Log.v("ISCLICKED", "123123");
//                        Toast.makeText(HistoryActivity.this, "123123", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        binding.recyclerViewHistory.setAdapter(adapter);
    }
}
