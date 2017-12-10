package org.inversebit.imek_tf2.processing.imgrek;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import org.inversebit.imek_tf2.R;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.inversebit.imek_tf2.data.Config.GLOBAL_TAG;

public class ImageParser
{
	private static String TAG = GLOBAL_TAG + "IP";

	public static String POSTPROC_IMG = "postproc.png";

	private int THRESH_MAXVAL;
	private int THRESH_BLOCKSIZE;
	private int THRESH_C;
	private int DILATE_ITRS;
	private int EROSION_ITRS;

	private String externalDirPath;

	private ImageParser(Context pCtx){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pCtx);
		THRESH_MAXVAL = prefs.getInt(pCtx.getResources().getString(R.string.pref_imgpreproc_thresh_maxval), 0);
		THRESH_BLOCKSIZE = prefs.getInt(pCtx.getResources().getString(R.string.pref_imgpreproc_thresh_blocksize), 0);
		THRESH_C = prefs.getInt(pCtx.getResources().getString(R.string.pref_imgpreproc_thresh_c), 0);
		DILATE_ITRS = prefs.getInt(pCtx.getResources().getString(R.string.pref_imgpreproc_dilate_itrs), 0);
		EROSION_ITRS = prefs.getInt(pCtx.getResources().getString(R.string.pref_imgpreproc_erosion_itrs), 0);

		externalDirPath = pCtx.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
	}

	public static ImageParser Instance(Context pCtx){
		return new ImageParser(pCtx);
	}

	public List<String> extractElements(String fileName)
	{
		Mat kernel = Mat.ones(7, 7, CvType.CV_8UC1);

		Mat tmp2 = Imgcodecs.imread(fileName);
		Mat tmp1 = Mat.zeros(tmp2.rows(), tmp2.rows(), tmp2.type());
		Mat thresh = Mat.zeros(tmp2.rows(), tmp2.rows(), tmp2.type());

		////////////
		// PREPROC
		////////////
		Imgproc.cvtColor(tmp2, tmp1, Imgproc.COLOR_BGR2GRAY);
		Imgproc.GaussianBlur(tmp1, tmp2, new Size(51, 51), 0);

		////////////
		// THRESH
		////////////
		Imgproc.adaptiveThreshold(tmp2, thresh, THRESH_MAXVAL, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, THRESH_BLOCKSIZE, THRESH_C);
		Imgproc.dilate(thresh, tmp2, kernel, new Point(-1,-1), DILATE_ITRS);
		Imgproc.erode(tmp2, tmp1, kernel, new Point(-1,-1), EROSION_ITRS);
		Imgproc.medianBlur(tmp1, tmp2, 9);

		saveToFile(tmp2, POSTPROC_IMG);

		////////////
		// EDGES
		////////////
		List<MatOfPoint> conts = new ArrayList<>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(tmp2, conts, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		////////////
		// CONT FILTERING
		////////////
		double maxContourArea = Imgproc.contourArea(Collections.max(conts, new Comparator<MatOfPoint>()
		{
			@Override
			public int compare(MatOfPoint x, MatOfPoint y)
			{
				double xa = Imgproc.contourArea(x);
				double ya = Imgproc.contourArea(y);
				return xa > ya ? 1 : -1;
			}
		}));
		maxContourArea *= 0.6;

		List<MatOfPoint> filtConts = new ArrayList<>(16);
		for (MatOfPoint mop: conts)
		{
			if(Imgproc.contourArea(mop) > maxContourArea){
				filtConts.add(mop);
			}
		}

		////////////
		// BOUNDING RECTS
		////////////
		List<Rect> rects = new ArrayList<>(filtConts.size());
		for(MatOfPoint cont:filtConts){
			rects.add(Imgproc.boundingRect(cont));
		}

		Collections.sort(rects, new Comparator<Rect>()
		{
			@Override
			public int compare(Rect a, Rect b)
			{
				return a.y > b.y ? 1 : -1;
			}
		});

		Log.i(TAG, String.format("extractElements: Got %d elems", rects.size()));

		////////////
		// EXTRACTION
		////////////
		Imgproc.dilate(thresh, tmp2, kernel, new Point(-1,-1), 3);
		Imgproc.erode(tmp2, tmp1, kernel, new Point(-1,-1), 1);
		Core.bitwise_not(tmp1, tmp2);

		String route = new File(fileName).getParent();
		List<String> result = new ArrayList<>(rects.size());

		Mat roi;
		Mat resized = Mat.zeros(224, 224, CvType.CV_8UC1);
		int idx = 0;
		for(Rect subimg: rects){
			roi = new Mat(tmp2, subimg);
			Imgproc.resize(roi, resized, new Size(224, 224));
			String imName = String.format("%s/elem%d.png", route, idx);
			Imgcodecs.imwrite(imName, resized);
			result.add(imName);
			idx++;
		}

		return result;
	}

	private void saveToFile(Mat img, String filename){
		String filePath = externalDirPath + "/" + filename;

		try{
			Imgcodecs.imwrite(filePath, img);
			new File(filePath);
		}catch(Exception ex){
			Log.e(TAG, String.format("saveToFile: Cannot save to file: %s", filePath), ex);
		}

	}
}
