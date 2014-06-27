package com.spbsu.commons.func.converters;

import com.spbsu.commons.func.Converter;
import com.spbsu.commons.text.CharSequenceReader;
import com.spbsu.commons.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.CharArrayReader;
import java.io.IOException;

public class JDom2CharSequenceConverter implements Converter<Element, CharSequence> {
  private static Logger LOG = Logger.create(JDom2CharSequenceConverter.class);
  private ThreadLocal<SAXBuilder> builderThreadLocal = new ThreadLocal<SAXBuilder>() {
    @Override
    protected SAXBuilder initialValue() {
      SAXBuilder saxBuilder = new SAXBuilder();
      saxBuilder.setEntityResolver(new EntityResolver() {
        @Override
        public InputSource resolveEntity(String publicId, String systemId) {
          return new InputSource(new CharArrayReader(new char[0]));
        }
      });
      return saxBuilder;
    }
  };
  @Override
  public Element convertFrom(final CharSequence source) {
    Document document;
    try {
      document = builderThreadLocal.get().build(new CharSequenceReader(source));
    } catch (JDOMException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      LOG.warn(e); // should never happen
      throw new RuntimeException(e);
    }
    return document.detachRootElement();
  }

  private static final XMLOutputter OUTPUTTER = new XMLOutputter(Format.getPrettyFormat().setLineSeparator("\n"));
  @Override
  public synchronized CharSequence convertTo(final Element element) {
    return OUTPUTTER.outputString(element);
  }
}
