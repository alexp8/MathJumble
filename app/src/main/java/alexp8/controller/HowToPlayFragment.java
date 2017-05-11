package alexp8.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.alexp8.mathstone.R;

/**
 *
 */
public class HowToPlayFragment extends Fragment {

    private Listener my_listener;

    interface Listener {
        void closeHowToPlay();
    }

    public void setListener(Listener listener) {
        my_listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_how_to_play, container, false);

        final Button close_button = ((Button) v.findViewById(R.id.close_how_to_play));
        close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                my_listener.closeHowToPlay();
            }
        });

        return v;
    }
}