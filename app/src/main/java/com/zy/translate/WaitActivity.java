package com.zy.translate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class WaitActivity extends Activity {

	public static final String EXTRA_TIME = "time";
	public static final String EXTRA_INTENT = "intent";
	long time;
	Intent mIntent;
	
	private static WaitActivity sInstance = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sInstance = this;
		View view = findViewById(android.R.id.content);
		time = getIntent().getLongExtra(EXTRA_TIME, -1);
		mIntent = getIntent().getParcelableExtra(EXTRA_INTENT);
		if(mIntent != null){
			startActivity(mIntent);
			finish();
			return;
		}
		if(time > 0){
			view.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					finish();
				}
			}, time);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}
	
	@Override
	protected void onDestroy() {
		sInstance = null;
		super.onDestroy();
	}
	
	public static WaitActivity getInstance(){
		return sInstance;
	}
}
