package com.spbsu.commons.io.codec;

import com.spbsu.commons.io.*;


import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 32-bit precision + 64-bit overflow, so that we can output not by bits, but ints
 * User: solar
 * Date: 31.05.14
 * Time: 8:21
 */
public class ArithmeticCoding {
  public static final class Encoder {
    private final BitOutput output;
    private final int[] limits;
    private final int total;
    private boolean flushed = false;

    private long high, low;
    private long underflow = 0;

    public Encoder(final ByteBuffer out, final int[] freq) {
      this(new BitOutputBuffer(out), freq);
    }

    public Encoder(final OutputStream out, final int[] freq) {
      this(new BitOutputStream(out), freq);
    }

    public Encoder(final BitOutput output, final int[] freq) {
      this.output = output;
      limits = new int[freq.length];
      int total = 0;
      for (int i = 0; i < freq.length; i++) {
        total += freq[i] > 0 ? freq[i] : 1;
        limits[i] = total;
      }
      this.total = total;
      high = 0xFFFFFFFFl;
      low = 0;
    }


    public void write(final int symbol) {
      final long tempRange= (high-low)+1;
      final long symStart = symbol > 0 ? limits[symbol - 1] : 0;
      final long symEnd = limits[symbol];
      high=low + ((tempRange*symEnd)/total)-1;
      low	=low + ((tempRange*symStart)/total);


      while (true){
        if((high & 0x80000000)==(low & 0x80000000)) {
          final int bit = (int)((high >>> 31)) & 1;
          output.write(bit, 1);
          final int neBit = bit^1;
          while(underflow > 0) {
            output.write(neBit, 1);
            underflow--;
          }
        }
        else {
          if((low	& 0x40000000) != 0 && (high	& 0x40000000) == 0) {
            underflow++;
            low	 &=	0x3FFFFFFF;
            high |=	0x40000000;
          }
          else break;
        }

        low	=(low<<1) &	0xFFFFFFFFl;
        high=((high<<1)|1) & 0xFFFFFFFFl;
      }
    }

    public void flush(){
      if(!flushed) {
        final int bit = (int)((low >>> 30) & 1);
        output.write(bit, 1);
        final int neBit = bit^1;

        underflow++;

        while(underflow > 0) {
          output.write(neBit, 1);
          underflow--;
        }

        output.flush();
        flushed=true;
      }
    }
  }

  public static final class Decoder {
    private final BitInput input;
    private final int[] limits;
    private final int total;

    private long high, low;
    private long code;

    public Decoder(final ByteBuffer input, final int[] freq) {
      this(new BitInputBuffer(input), freq);
    }

    public Decoder(final InputStream input, final int[] freq) {
      this(new BitInputStream(input), freq);
    }

    public Decoder(final BitInput input, final int[] freqs) {
      this.input = input;
      limits = new int[freqs.length]; // all symbols + eof
      int total = 0;
      for (int i = 0; i < freqs.length; i++) {
        final int freq = freqs[i] > 0 ? freqs[i] : 1;
        total += freq;
        limits[i] = total;
      }
      code = this.input.read(31);
      code <<= 1;
      code |= this.input.read(1);
      this.total = total;
      high = 0xFFFFFFFFl;
      low = 0;
    }

    public int read() {
      final long tempRange= (high-low)+1;
      final int value = (int)(((((code-low)+1)*total)-1)/tempRange);
      final int result = Math.abs(Arrays.binarySearch(limits, value) + 1);

      final long symHigh = limits[result];
      final long symLow = result > 0 ? limits[result - 1] : 0;

      high=low+((tempRange*symHigh)/total)-1;
      low	=low+((tempRange*symLow )/total);

      while (true) {
        if ((high & 0x80000000) != (low & 0x80000000)) {
          if((low	& 0x40000000) > 0  && (high	& 0x40000000) == 0) {
            code ^=	0x40000000;
            low	 &=	0x3FFFFFFF;
            high |=	0x40000000;
          }
          else break;
        }
        low	 = (low	<< 1) &	0xFFFFFFFFl;
        high = ((high<<1) |	1) & 0xFFFFFFFFl;

        code <<=1;
        code |= input.read(1);
        code &=0xFFFFFFFFl;
      }
      return result;
    }

    long[] state;
    public void mark() {
      state = new long[]{low, high, code};
      input.mark();
    }

    public void reset() {
      low = state[0];
      high = state[1];
      code = state[2];
      input.reset();
    }
  }
}
