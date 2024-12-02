package com.example.animewallpaper.Extra;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.style.ReplacementSpan;

public class BorderSpan extends ReplacementSpan {

    private int mBorderColor;
    private int mBorderWidth;

    // Constructor to pass border color and width
    public BorderSpan(int borderColor, int borderWidth) {
        mBorderColor = borderColor;
        mBorderWidth = borderWidth;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        // Measure the width of the text
        return Math.round(paint.measureText(text, start, end));
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        // Get the size of the text to create a border
        Rect rect = new Rect();
        paint.getTextBounds(text.toString(), start, end, rect);

        // Save the current color to restore it later
        int originalColor = paint.getColor();

        // Draw the text
        canvas.drawText(text, start, end, x, y, paint);

        // Set the color and style for the border
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(mBorderColor);
        paint.setStrokeWidth(mBorderWidth);

        // Draw a border around each character
        canvas.drawRect(x, top, x + rect.width(), bottom, paint);

        // Restore the original paint color
        paint.setColor(originalColor);
    }
}
