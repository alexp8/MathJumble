package alexp8.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.*;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.example.alexp8.mathjumble.R;

import com.flurry.android.FlurryAgent;

import java.util.Random;
import java.util.Set;

import alexp8.model.MathJumble;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, MainMenuFragment.Listener, GameplayFragment.Listener {

    private static final String EASY_LEADERBOARD_ID = "CgkIpYejmpQaEAIQAg",
            NORMAL_LEADERBOARD_ID ="CgkIpYejmpQaEAIQAw",
            HARD_LEADERBOARD_ID = "CgkIpYejmpQaEAIQBA";

    private static final long TWELVE_HOURS = 1000 * 60 * 60 * 12;
    private GoogleApiClient myGoogleApiClient;
    private GoogleSignInOptions myGoogleSignInOptions;
    private static int RC_SIGN_IN = 9001;
    private static int RC_UNUSED = 5001;

    private GameplayFragment myGameplayFragment;
    private MainMenuFragment myMainMenuFragment;

    //time in milliseconds for game to be played (15 seconds)
    private static final long START_TIME = 15 * 1000;
    private static final int ONE_SECOND = 1000;

    private long my_time;
    private Random rand = new Random();

    private int a = 0, b = 0, c = 0, answer = 0;
    private CountDownTimer my_timer;
    private String my_difficulty, my_leaderboard_id, my_name, my_img_url;

    private MathJumble my_jumble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        myMainMenuFragment = new MainMenuFragment();
        myGameplayFragment = new GameplayFragment();

        myMainMenuFragment.setArguments(getIntent().getExtras());


        myMainMenuFragment.setListener(this);
        myGameplayFragment.setListener(this);

        myGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        myGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, myGoogleSignInOptions)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        /*
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, "TKFQ6GDYM5GR67BCKJSK");
        */

    }

    /**
     * Sign in the user on start.
     */
    @Override
    protected void onStart() {
        super.onStart();
        signIn();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                myMainMenuFragment).commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Display the leaderboards to the user.
     */
    public void displayLeaderboards() {
        if (signedIn())
            startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(myGoogleApiClient),
                    RC_UNUSED);
        else
            BaseGameUtils.makeSimpleDialog(this, getString(R.string.leaderboards_not_available)).show();
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
     * Handle the user attempting to sign in.
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);

            if (result.isSuccess()) {
                myGoogleApiClient.connect();

                my_name = result.getSignInAccount().getDisplayName().toString();
                my_img_url = result.getSignInAccount().getPhotoUrl().toString();

                myMainMenuFragment.displaySignedIn(true);
            } else {
                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, R.string.signin_failure);
            }
        } else {

        }
    }

    /**
     * Play the game at the set difficulty.
     * @param the_difficulty of game
     */
    public void playGame(final String the_difficulty) {
        my_difficulty = the_difficulty;
        my_jumble = new MathJumble(the_difficulty);
        switchToFragment(myGameplayFragment);

        switch (my_difficulty) {
            case "Normal":
                my_leaderboard_id = NORMAL_LEADERBOARD_ID;
                break;
            case "Hard":
                my_leaderboard_id = HARD_LEADERBOARD_ID;
                break;
            default:
                my_leaderboard_id = EASY_LEADERBOARD_ID;
                break;
        }
        startTimer();
    }

    /**
     *
     */
    public void howToPlay() {

    }

    /**
     * Create the next problem to be solved and display it on the screen.
     */
    public void nextProblem() {
        my_jumble.nextProblem();
        final int[] variables = my_jumble.getVariables();
        final Set<Integer> answers = my_jumble.getAnswers();

        myGameplayFragment.updateTextViews(variables, my_jumble.getOperationText(), my_jumble.getUnknownIndex());
        myGameplayFragment.updateButtons(answers);
    }

    private void startTimer() {
        my_time = START_TIME;
        my_timer = new CountDownTimer(TWELVE_HOURS, ONE_SECOND) { //game will not run longer than 12 hours ;)
            public void onTick(long milliSeconds) {
                my_time -= 1000;
                myGameplayFragment.updateTimer(String.valueOf(my_time / 1000));

                if (my_time == 0) {
                    lose();
                }
            }
            public void onFinish() {
                //you just played for 12 hours you genius/cheater ?
                myGameplayFragment.updateTimer(String.valueOf(0));
                lose();
            }
        }.start();
    }

    @Override
    public void answerButtonClick(String value) {
        if (value == null || value.equals("")) return;

        final int answer = Integer.valueOf(value);
        final boolean correct = my_jumble.answer(answer);

        if (correct) {
            nextProblem();
            my_time += my_jumble.getTimerIncrease();
            myGameplayFragment.updateScore(String.valueOf(my_jumble.getScore()));
            myGameplayFragment.updateTimer(String.valueOf(my_time / 1000));
        }
        else
            lose();
    }


    /**
     *
     */
    private void lose() {

        my_timer.cancel();

        //submit the score otherwise store locally
        if (signedIn()) {
            Games.Leaderboards.submitScore(myGoogleApiClient, my_leaderboard_id,  my_jumble.getScore());
        }

        final String score = String.valueOf(my_jumble.getScore());
        myGameplayFragment.gameOver(score, true);
    }

    @Override
    public void playAgain() {
        my_jumble = new MathJumble(my_difficulty);
        startTimer(); //reset and start timer
        nextProblem(); //display next problem
    }

    @Override
    public void menu() {
        switchToFragment(myMainMenuFragment);
    }

    /**
     *
     * @param newFrag
     */
    private void switchToFragment(Fragment newFrag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newFrag)
                .commit();
    }
}