package com.example.fota;

import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Vector;

public class StateManager {
    public
        List<String> ecuList;
        List<String> firmwareList;
        List<String> selectedEcuList;
        String selectedFirmware;
        String selectedEcu;
        String firmwareVersion;
        FirebaseDatabase database;
        DatabaseReference verRef;
        DatabaseReference ecuUpdateVef;
        String status;
        DatabaseReference statusRef;
        DatabaseReference rootRef;
        Button updateBtn;
        String percent;
        public static synchronized StateManager getInstance() {
        if (instance == null) {
            instance = new StateManager();
        }
        return instance;
    }
    private
        static StateManager instance;
        StateManager() {
            ecuList = new Vector<>();
            firmwareList = new Vector<>();
            selectedEcuList = new Vector<>();
        }
}
