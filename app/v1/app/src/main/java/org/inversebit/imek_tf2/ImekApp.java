package org.inversebit.imek_tf2;

import android.app.Application;
import android.preference.PreferenceManager;

public class ImekApp extends Application
{
	static { System.loadLibrary("opencv_java3"); }

	@Override
	public void onCreate()
	{
		super.onCreate();

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}
}
