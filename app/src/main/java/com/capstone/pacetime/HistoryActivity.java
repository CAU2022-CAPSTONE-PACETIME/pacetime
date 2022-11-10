package com.capstone.pacetime;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.pacetime.databinding.ActivityHistoryBinding;
import com.capstone.pacetime.databinding.LayoutHistoryViewBinding;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

//    LinearLayoutManager linearLayoutManager;
    ActivityHistoryBinding binding;
//    LayoutHistoryViewBinding viewBinding;

    public class LayoutHistoryView{
        private String runTime;
        private String runStartPlace;
        private String runDistance;
        private String runPace;
        private String runHour;
        private String isBreathUsed;

        public LayoutHistoryView(){
            runTime ="";
            runStartPlace = "";
            runDistance = "";
            runPace = "";
            runHour = "";
            isBreathUsed = "";
        }

        public String getRunTime() {
            return runTime;
        }

        public String getRunStartPlace() {
            return runStartPlace;
        }

        public String getRunDistance() {
            return runDistance;
        }

        public String getRunPace() {
            return runPace;
        }

        public String getRunHour() {
            return runHour;
        }

        public String getIsBreathUsed() {
            return isBreathUsed;
        }

        public void setRunTime(String runTime) {
            this.runTime = runTime;
        }

        public void setRunStartPlace(String runStartPlace) {
            this.runStartPlace = runStartPlace;
        }

        public void setRunDistance(String runDistance) {
            this.runDistance = runDistance;
        }

        public void setRunPace(String runPace) {
            this.runPace = runPace;
        }

        public void setRunHour(String runHour) {
            this.runHour = runHour;
        }

        public void setIsBreathUsed(String isBreathUsed) {
            this.isBreathUsed = isBreathUsed;
        }
    }

    public interface OnItemClickListener {
        void onItemClicked(View view, LayoutHistoryView item, int position);
    }
//

    public class BindListViewAdapter extends RecyclerView.Adapter<BindListViewAdapter.RecyclerViewHolder> {

        private final ArrayList<LayoutHistoryView> itemList;
        Context context;
        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */

        //나중에 item 클릭 구현할 때 
        private OnItemClickListener mListener;
//
        public void setOnItemClickListener(OnItemClickListener listener){
            mListener = listener;
        }

        public BindListViewAdapter(Context context, ArrayList<LayoutHistoryView> items) {
            this.context = context;
            itemList = items;
        }



        public class RecyclerViewHolder extends RecyclerView.ViewHolder {
            LayoutHistoryViewBinding itemBinding;
//
//            public ViewHolder(LayoutHistoryViewBinding binding) {
//                super(binding.getRoot());
//                // Define click listener for the ViewHolder's View
//                itemBinding = binding;
//            }

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

//            public TextView getTextView() {
//                return textView;
//            }


            public LayoutHistoryViewBinding getItemBinding() {
                return itemBinding;
            }

            void bindItem(LayoutHistoryView item){
                itemBinding.textviewRunTime.setText(item.getRunTime());
                itemBinding.textviewRunStartPlace.setText(item.getRunStartPlace());
                itemBinding.textviewRunDistance.setText(item.getRunDistance());
                itemBinding.textviewRunPace.setText(item.getRunPace());
                itemBinding.textviewRunHour.setText(item.getRunHour());
                itemBinding.textviewIsBreathUsed.setText(item.getIsBreathUsed());



            }
        }


        /**
         * Initialize the dataset of the Adapter.
         *
         * @param dataSet String[] containing the data to populate views to be used
         * by RecyclerView.
         */
        public BindListViewAdapter(ArrayList<LayoutHistoryView> dataSet) {
            itemList = dataSet;
        }

        // Create new views (invoked by the layout manager)
        @NonNull
        public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            // Create a new view, which defines the UI of the list item


//            View view = LayoutInflater.from(viewGroup.getContext())x
//                    .inflate(R.layout.layout_history_view, viewGroup, false);


            LayoutHistoryViewBinding viewBinding = LayoutHistoryViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
            return new RecyclerViewHolder(viewBinding, mListener);

//            return new ViewHolder(view);
        }

//        @NonNull
//        @Override
//        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
//            LayoutHistoryViewBinding viewBinding = LayoutHistoryViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
//            return new ViewHolder(viewBinding);
//        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(@NonNull RecyclerViewHolder viewHolder, final int position) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.bindItem(itemList.get(position));
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
//        setContentView(R.layout.activity_history);

//        binding = DataBindingUtil.setContentView(this, R.layout.activity_history);
//        binding.setHistoryactivity(this);
//        viewBinding = LayoutHistoryViewBinding.inflate(getLayoutInflater());
//        View view = viewBinding.getRoot();
//        setContentView(view);
//
//        ArrayList<String> list = new ArrayList<String>();//임시로
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        ArrayList<LayoutHistoryView> itemList = new ArrayList<LayoutHistoryView>();
        for (int i=0; i<100; i++) {
            LayoutHistoryView item = new LayoutHistoryView();
            item.setRunTime("2022-11-10");
            item.setRunStartPlace("Dongjakgu");
            item.setRunDistance(Integer.toString(i));
            item.setRunPace("4   " + Integer.toString(i));
            item.setRunHour("123:" + Integer.toString(i));
            item.setIsBreathUsed("0");
            itemList.add(item);
        }


        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        BindListViewAdapter adapter = new BindListViewAdapter(itemList);
        adapter.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClicked(View view, LayoutHistoryView item, int position) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.v("ISCLICKED", "123123");
                        Toast.makeText(HistoryActivity.this, "123123", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

//        binding.recyclerViewHistory.setAdapter(new BindListViewAdapter(itemList));
        binding.recyclerViewHistory.setAdapter(adapter);
//
//        linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        binding.recyclerViewHistory.setLayoutManager(linearLayoutManager);
//        binding.recyclerViewHistory.
//
//        BindListViewAdapter adapter = new BindListViewAdapter(itemList);
    }
}
