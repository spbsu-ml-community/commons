//package com.spbsu.commons.net;
//
//import DownloadManagerImpl;
//import Interval;
//import Holder;
//import Pair;
//import StreamTools;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.nio.charset.Charset;
//import java.util.Arrays;
//import java.util.concurrent.Semaphore;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.zip.GZIPInputStream;
//
//import junit.framework.Assert;
//
///**
// * Created by IntelliJ IDEA.
// * User: solar
// * Date: 16.06.2007
// * Time: 14:02:37
// */
//public class DownloadManagerTest extends HttpTestCase {
//  public void testCharGet() throws Exception {
//    final DownloadManagerImpl manager = new DownloadManagerImpl();
//    final Holder<Boolean> result = new Holder<Boolean>();
//    final TaskBase<CharSequence> task = new TaskBase<CharSequence>() {
//      public void start(CharSequence param) {
//        result.setValue(param.toString().indexOf("hello you") >= 0);
//      }
//    };
//    manager.request("http://localhost:" + PORT + "/hello.html", task);
//    manager.waitFor(task);
//    assertTrue(result.getValue());
//  }
//
//  public void testStatus() throws Exception {
//    addServlet("/", HeaderServlet.class.getName());
//    final DownloadManagerImpl manager = new DownloadManagerImpl();
//    final Holder<DownloadManager.URLStatus> status = new Holder<DownloadManager.URLStatus>();
//    final TaskBase<DownloadManager.URLStatus> task = new TaskBase<DownloadManager.URLStatus>() {
//      public void start(DownloadManager.URLStatus param) {
//        status.setValue(param);
//      }
//    };
//    manager.requestStatus("http://localhost:" + PORT + "/hello.html", task);
//    manager.waitFor(task);
//    final DownloadManager.URLStatus urlStatus = status.getValue();
//    assertNotNull(urlStatus);
//    assertEquals(200, urlStatus.getStatusCode());
//    assertEquals("text/html", urlStatus.getContentType());
//  }
//
//  public void testCharGetAndStatus() throws Exception {
//    final DownloadManagerImpl manager = new DownloadManagerImpl();
//    final Holder<String> result = new Holder<String>();
//    final Holder<DownloadManager.URLStatus> status = new Holder<DownloadManager.URLStatus>();
//    final TaskBase<Pair<DownloadManager.URLStatus, CharSequence>> task = new TaskBase<Pair<DownloadManager.URLStatus, CharSequence>>() {
//      public void start(Pair<DownloadManager.URLStatus, CharSequence> param) {
//        result.setValue(param.getSecond().toString());
//        status.setValue(param.getFirst());
//      }
//    };
//    manager.requestCharData("http://localhost:" + PORT + "/hello.html", task);
//    manager.waitFor(task);
//    Assert.assertEquals("hello you", result.getValue().trim());
//    manager.waitFor(task); // this is also test please don't "optimize" it
//    final DownloadManager.URLStatus urlStatus = status.getValue();
//    assertNotNull(urlStatus);
//    assertEquals(200, urlStatus.getStatusCode());
//    assertEquals("windows-1251", urlStatus.getContentEncoding());
//    assertEquals("text/html", urlStatus.getContentType());
//  }
//
//  public void testBinary() throws Exception {
//    final DownloadManagerImpl manager = new DownloadManagerImpl();
//    final Holder<byte[]> result = new Holder<byte[]>();
//    final Holder<DownloadManager.URLStatus> status = new Holder<DownloadManager.URLStatus>();
//    final TaskBase<Pair<DownloadManager.URLStatus, byte[]>> task = new TaskBase<Pair<DownloadManager.URLStatus, byte[]>>() {
//      public void start(Pair<DownloadManager.URLStatus, byte[]> param) {
//        result.setValue(param.getSecond());
//        status.setValue(param.getFirst());
//      }
//    };
//    manager.requestBinaryData("http://localhost:" + PORT + "/hello.html.gz", task);
//    manager.waitFor(task);
//    final CharSequence resultSeq = StreamTools.readStream(new GZIPInputStream(new ByteArrayInputStream(result.getValue())), Charset.forName("windows-1251"));
//    assertEquals("hello you", resultSeq.toString().trim());
//    manager.waitFor(task); // this is also test please don't "optimize" it
//    final DownloadManager.URLStatus urlStatus = status.getValue();
//    assertNotNull(urlStatus);
//    assertEquals(200, urlStatus.getStatusCode());
//    assertEquals(null, urlStatus.getContentEncoding());
//    assertEquals("application/gzip", urlStatus.getContentType());
//  }
//
//  public void testRedirect() throws Exception {
//    addServlet("/redirect", RedirectServlet.class.getName());
//    final DownloadManagerImpl manager = new DownloadManagerImpl();
//    final Holder<String> result = new Holder<String>();
//    final Holder<DownloadManager.URLStatus> status = new Holder<DownloadManager.URLStatus>();
//    final TaskBase<Pair<DownloadManager.URLStatus, CharSequence>> task = new TaskBase<Pair<DownloadManager.URLStatus, CharSequence>>() {
//      public void start(Pair<DownloadManager.URLStatus, CharSequence> param) {
//        result.setValue(param.getSecond().toString());
//        status.setValue(param.getFirst());
//      }
//    };
//    manager.requestCharData("http://localhost:" + PORT + "/redirect", task);
//    manager.waitFor(task);
//    Assert.assertEquals("hello you", result.getValue().trim());
//    manager.waitFor(task); // this is also test please don't "optimize" it
//    final DownloadManager.URLStatus urlStatus = status.getValue();
//    assertNotNull(urlStatus);
//    assertEquals(200, urlStatus.getStatusCode());
//    assertEquals("windows-1251", urlStatus.getContentEncoding());
//    assertEquals("text/html", urlStatus.getContentType());
//    assertTrue(Arrays.equals(new Pair[]{
//        Pair.create("http://localhost:" + PORT + "/redirect", 302),
//        Pair.create("http://localhost:" + PORT + "/hello.html", 200)
//    }, urlStatus.getRedirectPath()));
//  }
//
//  public void testRedirectChain() throws Exception {
//    addServlet("/redirect4", RedirectServlet4.class.getName());
//    addServlet("/redirect3", RedirectServlet3.class.getName());
//    addServlet("/redirect2", RedirectServlet2.class.getName());
//    addServlet("/redirect", RedirectServlet.class.getName());
//    final DownloadManagerImpl manager = new DownloadManagerImpl();
//    final Holder<String> result = new Holder<String>();
//    final Holder<DownloadManager.URLStatus> status = new Holder<DownloadManager.URLStatus>();
//    final TaskBase<Pair<DownloadManager.URLStatus, CharSequence>> task = new TaskBase<Pair<DownloadManager.URLStatus, CharSequence>>() {
//      public void start(Pair<DownloadManager.URLStatus, CharSequence> param) {
//        result.setValue(param.getSecond().toString());
//        status.setValue(param.getFirst());
//      }
//    };
//    manager.requestCharData("http://localhost:" + PORT + "/redirect4", task);
//    manager.waitFor(task);
//    Assert.assertEquals("hello you", result.getValue().trim());
//    manager.waitFor(task); // this is also test please don't "optimize" it
//    final DownloadManager.URLStatus urlStatus = status.getValue();
//    assertNotNull(urlStatus);
//    assertEquals(200, urlStatus.getStatusCode());
//    assertEquals("windows-1251", urlStatus.getContentEncoding());
//    assertEquals("text/html", urlStatus.getContentType());
//    assertTrue(Arrays.equals(new Pair[]{
//        Pair.create("http://localhost:" + PORT + "/redirect4", 302),
//        Pair.create("http://localhost:" + PORT + "/redirect3", 302),
//        Pair.create("http://localhost:" + PORT + "/redirect2", 302),
//        Pair.create("http://localhost:" + PORT + "/redirect", 302),
//        Pair.create("http://localhost:" + PORT + "/hello.html", 200)
//    }, urlStatus.getRedirectPath()));
//  }
//
//  public void testHang() throws Exception {
//    addServlet("/sleep", SleepServlet.class.getName());
//    Interval.start();
//    final DownloadManagerImpl manager = new DownloadManagerImpl();
//    final TaskBase<Pair<DownloadManager.URLStatus, CharSequence>> task = new TaskBase<Pair<DownloadManager.URLStatus, CharSequence>>() {
//      public void start(Pair<DownloadManager.URLStatus, CharSequence> param) {
//      }
//    };
//    manager.requestCharData("http://localhost:" + PORT + "/sleep", task);
//    new Thread() {
//      @Override
//      public void run() {
//        try {
//          Thread.sleep(100);
//          manager.cancelRequest(task);
//        }
//        catch (InterruptedException e) {
//          e.printStackTrace();
//        }
//      }
//    }.start();
//    manager.waitFor(task);
//    System.out.println("Interrupt");
//    assertTrue(Interval.time() < 3000);
//  }
//
//  public void testTooLongCommunication() throws Exception {
//    addServlet("/hang", HangingServlet.class.getName());
//    Interval.start();
//    final DownloadManagerImpl manager = new DownloadManagerImpl();
//    final TaskBase<Pair<DownloadManager.URLStatus, CharSequence>> task = new TaskBase<Pair<DownloadManager.URLStatus, CharSequence>>() {
//      public void start(Pair<DownloadManager.URLStatus, CharSequence> param) {
//      }
//    };
//    manager.requestCharData("http://localhost:" + PORT + "/hang", task);
//    new Thread() {
//      @Override
//      public void run() {
//        try {
//          Thread.sleep(1000);
//          manager.cancelRequest(task);
//        }
//        catch (InterruptedException e) {
//          e.printStackTrace();
//        }
//      }
//    }.start();
//    manager.waitFor(task);
//    System.out.println("Interrupt");
//    assertTrue(Interval.time() < 3000);
//  }
//
//  public void testPolicyMaxCommuninicationsPerServer() throws Exception {
//    addServlet("/hang", HangingServlet.class.getName());
//    final AtomicInteger finishedTaskCount = new AtomicInteger(0);
//    final DownloadManagerImpl manager = new DownloadManagerImpl(new Policy(3, 10, 10 * 1000));
//    for (int i = 0; i < 3; i++) {
//      final TaskBase<Pair<DownloadManager.URLStatus, CharSequence>> task = new TaskBase<Pair<DownloadManager.URLStatus, CharSequence>>() {
//        public void start(Pair<DownloadManager.URLStatus, CharSequence> param) {
//          finishedTaskCount.incrementAndGet();
//        }
//      };
//      startHangingTask(manager, task, 3000);
//    }
//    final Holder<String> result = new Holder<String>();
//    final Holder<DownloadManager.URLStatus> status = new Holder<DownloadManager.URLStatus>();
//    final TaskBase<Pair<DownloadManager.URLStatus, CharSequence>> task = new TaskBase<Pair<DownloadManager.URLStatus, CharSequence>>() {
//      public void start(Pair<DownloadManager.URLStatus, CharSequence> param) {
//        assertTrue("too few finished tasks", finishedTaskCount.at() > 0);
//        result.setValue(param.getSecond().toString());
//        status.setValue(param.getFirst());
//      }
//    };
//    Thread.sleep(1000);
//    manager.requestCharData("http://localhost:" + PORT + "/hello.html", task);
//    manager.waitFor(task);
//    Assert.assertEquals("hello you", result.getValue().trim());
//    manager.waitFor(task); // this is also test please don't "optimize" it
//    final DownloadManager.URLStatus urlStatus = status.getValue();
//    assertNotNull(urlStatus);
//    assertEquals(200, urlStatus.getStatusCode());
//    assertEquals("windows-1251", urlStatus.getContentEncoding());
//    assertEquals("text/html", urlStatus.getContentType());
//  }
//
//  public void testPolicyMaxCommuninicationsPerMinute() throws Exception {
//    addServlet("/hang", HangingServlet.class.getName());
//    final AtomicInteger finishedTaskCount = new AtomicInteger(0);
//    final int intervalMs = 10 * 1000;
//    final DownloadManagerImpl manager = new DownloadManagerImpl(new Policy(3, 3, intervalMs));
//    final long startTimeMs = System.currentTimeMillis();
//    for (int i = 0; i < 3; i++) {
//      final TaskBase<Pair<DownloadManager.URLStatus, CharSequence>> task = new TaskBase<Pair<DownloadManager.URLStatus, CharSequence>>() {
//        public void start(Pair<DownloadManager.URLStatus, CharSequence> param) {
//          finishedTaskCount.incrementAndGet();
//        }
//      };
//      startHangingTask(manager, task, 3000);
//    }
//    while (finishedTaskCount.at() < 3) {
//      Thread.sleep(100);
//    }
//    final Holder<String> result = new Holder<String>();
//    final Holder<DownloadManager.URLStatus> status = new Holder<DownloadManager.URLStatus>();
//    final TaskBase<Pair<DownloadManager.URLStatus, CharSequence>> task = new TaskBase<Pair<DownloadManager.URLStatus, CharSequence>>() {
//      public void start(Pair<DownloadManager.URLStatus, CharSequence> param) {
//        final long passed = System.currentTimeMillis() - startTimeMs;
//        assertTrue(passed >= intervalMs);
//        result.setValue(param.getSecond().toString());
//        status.setValue(param.getFirst());
//      }
//    };
//    manager.requestCharData("http://localhost:" + PORT + "/hello.html", task);
//    manager.waitFor(task);
//    Assert.assertEquals("hello you", result.getValue().trim());
//    manager.waitFor(task); // this is also test please don't "optimize" it
//    final DownloadManager.URLStatus urlStatus = status.getValue();
//    assertNotNull(urlStatus);
//    assertEquals(200, urlStatus.getStatusCode());
//    assertEquals("windows-1251", urlStatus.getContentEncoding());
//    assertEquals("text/html", urlStatus.getContentType());
//  }
//
//  private static void startHangingTask(final DownloadManagerImpl manager, final TaskBase<Pair<DownloadManager.URLStatus, CharSequence>> task, final int timeToHang) {
//    final Semaphore sema = new Semaphore(1);
//    final Thread thread = new Thread() {
//      @Override
//      public void run() {
//        try {
//          manager.requestCharData("http://localhost:" + PORT + "/hang", task);
//        } finally {
//          sema.release();
//        }
//        new Thread() {
//          @Override
//          public void run() {
//            try {
//              Thread.sleep(timeToHang);
//              manager.cancelRequest(task);
//            }
//            catch (InterruptedException e) {
//              e.printStackTrace();
//            }
//          }
//        }.start();
//        manager.waitFor(task);
//        System.out.println("Interrupt");
//      }
//    };
//    sema.acquireUninterruptibly();
//    thread.start();
//    sema.acquireUninterruptibly();
//  }
//
//  public void testPathPolicy() {
//    final DownloadManagerImpl manager = new DownloadManagerImpl();
//    Policy policy = new Policy(1, 1, 1000);
//    manager.setCustomPolicy("www.yandex.ru/path1/path2", policy);
//    Policy policy2 = new Policy(2, 2, 1000);
//    manager.setCustomPolicy("www.yandex.ru/path1", policy2);
//    Policy policy1 = new Policy(3, 3, 1000);
//    manager.setCustomPolicy("www.yandex.ru", policy1);
//    assertEquals(manager.getPolicyForURL("http://www.yandex.ru/").getSecond(), policy1);
//    assertEquals(manager.getPolicyForURL("http://www.yandex.ru/path1/ppw").getSecond(), policy2);
//    assertEquals(manager.getPolicyForURL("http://www.yandex.ru/path1/path2/path3").getSecond(), policy);
//  }
//
//  public void testErrorStatus() throws Exception {
//    final DownloadManagerImpl manager = new DownloadManagerImpl();
//    final Holder<DownloadManager.URLStatus> status = new Holder<DownloadManager.URLStatus>();
//    final TaskBase<DownloadManager.URLStatus> task = new TaskBase<DownloadManager.URLStatus>() {
//      public void start(DownloadManager.URLStatus param) {
//        status.setValue(param);
//      }
//    };
//    manager.requestStatus("httpxxx://localhost:" + PORT + "/xxx.html", task);
//    manager.waitFor(task);
//    final DownloadManager.URLStatus urlStatus = status.getValue();
//    assertNotNull(urlStatus);
//    final IOException e = urlStatus.getException();
//    assertNotNull(e);
//  }
//
//  @Override
//  protected String getBasePath() {
//    return "commons/trunk/tests/src/data/dm";
//  }
//
//  @SuppressWarnings({"PublicInnerClass"})
//  public static class RedirectServlet extends HttpServlet {
//    @Override
//    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
//      httpServletResponse.sendRedirect("/hello.html");
//    }
//  }
//
//  @SuppressWarnings({"PublicInnerClass"})
//  public static class RedirectServlet2 extends HttpServlet {
//    @Override
//    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
//      httpServletResponse.sendRedirect("/redirect");
//    }
//  }
//
//  @SuppressWarnings({"PublicInnerClass"})
//  public static class RedirectServlet3 extends HttpServlet {
//    @Override
//    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
//      httpServletResponse.sendRedirect("/redirect2");
//    }
//  }
//
//  @SuppressWarnings({"PublicInnerClass"})
//  public static class RedirectServlet4 extends HttpServlet {
//    @Override
//    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
//      httpServletResponse.sendRedirect("/redirect3");
//    }
//  }
//
//  @SuppressWarnings({"PublicInnerClass"})
//  public static class SleepServlet extends HttpServlet {
//    @Override
//    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
//      try {
//        Thread.sleep(10000);
//      }
//      catch (InterruptedException e) {
//        //skip
//        System.out.println("End of sleep");
//        e.printStackTrace(System.out);
//      }
//    }
//  }
//
//  @SuppressWarnings({"PublicInnerClass"})
//  public static class HangingServlet extends HttpServlet {
//    @Override
//    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
//      final PrintWriter writer = httpServletResponse.getWriter();
//      while (true) {
//        try {
//          writer.print("q");
//          Thread.sleep(20);
//        }
//        catch (InterruptedException e) {
//          //skip
//          System.out.println("End of sleep");
//          e.printStackTrace(System.out);
//          break;
//        }
//      }
//    }
//  }
//
//  @SuppressWarnings({"PublicInnerClass"})
//  public static class HeaderServlet extends HttpServlet {
//    @Override
//    protected void doHead(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
//      httpServletResponse.setContentType("text/html");
//      httpServletResponse.setCharacterEncoding("windows-1251");
//    }
//  }
//}
