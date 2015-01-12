package com.spbsu.commons.text.stem;

import com.spbsu.commons.seq.CharSeqTools;
import com.spbsu.commons.text.stem.ext.EnglishStemmer;
import com.spbsu.commons.text.stem.ext.RussianStemmer;

/**
 * User: solar
 * Date: 01.07.2007
 */
public class Stemmer {
  public final EnglishStemmer english = new EnglishStemmer();
  public final RussianStemmer russian = new RussianStemmer();
  private static final Stemmer STEMMER = new Stemmer();

  private Stemmer() {
  }

  @Deprecated
  public static Stemmer getInstance(){
    return STEMMER;
  }

  public synchronized CharSequence stem(final CharSequence forStem){
    final CharSequence word = CharSeqTools.toLowerCase(forStem);
    final CharSequence result;
    final char firstLetter = word.length() > 0 ? word.charAt(0) : 'a';
    if (firstLetter >= (int) 'a' && firstLetter <= (int) 'z' ||
        firstLetter >= (int) 'A' && firstLetter <= (int) 'Z'){
      english.setCurrent(word);
      english.stem();
      result = english.getCurrent();
    }
    else {
      russian.setCurrent(word);
      russian.stem();
      result = russian.getCurrent();
    }
    return result;
  }
}
