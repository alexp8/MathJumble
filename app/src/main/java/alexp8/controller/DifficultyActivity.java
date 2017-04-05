package alexp8.controller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.alexp8.mathjumble.R;

public class DifficultyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);

        final Button easy_button = (Button) findViewById(R.id.easy_button);
        easy_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent play = new Intent(getApplicationContext(), PlayActivity.class);
                play.putExtra("Difficulty", "Easy");
                startActivity(play);
            }
        });


        final Button normal_button = (Button) findViewById(R.id.normal_button);
        normal_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent play = new Intent(getApplicationContext(), PlayActivity.class);
                play.putExtra("Difficulty", "Normal");
                startActivity(play);
            }
        });

        final Button hard_button = (Button) findViewById(R.id.hard_button);
        hard_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent play = new Intent(getApplicationContext(), PlayActivity.class);
                play.putExtra("Difficulty", "Hard");
                startActivity(play);
            }
        });

        final Button close_button = (Button) findViewById(R.id.close_button);
        close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent menu = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(menu);
                finish();
            }
        });
    }
}
