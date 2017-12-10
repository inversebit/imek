package org.inversebit.imek_tf2.utils

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import org.inversebit.imek_tf2.data.Config.GLOBAL_TAG

class ImageCleanupTask(val ctx: Context) : AsyncTask<Void?, Void?, Void?>() {

    private val TAG = GLOBAL_TAG + "ICT"
    private lateinit var pd:ProgressDialog

    override fun onPreExecute() {
        super.onPreExecute()
        pd = ProgressDialog.show(ctx, "", "Loading", true, false)
    }

    override fun doInBackground(vararg p0: Void?): Void? {
        try{
            var fileDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            fileDir.listFiles().forEach { file -> file.delete() }
        }catch(ex: Exception){
            Log.e(TAG, "doInBackground: Error deleting old images")
        }

        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        pd.dismiss()
    }
}