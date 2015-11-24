package com.spbsu.commons;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * User: solar
 * Date: 24.11.15
 * Time: 16:15
 */
public class JUnitIOCapture {
  private final ByteArrayOutputStream out = new ByteArrayOutputStream();
  private PrintStream oldOut;
  private final ByteArrayOutputStream err = new ByteArrayOutputStream();
  private PrintStream oldErr;
  @Before
  public void ioCapture() {
    out.reset();
    oldOut = System.out;
    System.setOut(new PrintStream(out));
    err.reset();
    oldErr = System.err;
    System.setErr(new PrintStream(err));
  }

  @After
  public void ioClear() {
    System.setOut(oldOut);
    System.setErr(oldErr);
  }

  @Rule
  public final TestWatcher watcher = new TestWatcher() {
    @Override
    protected void failed(Throwable e, Description description) {
      super.failed(e, description);
      oldOut.append(out.toString());
      oldErr.append(err.toString());
    }
  };
}
