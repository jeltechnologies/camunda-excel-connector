package com.jeltechnologies.camunda.connectors.excel;

import java.io.Serializable;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Cell implements Serializable {
    private static final long serialVersionUID = 739122025756899208L;
    private transient final org.apache.poi.ss.usermodel.Cell poiCell;
    private final Sheet sheet;
    private final CellCoordinates coordinates;

    public Cell(Sheet sheet, org.apache.poi.ss.usermodel.Cell poiCell) {
        this.poiCell = poiCell;
        this.sheet = sheet;
        String address = poiCell.getAddress().formatAsString();
        this.coordinates = new CellCoordinates(address);
    }

    public String getAddress() {
        return coordinates.getAddress();
    }

    public int getColumn() {
        return coordinates.getColumn();
    }

    public int getRow() {
        return coordinates.getRow();
    }

    public Sheet getSheet() {
        return sheet;
    }

    public Object getValue() {
        Object result;
        switch (poiCell.getCellType()) {
            case BOOLEAN:
                result = poiCell.getBooleanCellValue();
                break;
            case STRING:
                result = poiCell.getRichStringCellValue().getString();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(poiCell)) {
                    result = poiCell.getLocalDateTimeCellValue();
                } else {
                    result = poiCell.getNumericCellValue();
                }
                break;
            case FORMULA:
                FormulaEvaluator evaluator = poiCell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                CellType formulaResultType = evaluator.evaluate(poiCell).getCellType();
                switch (formulaResultType) {
                    case ERROR:
                        result = "ERROR IN FORMULA";
                        break;
                    case NUMERIC:
                        result = poiCell.getNumericCellValue();
                        break;
                    case STRING:
                        result = poiCell.getStringCellValue();
                        break;
                    default:
                        result = poiCell.getCellFormula();
                        break;
                }
                break;
            case BLANK:
                result = "";
                break;
            default:
                result = "";
        }
        return result;
    }
    
    public void addCellToJson(ObjectNode node, String fieldName) {
        FormulaEvaluator evaluator = poiCell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        CellType type = poiCell.getCellType();
        if (type == CellType.FORMULA) {
            type = evaluator.evaluate(poiCell).getCellType(); 
        }
        switch (type) {
            case STRING:
                node.put(fieldName, poiCell.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(poiCell)) {
                    node.put(fieldName, poiCell.getLocalDateTimeCellValue().toString());
                } else {
                    double val = poiCell.getNumericCellValue();
                    if (val == Math.floor(val) && !Double.isInfinite(val)) {
                        node.put(fieldName, (long) val); 
                    } else {
                        node.put(fieldName, val);      
                    }
                }
                break;
            case BOOLEAN:
                node.put(fieldName, poiCell.getBooleanCellValue());
                break;
            case BLANK:
            case _NONE:
            default:
                node.putNull(fieldName);
                break;
        }
    }

    public CellCoordinates getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(coordinates.getAddress()).append(":'").append(getValue()).append("' sheet '").append(sheet.getName());
        return builder.toString();
    }

}
