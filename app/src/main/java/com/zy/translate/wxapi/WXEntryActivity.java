package com.zy.translate.wxapi;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.zy.translate.AppConstants;
import com.zy.translate.R;

import java.io.ByteArrayOutputStream;


public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

	private static IWXAPI sWeiXinAPI;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
//		if(getIntent().getBooleanExtra("isShare", false)){
//			String url = getIntent().getStringExtra("url");
//			String title = getIntent().getStringExtra("title");
//			String msg = getIntent().getStringExtra("msg");
//			shareWeiXin(getApplicationContext(), 0, 0, url, title, msg);
//		}
	}
	
	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
		String result;
		
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			result = "success";
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			result = "cancel";
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			result = "deny";
			break;
		default:
			result = "unknown";
			break;
		}
		
		Toast.makeText(this, result, Toast.LENGTH_LONG).show();
	}
	
	public static void shareWeiXin(Context context, int type, int thumbId, String url, String title, String description) {
		if(checkWeiXinVersion(context)==-1){
			return;
		}
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = url;
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = title;
		msg.description = description;
		Bitmap thumb = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
		msg.thumbData = bmpToByteArray(thumb, true);

		// 构造一个Req
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis()); // transaction字段用于唯一标识一个请求
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneSession;
		
		// 调用api接口发送数据到微信
		sWeiXinAPI.sendReq(req);
	}
	
	private static int checkWeiXinVersion(Context context) {
		if(sWeiXinAPI == null)
			sWeiXinAPI = WXAPIFactory.createWXAPI(context, AppConstants.APP_ID, true);
		if (sWeiXinAPI.isWXAppInstalled()) {
			if (sWeiXinAPI.isWXAppSupportAPI()) {
				sWeiXinAPI.registerApp(AppConstants.APP_ID);
				return sWeiXinAPI.getWXAppSupportAPI();
			} else {
				Toast.makeText(context, "微信版本过低", Toast.LENGTH_SHORT).show();
			}
		}
		Toast.makeText(context, "微信未安装", Toast.LENGTH_SHORT).show();
		return -1;
	}
	
	public static byte[] bmpToByteArray(final Bitmap bmp,
			final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}

		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

}
