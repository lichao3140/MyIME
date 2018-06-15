package com.idata.bluetoothime;

import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @description:此类为扫描和连接蓝牙设备弹出框 扫描和连接蓝牙设备
 */
@SuppressWarnings("deprecation")
public class DevicesActivity extends Activity {
	private final String TAG = "DevicesActivity";
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDevicesAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.devices);
		setResult(Activity.RESULT_CANCELED);

		mPairedDevicesAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		// 已经绑定的设备
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		// 获取默认的蓝牙adapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		// 获取当前可用的蓝牙设置
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

		if (pairedDevices.size() > 0) {
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				mPairedDevicesAdapter.add(device.getName() + "\n"
						+ device.getAddress());
			}
		} else {
			String noDevices = getResources().getText(R.string.none_paired).toString();
			mPairedDevicesAdapter.add(noDevices);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}
	}
	
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			mBtAdapter.cancelDiscovery();
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
			setResult(Activity.RESULT_OK, intent);
			intent.setClass(DevicesActivity.this, IDataSettingActivity.class);
			startActivity(intent);
			finish();
		}
	};

}
