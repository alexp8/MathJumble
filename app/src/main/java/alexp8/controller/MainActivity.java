package alexp8.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.games.Games;
import com.example.alexp8.mathjumble.R;

import java.util.Iterator;

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
    private static final String MY_PREFS_NAME = "Math Jumble Preferences";
    private GoogleApiClient myGoogleSignInApiClient, myGoogleGameApiClient;
    private static int RC_SIGN_IN = 9001;
    private static int RC_SCOREBOARD = 5001;

    private GameplayFragment myGameplayFragment;
    private MainMenuFragment myMainMenuFragment;

    //time in milliseconds for game to be played (15 seconds)
    private static final long START_TIME = 15 * 1000;
    private static final int ONE_TENTH_SECOND = 100;

    private long my_time;
    private String my_difficulty = "", my_leaderboard_id, my_name, my_img_url;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    /**
     *  in_game = true for when a user is on game screen,
     *  in_game can be true while game_is_going false.
     *  game_is_going = false if user lost or hit pause
     */
    private boolean in_game = false, game_is_going = false;

    private MathJumble my_jumble;
    private CountDownTimer my_timer;
    private int tick = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("signed in", "false");
        editor.apply();

        myMainMenuFragment = new MainMenuFragment();
        myGameplayFragment = new GameplayFragment();

        myMainMenuFragment.setListener(this);
        myGameplayFragment.setListener(this);

        final GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        myGoogleSignInApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();


        myGoogleGameApiClient = new GoogleApiClient.Builder(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();


        silentSignIn();
        setUpTimer();
    }

    /**
     * Sign in the user on start.
     */
    @Override
    protected void onStart() {
        super.onStart();

        if (!myMainMenuFragment.isAdded() && !in_game) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                    myMainMenuFragment).commit();
        }
    }

    /**Silently sign in user.*/
    private void silentSignIn() {
        OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(myGoogleSignInApiClient);

        if (pendingResult.isDone()) {
            GoogleSignInResult result = pendingResult.get();
            handleSignInResult(result);
        } else {
            pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult result) {
                    handleSignInResult(result);
                }
            });
        }
    }

    /**
     * Grab the user's info if they signed in successfully.
     * Tell the user sign in failure.
     */
    private boolean handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            Log.d("MathJumble", "Mainactivity signIn(): successfully signed in user");
            editor.putString("signed in", "true");
            editor.apply();

            final GoogleSignInAccount acct = result.getSignInAccount();

            my_name = acct.getDisplayName();
            my_img_url = acct.getPhotoUrl().toString();
            myGoogleGameApiClient.connect();
            
            if (myMainMenuFragment.isAdded()) myMainMenuFragment.displaySignedIn(true);
            return true;
        } else {
           return false;
        }
    }

    /**
     * Set up the timer to tick every second.
     * Check if the user has run out of time.
     */
    private void setUpTimer() {
        my_timer = new CountDownTimer(THIRTY_SIX_HOURS, ONE_TENTH_SECOND) {
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
        if (!game_is_going) resume();
        super.onResume();
    }

    public boolean signedIn() {
        return (prefs.getString("signed in", "").equals("true"));
    }

    public String getName() {return my_name;}
    public String getImgUrl() {return my_img_url;}

    /**Sign out the user.*/
    public void signOut() {

        Auth.GoogleSignInApi.signOut(myGoogleSignInApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                myMainMenuFragment.signOut();
                Toast.makeText(MainActivity.this, "Warning scores will not be saved when signed out!", Toast.LENGTH_LONG).show();
                editor.putString("signed in", "false");
                editor.apply();
                myGoogleGameApiClient.disconnect();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("MathJumble", "onConnectionFailed() called, result: " + connectionResult);
    }

    /**Sign in the user.*/
    public void signIn() {
        final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(myGoogleSignInApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Display the leaderboards to the user.
     */
    public void displayLeaderboards() {
        Log.d("MathJumble", prefs.getString("signed in", ""));
        if (signedIn()) {
            final Intent leaderboard_intent = Games.Leaderboards.getAllLeaderboardsIntent(myGoogleGameApiClient);
            startActivityForResult(leaderboard_intent, RC_SCOREBOARD);
        } else {
            Toast.makeText(this, R.string.leaderboards_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle the user attempting to sign in.
     * @param requestCode of activity
     * @param resultCode of activity
     * @param intent of activity
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == RC_SIGN_IN) { //attempting to sign in
            final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
            boolean success = handleSignInResult(result);
            if (!success) {
                Log.d("MathJumble", "Mainactivity signIn(): unable to sign in user");

                Toast.makeText(this, R.string.signin_failure, Toast.LENGTH_SHORT).show();
            }
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
        Log.d("MathJumble", "next problem");
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
            Games.Leaderboards.submitScore(myGoogleGameApiClient, my_leaderboard_id,  my_jumble.getScore());
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

    /*** Pause the game.*/
    public void pause() {
        if (in_game) {
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
}