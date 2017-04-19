package alexp8.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alexp8.mathjumble.R;
import com.google.android.gms.games.Games;
import com.google.android.gms.vision.text.Text;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import alexp8.model.MathJumble;

/**
 *
 *
 */
public class GameplayFragment extends Fragment {

    private Listener my_listener = null;
    private GamePlayListener myGamePlayListener;
    private Activity myActivity;

    private int[] buttons;
    private int[] textviews;

    public interface Listener {
        void answerButtonClick(String value);
        void playAgain();
        void menu();
        void nextProblem();
        boolean inGame();
        void setInGame(boolean b);
        void pause();
        void resume();
    }

    public void setListener(Listener listener) {
        my_listener = listener;
    }

    public GameplayFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_gameplay, container, false);
        myGamePlayListener = new GamePlayListener();

        buttons = new int[]{
                R.id.a1_button, R.id.a2_button, R.id.a3_button, R.id.pause_button,
                R.id.menu_button, R.id.play_again_button,
                R.id.resume_button
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

        //begin the game
        if (!my_listener.inGame()) {
            my_listener.nextProblem();
            my_listener.setInGame(true);
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity)
            myActivity = (Activity) context;
    }

    public void gameOver(boolean game_over) {
        final RelativeLayout gameOverLayout = (RelativeLayout) myActivity.findViewById(R.id.game_over_layout);
        if (game_over) {
            flipActiveViews(0.5, false);
            gameOverLayout.setVisibility(View.VISIBLE);
        } else {
            flipActiveViews(1, true);
            gameOverLayout.setVisibility(View.GONE);
        }
    }

    public void gameOver(final String score, boolean game_over) {

        final TextView game_over_score = (TextView) myActivity.findViewById(R.id.game_over_score_textview);
        final String score_text = "Score " + score;
        game_over_score.setText(score_text);

        gameOver(game_over);
    }

    /**
     * Update the variables.
     *
     * @param variables
     * @param operation
     */
    public void updateTextViews(final int[] variables, final String operation, final int unknown_i) {

        if (myActivity == null) {
            Log.e("onupdate", "activity is null");
            return;
        }

        TextView op_textview = (TextView) myActivity.findViewById(R.id.operation_textview);
        op_textview.setText(operation);

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
     * @param set of int values for the buttons to show
     */
    public void updateButtons(final Set<Integer> set) {
        final Iterator iterator = set.iterator();

        //only set text for the answer buttons
        for (int i = 0; i < 3; i++) {
            final Button button = (Button) myActivity.findViewById(buttons[i]);
            button.setText(String.valueOf(iterator.next()));
        }
    }


    public void updateTimer(String the_time) {
        final TextView timer_textview = (TextView) myActivity.findViewById(R.id.timer_textview);
        timer_textview.setText(the_time);
    }

    /**
     *
     * @param the_score
     */
    public void updateScore(final String the_score) {
        final TextView score_tv = (TextView) myActivity.findViewById(R.id.score_textview);
        score_tv.setText(the_score);
    }

    /**
     * Swap views depending if game is ending or beginning a new game.
     * @param value value to set opacity of background (faded or full)
     * @param game_active set buttons enabled if  game is playing
     */
    public void flipActiveViews(double value, boolean game_active) {

        for (int i = 0; i < 4; i++) { //enable/disable pause and answer buttons
            Button button = (Button) myActivity.findViewById(buttons[i]);
            button.setEnabled(game_active);
            button.setAlpha((float) value);
        }

        for (int tv : textviews) {
            TextView textview = (TextView) myActivity.findViewById(tv);
            textview.setAlpha((float) value);
        }

        myActivity.findViewById(R.id.pause_button).setAlpha((float) value);
        myActivity.findViewById(R.id.operation_textview).setAlpha((float) value);
        myActivity.findViewById(R.id.score_textview).setAlpha((float) value);
        myActivity.findViewById(R.id.score_textview_label).setAlpha((float) value);
        myActivity.findViewById(R.id.timer_textview).setAlpha((float) value);
        myActivity.findViewById(R.id.timer_textview_label).setAlpha((float) value);
    }

    private class GamePlayListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.menu_button:
                    my_listener.menu();
                    break;
                case R.id.play_again_button:
                    gameOver(false);
                    my_listener.playAgain();
                    break;
                case R.id.a1_button:
                    my_listener.answerButtonClick(((TextView)myActivity.findViewById(R.id.a1_button)).getText().toString());
                    break;
                case R.id.a2_button:
                    my_listener.answerButtonClick(((TextView)myActivity.findViewById(R.id.a2_button)).getText().toString());
                    break;
                case R.id.a3_button:
                    my_listener.answerButtonClick(((TextView)myActivity.findViewById(R.id.a3_button)).getText().toString());
                    break;
                case R.id.pause_button:
                    my_listener.pause();
                    flipActiveViews(0.5, false);
                    myActivity.findViewById(R.id.pause_screen_layout).setVisibility(View.VISIBLE);
                    break;
                case R.id.resume_button:
                    my_listener.resume();
                    flipActiveViews(1, true);
                    myActivity.findViewById(R.id.pause_screen_layout).setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    }
}