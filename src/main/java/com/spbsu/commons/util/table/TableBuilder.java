package com.spbsu.commons.util.table;

import java.util.ArrayList;
import java.util.List;

/**
* User: qdeee
* Date: 29.03.15
*/
public class TableBuilder {
  private final List<String[]> rows;
  private String[] header;

  public TableBuilder(final String... header) {
    this();
    this.header = header;
  }

  public TableBuilder() {
    rows = new ArrayList<>();
  }

  public TableBuilder setHeader(final String description, final int... values) {
    header = new String[values.length + 1];
    header[0] = description;
    for (int i = 0; i < values.length; i++) {
      header[1 + i] = String.valueOf(values[i]);
    }
    return this;
  }

  public TableBuilder setHeader(final String description, final String... values) {
    header = new String[values.length + 1];
    header[0] = description;
    for (int i = 0; i < values.length; i++) {
      header[1 + i] = values[i];
    }
    return this;
  }

  public TableBuilder addRow(final String description, final double... values) {
    final String[] row = new String[values.length + 1];
    row[0] = description;
    for (int i = 0; i < values.length; i++) {
      row[1 + i] = String.format("%.6f", values[i]);
    }
    rows.add(row);
    return this;
  }

  public TableBuilder addRow(final String... row) {
    rows.add(row);
    return this;
  }

  public String build() {
    return TablePrinter.getTable(header, rows.toArray(new String[][]{new String[0]}), TablePrinter.ALIGN_LEFT);
  }
}
