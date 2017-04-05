package alexp8.model;

import java.util.Random;
import java.util.Set;

/**
 * Created by Alex Peterson on 3/17/2017.
 */
public class MathJumble {
    /**Different difficulty increases dependent on game difficulty. */
    private static final int EASY_DIFFICULTY_INCREASE = 2, NORMAL_DIFFICULTY_INCREASE = 3,
                            HARD_DIFFICULTY_INCREASE = 5;

    /**Amount of bonus time given upon correct answer.*/
    private static final int EASY_TIMER_INCREASE = 3500, NORMAL_TIMER_INCREASE = 2500, HARD_TIMER_INCREASE = 1500;

    private Random rand = new Random();
    private int unknown_index = 0, my_score = 0, my_timer_increase;
    private Operation my_operation;
    private AbstractOperation my_add, my_subtract, my_multiply, my_divide;
    private int[] my_variables, my_answers;

    /**
     *
     * @param the_difficulty the difficulty setting as string
     */
    public MathJumble(final String the_difficulty) {

        my_variables = new int[3];
        my_answers = new int[3];

        switch (the_difficulty) {
            case "Easy":
                my_timer_increase = EASY_TIMER_INCREASE;
            case "Normal":
                my_timer_increase = NORMAL_TIMER_INCREASE;
            default:
                my_timer_increase = HARD_TIMER_INCREASE;
        }

        my_add = new Add();
        my_divide = new Divide();
        my_multiply = new Multiply();
        my_subtract = new Subtract();
    }

    /**
     * Generates 3 new variables as well as 3 possible answers for the missing variable.
     * @return a 2D array, first row is the variables, second row the possible answers
     */
    public void nextProblem() {

        final int rand_operation = rand.nextInt(4);
        switch (rand_operation) {
            case 0:
                my_operation = my_add;
            case 1:
                my_operation = my_divide;
            case 2:
                my_operation = my_subtract;
            default:
                my_operation = my_multiply;
        }
        my_operation.operate();
    }

    public void lose() {

    }

    public boolean answer (final int the_answer) {
        if (my_operation.getAnswer() == the_answer) {
            my_score += my_operation.getScoreBonus();
            //increase game difficulty creating bigger numbers
            my_operation.increaseRange();
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

    public int[] getVariables() {return my_operation.getVariables();}

    public Set<Integer> getAnswers() {return my_operation.getAnswers();}
}