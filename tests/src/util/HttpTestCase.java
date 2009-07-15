package util;

import junit.framework.TestCase;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletHandler;

/**
 * User: solar
 * Date: 16.06.2007
 */
public abstract class HttpTestCase extends TestCase {
  private Server jetty = null;

  protected static final int PORT = 12345;
  private ServletHandler servletHandler;

  protected void setUp() throws Exception {
    super.setUp();
    jetty = new Server(PORT);
    Context httpContext = new Context(jetty, "/", Context.SESSIONS);
    httpContext.addServlet(new ServletHolder(), "/*");

    servletHandler = new ServletHandler();
    jetty.addHandler(servletHandler);
    httpContext.setResourceBase("./" + getBasePath());
    jetty.setHandler(servletHandler);
    jetty.start();
  }

  protected abstract String getBasePath();

  protected void addServlet(String context, String className) {
    servletHandler.addServletWithMapping(className, context);
  }

  protected void tearDown() throws Exception {
    if (jetty == null) return;
    Handler[] handlers = jetty.getHandlers();
    if (handlers != null) {
      for (final Handler handler : handlers) {
        jetty.removeHandler(handler);
        handler.stop();
      }
    }
    jetty.stop();
    jetty.destroy();
    jetty = null;
    super.tearDown();
  }
}
