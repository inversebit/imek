package org.inversebit.imek_tf2.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import org.inversebit.imek_tf2.MainActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.inversebit.imek_tf2.data.Config.APP_FILE_PROVIDER;

public class PhotoTaker
{
	static final int REQUEST_TAKE_PHOTO = 1;

	public static File createImageFile(File storageDir) throws IOException
	{
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File image = File.createTempFile(
				imageFileName,
				".jpg",
				storageDir
		);

		return image;
	}

	public static void dispatchTakePictureIntent(Activity sourceAct, File photoFile) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(sourceAct.getPackageManager()) != null) {
			// Continue only if the File was successfully created
			if (photoFile != null) {
				Uri photoURI = FileProvider.getUriForFile(sourceAct,
				                                          APP_FILE_PROVIDER,
				                                          photoFile);
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
				sourceAct.startActivityForResult(takePictureIntent, MainActivity.REQUEST_IMAGE_CAPTURE);
			}
		}
	}
}
