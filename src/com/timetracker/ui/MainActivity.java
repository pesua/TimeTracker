package com.timetracker.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.timetracker.R;
import com.timetracker.domain.clickcounter.data.ClickGroup;
import com.timetracker.domain.clickcounter.data.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;

public class MainActivity extends OrmLiteBaseActivity<DatabaseHelper> {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        try {
            Dao<ClickGroup, Integer> groupDao = getHelper().getGroupDao();
            ClickGroup data = new ClickGroup();
            data.name = "AAA";
            groupDao.create(data);

            ClickGroup data1 = new ClickGroup();
            data1.name = "BBB";
            groupDao.create(data1);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), "FFFFFFUUUUUUuuuuuuuu", e);
        }

        ListAdapter adapter = null;
        try {
            final List<ClickGroup> clickGroups = getHelper().getGroupDao().queryForAll();
            Log.e("Ollo", "Received " + clickGroups.size() + " entities");
            Log.e("Ollo", "#############################################");
            adapter = new BaseAdapter() {
                @Override
                public int getCount() {
                    return clickGroups.size();
                }

                @Override
                public Object getItem(int position) {
                    return clickGroups.get(position);
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView textView = new TextView(MainActivity.this);
                    textView.setText(clickGroups.get(position).name);
                    return textView;
                }
            };
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), "FFFFFFUUUUUUuuuuuuuu", e);
        }
//        ArrayAdapter adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1, );
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);


    }
}
