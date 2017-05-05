package alexp8.controller;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import alexp8.model.MathJumble;

/**
 * Main activity handling Google sign-in, fragment transitions, and run the game.
 */
public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        MainMenuFragment.Listener, GameplayFragment.Listener {

    private static final String EASY_LEADERBOARD_ID = "CgkIpYejmpQaEAIQAg",
            NORMAL_LEADERBOARD_ID ="CgkIpYejmpQaEAIQAw",
            HARD_LEADERBOARD_ID = "CgkIpYejmpQaEAIQBA";

    private static final long MAX_GAME_LENGTH = 1000 * 60 * 60 * 36; //36 hours
    private static final String MY_PREFS_NAME = "Math Jumble Preferences";
    private static final String TAG = "MathJumble";
    private static final int ACCESS_DENIED = 888, ACCESS_GRANTED = 889;
    private static final int MY_PERMISSION_ACCESS_ACC_NAME = ACCESS_DENIED;
    private GoogleApiClient myGoogleApiClient;
    private static int RC_SIGN_IN = 9001;
    private static int RC_SCOREBOARD = 5001;

    private GameplayFragment myGameplayFragment;
    private MainMenuFragment myMainMenuFragment;

    //time in milliseconds for game to be played (20 seconds)
    private static final long START_TIME = 20 * 1000;
    private static final int ONE_TENTH_SECOND = 100;

    private long my_time;
    private String my_difficulty = "", my_leaderboard_id, my_name, my_img_url;

    /**
     *  in_game = true for when a user is on game screen,
     *  in_game can be true while game_is_going false.
     *  game_is_going = false if user lost or hit pause
     */
    private boolean in_game = false, game_is_going = false;

    private MathJumble my_jumble;
    private CountDownTimer my_timer;
    private int tick = 0;
    private boolean mySignInClicked = false, myResolvingConnectionFailure = false, myAutoStartSignInFlow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        myMainMenuFragment = new MainMenuFragment();
        myGameplayFragment = new GameplayFragment();

        myMainMenuFragment.setListener(this);
        myGameplayFragment.setListener(this);

        myGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.GET_ACCOUNTS)) {
                Log.d(TAG, "dont show request?");
            } else {

                Log.d(TAG, "permission: " + MY_PERMISSION_ACCESS_ACC_NAME);

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.GET_ACCOUNTS}, MY_PERMISSION_ACCESS_ACC_NAME);

                Log.d(TAG, "permission: " + MY_PERMISSION_ACCESS_ACC_NAME);
            }
        } else {
            Log.d(TAG, " permission granted");
        }

        setUpTimer();
    }

    /**
     * Sign in the user on start.
     */
    @Override
    protected void onStart() {

        if (myGoogleApiClient != null && !myGoogleApiClient.isConnected())
            myGoogleApiClient.connect();

        if (!myMainMenuFragment.isAdded() && !in_game) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                    myMainMenuFragment).commit();
        }

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
        my_name = "";
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
        my_time = START_TIME; //reset time
        game_is_going = true;
        startTimer();

        if (!my_difficulty.equals(the_difficulty)) {
            my_difficulty = the_difficulty;
            setLeaderboardID(the_difficulty);
        }

        my_jumble = new MathJumble(my_difficulty);

        if (!myGameplayFragment.isAdded()) switchToFragment(myGameplayFragment);
    }

    /**
     * Create the next problem to be solved and display it on the screen.
     */
    public void nextProblem() {
        Log.d(TAG, "next problem");
        my_jumble.nextProblem();
        final int[] variables = my_jumble.getVariables();
        final Iterator iterator = my_jumble.getAnswers().iterator();
        final String[] answers = new String[3];

        answers[0] = String.valueOf(iterator.next());
        answers[1] = String.valueOf(iterator.next());
        answers[2]= String.valueOf(iterator.next());

        myGameplayFragment.updateTextViews(variables, my_jumble.getOperationText(), my_jumble.getUnknownIndex(),
                String.valueOf(my_jumble.getScore()), String.valueOf(my_time / 1000));
        myGameplayFragment.updateButtons(answers);
    }

    /**
     * Check if the user correctly solved the problem.
     * @param value of the answer button
     */
    @Override
    public void answerButtonClick(final String value) {
        if (value == null || value.equals("")) return;

        final int answer = Integer.valueOf(value);
        final boolean correct = my_jumble.answer(answer);

        if (correct) {
            nextProblem();
            my_time += my_jumble.getTimerIncrease();
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

        final String score = String.valueOf(my_jumble.getScore());
        myGameplayFragment.gameOver(score, true);
    }

    private void submitScore() {
        //submit the score otherwise store locally
        if (signedIn()) {
            Games.Leaderboards.submitScore(myGoogleApiClient, my_leaderboard_id,  my_jumble.getScore());
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

    @Override
    public String getName() {
        return my_name;
    }

    /*** Pause the game.*/
    public void pause() {
        if (in_game) {
            myGameplayFragment.pause();
            game_is_going = false;
        }
    }

    /*** Resume the game.*/
    public void resume() {
        if (in_game) {
            game_is_going = true;
            startTimer();
        }
    }

    private void startTimer() {
        my_timer.start();
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

        final int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "On connected name accessed");
            my_name = Games.getCurrentAccountName(myGoogleApiClient);
        } else {
            Log.d(TAG, "On connected name permission denied");
            my_name = "";
        }

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
        Log.e(TAG, "onConnectionFailed() called, result: " + connectionResult);

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
}