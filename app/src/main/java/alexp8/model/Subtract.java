package alexp8.model;

/**
 * Created by Cave Bois on 3/29/2017.
 */

public class Subtract extends AbstractOperation {
    /**The starting range of number for adding and subtracting.*/
    private static final int START_MAX = 15, START_MIN = 5;

    /** */
    private static final int MIN_INCREASE = 3, MAX_INCREASE = 5;

    /** */
    private static final int SCORE_BONUS = 50;
    private static final String LABEL = "-";

    public Subtract() {super(START_MIN, START_MAX, MIN_INCREASE, MAX_INCREASE, SCORE_BONUS, LABEL);}

    /**
     * Private helper method to calculate "random" variables and missing variable.
     * @param the_variables array to hold the three variables
     */
    @Override
    public void calculateVariables(final int[] the_variables) {
        the_variables[0] = my_rand.nextInt(cur_max) + cur_min; //a = {cur_min, cur_min + cur_max - 1}
        the_variables[1] = my_rand.nextInt(the_variables[0] - cur_min) + cur_min; //b = {a - cur_min, cur_min + a - 1}
        the_variables[2] = the_variables[0] - the_variables[1];
    }
}
