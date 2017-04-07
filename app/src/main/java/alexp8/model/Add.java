package alexp8.model;

/**
 * Created by Alex Peterson on 3/23/2017.
 */

public class Add extends AbstractOperation {
    /**The starting range of number for adding and subtracting.*/
    private static final int START_MAX = 15, START_MIN = 5;

    /** */
    private static final int MIN_INCREASE = 3, MAX_INCREASE = 5;

    /** */
    private static final int SCORE_BONUS = 50;
    private static final String LABEL = "+";
    /**
     *
     */
    public Add() {
        super(START_MIN, START_MAX, MIN_INCREASE, MAX_INCREASE, SCORE_BONUS, LABEL);
    }

    /**
     * Private helper method to calculate "random" variables and missing variable.
     * @param the_variables array to hold the three variables
     */
    @Override
    public void calculateVariables(final int[] the_variables) {
        the_variables[0]  = my_rand.nextInt(cur_max) + cur_min; //a = {cur_min, cur_min + cur_max - 1}
        the_variables[1] = my_rand.nextInt(cur_max) + cur_min; //b = {cur_min, cur_min + cur_max - 1}
        the_variables[2] = the_variables[0] + the_variables[1];
    }
}
