package com.timetracker.domain.clickcounter.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Counter information object saved to the database.
 *
 * @author kevingalligan
 */
@DatabaseTable
public class ClickCount {

    public static final String DATE_FIELD_NAME = "lastClickDate";

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(columnName = DATE_FIELD_NAME)
    private Date lastClickDate;

    @DatabaseField(index = true)
    private String name;

    @DatabaseField
    private String description;

    @DatabaseField
    private int value;

    @DatabaseField(canBeNull = true, foreign = true)
    private ClickGroup group;
}
