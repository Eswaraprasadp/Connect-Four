package com.eswar.connect4;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private LinearLayout layout;
    DisplayMetrics metrics;
    int height, width, x, y, colPx, paddingPx, col;
    Handler handler = new Handler();
    ImageView grids[][] = new ImageView[6][7];
    int filledGrids[][] = new int[6][7];
    boolean turnA = true, waitFlag = false, gameOver = false, draw = false;
    int savedIndices[][] = new int[4][2];
    private TextView resultText;
    private Button playAgain;
    private final static int EMPTY = 0, PLAYER_X = 1, PLAYER_O = 5, NONE = -1;
    private final static int A_WIN = 10, B_WIN = -10;
    private String winner;
    private int result, move = NONE;
    AI ai = new AI(6, 7);
    public final static String tag = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = (LinearLayout) findViewById(R.id.mainLayout);

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        height = metrics.heightPixels;
        width = metrics.widthPixels;
        colPx = pxFromDp(50*7);
        paddingPx = (int)(width - colPx)/2;
        resultText = (TextView)findViewById(R.id.tvResult);
        playAgain = (Button)findViewById(R.id.btnPlayAgain);
        playAgain.setOnClickListener(clickListener);
        playAgain.setEnabled(false);

        try {
            setImageResources();
        }
        catch (Exception e){
            Log.d(tag, "Error in setting Image resources");
        }
        init();
        layout.setOnTouchListener(handleTouch);

    }
    private void init(){
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 7; ++j) {
                filledGrids[i][j] = EMPTY;
            }
        }
        for (int i = 0; i < 6; ++i){
            for(int j = 0; j < 7; ++j){
                grids[i][j].setBackgroundResource(R.drawable.grid);
            }
        }
        gameOver = false;
        draw = false;
        winner = "";
        savedIndices = new int[4][2];
        resultText.setVisibility(View.INVISIBLE);
        playAgain.setVisibility(View.INVISIBLE);
        playAgain.setEnabled(false);
        waitFlag = false;
    }

    private void callAI(){
        move = ai.getMove(filledGrids);
        try {
            ballFall(move);
        }
        catch (Exception e){
            Log.d(tag, "Error in callAI()");
            e.printStackTrace();
        }
    }

    int pxFromDp(float dp) {
        return (int)(dp * metrics.densityDpi / 160f) ;
    }
    private void findCol(int x){
        if(x < paddingPx){
        }
        else if(x > colPx + paddingPx){
        }
        else{
            col = (int)((7 * (x - paddingPx))/colPx);
            try {
                ballFall(col);
            }
            catch (Exception e){
                Log.d("TAG","Error in ball fall");
            }
        }
    }
    private View.OnTouchListener handleTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            x = (int) event.getX();
            y = (int) event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
//                    Log.d("TAG", "touched down at coordinates (" + String.valueOf(x) + ", " + String.valueOf(y) + ")");
                    findCol(x);
                    break;
                case MotionEvent.ACTION_MOVE:
//                    Log.d("TAG", "moving: (" + String.valueOf(x) + ", " + String.valueOf(y) + ")");
                    break;
                case MotionEvent.ACTION_UP:
//                    Log.d("TAG", "touched up  at coordinates (" + String.valueOf(x) + ", " + String.valueOf(y) + ")");
                    break;
            }

            return true;
        }
    };
    private void ballFall(int col) throws Exception{

        if(!waitFlag && !gameOver) {
            int filledRow;
            for (filledRow = 0; filledRow < 6; ++filledRow) {
                if (filledGrids[filledRow][col] != EMPTY) {
                    break;
                }
            }
            if (filledRow > 0) {
                waitFlag = true;

                filledGrids[filledRow - 1][col] = (turnA ? PLAYER_X : PLAYER_O);
                try {
                    result = findResult(filledGrids);
                }
                catch (ArrayIndexOutOfBoundsException aiobe){
                    Log.d("TAG", "Array Of Out Of Bounds Exception in ballFall: " + aiobe);
                    aiobe.printStackTrace();
                }
                catch(Exception e){
                    Log.d("TAG", "Unknown exception in ballFall: " + e);
                    e.printStackTrace();
                }
                for (int i = 0; i < filledRow; ++i) {

                    MyRunnable runnable;
                    Thread thread;
                    int time = 0;
                    if(i >= 0){
//                        time = (int)(Math.sqrt(i/1.5)*300);
                        time = 10 * i;
                    }
                    runnable = new MyRunnable(col, i, filledRow, time);
                    thread = new Thread(runnable);
                    thread.start();
                }

            }
        }
        else{
            Log.d("TAG", "Touched on a column when a move is not completed");
        }

    }

    public class MyRunnable implements Runnable{
        private int col, row, time, filledRow, yellow;
        private Handler handler = new Handler();
        public MyRunnable(int col, int row, int filledRow, int time){
            this.col = col;
            this.row = row;
            this.time = time;
            this.filledRow = filledRow;
        }
        @Override
        public void run() {
            try {
                Thread.sleep(this.time);
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if(turnA) {
                            grids[row][col].setBackgroundResource(R.drawable.grid_yellow);
                        }
                        else{
                            grids[row][col].setBackgroundResource(R.drawable.grid_red);
                        }

                        for (int i = 0; i < filledRow; ++i) {
                            if (i != row ) {
                                grids[i][col].setBackgroundResource(R.drawable.grid);
                            }
                        }
                        if(row == filledRow - 1) {
                            waitFlag = false;
                            turnA = !turnA;

                            if(!turnA && !gameOver){
                                callAI();
                            }

                            if(gameOver){
                                waitFlag = true;
                                ending();
                            }
                        }
                    }
                });



            }
            catch (InterruptedException iex){
                Log.d("TAG", "Interrupted Exception in Thread");
                iex.printStackTrace();
            }
            catch (Exception e){
                Log.d("TAG", "Unknown exception in thread");
                e.printStackTrace();
            }
        }

    }

    private void ending(){
        resultText.setVisibility(View.VISIBLE);
        playAgain.setVisibility(View.VISIBLE);
        if(result == A_WIN){
            winner = "Player A";
        }
        else if(result == B_WIN){
            winner = "Player B";
        }

        try {
            for (int i = 0; i <= 3; ++i) {
                if (winner.equals("Player A")) {
                    grids[savedIndices[i][0]][savedIndices[i][1]].setBackgroundResource(R.drawable.grid_yellow_complete);
                }
                else if (winner.equals("Player B")) {
                    grids[savedIndices[i][0]][savedIndices[i][1]].setBackgroundResource(R.drawable.grid_red_complete);
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException aiobe){
            Log.d("TAG", "Array Out Of Bounds Exception in setting completed grids");
        }
        catch (Exception e){
            Log.d("TAG", "Exception in setting completed grids");
        }

        playAgain.setEnabled(true);
        if(!draw) {
            resultText.setText(winner + " Wins!");
        }
        else{
            resultText.setText("The game is draw!");
        }
    }

    public int findResult(int[][] boards){
        int horizontalMatches , verticalMatches, diagonalLeftMatches, diagonalRightMatches;

        //Horizontal Match
        for (int i = 5; i >= 0 && !gameOver; --i){
            horizontalMatches = 0;
            for (int j = 0; j <= 5 && !gameOver; ++j) {
                int current = boards[i][j];
                int next = boards[i][j + 1];
                if (current == next && current != EMPTY) {
                    ++horizontalMatches;
                } else {
                    horizontalMatches = 0;
                }
                if (horizontalMatches >= 3) {
                    gameOver = true;
                    result = (current == PLAYER_X) ? A_WIN : B_WIN;
                    Log.d("TAG", "Game Over! Horizontal match at row : " + String.valueOf(i+1));
                    for (int k = 0; k <= 3; ++k) {
                        savedIndices[k][0] = i;
                        savedIndices[k][1] = j - k + 1;
                    }
                    break;
                }
            }
        }

        //Vertical Match
        for (int j = 0; j <= 6 && !gameOver; ++j){
            verticalMatches = 0;
            for (int i = 0; i <= 4 && !gameOver; ++i){
                int current = boards[i][j];
                int next = boards[i + 1][j];
                if (current == next && current != EMPTY) {
                    ++verticalMatches;
                }
                else {
                    verticalMatches = 0;
                }
                if (verticalMatches >= 3) {
                    gameOver = true;
                    result = (current == PLAYER_X) ? A_WIN : B_WIN;
                    Log.d("TAG", "Game Over! Vertical match at column : " + String.valueOf(j+1));
                    for (int k = 0; k <= 3; ++k) {
                        savedIndices[k][0] = i - k + 1;
                        savedIndices[k][1] = j;
                    }
                    break;
                }
            }
        }

        //Right Diagonal
        for (int i = 3; (i <= 8)&& !gameOver; ++i){
            diagonalRightMatches = 0;
            if(i <= 5) {
                for (int j = 0; j < i; ++j) {
                    int current = boards[i - j][j];
                    int next = boards[i- j - 1][j + 1];
                    if (current == next && current != EMPTY) {
                        ++diagonalRightMatches;
                    } else {
                        diagonalRightMatches = 0;
                    }
                    if (diagonalRightMatches >= 3) {
                        gameOver = true;
                        result = (current == PLAYER_X) ? A_WIN : B_WIN;
                        Log.d("TAG", "Game Over! Right diagonal match at diagonal number : " + String.valueOf(i));
                        for (int k = 0; k <= 3; ++k) {
                            savedIndices[k][0] = i - j + k - 1;
                            savedIndices[k][1] = j - k + 1;
                        }
                        break;
                    }
                }
            }
            else {
                for(int j = 5; j >= i-5 ; --j){
                    int current = boards[j][i-j];
                    int next = boards[j-1][i-j+1];
                    if(current == next && current != EMPTY){
                        ++diagonalRightMatches;
                    }
                    else {
                        diagonalRightMatches = 0;
                    }
                    if(diagonalRightMatches >= 3){
                        gameOver = true;
                        result = (current == PLAYER_X) ? A_WIN : B_WIN;
                        Log.d("TAG", "Game Over! Right diagonal match at diagonal number : " + String.valueOf(i));

                        for (int k = 0; k <= 3; ++k) {
                            savedIndices[k][0] = j + k - 1;
                            savedIndices[k][1] = i - j - k + 1;
                        }
                        break;
                    }
                }
            }
        }

        //Left Diagonal
        for (int i = 3; i <= 8 && !gameOver; ++i){
            diagonalLeftMatches = 0;
            if(i <= 5){
                for (int j = 0; j < i; ++j){
                    int current = boards[j+5-i][j];
                    int next = boards[j+6-i][j+1];
                    if(current == next && current != EMPTY){
                        ++diagonalLeftMatches;
                    }
                    else {
                        diagonalLeftMatches = 0;
                    }
                    if (diagonalLeftMatches >= 3){
                        gameOver = true;
                        result = (current == PLAYER_X) ? A_WIN : B_WIN;
                        Log.d("TAG", "Game Over! Left diagonal match at diagonal number : " + String.valueOf(i));

                        for (int k = 0; k <= 3; ++k) {
                            savedIndices[k][0] = j - i + 5 - k + 1;
                            savedIndices[k][1] = j - k + 1;
                        }
                        break;
                    }
                }
            }
            else{
                for (int j = 5; j >= i-5; --j){
                    int current = boards[5-j][i-j];
                    int next = boards[6-j][i-j+1];
                    if(current == next && current != EMPTY){
                        ++diagonalLeftMatches;
                    }
                    else{
                        diagonalLeftMatches = 0;
                    }
                    if (diagonalLeftMatches >= 3){
                        gameOver = true;
                        result = (current == PLAYER_X) ? A_WIN : B_WIN;
                        Log.d("TAG", "Game Over! Left diagonal match at diagonal number : " + String.valueOf(i));

                        for (int k = 0; k <= 3; ++k) {
                            savedIndices[k][0] = 5 - j - k + 1;
                            savedIndices[k][1] = i - j - k + 1;
                        }

                        break;
                    }
                }
            }
        }
        if(!gameOver) {
            //Finding draw
            draw = true;
            for (int i = 0; i < 6 && draw; ++i) {
                for (int j = 0; j < 7; ++j) {
                    if (boards[i][j] == EMPTY) {
                        draw = false;
                        break;
                    }
                }
            }
            if (draw) {
                gameOver = true;
//                Log.d("TAG", "Game is draw in findResult()");
            }
        }

        if(gameOver) {
            Log.d("TAG", "Game Over set to " + String.valueOf(gameOver) + " in findResult()");
        }

        return result;
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            init();
        }
    };

    private void setImageResources() throws Exception{

        grids[0][0] = findViewById(R.id.grid11);
        grids[0][1] = findViewById(R.id.grid12);
        grids[0][2] = findViewById(R.id.grid13);
        grids[0][3] = findViewById(R.id.grid14);
        grids[0][4] = findViewById(R.id.grid15);
        grids[0][5] = findViewById(R.id.grid16);
        grids[0][6] = findViewById(R.id.grid17);

        grids[1][0] = findViewById(R.id.grid21);
        grids[1][1] = findViewById(R.id.grid22);
        grids[1][2] = findViewById(R.id.grid23);
        grids[1][3] = findViewById(R.id.grid24);
        grids[1][4] = findViewById(R.id.grid25);
        grids[1][5] = findViewById(R.id.grid26);
        grids[1][6] = findViewById(R.id.grid27);

        grids[2][0] = findViewById(R.id.grid31);
        grids[2][1] = findViewById(R.id.grid32);
        grids[2][2] = findViewById(R.id.grid33);
        grids[2][3] = findViewById(R.id.grid34);
        grids[2][4] = findViewById(R.id.grid35);
        grids[2][5] = findViewById(R.id.grid36);
        grids[2][6] = findViewById(R.id.grid37);

        grids[3][0] = findViewById(R.id.grid41);
        grids[3][1] = findViewById(R.id.grid42);
        grids[3][2] = findViewById(R.id.grid43);
        grids[3][3] = findViewById(R.id.grid44);
        grids[3][4] = findViewById(R.id.grid45);
        grids[3][5] = findViewById(R.id.grid46);
        grids[3][6] = findViewById(R.id.grid47);

        grids[4][0] = findViewById(R.id.grid51);
        grids[4][1] = findViewById(R.id.grid52);
        grids[4][2] = findViewById(R.id.grid53);
        grids[4][3] = findViewById(R.id.grid54);
        grids[4][4] = findViewById(R.id.grid55);
        grids[4][5] = findViewById(R.id.grid56);
        grids[4][6] = findViewById(R.id.grid57);

        grids[5][0] = findViewById(R.id.grid61);
        grids[5][1] = findViewById(R.id.grid62);
        grids[5][2] = findViewById(R.id.grid63);
        grids[5][3] = findViewById(R.id.grid64);
        grids[5][4] = findViewById(R.id.grid65);
        grids[5][5] = findViewById(R.id.grid66);
        grids[5][6] = findViewById(R.id.grid67);


    }
}
