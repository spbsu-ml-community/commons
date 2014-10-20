package com.spbsu.commons.seq;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by vkokarev on 06.10.14.
 */
public class CharBufferSeq implements Seq<CharBuffer> {
  final CharBuffer[] buffers;
  final long[] lengthArray;
  final long offset;

  public CharBufferSeq(@NotNull final List<CharBuffer> buffers) {
    this(buffers, 0);
  }
  public CharBufferSeq(@NotNull final List<CharBuffer> buffers, final long offset) {
    this(buffers.toArray(new CharBuffer[buffers.size()]), offset);
  }
  public CharBufferSeq(@NotNull final CharBuffer[] buffers, final long offset) {
    this.buffers = Arrays.copyOf(buffers, buffers.length);
    this.lengthArray = new long[buffers.length + 1];
    Long sum = 0L;
    int i = 0;
    for (final CharBuffer buffer : buffers) {
      lengthArray[i++] = sum;
      sum += buffer.length();
    }
    lengthArray[i] = sum - offset;
    this.offset = offset;
  }

  public CharBufferSeq(final CharBufferSeq cbs, int length) {
    this(cbs.buffers, length);
  }

  public CharBufferSeq(final CharSequence charSequence) {
    this(new CharBuffer[]{CharBuffer.wrap(charSequence)}, 0);
  }


  @Override
  public String toString(){
    final StringBuilder sb = new StringBuilder((int)lengthArray[lengthArray.length - 1]);
    for (CharBuffer cb : buffers) {
      sb.append(cb.asReadOnlyBuffer());
    }
    return sb.substring((int)offset);
  }
  @Override
  public CharBuffer at(final int i) {
    return buffers[i];
  }

  @Override
  public Seq<CharBuffer> sub(int start, int end) {
    return new CharBufferSeq(Arrays.copyOfRange(buffers, start, end), offset > start ? offset - start : 0);
  }

  @Override
  public int length() {
    return buffers.length;
  }

  public long commonSize() {
    return lengthArray[lengthArray.length - 1];
  }

  @Override
  public boolean isImmutable() {
    return false;
  }

  @Override
  public Class<CharBuffer> elementType() {
    return CharBuffer.class;
  }

  public Tokenizer getTokenizer(final String delim) {
    return new Tokenizer(delim);
  }

  public Reader getReader() {
    return new CharBufferSeqReader();
  }

  public class Tokenizer implements Enumeration<CharSequence> {
    final String delim;
    int bufferId = 0;
    int bufferOffset = 0;
    long lettersPassed = 0;
    CharSeqBuilder tail;
    CharSequence storedToken;

    Tokenizer(@NotNull final String delim) {
      this.delim = delim;
      this.tail = new CharSeqBuilder();
    }

    public CharSequence nextToken() {
      return nextElement();
    }
    public CharSequence nextToken(@NotNull final String delim) {
      if (storedToken != null) {
        final CharSequence result = storedToken;
        storedToken = null;
        return result;
      }
      while (bufferId < buffers.length) {
        CharBuffer buffer = buffers[bufferId];
        int len = buffer.length();
        StringBuilder sb = new StringBuilder();
        while (bufferOffset < len) {
          final char letter = buffer.get(bufferOffset++);
          if (lettersPassed++ < offset)
            continue;
          if (delim.indexOf(letter) >= 0) {
            final CharSequence token = tail.append(sb);
            tail = new CharSeqBuilder();
            return token;
          } else {
            sb.append(letter);
          }
        }
        tail.append(sb);
        ++bufferId;
        bufferOffset = 0;
      };

      final CharSequence token = tail;
      tail = null;
      return token;
    }

    @Override
    public boolean hasMoreElements() {
      if (storedToken == null)
        storedToken = nextElement();
      return storedToken != null;
    }

    @Override
    public CharSequence nextElement() {
      return nextToken(delim);
    }
  }


  public class CharBufferSeqReader extends Reader {
    final List<Reader> readers = new ArrayList<>();
    int skipped = 0;
    int readerId = 0;
    CharBufferSeqReader() {
      for (int i = 0; i < buffers.length; ++i) {
        final ByteBuffer bb = Charset.forName("UTF-8").encode(buffers[i].asReadOnlyBuffer());
        final byte[] arr = new byte[bb.remaining()];
        bb.get(arr);
        readers.add(new InputStreamReader(new ByteArrayInputStream(arr)));
      }
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
      while (skipped < offset) {
        char[] tmp = new char[(int)offset];
        int read = fairRead(tmp, 0, (int) offset);
        if (read > 0) {
          skipped += read;
        } else {
          return -1;
        }
      }
      return fairRead(cbuf, off, len);
    }

    public int fairRead(char[] cbuf, int off, int len) throws IOException {
      int total = 0;
      while (total < len && readerId < readers.size()) {
        final Reader reader = readers.get(readerId);
        int read = reader.read(cbuf, off + total, len - total);
        if (read > 0) {
          total += read;
        } else {
          ++readerId;
        }
      }
      return total == 0 && readerId >= readers.size() ? -1 : total;
    }

    @Override
    public void close() throws IOException {

    }
  }

}
