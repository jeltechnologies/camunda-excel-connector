package com.jeltechnologies.camunda.connectors.excel.camunda;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.jeltechnologies.camunda.connectors.excel.ExcelException;
import com.jeltechnologies.camunda.connectors.excel.ExcelFile;
import com.jeltechnologies.camunda.connectors.excel.Sheet;

import io.camunda.connector.api.document.Document;
import io.camunda.connector.api.document.DocumentMetadata;

public class ExcelFileLoader {

    public ExcelFile openFile(CommonInput input) throws IOException, ExcelException {
        FileSource source = input.fileSource() != null ? input.fileSource() : FileSource.URL;
        return switch (source) {
            case URL -> openFromUrl(input.fileURL());
            case DOCUMENT -> openFromDocument(input.document());
        };
    }

    public Sheet openSheet(CommonInput input, String workSheet) throws IOException, ExcelException {
        ExcelFile excelFile = openFile(input);
        if (workSheet == null || workSheet.isBlank()) {
            return excelFile.getSheetByIndex(0);
        }
        final boolean caseSensitive = true;
        Sheet result = excelFile.getSheetByName(workSheet.trim(), caseSensitive);
        if (result == null) {
            throw new ExcelException("No such sheet " + workSheet + " in " + excelFile.toString());
        }
        return result;
    }

    private ExcelFile openFromUrl(String fileURL) throws IOException, ExcelException {
        if (fileURL == null || fileURL.isBlank()) {
            throw new ExcelException("File URL is required when file source is URL");
        }
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(fileURL)).GET().build();
        HttpResponse<InputStream> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("HTTP request interrupted for URL: " + fileURL, e);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("HTTP GET failed with status " + response.statusCode() + " for URL: " + fileURL);
        }
        String fileName = fileURL.substring(fileURL.lastIndexOf('/') + 1);
        return new ExcelFile(fileName, response.body());
    }

    private ExcelFile openFromDocument(Document document) throws ExcelException {
        if (document == null) {
            throw new ExcelException("Document reference is required when file source is DOCUMENT");
        }
        String fileName = fileNameOf(document);
        InputStream stream = document.asInputStream();
        if (stream == null) {
            throw new ExcelException("Document " + fileName + " has no content");
        }
        return new ExcelFile(fileName, stream);
    }

    private String fileNameOf(Document document) {
        DocumentMetadata metadata = document.metadata();
        if (metadata != null && metadata.getFileName() != null && !metadata.getFileName().isBlank()) {
            return metadata.getFileName();
        }
        return "document.xlsx";
    }

}
