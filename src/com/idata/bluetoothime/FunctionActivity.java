package com.idata.bluetoothime;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FunctionActivity extends Activity {
	private ListView listView;
	private String[] GENRES = {"ÌõÂëÅäÖÃ", "·¢ËÍÃüÁîÅäÖÃÉ¨ÃèÆ÷", "¶ÁÈ¡É¨ÃèÆ÷ĞÅÏ¢", "É¨ÃèÆ÷¹¦ÄÜ"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_function);
		
		listView = (ListView) findViewById(R.id.listView);
		listView.setAdapter(new ArrayAdapter<String>(this,
	              android.R.layout.simple_list_item_single_choice, GENRES));
	}

}
