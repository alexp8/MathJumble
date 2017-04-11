package alexp8.controller;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

import com.example.alexp8.mathjumble.R;
import com.flurry.android.FlurryAgent;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 9001;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;
    boolean mExplicitSignOut = false;
    boolean mInSignInFlow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                // add other APIs and scopes here as needed
                .build();

        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, "TKFQ6GDYM5GR67BCKJSK");

        setupDifficultyButtons();
    }

    private void setupDifficultyButtons() {

        final LinearLayout layout = (LinearLayout) findViewById(R.id.difficulty_buttons_layout);

        final Button play_button = (Button) findViewById(R.id.play_button);
        play_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                layout.setVisibility(View.VISIBLE);
            }
        });

        final Button scores_button = (Button) findViewById(R.id.scores_button);
        scores_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            }
        });

        final SignInButton sign_in_button = (SignInButton) findViewById(R.id.sign_in_button);
        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignInClicked = true;
                mGoogleApiClient.connect();
            }
        });

        final Button sign_out_button = (Button) findViewById(R.id.sign_out_button);
        sign_out_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sign out.
                mSignInClicked = false;
                mExplicitSignOut = true;
                Games.signOut(mGoogleApiClient);
                mGoogleApiClient.disconnect();

                // show sign-in button, hide the sign-out button
                findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            }
        });

        final Button easy_button = (Button) findViewById(R.id.easy_button);
        easy_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent play = new Intent(getApplicationContext(), PlayActivity.class);
                play.putExtra("Difficulty", "Easy");
                startActivity(play);
            }
        });

        final Button normal_button = (Button) findViewById(R.id.normal_button);
        normal_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent play = new Intent(getApplicationContext(), PlayActivity.class);
                play.putExtra("Difficulty", "Normal");
                startActivity(play);
            }
        });

        final Button hard_button = (Button) findViewById(R.id.hard_button);
        hard_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent play = new Intent(getApplicationContext(), PlayActivity.class);
                play.putExtra("Difficulty", "Hard");
                startActivity(play);
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        // if the sign-in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, "unable to sign in")) {
                mResolvingConnectionFailure = false;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mInSignInFlow && !mExplicitSignOut) {
            // auto sign in
            mGoogleApiClient.connect();
        }
    }

}
