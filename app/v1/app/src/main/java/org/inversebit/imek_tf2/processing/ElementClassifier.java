package org.inversebit.imek_tf2.processing;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.inversebit.imek_tf2.processing.tf.Classifier;
import org.inversebit.imek_tf2.processing.tf.TensorFlowImageClassifier;

import java.io.InputStream;
import java.util.List;

import static org.inversebit.imek_tf2.data.Config.GLOBAL_TAG;

public class ElementClassifier
{
	private static final String TAG = GLOBAL_TAG + "IP";

	public static String classifyElem(AssetManager assMan, String imgName){
		Classifier tfimc = TensorFlowImageClassifier.create(assMan,
		                                                    "file:///android_asset/imekv1_graph.pb",
		                                                    "file:///android_asset/imekv1_labels.txt",
		                                                    224,
		                                                    128,
		                                                    128,
		                                                    "input",
		                                                    "final_result");

		InputStream istr;
		Bitmap bitmap = null;
		try {
			Bitmap tmpBitmap = null;
			istr = assMan.open(imgName);
			tmpBitmap = BitmapFactory.decodeStream(istr);
			bitmap = Bitmap.createScaledBitmap(
					tmpBitmap, 224, 224, false);

		} catch (Exception e) {
			Log.e(TAG, "goBtn: PETO", e);
		}

		List<Classifier.Recognition> reclist = tfimc.recognizeImage(bitmap);

		StringBuilder sb = new StringBuilder();
		sb.append(imgName + " results:\n");
		for(Classifier.Recognition rec:reclist){
			sb.append(String.format("goBtn: Rec --> %s, Conf --> %.3f\n", rec.getTitle(), rec.getConfidence()));
		}

		tfimc.close();

		return sb.toString();
	}
}
