package alexp8.controller;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.alexp8.mathjumble.R;
import com.google.android.gms.common.SignInButton;

/**
 * Fragment holding UI for main menu.
 */
public class MainMenuFragment extends Fragment {

    private Listener my_listener = null;
    private Activity myActivity;

    public interface Listener {
        void signOut();
        void signIn();
        void playGame(String difficulty);
        void displayLeaderboards();
        boolean signedIn();
        String getName();
        String getImgUrl();
        boolean inGame();
        void setInGame(boolean b);
    }

    public void setListener(Listener listener) {
        my_listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_main_menu, container, false);
        final MenuListener my_click_listener = new MenuListener();

        final int[] buttons = new int[] {
                R.id.play_button, R.id.scores_button,
                R.id.sign_in_button, R.id.sign_out_button,
                R.id.easy_button, R.id.normal_button,
                R.id.hard_button, R.id.how_to_play_button, R.id.exit_how_to_play
        };

        for (int i : buttons)
            v.findViewById(i).setOnClickListener(my_click_listener);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (my_listener.inGame()) my_listener.setInGame(false);

        if (my_listener.signedIn())
            displaySignedIn(true);
        else
            displaySignedIn(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity)
            myActivity = (Activity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void showDifficultyButtons(final boolean visible) {
        final LinearLayout my_difficulty_buttons = (LinearLayout) myActivity.findViewById(R.id.difficulty_buttons_layout);

        if (visible)
            my_difficulty_buttons.setVisibility(View.VISIBLE);
        else
            my_difficulty_buttons.setVisibility(View.GONE);

    }

    /** */
    public void signOut() {
        displaySignedIn(false);
    }

    /**
     * Display the corresponding views if the user is signed in or signed out.
     * @param signedIn boolean true or false if user is signed in
     */
    public void displaySignedIn(final boolean signedIn) {

        final ImageView user_pic = (ImageView) myActivity.findViewById(R.id.user_pic);
        final TextView user_name = (TextView) myActivity.findViewById(R.id.user_name);
        final SignInButton signInButton = (SignInButton) myActivity.findViewById(R.id.sign_in_button);
        final Button signOutButton= (Button) myActivity.findViewById(R.id.sign_out_button);

        if (signedIn) {
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
            user_name.setText(my_listener.getName());
            user_name.setVisibility(View.VISIBLE);
            Glide.with(this).load(my_listener.getImgUrl()).into(user_pic);
            user_pic.setVisibility(View.VISIBLE);
        } else {
            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.GONE);
            user_name.setVisibility(View.GONE);
            user_pic.setVisibility(View.GONE);
        }
    }

    /**
     * Perform the action of the corresponding button.
     */
    private class MenuListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.sign_in_button:
                    my_listener.signIn();
                    break;
                case R.id.sign_out_button:
                    my_listener.signOut();
                    break;
                case R.id.how_to_play_button:
                    myActivity.findViewById(R.id.HowToPlayLayout).setVisibility(View.VISIBLE);
                    break;
                case R.id.exit_how_to_play:
                    showDifficultyButtons(false);
                    myActivity.findViewById(R.id.HowToPlayLayout).setVisibility(View.GONE);
                    break;
                case R.id.scores_button:
                    my_listener.displayLeaderboards();
                    showDifficultyButtons(false);
                    break;
                case R.id.play_button:
                    showDifficultyButtons(true);
                    break;
                case R.id.easy_button:
                    my_listener.playGame(getString(R.string.easy_string));
                    break;
                case R.id.normal_button:
                    my_listener.playGame(getString(R.string.normal_string));
                    break;
                case R.id.hard_button:
                    my_listener.playGame(getString(R.string.hard_string));
                    break;
            }
        }
    }
}