package com.example.fota;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

public class FLashActivity extends AppCompatActivity {

    private List<ProgressBar> progressbarList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main2), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button returnButton = findViewById(R.id.returnBtn);
        StateManager stateManager = StateManager.getInstance();
        progressbarList = new Vector<>();
        LinearLayout layout = findViewById(R.id.progressLayout);

        for (String x : stateManager.selectedEcuList) {
            System.out.println("selected ecu l: " + x);

            LinearLayout horizontalLayout = new LinearLayout(this);
            horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
            horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            horizontalLayout.setPadding(70, 16, 16, 16); // Đặt padding nếu cần thiết

            TextView textView = new TextView(this);
            textView.setText(x);
            int textWidth = (int) (100 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(textWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

            textView.setLayoutParams(textLayoutParams);
            textView.setPadding(0, 0, 16, 0); // Đặt padding nếu cần thiết

            ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            int width = (int) (250 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);

            progressBar.setLayoutParams(layoutParams);
            progressBar.setMax(100);
            progressBar.setProgress(0);

            horizontalLayout.addView(textView);
            horizontalLayout.addView(progressBar);

            layout.addView(horizontalLayout);
            progressbarList.add(progressBar);

            DatabaseReference progressRef = stateManager.database.getReference("ECU/" + x + "/percent");

            progressRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String percent = snapshot.getValue(String.class);
                    int progressValue = Integer.parseInt(percent);
                    System.out.println("progress " + x +": " + percent);
                    progressBar.setProgress(progressValue);

                    if (progressValue == 100) {
                        stateManager.selectedEcuList.remove(x);
                        layout.removeView(horizontalLayout);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                finish();
                Intent intent = new Intent(FLashActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}