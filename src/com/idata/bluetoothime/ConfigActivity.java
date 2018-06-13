package com.idata.bluetoothime;

import java.util.ArrayList;
import java.util.List;

import com.idata.bluetoothime.ConfigAdapter.InnerItemOnclickListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint("ViewHolder") 
public class ConfigActivity extends Activity implements InnerItemOnclickListener,
		OnItemClickListener {
	
	private ListView lv_Config;
	private ConfigAdapter mAdapter;
	private List<Integer> iamgeDataList;
	private List<String> textDataList;
    private int[] images = {R.drawable.config_enter, R.drawable.config_exit,
    						  R.drawable.config_39, R.drawable.config_93};
    
    private static final String[] infos = {"进入设置模式", "退出设置模式", "显示版本信息", "恢复默认设置"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);
		
		lv_Config = (ListView) findViewById(R.id.lv_config);
		
		textDataList = new ArrayList<String>();
		iamgeDataList = new ArrayList<Integer>();
		for (int i = 0; i < infos.length; i++) {  
			textDataList.add(infos[i]);
			iamgeDataList.add(images[i]);
        }
		
		
		mAdapter = new ConfigAdapter(textDataList, iamgeDataList, this);
		mAdapter.setOnInnerItemOnClickListener(this);
		lv_Config.setAdapter(mAdapter);
		lv_Config.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.e("lichao", position + "=list");  
		Intent intent = new Intent();
		intent.setClass(ConfigActivity.this, ImageActivity.class);
		startActivity(intent);
	}

	@Override
	public void itemClick(View v) {
//		int position;
//        position = (Integer) v.getTag();
//        switch (v.getId()) {
//        case R.id.bt1:  
//            Log.e("内部item--1-->", position + "");
//            break;
//        case R.id.bt2:
//            Log.e("内部item--2-->", position + "");
//            break;
//        default:
//            break;
//        }
	}

}
