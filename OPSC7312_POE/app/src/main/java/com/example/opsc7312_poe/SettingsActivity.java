package com.example.opsc7312_poe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {

    Switch IMSwitch;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();

    Button btnSubmitSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        RegisterActivity reg = new RegisterActivity();
        IMSwitch = findViewById(R.id.measuringSwitch);
        btnSubmitSettings = findViewById(R.id.btnSubmitSettings);

        if(reg.newUser.measurement == "Imperial"){
            IMSwitch.setText("Imperial");
            db.getReference().child("Users").child("Measurement").setValue("Imperial");
        }
        else if(reg.newUser.measurement == "Metric"){
            IMSwitch.setText("Metric");
            db.getReference().child("Users").child("Measurement").setValue("Metric");
        }

        btnSubmitSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(IMSwitch.isChecked()){
                    IMSwitch.setText("Imperial");
                    reg.newUser.measurement = "Imperial";
                    db.getReference().child("Users").child("Measurement").setValue("Imperial");
                }
                else if(!IMSwitch.isChecked()){
                    IMSwitch.setText("Metric");
                    reg.newUser.measurement = "Metric";
                    db.getReference().child("Users").child("Measurement").setValue("Metric");
                }
                startActivity(new Intent(SettingsActivity.this, MapsActivity.class));

            }
        });
    }
}