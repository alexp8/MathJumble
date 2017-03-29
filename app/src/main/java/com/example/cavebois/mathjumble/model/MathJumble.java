package com.example.cavebois.mathjumble.model;

import android.widget.Button;

import java.util.Observable;
import java.util.Random;
import java.util.jar.Pack200;

import static com.example.cavebois.mathjumble.R.id.a_textview;

/**
 * Created by Alex Peterson on 3/17/2017.
 */
public class MathJumble {

    private Random rand = new Random();
    private int unknown_index = 0, my_score = 0, my_cur_max_increase, my_timer_increase;
    private Operation my_operation;
    private AbstractOperation my_add, my_subtract, my_multiply, my_divide;

    /**
     *
     * @param the_difficulty the difficulty setting as string
     */
    public MathJumble(final String the_difficulty) {
        int add_max = 15, div_max = 5;
        if (the_difficulty.equals("Easy")) {
            my_cur_max_increase = 2;
            add_max = 15;
            my_timer_increase = 3000;
        } else if (the_difficulty.equals("Normal")) {
            my_cur_max_increase = 3;
            add_max = 30;
            my_timer_increase = 2000;
        } else {
            my_cur_max_increase = 5;
            add_max = 50;
            my_timer_increase = 1000;
        }

        my_add = new Add(add_max);
        my_divide = new Divide(div_max);
    }

    /**
     * Generates 3 new variables as well as 3 possible answers for the missing variable.
     * @return a 2D array, first row is the variables, second row the possible answers
     */
    public int[][] nextProblem() {

        int rand_operation = rand.nextInt(4);

        if (rand_operation == 0 || rand_operation > 1 ) { //add
            my_operation = my_add;
        } else if (rand_operation == 1) { //divide
            my_operation = my_divide;
        } else if (true) { //multiply

        } else { //divide

        }

        return my_operation.operate();
    }

    public void lose() {

    }

    public boolean answer (final int the_answer) {
        if (my_operation.getAnswer() == the_answer) {
            my_score += my_operation.getScoreBonus();
            //increase game difficulty creating bigger numbers
            my_operation.increaseRange(my_cur_max_increase);
            return true;
        } else {
            return false;
        }
    }

    public int getUnknownIndex() {
        return my_operation.getUnknownIndex();
    }

    public String getOperationText() {
        return my_operation.toString();
    }

    public int getScore() {
        return my_score;
    }

    public int getTimerIncrease() {return my_timer_increase;}
}