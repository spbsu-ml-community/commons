package com.expleague.commons.text.charset.bigram.tools;

import com.expleague.commons.io.StreamTools;
import com.expleague.commons.text.charset.bigram.BigramsTable;
import com.expleague.commons.text.charset.bigram.BigramsTextAnalyzer;
import com.expleague.commons.text.charset.bigram.CharFilter;

import java.io.*;

/**
 * @author lyadzhin
 */
public class BigramTablesCollector {
  public static void main(final String[] args) {
    if (args.length < 2) {
      System.out.println("Usage: BigramTablesCollector source_file_name output_file_name");
      return;
    }
    final String inFile = args[0];
    final String outFile = args[1];
    try {
      final CharSequence inputText = StreamTools.readFile(new File(inFile));
      writeTableToFile(getTextAnalyzer().buildBigramsTable(inputText), outFile);
    } catch (IOException e) {
      System.err.println(e);
    }
  }

  private static BigramsTextAnalyzer getTextAnalyzer() {
    return new BigramsTextAnalyzer().setCharFilter(CharFilter.NOT_ASCII_FILTER);
  }

  private static void writeTableToFile(final BigramsTable table, final String outFile) {
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
