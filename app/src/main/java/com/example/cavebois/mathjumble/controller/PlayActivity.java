package com.example.cavebois.mathjumble.controller;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.cavebois.mathjumble.R;
import com.example.cavebois.mathjumble.model.MathJumble;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;
import java.util.Timer;

/**
 * Play the game.
 */
public class PlayActivity extends AppCompatActivity {
    //time in milliseconds for game to be played
    private static final long START_TIME = 30 * 1000;
    private static final int ONE_SECOND = 1000;

    private long my_time = START_TIME;
    private Random rand = new Random();

    private Button[] answer_buttons = new Button[3];
    private TextView[] variable_texts = new TextView[3];
    private TextView my_operation_textview, my_score_textview, timer_textview,
            my_game_over_textview, my_game_over_score_textview;

    private Button my_menu_button, my_play_again_button;
    private RelativeLayout my_game_over_layout;
    private int a = 0, b = 0, c = 0, answer = 0;
    private CountDownTimer my_timer;

    private MathJumble my_jumble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        final String difficulty = getIntent().getStringExtra("Difficulty");
        my_jumble = new MathJumble(difficulty);

        my_game_over_layout = (RelativeLayout) findViewById(R.id.game_over_layout);

        my_game_over_textview = (TextView) findViewById(R.id.game_over_textview);
        my_game_over_score_textview = (TextView) findViewById(R.id.game_over_score_textview);
        my_menu_button = (Button) findViewById(R.id.menu_button);
        my_menu_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Intent menu = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(menu);
                finish();
            }
        });
        my_play_again_button = (Button) findViewById(R.id.play_again_button);
        my_play_again_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });

        //create textviews
        variable_texts[0] = (TextView) findViewById(R.id.a_textview);
        variable_texts[1] = (TextView) findViewById(R.id.b_textview);
        variable_texts[2] = (TextView) findViewById(R.id.c_textview);
        timer_textview = (TextView) findViewById(R.id.timer_textview);
        timer_textview.setText(String.valueOf(START_TIME));
        my_score_textview = (TextView) findViewById(R.id.score_textview);
        my_score_textview.setText(String.valueOf(my_jumble.getScore()));
        my_operation_textview = (TextView) findViewById(R.id.operation_textview);
        final TextView equals_textview = (TextView) findViewById(R.id.equals_textview);

        //create buttons
        answer_buttons[0] = (Button) findViewById(R.id.a1_button);
        answer_buttons[0].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonClick((answer_buttons[0].getText().toString()));
            }
        });
        answer_buttons[1] = (Button) findViewById(R.id.a2_button);
        answer_buttons[1].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonClick((answer_buttons[1].getText().toString()));
            }
        });
        answer_buttons[2] = (Button) findViewById(R.id.a3_button);
        answer_buttons[2].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonClick((answer_buttons[2].getText().toString()));
            }
        });

        my_timer = new CountDownTimer(START_TIME, ONE_SECOND) {
            public void onTick(long milliSeconds) {
                my_time -= 1000;
                timer_textview.setText(String.valueOf(my_time / 1000));
            }
            public void onFinish() {
                timer_textview.setText(String.valueOf(0));
                lose();
            }
        }.start();

        nextProblem();
    }

    /**
     * Create the next problem to be solved.
     */
    private void nextProblem() {
        final int[][] answers = my_jumble.nextProblem();

        final int unknown_index = my_jumble.getUnknownIndex();

        my_operation_textview.setText(my_jumble.getOperationText());
        //update the text values on the buttons and text fields
        for (int i = 0; i < answers[0].length; i++) {
            if (i != unknown_index)
                variable_texts[i].setText(String.valueOf(answers[0][i]));
            else
                variable_texts[i].setText("?");
            answer_buttons[i].setText(String.valueOf(answers[1][i]));
        }
    }

    /**
     *
     * @param the_answer
     */
    private void buttonClick(final String the_answer) {
        boolean correct = my_jumble.answer(Integer.valueOf(the_answer));

        if (correct) {
            nextProblem();
            my_time += my_jumble.getTimerIncrease();
            timer_textview.setText(String.valueOf(my_time / 1000));
            my_score_textview.setText(String.valueOf(my_jumble.getScore()));

        }
        else lose();

    }

    private void lose() {
        my_timer.cancel();
        my_jumble.lose();

        my_game_over_score_textview.setText(String.valueOf(my_jumble.getScore()));
        for (Button button : answer_buttons) {
            button.setEnabled(false);
            button.setAlpha((float) 0.5);
        }
        for (TextView tv : variable_texts) {
            tv.setAlpha((float) 0.5);
        }
        my_score_textview.setAlpha((float) 0.5);
        timer_textview.setAlpha((float) 0.5);
        my_operation_textview.setAlpha((float) 0.5);
        my_score_textview.setAlpha((float) 0.5);
        findViewById(R.id.timer_textview_label).setAlpha((float) 0.5);
        findViewById(R.id.score_textview_label).setAlpha((float) 0.5);

        my_game_over_layout.setVisibility(View.VISIBLE);
    }
}