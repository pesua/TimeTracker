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

    @DatabaseField
    public boolean isDeleted = false;

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskContext context = (TaskContext) o;

        return isDeleted == context.isDeleted &&
                !(id != null ? !id.equals(context.id) : context.id != null) &&
                !(name != null ? !name.equals(context.name) : context.name != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (isDeleted ? 1 : 0);
        return result;
    }
}
