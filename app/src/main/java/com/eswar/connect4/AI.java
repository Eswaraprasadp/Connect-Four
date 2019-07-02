package com.eswar.connect4;

import android.util.Log;

import java.util.Arrays;

public class AI {
    private final String tag = "tag";
    private final int MAX = Integer.MAX_VALUE, MIN = Integer.MIN_VALUE;
    private int rows = 6, cols = 7, min, diff, cut, tot;
    private final int EMPTY = 0, PLAYER_X = 1, PLAYER_O = 5, NONE = 0;
    private final int NO_RESULT = 0, AI_WIN = 2, AI_LOST = -2, DRAW = 1;
    private int[][] ascArray, descArray;
    private int[][] trialBoards;
    private int moveNumber;

    public AI (int rows, int cols){
        this.rows = rows;
        this.cols = cols;
        min = Math.min(this.rows, this.cols);
        diff = Math.abs(this.rows - this.cols);
        cut = min + diff - 1;
        tot = 2 * min + diff - 1;

        ascArray = new int[tot][3];
        descArray = new int[tot][2];

        for (int i = 0; i < tot; ++i){
            ascArray[i][0] = ascStart(i)[0];
            ascArray[i][1] = ascStart(i)[1];
            ascArray[i][2] = asc(i);

            descArray[i][0] = descStart(i)[0];
            descArray[i][1] = descStart(i)[1];
        }
    }
    private int[] alphaBeta(int[][] boards, boolean turnAI, int alpha, int beta, int depth) {
        int bestMove = NONE;
        int currentScore = 0;
        int bestScore = ((turnAI) ? MIN : MAX);
        final int THRESHOLD_RANDOM = 13;

        int result = evaluate(boards)[0];

//        if(depth <= 1){
//            printDashes(depth);
//            Log.d(depthString(depth), "Entered for: ");
//            print(boards, depth);
//            printDashes(depth);
//        }

        if (result != NO_RESULT) {

            final int BONUS = 300,  PENALTY = 25;

            currentScore = evaluate(boards)[1];
            if(result == AI_WIN){
                currentScore += BONUS - depth * PENALTY;
            }
            else if(result == AI_LOST){
                currentScore += -BONUS + depth * PENALTY;
            }
            else{
                currentScore += BONUS/2 ;
            }

//            if(depth <= 1) { Log.d(depthString(depth), "Terminal condition. Current Score = " + currentScore); }

            return new int[]{currentScore, NONE};
        }

        else if (depth >= 5){

//            currentScore = evaluate(boards)[1];

//            if(result > NO_RESULT){
//                currentScore = result + 20 - depth;
//            }
//            else if (result < NO_RESULT){
//                currentScore = result - 20 + depth;
//            }
//            else {
//                currentScore = result;
//            }

            return new int[]{evaluate(boards)[1], NONE};
        }
        else {

            for (int col = cols/2; col >= 0 && col < cols; ) {

                int lowestRow = findLowestRow(boards, col);

                if (lowestRow >= 0) {

                    if (turnAI) {
                        boards[lowestRow][col] = PLAYER_O;
                        currentScore = alphaBeta(boards, false, alpha, beta, depth + 1)[0];
                    } else {
                        boards[lowestRow][col] = PLAYER_X;
                        currentScore = alphaBeta(boards, true, alpha, beta, depth + 1)[0];
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
//                    if(depth <= 1){
//                        print(boards, depth);
//                        Log.d(depthString(depth), "currentScore = " + currentScore + ", bestScore = " + bestScore + ", bestMove = " + bestMove);
//                    }
                    boards[lowestRow][col] = EMPTY;
                    if (alpha >= beta) {
                        break;
                    }

                }

                if(col <= cols/2) {
                    if(col == 0){ col = cols/2 + 1; }
                    else{ --col; }
                }
                else{
                    ++col;
                }
            }
//            if(depth <= 1) {
//                Log.d(depthString(depth), "Finally bestScore = " + bestScore + ", bestMove = " + bestMove);
//            }

            return new int[]{bestScore, bestMove};
        }
    }

    public int getMove(int[][] boards){
        trialBoards = copy(boards);
        return alphaBeta(trialBoards, true, MIN, MAX, 0)[1];
    }


    private int findLowestRow(int[][] boards, int col){
        int lowestRow;
        for (lowestRow = rows - 1; lowestRow >= 0; --lowestRow) {
            if (boards[lowestRow][col] == EMPTY) {
                return lowestRow;
            }
        }
        return lowestRow;
    }

    private int[] evaluate(int[][] boards){

        final int MATCHES_SCORE = 3, MATCHES_MULTIPLIER = 4;

        int score = 0;
        int result = NO_RESULT;

        //Horizontal
        for (int row = rows - 1; row >= 0; --row){
            int matches = 0;
            for (int col = 0; col < cols - 1; ++col){
                int current = boards[row][col];
                int next = boards[row][col + 1];

                if((current == next) && (current != EMPTY)){ ++matches; }
                else{ matches = 0; continue; }

                if(matches >= 3){
                    result = result(current);
                    score += getScore(current);
                    score += sign(current) * (MATCHES_SCORE + matches * MATCHES_MULTIPLIER);
                }
                else if(matches > 0){
                    score += sign(current) * (MATCHES_SCORE + matches * MATCHES_MULTIPLIER);
                }
            }
        }

        //Vertical
        for (int col = 0; col < cols; ++ col){
            int matches = 0;
            for (int row = 0; row < rows - 1; ++row){
                int current = boards[row][col];
                int next = boards[row + 1][col];

                if((current == next) && (current != EMPTY)){ ++matches; }
                else{ matches = 0; continue; }

                if(matches >= 3){
                    result = result(current);
                    score += getScore(current);
                    score += sign(current) * (MATCHES_SCORE + matches * MATCHES_MULTIPLIER);
                }
                else if(matches > 0){
                    score += sign(current) * (MATCHES_SCORE + matches * MATCHES_MULTIPLIER);
                }
            }
        }

        //Ascendant Diagonal
        for(int i = tot - 2; i >= 1; --i){
            int matches = 0;
            int startRow = ascArray[i][0];
            int startCol = ascArray[i][1];
            for (int j = 0; j < ascArray[i][2] - 1; ++j){
                int current = boards[startRow - j][startCol + j];
                int next =  boards[startRow - j - 1][startCol + j + 1];

                if((current == next) && (current != EMPTY)){ ++matches; }
                else { matches = 0; continue; }

                if(matches >= 3){
                    result = result(current);
                    score += getScore(current);
                    score += sign(current) * (MATCHES_SCORE + matches * MATCHES_MULTIPLIER);
                }
                else if(matches > 0){
                    score += sign(current) * (MATCHES_SCORE + matches * MATCHES_MULTIPLIER);
                }
            }
        }

        //Descendant Diagonal
        for (int i = 1; i < tot - 1; ++i){
            int matches = 0;
            int startRow = descArray[i][0];
            int startCol = descArray[i][1];
            for (int j = 0; j < ascArray[i][2] - 1; ++j){
                int current = boards[startRow + j][startCol + j];
                int next = boards[startRow + j + 1][startCol + j + 1];

                if((current == next) && (current != EMPTY)){ ++matches; }
                else{ matches = 0; continue; }

                if(matches >= 3){
                    result = result(current);
                    score += getScore(current);
                    score += sign(current) * (MATCHES_SCORE + matches * MATCHES_MULTIPLIER);
                }
                else if(matches > 0){
                    score += sign(current) * (MATCHES_SCORE + matches * MATCHES_MULTIPLIER);
                }
            }
        }

        //Draw
        for (int row = 0; row < rows; ++row){
            for (int col = 0; col < cols; ++col){
                if(boards[row][col] == EMPTY) {
                    return new int[] {result, score};
                }
            }
        }

        result = DRAW;
//        score += DRAW_SCORE;

        return new int[] {result, score};
    }

    private int asc(int i){
        if(i < min){ return (i + 1); }
        else if(i >= min && i < cut){ return min; }
        else{ return (tot - i); }
    }

    private int[] ascStart(int i){
        if(rows <= cols){
            if(i < min){ return new int[]{i, 0}; }
            else{ return new int[]{ rows - 1, i - min + 1}; }
        }
        else{
            if(i > cut){ return new int[]{rows - 1, i - cut}; }
            else { return new int[]{i, 0}; }
        }
    }
    private int[] descStart(int i){
        return new int[] {rows - 1 - ascStart(i)[0], ascStart(i)[1]};
    }

    public int result(int grid){
        return (grid == PLAYER_O) ? AI_WIN : AI_LOST;
    }

    private int getScore(int grid){
        final int AI_WIN_SCORE = 1000, AI_LOST_SCORE = -1000;
        return ((grid == PLAYER_O) ? AI_WIN_SCORE : AI_LOST_SCORE);
    }

    private int[][] copy(int boards[][]){
        int[][] newBoards = new int[rows][cols];
        for (int i = 0; i < rows; ++i){
            newBoards[i] = Arrays.copyOf(boards[i], cols);
        }
        return newBoards;
    }
    private void print(int[][] boards, int depth){
        for (int row = 0; row < rows; ++row){
            String str = "";
            for (int col = 0; col < cols; ++col){
                switch(boards[row][col]){
                    case EMPTY: str += "_ "; break;
                    case PLAYER_X: str += "R "; break;
                    case PLAYER_O: str += "Y "; break;
                }
            }
            Log.d(depthString(depth), str);
        }
//        Log.d(depthString(depth), "");
    }
    private String depthString(int depth){
        return String.valueOf("depth" + String.valueOf(depth));
    }
    private int sign(int grid){
        return ((grid == PLAYER_O) ? 1 : -1);
    }
    private void printDashes(int depth){ Log.d(depthString(depth), "____________"); }
    private void printFinalDashes(int depth) { Log.d(depthString(depth), "____________ Move Over ____________"); }
}
