package alexp8.controller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.alexp8.mathjumble.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final LinearLayout layout = (LinearLayout) findViewById(R.id.difficulty_buttons_layout);

        final Button play_button = (Button) findViewById(R.id.play_button);
        play_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            layout.setVisibility(View.VISIBLE);
            }
        });

        final Button scores_button = (Button) findViewById(R.id.scores_button);
        scores_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            }
        });

        setupDifficultyButtons();
    }

    private void setupDifficultyButtons() {
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
    }
}
