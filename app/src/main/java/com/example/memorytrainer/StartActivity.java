package com.example.memorytrainer;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ActionBar actionBar = getSupportActionBar();
        String actionBarColor = "#" + Integer.toHexString(getResources().getColor(R.color.actionbar_background_dark_blue)).substring(2);
        Log.i("zzz", actionBarColor);
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(actionBarColor)));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    public void startGame(View view) {
        Intent startGameIntent = new Intent(this, MainActivity.class);
        startActivity(startGameIntent);
    }
}
