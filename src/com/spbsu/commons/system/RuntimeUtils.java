package com.spbsu.commons.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;


import com.spbsu.commons.util.logging.Logger;
import sun.net.www.protocol.file.FileURLConnection;

/**
 * @author daywalker
 *         Date: 27.09.11
 */
public class RuntimeUtils {
  private static Logger LOG = Logger.create(RuntimeUtils.class);
  /**
   * Method adapted from http://jlibs.googlecode.com/svn/trunk/core/src/main/java/jlibs/core/lang/RuntimeUtil.java.
   * See http://jlibs.googlecode.com for more information
   * <p>
   * This method guarantees that garbage collection is done unlike <code>{@link System#gc()}</code>
   * </p>
   */
  public static void gc(){
    Object obj = new Object();
    WeakReference ref = new WeakReference<Object>(obj);
    //noinspection UnusedAssignment
    obj = null;
    while (ref.get()!=null) {
      System.gc();
    }
  }

  public static Class[] findTypeParameters(final Class<?> clazz, final Class<?> interfaceClass) {
    HashMap<TypeVariable, Type> mapping = new HashMap<TypeVariable, Type>();
    populateTypeParametersMapping(clazz, mapping);
    TypeVariable[] parameters = interfaceClass.getTypeParameters();
    Class[] infered = new Class[parameters.length];
    for (int i = 0; i < infered.length; i++) {
      Type current = parameters[i];

      while(current instanceof TypeVariable) {
        current = mapping.get(current);
      }
      if (current instanceof Class) {
        infered[i] = (Class) current;
      }
    }
    return infered;
  }

  private static void populateTypeParametersMapping(final Type type, Map<TypeVariable, Type> mapping) {
    if (type instanceof ParameterizedType) {
      ParameterizedType ptype = (ParameterizedType) type;
      Type raw = ((ParameterizedType) type).getRawType();
      if (raw instanceof Class) {
        TypeVariable[] vars = ((Class) raw).getTypeParameters();
        Type[] arguments = ptype.getActualTypeArguments();
        for (int i = 0; i < arguments.length; i++) {
          mapping.put(vars[i], arguments[i]);
        }
      }
      populateTypeParametersMapping(raw, mapping);
    }
    if (type instanceof Class) {
      Class clazz = (Class) type;
      populateTypeParametersMapping(clazz.getGenericSuperclass(), mapping);
      for (Type iface : clazz.getGenericInterfaces()) {
        populateTypeParametersMapping(iface, mapping);
      }
    }
  }


  public static String[] packageResourcesList(String path) throws URISyntaxException, IOException {
    path = path.replace('.', '/') + "/";
    ClassLoader loader = RuntimeUtils.class.getClassLoader();
    if (!(loader instanceof URLClassLoader)) {
      throw new UnsupportedOperationException("Operation is not supported for current type of classloader");
    }
    URL[] dirs = ((URLClassLoader) loader).getURLs();
    Set<String> result = new HashSet<String>();
    populateFromURLs(path, dirs, result);
    return result.toArray(new String[result.size()]);
  }

  private static void populateFromURLs(String path, URL[] dirs, Set<String> result) throws IOException {
    for (URL dir : dirs) {
      try {
        URLConnection connection = dir.openConnection();
        if (connection instanceof JarURLConnection) {
          final JarURLConnection jarConnection = (JarURLConnection) connection;
          final URL baseUrl = jarConnection.getJarFileURL();
          populateFromJar(path, result, jarConnection.getJarFile(), baseUrl);
        }
        else if (connection instanceof FileURLConnection) {
          String dirPath = URLDecoder.decode(dir.getPath(), "UTF-8");
          if (new File(dirPath).isDirectory()) {
            dirPath = dirPath.substring(0, dirPath.length());
            File packageDir = new File(dirPath + path);
            if (packageDir.listFiles() != null) {
              for (File file : packageDir.listFiles()) {
                result.add(path + file.getName());
              }
            }
          }
          else if (dirPath.endsWith(".jar")) {
            populateFromJar(path, result, new JarFile(dirPath), dir);
          }
        }
      } catch (FileNotFoundException e) {
        // a file specified in the classpath may be unavailable
        // it is not an error necessarily (classpath may contain unused URLs)
        // ignore
        LOG.debug("Unreachable classpath element: "+dir);
      }
    }
  }

  private static void populateFromJar(final String path, final Set<String> result, final JarFile jar, final URL jarUrl) throws IOException
  {
    Enumeration<JarEntry> entries = jar.entries();
    while(entries.hasMoreElements()) {
      String name = entries.nextElement().getName();
      if (name.startsWith(path)) {
        int checkSubdir = name.indexOf("/", path.length());
        if (checkSubdir >= 0) {
          name = name.substring(0, checkSubdir);
        }
        result.add(name);
      }
    }
    // manifest may specify additional classpath; we have to scan it as well
    final Manifest manifest = jar.getManifest();
    if (manifest == null) {
      return;
    }
    final String classPath = manifest.getMainAttributes().getValue("Class-Path");
    if (classPath == null) {
      return;
    }
    final List<URL> additionalUrls = new ArrayList<URL>();
    for (String urlString : classPath.split("\\s")) {
      try {
        additionalUrls.add(new URL(urlString));
      } catch (MalformedURLException e) {
        // urlString should be a relative path from this jar directory
        final String basePath = jarUrl.getFile();
        String baseDir = basePath.substring(0, basePath.lastIndexOf("/"));
        final URL constructedUrl = new URL(jarUrl.getProtocol(), jarUrl.getHost(), jarUrl.getPort(), baseDir + "/" + urlString);
        additionalUrls.add(constructedUrl);
      }
    }
    populateFromURLs(path, additionalUrls.toArray(new URL[additionalUrls.size()]), result);

  }
}
