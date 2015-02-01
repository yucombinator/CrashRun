package com.gamejam.crashrun.game;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.gamejam.crashrun.MainActivity;
import com.google.android.gms.games.achievement.Achievements;

import java.util.Random;

/**
 * Created by David on 2015-01-30.
 */
public class Game {
    public int level;
    public int score;
    private double dist;
    private double steps;
    private double speed;
    public void newGame() {
        level = 1;
        score = 0;
        dist = 0;
        steps = 0;
        speed = 0;
        Log.d("game lvl", String.valueOf(level));
    }


    public int levelAdd(int levelV) {
        level += levelV;
        Log.d("game lvl", String.valueOf(level));
        return level;

    }

    public int scoreAdd(int scoreV) {
        score += scoreV;


        return score;

    }

    public double[] stats(double timeV, double stepsV, double speedV) {
        if (timeV == 0 && stepsV == 0 && speedV == 0) {
            double[] array = {dist, steps, speed};
            return array;
        }
        else {

            dist = timeV;
            steps = stepsV;
            speed = speedV;
            double[] array = {dist, steps, speed};
            return array;


        }

    }

    public long getTime() {

        int addMoreOrbs = ((level-1)/2);

        if (addMoreOrbs > 10) {
            addMoreOrbs = 10;
        }
        int totalOrbs = 4 + addMoreOrbs;

        Log.d("number of total orbs", String.valueOf(totalOrbs));


        double toAdd = (level*1.0)/10000;
        if (toAdd >= 0.008) {
            toAdd = 0.008;
        }
        double totalRadius = (toAdd + 0.002)/0.002 * 120;

        Random rand = new Random();

        int n = rand.nextInt(100) + 1;


        return (long)(1000*(300+n+totalRadius*totalOrbs/4));
        //

    }

    public void newRound( ) {
        // Start new round. Reset time and add orbs
        Log.d("game lvl", String.valueOf(level));
        if (MainActivity.cdt != null) {
            MainActivity.a = 300000;
            MainActivity.cdt.cancel();
        }

    }



}
