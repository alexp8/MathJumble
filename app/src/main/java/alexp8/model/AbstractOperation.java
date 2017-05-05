package alexp8.model;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by Alex Peterson on 3/23/2017.
 */
abstract class AbstractOperation implements Operation {

    private int my_answer = 0, my_unknown_index = 0, my_score_bonus,
            my_max_increase;
    int cur_max;
    Random my_rand;
    private String my_label;
    private int[] my_variables;
    private Set<Integer> my_answers;


    /**
     *
     * @param the_score_bonus the points rewarded for completing problem type
     * @param the_label the operation string of the current problem
     */
    AbstractOperation(final int the_score_bonus, final String the_label, final String the_difficulty,
                             final int[] easy, final int[] normal, final int[] hard) {

        my_score_bonus = the_score_bonus;
        my_label = the_label;

        my_variables = new int[3];
        my_rand = new Random();
        my_answers = new HashSet<>();

        switch (the_difficulty) {
            case "Easy":
                cur_max = easy[0];
                my_max_increase = easy[1];
                break;
            case "Hard":
                cur_max = hard[0];
                my_max_increase = hard[1];
                break;
            default:
                cur_max = normal[0];
                my_max_increase = normal[1];
                break;
        }
    }

    /**Getters for data MathJumble will need.*/
    public int getUnknownIndex() {return my_unknown_index;}
    public int getAnswer() {return my_answer;}
    public int getScoreBonus() {return my_score_bonus;}
    public String toString() {return my_label;}
    public int[] getVariables() {return my_variables;}
    public Set<Integer> getAnswers() {return my_answers;}

    /**
     * Increase range of numbers that can be chosen.
     */
    public void increaseRange() {
        cur_max += my_max_increase;
    }

    /**
     * Abstract method to be overridden by child classes.
     * @param vars fill an array with 3 numbers for the problem
     */
    protected abstract void calculateVariables(int[] vars);

    /**
     *
     */
    public void operate() {
        my_answers.clear();
        my_unknown_index = my_rand.nextInt(2); //a, b, or c to randomly chosen as unknown variable

        //calculate the three random variables, as well as the correct answer for missing variable
        calculateVariables(my_variables);
        my_answer = my_variables[my_unknown_index];

        //calculates 2 fake answers (fake answers are close to real answers
        calculateFakeAnswers();
        my_answers.add(my_answer);
    }

    /**
     * Calculate 2 fake answers, fake answers close to real answer.
     */
    private void calculateFakeAnswers() {
        for (int i = 0; i < 2; i++) {
            int fake_answer;
            do {
                //calculate how close the fake answer will be to the real one
                int rand_difference = my_rand.nextInt(my_rand.nextInt(5) + 1) + 1;

                int plus_or_minus = my_rand.nextInt(2); //fake answer less than or greater than real answer
                if (plus_or_minus == 0 && my_answer - rand_difference > 0)
                    fake_answer = my_answer - rand_difference;
                else
                    fake_answer = my_answer + rand_difference;
            } while (my_answers.contains(fake_answer) || fake_answer == my_answer);

            my_answers.add(fake_answer);
        }
    }
}