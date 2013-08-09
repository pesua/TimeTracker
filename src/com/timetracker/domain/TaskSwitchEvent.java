package com.timetracker.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by Anton Chernetskij
 */
@DatabaseTable(tableName = "task_switch_events")
public class TaskSwitchEvent {

    @DatabaseField(generatedId = true)
    public Integer id;

    @DatabaseField
    public Date switchTime;

    @DatabaseField(canBeNull = false, foreign = true, maxForeignAutoRefreshLevel = 3, foreignAutoRefresh = true)
    public Task task;

    @Override
    public String toString() {
        return "TaskSwitchEvent{" +
                "switchTime=" + switchTime +
                ", task=" + task +
                '}';
    }
}
