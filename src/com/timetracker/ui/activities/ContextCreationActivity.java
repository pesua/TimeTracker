package com.timetracker.ui.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.timetracker.R;
import com.timetracker.domain.TaskContext;
import com.timetracker.domain.persistance.DatabaseHelper;

import java.sql.SQLException;

/**
 * Created by Anton Chernetskij
 *
 * Dialog sample
 */
public class ContextCreationActivity extends OrmLiteBaseActivity<DatabaseHelper> {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.context);
        Button saveButton = (Button) findViewById(R.id.contextSaveButton);
        final TextView nameEdit = (TextView) findViewById(R.id.contextNameEdit);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameEdit.getText().toString().trim().isEmpty()) {
                    Resources res = getResources();
                    String msg = String.format(res.getString(R.string.emptyName));
                    Toast toast = Toast.makeText(ContextCreationActivity.this, msg, Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    TaskContext context = new TaskContext();
                    context.name = nameEdit.getText().toString();
                    try {
                        getHelper().getContextDao().create(context);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    finish();
                }
            }
        });
    }
}