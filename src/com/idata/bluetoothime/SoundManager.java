package com.idata.bluetoothime;

import android.content.Context;
import android.media.AudioManager;

/**
 * Class used to manage related sound resources.
 */
/**
 * 声音管理类，该类采用单例模式。
 * 
 * @ClassName SoundManager
 * @author LiChao
 */
public class SoundManager {
	private static SoundManager mInstance = null;
	private Context mContext;
	private AudioManager mAudioManager;
	// Align sound effect volume on music volume
	private final float FX_VOLUME = -1.0f;
	private boolean mSilentMode;

	private SoundManager(Context context) {
		mContext = context;
		updateRingerMode();
	}

	public void updateRingerMode() {
		if (mAudioManager == null) {
			mAudioManager = (AudioManager) mContext
					.getSystemService(Context.AUDIO_SERVICE);
		}
		mSilentMode = (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL);
	}

	public static SoundManager getInstance(Context context) {
		if (null == mInstance) {
			if (null != context) {
				mInstance = new SoundManager(context);
			}
		}
		return mInstance;
	}

	/**
	 * 如果mSilentMode为false，就播放按下按键的声音。
	 */
	public void playKeyDown() {
		if (mAudioManager == null) {
			updateRingerMode();
		}
		if (!mSilentMode) {
			int sound = AudioManager.FX_KEYPRESS_STANDARD;
			mAudioManager.playSoundEffect(sound, FX_VOLUME);
		}
	}
}
