package com.spbsu.commons.system;

import com.spbsu.commons.filters.Filter;
import com.spbsu.commons.func.Action;
import com.spbsu.commons.io.StreamTools;
import com.spbsu.commons.seq.CharSeqTools;
import com.spbsu.commons.util.MultiMap;
import com.spbsu.commons.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.net.www.protocol.file.FileURLConnection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;

/**
 * @author daywalker
 *         Date: 27.09.11
 */
public class RuntimeUtils {
  private static final Logger LOG = Logger.create(RuntimeUtils.class);
  /**
   * Method adapted from http://jlibs.googlecode.com/svn/trunk/core/src/main/java/jlibs/core/lang/RuntimeUtil.java.
   * See http://jlibs.googlecode.com for more information
   * <p>
   * This method guarantees that garbage collection is done unlike <code>{@link System#gc()}</code>
   * </p>
   */
  public static void gc(){
    Object obj = new Object();
    final WeakReference ref = new WeakReference<>(obj);
    //noinspection UnusedAssignment
    obj = null;
    while (ref.get()!=null) {
      System.gc();
    }
  }

  @NotNull
  public static Class[] findTypeParameters(final Class<?> clazz, final Class<?> interfaceClass) {
    final HashMap<TypeVariable, Type> mapping = new HashMap<>();
    populateTypeParametersMapping(clazz, mapping);
    final TypeVariable[] parameters = interfaceClass.getTypeParameters();
    final Class[] infered = new Class[parameters.length];
    for (int i = 0; i < infered.length; i++) {
      Type current = parameters[i];
      int arraysCount = 0;
      while(current instanceof TypeVariable) {
        current = mapping.get(current);
        while (current instanceof GenericArrayType) {
          arraysCount++;
          current = ((GenericArrayType) current).getGenericComponentType();
        }
      }
      //noinspection ChainOfInstanceofChecks
      if (current instanceof ParameterizedType) {
        current = ((ParameterizedType) current).getRawType();
      }
      if (current instanceof Class) {
        infered[i] = (Class) current;
        while(arraysCount-- > 0)
          infered[i] = Array.newInstance(infered[i], 0).getClass();
      }
      if (infered[i] == null) {
        LOG.warn("Cant find type for parameter " + parameters[i] + ": " + clazz + ", " + interfaceClass);
      }
    }
    return infered;
  }

  private static void populateTypeParametersMapping(final Type type, final Map<TypeVariable, Type> mapping) {
    if (type instanceof ParameterizedType) {
      final ParameterizedType ptype = (ParameterizedType) type;
      final Type raw = ((ParameterizedType) type).getRawType();
      if (raw instanceof Class) {
        final TypeVariable[] vars = ((Class) raw).getTypeParameters();
        final Type[] arguments = ptype.getActualTypeArguments();
        for (int i = 0; i < arguments.length; i++) {
          mapping.put(vars[i], arguments[i]);
        }
      }
      populateTypeParametersMapping(raw, mapping);
    }
    if (type instanceof Class) {
      final Class clazz = (Class) type;
      populateTypeParametersMapping(clazz.getGenericSuperclass(), mapping);
      for (final Type iface : clazz.getGenericInterfaces()) {
        populateTypeParametersMapping(iface, mapping);
      }
    }
  }

  public static String[] packageResourcesList(String... paths) throws URISyntaxException, IOException {
    final Set<String> result = new HashSet<>();
    for (String path : paths) {
      path = path.replace('.', '/') + "/";
      final ClassLoader loader = RuntimeUtils.class.getClassLoader();
      if (!(loader instanceof URLClassLoader)) {
        throw new UnsupportedOperationException("Operation is not supported for current type of classloader");
      }
      final URL[] dirs = ((URLClassLoader) loader).getURLs();
      final Set<String> increment = new HashSet<>();
      populateFromURLs(path, dirs, increment);
      result.addAll(increment);
    }
    return result.toArray(new String[result.size()]);
  }

  public static List<String> packageResourcesList(final Class clazz, String path) throws URISyntaxException, IOException {
    if (!path.endsWith("/")) {
      path += "/";
    }
    final List<String> result = new ArrayList<>();

    final URL dirURL = clazz.getResource(path);
    if (dirURL == null) {
      throw new RuntimeException("Invalid path " + path);
    }

    switch (dirURL.getProtocol()) {
      case "file":
        for (final String fileName : new File(dirURL.toURI()).list()) {
          result.add(path + fileName);
        }
        return result;
      case "jar":
        final String classpath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        for (final JarEntry entry : Collections.list(new JarFile(new File(classpath)).entries())) {
          final String name = entry.getName();
          if (name.matches(path.substring(1) + "[^\\/]+\\/?")) {
            result.add("/" + entry);
          }
        }
        return result;
    }
    throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
  }

  private static void populateFromURLs(final String path, final URL[] dirs, final Set<String> result) throws IOException {
    for (final URL dir : dirs) {
      try {
        final URLConnection connection = dir.openConnection();
        if (connection instanceof JarURLConnection) {
          final JarURLConnection jarConnection = (JarURLConnection) connection;
          final URL baseUrl = jarConnection.getJarFileURL();
          populateFromJar(path, result, jarConnection.getJarFile(), baseUrl);
        }
        else if (connection instanceof FileURLConnection) {
          String dirPath = URLDecoder.decode(dir.getPath(), "UTF-8");
          if (new File(dirPath).isDirectory()) {
            dirPath = dirPath.substring(0, dirPath.length());
            final File packageDir = new File(dirPath + path);
            final File[] files = packageDir.listFiles();
            if (files != null)
              StreamTools.visitFiles(packageDir, arg -> result.add(path + arg));
          }
          else {
            try {
              populateFromJar(path, result, new JarFile(dirPath), dir);
            }
            catch (IOException exc) {
              // skip
            }
          }
        }
        else throw new RuntimeException("Unknown connection");
      } catch (FileNotFoundException e) {
        // a file specified in the classpath may be unavailable
        // it is not an error necessarily (classpath may contain unused URLs)
        // ignore
        throw new RuntimeException("Unreachable directory " + dir.toString());
      }
    }
  }

  private static void populateFromJar(final String path, final Set<String> result, final JarFile jar, final URL jarUrl) throws IOException
  {
    final Enumeration<JarEntry> entries = jar.entries();
    while(entries.hasMoreElements()) {
      String name = entries.nextElement().getName();
      if (name.startsWith(path)) {
//        final int checkSubdir = name.indexOf("/", path.length());
//        if (checkSubdir >= 0) {
//          name = name.substring(0, checkSubdir);
//        }
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
    final List<URL> additionalUrls = new ArrayList<>();
    for (final String urlString : classPath.split("\\s")) {
      try {
        additionalUrls.add(new URL(urlString));
      } catch (MalformedURLException e) {
        // urlString should be a relative path from this jar directory
        final String basePath = jarUrl.getFile();
        final String baseDir = basePath.substring(0, basePath.lastIndexOf("/"));
        final URL constructedUrl = new URL(jarUrl.getProtocol(), jarUrl.getHost(), jarUrl.getPort(), baseDir + "/" + urlString);
        additionalUrls.add(constructedUrl);
      }
    }
    populateFromURLs(path, additionalUrls.toArray(new URL[additionalUrls.size()]), result);
  }

  public static void processSupers(final Class<?> clazz, final Filter<Class<?>> proc) {
    final Stack<Class<?>> toBeProcessed = new Stack<>();
    toBeProcessed.push(clazz);
    while (!toBeProcessed.isEmpty()) {
      final Class<?> pop = toBeProcessed.pop();
      if (pop == null)
        continue;
      if (proc.accept(pop))
        return;

      toBeProcessed.push(pop.getSuperclass());
      for (final Class<?> aClass : pop.getInterfaces()) {
        toBeProcessed.push(aClass);
      }
    }
  }

  @Nullable
  public static <T> T newInstanceByAssignable(final Class<T> targetClass, final Object... args) {
    @SuppressWarnings("unchecked")
    final Constructor<T>[] constructors = (Constructor<T>[])targetClass.getConstructors();
constructor_next:
    for (final Constructor<T> constructor : constructors) {
      final Class<?>[] parameters = constructor.getParameterTypes();
      if (parameters.length == args.length) {
        for (int i = 0; i < parameters.length; i++) {
          if(!parameters[i].isAssignableFrom(args[i].getClass()))
            continue constructor_next;
        }
        try {
          return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return null;
  }

  public static String getJavaExec() {
    return System.getProperty("java.home") + "/bin/java";
  }

  public static String getJavaLibraryPath() {
    return System.getProperty("java.library.path");
  }

  public static String getJavaClassPath() {
    return System.getProperty("java.class.path");
  }

  public static String getArchDataModel() {
    return System.getProperty("sun.arch.data.model");
  }

  public static String getOsName() {
    return System.getProperty("os.name");
  }

  private RuntimeUtils() {}

  public static CharSequence bashEscape(CharSequence command) {
    command = CharSeqTools.replace(command, "\\", "\\\\");
    command = CharSeqTools.replace(command, "\"", "\\\"");
    command = CharSeqTools.replace(command, "$", "\\$");
    command = CharSeqTools.replace(command, "&", "\\&");
    command = CharSeqTools.replace(command, "<", "\\<");
    command = CharSeqTools.replace(command, ">", "\\>");
    command = CharSeqTools.replace(command, " ", "\\ ");
    return command;
  }

  public static Process runJvm(final Class<?> mainClass, final String... args) {
    try {
      final Method main = mainClass.getMethod("main", String[].class);
      if (main.getReturnType().equals(void.class)
              && Modifier.isStatic(main.getModifiers())
              && Modifier.isPublic(main.getModifiers())) {
        try {
          final List<String> parameters = new ArrayList<>();
          parameters.add(getJavaExec());
          parameters.add("-Xmx1g");
          parameters.add("-classpath");
          parameters.add(getJavaClassPath());
          parameters.add(mainClass.getName());
          parameters.addAll(Arrays.asList(args));
          return Runtime.getRuntime().exec(parameters.toArray(new String[parameters.size()]));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

    } catch (NoSuchMethodException e) {
      //
    }
    throw new IllegalArgumentException("Main class must contain main method :)");
  }

  public static class InvokeDispatcher {
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(InvokeDispatcher.class.getName());
    public final Map<Class<?>, Method> typesMap = new HashMap<>();
    public final MultiMap<Class<?>, Method> cache = new MultiMap<>();
    private final Action<Object> unhandledCallback;

    public InvokeDispatcher(Class<?> clazz, Action<Object> unhandledCallback) {
      this(clazz, unhandledCallback, "invoke");
    }

    public InvokeDispatcher(Class<?> clazz, Action<Object> unhandledCallback, String methodName) {
      this.unhandledCallback = unhandledCallback;
      Arrays.asList(clazz.getMethods()).stream()
          .filter(method -> methodName.equals(method.getName()) && method.getParameterCount() == 1 && method.getReturnType() == void.class)
          .forEach(method -> typesMap.put(method.getParameterTypes()[0], method));
    }

    public InvokeDispatcher(Class<?> clazz, Action<Object> unhandledCallback, Class<? extends Annotation> annotation) {
      this.unhandledCallback = unhandledCallback;
      Arrays.asList(clazz.getMethods()).stream()
          .filter(method -> method.getAnnotation(annotation) != null && method.getParameterCount() == 1 && method.getReturnType() == void.class)
          .forEach(method -> typesMap.put(method.getParameterTypes()[0], method));
    }

    public final void invoke(Object instance, Object message) {
      Collection<Method> methods = cache.get(message.getClass());
      if (methods == MultiMap.EMPTY) {
        methods = new ArrayList<>();
        for (final Class<?> aClass : typesMap.keySet()) {
          if (aClass.isAssignableFrom(message.getClass())) {
            methods.add(typesMap.get(aClass));
          }
        }
        cache.putAll(message.getClass(), methods);
      }
      if (!methods.isEmpty()) {
        for (final Method method : methods) {
          try {
            method.setAccessible(true);
            method.invoke(instance, message);
          }
          catch (IllegalAccessException | InvocationTargetException e) {
            log.log(Level.WARNING, "Unable to call method", e);
          }
        }
      }
      else {
        log.log(Level.WARNING, "Unhandeled @" + instance + ": " + message.toString());
        unhandledCallback.invoke(message);
      }
    }
  }
}
