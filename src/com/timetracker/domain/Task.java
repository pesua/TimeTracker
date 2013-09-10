package com.timetracker.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Anton Chernetskiy
 */
@DatabaseTable(tableName = "tasks")
public class Task {

    public static final int[] COLORS = {0xFF000000, 0xFF444444, 0xFF888888, 0xFFCCCCCC, /*0xFFFFFFFF, */0xFFFF0000,
            0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFF00FFFF, 0xFFFF00FF};

    @DatabaseField(generatedId = true)
    public Integer id;

    @DatabaseField
    public String name;

    @DatabaseField(canBeNull = false, foreign = true, maxForeignAutoRefreshLevel = 3, foreignAutoRefresh = true)
    public TaskContext context;

    @DatabaseField
    public boolean isDeleted = false;

    @DatabaseField
    public int color;

    @DatabaseField
    public String tags;

    public Task() {
    }

    public Task(String name, TaskContext context) {
        this.name = name;
        this.context = context;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", context=" + context +
                '}';
    }
}
