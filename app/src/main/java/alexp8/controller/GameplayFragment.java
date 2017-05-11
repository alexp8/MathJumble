package alexp8.controller;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alexp8.mathstone.R;

/**
 * Fragment handling the game UI.
 */
public class GameplayFragment extends Fragment {

    private static final float TRANSPARENT = (float) 0.5;
    private static final float FULL = 1;
    private Listener my_listener = null;
    private Activity myActivity;

    private int[] buttons;
    private int[] textviews;
    private Button my_last_button;

    interface Listener {
        void answerButtonClick(String value);
        void playAgain();
        void menu();
        void nextProblem();
        void pause();
        void resume();
        void quit();
        boolean inGame();
        void setInGame(boolean b);
    }

    public void setListener(Listener listener) {
        my_listener = listener;
    }

    public GameplayFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_gameplay, container, false);
        final GamePlayListener myGamePlayListener = new GamePlayListener();

        buttons = new int[]{
                R.id.a1_button, R.id.a2_button, R.id.a3_button, R.id.pause_button,
                R.id.menu_button, R.id.play_again_button,
                R.id.resume_button, R.id.menu_button_at_pause
        };

        textviews = new int[]{
                R.id.a_textview, R.id.b_textview,
                R.id.c_textview
        };

        for (int i : buttons)
            v.findViewById(i).setOnClickListener(myGamePlayListener);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!my_listener.inGame()) { //start the game if one isn't currently going
            my_listener.setInGame(true);
            my_listener.nextProblem();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity)
            myActivity = (Activity) context;
    }

    /**
     * Helper method that displays game over screen on game over.
     * @param game_over true or false if game is over
     */
    public void gameOver(final boolean game_over) {
        final RelativeLayout gameOverLayout = (RelativeLayout) myActivity.findViewById(R.id.game_over_layout);
        if (game_over) {
            flipActiveViews(TRANSPARENT, false);
            gameOverLayout.setVisibility(View.VISIBLE);
        } else {
            flipActiveViews(FULL, true);
            gameOverLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Display the information when the game ends: score and prompts.
     * @param score of user
     * @param game_over true or false for sub-method
     */
    public void gameOver(final String score, final boolean game_over) {

        final TextView game_over_score = (TextView) myActivity.findViewById(R.id.game_over_score_textview);
        final String score_text = "Score " + score;
        game_over_score.setText(score_text);

        my_last_button.setBackgroundResource(R.drawable.red_bg);
        my_last_button.setTextColor(Color.WHITE);

        gameOver(game_over);
    }

    /**
     * Update the variables.
     */
    public void updateTextViews(final int[] variables, final String operation, final int unknown_i,
                                final String the_score, final String the_time) {

        if (myActivity == null) {
            Log.e("onupdate", "activity is null");
            return;
        }

        //update operation, timer, and score
        ((TextView) myActivity.findViewById(R.id.operation_textview)).setText(operation);
        ((TextView) myActivity.findViewById(R.id.score_textview)).setText(the_score);
        ((TextView) myActivity.findViewById(R.id.timer_textview)).setText(the_time);

        for (int i = 0; i < textviews.length; i++) {
            final TextView tv =  ((TextView) myActivity.findViewById(textviews[i]));
            String text = String.valueOf(variables[i]);

            if (i == unknown_i) {//don't show the answer
                text = "?";
            }

            tv.setText(text);
        }
    }

    /**
     * Update the text values on the buttons
     */
    public void updateButtons(final String[] answers) {

        //only set text for the answer buttons
        for (int i = 0; i < 3; i++) {
            final Button button = (Button) myActivity.findViewById(buttons[i]);
            button.setText(answers[i]);
        }
    }

    public void updateTimer(final String the_time) {
        final TextView timer_textview = (TextView) myActivity.findViewById(R.id.timer_textview);
        timer_textview.setText(the_time);
    }

    /**
     * Swap views depending if game is ending or beginning a new game.
     * @param value value to set opacity of background (faded or full)
     * @param game_active true if game is active
     */
    public void flipActiveViews(float value, boolean game_active) {

        //enable/disable pause and answer buttons
        for (int i = 0; i < 4; i++) {
            final Button button = (Button) myActivity.findViewById(buttons[i]);
            button.setEnabled(game_active);
            button.setAlpha(value);
        }

        //fade textviews if paused or game over
        for (int tv : textviews) {
            TextView textview = (TextView) myActivity.findViewById(tv);
            textview.setAlpha(value);
        }

        myActivity.findViewById(R.id.pause_button).setAlpha(value);
        myActivity.findViewById(R.id.operation_textview).setAlpha(value);
        myActivity.findViewById(R.id.score_textview).setAlpha(value);
        myActivity.findViewById(R.id.score_textview_label).setAlpha(value);
        myActivity.findViewById(R.id.timer_textview).setAlpha(value);
        myActivity.findViewById(R.id.timer_textview_label).setAlpha(value);
    }

    /**MainActivity calls this if user paused via pausing app or if user hits pause in game.*/
    public void pause() {
        flipActiveViews(TRANSPARENT, false);

        myActivity.findViewById(R.id.pause_button).setVisibility(View.GONE);
        myActivity.findViewById(R.id.pause_screen_layout).setVisibility(View.VISIBLE);
    }

    private void resume() {
        flipActiveViews(FULL, true);

        myActivity.findViewById(R.id.pause_button).setVisibility(View.VISIBLE);
        myActivity.findViewById(R.id.pause_button).setEnabled(true);
        myActivity.findViewById(R.id.pause_screen_layout).setVisibility(View.GONE);
    }

    /**
     * Private listener class handling button clicks.
     */
    private class GamePlayListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.menu_button:
                    my_listener.menu();
                    break;
                case R.id.menu_button_at_pause:
                    my_listener.quit();
                    my_listener.menu();
                    break;
                case R.id.play_again_button:
                    my_last_button.setBackgroundResource(R.drawable.light_orange_bg);
                    my_last_button.setTextColor(Color.BLACK);
                    ((TextView)myActivity.findViewById(R.id.score_textview)).setText(String.valueOf(0));
                    my_listener.setInGame(true);
                    gameOver(false);
                    my_listener.playAgain();
                    break;
                case R.id.a1_button:
                    my_last_button = ((Button) myActivity.findViewById(R.id.a1_button));
                    my_listener.answerButtonClick(my_last_button.getText().toString());
                    break;
                case R.id.a2_button:
                    my_last_button = ((Button) myActivity.findViewById(R.id.a2_button));
                    my_listener.answerButtonClick(my_last_button.getText().toString());
                    break;
                case R.id.a3_button:
                    my_last_button = ((Button) myActivity.findViewById(R.id.a3_button));
                    my_listener.answerButtonClick(my_last_button.getText().toString());
                    break;
                case R.id.pause_button:
                    my_listener.pause();
                    pause();
                    break;
                case R.id.resume_button:
                    my_listener.resume();
                    resume();
                    break;
                default:
                    break;
            }
        }
    }
}