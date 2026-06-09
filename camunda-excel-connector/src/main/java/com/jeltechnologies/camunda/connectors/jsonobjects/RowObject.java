package com.jeltechnologies.camunda.connectors.jsonobjects;

import java.util.ArrayList;
import java.util.List;

public class RowObject {
    private List<Object> cells;

    public List<Object> getCells() {
        return cells;
    }

    public void setCells(List<Object> cells) {
        this.cells = cells;
    }
    
    public void addColumn(Object column) {
        if (cells == null) {
            cells = new ArrayList<Object>();
        }
        this.cells.add(column);
    }
}
