package com.jeltechnologies.camunda.connectors.jsonobjects;

import java.util.ArrayList;
import java.util.List;

public class FileObject {
    private String fileName;
    
    private List<SheetObject> workSheets;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<SheetObject> getWorkSheets() {
        return workSheets;
    }

    public void setWorkSheets(List<SheetObject> workSheets) {
        this.workSheets = workSheets;
    }
    
    public void addWorkSheet(SheetObject workSheet) {
        if (workSheets == null) {
            workSheets = new ArrayList<SheetObject>();
        }
        workSheets.add(workSheet);
    }
}
