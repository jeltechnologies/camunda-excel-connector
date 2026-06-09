package com.jeltechnologies.camunda.connectors.excel.camunda;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.jeltechnologies.camunda.connectors.excel.Cell;
import com.jeltechnologies.camunda.connectors.excel.CellCoordinates;
import com.jeltechnologies.camunda.connectors.excel.ExcelFile;
import com.jeltechnologies.camunda.connectors.excel.Sheet;
import com.jeltechnologies.camunda.connectors.jsonobjects.CellObject;
import com.jeltechnologies.camunda.connectors.jsonobjects.FileObject;
import com.jeltechnologies.camunda.connectors.jsonobjects.RowObject;
import com.jeltechnologies.camunda.connectors.jsonobjects.SheetObject;

class ExcelReader {

    private final ExcelFileLoader loader = new ExcelFileLoader();

    private record CellObjectAndValue(Object object, Object value) {
    }

    public Object getCellByAddress(CommonInput commonInput, String workSheet, String cellAddress) throws Exception {
        ExcelFile file = null;
        try {
            file = loader.openFile(commonInput);
            Cell cell = file.getCell(workSheet, new CellCoordinates(cellAddress));
            return getSingleCell(commonInput, cell);
        }
        finally {
            if (file != null) {
                file.close();
            }
        }
    }

    public Object getCellByCoordinates(CommonInput commonInput, String workSheet, int column, int row) throws Exception {
        ExcelFile file = null;
        try {
            file = loader.openFile(commonInput);
            Cell cell = file.getCell(workSheet, new CellCoordinates(column, row));
            return getSingleCell(commonInput, cell);
        }
        finally {
            if (file != null) {
                file.close();
            }
        }
    }

    public FileObject getFileContents(CommonInput commonInput) throws Exception {
        ExcelFile file = null;
        try {
            file = loader.openFile(commonInput);
            FileObject fileObject = new FileObject();
            fileObject.setFileName(commonInput.fileURL());
            for (int sheetIndex = 0; sheetIndex < file.getNrOfSheets(); sheetIndex++) {
                Sheet sheet = file.getSheetByIndex(sheetIndex);
                fileObject.addWorkSheet(toSheetObject(commonInput, sheet));
            }
            return fileObject;
        }
        finally {
            if (file != null) {
                file.close();
            }
        }
    }

    public SheetObject getSheetContents(CommonInput commonInput, String workSheet) throws Exception {
        Sheet sheet = null;
        try {
            sheet = loader.openSheet(commonInput, workSheet);
            return toSheetObject(commonInput, sheet);
        }
        finally {
            if (sheet != null) {
                sheet.close();
            }
        }
    }

    public List<Map<String, Object>> getTableFromRange(CommonInput commonInput, String workSheet, String cellRange) throws Exception {
        Sheet sheet = null;
        try {
            sheet = loader.openSheet(commonInput, workSheet);
            List<List<Cell>> cellsInRange = sheet.getRange(cellRange);
            List<Map<String, Object>> rows = new ArrayList<>();
            if (cellsInRange.isEmpty()) {
                return rows;
            }
            List<Cell> headerCells = cellsInRange.get(0);
            List<String> columnNames = new ArrayList<String>(headerCells.size());
            for (int cellIndex = 0; cellIndex < headerCells.size(); cellIndex++) {
                Cell headerCell = headerCells.get(cellIndex);
                Object value = (headerCell != null) ? headerCell.getValue() : null;
                if (value == null) {
                    columnNames.add("column_" + cellIndex);
                }
                else {
                    columnNames.add(toSnakeCase(value.toString()));
                }
            }
            for (int row = 1; row < cellsInRange.size(); row++) {
                List<Cell> cells = cellsInRange.get(row);
                Map<String, Object> rowMap = new LinkedHashMap<>();
                for (int cellIndex = 0; cellIndex < columnNames.size(); cellIndex++) {
                    Cell cell = cellIndex < cells.size() ? cells.get(cellIndex) : null;
                    Object value = (cell != null) ? cell.getValue() : null;
                    if (value == null || value.toString().isBlank()) {
                        continue;
                    }
                    rowMap.put(columnNames.get(cellIndex), value);
                }
                rows.add(rowMap);
            }
            return rows;
        }
        finally {
            if (sheet != null) {
                sheet.close();
            }
        }
    }

    private Object getSingleCell(CommonInput commonInput, Cell cell) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell not found in work sheet");
        }
        CellObjectAndValue objectAndValue = toCellObjectAndValue(commonInput, cell);
        return mustCellBeAdded(commonInput, objectAndValue.value()) ? objectAndValue.object() : null;
    }

    private SheetObject toSheetObject(CommonInput input, Sheet sheet) {
        SheetObject sheetObject = new SheetObject();
        sheetObject.setName(sheet.getName());
        for (int rowIndex = 1; rowIndex <= sheet.getRows(); rowIndex++) {
            RowObject rowObject = new RowObject();
            for (int columnIndex = 1; columnIndex <= sheet.getColumns(); columnIndex++) {
                Cell cell = sheet.getCell(new CellCoordinates(columnIndex, rowIndex));
                CellObjectAndValue objectAndValue = toCellObjectAndValue(input, cell);
                if (mustCellBeAdded(input, objectAndValue.value())) {
                    rowObject.addColumn(objectAndValue.object());
                }
            }
            addRowIfNeeded(input, sheetObject, rowObject);
        }
        return sheetObject;
    }

    private CellObjectAndValue toCellObjectAndValue(CommonInput input, Cell cell) {
        Object cellValue = (cell != null) ? cell.getValue() : null;
        if (cellValue == null || cellValue.toString().isBlank()) {
            if (input.output().handleEmptyCells() == FilterEmptyCells.AS_EMPTY_STRINGS) {
                cellValue = "";
            }
        }
        Object cellObject;
        if (input.output().includeCellDetails() == IncludeCellInformationMode.VALUES_AND_METADATA) {
            CellCoordinates coordinates = new CellCoordinates(cell.getAddress());
            cellObject = new CellObject(coordinates.getAddress(), coordinates.getRow(), coordinates.getColumn(), cellValue);
        }
        else {
            cellObject = cellValue;
        }
        return new CellObjectAndValue(cellObject, cellValue);
    }

    private boolean mustCellBeAdded(CommonInput input, Object cellValue) {
        if (cellValue != null) {
            return true;
        }
        return input.output().handleEmptyCells() != FilterEmptyCells.SKIP;
    }

    private static String toSnakeCase(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        String separated = input.trim().replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        String normalized = separated.replaceAll("[^A-Za-z0-9]+", "_").toLowerCase();
        return normalized.replaceAll("^_+|_+$", "");
    }

    private void addRowIfNeeded(CommonInput input, SheetObject sheetObject, RowObject rowObject) {
        boolean containsValues = false;
        List<Object> cells = rowObject.getCells();
        if (cells != null) {
            Iterator<Object> iterator = cells.iterator();
            while (!containsValues && iterator.hasNext()) {
                Object current = iterator.next();
                if (current != null) {
                    if (current instanceof CellObject cellObject) {
                        Object value = cellObject.value();
                        if (value != null && !value.toString().isBlank()) {
                            containsValues = true;
                        }
                    }
                    else if (!current.toString().isBlank()) {
                        containsValues = true;
                    }
                }
            }
        }
        if (containsValues || input.output().handleEmptyRows() == FilterRows.ALL) {
            sheetObject.addRow(rowObject);
        }
    }

}
