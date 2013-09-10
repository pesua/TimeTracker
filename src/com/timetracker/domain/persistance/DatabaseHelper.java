package com.timetracker.domain.persistance;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.timetracker.domain.Task;
import com.timetracker.domain.TaskContext;
import com.timetracker.domain.TaskSwitchEvent;

import java.sql.SQLException;

/**
 * Database helper which creates and upgrades the database and provides the DAOs for the app.
 *
 * @author kevingalligan
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "time_tracker.db";
    private static final int DATABASE_VERSION = 1;

    private static final Class[] DOMAIN_CLASSES = {Task.class, TaskContext.class, TaskSwitchEvent.class};

    private Dao<Task, Integer> taskDao;
    private Dao<TaskContext, Integer> contextDao;
    private Dao<TaskSwitchEvent, Integer> eventsDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION/*, R.raw.ormlite_config*/);
    }

    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
        try {
            for (Class<?> domainClass : DOMAIN_CLASSES) {
                TableUtils.createTable(connectionSource, domainClass);
            }
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Unable to create datbases", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
        try {
            for (Class<?> domainClass : DOMAIN_CLASSES) {
                TableUtils.dropTable(connectionSource, domainClass, true);
            }
            onCreate(sqliteDatabase, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to new "
                    + newVer, e);
            throw new RuntimeException(e);
        }
    }

    public Dao<Task, Integer> getTaskDao() throws SQLException {
        if (taskDao == null) {
            taskDao = getDao(Task.class);
        }
        return taskDao;
    }

    public Dao<TaskContext, Integer> getContextDao() throws SQLException {
        if (contextDao == null) {
            contextDao = getDao(TaskContext.class);
        }
        return contextDao;
    }

    public Dao<TaskSwitchEvent, Integer> getEventsDao() throws SQLException {
        if (eventsDao == null) {
            eventsDao = getDao(TaskSwitchEvent.class);
        }
        return eventsDao;
    }
}
