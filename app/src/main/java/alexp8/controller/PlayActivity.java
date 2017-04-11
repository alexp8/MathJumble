package alexp8.controller;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.alexp8.mathjumble.R;
import alexp8.model.MathJumble;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * Play the game.
 */
public class PlayActivity extends AppCompatActivity {
    //time in milliseconds for game to be played
    private static final long START_TIME = 30 * 1000;
    private static final int ONE_SECOND = 1000;

    private long my_time;
    private Random rand = new Random();

    private Button[] answer_buttons = new Button[3];
    private TextView[] variable_texts = new TextView[3];
    private TextView my_operation_textview, my_score_textview, timer_textview,
            my_game_over_textview, my_game_over_score_textview;

    private Button my_menu_button, my_play_again_button;
    private RelativeLayout my_game_over_layout;
    private int a = 0, b = 0, c = 0, answer = 0;
    private CountDownTimer my_timer;
    private String my_difficulty;

    private MathJumble my_jumble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        my_difficulty = getIntent().getStringExtra("Difficulty");
        my_jumble = new MathJumble(my_difficulty);

        //set up buttons
        setupButtons();

        //start game timer
        startTimer();

        //begin the game
        nextProblem();
    }

    private void startTimer() {
        my_time = START_TIME;
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
    }

    /**
     * Create texts and button on screen.
     */
    private void setupButtons() {
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
                my_jumble = new MathJumble(my_difficulty);
                my_game_over_layout.setVisibility(View.INVISIBLE);
                flipActiveViews(1, true); //activate buttons and views
                my_game_over_score_textview.setText(String.valueOf(my_jumble.getScore()));
                startTimer(); //reset and start timer
                nextProblem(); //display next problem
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
    }

    /**
     * Create the next problem to be solved and display it on the screen.
     */
    private void nextProblem() {
        my_jumble.nextProblem();
        final int[] variables = my_jumble.getVariables();
        final Set<Integer> answers = my_jumble.getAnswers();

        final int unknown_index = my_jumble.getUnknownIndex();

        my_operation_textview.setText(my_jumble.getOperationText());

        final Iterator iterator = answers.iterator();
        //update the text values on the buttons and text fields
        for (int i = 0; i < 3; i++) {
            if (i != unknown_index)
                variable_texts[i].setText(String.valueOf(variables[i]));
            else
                variable_texts[i].setText(String.valueOf("?"));
            answer_buttons[i].setText(String.valueOf(iterator.next().toString()));
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

    /**
     * Swap views depending if game is ending or beginning a new game.
     * @param value
     * @param active
     */
    private void flipActiveViews(double value, boolean active) {
        for (Button button : answer_buttons) {
            button.setEnabled(active);
            button.setAlpha((float) value);
        }
        for (TextView tv : variable_texts) {
            tv.setAlpha((float) value);
        }
        my_score_textview.setAlpha((float) value);
        timer_textview.setAlpha((float) value);
        my_operation_textview.setAlpha((float) value);
        my_score_textview.setAlpha((float) value);
        findViewById(R.id.timer_textview_label).setAlpha((float) value);
        findViewById(R.id.score_textview_label).setAlpha((float) value);
    }

    private void lose() {
        my_timer.cancel();
        my_jumble.lose();

        my_game_over_score_textview.setText(String.valueOf(my_jumble.getScore()));

        flipActiveViews(0.5, false);

        my_game_over_layout.setVisibility(View.VISIBLE);
    }
}