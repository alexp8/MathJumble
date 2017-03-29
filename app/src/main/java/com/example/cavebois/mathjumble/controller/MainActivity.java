package com.example.cavebois.mathjumble.controller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.cavebois.mathjumble.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button play_button = (Button) findViewById(R.id.play_button);
        play_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Intent difficulty = new Intent(getApplicationContext(), DifficultyActivity.class);
                startActivity(difficulty);
            }
        });

        final Button scores_button = (Button) findViewById(R.id.scores_button);
        scores_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            }
        });
    }
}
