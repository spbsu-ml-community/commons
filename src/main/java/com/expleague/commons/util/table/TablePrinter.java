package com.expleague.commons.util.table;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qdeee
 * Date: 25.03.15
 */
public class TablePrinter {
  public static final int ALIGN_LEFT = -1;
  public static final int ALIGN_CENTER = 0;
  public static final int ALIGN_RIGHT = 1;

  public static final int DEFAULT_HEADER_ALIGN = ALIGN_CENTER;
  public static final int DEFAULT_DATA_ALIGN = ALIGN_RIGHT;

  public static void printTable(String[] header, String[][] data) {
    printTable(header, DEFAULT_HEADER_ALIGN, data, DEFAULT_DATA_ALIGN);
  }

  public static void printTable(String[] header, String[][] data, int dataAlign) {
    printTable(header, DEFAULT_HEADER_ALIGN, data, dataAlign);
  }

  public static void printTable(String[] header, int headerAlign, String[][] data, int dataAlign) {
    System.out.println(getTable(header, headerAlign, data, dataAlign));
  }

  public static String getTable(String[] header, String[][] data) {
    return getTable(header, DEFAULT_HEADER_ALIGN, data, DEFAULT_DATA_ALIGN);
  }

  public static String getTable(String[] header, String[][] data, int dataAlign) {
    return getTable(header, DEFAULT_HEADER_ALIGN, data, dataAlign);
  }

  public static String getTable(String[] header, int headerAlign, String[][] data, int dataAlign) {
    ASCIITableHeader[] headerObjs = new ASCIITableHeader[0];

    if (header != null && header.length > 0) {
      headerObjs = new ASCIITableHeader[header.length];
      for (int i = 0 ; i < header.length ; i ++) {
        headerObjs[i] = new ASCIITableHeader(header[i], dataAlign, headerAlign);
      }
    }
    return getTable(headerObjs, data);
  }

  public static void printTable(ASCIITableHeader[] headerObjs, String[][] data) {
    System.out.println(getTable(headerObjs, data));
  }

  public static String getTable(ASCIITableHeader[] headerObjs, String[][] data) {

    if (data == null || data.length == 0) {
      throw new IllegalArgumentException("Please provide valid data : " + data);
    }

    /**
     * Table String buffer
     */
    StringBuilder tableBuf = new StringBuilder();

    /**
     * Get maximum number of columns across all rows
     */
    String [] header = getHeaders(headerObjs);
    int colCount = getMaxColumns(header, data);

    /**
     * Get max length of data in each column
     */
    List<Integer> colMaxLenList = getMaxColLengths(colCount, header, data);

    /**
     * Check for the existence of header
     */
    if (header != null && header.length > 0) {
      /**
       * 1. Row line
       */
      tableBuf.append(getRowLineBuf(colCount, colMaxLenList, data));

      /**
       * 2. Header line
       */
      tableBuf.append(getRowDataBuf(colCount, colMaxLenList, header, headerObjs, true));
    }

    /**
     * 3. Data Row lines
     */
    tableBuf.append(getRowLineBuf(colCount, colMaxLenList, data));
    String [] rowData = null;

    //Build row data buffer by iterating through all rows
    for (int i = 0 ; i < data.length ; i++) {

      //Build cell data in each row
      rowData = new String [colCount];
      for (int j = 0 ; j < colCount ; j++) {

        if (j < data[i].length) {
          rowData[j] = data[i][j];
        } else {
          rowData[j] = "";
        }
      }

      tableBuf.append(getRowDataBuf(colCount, colMaxLenList, rowData, headerObjs, false));
    }

    /**
     * 4. Row line
     */
    tableBuf.append(getRowLineBuf(colCount, colMaxLenList, data));
    return tableBuf.toString();
  }

  private static String getRowDataBuf(int colCount, List<Integer> colMaxLenList,
                               String[] row, ASCIITableHeader[] headerObjs, boolean isHeader) {

    StringBuilder rowBuilder = new StringBuilder();
    String formattedData = null;
    int align;

    for (int i = 0 ; i < colCount ; i ++) {

      align = isHeader ? DEFAULT_HEADER_ALIGN : DEFAULT_DATA_ALIGN;

      if (headerObjs != null && i < headerObjs.length) {
        if (isHeader) {
          align = headerObjs[i].getHeaderAlign();
        } else {
          align = headerObjs[i].getDataAlign();
        }
      }

      formattedData = i < row.length ? row[i] : "";

      //format = "| %" + colFormat.get(i) + "s ";
      formattedData = "| " +
          getFormattedData(colMaxLenList.get(i), formattedData, align) + " ";

      if (i+1 == colCount) {
        formattedData += "|";
      }

      rowBuilder.append(formattedData);
    }

    return rowBuilder.append("\n").toString();
  }

  private static String getFormattedData(int maxLength, String data, int align) {

    if (data.length() > maxLength) {
      return data;
    }

    boolean toggle = true;

    while (data.length() < maxLength) {

      if (align == ALIGN_LEFT) {
        data = data + " ";
      } else if (align == ALIGN_RIGHT) {
        data = " " + data;
      } else if (align == ALIGN_CENTER) {
        if (toggle) {
          data = " " + data;
          toggle = false;
        } else {
          data = data + " ";
          toggle = true;
        }
      }
    }

    return data;
  }

  /**
   * Each string item rendering requires the border and a space on both sides.
   *
   * 12   3   12      3  12    34
   * +-----   +--------  +------+
   *   abc      venkat     last
   *
   * @param colCount
   * @param colMaxLenList
   * @param data
   * @return
   */
  private static String getRowLineBuf(int colCount, List<Integer> colMaxLenList, String[][] data) {

    StringBuilder rowBuilder = new StringBuilder();
    int colWidth = 0 ;

    for (int i = 0 ; i < colCount ; i ++) {

      colWidth = colMaxLenList.get(i) + 3;

      for (int j = 0; j < colWidth ; j ++) {
        if (j==0) {
          rowBuilder.append("+");
        } else if ((i+1 == colCount && j+1 == colWidth)) {//for last column close the border
          rowBuilder.append("-+");
        } else {
          rowBuilder.append("-");
        }
      }
    }

    return rowBuilder.append("\n").toString();
  }

  private static int getMaxItemLength(List<String> colData) {
    int maxLength = 0;
    for (int i = 0 ; i < colData.size() ; i ++) {
      maxLength = Math.max(colData.get(i).length(), maxLength);
    }
    return maxLength;
  }

  private static int getMaxColumns(String [] header, String[][] data) {
    int maxColumns = 0;
    for (int i = 0; i < data.length; i++) {
      maxColumns = Math.max(data[i].length, maxColumns);
    }
    maxColumns = Math.max(header.length, maxColumns);
    return maxColumns;
  }

  private static List<Integer> getMaxColLengths(int colCount, String[] header, String[][] data) {

    List<Integer> colMaxLenList = new ArrayList<Integer>(colCount);
    List<String> colData = null;
    int maxLength;

    for (int i = 0 ; i < colCount ; i ++) {
      colData = new ArrayList<String>();

      if (header != null && i < header.length) {
        colData.add(header[i]);
      }

      for (int j = 0 ; j < data.length; j ++) {
        if (i < data[j].length) {
          colData.add(data[j][i]);
        } else {
          colData.add("");
        }
      }

      maxLength = getMaxItemLength(colData);
      colMaxLenList.add(maxLength);
    }

    return colMaxLenList;
  }

  private static String [] getHeaders(ASCIITableHeader[] headerObjs) {
    String [] header = new String[0];
    if (headerObjs != null && headerObjs.length > 0) {
      header = new String[headerObjs.length];
      for (int i = 0 ; i < headerObjs.length ; i ++) {
        header[i] = headerObjs[i].getHeaderName();
      }
    }

    return header;
  }

  /**
   * Represents ASCII table header.
   *
   * @author K Venkata Sudhakar (kvenkatasudhakar@gmail.com)
   * @version 1.0
   *
   */
  public static class ASCIITableHeader {

    private String headerName;
    private int headerAlign = DEFAULT_HEADER_ALIGN;
    private int dataAlign = DEFAULT_DATA_ALIGN;

    public ASCIITableHeader(String headerName) {
      this.headerName = headerName;
    }

    public ASCIITableHeader(String headerName, int dataAlign) {
      this.headerName = headerName;
      this.dataAlign = dataAlign;
    }

    public ASCIITableHeader(String headerName, int dataAlign, int headerAlign) {
      this.headerName = headerName;
      this.dataAlign = dataAlign;
      this.headerAlign = headerAlign;
    }

    public String getHeaderName() {
      return headerName;
    }

    public void setHeaderName(String headerName) {
      this.headerName = headerName;
    }

    public int getHeaderAlign() {
      return headerAlign;
    }

    public void setHeaderAlign(int headerAlign) {
      this.headerAlign = headerAlign;
    }

    public int getDataAlign() {
      return dataAlign;
    }

    public void setDataAlign(int dataAlign) {
      this.dataAlign = dataAlign;
    }

  }


//  Example:
//  TablePrinter.ASCIITableHeader[] headerObjs = {
//      new TablePrinter.ASCIITableHeader("User Name", TablePrinter.ALIGN_LEFT),
//      new TablePrinter.ASCIITableHeader("Salary"),
//      new TablePrinter.ASCIITableHeader("Designation", TablePrinter.ALIGN_CENTER),
//      new TablePrinter.ASCIITableHeader("Address", TablePrinter.ALIGN_LEFT),
//      new TablePrinter.ASCIITableHeader("Lucky#", TablePrinter.ALIGN_RIGHT),
//  };
}
