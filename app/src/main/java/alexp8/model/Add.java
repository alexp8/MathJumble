package alexp8.model;

/**
 * Add class child of AbstractOperation.
 * Holds data for scores, difficulty increases, and variable calculation for addition problems.
 * Created by Alex Peterson on 3/23/2017.
 */
public class Add extends AbstractOperation {

    /**3 arrays holding starting max value, and max increase as game progresses*/
    private static final int[] EASY = {10, 10};
    private static final int[] NORMAL = {25, 25};
    private static final int[] HARD = {100, 50};

    private static final int MINIMUM = 3;

    /** */
    private static final int SCORE_BONUS = 50;
    private static final String LABEL = "+";
     /**
     *
     */
    public Add(final String the_difficulty) {
        super(SCORE_BONUS, LABEL, the_difficulty, EASY, NORMAL, HARD);
    }

    /**
     * Private helper method to calculate "random" variables and missing variable.
     * @param the_variables array to hold the three variables
     */
    @Override
    protected void calculateVariables(final int[] the_variables) {
        the_variables[0]  = my_rand.nextInt(cur_max) + MINIMUM; //a = {MINIMUM, cur_max - 1}
        the_variables[1] = my_rand.nextInt(cur_max); //b = {0, cur_max - 1}
        the_variables[2] = the_variables[0] + the_variables[1];
    }
}