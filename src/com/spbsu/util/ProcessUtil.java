package com.spbsu.util;

import java.io.*;
import java.util.ArrayList;

/**
 * We need to read process output to prevent process blocking.
 * See issue decsription at http://forums.java.net/jive/thread.jspa?threadID=17034&tstart=0
 * @author vp
 */
public abstract class ProcessUtil {
  private ProcessUtil()  {}

  public static Process createProcess(final String[] args, final File workingDirectory) throws RuntimeException {
    final Process process;
    try {
      process = Runtime.getRuntime().exec(args, null, workingDirectory);
    }
    catch (IOException e) {
      final StringBuilder builder = new StringBuilder(1024);
      for (final String s : args){
        builder.append("  ");
        builder.append(s);
        builder.append('\n');
      }

      throw new RuntimeException(
        "Cannot execute command:\n" + builder.toString()
      );
    }

    return process;
  }

  public static Process createProcess(final String command) throws RuntimeException{
    try {
      return Runtime.getRuntime().exec(command, null, null);
    }
    catch (IOException e) {
      throw new RuntimeException("Cannot execute command: " + command);
    }
  }

  /**
   * @param shouldCollectOutput If true, waits for process termination (end of streams) and collects
   * stdout and stderr output. If false, starts reading threads and quits immediately.
   * @return null if shouldCollectOutput is false
   */
  public static ProcessOutput readOutput(final Process process, final boolean shouldCollectOutput) {
    final ArrayList<String> stdout = shouldCollectOutput ? new ArrayList<String>() : null;
    final ArrayList<String> stderr = shouldCollectOutput ? new ArrayList<String>() : null;

    final Thread stdoutReader = new MyReaderThread(process.getInputStream(), stdout, "stdout reader");
    final Thread stderrReader = new MyReaderThread(process.getErrorStream(), stderr, "stderr reader");

    stdoutReader.start();
    stderrReader.start();

    if (shouldCollectOutput) {
      try {
        stdoutReader.join();
      }
      catch (InterruptedException e) {
        // skip
      }

      int exitValue = 0;
      try {
        stderrReader.join();
        exitValue = process.waitFor();
      }
      catch (InterruptedException e) {
        // skip
      }

      return new ProcessOutput(
        stdout.toArray(new String[stdout.size()]),
        stderr.toArray(new String[stderr.size()]),
        exitValue
      );
    }

    return null;
  }

  public static final class ProcessOutput {
    public final String[] myOut;
    public final String[] myErr;
    public final int myExitValue;

    public ProcessOutput(final String[] out, final String[] err, final int exitValue) {
      myOut = out;
      myErr = err;
      myExitValue = exitValue;
    }
  }

  private static final class MyReaderThread extends Thread {
    private final InputStream myStream;
    private final ArrayList<String> myOutput;

    public MyReaderThread(final InputStream stream, final ArrayList<String> output, final String threadName) {
      super(threadName);
      myStream = stream;

      myOutput = output;
    }

    public void run() {
      try {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(myStream));
        try {
          String line = reader.readLine();
          while (line != null) {
            if (myOutput != null) {
              myOutput.add(line);
            }
            line = reader.readLine();
          }
        }
        finally {
          reader.close();
        }
      }
      catch (final IOException exc) {
        // skip
      }
    }
  }
}
