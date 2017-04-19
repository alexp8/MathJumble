package alexp8.model;

/**
 * Created by Cave Bois on 3/29/2017.
 */

public class Subtract extends AbstractOperation {

    private static final int[] EASY = {10, 5};
    private static final int[] NORMAL = {10, 5};
    private static final int[] HARD = {10, 5};

    private static final int MINIMUM = 3;

    /** */
    private static final int SCORE_BONUS = 50;
    private static final String LABEL = "-";

    public Subtract(final String the_difficulty) {
        super(SCORE_BONUS, LABEL, the_difficulty, EASY, NORMAL, HARD);
    }

    /**
     * Private helper method to calculate "random" variables and missing variable.
     * @param the_variables array to hold the three variables
     */
    @Override
    public void calculateVariables(final int[] the_variables) {
        the_variables[1] = my_rand.nextInt(cur_max) + MINIMUM; //b = {MINIMUM, cur_max - 1}
        the_variables[2] = my_rand.nextInt(cur_max) + MINIMUM; //c = {MINIMUM, b - 1}
        the_variables[0] = the_variables[1] + the_variables[2]; //a = b + c  same as c = a - b
    }
}
