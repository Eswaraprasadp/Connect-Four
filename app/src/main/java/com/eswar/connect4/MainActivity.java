package com.eswar.connect4;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
//    DisplayMetrics metrics;
//    private int height, width;
//    public final static int WC = LinearLayout.LayoutParams.WRAP_CONTENT, MP = LinearLayout.LayoutParams.MATCH_PARENT;
    private BoardView boardView;

//    private final String tag = "tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        metrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        height = metrics.heightPixels;
//        width = metrics.widthPixels;

//        final LinearLayout mainLayout = findViewById(R.id.ll_test_layout);
//        final LinearLayout undoRedoLayout = findViewById(R.id.undo_redo_layout);
//        final LinearLayout boardLayout = findViewById(R.id.board_layout);
//        final RelativeLayout resultLayout = findViewById(R.id.result_dimensions_layout);
        boardView = findViewById(R.id.board_view);

        final Button undo = (Button)findViewById(R.id.btn_undo);
        final Button restart = (Button)findViewById(R.id.btn_redo);

        final CheckBox checkBox = findViewById(R.id.check_box);
        final NumberPicker rowsPicker = findViewById(R.id.rows_picker);
        final NumberPicker colsPicker = findViewById(R.id.cols_picker);

        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d(tag, "Undo button pressed in MainActivity");
                boardView.undo();

            }
        });
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boardView.start();
            }
        });


        boardView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                Log.d("TAG","Board View's specs changed to width = " + (right - left) + ", height = " + (bottom - top));
                boardView.changeDimen();
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boardView.computerPlaying(isChecked);
            }
        });

        rowsPicker.setMaxValue(10);
        rowsPicker.setMinValue(4);
        colsPicker.setMaxValue(10);
        colsPicker.setMinValue(4);

        rowsPicker.setValue(6);
        colsPicker.setValue(7);

        setDividerColor(rowsPicker, getResources().getColor(R.color.white));
        setDividerColor(colsPicker, getResources().getColor(R.color.white));

        NumberPicker.OnValueChangeListener dimensionChangeListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                boardView.setGrids(rowsPicker.getValue(), colsPicker.getValue());
            }
        };

        rowsPicker.setOnValueChangedListener(dimensionChangeListener);
        colsPicker.setOnValueChangedListener(dimensionChangeListener);

    }
    private void setDividerColor(NumberPicker picker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        boardView.destroyMediaResources();
    }
}
