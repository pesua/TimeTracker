package com.timetracker.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Anton Chernetskiy
 */
@DatabaseTable(tableName = "tasks")
public class Task {

    @DatabaseField(generatedId = true)
    public Integer id;

    @DatabaseField
    public String name;

    @DatabaseField(canBeNull = false, foreign = true, maxForeignAutoRefreshLevel = 3, foreignAutoRefresh = true)
    public TaskContext context;

    @DatabaseField
    public boolean isDeleted = false;

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
