package com.idata.bluetoothime;

import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @description:此类为扫描和连接蓝牙设备弹出框 扫描和连接蓝牙设备
 */
@SuppressWarnings("deprecation")
public class DeviceListActivity extends Activity {
	private final String TAG = "DeviceListActivity";
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDevicesAdapter;
	private ArrayAdapter<String> mNewDevicesAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);
		setResult(Activity.RESULT_CANCELED);
		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				discoverDevice();
			}
		});

		mPairedDevicesAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);
		mNewDevicesAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);
		// 已经绑定的设备
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		// 搜索到的可用设备
		ListView newListView = (ListView) findViewById(R.id.new_devices);
		newListView.setAdapter(mNewDevicesAdapter);
		newListView.setOnItemClickListener(mDeviceClickListener);

		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

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
		this.unregisterReceiver(mReceiver);
	}

	/**
	 * 扫描本地可用的设备
	 */
	private void discoverDevice() {
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);
		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
		//如果正在扫描，先停止扫描，再重新扫描
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}
		mBtAdapter.startDiscovery();
	}
	
	/**
	 * 监听搜索到的设备
	 */
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				Log.e(TAG, "----ACTION_FOUND---");
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					if(!TextUtils.isEmpty(device.getName())){
						if(device.getName().equals("小米手机")){
							Log.e(TAG, "----device.getName():"+device.getName());
							mNewDevicesAdapter.add(device.getName() + "\n"
									+ device.getAddress());
						}
					
					}
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				Log.e(TAG, "----ACTION_DISCOVERY_FINISHED----");
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.select_device);
				if (mNewDevicesAdapter.getCount() == 0) {
					String noDevices = getResources().getText(
							R.string.none_found_device).toString();
					mNewDevicesAdapter.add(noDevices);
				}
			}
		}
	};
	
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			mBtAdapter.cancelDiscovery();
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

}
