package com.eswar.connect4;

import android.util.Log;

import java.util.Arrays;

public class AI {
    public final static String tag = "tag";
    private final static int MAX = Integer.MAX_VALUE, MIN = Integer.MIN_VALUE;
    private int rows = 6, cols = 7, min, diff, cut, tot;
    private int savedMove;
    private int initialDepth = 0;
    public final static int EMPTY = 0, PLAYER_X = 1, PLAYER_O = 5, NONE = 0;
    public final static int NO_RESULT = 0, DRAW = 1, AI_WIN = 300, AI_LOST = -300, MATCHES = 5;

    public AI (int rows, int cols){
        this.rows = rows;
        this.cols = cols;
        min = min(this.rows, this.cols);
        diff = diff(this.rows, this.cols);
        cut = min + diff - 1;
        tot = 2 * min + diff - 1;
    }
    private int[] alphaBeta(int[][] boards, boolean turnAI, int alpha, int beta, int depth) {
        int bestMove = NONE;
        int currentScore = 0;
        int bestScore = ((turnAI) ? MIN : MAX);

        int result = evaluate(boards);

//        if(depth <= 1){
//            printDashes(depth);
//            Log.d(depthString(depth), "Entered for: ");
//            print(boards, depth);
//            printDashes(depth);
//        }

        if (((result == AI_WIN) || (result == AI_LOST) || (result == DRAW))) {

            if(result == AI_WIN){
                bestScore = result + 20 - depth;
            }
            else if(result == AI_LOST){
                bestScore = result - 20 + depth;
            }
            else{
                bestScore = result;
            }

//            Log.d(depthString(depth), "Terminal condition. Current Score = " + bestScore);

            return new int[]{bestScore, NONE};
        }
        else if (depth >= 5){
            if(result > NO_RESULT){
                bestScore = result + 20 - depth;
            }
            else if (result < NO_RESULT){
                bestScore = result - 20 + depth;
            }
            else {
                bestScore = result;
            }

            return new int[]{bestScore, NONE};
        }
        else {

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
//                if(depth <= 1){
//                    print(trialBoards, depth);
//                    Log.d(depthString(depth), "currentScore = " + currentScore + ", bestScore = " + bestScore + ", bestMove = " + bestMove);
//                }
            }
//            if(depth <= 1) {
//                Log.d(depthString(depth), "Finally bestScore = " + bestScore);
//            }

            return new int[]{bestScore, bestMove};
        }
    }

    public int getMove(int[][] boards){
        int move =  alphaBeta(copy(boards), true, MIN, MAX, 0)[1];

        Log.d(tag, "Move returned by getMove: " + move);
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

        int result = NO_RESULT;

        //Horizontal
        for (int row = rows - 1; row >= 0; --row){
            int matches = 0;
            for (int col = 0; col < cols - 1; ++col){
                int current = boards[row][col];
                int next = boards[row][col + 1];

                if((current == next) && (current != EMPTY)){ ++matches; }
                else{ matches = 0; }

                if(matches >= 3){
                    return result(current);
                }
                else if(matches > 0){
                    result += sign(current) * (MATCHES + matches);
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
                else{ matches = 0; }

                if(matches >= 3){
                    return result(current);
                }
                else if(matches > 0){
                    result += sign(current) * (MATCHES + matches);
                }
            }
        }

        //Ascendant Diagonal
        for(int i = tot - 1; i >= 0; --i){
            int matches = 0;
            int startRow = ascStart(i)[0];
            int startCol = ascStart(i)[1];
            for (int j = 0; j < asc(i) - 1; ++j){
                int current = boards[startRow - j][startCol + j];
                int next =  boards[startRow - j - 1][startCol + j + 1];

                if((current == next) && (current != EMPTY)){ ++matches; }
                else { matches = 0; }

                if(matches >= 3){
                    return result(current);
                }
                else if(matches > 0){
                    result += sign(current) * (MATCHES + matches);
                }
            }
        }

        //Descendant Diagonal
        for (int i = 0; i < tot; ++i){
            int matches = 0;
            int startRow = descStart(i)[0];
            int startCol = descStart(i)[1];
            for (int j = 0; j < asc(i) - 1; ++j){
                int current = boards[startRow + j][startCol + j];
                int next = boards[startRow + j + 1][startCol + j + 1];

                if((current == next) && (current != EMPTY)){ ++matches; }
                else{ matches = 0; }

                if(matches >= 3){
                    return result(current);
                }
                else if(matches > 0){
                    result += sign(current) * (MATCHES + matches);
                }
            }
        }

        //Draw
        for (int row = 0; row < rows; ++row){
            for (int col = 0; col < cols; ++col){
                if(boards[row][col] == EMPTY) {
                    return result;
                }
            }
        }

        return DRAW;
    }

    public int asc(int i){
        if(i < min){ return (i + 1); }
        else if(i >= min && i < cut){ return min; }
        else{ return (tot - i); }
    }

    public int[] ascStart(int i){
        if(min == rows){
            if(i < min){ return new int[]{i, 0}; }
            else{ return new int[]{ rows - 1, i - min + 1}; }
        }
        else{
            if(i > cut){ return new int[]{rows - 1, i - cut}; }
            else { return new int[]{i, 0}; }
        }
    }
    public int[] descStart(int i){
        return new int[] {rows - 1 - ascStart(i)[0], ascStart(i)[1]};
    }

    public int result(int grid){
        return (grid == PLAYER_O) ? AI_WIN : AI_LOST;
    }

    public int[][] copy(int boards[][]){
        return Arrays.copyOf(boards, rows);
    }
//    private void print(int[][] boards, int depth){
//        for (int row = 0; row < rows; ++row){
//            String str = "";
//            for (int col = 0; col < cols; ++col){
//                switch(boards[row][col]){
//                    case EMPTY: str += "_ "; break;
//                    case PLAYER_X: str += "Y "; break;
//                    case PLAYER_O: str += "R "; break;
//                }
//            }
//            Log.d(depthString(depth), str);
//        }
//        Log.d(depthString(depth), "");
//    }
//    private String depthString(int depth){
//        return String.valueOf("depth" + String.valueOf(depth));
//    }
    private int sign(int grid){
        return ((grid == PLAYER_O) ? 1 : -1);
    }
    private int min(int a, int b){ return ((a <= b) ? a : b); }
    private int diff(int a, int b){ return (((a - b) >= 0) ? (a - b) : (b - a)); }
}
