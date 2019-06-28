package com.eswar.connect4;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AI {
    private final String tag = "tag";
    private final static int MAX = Integer.MAX_VALUE, MIN = Integer.MIN_VALUE;
    private int rows = 6, cols = 7;
    private int initialDepth = 0;
    public final static int EMPTY = 0, PLAYER_X = 1, PLAYER_O = 5, NONE = -1;
    public final static int NO_RESULT = 0, DRAW = 1, AI_WIN = 100, AI_LOST = -100, MATCHES = 10;

    public AI (int rows, int cols){
        this.rows = rows;
        this.cols = cols;
    }
    private int[] alphaBeta(int[][] boards, boolean turnAI, int alpha, int beta, int depth) {
        int bestMove = -1;
        int currentScore = 0;
        int bestScore = ((turnAI) ? MIN : MAX);

        int result = evaluate(boards);

        if ((result != NO_RESULT) || (depth >= 5)) {

//            print(boards, depth);
            if(result == AI_WIN){
                bestScore = result + 20 - depth;
//                Log.d(depthString(depth), "AI Won. Score = " + bestScore);
            }
            else if(result == AI_LOST){
                bestScore = result - 20 - depth;
//                Log.d(depthString(depth), "AI Lost. Score = " + bestScore);
            }
            else{
                bestScore = result;
//                Log.d(depthString(depth), "AI Draw. Score = " + bestScore);
            }

            return new int[]{bestScore, -1};
        } else {

            for (int col = 0; col < cols; ++col) {

                int trialBoards[][] = copy(boards);
                int lowestRow = findLowestRow(boards, col);
                if (lowestRow > 0) {

                    if (turnAI) {
                        trialBoards[lowestRow][col] = PLAYER_O;
                        currentScore = alphaBeta(trialBoards, false, alpha, beta, depth + 1)[0];
                    } else {
                        trialBoards[lowestRow][col] = PLAYER_X;
                        currentScore = alphaBeta(trialBoards, true, alpha, beta, depth + 1)[0];
                    }

                    if (turnAI) {
                        if (currentScore > bestScore) {

                            bestScore = currentScore;
                            bestMove = col;

                        }
                        if (bestScore > alpha) {
                            alpha = bestScore;
                        }
                    } else {
                        if (currentScore < bestScore) {
                            bestScore = currentScore;
                            bestMove = col;
                        }
                        if (bestScore < beta) {
                            beta = bestScore;
                        }
                    }
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            if(depth <= 3){
                print(boards, depth);
                Log.d(depthString(depth), "bestScore = " + bestScore + ", bestMove = " + bestMove);
            }
            return new int[]{bestScore, bestMove};
        }
    }

    public int getMove(int[][] boards){
        int move =  alphaBeta(copy(boards), true, MIN, MAX, 0)[1];
        Log.d(tag, "Move by AI: " + move);
        return move;
    }

    public int findLowestRow(int[][] boards, int col){
        int lowestRow;
        for (lowestRow = rows - 1; lowestRow >= 0; --lowestRow) {
            if (boards[lowestRow][col] == EMPTY) {
                break;
            }
        }
        return lowestRow;
    }
    private boolean validColumn(int[][] boards, int col){
        return (findLowestRow(boards, col) > 0);
    }

    public int evaluate(int[][] boards){
        boolean gameOver = false, draw = false;
        int result = NO_RESULT;

        //Horizontal Match
        for (int i = 5; i >= 0 && !gameOver; --i){
            int horizontal = 0;
            for (int j = 0; j <= 5 && !gameOver; ++j) {
                int current = boards[i][j];
                int next = boards[i][j + 1];

                if ((current == next) && (current !=  EMPTY)) {
                    ++horizontal;
                } else {
                    horizontal = 0;
                }

                if (horizontal >= 3) {
                    gameOver = true;
                    result = (current == PLAYER_O) ? AI_WIN : AI_LOST;
                    break;
                }
            }
        }

        //Vertical Match
        for (int j = 0; j <= 6 && !gameOver; ++j){
            int vertical = 0;
            for (int i = 0; i <= 4 && !gameOver; ++i){
                int current = boards[i][j];
                int next = boards[i + 1][j];
                if ((current == next) && (current != EMPTY)) {
                    ++vertical;
                } else{
                    vertical = 0;
                }
                if (vertical >= 3) {
                    gameOver = true;
                    result = (current == PLAYER_O) ? AI_WIN : AI_LOST;
                    break;
                }
            }
        }

        //Right Diagonal
        for (int i = 3; (i <= 8)&& !gameOver; ++i){
            int diagonalRight = 0;
            if(i <= 5) {
                for (int j = 0; j < i; ++j) {

                    int current = boards[i - j][j];
                    int next = boards[i - j - 1][j + 1];

                    if ((current == next) && (current != EMPTY)) {
                        ++diagonalRight;
                    } else {
                        diagonalRight = 0;
                    }

                    if (diagonalRight >= 3) {
                        gameOver = true;
                        result = (current == PLAYER_O) ? AI_WIN : AI_LOST;
                        break;
                    }
                }
            }
            else {
                for(int j = 5; j >= i-5 ; --j){
                    int current = boards[j][i-j];
                    int next = boards[j-1][i-j+1];
                    if((current == next) && (current != EMPTY)){
                        ++diagonalRight;
                    } else {
                        diagonalRight = 0;
                    }

                    if(diagonalRight >= 3){
                        gameOver = true;
                        result = (current == PLAYER_O) ? AI_WIN : AI_LOST;
                        break;
                    }
                }
            }
        }

        //Left Diagonal
        for (int i = 3; i <= 8 && !gameOver; ++i){
            int diagonalLeft = 0;
            if(i <= 5){
                for (int j = 0; j < i; ++j){
                    int current = boards[j+5-i][j];
                    int next = boards[j+6-i][j+1];
                    if((current == next) && (current != EMPTY)){
                        ++diagonalLeft;
                    } else {
                        diagonalLeft = 0;
                        diagonalLeft = 0;
                    }

                    if (diagonalLeft >= 3){
                        gameOver = true;
                        result = (current == PLAYER_O) ? AI_WIN : AI_LOST;
                        break;
                    }
                }
            }
            else{
                for (int j = 5; j >= i-5; --j){
                    int current = boards[5-j][i-j];
                    int next = boards[6-j][i-j+1];
                    int first = NONE;
                    int prev = NONE;
                    if((current == next) && (current != EMPTY)){
                        ++diagonalLeft;
                    } else {
                        diagonalLeft = 0;
                    }

                    if (diagonalLeft >= 3){
                        gameOver = true;
                        result = (current == PLAYER_O) ? AI_WIN : AI_LOST;
                        break;
                    }
                }
            }
        }
        if(!gameOver) {
            //Finding draw
            draw = true;
            for (int i = 0; i < rows && draw; ++i) {
                for (int j = 0; j < cols; ++j) {
                    if (boards[i][j] == EMPTY) {
                        draw = false;
                        break;
                    }
                }
            }
        }

        return result;
    }

    public int[][] copy(int boards[][]){
        int newBoard[][] = new int[rows][cols];
        for (int row = 0; row < rows; ++row)
            for (int col = 0; col < cols; ++col)
                newBoard[row][col] = boards[row][col];

        return newBoard;
    }
    private void print(int[][] boards, int depth){
        for (int row = 0; row < rows; ++row){
            String str = "";
            for (int col = 0; col < cols; ++col){
                switch(boards[row][col]){
                    case EMPTY: str += "_ "; break;
                    case PLAYER_X: str += "Y "; break;
                    case PLAYER_O: str += "R "; break;
                }
            }
            Log.d(depthString(depth), str);
        }
        Log.d(depthString(depth), "");
    }
    private String depthString(int depth){
        return String.valueOf("depth" + String.valueOf(depth));
    }
}
