package com.zy.translate;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    static boolean sWaitForAccessibility = false;
    Button btn, btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SPHelper.init(getApplicationContext());
        SPHelper.setStarted(false);
        setContentView(R.layout.activity_main);
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
}
