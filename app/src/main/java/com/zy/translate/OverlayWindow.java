package com.zy.translate;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by Yong on 2016/3/31.
 */
public class OverlayWindow {

    private static WindowManager.LayoutParams sWindowParams;
    private static WindowManager sWindowManager;
    private static View sView;
    private static TextView tvClose;
    private static boolean isShown = false;

    public static void init(final Context context) {
        sWindowManager = (WindowManager) context.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);

        sWindowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                2002,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON +
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON +
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE +
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                ,
                PixelFormat.TRANSLUCENT);
        sWindowParams.width = 200;
        sWindowParams.height = 200;
        sWindowParams.gravity = Gravity.TOP | Gravity.RIGHT;
        sView = LayoutInflater.from(context).inflate(R.layout.window_overlay, null);

        tvClose = (TextView) sView.findViewById(R.id.tv_close);
        isShown = false;
        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPHelper.setStarted(false);
                dismiss();
            }
        });
    }

    public static void show(Context context){
        if (sWindowManager == null) {
            init(context);
        }
        try {
            sWindowManager.addView(sView, sWindowParams);
            isShown = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dismiss() {
        try {
            sWindowManager.removeView(sView);
            isShown = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isShown(){
        return isShown;
    }

//    public static void setTouchable(boolean touchable){
//        if(touchable) {
//            sWindowParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON +
//                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON +
//                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        }else{
//            sWindowParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON +
//                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON +
//                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE +
//                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
//        }
//        sWindowManager.updateViewLayout(sView, sWindowParams);
//    }
}
