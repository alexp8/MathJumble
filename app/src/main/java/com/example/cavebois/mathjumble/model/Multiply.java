package com.example.cavebois.mathjumble.model;

/**
 * Created by Cave Bois on 3/29/2017.
 */

public class Multiply extends AbstractOperation {
    /**The starting range of number for multiply and dividing.*/
    private static final int START_MAX = 6, START_MIN = 3;

    /** */
    private static final int MIN_INCREASE = 1, MAX_INCREASE = 2;

    private static final int SCORE_BONUS = 5;
    private static final String LABEL = "*";

    public Multiply() {super(START_MIN, START_MAX, MIN_INCREASE, MAX_INCREASE, SCORE_BONUS, LABEL);}

    /**
     * Private helper method to calculate "random" variables and missing variable.
     * @param the_variables array to hold the three variables
     */
    @Override
    public void calculateVariables(final int[] the_variables) {
        the_variables[0]  = my_rand.nextInt(cur_max - cur_min) + cur_min; //a = {cur_min, cur_max - 1}
        the_variables[1] = my_rand.nextInt(cur_max - cur_min) + cur_min; //b = {cur_min, cur_max - 1}
        the_variables[2] = the_variables[0] * the_variables[1];
    }
}
