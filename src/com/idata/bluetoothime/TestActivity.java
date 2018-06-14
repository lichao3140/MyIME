package com.idata.bluetoothime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TestActivity extends Activity implements OnClickListener{

	private static final String INPUT_MOTHOD = "com.idata.bluetoothime/.PinyinIME";
	private TextView tv_Title;
	private Button bt_Back;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		
		init();
	}

	private void init() {
		tv_Title = (TextView) findViewById(R.id.tvTitle);
		tv_Title.setText(R.string.Test);
		
		bt_Back = (Button) findViewById(R.id.bt_back);
		bt_Back.setOnClickListener(this);
		
		String im = Settings.Secure.getString(TestActivity.this.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);
		if (!im.equals(INPUT_MOTHOD)) {
			ToolsUtil.showToast("当前输入法不是蓝牙输入法");
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.bt_back:
			Intent intent =new Intent();
			intent.setClass(TestActivity.this, IDataSettingActivity.class);
			startActivity(intent);
		default:
			break;
		}
	}
	
	
}
