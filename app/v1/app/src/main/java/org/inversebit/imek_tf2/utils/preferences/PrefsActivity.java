package org.inversebit.imek_tf2.utils.preferences;

import android.app.Activity;
import android.os.Bundle;

public class PrefsActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PrefsFragment())
				.commit();
	}
}
