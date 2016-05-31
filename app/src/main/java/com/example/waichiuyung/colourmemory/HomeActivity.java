package com.example.waichiuyung.colourmemory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


public class HomeActivity extends AppCompatActivity  {
    private static int ROW_COUNT = -1;
    private static int COL_COUNT = -1;
    private Context context;

    private int[][] cards;
    private List<Drawable> images;
    private Card firstCard;
    private Card seconedCard;
    private ButtonListener buttonListener;

    private static Object lock = new Object();

    int turns;
    int counts;
    String username="";
    private TableLayout mainTable;
    private UpdateCardsHandler handler;

    private SharedPreferences gamePrefs;
    public static final String GAME_PREFS = "HighScoreFile";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        handler = new UpdateCardsHandler();
        loadImages();
        setContentView(R.layout.activity_home);
        gamePrefs = getSharedPreferences(GAME_PREFS, 0);

        buttonListener = new ButtonListener();

        mainTable = (TableLayout)findViewById(R.id.gameTable);


        context  = mainTable.getContext();


        newGame(4,4);


        Button high_score = (Button)findViewById(R.id.highscore);
        high_score.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        Intent i = new Intent(HomeActivity.this,HighScore.class);
                        startActivity(i);
                    }
                }
        );



    }

    private void newGame(int c, int r) {
        ROW_COUNT = r;
        COL_COUNT = c;

        cards = new int [COL_COUNT] [ROW_COUNT];
        TableRow tr = ((TableRow)findViewById(R.id.cardRow));
        tr.removeAllViews();

        mainTable = new TableLayout(context);
        tr.addView(mainTable);

        for (int y = 0; y < ROW_COUNT; y++) {
            mainTable.addView(createRow(y));
        }

        firstCard=null;
        loadCards();

        counts=0;
        turns=0;
        ((TextView)findViewById(R.id.score)).setText("Score: "+turns);


    }

    private void loadImages() {
        images = new ArrayList<Drawable>();

        images.add(getResources().getDrawable(R.drawable.colour1));
        images.add(getResources().getDrawable(R.drawable.colour2));
        images.add(getResources().getDrawable(R.drawable.colour3));
        images.add(getResources().getDrawable(R.drawable.colour4));
        images.add(getResources().getDrawable(R.drawable.colour5));
        images.add(getResources().getDrawable(R.drawable.colour6));
        images.add(getResources().getDrawable(R.drawable.colour7));
        images.add(getResources().getDrawable(R.drawable.colour8));

    }

    private void loadCards(){
        try{
            int size = ROW_COUNT*COL_COUNT;

            Log.i("loadCards()","size=" + size);

            ArrayList<Integer> list = new ArrayList<Integer>();

            for(int i=0;i<size;i++){
                list.add(new Integer(i));
            }


            Random r = new Random();

            for(int i=size-1;i>=0;i--){
                int t=0;

                if(i>0){
                    t = r.nextInt(i);
                }

                t=list.remove(t).intValue();
                cards[i%COL_COUNT][i/COL_COUNT]=t%(size/2);

                Log.i("loadCards()", "card["+(i%COL_COUNT)+
                        "]["+(i/COL_COUNT)+"]=" + cards[i%COL_COUNT][i/COL_COUNT]);
            }
        }
        catch (Exception e) {
            Log.e("loadCards()", e+"");
        }

    }

    private TableRow createRow(int y){
        TableRow row = new TableRow(context);
        row.setHorizontalGravity(Gravity.CENTER);

        for (int x = 0; x < COL_COUNT; x++) {
            row.addView(createImageButton(x,y));
        }
        return row;
    }

    private View createImageButton(int x, int y){
        Button button = new Button(context);
        button.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_bg));
        button.setId(100 * x + y);
        button.setOnClickListener(buttonListener);
        return button;
    }

    class ButtonListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            synchronized (lock) {
                if(firstCard!=null && seconedCard != null){
                    return;
                }
                int id = v.getId();
                int x = id/100;
                int y = id%100;
                turnCard((Button)v,x,y);
            }

        }

        private void turnCard(Button button,int x, int y) {
            button.setBackgroundDrawable(images.get(cards[x][y]));

            if(firstCard==null){
                firstCard = new Card(button,x,y);
            }
            else{

                if(firstCard.x == x && firstCard.y == y){

                    return; //the user pressed the same card
                }

                seconedCard = new Card(button,x,y);





                TimerTask tt = new TimerTask() {

                    @Override
                    public void run() {
                        try{
                            synchronized (lock) {
                                handler.sendEmptyMessage(0);
                            }
                        }
                        catch (Exception e) {
                            Log.e("E1", e.getMessage());
                        }
                    }
                };

                Timer t = new Timer(false);
                t.schedule(tt, 1300);
            }


        }

    }

    class UpdateCardsHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {
            synchronized (lock) {
                checkCards();
            }
        }
        public void checkCards(){

            if(cards[seconedCard.x][seconedCard.y] == cards[firstCard.x][firstCard.y]){
                firstCard.button.setVisibility(View.INVISIBLE);
                seconedCard.button.setVisibility(View.INVISIBLE);
                turns=turns+2;
                counts++;
            }
            else {
                seconedCard.button.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_bg));
                firstCard.button.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_bg));
                turns--;
            }

            if (counts == 8){
                createAlert();
            }
            firstCard=null;
            seconedCard=null;
            ((TextView)findViewById(R.id.score)).setText("Scores: " + turns);
        }
    }

    private void setHighScore(String username){
        //set high score
        int exScore = turns;
            //we have a valid score
            SharedPreferences.Editor scoreEdit = gamePrefs.edit();
            String scores = gamePrefs.getString("highScores", "");
            if(scores.length()>0) {
                //we have existing scores
                List<Score> scoreStrings = new ArrayList<Score>();
                String[] exScores = scores.split("\\|");
                for (String eSc : exScores) {
                    String[] parts = eSc.split(" - ");
                    scoreStrings.add(new Score(parts[0], Integer.parseInt(parts[1])));
                }
                Score newScore = new Score(username, exScore);
                scoreStrings.add(newScore);
                Collections.sort(scoreStrings);

                StringBuilder scoreBuild = new StringBuilder("");
                for (int s = 0; s < scoreStrings.size(); s++) {
                    if (s >= 10) break;//only want ten
                    if (s > 0) scoreBuild.append("|");//pipe separate the score strings
                    scoreBuild.append(scoreStrings.get(s).getScoreText());
                }
//write to prefs
                scoreEdit.putString("highScores", scoreBuild.toString());
                scoreEdit.commit();
            } else{
                //no existing scores
                scoreEdit.putString("highScores", ""+username+" - "+exScore);
                scoreEdit.commit();
            }
        }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
//save state
        int exScore = turns;
        savedInstanceState.putInt("score", exScore);
        savedInstanceState.putString("username", username);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void createAlert(){
        AlertDialog.Builder endGame = new AlertDialog.Builder(context);
        endGame.setTitle(R.string.alert_title);
        endGame.setMessage(R.string.instruction_alert);
        final EditText input = new EditText(getApplicationContext());
        input.setTextColor(Color.BLACK);
        endGame.setView(input);
        endGame.setNegativeButton("Enter",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        username = input.getText().toString();
                        String score = String.valueOf(turns);
                        if (username.matches("")){
                            Toast.makeText(getApplicationContext(),"Should input username", Toast.LENGTH_LONG).show();
                            createAlert(); // restart
                        } else{
                            setHighScore(username);
                            Intent i = new Intent(HomeActivity.this, HighScore.class);
                            i.putExtra("username", username);
                            i.putExtra("score", score);
                            startActivity(i);
                            dialog.dismiss();
                        }

                    }
                });
        AlertDialog alert = endGame.create();
        alert.show();
    }

}