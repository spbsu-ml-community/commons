package com.spbsu.util.charset.bigram.tools;

import com.spbsu.util.charset.bigram.BigramsTable;
import com.spbsu.util.charset.bigram.BigramsTextAnalyzer;
import com.spbsu.util.charset.bigram.CharFilter;
import com.spbsu.util.StreamUtil;

import java.io.*;

/**
 * @author lyadzhin
 */
public class BigramTablesCollector {
  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Usage: BigramTablesCollector source_file_name output_file_name");
      return;
    }
    final String inFile = args[0];
    final String outFile = args[1];
    try {
      final CharSequence inputText = StreamUtil.readFile(new File(inFile));
      writeTableToFile(getTextAnalyzer().buildBigramsTable(inputText), outFile);
    } catch (IOException e) {
      System.err.println(e);
    }
  }

  private static BigramsTextAnalyzer getTextAnalyzer() {
    return new BigramsTextAnalyzer().setCharFilter(CharFilter.NOT_ASCII_FILTER);
  }

  private static void writeTableToFile(BigramsTable table, String outFile) {
    OutputStream outputStream = null;
    try {
      outputStream = new FileOutputStream(outFile);
      table.toProperties().store(outputStream, "");
    } catch (IOException e) {
      System.err.print("Output file " + outFile + " can't be written");
    } finally {
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (IOException e) {
          //skip
        }
      }
    }
  }
}
