package alexp8.controller;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alexp8.mathstone.R;
import com.google.android.gms.common.SignInButton;

/**
 * Fragment holding UI for main menu.
 */
public class MainMenuFragment extends Fragment {

    private static final float FULL = 1;
    private static final float TRANSPARENT = (float) 0.5;

    private Listener my_listener = null;
    private Activity myActivity;

    private int[] button_ids;
    private String my_name;

    interface Listener {
        void signOut();
        void signIn();
        void playGame(String difficulty);
        void displayLeaderboards();
        boolean signedIn();
        boolean inGame();
        void setInGame(boolean b);
        void howToPlay();
        void displayAchievements();
    }

    public void setListener(Listener listener) {
        my_listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_main_menu, container, false);
        final MenuListener my_click_listener = new MenuListener();

        button_ids = new int[] {
                R.id.sign_in_button, R.id.play_button,
                R.id.sign_out_button, R.id.scores_button,
                R.id.easy_button, R.id.normal_button,
                R.id.hard_button, R.id.settings,
                R.id.achievements_button, R.id.how_to_play_button,
                R.id.close_settings
        };

        for (int i : button_ids)
            v.findViewById(i).setOnClickListener(my_click_listener);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (my_listener.inGame())
            my_listener.setInGame(false);

        if (my_listener.signedIn())
            displaySignedIn(true);
        else
            displaySignedIn(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            myActivity = (Activity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void showDifficultyButtons(final boolean show_difficulty_buttons) {
        final LinearLayout my_difficulty_buttons = (LinearLayout) myActivity.findViewById(R.id.difficulty_buttons_layout);

        myActivity.findViewById(R.id.play_button).setVisibility(show_difficulty_buttons ? View.GONE : View.VISIBLE);
        my_difficulty_buttons.setVisibility(show_difficulty_buttons ? View.VISIBLE : View.GONE);
    }

    public void setName(final String the_name) {
        my_name = the_name;
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

        final SignInButton signInButton = (SignInButton) myActivity.findViewById(R.id.sign_in_button);
        signInButton.setColorScheme(SignInButton.COLOR_AUTO);
        final Button signOutButton= (Button) myActivity.findViewById(R.id.sign_out_button);

        final TextView name = (TextView) myActivity.findViewById(R.id.user_name);
        name.setText(my_name);
        name.setVisibility(signedIn ? View.VISIBLE : View.GONE);

        signInButton.setVisibility(signedIn ? View.GONE : View.VISIBLE);
        signOutButton.setEnabled(signedIn);
    }

    /**Disable all other buttons while in settings mode.*/
    private void disableOtherButtons(boolean active) {

        final SignInButton sb = (SignInButton) myActivity.findViewById(R.id.sign_in_button);
        sb.setEnabled(active);
        sb.setAlpha(active ? FULL : TRANSPARENT);

        for (int i = 1; i < button_ids.length; i++) {
            final Button b = (Button) myActivity.findViewById(button_ids[i]);

            if (!(button_ids[i] == R.id.close_settings || button_ids[i] == R.id.sign_out_button)) {
                b.setEnabled(active);
                sb.setAlpha(active ? FULL : TRANSPARENT);
            }
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
                    howToPlay();
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
                case R.id.settings:
                    settings();
                    break;
                case R.id.close_settings:
                    closeSettings();
                    break;
                case R.id.achievements_button:
                    my_listener.displayAchievements();
                    break;
                default:
                    break;
            }
        }

        private void closeSettings() {
            disableOtherButtons(true);
            myActivity.findViewById(R.id.settings_layout).setVisibility(View.GONE);
        }

        private void settings() {
            disableOtherButtons(false);
            myActivity.findViewById(R.id.settings_layout).setVisibility(View.VISIBLE);
        }

        private void howToPlay() {
            my_listener.howToPlay();
            showDifficultyButtons(false);
        }
    }
}