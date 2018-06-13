package com.idata.bluetoothime;

import android.app.Activity;
import android.os.Bundle;

public class ImageActivity extends Activity {
	
	private MyImageView myImageView;
	private int position;
	
	private int[] images = {R.drawable.config_enter, R.drawable.config_exit,
			  R.drawable.config_39, R.drawable.config_93};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);
		
		Bundle extras = getIntent().getExtras();
		position = extras.getInt("bar_code");
		myImageView = (MyImageView) findViewById(R.id.myv_iamge);
		myImageView.setImageResource(images[position]);
	}
}
