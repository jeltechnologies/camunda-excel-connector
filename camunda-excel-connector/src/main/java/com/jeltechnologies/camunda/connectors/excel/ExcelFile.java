package com.jeltechnologies.camunda.connectors.excel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Excel workbook
 * <p>
 * Hides complexity of Excel and POI, provides read only access
 */
public class ExcelFile {
	private final Workbook workbook;
	private final String fileName;

	public ExcelFile(File file) throws ExcelException {
		workbook = init(file);
		this.fileName = file.getName();
	}

	public ExcelFile(String fileName, InputStream stream) throws ExcelException {
		workbook = init(stream);
		this.fileName = fileName;
	}

	private Workbook init(File file) throws ExcelException {
		try {
			return WorkbookFactory.create(file);
		} catch (Exception e) {
			throw new ExcelException("Cannot open Excel file because " + e.getMessage(), e);
		}
	}

	private Workbook init(InputStream inputStream) throws ExcelException {
		try {
			return WorkbookFactory.create(inputStream);
		} catch (Exception e) {
			throw new ExcelException("Cannot open Excel file because " + e.getMessage(), e);
		}
	}

	public void close() {
		try {
			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<Cell> search(String value) {
		List<Cell> cells = new ArrayList<Cell>();
		int nrOfSheets = getNrOfSheets();
		for (int i = 0; i < nrOfSheets; i++) {
			Sheet sheet = getSheetByIndex(i);
			List<Cell> cellsFoundInSheet = sheet.getCellsByValue(value);
			for (Cell cell : cellsFoundInSheet) {
				cells.add(cell);
			}
		}
		return cells;
	}

	public Cell searchFirstCell(String value) {
		Cell result;
		List<Cell> cells = search(value);
		if (!cells.isEmpty()) {
			result = cells.get(0);
		} else {
			result = null;
		}
		return result;
	}

	public int getNrOfSheets() {
		return workbook.getNumberOfSheets();
	}

	public Sheet getSheetByIndex(int index) {
		org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(index);
		return new Sheet(this, sheet);
	}

	public Sheet getSheetByName(String name) {
		return getSheetByName(name, false);
	}

	public Sheet getSheetByName(String name, boolean caseSensitive) {
		String searchName = name.trim();
		org.apache.poi.ss.usermodel.Sheet found = null;
		Iterator<org.apache.poi.ss.usermodel.Sheet> iterator = workbook.iterator();
		while (iterator.hasNext() && found == null) {
			org.apache.poi.ss.usermodel.Sheet current = iterator.next();
			String sheetName = current.getSheetName().trim();
			boolean sameSame;
			if (caseSensitive) {
				sameSame = sheetName.equals(searchName);
			} else {
				sameSame = sheetName.equalsIgnoreCase(searchName);
			}
			if (sameSame) {
				found = current;
			}
		}
		Sheet result = null;
		if (found != null) {
			result = new Sheet(this, found);
		}
		return result;
	}

	public Cell getCell(String sheetName, CellCoordinates coordinates) throws ExcelException {
		Cell result = null;
		Sheet sheet;
		if (sheetName != null && !sheetName.isBlank()) {
			sheet = getSheetByName(sheetName);
		} else {
			sheet = getSheetByIndex(0);
		}
		if (sheet != null) {
			result = sheet.getCell(coordinates);
		} else {
			throw new ExcelException("No sheet found named '" + sheetName + "'");
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ExcelFile [fileName=");
		builder.append(fileName);
		builder.append("]");
		return builder.toString();
	}

}
