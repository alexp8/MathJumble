package com.example.cavebois.mathjumble.model;

/**
 * Created by Alex Peterson on 3/23/2017.
 */

public class Add extends AbstractOperation {
    private static final int SCORE_BONUS = 5;
    private static final String LABEL = "+";
    /**
     *
     * @param the_cur_max
     */
    public Add(final int the_cur_max) {
        super(the_cur_max, SCORE_BONUS, LABEL);
    }

    @Override
    /**
     * Create 2 sets of numbers. The first set a, b, c: where a + b = c.
     * The second set being three numbers with one of the numbers equal to the missing variable.
     */
    public int[][] operate() {
        final int[][] problem = new int[3][3];
        my_unknown_index = my_rand.nextInt(2); //a, b, or c to randomly chosen as unknown variable

        //calculate the three random variables, as well as the correct answer for missing variable
        my_answer = calculateVariables(problem[0]);

        //calculates 2 fake answers (fake answers are close to real answer)
        final int[] answers = calculateFakeAnswers();
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
    private int[] calculateFakeAnswers() {
        int[] fake_answers = new int[3];

        //calculates a number between 1 and 1, or 1 and 2, or 1 and 3, etc. for the fake answer to be close to real answer
        final int rand_difference = my_rand.nextInt(my_rand.nextInt(5) + 1) + 1;
        int plus_or_minus = my_rand.nextInt(2); //fake answer less than or greater than real answer
        if (plus_or_minus == 0 && my_answer - rand_difference > 0)
            fake_answers[0] = my_answer - rand_difference;
        else
            fake_answers[0] = my_answer + rand_difference;

        //now for second my_random number
        int rand_difference2 = my_rand.nextInt(my_rand.nextInt(5) + 1) + 1;
        while (rand_difference == rand_difference2) { //pick number different from first number
            rand_difference2 = my_rand.nextInt(my_rand.nextInt(5) + 1) + 1;
        }

        plus_or_minus = my_rand.nextInt(2);
        if (plus_or_minus == 0 && my_answer - rand_difference2 > 0)
            fake_answers[1] = my_answer - rand_difference2;
        else
            fake_answers[1] = my_answer + rand_difference2;

        return fake_answers;
    }

    /**
     * Private helper method to calculate "random" variables and missing variable.
     * @param the_variables array to hold the three variables
     * @return
     */
    private int calculateVariables(final int[] the_variables) {
        int my_answer = 0;

        final int a = my_rand.nextInt(cur_max) + cur_min; //a = {cur_min, cur_min + cur_max - 1}
        final int b = my_rand.nextInt(cur_max) + cur_min; //b = {cur_min, cur_min + cur_max - 1}
        final int c = a + b;

        if (my_unknown_index == 0) {
            my_answer = a;
        } else if (my_unknown_index == 1) {
            my_answer = b;
        } else {
            my_answer = c;
        }

        the_variables[0] = a;
        the_variables[1] = b;
        the_variables[2] = c;

        return my_answer;
    }

    public String toString() {
        return "+";
    }


}
