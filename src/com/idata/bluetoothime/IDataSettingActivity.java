package com.idata.bluetoothime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class IDataSettingActivity extends Activity implements OnClickListener {
	static final String TAG = IDataSettingActivity.class.getSimpleName();
	
	private TextView tv_Title;
	private Button bt_Activation, bt_Open, bt_Select, bt_Change, bt_Test, bt_Back;
	
	private InputMethodManager mImm;
	private boolean mNeedsToAdjustStepNumberToSystemState;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_idata);
		
		mImm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		
		init();
	}

	private void init() {
		tv_Title = (TextView) findViewById(R.id.tvTitle);
		tv_Title.setText(R.string.setting);
		bt_Activation = (Button) findViewById(R.id.bt_activation);
		bt_Open = (Button) findViewById(R.id.bt_open);
		bt_Select = (Button) findViewById(R.id.bt_select);
		bt_Change = (Button) findViewById(R.id.bt_change);
		bt_Test = (Button) findViewById(R.id.bt_test);
		bt_Back = (Button) findViewById(R.id.bt_back);
		bt_Back.setText(R.string.ui_title_complete);
		
		bt_Activation.setOnClickListener(this);
		bt_Open.setOnClickListener(this);
		bt_Select.setOnClickListener(this);
		bt_Change.setOnClickListener(this);
		bt_Test.setOnClickListener(this);
		bt_Back.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		Intent intent = new Intent();
		switch (view.getId()) {
		case R.id.bt_activation:
			Toast.makeText(IDataSettingActivity.this, R.string.select_bluetoothIME, Toast.LENGTH_LONG).show();
			Intent input = new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS);	
			startActivity(input);
			break;
		case R.id.bt_open:
			Intent bluetooth = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
			startActivity(bluetooth);
			break;
		case R.id.bt_select:
			
			break;
		case R.id.bt_change:
			mImm.showInputMethodPicker();
			mNeedsToAdjustStepNumberToSystemState = true;
			break;
		case R.id.bt_test:
			
			break;
		case R.id.bt_back:
			finish();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && mNeedsToAdjustStepNumberToSystemState) {
            mNeedsToAdjustStepNumberToSystemState = false;
        }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
