package alexp8.controller;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.example.alexp8.mathjumble.R;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import alexp8.model.AbstractOperation;
import alexp8.model.Add;
import alexp8.model.Divide;
import alexp8.model.Multiply;
import alexp8.model.Operation;
import alexp8.model.Subtract;

/**
 * Main activity handling Google sign-in, fragment transitions, and run the game.
 */
public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        MainMenuFragment.Listener, GameplayFragment.Listener, HowToPlayFragment.Listener {

    private static final String EASY_LEADERBOARD_ID = "CgkIpYejmpQaEAIQAg",
            NORMAL_LEADERBOARD_ID ="CgkIpYejmpQaEAIQAw",
            HARD_LEADERBOARD_ID = "CgkIpYejmpQaEAIQBA";
    private static final String TAG = "MathStone";
    private static final long MAX_GAME_LENGTH = 1000 * 60 * 60 * 36; //36 hours
    private static final int RC_SIGN_IN = 9001, RC_SCOREBOARD = 5001;
    private static final int TIMER_INCREASE = 1000;

    private GameplayFragment myGameplayFragment;
    private MainMenuFragment myMainMenuFragment;
    private HowToPlayFragment myHowToPlayFragment;

    //time in milliseconds for game to be played (20 seconds)
    private static final long START_TIME = 20 * 1000;
    private static final int ONE_TENTH_SECOND = 100;

    private long my_time, tick = 0;
    private String my_difficulty = "", my_leaderboard_id;

    private boolean in_game = false, game_is_going = false,
            mySignInClicked = false, myResolvingConnectionFailure = false, myAutoStartSignInFlow = true;

    private GoogleApiClient myGoogleApiClient;
    private CountDownTimer my_timer;

    private Random rand = new Random();
    private int my_score = 0;
    private Operation my_operation;
    private AbstractOperation my_add, my_subtract, my_multiply, my_divide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        myMainMenuFragment = new MainMenuFragment();
        myGameplayFragment = new GameplayFragment();
        myHowToPlayFragment = new HowToPlayFragment();

        myMainMenuFragment.setListener(this);
        myGameplayFragment.setListener(this);
        myHowToPlayFragment.setListener(this);

        myGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        setUpTimer();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                myMainMenuFragment).commit();
    }

    /**
     * Sign in the user on start.
     */
    @Override
    protected void onStart() {

        if (myGoogleApiClient != null && !myGoogleApiClient.isConnected())
            myGoogleApiClient.connect();

        super.onStart();
    }

    /**
     * Set up the timer to tick every second.
     * Check if the user has run out of time.
     */
    private void setUpTimer() {
        my_timer = new CountDownTimer(MAX_GAME_LENGTH, ONE_TENTH_SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                tick++;

                if (tick % 10 == 0 && game_is_going) { //increment timer every second and only when unpaused
                    tick = 0;
                    my_time -= 1000;

                    if (myGameplayFragment.isAdded())
                        myGameplayFragment.updateTimer(String.valueOf(my_time / 1000));
                    if (my_time < 100) //round to tenth of a second
                        lose();

                } else if (!game_is_going) {
                    my_timer.cancel();
                }
            }

            @Override
            public void onFinish() {

            }
        };
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**If app is interrupted, pause game*/
    @Override
    public void onPause() {
        pause();
        super.onPause();
    }

    public boolean signedIn() {
        return (myGoogleApiClient != null && myGoogleApiClient.isConnected());
    }

    /**Sign out the user.*/
    public void signOut() {
        Log.d(TAG, "Sign-out button clicked");

        Games.signOut(myGoogleApiClient);
        myGoogleApiClient.disconnect();
        Toast.makeText(MainActivity.this, "Warning scores will not be saved when signed out!", Toast.LENGTH_LONG).show();

        myMainMenuFragment.signOut();
    }

    /**Sign in the user.*/
    public void signIn() {
        Log.d(TAG, "Sign-in button clicked");
        mySignInClicked = true;
        myGoogleApiClient.connect();
    }

    /**
     * Display the leaderboards to the user.
     */
    public void displayLeaderboards() {
        Log.d(TAG, "display leaderboards api is connected: " + myGoogleApiClient.isConnected());

        if (!signedIn()) {
            Toast.makeText(this, R.string.leaderboards_not_available, Toast.LENGTH_SHORT).show();
        } else if(myGoogleApiClient.isConnected()) {
            final Intent leaderboard_intent = Games.Leaderboards.getAllLeaderboardsIntent(myGoogleApiClient);
            startActivityForResult(leaderboard_intent, RC_SCOREBOARD);
        } else {
            Toast.makeText(this, "Unable to display leaderboards", Toast.LENGTH_SHORT).show();
        }
    }

    /**Set the leaderboard ID when difficulty is changed.*/
    private void setLeaderboardID(final String the_difficulty) {
        switch (the_difficulty) {
            case "Normal":
                my_leaderboard_id = NORMAL_LEADERBOARD_ID;
                break;
            case "Hard":
                my_leaderboard_id = HARD_LEADERBOARD_ID;
                break;
            case "Easy":
                my_leaderboard_id = EASY_LEADERBOARD_ID;
                break;
            default:
                Log.e(TAG, "string difficulty error");
                break;
        }
    }

    /**
     * Play the game at the set difficulty.
     * @param the_difficulty of game
     */
    public void playGame(final String the_difficulty) {
        my_add = new Add(the_difficulty);
        my_divide = new Divide(the_difficulty);
        my_multiply = new Multiply(the_difficulty);
        my_subtract = new Subtract(the_difficulty);

        my_time = START_TIME; //reset time
        game_is_going = true;
        startTimer();

        if (!my_difficulty.equals(the_difficulty)) {
            my_difficulty = the_difficulty;
            setLeaderboardID(the_difficulty);
        }

        if (!myGameplayFragment.isAdded()) switchToFragment(myGameplayFragment);
    }

    /**
     * Create the next problem to be solved and display it on the screen.
     */
    public void nextProblem() {
        Log.d(TAG, "next problem");
        pickOperation();
        final int[] variables = my_operation.getVariables();
        final Iterator iterator = my_operation.getAnswers().iterator();
        final String[] answers = new String[3];

        answers[0] = String.valueOf(iterator.next());
        answers[1] = String.valueOf(iterator.next());
        answers[2]= String.valueOf(iterator.next());

        myGameplayFragment.updateTextViews(variables, my_operation.toString(), my_operation.getUnknownIndex(),
                String.valueOf(my_score), String.valueOf(my_time / 1000));
        myGameplayFragment.updateButtons(answers);
    }

    /**Randomly decide which problem to be solved.*/
    private void pickOperation() {

        final int rand_operation = rand.nextInt(4);
        switch (rand_operation) {
            case 0:
                my_operation = my_add;
                break;
            case 1:
                my_operation = my_divide;
                break;
            case 2:
                my_operation = my_subtract;
                break;
            default:
                my_operation = my_multiply;
                break;
        }
        my_operation.operate();
    }

    /**
     * Check if the user correctly solved the problem.
     * @param value of the answer button
     */
    @Override
    public void answerButtonClick(final String value) {
        if (value == null || value.equals("")) return;

        final int answer = Integer.valueOf(value);

        if (my_operation.getAnswer() == answer) {
            my_score += my_operation.getScoreBonus(); //increment score
            my_operation.increaseRange(); //increase game difficulty creating bigger numbers
            nextProblem();
            my_time += TIMER_INCREASE;
        } else {
            lose();
        }
    }

    /**
     * End the game, display game over screen, submit their score.
     */
    private void lose() {
        tick = 0;
        game_is_going = false;

        submitScore();

        final String score = String.valueOf(my_score);
        myGameplayFragment.gameOver(score, true);
    }

    private void submitScore() {
        //submit the score otherwise store locally
        if (signedIn()) {
            Games.Leaderboards.submitScore(myGoogleApiClient, my_leaderboard_id,  my_score);
        } else {
            Log.d(TAG, "unable to submit score");
        }
    }

    /**
     * On game over screen, user hits play again to start a new game same difficulty.
     */
    @Override
    public void playAgain() {
        playGame(my_difficulty);
        nextProblem();
    }

    /**
     * User during game hits pause screen, hits menu.
     */
    public void quit() {
        game_is_going = false;
        menu();
    }

    /**
     * Switch to main menu.
     */
    @Override
    public void menu() {
        switchToFragment(myMainMenuFragment);
    }

    @Override
    public boolean inGame() {
        return in_game;
    }

    @Override
    public void setInGame(boolean in_game) {
        this.in_game = in_game;
    }

    /** Pause the game.*/
    public void pause() {
        if (in_game) {
            myGameplayFragment.pause();
            game_is_going = false;
        }
    }

    /** Resume the game.*/
    public void resume() {
        if (in_game) {
            game_is_going = true;
            startTimer();
        }
    }

    private void startTimer() {
        my_timer.start();
    }


    @Override
    public void howToPlay() {
        switchToFragment(myHowToPlayFragment);
    }

    /**
     * Switch from gameplay to menu or vice versa.
     * @param newFrag to switch to
     */
    private void switchToFragment(final Fragment newFrag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newFrag)
                .commit();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected() called. Sign in successful!");

        if (myMainMenuFragment.isAdded()) myMainMenuFragment.displaySignedIn(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        myGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (myResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mySignInClicked || myAutoStartSignInFlow) {
            myAutoStartSignInFlow = false;
            mySignInClicked = false;
            myResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this, myGoogleApiClient,
                    connectionResult, RC_SIGN_IN, getString(R.string.signin_other_error));
        }
    }

    /**
     * Handle the user attempting to sign in.
     * @param requestCode of activity
     * @param resultCode of activity
     * @param intent of activity
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == RC_SIGN_IN) { //attempting to sign in
            Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, resultCode="
                    + resultCode + ", intent=" + intent);

            mySignInClicked = false;
            myResolvingConnectionFailure = false;

            if (resultCode == RESULT_OK) {
                myGoogleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.signin_other_error);
            }
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void closeHowToPlay() {
        switchToFragment(myMainMenuFragment);
    }
}