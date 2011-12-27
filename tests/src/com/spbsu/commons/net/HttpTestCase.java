//package com.spbsu.commons.net;
//
//import com.spbsu.commons.util.Pair;
//import junit.framework.TestCase;
//import org.mortbay.jetty.Server;
//import org.mortbay.jetty.handler.ResourceHandler;
//import org.mortbay.jetty.servlet.Context;
//import org.mortbay.jetty.servlet.ServletHolder;
//
///**
// * User: solar
// * Date: 16.06.2007
// * Time: 14:23:02
// */
//public abstract class HttpTestCase extends TestCase {
//  private Server jetty = null;
//  protected static final int PORT = 12345;
//  private Context httpContext;
//
//  protected void setUp() throws Exception {
//    super.setUp();
//    jetty = new Server(PORT);
//    String resourceBase = "./" + getBasePath();
//    Context resourceContext = new Context(jetty, "/");
//    resourceContext.setResourceBase(resourceBase);
//    final ResourceHandler files = new ResourceHandler();
//    files.setResourceBase(resourceBase);
//    resourceContext.setHandler(files);
//    httpContext = new Context(jetty, "/");
//    jetty.addHandler(resourceContext);
//    jetty.addHandler(httpContext);
//    jetty.start();
//  }
//
//  protected abstract String getBasePath();
//
//  protected void addServlets(Pair<String, String>[] context2class) throws ClassNotFoundException {
//    for (Pair<String, String> pair : context2class) {
//      addServlet(pair.getFirst(), pair.getSecond());
//    }
//  }
//
//  protected void addServlet(final String context, String className) throws ClassNotFoundException {
//    httpContext.addServlet(new ServletHolder(Class.forName(className)), context);
//  }
//
//  protected void tearDown() throws Exception {
//    if (jetty == null) {
//      return;
//    }
//    jetty.stop();
//    jetty.destroy();
//    jetty = null;
//
//    super.tearDown();
//  }
//}
