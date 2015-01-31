package com.gamejam.crashrun.game;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.gamejam.crashrun.MainActivity;

/**
 * Created by David on 2015-01-30.
 */
public class Game {
    private int level;
    private int score;
    private int time;
    public void newGame() {
        level = 1;
        score = 0;
        time = 0;
        Log.d("game lvl", String.valueOf(level));
    }

    public int levelAdd(int levelV) {
        level += levelV;
        Log.d("game lvl", String.valueOf(level));
        return level;

    }

    public int scoreAdd(int scoreV) {
        score += scoreV;
        Log.d("game score", String.valueOf(score));
        return score;

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
