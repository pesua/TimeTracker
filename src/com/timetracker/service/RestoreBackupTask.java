package com.timetracker.service;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.timetracker.R;
import com.timetracker.domain.persistance.DatabaseHelper;
import com.timetracker.ui.activities.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @author Anton Chernetskij
 */
public class RestoreBackupTask extends AsyncTask<String, Void, String> {

    private static final String TAG = RestoreBackupTask.class.getSimpleName();

    private MainActivity mainActivity;

    public RestoreBackupTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected String doInBackground(String... params) {
        return importDB(params[0]);
    }

    private String importDB(String filename) {
        try {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//com.timetracker//databases//" + DatabaseHelper.DATABASE_NAME;
                File data = Environment.getDataDirectory();
                File currentDB = new File(data, currentDBPath);

                String backupDBPath = "/" + BackupTask.APP_FOLDER + "/" + filename + ".sqlite";
                File backupDB = new File(sd, backupDBPath);
                if (backupDB.exists()) {
                    copyFile(backupDB, currentDB);

                    DatabaseHelper helper = OpenHelperManager.getHelper(mainActivity, DatabaseHelper.class);
                    helper.getWritableDatabase().close();
                    releaseHelperForSure();
                    Log.i(TAG, "Successfully imported DB to " + backupDB.getAbsolutePath());
                    return mainActivity.getResources().getText(R.string.import_success).toString();
                } else {
                    Log.e(TAG, "Backup file not found " + backupDB.getAbsolutePath());
                }
            } else {
                Log.e(TAG, "Can't write to storage");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to export DB", e);
        }
        return null;

    }

    private void releaseHelperForSure() {
        DatabaseHelper oldHelper = OpenHelperManager.getHelper(mainActivity, DatabaseHelper.class);
        DatabaseHelper newHelper = oldHelper;
        while (oldHelper == newHelper){
            for (int i = 0; i < 10; i++) {
                OpenHelperManager.releaseHelper();
            }
            newHelper = OpenHelperManager.getHelper(mainActivity, DatabaseHelper.class);
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null) {
            mainActivity.refreshAll();
            Toast.makeText(mainActivity, result, Toast.LENGTH_LONG).show();
        }
    }

    private void copyFile(File source, File destination) throws IOException {
        destination.createNewFile();

        FileChannel src = new FileInputStream(source).getChannel();
        FileChannel dst = new FileOutputStream(destination).getChannel();
        dst.transferFrom(src, 0, src.size());
        src.close();
        dst.close();
    }
}
