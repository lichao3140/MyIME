package com.idata.bluetoothime;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.widget.Toast;

public class ToolsUtil {
	private static Dialog singleDialog = null;
	private static Toast toast;

	/**
	 * 弹出toast提示 防止覆盖
	 * 
	 * @param msg
	 */
	public static void showToast(String msg) {

		if (toast == null) {
			toast = Toast.makeText(ApplicationContext.getInstance(), msg + "", Toast.LENGTH_SHORT);
		} else {
			toast.setText(msg);
		}
		toast.show();
	}

	/**
	 * yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String getTime() {
		Date nowdate = new Date(); // 当前时间
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(nowdate);
	}

	// 获取版本号
	public static String getVersionName() {

		try {
			PackageManager pm = ApplicationContext.getInstance().getPackageManager();
			String versionName = "";
			try {
				PackageInfo packageInfo = pm.getPackageInfo(
						ApplicationContext.getInstance().getPackageName(), 0);
				versionName = packageInfo.versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			return versionName;
		} catch (Exception e) {
			return "1.0.0";
		}
	}

	public static abstract class CallBack {
		public abstract void callback(int pos);
	}

	/**
	 * 弹出信息，需要手动关
	 * 
	 * @param context
	 * @param strMsg
	 */
	public static void showDialog(final Context context, final String strMsg, String btnMsg, final CallBack callBack) {

		Activity mActivity = (Activity) context;
		if (mActivity.isFinishing()) {
			Log.v("lichao", "当前activity界面已关闭，不能显示对话?");
			return;
		}

		if(singleDialog != null){
			singleDialog.dismiss();
			singleDialog = null;
		}

		singleDialog = new AlertDialog.Builder(context)
		.setTitle("提示")
		.setMessage(strMsg + "")
		.setPositiveButton(btnMsg + "", new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
				if(callBack != null){
					callBack.callback(0);
				}
			}
		}).show();
		
	}

	/**
	 * 弹出确认取消框
	 * @param context
	 * @param strMsg
	 */
	static Builder dialog;
	public static void showChooseDialog(final Context context, final String strMsg, final CallBack callback) {

		Activity mActivity = (Activity) context;
		if (mActivity.isFinishing()) {
			return;
		}

		dialog = new AlertDialog.Builder(context);

		dialog.setTitle("提示");
		dialog.setMessage(strMsg + "");
		dialog.setPositiveButton("确认", new DialogInterface.OnClickListener(){  

			public void onClick(DialogInterface dialoginterface, int i){   

				callback.callback(0);
			}   
		});
		dialog.setNegativeButton("取消", new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				callback.callback(-1);
			}
		});

		dialog.show();
	}

	/**
	 * 获取当前上下文
	 * @return
	 */
	public static Activity getGlobalActivity(){

		Class<?> activityThreadClass;
		try {
			activityThreadClass = Class.forName("android.app.ActivityThread");
			Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
			Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
			activitiesField.setAccessible(true);
			Map<?, ?> activities = (Map<?, ?>) activitiesField.get(activityThread);
			for(Object activityRecord:activities.values()){
				Class<? extends Object> activityRecordClass = activityRecord.getClass();
				Field pausedField = activityRecordClass.getDeclaredField("paused");
				pausedField.setAccessible(true);
				if(!pausedField.getBoolean(activityRecord)) {
					Field activityField = activityRecordClass.getDeclaredField("activity");
					activityField.setAccessible(true);
					Activity activity = (Activity) activityField.get(activityRecord);
					return activity;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
