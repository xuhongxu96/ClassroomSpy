package com.xuhongxu.xiaoya.model;

/**
 * Created by Hongxu Xu on 4/17/2017.
 */
public class Building {
    public String id, name;
    public String peopleCountId;
    public int roomNum;

    public Building(String id, String name) {
        this.id = id;

        if (name.contains("("))
        {
            name = name.substring(0, name.indexOf("("));
        }

        this.name = name;
    }
}
