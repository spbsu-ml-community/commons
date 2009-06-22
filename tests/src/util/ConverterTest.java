package util;

import com.spbsu.util.converters.ConverterUtil;
import junit.framework.TestCase;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 22.06.2009
 * Time: 12:07:30
 * To change this template use File | Settings | File Templates.
 */
public class ConverterTest extends TestCase {
  public void testSizeConvertion() {
    int[] sizes = new int[]{0, 1, 100, 10000, 299999, 17000000};
    for (int size : sizes) {
      ByteBuffer buffer = ByteBuffer.allocate(5);
      ConverterUtil.storeSize(size, buffer);
      buffer.rewind();
      assertEquals(size, ConverterUtil.restoreSize(buffer));
    }
  }

}
