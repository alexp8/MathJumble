package alexp8.model;

import com.example.alexp8.mathjumble.R;

import java.util.Random;
import java.util.Set;

/**
 * Created by Alex Peterson on 3/17/2017.
 */
public class MathJumble {
    /**Amount of bonus time given upon correct answer.*/
    private static final int TIMER_INCREASE = 1000;

    private Random rand = new Random();
    private int my_score = 0;
    private Operation my_operation;
    private AbstractOperation my_add, my_subtract, my_multiply, my_divide;

    /**
     *
     */
    public MathJumble(String the_difficulty) {
                my_add = new Add(the_difficulty);
        my_divide = new Divide(the_difficulty);
        my_multiply = new Multiply(the_difficulty);
        my_subtract = new Subtract(the_difficulty);
    }

    /**
     * Generates 3 new variables as well as 3 possible answers for the missing variable.
     */
    public void nextProblem() {

        final int rand_operation = rand.nextInt(4);
        switch (rand_operation) {
            case 0:
                my_operation = my_add;
                break;
            case 1:
                my_operation = my_divide;
                break;
            case 2:
                my_operation = my_subtract;
                break;
            default:
                my_operation = my_multiply;
                break;
        }
        my_operation.operate();
    }

    /**
     * Determine if user answered correctly and progress game.
     * @param the_answer user's answer
     * @return true if user answered correctly
     */
    public boolean answer (final int the_answer) {
        if (my_operation.getAnswer() == the_answer) {
            my_score += my_operation.getScoreBonus(); //increment score
            my_operation.increaseRange(); //increase game difficulty creating bigger numbers
            return true;
        } else {
            return false;
        }
    }

    /**Getters for data that controller will need to display new data.*/
    public int getUnknownIndex() {return my_operation.getUnknownIndex();}
    public String getOperationText() {return my_operation.toString();}
    public int getScore() {return my_score;}
    public int getTimerIncrease() {return TIMER_INCREASE;}
    public int[] getVariables() {return my_operation.getVariables();}
    public Set<Integer> getAnswers() {return my_operation.getAnswers();}
}