package com.example.cavebois.mathjumble.model;

import java.util.Random;

/**
 * Created by Alex Peterson on 3/23/2017.
 */

public abstract class AbstractOperation implements Operation {

    protected int my_answer, cur_max, cur_min, my_unknown_index, my_score_bonus,
            my_max_increase, my_min_increase;
    protected Random my_rand;
    private String my_label;

    /**
     *
     * @param the_score_bonus the points rewarded for completing problem type
     * @param the_label the operation string of the current problem
     */
    public AbstractOperation(final int the_start_min, final int the_start_max, final int the_min_increase,
                             final int the_max_increase, final int the_score_bonus,
                             final String the_label) {
        my_answer = 0;
        my_rand = new Random();
        cur_min = the_start_min;
        cur_max = the_start_max;
        my_unknown_index = 0;
        my_score_bonus = the_score_bonus;
        my_label = the_label;
        my_max_increase = the_max_increase;
        my_min_increase = the_min_increase;
    }

    public int getUnknownIndex() {
        return my_unknown_index;
    }

    public int getAnswer() {return my_answer;}

    public void increaseRange() {
        cur_max += my_max_increase;
        cur_min += my_min_increase;
    }

    /**
     * Abstract method to be overriden by child classes.
     * @param vars fill an array with 3 numbers for the problem
     * @return the answer to the created problem
     */
    public abstract void calculateVariables(int[] vars);

    public int getScoreBonus() {return my_score_bonus;}

    public String toString() {return my_label;}

    public int[][] operate() {
        final int[][] problem = new int[3][3];
        my_unknown_index = my_rand.nextInt(2); //a, b, or c to randomly chosen as unknown variable

        //calculate the three random variables, as well as the correct answer for missing variable
        calculateVariables(problem[0]);

        //calculates 2 fake answers (fake answers are close to real answer)

        my_answer = problem[0][my_unknown_index];
        final int[] answers = calculateFakeAnswers(my_answer);
        answers[2] = my_answer;

        //Shuffle the possible answers in random order
        for (int i = 0; i < 3; i++) {
            int index = my_rand.nextInt(problem[1].length);
            while (problem[1][index] != 0) {//randomly choose an index not yet chosen
                index = my_rand.nextInt(problem[1].length);
            }
            problem[1][index] = answers[i];
        }

        return problem;
    }

    /**
     *
     * @return an array of 2 "random" numbers with each number being close to the real answer of the current problem
     */
    private int[] calculateFakeAnswers(final int real_answer) {
        final int[] fake_answers = new int[3];

        for (int i = 0; i < 2; i++) {
            int fake_answer = 0;
            do {
                int rand_difference = my_rand.nextInt(my_rand.nextInt(5) + 1) + 1;

                int plus_or_minus = my_rand.nextInt(2); //fake answer less than or greater than real answer
                if (plus_or_minus == 0 && my_answer - rand_difference > 0)
                    fake_answer = my_answer - rand_difference;
                else
                    fake_answer = my_answer + rand_difference;
            } while (fake_answer == fake_answers[0] || fake_answer == real_answer);

            fake_answers[i] = fake_answer;
        }

        return fake_answers;
    }
}
