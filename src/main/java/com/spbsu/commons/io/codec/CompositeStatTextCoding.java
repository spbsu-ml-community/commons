package com.spbsu.commons.io.codec;

import com.spbsu.commons.seq.DictExpansion;
import com.spbsu.commons.seq.ListDictionary;

import java.nio.ByteBuffer;
import java.util.Collection;

/**
 * User: solar
 * Date: 03.06.14
 * Time: 10:33
 */
public class CompositeStatTextCoding {
  private final DictExpansion expansion;
  private boolean stop = false;

  public CompositeStatTextCoding(Collection<Character> alphabet, int dictSize) {
    this.expansion = new DictExpansion(alphabet, dictSize);
  }

  public void accept(CharSequence seq) {
    if (!stop)
      expansion.accept(seq);
    else throw new RuntimeException("Expansion is not supported after encode/decode routine called");
  }

  public DictExpansion expansion() {
    return expansion;
  }

  public class Encode {
    public ArithmeticCoding.Encoder output;
    private final ListDictionary dict;

    public Encode(ByteBuffer output) {
      this.output = new ArithmeticCoding.Encoder(output, expansion.resultFreqs());
      this.dict = expansion.result();
      stop = true;
    }

    public void write(CharSequence suffix) {
      while(suffix.length() > 0) {
        final int symbol = dict.search(suffix);
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
    private final ListDictionary dict;

    public Decode(ByteBuffer input) {
      this.input = new ArithmeticCoding.Decoder(input, expansion.resultFreqs());
      this.dict = expansion.result();
      stop = true;
    }

    public CharSequence read() {
      int symbol;
      StringBuilder builder = new StringBuilder();
      while ((symbol = input.read()) != 0) {
        builder.append(dict.get(symbol));
      }
      return builder;
    }
  }
}
