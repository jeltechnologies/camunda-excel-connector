package com.jeltechnologies.camunda.connectors.excel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExcelFileTest {

    private static final String RESOURCE = "/sample-file.xlsx";
    private static final String SHEET_NAME = "Supply Chain";

    private ExcelFile excel;

    @BeforeEach
    void openWorkbook() throws Exception {
        InputStream in = getClass().getResourceAsStream(RESOURCE);
        assertNotNull(in, "Test resource " + RESOURCE + " not found on classpath");
        excel = new ExcelFile("sample-file.xlsx", in);
    }

    @AfterEach
    void closeWorkbook() {
        if (excel != null) {
            excel.close();
        }
    }

    @Test
    void readsWorkbookMetadata() {
        assertEquals(1, excel.getNrOfSheets());
        assertEquals(SHEET_NAME, excel.getSheetByIndex(0).getName());
    }

    @Test
    void looksUpSheetByNameCaseInsensitively() {
        assertNotNull(excel.getSheetByName(SHEET_NAME));
        assertNotNull(excel.getSheetByName("supply chain"), "lookup should be case-insensitive by default");
        assertNull(excel.getSheetByName("supply chain", true), "case-sensitive lookup must not match different casing");
        assertNull(excel.getSheetByName("No Such Sheet"));
    }

    @Test
    void readsHeaderRowAsStrings() {
        Sheet sheet = excel.getSheetByName(SHEET_NAME);
        assertEquals("Shipment ID", sheet.getCellByAddress("A1").getValue());
        assertEquals("Order Date", sheet.getCellByAddress("B1").getValue());
        assertEquals("Quantity", sheet.getCellByAddress("E1").getValue());
        assertEquals("On-Time Delivery", sheet.getCellByAddress("K1").getValue());
    }

    @Test
    void readsStringDataCell() {
        Sheet sheet = excel.getSheetByName(SHEET_NAME);
        assertEquals("SHP-40001", sheet.getCellByAddress("A2").getValue());
        assertEquals("Frontier Chemical Supply", sheet.getCellByAddress("C2").getValue());
        assertEquals("No", sheet.getCellByAddress("K2").getValue());
        assertEquals("Yes", sheet.getCellByAddress("K3").getValue());
    }

    @Test
    void readsNumericCellsAsDoubles() {
        Sheet sheet = excel.getSheetByName(SHEET_NAME);
        Object quantity = sheet.getCellByAddress("E2").getValue();
        assertInstanceOf(Double.class, quantity);
        assertEquals(2968.0d, (Double) quantity, 0.0001d);

        Object totalCost = sheet.getCellByAddress("F2").getValue();
        assertInstanceOf(Double.class, totalCost);
        assertEquals(771.78d, (Double) totalCost, 0.0001d);
    }

    @Test
    void readsDateFormattedCellAsLocalDateTime() {
        Sheet sheet = excel.getSheetByName(SHEET_NAME);
        Object orderDate = sheet.getCellByAddress("B2").getValue();
        assertInstanceOf(LocalDateTime.class, orderDate, "date-formatted numeric cell should become a LocalDateTime");
        assertEquals(LocalDateTime.of(2024, 7, 6, 0, 0), orderDate);
    }

    @Test
    void resolvesCellByNumericCoordinates() throws Exception {
        Sheet sheet = excel.getSheetByName(SHEET_NAME);
        // Column 1, row 2 == "A2"
        Cell byCoordinates = sheet.getCell(1, 2);
        assertNotNull(byCoordinates);
        assertEquals("A2", byCoordinates.getAddress());
        assertEquals("SHP-40001", byCoordinates.getValue());
        // Same cell via the workbook-level accessor
        Cell byWorkbook = excel.getCell(SHEET_NAME, new CellCoordinates("A2"));
        assertEquals(byCoordinates.getValue(), byWorkbook.getValue());
    }

    @Test
    void searchFindsFirstMatchingCell() {
        Cell found = excel.searchFirstCell("SHP-40001");
        assertNotNull(found);
        assertEquals("A2", found.getAddress());
        assertEquals(SHEET_NAME, found.getSheet().getName());
        assertNull(excel.searchFirstCell("value-that-does-not-exist"));
    }

    @Test
    void hasOneHeaderRowPlusOneThousandDataRows() {
        Sheet sheet = excel.getSheetByName(SHEET_NAME);
        // Last populated address is K1001; the sheet caches every cell by address.
        assertNotNull(sheet.getCellByAddress("K1001"));
        assertTrue(sheet.getNumberOfCellsByValue("SHP-40001") >= 1);
    }
}
