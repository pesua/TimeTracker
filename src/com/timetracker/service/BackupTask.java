package com.timetracker.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.timetracker.R;
import com.timetracker.domain.persistance.DatabaseHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Anton Chernetskij
 */
public class BackupTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = BackupTask.class.getSimpleName();
    public static final String APP_FOLDER = "Timetracker";

    private Context context;

    public BackupTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        return exportDB();
    }

    private String exportDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//com.timetracker//databases//" + DatabaseHelper.DATABASE_NAME;
                File data = Environment.getDataDirectory();
                File currentDB = new File(data, currentDBPath);
                new File(sd, APP_FOLDER).mkdir();

                String backupDBPath = "/" + APP_FOLDER + "/" + new SimpleDateFormat("yyyy.MM.dd_hh-mm").format(new Date()) + ".sqlite";
                File backupDB = new File(sd, backupDBPath);

                copyFile(currentDB, backupDB);
                Log.i(TAG, "Successfully exported DB to " + backupDB.getAbsolutePath());
                return context.getResources().getText(R.string.export_success).toString();
            } else {
                Log.e(TAG, "Can't write to storage");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to export DB", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null) {
            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
        }
    }

    private void copyFile(File source, File destination) throws IOException {
        Log.i(TAG, "Copying " + source.getAbsolutePath() + " to " + destination.getAbsolutePath());
        destination.createNewFile();

        FileChannel src = new FileInputStream(source).getChannel();
        FileChannel dst = new FileOutputStream(destination).getChannel();
        dst.transferFrom(src, 0, src.size());
        src.close();
        dst.close();
    }
}
