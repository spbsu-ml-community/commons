package com.spbsu.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: Dan Dyugurov
 * Date: 20.07.2006
 * Time: 12:05:30
 * To change this template use File | Settings | File Templates.
 */
public class UrlUtil {

  public static URL createURL(String link) {
    try {
      return new URL(link);
    }
    catch (MalformedURLException e) {
      return null;
    }
  }

  public static URL resolve(URL base, String relative) {
    final URL resolvedURL;
    try {
      final URI uri = URI.create(relative);
      resolvedURL = new URL(base.toURI().resolve(uri).toString());
    }
    catch (Exception e) {
      return null;
    }
    return resolvedURL;
  }

  public static String resolveUrls(URL base, CharSequence htmlChunk) {
    final StringBuffer buffer = new StringBuffer(htmlChunk);
    final Pattern hrefOrSrc = Pattern.compile("([hH][rR][eE][fF]|[sS][rR][cC])[ \r\n\t]*=[ \r\n\t]*");
    final Pattern quotedLink = Pattern.compile("\"[^\"<>]*\"|\'[^\'<>]*\'");
    final Pattern unquotedLink = Pattern.compile("[^ \r\n\t<>]*");
    final Matcher matcher = hrefOrSrc.matcher(htmlChunk);
    int offset = 0;
    while (matcher.find()) {
      final char startQoute = htmlChunk.charAt(matcher.end());
      final Pattern linkPat;
      if ('\"' == startQoute || '\'' == startQoute)
        linkPat = quotedLink;
      else
        linkPat = unquotedLink;
      matcher.usePattern(linkPat);
      if (matcher.find()) {
        final int start = linkPat == quotedLink ? matcher.start() + 1 : matcher.start();
        final int end = linkPat == quotedLink ? matcher.end() - 1 : matcher.end();
        final String link = htmlChunk.subSequence(start, end).toString().trim().replaceAll(" ", "%20");
        final URL resolvedLink = UrlUtil.resolve(base, link);
        if (resolvedLink != null) {
          buffer.replace(start + offset, end + offset, resolvedLink.toString());
          offset += resolvedLink.toString().length() - (end - start);
        }
      }
      matcher.usePattern(hrefOrSrc);
    }
    return buffer.toString();
  }
}
