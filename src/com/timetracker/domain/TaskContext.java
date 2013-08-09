package com.timetracker.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Anton Chernetskij
 */
@DatabaseTable(tableName = "contexts")
public class TaskContext {

    @DatabaseField(generatedId = true)
    public Integer id;

    @DatabaseField
    public String name;

    @Override
    public String toString() {
        return name;
    }
}
