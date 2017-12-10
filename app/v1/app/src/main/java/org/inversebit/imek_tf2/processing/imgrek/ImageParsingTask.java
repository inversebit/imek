package org.inversebit.imek_tf2.processing.imgrek;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.inversebit.imek_tf2.UIBuildActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.inversebit.imek_tf2.data.Config.GLOBAL_TAG;


public class ImageParsingTask extends AsyncTask<File, Void, List<String>>
{
	private static String TAG = GLOBAL_TAG + "IPT";

	private ImageParser imgPar;
	private Context ctx;

	protected ProgressDialog pd;

	public ImageParsingTask(Context pCtx, ImageParser pImgPar){
		ctx = pCtx;
		imgPar = pImgPar;
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();

		Log.i(TAG, "onPostExecute: ImageParsingTask STARTED");

		pd = ProgressDialog.show(ctx, "", "Processing", true, false);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}

	@Override
	protected List<String> doInBackground(File... files)
	{
		Log.i(TAG, "onPostExecute: ImageParsingTask WORKING");

		List<String> subElemPaths = imgPar.extractElements(files[0].getAbsolutePath());
		return subElemPaths;
	}

	@Override
	protected void onPostExecute(List<String> fileList)
	{
		super.onPostExecute(fileList);
		Log.i(TAG, "onPostExecute: ImageParsingTask DONE!");
		pd.dismiss();

		//Launch intent with imgs to UIBuilderActivity
		Intent launchNewIntent = new Intent(ctx, UIBuildActivity.class);
		launchNewIntent.putExtra(UIBuildActivity.UIBUILD_SUBELEMS_EXTRA, new ArrayList<>(fileList));
		ctx.startActivity(launchNewIntent);
	}

	@Override
	protected void onCancelled()
	{
		super.onCancelled();
		Log.i(TAG, "onPostExecute: ImageParsingTask CANCELLED :(");
		pd.dismiss();
	}
}
