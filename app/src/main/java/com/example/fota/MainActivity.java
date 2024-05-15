package com.example.fota;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Vector;


public class MainActivity extends AppCompatActivity {
    private List<String> ecuList = new Vector<>();
    private List<String> firmwareList = new Vector<>();
    private Spinner ecuSpinner;
    private Button updateBtn;
    private Spinner firmwareSpinner;
    private String selectedFirmware;
    private String selectedEcu;
    private String firmwareVersion;
    private DatabaseReference verRef;

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

        // Firebase realtime database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference();

        // Firebase storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        ecuSpinner = findViewById(R.id.ecuSpinner);
        firmwareSpinner = findViewById(R.id.firmwareSpinner);
        updateBtn = findViewById(R.id.updateBtn);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ecuList.clear();
                for (DataSnapshot msnapshot : snapshot.getChildren()) {
                    String key = msnapshot.getKey();
                    ecuList.add(key);
                    System.out.println("Node Name (Key): " + key);
                }

                ArrayAdapter<String> ecuAdapter = new ArrayAdapter<>(
                        MainActivity.this,
                        android.R.layout.simple_spinner_item,
                        ecuList
                );
                ecuAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                ecuSpinner.setAdapter(ecuAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ////
            }
        });


        // ECU spinner
        ecuSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedEcu = (String)adapterView.getItemAtPosition(position);
                System.out.println("selectedEcu: " + selectedEcu);
                verRef = database.getReference(selectedEcu + "/version");
                System.out.println("verRef: " + verRef);

                // firmware list
                StorageReference firmwareRef = storageRef.child(selectedEcu);
                firmwareList.clear();
                firmwareRef.listAll().addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                            System.out.println("firmware: " + item.getName());
                            firmwareList.add(item.getName());
                    }

                    ArrayAdapter<String> firmwareAdapter = new ArrayAdapter<>(
                            MainActivity.this,
                            android.R.layout.simple_spinner_item,
                            firmwareList
                    );
                    firmwareAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    firmwareSpinner.setAdapter(firmwareAdapter);
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        firmwareSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedFirmware = (String)adapterView.getItemAtPosition(position);
                System.out.println("selectedFirmware: " + selectedFirmware);
                firmwareVersion = getVerFirmware(selectedFirmware);
                System.out.println("version: " + firmwareVersion);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verRef.setValue(firmwareVersion);
                System.out.println("Update");
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