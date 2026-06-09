package com.jeltechnologies.camunda.connectors.jsonobjects;

import java.util.ArrayList;
import java.util.List;

public class SheetObject {
    private String name;
    private List<RowObject> rows;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RowObject> getRows() {
        return rows;
    }

    public void setRows(List<RowObject> rows) {
        this.rows = rows;
    }
    
    public void addRow(RowObject row) {
        if (rows == null) {
            rows = new ArrayList<RowObject>();
        }
        rows.add(row);
    }

}
