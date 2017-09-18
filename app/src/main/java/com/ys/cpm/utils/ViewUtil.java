package com.ys.cpm.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by Ys on 16/12/29.
 * View批量处理
 */

public class ViewUtil { //todo kotlin转换

    public static void setOnClickListener(View view, View.OnClickListener listener, int... ids) {
        if (view == null || listener == null || ids.length < 1) return;
        for (int id : ids)
            view.findViewById(id).setOnClickListener(listener);
    }

    public static void setOnClickListener(Activity activity, View.OnClickListener listener, int... ids) {
        if (activity == null || listener == null || ids.length < 1) return;
        for (int id : ids)
            activity.findViewById(id).setOnClickListener(listener);
    }

    public static void setOnClickListener(View view, View.OnClickListener listener, View... views) {
        if (view == null || listener == null || views.length < 1) return;
        for (View v : views)
            v.setOnClickListener(listener);
    }

    public static void setOnClickListener(Activity activity, View.OnClickListener listener, View... views) {
        if (activity == null || listener == null || views.length < 1) return;
        for (View v : views)
            v.setOnClickListener(listener);
    }

    public static void setListViewHeightByChildren(ListView listView) {
        ListAdapter adapter = listView.getAdapter();
        if (adapter == null) return;

        int totalH = 0;
        for (int i = 0, c = adapter.getCount(); i < c; i++) {
            View item = adapter.getView(i, null, listView);
            item.measure(0, 0);
            totalH += item.getMeasuredHeight();
        }
        ViewGroup.LayoutParams lp = listView.getLayoutParams();
        lp.height = totalH + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(lp);
    }
}
