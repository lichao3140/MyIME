package com.idata.bluetoothime;

import java.util.List;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

/**
 * Setting activity of Pinyin IME. 设置页面
 * 
 * @ClassName SettingsActivity
 * @author LiChao
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener {

	private static String TAG = "SettingsActivity";

	private CheckBoxPreference mKeySoundPref;
	private CheckBoxPreference mVibratePref;
	private CheckBoxPreference mPredictionPref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		PreferenceScreen prefSet = getPreferenceScreen();

		mKeySoundPref = (CheckBoxPreference) prefSet
				.findPreference(getString(R.string.setting_sound_key));
		mVibratePref = (CheckBoxPreference) prefSet
				.findPreference(getString(R.string.setting_vibrate_key));
		mPredictionPref = (CheckBoxPreference) prefSet
				.findPreference(getString(R.string.setting_prediction_key));

		prefSet.setOnPreferenceChangeListener(this);

		Settings.getInstance(PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext()));

		updatePreference(prefSet, getString(R.string.setting_advanced_key));

		updateWidgets();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateWidgets();
	}

	@Override
	protected void onDestroy() {
		Settings.releaseInstance();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// 把用户的设置存入配置文件中
		Settings.setKeySound(mKeySoundPref.isChecked());
		Settings.setVibrate(mVibratePref.isChecked());
		Settings.setPrediction(mPredictionPref.isChecked());

		Settings.writeBack();
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		return true;
	}

	/**
	 * 从配置文件获取之前的设置，更像UI
	 */
	private void updateWidgets() {
		mKeySoundPref.setChecked(Settings.getKeySound());
		mVibratePref.setChecked(Settings.getVibrate());
		mPredictionPref.setChecked(Settings.getPrediction());
	}

	/**
	 * 设置PreferenceScreen
	 * 
	 * @param parentPref
	 * @param prefKey
	 */
	public void updatePreference(PreferenceGroup parentPref, String prefKey) {
		Preference preference = parentPref.findPreference(prefKey);
		if (preference == null) {
			return;
		}
		Intent intent = preference.getIntent();
		if (intent != null) {
			PackageManager pm = getPackageManager();
			List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
			int listSize = list.size();
			if (listSize == 0)
				parentPref.removePreference(preference);
		}
	}
}
