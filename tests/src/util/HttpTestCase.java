package util;

import junit.framework.TestCase;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpListener;
import org.mortbay.http.HttpServer;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.jetty.servlet.ServletHandler;

/**
 * User: solar
 * Date: 16.06.2007
 * Time: 14:23:02
 */
public abstract class HttpTestCase extends TestCase {
  private HttpServer jetty = null;
  protected static final int PORT = 12345;
  private ServletHandler servletHandler;

  protected void setUp() throws Exception {
    super.setUp();
    jetty = new HttpServer();
    jetty.addListener(":" + PORT);

    final HttpContext httpContext = jetty.getContext("/");
    servletHandler = new ServletHandler();
    httpContext.addHandler(servletHandler);
    httpContext.setResourceBase("./" + getBasePath());
    httpContext.addHandler(new ResourceHandler());
    jetty.addContext(httpContext);
    jetty.addContext(httpContext);
    jetty.start();
  }

  protected abstract String getBasePath();

  protected void addServlet(final String context, String className) {
    servletHandler.addServlet(context, className);
  }

  protected void tearDown() throws Exception {
    if (jetty == null) return;
    final HttpListener[] listeners = jetty.getListeners();
    for (int i = 0; i < listeners.length; i++) {
      final HttpListener listener = listeners[i];
      jetty.removeListener(listener);
      listener.stop();
    }
    jetty.stop();
    jetty.destroy();
    jetty = null;

    super.tearDown();
  }
}
