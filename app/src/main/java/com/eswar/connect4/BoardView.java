package com.eswar.connect4;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Toast;

public class BoardView extends View {
    private Context context;

    private int rows = 6, cols = 7, min, cut, tot, diff, col, row, savedRow = NONE, savedCol = NONE, aiRow = NONE, aiCol = NONE;
    private float x, y;
    public int[][] grids, savedIndices = new int[4][2];
    public boolean turnA = true, gameOver = false, singlePayer = true, waitFlag = false, started = false;
    public int result;
    public final static int EMPTY = 0, PLAYER_A = 1, PLAYER_B = 5, NONE = -1;
    public final static int A_WIN = 10, B_WIN = -10, DRAW = 1, NO_RESULT = 0;
    private Paint aPaint = new Paint(), bPaint = new Paint(), nonFilledPaint = new Paint(), winPaint = new Paint();
    private int winStrokeWidth = 10;
    private float diameter, padding = 10.0f, extPadding = 40.0f, winStrokeDiameter, winStrokeDiameterRatio = 0.7f;
    private boolean portrait;
    public final static String tag = "TAG";
    private MediaPlayer pop, gameOverSound;
    private ValueAnimator animator;
    public int width, height;
    private AI ai;

    public BoardView(Context context){
        super(context);
        init(context);
    }
    public BoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context){
        this.context = context;
        aPaint.setColor(getResources().getColor(R.color.blue));
        bPaint.setColor(getResources().getColor(R.color.red));
        nonFilledPaint.setColor(getResources().getColor(R.color.transparent_gray));
        winPaint.setColor(getResources().getColor(R.color.black));
        winPaint.setStyle(Paint.Style.STROKE);
        winPaint.setStrokeWidth(winStrokeWidth);
        pop = createMediaPlayer(R.raw.pop_x);
        gameOverSound = createMediaPlayer(R.raw.game_over);
        setGrids(rows, cols);
    }
    public void setGrids(int rows, int cols){
        this.rows = rows;
        this.cols = cols;
        min = min(rows, cols);
        diff = diff(rows, cols);
        cut = min + diff - 1;
        tot = 2 * min + diff - 1;
        changeDimen();
    }
    public void changeDimen(){
        height = getHeight() - (int)extPadding;
        width = getWidth() - (int)extPadding;

        if(height >= width){
            portrait = true;
            diameter = toFloat(width/cols) - toFloat(padding) ;
        }
        else{
            portrait = false;
            diameter = toFloat(height/rows) - toFloat(padding);
        }
        winStrokeDiameter = diameter * winStrokeDiameterRatio;

        start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!waitFlag){
            float x = event.getX();
            float y = event.getY();

            if(event.getAction() == MotionEvent.ACTION_DOWN){
                handleTouch(x, y);
                performClick();
            }
        }
        return super.onTouchEvent(event);
    }

    public void handleTouch(float x, float y){
        if((x < getGridLeft(0)) || (x > getGridLeft(cols)) || (y < getGridTop(-1)) || (y > getGridTop(rows))){
            return;
        }

        if(gameOver) return;

        col = (int)((x - extPadding)*cols/width);
//        Log.d(tag, "Touched at column number: " + (col + 1));

        row = findLowestRow(col);
        if(row < 0) return;

        if(!started){ started = true; }

        animator = ValueAnimator.ofFloat(getGridTop(0), getY(row));
        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                BoardView.this.y = (float)animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                grids[row][col] = value(turnA);
                if(!singlePayer || turnA) {
                    savedRow = row;
                    savedCol = col;
                }
                else if(singlePayer && !turnA){
                    aiRow = row;
                    aiCol = col;
                }

                result = findResult();

                if(waitFlag) {
                    if (gameOver) {
                        String resultString = resultString(result);
                        Log.d(tag, resultString);
                        Toast.makeText(context, resultString, Toast.LENGTH_SHORT).show();
                        playMedia(gameOverSound);
                        started = false;
                    } else {
                        playMedia(pop);
                    }

                    waitFlag = false;

                    if(singlePayer && turnA && !gameOver){
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MotionEvent aiTouch = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_DOWN, getX(ai.getMove(grids)), getY(rows/2), 0);
                                onTouchEvent(aiTouch);
                            }
                        }, 100);
                    }
                    turnA = !turnA;
                    invalidate();
                }
            }
        });
        animator.start();
        waitFlag = true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void start(){
        grids = new int[rows][cols];
        for (int row = 0; row < rows; ++row){
            for(int col = 0; col < cols; ++col){
                grids[row][col] = EMPTY;
            }
        }
        for (int i = 0; i < 4; ++i){
            savedIndices[i][0] = NONE;
            savedIndices[i][1] = NONE;
        }
        gameOver = false;
        turnA = true;
        waitFlag = false;
        result = NO_RESULT;
        savedRow = NONE;
        savedCol = NONE;
        ai = new AI(rows, cols);

        invalidate();
    }

    public void computerPlaying(boolean check){
        singlePayer = check;
        start();
    }

    public void undo(){
//        if(waitFlag){
//            waitFlag = false;
//            animator.end();
//        }
        if(!waitFlag){
            grids[savedRow][savedCol] = EMPTY;
            turnA = !turnA;
        }
        if(!turnA && singlePayer && !waitFlag){
            Log.d(tag, "Undo pressed");
            grids[aiRow][aiCol] = EMPTY;
            turnA = !turnA;
        }
        if(!waitFlag && gameOver){
            result = NO_RESULT;
            gameOver = false;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int row = rows - 1; row >= 0; --row){
            for (int col = 0; col < cols; ++col){
                if(grids[row][col] == PLAYER_A) {
                    canvas.drawCircle(getX(col), getY(row), diameter / 2, aPaint);
                }
                else if(grids[row][col] == PLAYER_B){
                    canvas.drawCircle(getX(col), getY(row), diameter/2, bPaint);
                }
                else{
                    canvas.drawCircle(getX(col), getY(row), diameter/2, nonFilledPaint);
                }
            }
        }
        if(waitFlag && started){
            if(turnA){
                canvas.drawCircle(getX(col), y, diameter/2, aPaint);
            }
            else{
                canvas.drawCircle(getX(col), y, diameter/2, bPaint);
            }
        }
        if(gameOver && !waitFlag){
            for (int i = 0; i < 4; ++i){
                canvas.drawCircle(getX(savedIndices[i][1]), getY(savedIndices[i][0]), (winStrokeDiameter)/2, winPaint);
            }
        }
    }
    public void destroyMediaResources(){
        destroyMediaPlayer(pop);
        destroyMediaPlayer(gameOverSound);
    }
    public void destroyMediaPlayer(MediaPlayer mediaPlayer){
        try {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
        catch (IllegalStateException ise){
            Log.d(tag, "IllegalStateException in destroyMediaPlayer");
//            ise.printStackTrace();
        }
        catch (Exception e){
            Log.d(tag, "Exception in destroyMediaPlayer");
//            e.printStackTrace();
        }
    }
    public void playMedia(MediaPlayer mediaPlayer){
        try {
            mediaPlayer.start();
        }
        catch (IllegalStateException ise){
            Log.d(tag, "IllegalStateException in playMedia");
//            ise.printStackTrace();
        }
        catch (Exception e){
            Log.d(tag, "Exception in playMedia");
//            e.printStackTrace();
        }
    }

    public float getX(int col){
        return (float)((width * 1.0f + extPadding/1.0f - cols * (diameter + padding))/2.0f + (diameter + padding)/2.0f + (diameter + padding) * col * 1.0f);
    }
    public float getY(int row){
        return (float) ((height * 1.0f + extPadding/1.0f - rows * (diameter + padding))/2.0f + (diameter + padding)/2.0f + (diameter + padding) * row * 1.0f);
    }
    public float getGridLeft(int col){
        return (float)(getX(col) - (diameter + padding)/2.0f);
    }
    public float getGridTop(int row){
        return (float)(getY(row) - (diameter + padding)/2.0f);
    }

    public int findResult(){

        //Horizontal
        for (int row = rows - 1; row >= 0; --row){
            int matches = 0;
            for (int col = 0; col < cols - 1; ++col){
                int current = grids[row][col];
                int next = grids[row][col + 1];

                if((current == next) && (current != EMPTY)){ ++matches; }
                else{ matches = 0; }

                if(matches >= 3){
                    gameOver = true;
                    for (int k = 0; k < 4; ++k){
                        savedIndices[k][0] = row;
                        savedIndices[k][1] = col - k + 1;
                    }
                    return result(current);
                }
            }
        }

        //Vertical
        for (int col = 0; col < cols; ++ col){
            int matches = 0;
            for (int row = 0; row < rows - 1; ++row){
                int current = grids[row][col];
                int next = grids[row + 1][col];

                if((current == next) && (current != EMPTY)){ ++matches; }
                else{ matches = 0; }

                if(matches >= 3){
                    gameOver = true;
                    for (int k = 0; k < 4; ++k){
                        savedIndices[k][0] = row - k + 1;
                        savedIndices[k][1] = col;
                    }
                    return result(current);
                }
            }
        }

        //Ascendant Diagonal
        for(int i = tot - 1; i >= 0; --i){
            int matches = 0;
            int startRow = ascStart(i)[0];
            int startCol = ascStart(i)[1];
            for (int j = 0; j < asc(i) - 1; ++j){
                int current = grids[startRow - j][startCol + j];
                int next =  grids[startRow - j - 1][startCol + j + 1];

                if((current == next) && (current != EMPTY)){ ++matches; }
                else { matches = 0; }

                if(matches >= 3){
                    gameOver = true;
                    for (int k = 0; k < 4; ++k){
                        savedIndices[k][0] = startRow - j + k - 1;
                        savedIndices[k][1] = startCol + j - k + 1;
                    }
                    return result(current);
                }
            }
        }

        //Descendant Diagonal
        for (int i = 0; i < tot; ++i){
            int matches = 0;
            int startRow = descStart(i)[0];
            int startCol = descStart(i)[1];
            for (int j = 0; j < asc(i) - 1; ++j){
                int current = grids[startRow + j][startCol + j];
                int next = grids[startRow + j + 1][startCol + j + 1];

                if((current == next) && (current != EMPTY)){ ++matches; }
                else{ matches = 0; }

                if(matches >= 3){
                    gameOver = true;
                    for (int k = 0; k < 4; ++k){
                        savedIndices[k][0] = startRow + j - k + 1;
                        savedIndices[k][1] = startCol + j - k + 1;
                    }
                    return result(current);
                }
            }
        }

        //Draw
        for (int row = 0; row < rows; ++row){
            for (int col = 0; col < cols; ++col){
                if(grids[row][col] == EMPTY)
                    return NO_RESULT;
            }
        }
        gameOver = true;
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
        return (grid == PLAYER_B) ? B_WIN : A_WIN;
    }

    public String resultString(int result){
        if(!singlePayer) {
            switch (result) {
                case A_WIN: return "Player A Wins!";
                case B_WIN: return "Player B Wins!";
                case DRAW: return "Draw!";
                default: return "No result";
            }
        }
        else{
            switch (result){
                case A_WIN: return "You Won!";
                case B_WIN: return "You lost!";
                case DRAW: return "Draw!";
                default: return "No result";
            }
        }
    }

    public int findLowestRow(int col){
        int row = rows - 1;
        for (row = rows - 1; row >= 0; --row){
            if(grids[row][col] == EMPTY)
                return row;
        }
        return row;
    }
    public int value(boolean turnA){
        return (turnA ? PLAYER_A : PLAYER_B);
    }

    public float toFloat(int i){
        return i * 1.0f;
    }
    public float toFloat(float f){
        return f * 1.0f;
    }

    public int min(int a, int b){
        return (a <= b) ? a : b;
    }
    public int diff(int a, int b){
        return (((a - b) >= 0) ? (a - b) : (b - a));
    }

    private MediaPlayer createMediaPlayer(int resource){
          MediaPlayer mediaPlayer = MediaPlayer.create(context, resource);
          mediaPlayer.setLooping(false);
          return mediaPlayer;
    }

}
