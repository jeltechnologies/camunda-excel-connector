package com.jeltechnologies.camunda.connectors.excel;

import java.io.Serializable;
import java.util.Objects;

public class CellCoordinates implements Serializable {
    private static final long serialVersionUID = 6439727960903053718L;
    private final int column;
    private final int row;
    private final String address;

    public CellCoordinates(int column, int row) {
        this.row = row;
        this.column = column;
        StringBuilder letters = new StringBuilder();
        int col = column;
        while (col > 0) {
            col--;
            letters.insert(0, (char) ('A' + col % 26));
            col /= 26;
        }
        address = letters + Integer.toString(row);
    }

    public CellCoordinates(String address) {
        this.address = address;
        int i = 0;
        int column = 0;
        while (i < address.length() && Character.isLetter(address.charAt(i))) {
            column = column * 26 + (Character.toUpperCase(address.charAt(i)) - 'A' + 1);
            i++;
        }
        this.column = column;
        this.row = Integer.parseInt(address.substring(i));
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public int hashCode() {
        return Objects.hash(column, address, row);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CellCoordinates other = (CellCoordinates) obj;
        return column == other.column && Objects.equals(address, other.address) && row == other.row;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CellCoordinates [address=");
        builder.append(address);
        builder.append(", column=");
        builder.append(column);
        builder.append(", row=");
        builder.append(row);
        builder.append("]");
        return builder.toString();
    }



}
