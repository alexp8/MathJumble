package alexp8.controller;

import android.app.AlertDialog;
import android.content.Intent;
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
    private String my_difficulty;

    private MathJumble my_jumble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        myMainMenuFragment = new MainMenuFragment();
        myGameplayFragment = new GameplayFragment();

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

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                myMainMenuFragment).commit();

        /*
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, "TKFQ6GDYM5GR67BCKJSK");
        */


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

    private boolean signedIn() {
        return (myGoogleApiClient != null && myGoogleApiClient.isConnected());
    }
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
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("MathJumble", "onConnectionFailed() called, result: " + connectionResult);
    }

    /**
     *
     */
    @Override
    protected void onStart() {
        super.onStart();
        signIn();
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == RC_SIGN_IN) {

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
            if (result.isSuccess()) {

                final String name = result.getSignInAccount().getDisplayName().toString();
                final String img_url = result.getSignInAccount().getPhotoUrl().toString();

                myMainMenuFragment.signIn(true, name, img_url);
            } else {
                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, R.string.signin_failure);
            }
        }
    }

    /**
     * Play the game at the set difficulty.
     * @param the_difficulty of game
     */
    public void playGame(final String the_difficulty) {
        my_jumble = new MathJumble(the_difficulty);
        switchToFragment(myGameplayFragment);
        startTimer();
        //nextProblem();
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

    /**
     *
     */
    private void lose() {

        if (signedIn())
            Games.Leaderboards.submitScore(myGoogleApiClient, my_jumble.getLeaderboardID(),  my_jumble.getScore());
        else {
            //store locally
        }

        my_timer.cancel();
        my_jumble.lose();

        final String score = String.valueOf(my_jumble.getScore());
        myGameplayFragment.updateScore(score);
        myGameplayFragment.flipActiveViews(0.5, false);
        myGameplayFragment.gameOver();
    }

    private void startTimer() {
        my_time = START_TIME;
        my_timer = new CountDownTimer(START_TIME, ONE_SECOND) {
            public void onTick(long milliSeconds) {
                my_time -= 1000;
                myGameplayFragment.updateTimer(String.valueOf(my_time / 1000));
            }
            public void onFinish() {
                myGameplayFragment.updateTimer(String.valueOf(0));
                lose();
            }
        }.start();
    }

    @Override
    public void answerButtonClick(String value) {

        final int answer = Integer.valueOf(value);
        final boolean correct = my_jumble.answer(answer);

        if (correct) {
            nextProblem();
            my_time += my_jumble.getTimerIncrease();
            myGameplayFragment.updateScore(String.valueOf(my_jumble.getScore()));
        }
        else
            lose();
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
