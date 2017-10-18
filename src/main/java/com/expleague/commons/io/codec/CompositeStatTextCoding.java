package com.expleague.commons.io.codec;

import com.expleague.commons.io.codec.seq.DictExpansion;
import com.expleague.commons.io.codec.seq.Dictionary;
import com.expleague.commons.seq.CharSeq;

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
    this.expansion = new DictExpansion<>(alphabet, dictSize, true);
  }

  public CompositeStatTextCoding(int slots) {
    this.expansion = new DictExpansion<Character>(slots);
  }

  public CompositeStatTextCoding(int size, PrintStream trace) {
    this.expansion = new DictExpansion<Character>(size, trace);
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

    public Encode(final ByteBuffer output) {
      this.output = new ArithmeticCoding.Encoder(output, expansion.resultFreqs());
      this.dict = expansion.result();
      stop = true;
    }

    public void write(CharSequence suffix) {
      while(suffix.length() > 0) {
        final int symbol = dict.search(CharSeq.create(suffix));
        suffix = suffix.subSequence(dict.get(symbol).length(), suffix.length());
        output.write(symbol);
      }
      output.write(0);
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
        builder.append(dict.get(symbol));
      }
      return builder;
    }
  }
}
