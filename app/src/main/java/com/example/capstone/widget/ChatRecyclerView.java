package com.example.capstone.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class ChatRecyclerView extends RecyclerView {
    private int oldHeight;
    private int fullHeight = 0;

    public ChatRecyclerView(Context context) {
        super(context);
    }

    public ChatRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public int getFullHeight() {
        return fullHeight;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (fullHeight == 0 || fullHeight < (b - t)) {
            fullHeight = b - t;
        }

        int delta = b - t - this.oldHeight;
        this.oldHeight = b - t;

        if (delta < 0) {
            if (getLayoutManager() != null && getLayoutManager() instanceof LinearLayoutManager) {
                if (!((LinearLayoutManager) getLayoutManager()).getStackFromEnd()) {
                    this.scrollBy(0, -delta);
                }
            }
        }
    }
}

