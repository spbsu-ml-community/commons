package com.expleague.commons.csv;

import com.expleague.commons.io.StreamTools;
import com.expleague.commons.seq.CharSeq;
import com.expleague.commons.seq.CharSeqBuilder;
import com.expleague.commons.seq.ReaderChopper;
import com.expleague.commons.util.logging.Logger;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CsvTools {
  private static final Logger log = Logger.create(CsvTools.class);
  public static Stream<CharSeq[]> readCSV(Reader reader, boolean parallel) {
    return readCSV(reader, parallel, new char[]{'\n', ',', '"', '\''});
  }

  public static Stream<CharSeq[]> readCSV(Reader reader, boolean parallel, char[] delimiters) {
    final ReaderChopper chopper = new ReaderChopper(reader);
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new CSVLinesIterator(chopper, delimiters), Spliterator.IMMUTABLE), parallel).onClose(() -> StreamTools.close(reader));
  }

  public static void readCSVWithHeader(String file, Consumer<CsvRow> processor) {
    readCSVWithHeader(file, -1, processor);
  }

  public static void readCSVWithHeader(String file, long limit, Consumer<CsvRow> processor) {
    try {
      readCSVWithHeader(StreamTools.openTextFile(file), limit, processor);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void readCSVWithHeader(Reader reader, Consumer<CsvRow> processor) {
    readCSVWithHeader(reader, -1, processor);
  }

  public static void readCSVWithHeader(Reader reader, long limit, Consumer<CsvRow> processor) {
    final TObjectIntMap<String> names = new TObjectIntHashMap<>();
    final Stream<CharSeq[]> lines = readCSV(reader, false);
    final Spliterator<CharSeq[]> spliterator = lines.spliterator();
    spliterator.tryAdvance(header -> {
      for (int i = 0; i < header.length; i++) {
        names.put(header[i].toString(), i + 1);
      }
    });
    final Stream<CharSeq[]> slice = limit > 0 ? StreamSupport.stream(spliterator, false).limit(limit) : StreamSupport.stream(spliterator, false);

    slice.forEach(split -> {
      try {
        processor.accept(new WritableCsvRow(split, names));
      }
      catch (Exception e) {
        log.error("Unable to parse line: " + Arrays.toString(split), e);
      }
    });
  }

  public static Stream<CsvRow> csvLines(Reader reader) {
    return csvLines(reader, new char[]{'\n', ',', '"', '\''});
  }

  public static Stream<CsvRow> csvLines(Reader reader, char[] delimiters) {
    final TObjectIntMap<String> names = new TObjectIntHashMap<>();
    final Stream<CharSeq[]> lines = readCSV(reader, false, delimiters);
    final Spliterator<CharSeq[]> spliterator = lines.spliterator();
    spliterator.tryAdvance(header -> {
      for (int i = 0; i < header.length; i++) {
        names.put(header[i].toString(), i + 1);
      }
    });

    return StreamSupport.stream(spliterator, false).onClose(() -> StreamTools.close(reader)).map(line -> new WritableCsvRow(line, names));
  }

  private static class CSVLinesIterator implements Iterator<CharSeq[]> {
    private final ReaderChopper chopper;
    private final char[] delimiters;
    CharSeq[] next;
    CharSeq[] prev;
    CharSeqBuilder builder;

    public CSVLinesIterator(ReaderChopper chopper, char[] delimiters) {
      this.delimiters = delimiters;
      this.chopper = chopper;
      builder = new CharSeqBuilder();
    }

    @Override
    public boolean hasNext() {
      if (next != null)
        return true;
      try {
        next = prev != null ? prev : new CharSeq[0];
        int index = 0;
        lineRead:
        while (true) {
          final int result = chopper.chop(builder, delimiters);
          switch (result) {
            case 1:
              appendAt(index++);
              break;
            case 2:
              while (true) {
                chopper.chop(builder, '"');
                if (chopper.eat('"'))
                  builder.add('"');
                else
                  break;
              }
              break;
            case 3:
              while (true) {
                chopper.chop(builder, '\'');
                if (chopper.eat('\''))
                  builder.add('\'');
                else
                  break;
              }
              break;
            case 0: // EOL
              appendAt(index++);
              if (index < next.length) { // not enough records in this line
                index = 0;
                continue;
              }
              break lineRead;
            default: // or EOF
              if (!appendAt(index++) || index < next.length) { // maximum line is bigger then this one, skip the record
                next = null;
              }
              break lineRead;
          }
        }
        return next != null;
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    private boolean appendAt(int index) {
      CharSeq build = builder.build().trim();
      builder.clear();
      if (index >= next.length) { // expanding the line
        final CharSeq[] expand = new CharSeq[index + 1];
        System.arraycopy(next, 0, expand, 0, next.length);
        next = expand;
      }
      next[index] = build;
      return build.length() > 0;
    }

    @Override
    public CharSeq[] next() {
      this.prev = this.next;
      this.next = null;
      return this.prev;
    }
  }
}
