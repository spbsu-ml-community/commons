//package util;
//
//import com.spbsu.commons.io.StreamTools;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.net.HttpURLConnection;
//import java.net.URL;
//
///**
// * User: solar
// * Date: 16.06.2007
// */
//public class HttpTestCaseTest extends HttpTestCase {
//  protected String getBasePath() {
//    return "tests/data/http";
//  }
//
//  public void testPlainHTML() throws Exception {
//    final URL url = new URL("http://localhost:" + PORT + "/hello.html");
//    final CharSequence result = StreamTools.readStream(url.openConnection().getInputStream());
//    assertEquals("Hello", result.toString());
//  }
//
//  public void testServlet() throws Exception {
//    final URL url = new URL("http://localhost:" + PORT + "/xxx");
//    addServlet("/xxx", FakeServlet.class.getName());
//    final CharSequence result = StreamTools.readStream(url.openConnection().getInputStream());
//    assertEquals("Hello off.", result.toString().trim());
//  }
//
//  public void testRedirect() throws Exception {
//    final URL url = new URL("http://localhost:" + PORT + "/xxx");
//    addServlet("/xxx", RedirectServlet.class.getName());
//    addServlet("/yyy", FakeServlet.class.getName());
//    final HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
//    urlConnection.setInstanceFollowRedirects(false);
//    urlConnection.connect();
//    assertEquals(302, urlConnection.getResponseCode());
//  }
//
//  public static class FakeServlet extends HttpServlet {
//    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
//      final PrintWriter printWriter = httpServletResponse.getWriter();
//      printWriter.println("Hello off.");
//    }
//  }
//
//  public static class RedirectServlet extends HttpServlet {
//    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
//      httpServletResponse.sendRedirect("/yyy");
//    }
//  }
//}
