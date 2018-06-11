package com.idata.bluetoothime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
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
public class BluetoothService {
	private final String TAG = "BluetoothService";
	// 测试数据
	private static final String NAME = "BluetoothChat";

	// 声明一个唯一的UUID
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private final BluetoothAdapter mAdapter;
	private final Handler mHandler;
	private AcceptThread mAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;
	PinyinIME ss = new PinyinIME();

	// 常量,显示当前的连接状态
	public static final int STATE_NONE = 0;
	public static final int STATE_LISTEN = 1;
	public static final int STATE_CONNECTING = 2;
	public static final int STATE_CONNECTED = 3;

	public BluetoothService(Context context, Handler handler) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mHandler = handler;
	}

	/**
	 * 设置当前的连接状态
	 * 
	 * @param state
	 *            连接状态
	 */
	private synchronized void setState(int state) {
		mState = state;
		// 通知Activity更新UI
		mHandler.obtainMessage(ConnectActivity.MESSAGE_STATE_CHANGE,
				state, -1).sendToTarget();
	}

	/**
	 * 返回当前连接状态
	 * 
	 */
	public synchronized int getState() {
		return mState;
	}

	/**
	 * 开始聊天服务
	 * 
	 */
	public void startChat() {
		Log.e(TAG, "start ");

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
	 *            连接
	 */
	public synchronized void connect(BluetoothDevice device) {

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
		setState(STATE_CONNECTING);
	}

	/**
	 * 启动ConnectedThread开始管理一个蓝牙连接
	 */
	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {
		Log.e(TAG, "连接 ");

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

		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();
		// 连接成功，通知activity
		Message msg = mHandler.obtainMessage(ConnectActivity.MESSAGE_DEVICE_NAME);
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
				tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
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

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
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
			// 循环监听消息
			while (true) {
				try {
					byte[] buffer = new byte[256];
					bytes = mmInStream.read(buffer);
					String readStr = new String(buffer, 0, bytes);// 字节数组直接转换成字符串
					String str = bytes2HexString(buffer).replaceAll("00", "").trim();
					Log.e("lichao", "BluetoothChatService->readStr=" + readStr);
					Log.e("lichao", "BluetoothChatService->str=" + str);
					if (bytes > 0) {// 将读取到的消息发到主线程
						mHandler.obtainMessage(
								ConnectActivity.MESSAGE_READ, bytes, -1,
								buffer).sendToTarget();
						ss.pinyinIME.SetText(readStr);
					} else {
						Log.e(TAG, "disconnected");
						connectionLost();

						if (mState != STATE_NONE) {
							Log.e(TAG, "disconnected");
							startChat();
						}
						break;
					}
				} catch (IOException e) {
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
				mHandler.obtainMessage(ConnectActivity.MESSAGE_WRITE, -1,
						-1, buffer).sendToTarget();
			} catch (IOException e) {
				Log.e(TAG, "Exception during write:" + e);
			}
		}

		public void cancel() {
			try {
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
}
