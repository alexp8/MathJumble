package alexp8.model;

/**
 * Multiply class child of AbstractOperation.
 * Holds data for scores, difficulty increases, and variable calculation for multiplication problems.
 * Created by Alex Peterson on 3/29/2017.
 */
public class Multiply extends AbstractOperation {

    private static final int[] EASY = {6, 3};
    private static final int[] NORMAL = {10, 4};
    private static final int[] HARD = {25, 8};

    private static final int MINIMUM = 3;

    private static final int SCORE_BONUS = 75;
    private static final String LABEL = "*";

    public Multiply(final String the_difficulty) {
        super(SCORE_BONUS, LABEL, the_difficulty, EASY, NORMAL, HARD);
    }

    /**
     * Private helper method to calculate "random" variables and missing variable.
     * @param the_variables array to hold the three variables
     */
    @Override
    public void calculateVariables(final int[] the_variables) {
        the_variables[0] = my_rand.nextInt(cur_max - MINIMUM) + MINIMUM; //a = {MINIMUM, cur_max - 1}
        the_variables[1] = my_rand.nextInt(cur_max - MINIMUM) + MINIMUM; //b = {MINIMUM, cur_max - 1}
        the_variables[2] = the_variables[0] * the_variables[1];
    }
}