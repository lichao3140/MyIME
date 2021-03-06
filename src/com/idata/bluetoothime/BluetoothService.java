package com.idata.bluetoothime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 描述：蓝牙服务核心类
 */
@SuppressLint("Instantiatable")
public class BluetoothService {
	private final static String TAG = "BluetoothService";
	private static final boolean D = true;
	// 测试数据
	private static final String NAME = "BluetoothChat";
	// 自己的输入法
	private static final String INPUT_MOTHOD = "com.idata.bluetoothime/.PinyinIME";

	// 将蓝牙模拟成串口的服务，声明一个唯一的UUID
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private static BluetoothAdapter mAdapter;
	private final Handler mHandler;
	private AcceptThread mAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private WatchServerSocketThread wsst;
	private int mState;
	PinyinIME ss = new PinyinIME();

	static BluetoothService bluetoothService;
	// 常量显示当前的连接状态
	public static final int STATE_NONE = 0;
	public static final int STATE_LISTEN = 1;
	public static final int STATE_CONNECTING = 2;
	public static final int STATE_CONNECTED = 3;

	private int conn_status = 0;
	private boolean BleIsOKFlag = false;
	private boolean ServerSocketIsClose = false;
	private int Conn_Error_Num = 0;
	private int Error_Num = 0;
	private int Num = 30; // 因为每30分钟检测一次，2次就是1分钟
	private String remote_ble_address = null; // 用于存储已连接蓝牙的地址

	public BluetoothService(Context context, Handler handler) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mHandler = handler;
	}

	/**
	 * 设置当前蓝牙的连接状态
	 * 
	 * @param state
	 *            连接状态
	 */
	private synchronized void setState(int state) {
		mState = state;
		// 通知Activity更新UI
		mHandler.obtainMessage(ConnectActivity.MESSAGE_STATE_CHANGE, state, -1)
				.sendToTarget();
	}

	/**
	 * 返回当前连接状态
	 */
	public synchronized int getState() {
		return mState;
	}

	/**
	 * 开始聊天服务
	 */
	public void startChat() {
		if (D)
			Log.e(TAG, "start 聊天");

		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}

		setState(STATE_LISTEN);
	}

	/**
	 * 连接远程设备
	 * 
	 * @param device
	 *            连接蓝牙
	 */
	public synchronized void connect(BluetoothDevice device) {
		if (D)
			Log.d(TAG, "connect to: " + device);
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		wsst = new WatchServerSocketThread();
		wsst.start();
		setState(STATE_CONNECTING);
	}

	/**
	 * 启动ConnectedThread开始管理一个蓝牙连接
	 */
	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {
		Log.e(TAG, "确认蓝牙配对 ");
		remote_ble_address = device.getAddress();
		// Cancel any thread attempting to make a connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}

		// Start the thread to connect with the given device
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();
		BleIsOKFlag = true;
		// 连接成功，通知activity
		Message msg = mHandler
				.obtainMessage(ConnectActivity.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(ConnectActivity.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);
		setState(STATE_CONNECTED);
	}

	/**
	 * 停止所有线程
	 */
	public synchronized void stop() {
		if (D)
			Log.e(TAG, "---stop()");
		setState(STATE_NONE);
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}
	}

	public static void discoverDevice() {
		// 如果正在扫描，先停止扫描，再重新扫描
		if (mAdapter.isDiscovering()) {
			mAdapter.cancelDiscovery();
		}
		mAdapter.startDiscovery();
	}

	// 得到配对的设备列表，清除已配对的设备
	public static void removePairDevice() {
		if (mAdapter != null) {
			Set<BluetoothDevice> bondedDevices = mAdapter.getBondedDevices();
			for (BluetoothDevice device : bondedDevices) {
				unpairDevice(device);
			}
		}
	}

	// 反射来调用BluetoothDevice.removeBond取消设备的配对
	private static void unpairDevice(BluetoothDevice device) {
		try {
			Method m = device.getClass()
					.getMethod("removeBond", (Class[]) null);
			m.invoke(device, (Object[]) null);
		} catch (Exception e) {
			Log.e(TAG, "unpairDevice" + e.getMessage());
		}
	}

	/**
	 * 发送消息
	 * 
	 * @param message
	 */
	public void sendMessage(String message) {
		if (message.length() > 0) {
			byte[] send = message.getBytes();
			write(send);
		}
	}

	/**
	 * 以非同步方式写入ConnectedThread
	 * 
	 * @param out
	 */
	public void write(byte[] out) {
		ConnectedThread r;
		synchronized (this) {
			if (mState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		r.write(out);
	}

	/**
	 * 无法连接，通知Activity
	 */
	private void connectionFailed() {
		setState(STATE_LISTEN);
		Message msg = mHandler.obtainMessage(ConnectActivity.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(ConnectActivity.TOAST, "无法连接设备");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * 设备断开连接，通知Activity
	 */
	private void connectionLost() {
		ServerSocketIsClose = true;
		Message msg = mHandler.obtainMessage(ConnectActivity.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(ConnectActivity.TOAST, "设备断开连接");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * 监听传入的连接
	 */
	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mmServerSocket;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			try {
				tmp = mAdapter
						.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
			} catch (IOException e) {
				Log.e(TAG, "--获取socket失败:" + e);
			}
			mmServerSocket = tmp;
		}

		public void run() {
			setName("AcceptThread");
			BluetoothSocket socket = null;
			while (mState != STATE_CONNECTED) {
				Log.e(TAG, "----accept-循环执行中-");
				try {
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, "accept() 失败" + e);
					break;
				}

				// 如果连接被接受
				if (socket != null) {
					synchronized (BluetoothService.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// 开始连接线程
							connected(socket, socket.getRemoteDevice());
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// 没有准备好或已经连接
							try {
								socket.close();
							} catch (IOException e) {
								Log.e(TAG, "不能关闭这些连接" + e);
							}
							break;
						}
					}
				}
			}
			Log.e(TAG, "结束mAcceptThread");
		}

		public void cancel() {
			Log.e(TAG, "取消 " + this);
			try {
				mmServerSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "关闭失败" + e);
			}
		}
	}

	/**
	 * @description:蓝牙连接线程
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			mmDevice = device;
			BluetoothSocket tmp = null;
			try {
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				Log.e(TAG, "socket获取失败：" + e);
			}
			mmSocket = tmp;
		}

		public void run() {
			Log.e(TAG, "开始mConnectThread");
			setName("ConnectThread");
			// mAdapter.cancelDiscovery();
			try {
				mmSocket.connect();
			} catch (IOException e) {
				// 连接失败，更新ui
				connectionFailed();
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG, "关闭连接失败" + e2);
				}
				Log.e(TAG, "关闭连接==" + e);
				// 开启聊天接收线程
				startChat();
				return;
			}

			synchronized (BluetoothService.this) {
				mConnectThread = null;
			}
			connected(mmSocket, mmDevice);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "关闭连接失败" + e);
			}
		}
	}

	/**
	 * 已经连接成功后的线程 处理所有传入和传出的传输
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
		private boolean iSExit = false;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			iSExit = false;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			// 得到BluetoothSocket输入和输出流
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created" + e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			int bytes;
			String im = android.provider.Settings.Secure.getString(
					ApplicationContext.getInstance().getContentResolver(),
					android.provider.Settings.Secure.DEFAULT_INPUT_METHOD);
			// 循环监听消息
			while (true) {
				try {
					byte[] buffer = new byte[1024];
					// 此方法分段传输，快速扫描有bug
					// if (mmInStream.available() >0 == false) {
					// continue;
					// } else {
					// Thread.sleep(500);
					// }
					// bytes = mmInStream.read(buffer);

					// 此方法蓝牙枪要设置结尾添加回车换行
					int len = 0;
					while (!iSExit) {
						bytes = mmInStream.read(buffer, len, 1);
						if (buffer[len] == 0xA) {
							break;
						}
						len++;
					}
					String readStr = new String(buffer, 0, len) + "\r\n";// 字节数组直接转换成字符串
					String str = bytes2HexString(buffer).replaceAll("00", "").trim();

					Log.e("lichao", "BluetoothChatService->readStr=" + readStr);

					if (len > 0) {// 将读取到的消息发到主线程
						mHandler.obtainMessage(ConnectActivity.MESSAGE_READ,
								len, -1, buffer).sendToTarget();
						if (im.equals(INPUT_MOTHOD)) {
							ss.pinyinIME.SetText(readStr);
						}
					} else {
						Log.e(TAG, "disconnected");
						connectionLost();
						if (mState != STATE_NONE) {
							Log.e(TAG, "disconnected");
							startChat();
						}
						break;
					}
				} catch (Exception e) {
					Log.e(TAG, "disconnected" + e);
					connectionLost();
					if (mState != STATE_NONE) {
						// 在重新启动监听模式启动该服务
						startChat();
					}
					break;
				}
			}
		}

		/**
		 * 写入OutStream连接
		 * 
		 * @param buffer
		 *            要写的字节
		 */
		public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);

				// 把消息传给UI
				mHandler.obtainMessage(ConnectActivity.MESSAGE_WRITE, -1, -1,
						buffer).sendToTarget();
			} catch (IOException e) {
				Log.e(TAG, "Exception during write:" + e);
			}
		}

		public void cancel() {
			try {
				iSExit = true;
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed:" + e);
			}
		}
	}

	/**
	 * 从字节数组到十六进制字符串转换
	 */
	public static String bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}

	/**
	 * 16进制直接转换成为字符串(无需Unicode解码)
	 * 
	 * @param hexStr
	 * @return
	 */
	public static String hexStr2Str(String hexStr) {
		String str = "0123456789ABCDEF";
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int n;
		for (int i = 0; i < bytes.length; i++) {
			n = str.indexOf(hexs[2 * i]) * 16;
			n += str.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (n & 0xff);
		}
		return new String(bytes);
	}

	/**
	 * 监听服务器socket线程——检测对方设备是否主动断开蓝牙连接
	 * 
	 * @author DELL
	 * 
	 */
	class WatchServerSocketThread extends Thread {
		private BluetoothSocket mmSocket;
		private BluetoothDevice mmDevice;

		@Override
		public void run() {
			while (true) {
				switch (conn_status) {
				case 0: // 检测
					if (BleIsOKFlag && ServerSocketIsClose) {
						// 已经确认是连接断开
						ServerSocketIsClose = false;
						Log.e("lichao", "===未连接");
						conn_status = 1;
					}
					break;

				case 1: // 重连
					// 建立客户端的socket
					try {
						mmDevice = mAdapter.getRemoteDevice(remote_ble_address);
						mmSocket = mmDevice
								.createRfcommSocketToServiceRecord(MY_UUID);
						mmSocket.connect();
					} catch (IOException e) {
						Error_Num++;
						if (Error_Num > Num) {
							Error_Num = 0;
							Conn_Error_Num++;
							// Log.i("lichao", "连接错误次数:" + Conn_Error_Num);
						}
						e.printStackTrace();
						// 注意注意[既然没连接成功，没必要执行下面的代码了]
						continue;
					}

					Error_Num = 0;
					startChat();
					connected(mmSocket, mmDevice);
					Log.e("lichao", "===已连接");
					Conn_Error_Num = 0;
					// 再次检测
					conn_status = 0;
					break;

				default: // 默认
					System.out.print("nothing to do...");
					break;
				}
			}
		}
	}

}
