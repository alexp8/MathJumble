package alexp8.model;

/**
 * Subtract class child of AbstractOperation.
 * Holds data for scores, difficulty increases, and variable calculation for subtraction problems.
 * Created by Cave Bois on 3/29/2017.
 */
public class Subtract extends AbstractOperation {

    /**3 arrays holding starting max value, and max increase as game progresses*/
    private static final int[] EASY_DIFFICULTY = {10, 5};
    private static final int[] NORMAL_DIFFICULTY = {10, 5};
    private static final int[] HARD_DIFFICULTY = {10, 5};

    private static final int MINIMUM = 3;

    private static final int SCORE_BONUS = 50;
    private static final String LABEL = "-";

    public Subtract(final String the_difficulty) {
        super(SCORE_BONUS, LABEL, the_difficulty, EASY_DIFFICULTY, NORMAL_DIFFICULTY, HARD_DIFFICULTY);
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
