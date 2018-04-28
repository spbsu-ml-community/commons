package com.expleague.commons.io.codec;

import com.expleague.commons.io.codec.seq.DictExpansion;
import com.expleague.commons.io.codec.seq.Dictionary;
import com.expleague.commons.seq.CharSeq;
import gnu.trove.list.array.TIntArrayList;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Collection;

/**
 * User: solar
 * Date: 03.06.14
 * Time: 10:33
 */
public class CompositeStatTextCoding {
  private final DictExpansion<Character> expansion;
  private boolean stop = false;

  public CompositeStatTextCoding(final Collection<Character> alphabet, final int dictSize) {
    this.expansion = new DictExpansion<>(alphabet, dictSize, System.out);
  }

  public CompositeStatTextCoding(int slots) {
    this.expansion = new DictExpansion<>(slots);
  }

  public CompositeStatTextCoding(int size, PrintStream trace) {
    this.expansion = new DictExpansion<>(size, trace);
  }

  public void accept(final CharSequence seq) {
    if (!stop)
      expansion.accept(CharSeq.create(seq));
    else throw new RuntimeException("Expansion is not supported after encode/decode routine called");
  }

  public DictExpansion<Character> expansion() {
    return expansion;
  }

  public class Encode {
    public ArithmeticCoding.Encoder output;
    private final Dictionary<Character> dict;
    private TIntArrayList freqs;
    private double totalFreq;

    public Encode(final ByteBuffer output) {
      freqs = new TIntArrayList(expansion.resultFreqs());
      totalFreq = freqs.sum();
      this.output = new ArithmeticCoding.Encoder(output, expansion.resultFreqs());
      this.dict = expansion.result();
      stop = true;
    }

    public Encode(final OutputStream output) {
      this.output = new ArithmeticCoding.Encoder(output, expansion.resultFreqs());
      this.dict = expansion.result();
      stop = true;
    }

    public void write(CharSequence suffix) {
      dict.parse(CharSeq.create(suffix), freqs, totalFreq).stream().forEach(output::write);
    }

    public void flush() {
      output.flush();
    }
  }

  public class Decode {
    private final ArithmeticCoding.Decoder input;
    private final Dictionary dict;

    public Decode(final ByteBuffer input) {
      this.input = new ArithmeticCoding.Decoder(input, expansion.resultFreqs());
      this.dict = expansion.result();
      stop = true;
    }

    public CharSequence read() {
      int symbol;
      final StringBuilder builder = new StringBuilder();
      while ((symbol = input.read()) != 0) {
        builder.append(dict.condition(symbol));
      }
      return builder;
    }
  }
}
