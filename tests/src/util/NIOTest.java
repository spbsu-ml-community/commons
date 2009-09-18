package util;

import com.spbsu.util.nio.BufferFactory;
import com.spbsu.util.nio.Buffer;
import junit.framework.TestCase;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 12.09.2009
 * Time: 17:27:30
 * To change this template use File | Settings | File Templates.
 */
public class NIOTest extends TestCase {
  public void testComposite0() {
    final Buffer b = BufferFactory.wrap();
    boolean caught = false;
    try {
      b.getInt();
    }
    catch (BufferUnderflowException bue) {
      caught = true;
    }
    assertTrue(caught);
  }

  public void testComposite1() {
    final Buffer b = BufferFactory.wrap(new byte[4]);
    b.putInt(48);
    b.position(0);
    assertEquals(48, b.getInt());
  }

  public void testComposite2() {
    final Buffer b = BufferFactory.wrap(ByteBuffer.wrap(new byte[2]), ByteBuffer.wrap(new byte[2]));
    b.putInt(48);
    b.position(0);
    assertEquals(48, b.getInt());
  }

  public void testComposite3() {
    final Buffer b = BufferFactory.wrap(ByteBuffer.wrap(new byte[2]), ByteBuffer.wrap(new byte[2]));
    b.putInt(48);
    b.position(0);
    assertEquals(48, b.getInt());
    assertEquals(48, b.getInt(0));
  }

  public void testComposite4() {
    final Buffer b = BufferFactory.wrap(ByteBuffer.wrap(new byte[2]), ByteBuffer.wrap(new byte[2]));
    b.putFloat(0.187236f);
    b.position(0);
    assertEquals(1044363979, b.getInt(0));
    assertEquals(0.187236f, b.getFloat());
    assertEquals(0, b.remaining());
  }

  public void testComposite5() {
    final Buffer b = BufferFactory.wrap(ByteBuffer.wrap(new byte[1]), ByteBuffer.wrap(new byte[1]));
    b.putChar('ф');
    b.position(0);
    assertEquals('ф', b.getChar());
    assertEquals(0, b.remaining());
  }
}
