package com.capstone.pacetime.util;

import android.util.Log;

import androidx.annotation.NonNull;

import com.capstone.pacetime.data.RunInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunDataManager {
    private static final String TAG = "RUNDATAMANAGER";
    private RunInfoParser runInfoParser = new RunInfoParser();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private boolean isLoading = false;
    private boolean isAddLoading = false;
    private final List<RunInfo> runInfos;

    private static RunDataManager instance;

    public static RunDataManager getInstance(){
        if(instance == null){
            instance = new RunDataManager();
        }
        return instance;
    }

    public void destory(){
        instance = null;
    }

    private RunDataManager(){
        runInfos = new ArrayList<>();
    }

    public void runInfoToFirebase(RunInfoParser runInfoParser) {

        isAddLoading = true;
        CollectionReference runDataStoreTest = firestore.collection("runDataStoreTest");

        Map<String, Object> runData = new HashMap<>();
        runData.put("startDateTime", runInfoParser.getStartDateTime());
        runData.put("endDateTime", runInfoParser.getEndDateTime());
        runData.put("trace", runInfoParser.getTrace()); //runInfo에 아직 장소가 없음.
        runData.put("distance", runInfoParser.getDistance());
        runData.put("pace", runInfoParser.getPace());
        runData.put("runningTime", runInfoParser.getRunningTime());
        runData.put("cadence", runInfoParser.getCadence());
        runData.put("stepCount", runInfoParser.getStepCount());
        runData.put("isBreathUsed", runInfoParser.getIsBreathUsed()); //runInfo에 아직 isBreathUsed가 없음. 혹은 runInfo.getBreathItems가 null인지 판단하는 방법도 있을 듯.
        runData.put("breathItems", runInfoParser.getBreathItems());
        runData.put("dateEpochSecond", runInfoParser.getDateEpochSecond());

        runInfos.add(0, runInfoParser.parserToOrigin());

        Log.d(TAG, "runinfos size = " + runInfos.size());
        runDataStoreTest.document("" + runInfos.size()).set(runData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.v(TAG, "Success");
                Log.d(TAG, "runinfos size why = " + runInfos.size());
                RunDataManager.this.isAddLoading = false;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.v(TAG, "Failed");
                synchronized (runInfos){
                    runInfos.remove(0);
                }
                RunDataManager.this.isAddLoading = false;
            }
        });
    }

    //아마 HistoryActivity에서 item 선택했을 때, 선택한 item의 정보를 RunInfo 형태로 반환해줄 때(바인딩 위해) 쓰지 않을까. item선택했을 때 startDateTime으로 식별하면 좋을 듯.
    public RunInfo firebaseToRunInfo(int itemIndex){
        RunInfo runInfo;
        synchronized (runInfos){
            runInfo = runInfos.get(itemIndex);
        }
        return runInfo;
    }

    public void allFirebaseToRunInfos(){
        isLoading = true;
        firestore.collection("runDataStoreTest")
                .orderBy("dateEpochSecond", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    RunInfoParser runInfoParserTemp = document.toObject(RunInfoParser.class);
                                    RunInfo runInfo = runInfoParserTemp.parserToOrigin();
                                    runInfos.add(runInfo);
                                    Log.d(TAG, "runInfos size:" + runInfos.size());
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                }
                            } else{
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                            isLoading = false;
                    }
                });
    }

//    public void updateRunInfos(RunInfo addedRunInfo){
//        runInfos.add(addedRunInfo);
//    }

    public boolean getIsLoading(){
        return isLoading;
    }

//    public void setIsLoading(boolean isLoading){
//        this.isLoading = isLoading;
//    }

    public boolean getIsAddLoading(){
        return isAddLoading;
    }

//    public void setIsAddLoading(boolean isAddLoading){
//        this.isAddLoading = isAddLoading;
//    }

    public ArrayList<RunInfo> getRunInfos(){
        return (ArrayList<RunInfo>) runInfos;
    }

}
