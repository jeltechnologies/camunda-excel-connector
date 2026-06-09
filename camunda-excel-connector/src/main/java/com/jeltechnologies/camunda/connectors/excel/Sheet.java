package com.jeltechnologies.camunda.connectors.excel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;

public class Sheet {
    private final org.apache.poi.ss.usermodel.Sheet sheet;
    private final ExcelFile file;
    private final Map<String, Cell> cells = new HashMap<String, Cell>();
    private final int columns;
    private final int rows;

    public Sheet(ExcelFile file, org.apache.poi.ss.usermodel.Sheet sheet) {
        this.sheet = sheet;
        this.file = file;
        int highestColumnNumber = 0;
        int rowIndex = 1;
        for (org.apache.poi.ss.usermodel.Row poiRow : sheet) {
            int columnIndex = 1;
            for (org.apache.poi.ss.usermodel.Cell poiCell : poiRow) {
                Cell cell = new Cell(this, poiCell);
                cells.put(cell.getAddress(), cell);
                columnIndex++;
            }
            if (columnIndex > highestColumnNumber) {
                highestColumnNumber = columnIndex;
            }
            rowIndex++;
        }
        this.columns = highestColumnNumber;
        this.rows = rowIndex;
    }

    public String getName() {
        if (sheet == null) {
            return "null";
        }
        else {
            return sheet.getSheetName();
        }
    }

    public Cell getCell(int column, int row) {
        String address = new CellCoordinates(column, row).getAddress();
        return cells.get(address);
    }

    public Cell getCell(CellCoordinates coordinates) {
        String address = coordinates.getAddress();
        return cells.get(address);
    }

    public Cell getCellByAddress(String address) {
        Cell result = cells.get(address);
        return result;
    }

    public List<Cell> getCellsByValue(String value) {
        String valueLower = value.toLowerCase();
        List<Cell> results = new ArrayList<Cell>();
        for (String key : cells.keySet()) {
            Cell current = cells.get(key);
            String currentValueLower = current.getValue().toString().toLowerCase();
            if (valueLower.equals(currentValueLower)) {
                results.add(current);
            }
        }
        return results;
    }

    public int getNumberOfCellsByValue(String value) {
        List<Cell> cellsByValue = getCellsByValue(value);
        return cellsByValue.size();
    }

    public Cell getFirstWithSameValue(String value) {
        List<Cell> allResults = getCellsByValue(value);
        Cell result;
        if (allResults.isEmpty()) {
            result = null;
        }
        else {
            result = allResults.get(0);
        }
        return result;
    }

    public List<List<Cell>> getRange(String rangeStr) throws Exception {
        List<List<Cell>> result = new ArrayList<>();
        CellRangeAddress range = CellRangeAddress.valueOf(rangeStr);
        for (int rowIdx = range.getFirstRow(); rowIdx <= range.getLastRow(); rowIdx++) {
            List<Cell> rowData = new ArrayList<Cell>();
            Row row = sheet.getRow(rowIdx);
            for (int colIdx = range.getFirstColumn(); colIdx <= range.getLastColumn(); colIdx++) {
                org.apache.poi.ss.usermodel.Cell poiCell;
                if (row != null) {
                    poiCell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (poiCell == null) {
                        rowData.add(null);
                    } else {
                        Cell cell = new Cell(this, poiCell);
                        rowData.add(cell);
                    }
                }
            }
            result.add(rowData);
        }
        return result;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public ExcelFile getExcelFile() {
        return file;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("tab ").append(sheet.getSheetName()).append(" in ").append(file);
        return builder.toString();
    }

    public void close() {
        if (file != null) {
            file.close();
        }
    }

}
