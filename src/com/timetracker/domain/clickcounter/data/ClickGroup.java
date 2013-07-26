package com.timetracker.domain.clickcounter.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Counter group information object saved to the database.
 *
 * @author kevingalligan
 */
@DatabaseTable
public class ClickGroup {

    @DatabaseField(generatedId = true)
    public Integer id;

    @DatabaseField
    public String name;

    @Override
    public String toString() {
        return "ClickGroup{" +
                "name='" + name + '\'' +
                '}';
    }
}
