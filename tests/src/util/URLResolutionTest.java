//package util;
//
//import junit.framework.TestCase;
//
//import java.net.MalformedURLException;
//import java.net.URL;
//
///**
// * User: Dan Dyugurov
// * Date: 20.07.2006
// */
//public class URLResolutionTest extends TestCase {
//
//  public void testResolving() {
//
//    final String[] relative = {"g", "./g", "g/", "/g", "//g", "g?y", "#s", "g#s", "g?y#s",
//                               ";x", "g;x", "g;x?y#s", ".", "./", "..", "../", "../g", "../..", "../../", "../../g",
//                               "../../../g", "../../../../g", /*"/./g", */"/../g", ".g", "g.", "..g", "g..",
//                               "./../g", "./g/.", "g/./h", "g/../h", "g;x=1/./y", "g;x=1/../y", "g?y/./x", "g#s/../x",
//                               "http:g"};
//
//    final String base = "http://a/b/c/d;p?q";
//
//    final String[] resolved = {"http://a/b/c/g", "http://a/b/c/g", "http://a/b/c/g/", "http://a/g",
//                               "http://g", "http://a/b/c/g?y", "http://a/b/c/d;p?q#s",
//                               "http://a/b/c/g#s", "http://a/b/c/g?y#s", "http://a/b/c/;x", "http://a/b/c/g;x",
//                               "http://a/b/c/g;x?y#s", "http://a/b/c/", "http://a/b/c/", "http://a/b/",
//                               "http://a/b/", "http://a/b/g", "http://a/", "http://a/", "http://a/g",
//                               "http://a/../g", "http://a/../../g", /*"http://a/./g",*/ "http://a/../g",
//                               "http://a/b/c/.g", "http://a/b/c/g.", "http://a/b/c/..g", "http://a/b/c/g..",
//                               "http://a/b/g", "http://a/b/c/g/", "http://a/b/c/g/h", "http://a/b/c/h",
//                               "http://a/b/c/g;x=1/y", "http://a/b/c/y", "http://a/b/c/g?y/./x", "http://a/b/c/g#s/../x",
//                               "http:g"};
//    try {
//      final URL baseURL = new URL(base);
//      for (int i = 0; i < relative.length; i++) {
//        final URL resolvedURL = URLUtil.resolve(baseURL, relative[i]);
//        assertEquals(resolved[i], resolvedURL.toString());
//      }
//    }
//    catch (MalformedURLException ex) {
//      fail();
//    }
//  }
//
//  public void testHtmlChunkResolving() {
//    try {
//      String chunk = "<a href=\"hz/hz/hz.hz\"><img src='hello.jpg'/>";
//      URL base = new URL("http://servak/laja/la");
//      final String result = UrlUtil.resolveUrls(base, chunk);
//      assertEquals("<a href=\"http://servak/laja/hz/hz/hz.hz\"><img src='http://servak/laja/hello.jpg'/>", result);
//    }
//    catch (MalformedURLException e) {
//    }
//  }
//
//  public void testSofisticatedChunks() throws Exception{
//    URL base = new URL("http://servak/laja/la");
//    String chunk = "<a href  =  \"  hz/hz/hz.hz1    \">";
//    assertEquals("<a href  =  \"http://servak/laja/hz/hz/hz.hz1\">", UrlUtil.resolveUrls(base, chunk));
//    String chunk2 = "<a href\n=\t\"\thz/hz/hz.hz2 \n  \t\">";
//    assertEquals("<a href\n=\t\"http://servak/laja/hz/hz/hz.hz2\">", UrlUtil.resolveUrls(base, chunk2));
//    String chunk3 = "<a srC = \' /hz/hz/hz.hz3 \' >";
//    assertEquals("<a srC = \'http://servak/hz/hz/hz.hz3\' >", UrlUtil.resolveUrls(base, chunk3));
//    String chunk4 = "<a HrEF=\'http://hz/hz/hz.hz4  \'>";
//    assertEquals("<a HrEF=\'http://hz/hz/hz.hz4\'>", UrlUtil.resolveUrls(base, chunk4));
//    String chunk5 = "<a HREF=\'http://g  g/g g/f?d = d  \'>";
//    assertEquals("<a HREF=\'http://g%20%20g/g%20g/f?d%20=%20d\'>", UrlUtil.resolveUrls(base, chunk5));
//
//    String three = "<a HREF='ff'> <df src=\"hi\"> <df src=\"hi\">";
//    assertEquals("<a HREF='http://servak/laja/ff'> <df src=\"http://servak/laja/hi\"> <df src=\"http://servak/laja/hi\">", UrlUtil.resolveUrls(base, three));
//
//    String unquoted2 = "<a HRef= d d=w>\"hello\"<df src=\"hi\">";
//    assertEquals("<a HRef= http://servak/laja/d d=w>\"hello\"<df src=\"http://servak/laja/hi\">", UrlUtil.resolveUrls(base, unquoted2));
//
//    String unquoted = "<a HREF=#2> <df src=\"hi\">";
//    assertEquals("<a HREF=http://servak/laja/la#2> <df src=\"http://servak/laja/hi\">", UrlUtil.resolveUrls(base, unquoted));
//
////      String empty = "<a href =\"\"> <df src=\"hi\">";
////      assertEquals("<a href =\"http://servak/laja/\"> <df src=\"http://servak/laja/hi\">", UrlUtil.resolveUrls(base, empty));
//  }
//}
