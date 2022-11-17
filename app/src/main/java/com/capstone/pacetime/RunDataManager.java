package com.capstone.pacetime;

import android.hardware.camera2.params.BlackLevelPattern;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.capstone.pacetime.data.Breath;
import com.capstone.pacetime.data.Step;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class RunDataManager {
    private static final String TAG = "RUNDATAMANAGER";
    private RunInfo runInfo = new RunInfo();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//    private Queue<RunInfo> queue = new LinkedList<>(); // new도 해줘야 할듯.
    private boolean isLoading = false;
    private List<RunInfo> runInfos;

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

    public void runInfoToFirebase(RunInfo runInfo) {

        CollectionReference runDataStoreTest = firestore.collection("runDataStoreTest");

        Map<String, Object> runData = new HashMap<>();
        runData.put("runStartDateTime", runInfo.getStartDateTime());
        runData.put("runEndDateTime", runInfo.getEndDateTime());
        runData.put("runCourse", runInfo.getTrace()); //runInfo에 아직 장소가 없음.
        runData.put("runDistance", runInfo.getDistance());
        runData.put("runPace", runInfo.getPace());
        runData.put("runHour", runInfo.getRunningTime());
        runData.put("cadence", runInfo.getCadence());
        runData.put("stepCount", runInfo.getStepCount());
        runData.put("isBreathUsed", runInfo.getIsBreathUsed()); //runInfo에 아직 isBreathUsed가 없음. 혹은 runInfo.getBreathItems가 null인지 판단하는 방법도 있을 듯.
        runData.put("runBreathData", runInfo.getBreathItems());
        //flag 넣어야되나?
        //command 넣어야되나?

        runDataStoreTest.add(runData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.v(TAG, "Success");
                updateRunInfos(runInfo);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.v(TAG, "Failed");
            }
        });


//        runDataStoreTest.document(runInfo.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss.SSS"))).set(runData).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void unused) {
//                Log.v(TAG, "Success");
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.v(TAG, "Failed");
//            }
//        });
    }

    //아마 HistoryActivity에서 item 선택했을 때, 선택한 item의 정보를 RunInfo 형태로 반환해줄 때(바인딩 위해) 쓰지 않을까. item선택했을 때 startDateTime으로 식별하면 좋을 듯.
    public RunInfo firebaseToRunInfo(int itemIndex){
//        DocumentReference documentReference = firestore.collection("test").document(startDateTime);
//        isLoading = true;
//        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                runInfo.setStartDateTime(documentSnapshot.get("runStartDataTime", OffsetDateTime.class));
//                runInfo.setEndDateTime(documentSnapshot.get("runEndDataTime", OffsetDateTime.class));
////                runInfo.setRunningPlace(documentSnapshot.get("runStartPlace", point));
//                runInfo.setDistance(documentSnapshot.get("runDistance", Float.class));
//                runInfo.setPace(documentSnapshot.get("runPace", Long.class));
//                runInfo.setRunningTime(documentSnapshot.get("runHour", Long.class));
//                runInfo.setCadence(documentSnapshot.get("cadence", Integer.class));
////                runInfo.setStepCount(documentSnapshot.get("stepCount", List.class));
////                runinfo.setIsBreathUsed(documentSnapshot.get("isBreathUsed", Boolean.class));
////                runInfo.setBreathItems(documentSnapshot.get("runBreathData", List.class));
//                //flag 넣어야 되나?
//                //command 넣어야 되나?
//                isLoading = false;
//            }
//        }).addOnFailureListener(
//                new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        isLoading = false;
//                    }
//                }
//        );

        return runInfos.get(itemIndex);
    }


    public void allFirebaseToRunInfos(){
        firestore.collection("runDataStoreTest")
//                .orderBy("runStartDateTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                runInfos.add(documentDataToRunInfo(document));
                                runInfos.add(document.toObject(RunInfo.class));
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else{
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private RunInfo documentDataToRunInfo(QueryDocumentSnapshot document){
        Map<String, Object> runData = document.getData();
        RunInfo localRunInfo = new RunInfo((boolean) runData.get("isBreathUsed"));

//        RunInfo myRunInfo = document.toObject(RunInfo.class);
//        OffsetDateTime runStartDateTime = OffsetDateTime.of()

        //document.toObject()

//        localRunInfo.setStartDateTime((OffsetDateTime) runData.get("runStartDateTime"));
//        localRunInfo.setEndDateTime((OffsetDateTime) runData.get("runEndDateTime"));
//        localRunInfo.setTrace((List<Location>) runData.get("runCourse"));
//        localRunInfo.setDistance((float) runData.get("runDistance"));
//        localRunInfo.setPace((long) runData.get("runPace"));
//        localRunInfo.setRunningTime((long) runData.get("runHour"));
//        localRunInfo.setCadence((int) runData.get("cadence"));
////나중에 주석 풀기
//        localRunInfo.setStepCount((List<Step>) runData.get("stepCount"));
//        localRunInfo.setBreathItems((List<Breath>) runData.get("runBreathData"));

        return localRunInfo;
    }

    public void updateRunInfos(RunInfo addedRunInfo){
        runInfos.add(addedRunInfo);
    }

    public boolean checkIsLoading(){
        return isLoading;
    }

    public ArrayList<RunInfo> getRunInfos(){
        return (ArrayList<RunInfo>) runInfos;
    }

}
