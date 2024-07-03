package com.example.fota;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class MainActivity extends AppCompatActivity {

    private Spinner ecuSpinner;
    private ImageButton checkBtn;
    private Spinner firmwareSpinner;
    private TextView warningText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        StateManager stateManager = StateManager.getInstance();

        // Firebase realtime database
        stateManager.database = FirebaseDatabase.getInstance();
        stateManager.rootRef = stateManager.database.getReference("ECU");

        // Firebase storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        ecuSpinner = findViewById(R.id.ecuSpinner);
        firmwareSpinner = findViewById(R.id.firmwareSpinner);
        stateManager.updateBtn = findViewById(R.id.updateBtn);
        stateManager.rstBtn = findViewById(R.id.rstBtn);
        checkBtn = findViewById(R.id.checkBtn);
        warningText = findViewById(R.id.warningText);
        stateManager.ecuList.clear();
        stateManager.ecuList.add("Please select an ECU");

        stateManager.rootRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String key = snapshot.getKey();
                stateManager.ecuList.add(key);

                ArrayAdapter<String> ecuAdapter = new ArrayAdapter<>(
                        MainActivity.this,
                        android.R.layout.simple_spinner_item,
                        stateManager.ecuList
                );
                ecuAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                ecuSpinner.setAdapter(ecuAdapter);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        // ECU spinner
        ecuSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                stateManager.selectedEcu = (String)adapterView.getItemAtPosition(position);
                System.out.println("selectedEcu: " + stateManager.selectedEcu);
                if (!"Please select an ECU".equals(stateManager.selectedEcu)) {
                    stateManager.verRef = stateManager.database.getReference("ECU/" + stateManager.selectedEcu + "/version");
                    System.out.println("verRef: " + stateManager.verRef);
                    stateManager.ecuUpdateVef = stateManager.database.getReference("ECU_UPDATE");
                    stateManager.ecuResetRef = stateManager.database.getReference("ECU_RESET");
                    System.out.println("ResetRef: " + stateManager.ecuResetRef);
                    // firmware list
                    StorageReference firmwareRef = storageRef.child(stateManager.selectedEcu);
                    stateManager.firmwareList.clear();
                    stateManager.firmwareList.add("Please select firmware");
                    firmwareRef.listAll().addOnSuccessListener(listResult -> {
                        for (StorageReference item : listResult.getItems()) {
                            System.out.println("firmware: " + item.getName());
                            stateManager.firmwareList.add(item.getName());
                        }

                        ArrayAdapter<String> firmwareAdapter = new ArrayAdapter<>(
                                MainActivity.this,
                                android.R.layout.simple_spinner_item,
                                stateManager.firmwareList
                        );
                        firmwareAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        firmwareSpinner.setAdapter(firmwareAdapter);
                    });

                } else {
                    stateManager.firmwareList.clear();
                    ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(
                            MainActivity.this,
                            android.R.layout.simple_spinner_item,
                            new Vector<>()
                    );
                    emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    firmwareSpinner.setAdapter(emptyAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        firmwareSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                stateManager.selectedFirmware = (String)adapterView.getItemAtPosition(position);
                if (!"Please select firmware".equals(stateManager.selectedFirmware)) {
                    System.out.println("selectedFirmware: " + stateManager.selectedFirmware);
                    stateManager.firmwareVersion = getVerFirmware(stateManager.selectedFirmware);
                    System.out.println("version: " + stateManager.firmwareVersion);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        stateManager.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!"Please select an ECU".equals(stateManager.selectedEcu) && !"Please select firmware".equals(stateManager.selectedFirmware)) {
                    stateManager.verRef.setValue(stateManager.firmwareVersion);
                    String ecu_update = stateManager.selectedEcu;
                    System.out.println("Update");
                    stateManager.ecuUpdateVef.setValue(stateManager.selectedEcu);

                    stateManager.statusRef = stateManager.database.getReference("ECU/" + stateManager.selectedEcu + "/status");
                    stateManager.statusRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String newStatus = snapshot.getValue(String.class);
                            if (!newStatus.equals(stateManager.status)) {
                                stateManager.status = newStatus;

                                System.out.println("main status: " + stateManager.status);
                                if (stateManager.status.equals("UPDATE")) {
                                    stateManager.selectedEcuList.add(ecu_update);
                                    System.out.println("ADDED: " + ecu_update);
                                    Intent intent = new Intent(MainActivity.this, FLashActivity.class);
                                    startActivity(intent);
                                } else if (stateManager.status.equals("REJECT")) {

                                    LayoutInflater inflater = getLayoutInflater();
                                    View layout = inflater.inflate(R.layout.custom_toast,
                                            null);

                                    TextView text = (TextView) layout.findViewById(R.id.textView);
                                    text.setText("Update is rejected!");

                                    Toast toast = new Toast(getApplicationContext());
                                    toast.setGravity(Gravity.TOP, 0, 300);
                                    toast.setDuration(Toast.LENGTH_LONG);
                                    toast.setView(layout);
                                    toast.show();
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }
        });

        stateManager.rstBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!"Please select an ECU".equals(stateManager.selectedEcu)) {
                    String ecu_update = stateManager.selectedEcu;
                    System.out.println("Reset");
                    stateManager.ecuResetRef.setValue(stateManager.selectedEcu);
                }
            }
        });

        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FLashActivity.class);
                startActivity(intent);
            }
        });
    }

    public String getVerFirmware(String selectedFirmware) {
        int startIndex = selectedFirmware.indexOf("_") + 1;
        int endIndex = selectedFirmware.indexOf(".hex");
        if (endIndex == -1) endIndex = selectedFirmware.indexOf(".bin");
        String version = selectedFirmware.substring(startIndex, endIndex);
        return version;
    }
}