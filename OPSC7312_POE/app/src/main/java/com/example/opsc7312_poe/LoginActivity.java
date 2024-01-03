package com.example.opsc7312_poe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText mUsername, mPassword;
    private Button btnLogin, btnRegister;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference root = db.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsername = findViewById(R.id.lgnUsername);
        mPassword = findViewById(R.id.lgnPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister2);

        RegisterActivity reg = new RegisterActivity();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //String userUsername = reg.newUser.username, userPassword = reg.newUser.password;

                //.w("TAG:", "Username: " + userUsername);
                //Log.w("TAG:", "Password: " + userPassword);

                root.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(!mUsername.getText().toString().equals(snapshot.child("Users").child("Username").getValue())){
                            Toast.makeText(LoginActivity.this, "Incorrect Username", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else if(!mPassword.getText().toString().equals(snapshot.child("Users").child("Password").getValue())){
                            Toast.makeText(LoginActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Loading Maps",Toast.LENGTH_SHORT).show();
                            Intent intentProfile = new Intent(LoginActivity.this, MapsActivity.class);
                            startActivity(intentProfile);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentProfile = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intentProfile);
            }
        });
    }
}