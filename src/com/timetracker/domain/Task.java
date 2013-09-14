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

    public static final int DEFAULT_COLOR_ALPHA = 100;

    public static final String TAGS_SEPARATOR = "~";

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

    public String[] getTags() {
        return tags.substring(1).split(TAGS_SEPARATOR);
    }

    public void setTags(String[] tagsList) {
        StringBuilder builder = new StringBuilder(TAGS_SEPARATOR);
        for (String tag : tagsList) {
            tag = tag.replace(TAGS_SEPARATOR, "-");
            builder.append(tag.trim());
            builder.append(TAGS_SEPARATOR);
        }
        tags = builder.toString();
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", context=" + context +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        if (color != task.color) return false;
        if (isDeleted != task.isDeleted) return false;
        if (context != null ? !context.equals(task.context) : task.context != null) return false;
        if (id != null ? !id.equals(task.id) : task.id != null) return false;
        if (name != null ? !name.equals(task.name) : task.name != null) return false;
        return !(tags != null ? !tags.equals(task.tags) : task.tags != null);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        result = 31 * result + (isDeleted ? 1 : 0);
        result = 31 * result + color;
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        return result;
    }
}
