package com.example.opsc7312_poe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText mUsername, mEmail, mPassword;
    private Button regButton, loginButton;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();

    public static User newUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mUsername = findViewById(R.id.edtUsername);
        mEmail = findViewById(R.id.edtEmail);
        mPassword = findViewById(R.id.edtPassword);
        regButton = findViewById(R.id.btnRegister);
        loginButton = findViewById(R.id.btnLogin2);

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = mUsername.getText().toString();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                String measurement = "Imperial";

                DatabaseReference userRef = db.getReference().child("Users").child("name");

                db.getReference().child("Users").child("Username").setValue(username);
                db.getReference().child("Users").child("Email").setValue(email);
                db.getReference().child("Users").child("Password").setValue(password);
                db.getReference().child("Users").child("Measurement").setValue(measurement);

                newUser = new User(username, email, password, measurement);
//                Log.d("TAG", "newUser: " + newUser.username
//                        + " " + newUser.email
//                        + " " + newUser.password);

                Intent intentProfile = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intentProfile);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentProfile = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intentProfile);
            }
        });



    }
}