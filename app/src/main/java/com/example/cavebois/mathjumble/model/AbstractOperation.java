package com.example.cavebois.mathjumble.model;

import java.util.Random;

/**
 * Created by Alex Peterson on 3/23/2017.
 */

public abstract class AbstractOperation implements Operation {

    protected int my_answer, cur_max, cur_min, my_unknown_index, my_score_bonus;
    protected Random my_rand;
    private String my_label;

    public AbstractOperation(final int the_cur_max, final int the_score_bonus,
                             final String the_label) {
        my_answer = 0;
        my_rand = new Random();
        cur_max = the_cur_max;
        cur_min = 5;
        my_unknown_index = 0;
        my_score_bonus = the_score_bonus;
        my_label = the_label;
    }

    public int getUnknownIndex() {
        return my_unknown_index;
    }

    public int getAnswer() {return my_answer;}

    public void increaseRange(final int the_increase) {
        cur_max += the_increase;
        cur_min += the_increase;
    }

    public int getScoreBonus() {return my_score_bonus;}

    public String toString() {return my_label;}
}
