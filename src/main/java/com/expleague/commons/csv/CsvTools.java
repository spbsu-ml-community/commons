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
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("unused")
public class CsvTools {
  private static final Logger log = Logger.create(CsvTools.class);
  public static Stream<CharSeq[]> readCSV(Reader reader, boolean parallel) {
    return readCSV(reader, parallel, ',', '"', '"', true);
  }

  public static Stream<CharSeq[]> readCSV(Reader reader, boolean parallel, char separator, char quote, char escape, boolean skipErrors) {
    final ReaderChopper chopper = new ReaderChopper(reader);
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
        new CSVLinesIterator(chopper, quote, separator, escape, skipErrors),
        Spliterator.IMMUTABLE),
        parallel
    ).onClose(() -> StreamTools.close(reader));
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
    return csvLines(reader, ',', '"', '"', true);
  }

  public static Stream<CsvRow> csvLines(Reader reader, char separator, char quote, char escape, boolean skipErrors) {
    final TObjectIntMap<String> names = new TObjectIntHashMap<>();
    final Stream<CharSeq[]> lines = readCSV(reader, false, separator, quote, escape, skipErrors);
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
    private final BitSet mask;
    private CharSeq[] next;
    private CharSeq[] prev;
    private CharSeqBuilder builder;
    private final char fieldQuote;
    private final char fieldSeparator;
    private final char escape;
    private final boolean skipErrors;

    public CSVLinesIterator(ReaderChopper chopper, char quote, char separator, char escape, boolean skipErrors) {
      this.mask = new BitSet(Character.MAX_VALUE);
      IntStream.of(this.fieldQuote = quote, this.fieldSeparator = separator, this.escape = escape, '\n').forEach(mask::set);
      this.skipErrors = skipErrors;
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
        while (true) {
          final int result = chopper.chop(builder, mask);
          if (result == fieldQuote) {
            while (true) {
              final int ch = chopper.chop(builder, mask);
              if (ch == fieldQuote) {
                if (chopper.eat(fieldQuote))
                  builder.add(fieldQuote);
                else
                  break;
              }
              else if (ch == escape) {
                final char next = chopper.next();
                if (next > 0)
                  builder.append(next);
                else
                  return false;
              }
              else builder.append((char)ch);
            }
          }
          else if (result == fieldSeparator) {
            appendAt(index++);
          }
          else if (result == escape) {
              builder.append((char)result);
          }
          else {
            if (builder.length() > 0)
              appendAt(index++);

            if (index > 0 && index < next.length - 1) { // not enough records in this line, skip it
              if (skipErrors) {
                index = 0;
                continue;
              }
              else throw new RuntimeException("");
            }
            else if (index < next.length)
              appendAt(index);
            return result == '\n';
          }
        }
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    private void appendAt(int index) {
      CharSeq build = builder.build().trim();
      builder.clear();
      if (index >= next.length) { // expanding the line
        final CharSeq[] expand = new CharSeq[index + 1];
        System.arraycopy(next, 0, expand, 0, next.length);
        next = expand;
      }
      next[index] = build;
//      return build.length() > 0;
    }

    @Override
    public CharSeq[] next() {
      this.prev = this.next;
      this.next = null;
      return this.prev;
    }
  }
}
