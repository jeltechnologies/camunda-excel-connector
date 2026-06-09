package com.jeltechnologies.camunda.connectors.excel.camunda;

import io.camunda.connector.api.document.Document;
import io.camunda.connector.generator.dsl.Property;
import io.camunda.connector.generator.java.annotation.DropdownItem;
import io.camunda.connector.generator.java.annotation.TemplateProperty;

enum IncludeCellInformationMode {
    @DropdownItem(order = 1, label = "Only values")
    VALUES,
    @DropdownItem(order = 2, label = "Values and cell address data")
    VALUES_AND_METADATA,
}

enum FilterEmptyCells {
    @DropdownItem(order = 2, label = "Returned as null value")
    AS_NULL,
    @DropdownItem(order = 1, label = "Returned as empty string")
    AS_EMPTY_STRINGS,
    @DropdownItem(order = 2, label = "Not returned")
    SKIP,
}

enum FilterRows {
    @DropdownItem(order = 1, label = "Only rows that contain cells with values")
    SKIP,
    @DropdownItem(order = 2, label = "All rows")
    ALL,
}

record OutputSettings(

        @TemplateProperty(
                label = "Cell info",
                description = "Include how much cell information should be exported",
                group = "output",
                feel = Property.FeelMode.optional,
                constraints = @TemplateProperty.PropertyConstraints(notEmpty = true))
        IncludeCellInformationMode includeCellDetails,

        @TemplateProperty(
                label = "Handling of empty cells",
                description = "Get all cells, or just cells with values",
                group = "output",
                feel = Property.FeelMode.optional,
                constraints = @TemplateProperty.PropertyConstraints(notEmpty = true))
        FilterEmptyCells handleEmptyCells,

        @TemplateProperty(
                label = "Row included",
                description = "Get all rows, or just rows with values",
                group = "output",
                feel = Property.FeelMode.optional,
                constraints = @TemplateProperty.PropertyConstraints(notEmpty = true))
        FilterRows handleEmptyRows) {
}

public record CommonInput(FileSource fileSource, String fileURL, Document document, OutputSettings output) {
}
