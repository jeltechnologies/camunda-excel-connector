package com.jeltechnologies.camunda.connectors.jsonobjects;

import java.util.ArrayList;
import java.util.List;

public class RowWithValuesObject {
    private List<Object> rows = new ArrayList<Object>();

    public List<Object> getRows() {
        return rows;
    }

    public void setRows(List<Object> rows) {
        this.rows = rows;
    }
}
