package com.zy.translate;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RemoteViews;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ListenerService extends AccessibilityService {

	private static ListenerService sInstance;
	private ClipboardManager clip;
	private String mCurrentActivity = "";
	private String mPackageName;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {

			}
		}
	};

	@SuppressLint("NewApi")
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (event == null || event.getPackageName() == null) {
			return;
		}
		if (event.getPackageName() != null) {
			mPackageName = event.getPackageName().toString();
		}
		boolean isWeixin = AppConstants.WECHAT_PACKAGE_NAME.equals(mPackageName);
		if(isWeixin) {
			switch (event.getEventType()) {
				case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
					mCurrentActivity = event.getClassName().toString();
					break;
				case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
					Notification notification = (Notification) event.getParcelableData();
					if (notification == null) {
						return;
					}

					String texts[] = getContentText(notification);
					String contentText = texts[0];
					String title = texts[1];
					if (contentText == null || title == null || notification.tickerText == null) {
						return;
					}
					if ("小号".equals(title)) {//test
						try {
							notification.contentIntent.send();
						} catch (PendingIntent.CanceledException e) {
							e.printStackTrace();
						}
					}
					break;
			}
		}
	}

	public static ArrayList<AccessibilityNodeInfo> findNodeByClass(AccessibilityNodeInfo nodeInfo, String className) {
		if (nodeInfo == null) {
			return null;
		}
		ArrayList<AccessibilityNodeInfo> result = new ArrayList<>();
		for (int i = 0; i < nodeInfo.getChildCount(); i++) {
			AccessibilityNodeInfo child = nodeInfo.getChild(i);
			if (child == null) {
				continue;
			}
			if (className.equals(child.getClassName())) {
				result.add(child);
			}

			if (child.getChildCount() > 0) {
				result.addAll(findNodeByClass(child, className));
			}
		}
		return result;
	}

	public static AccessibilityNodeInfo findNodeByClass(AccessibilityNodeInfo nodeInfo, String className, String untilClass) {
		if (nodeInfo == null) {
			return null;
		}
		for (int i = 0; i < nodeInfo.getChildCount(); i++) {
			AccessibilityNodeInfo child = nodeInfo.getChild(i);
			if (child == null) {
				continue;
			}
			if (className.equals(child.getClassName())) {
				return child;
			} else if (child.getChildCount() > 0 && (untilClass == null || !untilClass.equals(child.getClassName()))) {
				AccessibilityNodeInfo node = findNodeByClass(child, className, untilClass);
				if (node != null) {
					child.recycle();
					return node;
				}
			}
			child.recycle();
		}
		return null;
	}

	public static ArrayList<AccessibilityNodeInfo> findNodeByClass(AccessibilityNodeInfo nodeInfo, String className, boolean recycleSource) {
		if (nodeInfo == null) {
			return null;
		}
		ArrayList<AccessibilityNodeInfo> result = new ArrayList<>();
		for (int i = 0; i < nodeInfo.getChildCount(); i++) {
			AccessibilityNodeInfo child = nodeInfo.getChild(i);
			if (child == null) {
				continue;
			}
			boolean recycle = true;
			if (className.equals(child.getClassName())) {
				result.add(child);
				recycle = false;
			}

			if (child.getChildCount() > 0) {
				result.addAll(findNodeByClass(child, className, true));
				recycle = false;
			}
			if (recycle)
				child.recycle();
		}
		if (recycleSource && !result.contains(nodeInfo)) {
			nodeInfo.recycle();
		}
		return result;
	}

	public static AccessibilityNodeInfo findNodeByText2(AccessibilityNodeInfo nodeInfo, String textToFind, String className, boolean isContentDesc) {
		if (nodeInfo == null) {
			return null;
		}
		for (int i = 0; i < nodeInfo.getChildCount(); i++) {
			AccessibilityNodeInfo child = nodeInfo.getChild(i);
			if (child == null) {
				continue;
			}
			CharSequence text;
			if (isContentDesc) {
				text = child.getContentDescription();
			} else {
				text = child.getText();
			}
			if (text != null && text.toString().equals(textToFind)) {
				if (className == null || (className != null && className.equals(child.getClassName()))) {
					return child;
				}
			}

			if (child.getChildCount() > 0) {
				AccessibilityNodeInfo node = findNodeByText2(child, textToFind, className, isContentDesc);
				if (node != null) {
					child.recycle();
					return node;
				}
			}
			child.recycle();
		}
		return null;
	}

	public static AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo nodeInfo, String textToFind, String className, int action, boolean isContentDesc) {
		if (nodeInfo == null) {
			return null;
		}
		List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText(textToFind);
		for (int i = 0; i < nodes.size(); i++) {
			AccessibilityNodeInfo node = nodes.get(i);
			if (node == null) {
				continue;
			}
			String text = null;
			if (isContentDesc) {
				if (node.getContentDescription() == null) {
					node.recycle();
					continue;
				}
				text = node.getContentDescription().toString();
			} else {
				if (node.getText() == null) {
					node.recycle();
					continue;
				}
				text = node.getText().toString();
			}
			if (textToFind.equals(text)) {
				if (action == AccessibilityNodeInfo.ACTION_CLICK) {
					if (!node.isClickable()) {
						node.recycle();
						continue;
					}
				} else if (action == AccessibilityNodeInfo.ACTION_LONG_CLICK) {
					if (!node.isLongClickable()) {
						node.recycle();
						continue;
					}
				}
				if (className != null) {
					CharSequence nodeClass = node.getClassName();
					if (nodeClass == null) {
						node.recycle();
						continue;
					}
					if (!className.equals(nodeClass.toString())) {
						node.recycle();
						continue;
					}
				}
				return node;
			}
		}
		return null;
	}

	@Override
	public void onInterrupt() {
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onKeyEvent(KeyEvent event) {
		Log.e("", "" + event.getKeyCode());
		return false;
	}

	private boolean setText(AccessibilityNodeInfo nodeInfo, String text) {
		CharSequence oldText = nodeInfo.getText();
		if (oldText != null && text.equals(oldText.toString())) {
			return false;
		}
		AccessibilityNodeInfoCompat nodeInfoCompat = new AccessibilityNodeInfoCompat(nodeInfo);
		if (Build.VERSION.SDK_INT >= 21) {
			Bundle arguments = new Bundle();
			arguments.putCharSequence(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
			nodeInfoCompat.performAction(AccessibilityNodeInfoCompat.ACTION_SET_TEXT, arguments);
		} else if (Build.VERSION.SDK_INT >= 18) {
			nodeInfoCompat.performAction(AccessibilityNodeInfoCompat.ACTION_FOCUS);
			Bundle arguments = new Bundle();
			int len = 0;
			if (oldText != null)
				len = oldText.length();
			if (len > 0) {
				arguments.putInt(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_SELECTION_START_INT, 0);
				arguments.putInt(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_SELECTION_END_INT, len);
			}
			nodeInfoCompat.performAction(AccessibilityNodeInfoCompat.ACTION_SET_SELECTION, arguments);
			ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData oldClipText = clip.getPrimaryClip();
			clip.setPrimaryClip(ClipData.newPlainText(null, text));
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			nodeInfoCompat.performAction(AccessibilityNodeInfo.ACTION_PASTE);
			if (oldClipText != null)
				clip.setPrimaryClip(oldClipText);
		}
		return true;
	}

	@SuppressLint("NewApi")
	private static String[] getContentText(Notification notification) {

		if (Build.VERSION.SDK_INT >= 19) {
			Bundle extras = notification.extras;
			if (extras == null) {
				return null;
			}
			String contentText = extras.getString(Notification.EXTRA_TEXT);
			String title = extras.getString(Notification.EXTRA_TITLE);
			return new String[] { contentText, title };
		} else {
			RemoteViews remoteViews = notification.contentView;

			Object value = null;
			Integer type = null;
			Integer viewId = null;
			Map<Integer, String> text = new HashMap<Integer, String>();

			Class<? extends RemoteViews> remoteViewsCls = remoteViews.getClass();
			try {
				Field actionsField = remoteViewsCls.getDeclaredField("mActions");
				actionsField.setAccessible(true);
				ArrayList<Object> actions;
				actions = (ArrayList<Object>) actionsField.get(remoteViews);
				// Log.e("NULL", "actions:"+actions.size());
				for (Object action : actions) {
					Log.i("NULL", "action instanceof " + action.getClass().getSimpleName());
					try {
						Field valueField = action.getClass().getDeclaredField("value");
						valueField.setAccessible(true);
						value = valueField.get(action);
						// Log.d("NULL","value = "+value);
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						Field typeField = action.getClass().getDeclaredField("type");
						typeField.setAccessible(true);
						type = typeField.getInt(action);
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						Field viewIdField = action.getClass().getDeclaredField("viewId");
						viewIdField.setAccessible(true);
						viewId = viewIdField.getInt(action);
					} catch (NoSuchFieldException e) {
						try {
							Field idField = action.getClass().getSuperclass().getDeclaredField("viewId");
							idField.setAccessible(true);
							viewId = idField.getInt(action);
							// Log.d("NULL","viewId = "+viewId);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (type != null && (type == 9 || type == 10)) {
						text.put(viewId, value.toString());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			String title = null;
			String contentText = null;
			if (text.containsKey(16908358)) {
				title = text.get(16908310);
			}
			contentText = notification.tickerText.toString();
			if (TextUtils.isEmpty(contentText)) {
				if (text.containsKey(201458741)) {
					contentText = text.get(201458741);
				} else if (text.size() > 0) {
					Iterator<Map.Entry<Integer, String>> iterator = text.entrySet().iterator();
					StringBuffer sb = new StringBuffer();
					while (iterator.hasNext()) {
						Map.Entry<Integer, String> entry = iterator.next();
						sb.append(entry.getValue());
						sb.append(" || ");
					}
					contentText = sb.toString();
				}
			}
			return new String[] { contentText, title };
		}
	}

	@Override
	protected void onServiceConnected() {
		clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		sInstance = this;
		super.onServiceConnected();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		sInstance = null;
		return super.onUnbind(intent);
	}

	public static boolean isScreenOn(Context context) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return pm.isScreenOn();
	}

	public static ListenerService getInstance(){
		return sInstance;
	}
	
}
