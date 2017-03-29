package com.example.cavebois.mathjumble.model;

/**
 * Created by Alex Peterson on 3/23/2017.
 */

public interface Operation {

    int[][] operate();

    int getUnknownIndex();

    String toString();

    int getAnswer();

    void increaseRange(int inc);

    int getScoreBonus();
}
