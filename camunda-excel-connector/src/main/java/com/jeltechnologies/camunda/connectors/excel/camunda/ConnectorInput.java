package com.jeltechnologies.camunda.connectors.excel.camunda;

import io.camunda.connector.api.document.Document;
import io.camunda.connector.generator.dsl.Property;
import io.camunda.connector.generator.java.annotation.DropdownItem;
import io.camunda.connector.generator.java.annotation.NestedProperties;
import io.camunda.connector.generator.java.annotation.TemplateProperty;

enum Operation {
    @DropdownItem(order = 1, label = "Get table, using first row as header" )
    getTableFromRange,
    @DropdownItem(order = 2, label = "Get file contents")
    getFileContents,
    @DropdownItem(order = 3, label = "Get sheet contents")
    getSheetContents,
    @DropdownItem(order = 4, label = "Get cell by address")
    gellCellValueByAddress,
    @DropdownItem(order = 5, label = "Get cell by coordinates")
    getCellValueByCoordinates
}

enum FileSource {
    @DropdownItem(order = 1, label = "Camunda Document Reference")
    DOCUMENT,
    @DropdownItem(order = 2, label = "URL")
    URL
}

public record ConnectorInput(

        @TemplateProperty(
                label = "Operation",
                description = "Select the Excel operation to perform",
                group = "operation",
                feel = Property.FeelMode.disabled,
                defaultValue = "getTableFromRange",
                constraints = @TemplateProperty.PropertyConstraints(notEmpty = true))
        Operation operation,

        @TemplateProperty(
                label = "File source",
                description = "Where to load the Excel file from",
                group = "file",
                feel = Property.FeelMode.disabled,
                defaultValue = "URL",
                constraints = @TemplateProperty.PropertyConstraints(notEmpty = true))
        FileSource fileSource,

        @TemplateProperty(
                label = "File URL",
                description = "URL of the Excel file to fetch (e.g. http://host/file.xlsx)",
                group = "file",
                feel = Property.FeelMode.optional,
                optional = true,
                condition = @TemplateProperty.PropertyCondition(
                        property = "fileSource",
                        equals = "URL"))
        String fileURL,

        @TemplateProperty(
                label = "Document reference",
                description = "FEEL expression resolving to a Camunda document (e.g. =myDocument)",
                group = "file",
                type = TemplateProperty.PropertyType.String,
                feel = Property.FeelMode.required,
                optional = true,
                condition = @TemplateProperty.PropertyCondition(
                        property = "fileSource",
                        equals = "DOCUMENT"))
        Document document,

        @NestedProperties(
                addNestedPath = false,
                condition = @TemplateProperty.PropertyCondition(
                        property = "operation",
                        oneOf = {"gellCellValueByAddress", "getCellValueByCoordinates", "getSheetContents"}))
        OutputSettings output,

        @TemplateProperty(
                label = "Sheet Name",
                description = "Sheet name (leave empty to use the first sheet)",
                group = "operation",
                feel = Property.FeelMode.optional,
                optional = true,
                condition = @TemplateProperty.PropertyCondition(
                        property = "operation",
                        oneOf = {"gellCellValueByAddress", "getCellValueByCoordinates", "getSheetContents", "getTableFromRange"}))
        String workSheet,

        @TemplateProperty(
                label = "Cell Address",
                description = "Excel cell address, e.g. A1",
                group = "operation",
                feel = Property.FeelMode.optional,
                condition = @TemplateProperty.PropertyCondition(
                        property = "operation",
                        equals = "gellCellValueByAddress"))
        String cellAddress,

        @TemplateProperty(
                label = "Range",
                description = "Excel range, e.g. A1:E10.",
                group = "operation",
                feel = Property.FeelMode.optional,
                condition = @TemplateProperty.PropertyCondition(
                        property = "operation",
                        equals = "getTableFromRange"),
                constraints = @TemplateProperty.PropertyConstraints(notEmpty = true))
        String cellRange,

        @TemplateProperty(
                label = "Column",
                description = "Column number (1-based)",
                group = "operation",
                feel = Property.FeelMode.optional,
                condition = @TemplateProperty.PropertyCondition(
                        property = "operation",
                        equals = "getCellValueByCoordinates"))
        Integer column,

        @TemplateProperty(
                label = "Row",
                description = "Row number (1-based)",
                group = "operation",
                feel = Property.FeelMode.optional,
                condition = @TemplateProperty.PropertyCondition(
                        property = "operation",
                        equals = "getCellValueByCoordinates"))
        Integer row) {
}
