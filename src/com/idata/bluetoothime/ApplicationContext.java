package com.idata.bluetoothime;

import android.app.Application;

public class ApplicationContext extends Application {

	public static ApplicationContext instance;
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = ApplicationContext.this;
	}
	
	/**
	 * »ñÈ¡µ¥Àý
	 * 
	 * @return
	 */
	public static ApplicationContext getInstance() {

		return instance;
	}
}
