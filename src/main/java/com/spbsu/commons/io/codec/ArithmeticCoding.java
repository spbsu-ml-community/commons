package com.spbsu.commons.io.codec;

import com.spbsu.commons.io.BitInput;
import com.spbsu.commons.io.BitOutput;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 32-bit precision + 64-bit overflow, so that we can output not by bits, but ints
 * User: solar
 * Date: 31.05.14
 * Time: 8:21
 */
public class ArithmeticCoding {
  public final static class Encoder {
    private final BitOutput output;
    private final int[] limits;
    private final int total;
    private boolean flushed = false;

    private long high, low;
    private long underflow = 0;

    public Encoder(ByteBuffer output, int[] freq) {
      this.output = new BitOutput(output);
      limits = new int[freq.length];
      int total = 0;
      for (int i = 0; i < freq.length; i++) {
//        if (freq[i] <= 0)
//          throw new IllegalArgumentException("Frequencies must be >0! At " + i + " found " + freq[i]);
        total += freq[i] > 0 ? freq[i] : 1;
        limits[i] = total;
      }
      this.total = total;
      high = 0xFFFFFFFFl;
      low = 0;
    }


    public void write(int symbol) {
      long tempRange= (high-low)+1;
      long symStart = symbol > 0 ? limits[symbol - 1] : 0;
      long symEnd = limits[symbol];
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

  public final static class Decoder {
    private final BitInput input;
    private final int[] limits;
    private final int total;

    private long high, low;
    private long code;

    public Decoder(ByteBuffer input, int[] freq) {
      this.input = new BitInput(input);
      limits = new int[freq.length]; // all symbols + eof
      int total = 0;
      for (int i = 0; i < freq.length; i++) {
        if (freq[i] <= 0)
          throw new IllegalArgumentException("Frequencies must be >0! At " + i + " found " + freq[i]);
        total += freq[i];
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
      long tempRange= (high-low)+1;
      int value = (int)(((((code-low)+1)*total)-1)/tempRange);
      int result = Math.abs(Arrays.binarySearch(limits, value) + 1);

      long symHigh = limits[result];
      long symLow = result > 0 ? limits[result - 1] : 0;

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
