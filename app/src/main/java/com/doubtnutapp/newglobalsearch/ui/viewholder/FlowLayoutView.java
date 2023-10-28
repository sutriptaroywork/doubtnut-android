package com.doubtnutapp.newglobalsearch.ui.viewholder;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilterItem;

import java.util.ArrayList;

public class FlowLayoutView extends ViewGroup {
    public FlowLayoutView(Context context) {
        this(context, null);
    }

    public FlowLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addItems(ArrayList<SearchFilterItem> itemList){
        for (SearchFilterItem item : itemList){

        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
