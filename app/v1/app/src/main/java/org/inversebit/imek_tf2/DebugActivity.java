package org.inversebit.imek_tf2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;

import org.inversebit.imek_tf2.processing.imgrek.ImageParser;
import org.inversebit.imek_tf2.processing.imgrek.ImageParsingTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.inversebit.imek_tf2.data.Config.GLOBAL_TAG;

public class DebugActivity extends AppCompatActivity
{
	public static String DEBUG_EXTRA_FILEPATH = "DEBUG_EXTRA_FILEPATH";

	private static String TAG = GLOBAL_TAG + "DA";
	private static int RESIZED_IMG_WIDTH = 500;

	private ImageParsingTask imgPars;

	@BindView(R.id.imgPrev)
	ImageView prevImg;

	@BindView(R.id.imgProc)
	ImageView processedImg;

	@BindView(R.id.subelems)
	GridLayout subelementsGrid;

	@BindView(R.id.contBtn)
	Button continueButton;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_debug);
		ButterKnife.bind(this);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		String fileDest = getIntent().getStringExtra(DEBUG_EXTRA_FILEPATH);

		try{
			File photo = new File(fileDest);
			if(imgPars != null) imgPars.cancel(true);
			imgPars = new DebugImageParsingTask(this, ImageParser.Instance(this));
			imgPars.execute(photo);
		}catch(Exception ex){
			Log.e(TAG, "onStart: Error running ImageParsingTask", ex);
		}
	}

	protected void setDebugImgs(final List<String> imgs){
		//Get postproc img
		loadAndDrawImg(getIntent().getStringExtra(DEBUG_EXTRA_FILEPATH), RESIZED_IMG_WIDTH, prevImg);

		//Draw postproc img
		loadAndDrawImg(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + ImageParser.POSTPROC_IMG, RESIZED_IMG_WIDTH, processedImg);

		//Draw subimgs
		for(String subelem: imgs){
			ImageView iv = new ImageView(this);
			loadAndDrawImg(subelem, 224, iv);
			subelementsGrid.addView(iv);
		}

		//Prepare cont button to launch intent with imgs to UIBuilderActivity
		continueButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent launchNewIntent = new Intent(DebugActivity.this, UIBuildActivity.class);
				launchNewIntent.putExtra(UIBuildActivity.UIBUILD_SUBELEMS_EXTRA, new ArrayList<>(imgs));
				startActivity(launchNewIntent);
			}
		});
	}

	private void loadAndDrawImg(String imgPath, int imgWidth, ImageView imgDest){
		try
		{
			File photo = new File(imgPath);
			Bitmap myBitmap = BitmapFactory.decodeFile(photo.getAbsolutePath());
			float ar = myBitmap.getHeight() / (float) myBitmap.getWidth();
			Bitmap scaled = Bitmap.createScaledBitmap(myBitmap, imgWidth, Math.round(ar * imgWidth), false);
			imgDest.setImageBitmap(scaled);
		}catch(Exception ex){
			Log.e(TAG, String.format("loadAndDrawImg: Cannot set image %s", imgPath), ex);
		}
	}

	private class DebugImageParsingTask extends ImageParsingTask{

		public DebugImageParsingTask(Context pCtx, ImageParser pImgPar)
		{
			super(pCtx, pImgPar);
		}

		@Override
		protected void onPostExecute(List<String> fileList)
		{
			super.pd.dismiss();
			setDebugImgs(fileList);
		}
	}
}
