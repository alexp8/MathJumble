package alexp8.model;

import java.util.Set;

/**
 * Created by Alex Peterson on 3/23/2017.
 */

public interface Operation {

    void operate();

    int getUnknownIndex();

    String toString();

    int getAnswer();

    void increaseRange();

    int getScoreBonus();

    void calculateVariables(int[] variables);

    Set<Integer> getAnswers();

    int[] getVariables();
}
