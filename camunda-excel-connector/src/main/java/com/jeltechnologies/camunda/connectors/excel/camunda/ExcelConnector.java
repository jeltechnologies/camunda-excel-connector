package com.jeltechnologies.camunda.connectors.excel.camunda;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.generator.java.annotation.ElementTemplate;

@OutboundConnector(name = "Excel Connector", type = "io.camunda:excel:1")

@ElementTemplate(
  id = "io.camunda.excel-connector:1", 
  name = "Excel Connector", description = "Read Excel files",
  icon = "excel-connector.svg",
  documentationRef = "https://your-docs-url.com",
  version = 17, 
  inputDataClass = ConnectorInput.class, propertyGroups = {
        @ElementTemplate.PropertyGroup(
                id = "file", 
                label = "File Settings"), 
         @ElementTemplate.PropertyGroup(id = "operation", 
                label = "Operation"), 
         @ElementTemplate.PropertyGroup(id = "output", 
             label = "Output Settings")
})

public class ExcelConnector implements OutboundConnectorFunction {

    public record InputForCellByAddress(String workSheet, String cellAddres) {
    }

    public record InputForCellByCoordinates(String workSheet, int column, int row) {
    }

    public record InputForSheet(String workSheet) {
    }

    public record Output(Object value) {
    }

    private final ExcelReader reader = new ExcelReader();

    @Override
    public Object execute(OutboundConnectorContext context) throws Exception {
        ConnectorInput input = context.bindVariables(ConnectorInput.class);
        if (input.operation() == null) {
            throw new ConnectorException("EXCEL_CONNECTOR_ERROR", "Operation is required. Please select an operation in the element template.");
        }
        FileSource source = input.fileSource() != null ? input.fileSource() : FileSource.URL;
        CommonInput common = new CommonInput(source, input.fileURL(), input.document(), input.output());
        try {
            return switch (input.operation()) {
            case gellCellValueByAddress -> reader.getCellByAddress(common, input.workSheet(), input.cellAddress());
            case getCellValueByCoordinates -> reader.getCellByCoordinates(common, input.workSheet(), input.column(), input.row());
            case getSheetContents -> reader.getSheetContents(common, input.workSheet());
            case getFileContents -> reader.getFileContents(common);
            case getTableFromRange -> reader.getTableFromRange(common, input.workSheet(), input.cellRange());
            };
        }
        catch (Exception e) {
            throw new ConnectorException("EXCEL_CONNECTOR_ERROR", e.getMessage());
        }
    }

}
