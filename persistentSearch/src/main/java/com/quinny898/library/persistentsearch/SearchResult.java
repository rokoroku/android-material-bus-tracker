package com.quinny898.library.persistentsearch;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;

import java.io.Serializable;

public class SearchResult {
    public String title;
    public Drawable icon;
    public int textColor = -1;

    /**
     * Create a search result with text and an icon
     * @param title
     * @param icon
     */
    public SearchResult(String title, Drawable icon) {
       this.title = title;
       this.icon = icon;
    }
    
    public SearchResult(String title, Drawable icon, @ColorInt int textColor) {
        this(title, icon);
        this.textColor = textColor;
    }

    /**
     * Return the title of the result
     */
    @Override
    public String toString() {
        return title;
    }
    
}