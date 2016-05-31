package com.example.waichiuyung.colourmemory;

/**
 * Created by waichiuyung on 16/4/16.
 */
public class Score implements Comparable<Score>{
    private String scoreUsername;
    public int scoreNum;

    public Score(String username, int num){
        scoreUsername=username;
        scoreNum=num;
    }

    public int compareTo(Score sc){
        //return 0 if equal
        //1 if passed greater than this
        //-1 if this greater than passed
        return sc.scoreNum>scoreNum? 1 : sc.scoreNum<scoreNum? -1 : 0;
    }

    public String getScoreText()
    {
        return scoreUsername+" - "+scoreNum;
    }

}