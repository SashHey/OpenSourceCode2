package com.example.opsc7312_poe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.opsc7312_poe.MapsActivity;
import java.util.ArrayList;

public class FavouritesActivity extends AppCompatActivity {


    Button backBtn;
    TextView txtFavourites;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        txtFavourites = findViewById(R.id.txtFavourites);
        for (String loc: MapsActivity.favouriteLocations) {
            txtFavourites.append(loc+"\n");
        }

        backBtn = findViewById(R.id.fav_back_button);
        backBtn.setOnClickListener(view -> {

            startActivity(new Intent(FavouritesActivity.this, MapsActivity.class));

            //finish();
        });
    }
}
