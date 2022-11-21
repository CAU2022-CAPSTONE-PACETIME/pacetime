package com.capstone.pacetime;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.pacetime.data.Step;
import com.capstone.pacetime.databinding.ActivityHistoryBinding;
import com.capstone.pacetime.databinding.LayoutHistoryViewBinding;
import com.capstone.pacetime.viewmodel.RunDetailInfoViewModel;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private final String TAG = "HISTORY_ACTIVITY";
    ActivityHistoryBinding binding;
    RunDataManager runDataManager;
    BindListViewAdapter adapter;
    private Handler firebaseHandler;
    private HandlerThread handlerThread;
    private ArrayList<RunInfo> runInfos = new ArrayList<>();

    private boolean isLoadFinished = true;

    private final Object lock = new Object();

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
//        RunInfoParser runInfoParser1 = new RunInfoParser(new RunInfoParser.OffsetDateTimeParser(OffsetDateTime.now()), new RunInfoParser.OffsetDateTimeParser(OffsetDateTime.now()), null, null, new ArrayList<Step>(), 1.2f, 153, 332, 43, false);
//        RunInfoParser runInfoParser2 = new RunInfoParser(new RunInfoParser.OffsetDateTimeParser(OffsetDateTime.now()), new RunInfoParser.OffsetDateTimeParser(OffsetDateTime.now()), null, null, new ArrayList<Step>(), 2.4f, 163, 351, 20, true);
        RunInfoParser runInfoParser1 = new RunInfoParser(new RunInfoParser.OffsetDateTimeParser(OffsetDateTime.of(2001, 2, 3, 4, 35, 2, 41300000, ZoneOffset.of("+10:00"))), new RunInfoParser.OffsetDateTimeParser(OffsetDateTime.now()), null, null, new ArrayList<Step>(), 1.2f, 153, 332, 43, false, new RunInfoParser.OffsetDateTimeParser(OffsetDateTime.of(2001, 2, 3, 4, 35, 2, 41300000, ZoneOffset.of("+10:00"))).getDateEpochSecond());
        RunInfoParser runInfoParser2 = new RunInfoParser(new RunInfoParser.OffsetDateTimeParser(OffsetDateTime.of(2002, 3, 4, 11, 32, 1, 126000000, ZoneOffset.of("+08:00"))), new RunInfoParser.OffsetDateTimeParser(OffsetDateTime.now()), null, null, new ArrayList<Step>(), 2.4f, 163, 351, 20, true, new RunInfoParser.OffsetDateTimeParser(OffsetDateTime.of(2002, 3, 4, 11, 32, 1, 126000000, ZoneOffset.of("+08:00"))).getDateEpochSecond());


        handlerThread = new HandlerThread("data storing and loading thread");
        handlerThread.start();
        firebaseHandler = new Handler(handlerThread.getLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
            }
        };

        Runnable runnableLoad = new Runnable() {
            @Override
            public void run() {
                while (runDataManager.getIsLoading()){
                    synchronized (lock){
                        try {
                            lock.wait();
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
                runInfos = runDataManager.getRunInfos();
                adapter = new BindListViewAdapter(runInfos);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        };
        firebaseHandler.post(runnableLoad);

        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                runDataManager.runInfoToFirebase(runInfoParser1);
                while (runDataManager.getIsAddLoading()) {
                    synchronized (lock) {
                        try {
                            lock.wait(100);
                            Log.d("RUNDATAMANAGER", "success" + runDataManager.getRunInfos().size());
                        } catch (InterruptedException e) {
                            Log.d("RUNDATAMANAGER", "failed" + runDataManager.getRunInfos().size());
                            e.printStackTrace();
                        }
                    }
                }
//                isLoadFinished = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemInserted(0);
                    }
                });
            }
        };
        firebaseHandler.post(runnable1);

        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                runDataManager.runInfoToFirebase(runInfoParser2);
                while (runDataManager.getIsAddLoading()) {
                    synchronized (lock) {
                        try {
                            lock.wait(100);
                            Log.d("RUNDATAMANAGER", "success" + runDataManager.getRunInfos().size());
                        } catch (InterruptedException e) {
                            Log.d("RUNDATAMANAGER", "failed" + runDataManager.getRunInfos().size());
                            e.printStackTrace();
                        }
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemInserted(0);
                    }
                });
            }
        };

        firebaseHandler.post(runnable2);


        LinearLayoutManager layoutManager =  new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//        layoutManager.scrollToPositionWithOffset(0, 0);
        binding.recyclerViewHistory.setLayoutManager(layoutManager);
        new Thread(() -> {
            while (runDataManager.getIsLoading()) {
                synchronized (lock) {
                    try {
                        lock.wait(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            runOnUiThread(() -> {
                adapter.setOnItemClickListener(
                        new OnItemClickListener() {
                            @Override
                            public void onItemClicked(View view, RunInfo item, int position) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(HistoryActivity.this, ResultActivity.class);
                                        intent.putExtra("index", position);
                                        startActivity(intent);
                                    }
                                });
                            }
                        }
                );
                binding.recyclerViewHistory.setAdapter(adapter);
            });

        }).start();
    }
}
