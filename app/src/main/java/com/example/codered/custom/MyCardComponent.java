package com.example.codered.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;

import com.example.codered.R;

public class MyCardComponent extends CardView {
    private final Paint paint;
    private Canvas canvas;
    private int centerX, centerY;
    private final RectF rectangle;

    public MyCardComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(R.drawable.custom_card_background);

        paint = new Paint();
        rectangle = new RectF();
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;

        setCoordinates();

        drawYellowArc();
        drawBlueArc();
        drawWhiteCircle();
        drawWhiteArc1();
    }

    private void drawWhiteArc1() {
        int l = 390;
        int b = 275;
        int left = centerX - 275 + l;
        int top = centerY - 275 + b;
        int right = centerX + 175 + l;
        int bottom = centerY + 175 + b;

        paint.reset();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(110);
        paint.setColor(getResources().getColor(R.color.white40));

        canvas.drawArc(left, top, right, bottom, 90, 270, false, paint);
    }
    private void drawWhiteCircle() {
        int l = 340, b = 225;
        int left = centerX - 25+l;
        int top =  centerY +b-20;
        int right = centerX + 25+l;
        int bottom = centerY+b+30;

        rectangle.set(left, top, right, bottom);

        paint.setARGB(25, 255, 255, 255);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRoundRect(rectangle, 200, 200, paint);
    }
    private void drawYellowArc() {
        int l = -420;
        int b = -300;
        int left = centerX - 300 + l;
        int top = centerY - 300 + b;
        int right = centerX + 200 + l;
        int bottom = centerY + 200 + b;

        paint.reset();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(100);
        paint.setColor(Color.YELLOW);

        canvas.drawArc(left, top, right, bottom, 0, 90, false, paint);
    }
    private void drawBlueArc() {
        int l = -500;
        int b = -350;
        int left = centerX - 450 + l + 30;
        int top = centerY - 450 + b;
        int right = centerX + 450 + l - 69;
        int bottom = centerY + 450 + b;

        paint.reset();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(100);
        paint.setARGB(1000, 135, 206, 235);

        canvas.drawArc(left, top, right, bottom, -10, 120, false, paint);
    }
    private void setCoordinates() {
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
    }
}
