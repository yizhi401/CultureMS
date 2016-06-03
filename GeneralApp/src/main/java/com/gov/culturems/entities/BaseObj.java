package com.gov.culturems.entities;

import java.io.Serializable;

/**
 * 所有场景、设备的基类
 * Created by peter on 4/6/16.
 */
public abstract class BaseObj implements Serializable {
    protected String id;
    protected String name;

    public BaseObj() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract boolean query(String query);

    @Override
    public String toString() {
        return "BaseObj{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
