package io.virtualapp.widgets;

import io.virtualapp.VApp;

/**
 * @author Lody
 */
public class ViewHelper {

    public static int dip2px(float dpValue) {
        final float scale = VApp.getApp().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
