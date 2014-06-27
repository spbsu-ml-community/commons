package com.spbsu.commons.io.persist;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.io.Buffer;
import com.spbsu.commons.io.BufferFactory;

/**
 * Created by IntelliJ IDEA.
* User: igorkuralenok
* Date: 14.10.2009
* Time: 14:39:09
* To change this template use File | Settings | File Templates.
*/
public class PageFileAddress {
  public static Converter<PageFileAddress, Buffer> CONVERTER = new Converter<PageFileAddress, Buffer>() {
    public PageFileAddress convertFrom(Buffer source) {
      final int pageNo = source.getInt();
      final short offset = source.getShort();
      final short length = source.getShort();

      return new PageFileAddress(pageNo, offset, length);
    }
    public Buffer convertTo(PageFileAddress object) {
      Buffer buf = BufferFactory.wrap(new byte[SIZE_OF]);
      buf.putInt(object.pageNo);
      buf.putShort(object.offset);
      buf.putShort(object.length);
      buf.position(0);
      return buf;
    }
  };
  public static PageFileAddress UNKNOWN = new PageFileAddress(-1, (short)-1, (short)-1);
  public static final short SIZE_OF = 8;

  private final int pageNo;
  private final short offset;
  private final short length;

  public PageFileAddress(int pageNo, short offset, short length) {
    this.pageNo = pageNo;
    this.offset = offset;
    this.length = length;
  }

  public int getPageNo(){
    return pageNo;
  }

  public short getOffset(){
    return offset;
  }

  public short getLength(){
    return length;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final PageFileAddress that = (PageFileAddress) o;
    return length == that.length && offset == that.offset && pageNo == that.pageNo;

  }

  public int hashCode() {
    int result = pageNo;
    result = 31 * result + offset;
    result = 31 * result + length;
    return result;
  }
}
