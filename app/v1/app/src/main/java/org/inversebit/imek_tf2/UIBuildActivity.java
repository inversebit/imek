package org.inversebit.imek_tf2;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.inversebit.imek_tf2.processing.tf.Classifier;
import org.inversebit.imek_tf2.processing.tf.TensorFlowImageClassifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.inversebit.imek_tf2.data.Config.GLOBAL_TAG;

public class UIBuildActivity extends AppCompatActivity
{
	private static String TAG = GLOBAL_TAG + "UIBA";

	public static String UIBUILD_SUBELEMS_EXTRA = "UIBUILD_SUBELEMS_EXTRA";

	@BindView(R.id.uiBuildContainer)
	LinearLayout uiBuildContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_uibuild);
		ButterKnife.bind(this);
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		//Get subelem list form intent
		List<String> subElemFileList = getIntent().getStringArrayListExtra(UIBUILD_SUBELEMS_EXTRA);

		//Start asynctask to build UI
		AsyncTask<List<String>, Void, List<String>> uiBuildTask  = new UIBuilderTask();
		uiBuildTask.execute(subElemFileList);
	}

	private class UIBuilderTask extends AsyncTask<List<String>, Void, List<String>>
	{
		private String TAG = GLOBAL_TAG + "UIBT";

		private ProgressDialog pd;

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();

			Log.i(TAG, "onPreExecute: Start classification");

			pd = ProgressDialog.show(UIBuildActivity.this, "", "Building UI", true, false);
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}

		@Override
		protected List<String> doInBackground(List<String>... subelems)
		{
			Log.i(TAG, "onPreExecute: Processing elems");
			Classifier classifier = TensorFlowImageClassifier.create(getAssets(),
			                  "imekv1_graph.pb",
			                  "imekv1_labels.txt",
			                  224,
			                  128,
			                  128,
			                  "input",
			                  "final_result");

			List<String> subElemList = subelems[0];
			List<String> res = new ArrayList<>(subElemList.size());
			for(String subElemPath: subElemList){
				Bitmap myBitmap = BitmapFactory.decodeFile(subElemPath);
				List<Classifier.Recognition> rec = classifier.recognizeImage(myBitmap);

				Log.i(TAG, String.format("doInBackground: Got classification. %s ==> %s --> %.2f", subElemPath, rec.get(0).getId(), rec.get(0).getConfidence()));

				res.add(rec.get(0).getTitle());
			}

			classifier.close();

			return res;
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
			pd.dismiss();
		}

		@Override
		protected void onPostExecute(List<String> uiElemList)
		{
			super.onPostExecute(uiElemList);
			pd.dismiss();

			Log.i(TAG, "onPostExecute: Finished classification");

			for(String uiElem : uiElemList){
				View elem = null;

				switch(uiElem){
					case "img":
						elem = generateImageView();
						break;
					case "txt":
						elem = generateTextView();
						break;
					case "chk":
						elem = generateRadioGroupView();
						break;
				}

				Log.i(TAG, String.format("onPostExecute: Will add element to UI: %s", elem == null ? elem.getClass().toString() : "null"));
				if(elem != null){
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					params.setMargins(0, 0, 0,8);

					elem.setLayoutParams(params);
					uiBuildContainer.addView(elem);
				}
			}

			Log.i(TAG, "onPostExecute: Finiched building UI");
		}
	}

	protected ImageView generateImageView()
	{
		ImageView iv = new ImageView(this);
		String assetName = new Random().nextInt(4) + ".jpg";
		Bitmap myBitmap = null;
		try{
			myBitmap = BitmapFactory.decodeStream(getAssets().open(assetName));
		}catch(Exception ex){
			Log.e(TAG, String.format("generateImageView: Cannot decode bitmap %s", assetName), ex);
		}

		if(myBitmap != null)iv.setImageBitmap(myBitmap);

		return iv;
	}

	protected TextView generateTextView(){
		TextView tv = new TextView(this);
		String[] texts = getResources().getStringArray(R.array.ui_texts);
		tv.setText(texts[new Random().nextInt(texts.length-1)]);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		return tv;
	}

	protected RadioGroup generateRadioGroupView(){
		RadioGroup rg = new RadioGroup(this);

		String[] options = getResources().getStringArray(R.array.ui_options);

		for(int i = 0; i < 2; i++){
			RadioButton rb = new RadioButton(this);
			rb.setText(options[new Random().nextInt(options.length-1)]);
			rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			rg.addView(rb);
		}

		return rg;
	}
}
