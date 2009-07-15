package com.spbsu.text.stemmer;

import com.spbsu.text.stemmer.ext.EnglishStemmer;
import com.spbsu.text.stemmer.ext.RussianStemmer;

/**
 * User: solar
 * Date: 01.07.2007
 */
public class Stemmer {
  public final EnglishStemmer english = new EnglishStemmer();
  public final RussianStemmer russian = new RussianStemmer();

//  private final WeakHashMap<CharSequence, WeakReference<CharSequence>> cache = new WeakHashMap<CharSequence, WeakReference<CharSequence>>();

  private static final Stemmer STEMMER = new Stemmer();

  public static Stemmer getInstance(){
    return STEMMER;
  }

  public synchronized CharSequence stem(CharSequence word){
//    final WeakReference<CharSequence> ref = cache.get(word);
//    if (ref != null) {
//      final CharSequence value = ref.get();
//      if (value != null) return value;
//    }
    final CharSequence result;
    char firstLetter = word.length() > 0 ? word.charAt(0) : 'a';
    if (firstLetter > (int) 'a' && firstLetter < (int) 'z' ||
        firstLetter > (int) 'A' && firstLetter < (int) 'Z'){
      english.setCurrent(word);
      english.stem();
      result = english.getCurrent();
    }
    else {
      russian.setCurrent(word);
      russian.stem();
      result = russian.getCurrent();
    }
//    cache.put(word, new WeakReference<CharSequence>(result));
    return result;
  }
}
