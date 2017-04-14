package alexp8.controller;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.*;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.example.alexp8.mathjumble.R;

import com.flurry.android.FlurryAgent;

import java.io.Serializable;

import alexp8.model.MathJumble;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    public static GoogleApiClient myGoogleApiClient;
    private GoogleSignInOptions myGoogleSignInOptions;
    private static int RC_SIGN_IN = 9001;
    private static int RC_UNUSED = 5001;

    private SignInButton sign_in_button;
    private Button sign_out_button;
    private TextView user_name;
    private ImageView user_pic;
    private MJButtonListener myButtonListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        myGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        myGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, myGoogleSignInOptions)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        myButtonListener = new MJButtonListener();
        /*
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, "TKFQ6GDYM5GR67BCKJSK");
        */

        setUpViews();
    }

    private void setUpViews() {

        user_name = (TextView) findViewById(R.id.user_name);
        user_pic = (ImageView)findViewById(R.id.user_pic);

        final Button play_button = (Button) findViewById(R.id.play_button);
        play_button.setOnClickListener(myButtonListener);

        final Button scores_button = (Button) findViewById(R.id.scores_button);
        scores_button.setOnClickListener(myButtonListener);

        sign_in_button = (SignInButton) findViewById(R.id.sign_in_button);
        sign_in_button.setSize(SignInButton.SIZE_STANDARD);
        sign_in_button.setOnClickListener(myButtonListener);

        sign_out_button = (Button) findViewById(R.id.sign_out_button);
        sign_out_button.setOnClickListener(myButtonListener);

        final Button easy_button = (Button) findViewById(R.id.easy_button);
        easy_button.setOnClickListener(myButtonListener);

        final Button normal_button = (Button) findViewById(R.id.normal_button);
        normal_button.setOnClickListener(myButtonListener);

        final Button hard_button = (Button) findViewById(R.id.hard_button);
        hard_button.setOnClickListener(myButtonListener);
    }

    /**
     *
     */
    private void displayLeaderboards() {
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
    private void signIn() {

        Intent intent = Auth.GoogleSignInApi.getSignInIntent(myGoogleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private void signOut() {

        Auth.GoogleSignInApi.signOut(myGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                // show sign-in button, hide the sign-out button
                sign_in_button.setVisibility(View.VISIBLE);
                sign_out_button.setVisibility(View.GONE);
                user_name.setVisibility(View.GONE);
                user_pic.setVisibility(View.GONE);
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

                sign_in_button.setVisibility(View.GONE);
                sign_out_button.setVisibility(View.VISIBLE);

                final String name = result.getSignInAccount().getDisplayName().toString();
                user_name.setText(name);
                user_name.setVisibility(View.VISIBLE);

                final String img_url = result.getSignInAccount().getPhotoUrl().toString();
                Glide.with(this).load(img_url).into(user_pic);
                user_pic.setVisibility(View.VISIBLE);

            } else {
                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, R.string.signin_failure);
            }
        }
    }

    /**
     *
     * @param difficulty of game
     */
    private void playGame(String difficulty) {
        final Intent play = new Intent(getApplicationContext(), PlayActivity.class);
        play.putExtra("Difficulty", difficulty);
        startActivity(play);
    }

    private class MJButtonListener implements View.OnClickListener {

        public MJButtonListener() {

        }

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.play_button:
                    findViewById(R.id.difficulty_buttons_layout).setVisibility(View.VISIBLE);
                    break;
                case R.id.scores_button:
                    displayLeaderboards();
                    break;
                case R.id.sign_in_button:
                    signIn();
                    break;
                case R.id.sign_out_button:
                    signOut();
                    break;
                case R.id.easy_button:
                    playGame("Easy");
                    break;
                case R.id.normal_button:
                    playGame("Normal");
                    break;
                case R.id.hard_button:
                    playGame("Hard");
                    break;
            }
        }
    }
}
