package alexp8.controller;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.games.Games;
import com.example.alexp8.mathjumble.R;

import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import alexp8.model.MathJumble;

/**
 * Main activity handling Google sign-in, fragment transitions, and run the game.
 */
public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, MainMenuFragment.Listener, GameplayFragment.Listener {

    private static final String EASY_LEADERBOARD_ID = "CgkIpYejmpQaEAIQAg",
            NORMAL_LEADERBOARD_ID ="CgkIpYejmpQaEAIQAw",
            HARD_LEADERBOARD_ID = "CgkIpYejmpQaEAIQBA";

    private static final long THIRTY_SIX_HOURS = 1000 * 60 * 60 * 36;
    private GoogleApiClient myGoogleApiClient;
    private static int RC_SIGN_IN = 9001;
    private static int RC_SCOREBOARD = 5001;

    private GameplayFragment myGameplayFragment;
    private MainMenuFragment myMainMenuFragment;

    //time in milliseconds for game to be played (15 seconds)
    private static final long START_TIME = 15 * 1000;
    private static final int ONE_SECOND = 1000, ONE_TENTH_SECOND = 100;

    private long my_time, my_resume_time, my_pause_time;
    private String my_difficulty = "", my_leaderboard_id, my_name, my_img_url;
    /**in_game for when a user has a game going, paused if the user hit pause*/
    private boolean in_game = false, paused = false;

    private MathJumble my_jumble;
    private CountDownTimer my_timer;
    private int tick = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        myMainMenuFragment = new MainMenuFragment();
        myGameplayFragment = new GameplayFragment();

        myMainMenuFragment.setListener(this);
        myGameplayFragment.setListener(this);

        final GoogleSignInOptions myGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        myGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, myGoogleSignInOptions)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        signIn();
        setUpTimer();
    }

    /**
     * Set up the timer to tick every second.
     * Check if the user has run out of time.
     */
    private void setUpTimer() {
        my_timer = new CountDownTimer(THIRTY_SIX_HOURS, ONE_TENTH_SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {

                if (tick % 10 == 0) {

                    if (in_game) {
                        my_time -= 1000;
                        if (myGameplayFragment.isAdded())
                            myGameplayFragment.updateTimer(String.valueOf(my_time / 1000));
                        if (my_time < 100) //round to tenth of a second
                            lose();
                    }

                    Log.d("MathJumble", "ticking " + my_time);
                }

                tick++;
            }

            @Override
            public void onFinish() {

            }
        };
    }

    /**
     * Sign in the user on start.
     */
    @Override
    protected void onStart() {
        super.onStart();
        myGoogleApiClient.connect();

        if (!myMainMenuFragment.isAdded() && !in_game) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                    myMainMenuFragment).commit();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * If app is interrupted, pause game
     */
    @Override
    public void onPause() {
        pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        if (!paused) resume();
        super.onResume();
    }

    public boolean signedIn() {
        return (myGoogleApiClient != null && myGoogleApiClient.isConnected());
    }

    public String getName() {return my_name;}
    public String getImgUrl() {return my_img_url;}

    /**
     * Sign in the User.
     */
    public void signIn() {
        final Intent intent = Auth.GoogleSignInApi.getSignInIntent(myGoogleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
    }

    public void signOut() {

        Auth.GoogleSignInApi.signOut(myGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                myMainMenuFragment.signOut();
                myGoogleApiClient.disconnect();
                Toast.makeText(MainActivity.this, "Warning scores will not be saved when signed out!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("MathJumble", "onConnectionFailed() called, result: " + connectionResult);
    }

    /**
     * Display the leaderboards to the user.
     */
    public void displayLeaderboards() {
        startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(myGoogleApiClient),
                RC_SCOREBOARD);
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
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);

            if (result.isSuccess()) {
                Log.d("MathJumble", "Mainactivity signIn(): successfully signed in user");
                myGoogleApiClient.connect();

                my_name = result.getSignInAccount().getDisplayName();
                my_img_url = result.getSignInAccount().getPhotoUrl().toString();

                myMainMenuFragment.displaySignedIn(true);
            } else {
                Log.d("MathJumble", "Mainactivity signIn(): unable to sign in user");
                if (myGoogleApiClient.isConnected())
                    myGoogleApiClient.disconnect();
                Toast.makeText(this, R.string.signin_failure, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == RC_SCOREBOARD) {
            if (!signedIn())
                Toast.makeText(this, R.string.leaderboards_not_available, Toast.LENGTH_SHORT).show();

        }
    }

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
                break;
        }
    }

    /**
     * Play the game at the set difficulty.
     * @param the_difficulty of game
     */
    public void playGame(final String the_difficulty) {
        my_time = START_TIME; //reset time
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
        Log.d("MathJumble", "next problem");
        my_jumble.nextProblem();
        final int[] variables = my_jumble.getVariables();
        final Iterator iterator = my_jumble.getAnswers().iterator();
        final String[] answers = new String[3];

        answers[0] = String.valueOf(iterator.next());
        answers[1] = String.valueOf(iterator.next());
        answers[2]= String.valueOf(iterator.next());

        myGameplayFragment.updateTextViews(variables, my_jumble.getOperationText(), my_jumble.getUnknownIndex(), my_time);
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
            myGameplayFragment.updateScore(String.valueOf(my_jumble.getScore()));
            myGameplayFragment.updateTimer(String.valueOf(my_time / 1000));
        } else {
            lose();
        }
    }


    /**
     * End the game, display game over screen, submit their score.
     */
    private void lose() {
        pauseTimer();

        submitScore();

        final String score = String.valueOf(my_jumble.getScore());
        myGameplayFragment.gameOver(score, true);
    }

    private void submitScore() {
        //submit the score otherwise store locally
        if (signedIn()) {
            Games.Leaderboards.submitScore(myGoogleApiClient, my_leaderboard_id,  my_jumble.getScore());
        } else {
            Log.d("MathJumble", "unable to submit score");
        }
    }

    /**
     * On game over screen, user hits play again to start a new game same difficulty.
     */
    @Override
    public void playAgain() {
        playGame(my_difficulty);
        my_time += 1000;
        nextProblem();
    }

    private void startTimer() {
        my_timer.start();
    }

    private void pauseTimer() {
        my_timer.cancel();
    }

    /**
     * User during game hits pause screen, hits menu.
     */
    public void quit() {
        pauseTimer();
        menu();
    }

    /**
     * Switch to main menu.
     */
    @Override
    public void menu() {
        switchToFragment(myMainMenuFragment);
    }

    public void setPaused(boolean paused) {this.paused = paused;}

    @Override
    public boolean inGame() {
        return in_game;
    }

    @Override
    public void setInGame(boolean in_game) {
        this.in_game = in_game;
    }

    /*** Pause the game.*/
    public void pause() {
        if (in_game) {
            pauseTimer();
            long cur_time = System.currentTimeMillis();
            Log.d("MathJumble", "paused at " + cur_time);
            my_pause_time = cur_time % 1000;
        }
    }

    /*** Resume the game.*/
    public void resume() {
        if (in_game) {
            long cur_time = System.currentTimeMillis();
            Log.d("MathJumble", "resumed at " + cur_time);
            my_resume_time = cur_time % 1000;

            long delay_time = my_resume_time - my_pause_time;
            Log.d("MathJumble", "delay time: " + delay_time);
            my_time += (long) Math.abs(delay_time);

            startTimer();
        }
    }

    /**
     * Switch from gameplay to menu or vice versa.
     * @param newFrag to switch to
     */
    private void switchToFragment(final Fragment newFrag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newFrag)
                .commit();
    }
}