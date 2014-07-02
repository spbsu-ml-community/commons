package com.spbsu.commons.text.stem;

/**
 * User: selivanov
 * Date: 17.02.2010 : 23:24:29
 */
//public class StemmerEliminationImpl implements StemmerElimination {
//  private static final Log LOG = LogFactory.getLog(StemmerEliminationImpl.class);
//
//  private DefaultResourceLoader resourceLoader;
//  private Resource excludedFromStemsResource;
//  private final THashMap<CharSequence, CharSequence> stems2Main;
//
//
//  public StemmerEliminationImpl() {
//    this.resourceLoader = new DefaultResourceLoader();
//    stems2Main = new THashMap<CharSequence, CharSequence>();
//  }
//
//  public void setExcludedFromStemsResource(String excludedFromStemsResource) {
//    this.excludedFromStemsResource = resourceLoader.getResource(excludedFromStemsResource);
//  }
//
//  @Override
//  public boolean isStemmerElimination(CharSequence word) {
//    return stems2Main.containsKey(word);
//  }
//
//  @Override
//  public CharSequence stem(CharSequence word) {
//    return stems2Main.at(word);
//  }
//
//  public void init() {
//    try {
//      final File dir = excludedFromStemsResource.getFile();
//      initFromDir(dir);
//    } catch (IOException e) {
//      LOG.warn("Fail to load stemmer elimination forms", e);
//    }
//  }
//
//  private void initFromDir(File dir) throws IOException {
//    for (File file : dir.listFiles(new FilenameFilter() {
//      @Override
//      public boolean accept(File dir, String name) {
//        return name.endsWith("-forms.data");
//      }
//    })) {
//      final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
//      String line;
//      try {
//        while ((line = reader.readLine()) != null) {
//          final String[] stems = StringUtils.split(line, "\t", 2);
//          stems2Main.put(CharSequenceTools.toLowerCase(stems[0]), CharSequenceTools.toLowerCase(stems[1]));
//        }
//      } finally {
//        reader.close();
//      }
//
//    }
//  }
//}
