package com.zy.translate;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.zy.translate.wxapi.WXEntryActivity;

import java.io.DataOutputStream;

public class MainActivity extends Activity {

    static boolean sWaitForAccessibility = false;
    Button btn, btn2;
    View ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager wm = this.getWindowManager();
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        AppConstants.width = point.x;
        AppConstants.height = point.y;

//        upgradeRootPermission(getPackageCodePath());
        SPHelper.init(getApplicationContext());
        SPHelper.setStarted(false);
        setContentView(R.layout.activity_main);
        ll = findViewById(R.id.ll);
        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sWaitForAccessibility = true;
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });
        btn2 = (Button) findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(ListenerService.getInstance()==null) {
                    sWaitForAccessibility = true;
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                }else{
                    SPHelper.setStarted(true);
                    OverlayWindow.show(getApplicationContext());
                    startApplication(MainActivity.this, AppConstants.WECHAT_PACKAGE_NAME, Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_NEW_TASK);
                }
//                new Thread(){
//                    @Override
//                    public void run() {
//                        ShellUtils.execCommand("input tap "+AppConstants.width/2+" "+(int)(390*getResources().getDisplayMetrics().density), false);
//                    }
//                }.start();
            }
        });
        ll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("null", ""+event.getRawY());
                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ListenerService.getInstance()==null){
            btn.setText("开启辅助功能");
            btn.setEnabled(true);
        }else{
            btn.setText("辅助功能已开启");
            btn.setEnabled(false);
        }
    }

    public static boolean startApplication(Context context, String pkgName, int flags) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.addFlags(0x10000);
        for (ResolveInfo rInfo : context.getPackageManager()
                .queryIntentActivities(intent, 0)) {
            if (rInfo.activityInfo.packageName.equals(pkgName)) {
                Intent launchIntent = new Intent("android.intent.action.MAIN");
                launchIntent.addCategory("android.intent.category.LAUNCHER");
                launchIntent.setPackage(pkgName);
                launchIntent.setComponent(new ComponentName(
                        rInfo.activityInfo.packageName,
                        rInfo.activityInfo.name));
                launchIntent.setFlags(flags);
                context.startActivity(launchIntent);
                return true;
            }
        }
        return false;
    }

    public static boolean upgradeRootPermission(String pkgCodePath) {
        ShellUtils.checkRootPermission();
        return true;
    }
}
