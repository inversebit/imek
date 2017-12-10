package org.inversebit.imek_tf2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.inversebit.imek_tf2.processing.imgrek.ImageParser;
import org.inversebit.imek_tf2.processing.imgrek.ImageParsingTask;
import org.inversebit.imek_tf2.utils.ImageCleanupTask;
import org.inversebit.imek_tf2.utils.PhotoTaker;
import org.inversebit.imek_tf2.utils.preferences.PrefsActivity;

import java.io.File;

import static org.inversebit.imek_tf2.data.Config.GLOBAL_TAG;

public class MainActivity extends AppCompatActivity
{
	private static String TAG = GLOBAL_TAG + "MA";

	private static int MY_PERMISSIONS_REQUEST_CAMERA = 1;
	private static int MY_PERMISSIONS_REQUEST_STORAGE = 2;


	public static int REQUEST_IMAGE_CAPTURE = 1;

	public String fileDest;

	private ImageParsingTask imgPars;

	private boolean debugMode = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Log.i(TAG, "onClick: Clicked");
				if (gotAllPermissions())
				{
					takePhoto();
				}
			}
		});

		new ImageCleanupTask(this).execute();
	}

	private void askForPermission(String perm, int callBackCode)
	{
		ActivityCompat.requestPermissions(this, new String[]{perm}, callBackCode);
	}

	private boolean gotAllPermissions(){
		int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
		if(permissionCheck != PackageManager.PERMISSION_GRANTED){
			askForPermission(Manifest.permission.CAMERA, MY_PERMISSIONS_REQUEST_CAMERA);
			return false;
		}

		int permissionCheck2 = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if(permissionCheck2 != PackageManager.PERMISSION_GRANTED){
			askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_STORAGE);
			return false;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_debug)
		{
			if (gotAllPermissions())
			{
				debugMode = true;
				takePhoto();
			}

			return true;
		}else if(id == R.id.action_prefs){
			Intent launchNewIntent = new Intent(this, PrefsActivity.class);
			startActivityForResult(launchNewIntent, 0);
		}

		return super.onOptionsItemSelected(item);
	}

	private void takePhoto(){
		try{
			File dest = PhotoTaker.createImageFile(getExternalFilesDir(Environment.DIRECTORY_PICTURES));
			fileDest = dest.getAbsolutePath();
			PhotoTaker.dispatchTakePictureIntent(MainActivity.this, dest);
		}
		catch (Exception ex){
			Log.e(TAG, "onClick: ", ex);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			if(fileDest != null && !fileDest.isEmpty()){
				File photo = new File(fileDest);
				if(photo.exists()){
					Log.i(TAG, "onActivityResult: Got photo" + photo.toString());

					if(debugMode){
						debugMode = false;
						Intent launchNewIntent = new Intent(this, DebugActivity.class);
						launchNewIntent.putExtra(DebugActivity.DEBUG_EXTRA_FILEPATH, fileDest);
						startActivity(launchNewIntent);
					}else{
						try{
							if(imgPars != null) imgPars.cancel(true);
							imgPars = new ImageParsingTask(this, ImageParser.Instance(this));
							imgPars.execute(photo);
						}catch(Exception ex){
							Log.e(TAG, "onActivityResult: Error running ImageParsingTask", ex);
						}
					}
				}else{
					Log.w(TAG, "onActivityResult: File doesnt exist");
				}
			}else{
				Log.w(TAG, "onActivityResult: fileDest null or empty");
			}
		}else{
			Log.w(TAG, String.format("onActivityResult: Received unknown activity result code. Reqc: %d, Resc: %d", requestCode, resultCode));
		}
	}
}
