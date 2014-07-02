package com.spbsu.commons.text.stem;

import com.spbsu.commons.seq.CharSeqComposite;
import com.spbsu.commons.util.ArrayTools;


import java.lang.reflect.InvocationTargetException;

public abstract class AbstractStemmer {
  protected AbstractStemmer() {
    setCurrent("");
  }

  /**
   * Set the current string.
   */
  public void setCurrent(final CharSequence value) {
    current = value;
    cursor = 0;
    limit = current.length();
    limit_backward = 0;
    bra = cursor;
    ket = limit;
  }

  /**
   * Get the current string.
   */
  public CharSequence getCurrent() {
    return current;
  }

  public abstract boolean stem();

  // current string
  protected CharSequence current;

  protected int cursor;
  protected int limit;
  protected int limit_backward;
  protected int bra;
  protected int ket;

  protected void copy_from(final AbstractStemmer other) {
    current = other.current;
    cursor = other.cursor;
    limit = other.limit;
    limit_backward = other.limit_backward;
    bra = other.bra;
    ket = other.ket;
  }

  protected boolean in_grouping(final char [] s, final int min, final int max) {
    if (cursor >= limit) return false;
    char ch = current.charAt(cursor);
    if (ch > max || ch < min) return false;
    ch -= min;
    if ((s[ch >> 3] & (0X1 << (ch & 0X7))) == 0) return false;
    cursor++;
    return true;
  }

  protected boolean in_grouping_b(final char [] s, final int min, final int max) {
    if (cursor <= limit_backward) return false;
    char ch = current.charAt(cursor - 1);
    if (ch > max || ch < min) return false;
    ch -= min;
    if ((s[ch >> 3] & (0X1 << (ch & 0X7))) == 0) return false;
    cursor--;
    return true;
  }

  protected boolean out_grouping(final char [] s, final int min, final int max) {
    if (cursor >= limit) return false;
    char ch = current.charAt(cursor);
    if (ch > max || ch < min) {
      cursor++;
      return true;
    }
    ch -= min;
    if ((s[ch >> 3] & (0X1 << (ch & 0X7))) == 0) {
      cursor ++;
      return true;
    }
    return false;
  }

  protected boolean out_grouping_b(final char [] s, final int min, final int max) {
    if (cursor <= limit_backward) return false;
    char ch = current.charAt(cursor - 1);
    if (ch > max || ch < min) {
      cursor--;
      return true;
    }
    ch -= min;
    if ((s[ch >> 3] & (0X1 << (ch & 0X7))) == 0) {
      cursor--;
      return true;
    }
    return false;
  }

  protected boolean in_range(final int min, final int max) {
    if (cursor >= limit) return false;
    final char ch = current.charAt(cursor);
    if (ch > max || ch < min) return false;
    cursor++;
    return true;
  }

  protected boolean in_range_b(final int min, final int max) {
    if (cursor <= limit_backward) return false;
    final char ch = current.charAt(cursor - 1);
    if (ch > max || ch < min) return false;
    cursor--;
    return true;
  }

  protected boolean out_range(final int min, final int max) {
    if (cursor >= limit) return false;
    final char ch = current.charAt(cursor);
    if (!(ch > max || ch < min)) return false;
    cursor++;
    return true;
  }

  protected boolean out_range_b(final int min, final int max) {
    if (cursor <= limit_backward) return false;
    final char ch = current.charAt(cursor - 1);
    if (!(ch > max || ch < min)) return false;
    cursor--;
    return true;
  }

  protected boolean eq_s(final int s_size, final String s) {
    if (limit - cursor < s_size) return false;
    int i;
    for (i = 0; i != s_size; i++) {
      if (current.charAt(cursor + i) != s.charAt(i)) return false;
    }
    cursor += s_size;
    return true;
  }

  protected boolean eq_s_b(final int s_size, final String s) {
    if (cursor - limit_backward < s_size) return false;
    int i;
    for (i = 0; i != s_size; i++) {
      if (current.charAt(cursor - s_size + i) != s.charAt(i)) return false;
    }
    cursor -= s_size;
    return true;
  }

  protected boolean eq_v(final StringBuffer s) {
    return eq_s(s.length(), s.toString());
  }

  protected boolean eq_v_b(final StringBuffer s) {
    return eq_s_b(s.length(), s.toString());
  }

  protected int find_among(final Among[] v, final int v_size) {
    int i = 0;
    int j = v_size;

    final int c = cursor;
    final int l = limit;

    int common_i = 0;
    int common_j = 0;

    boolean first_key_inspected = false;

    while (true) {
      final int k = i + ((j - i) >> 1);
      int diff = 0;
      int common = common_i < common_j ? common_i : common_j; // smaller
      final Among w = v[k];
      int i2;
      for (i2 = common; i2 < w.s_size; i2++) {
        if (c + common == l) {
          diff = -1;
          break;
        }
        diff = current.charAt(c + common) - w.s.charAt(i2);
        if (diff != 0) break;
        common++;
      }
      if (diff < 0) {
        j = k;
        common_j = common;
      }
      else {
        i = k;
        common_i = common;
      }
      if (j - i <= 1) {
        if (i > 0) break; // v->s has been inspected
        if (j == i) break; // only one item in v

        // - but now we need to go round once more to at
        // v->s inspected. This looks messy, but is actually
        // the optimal approach.

        if (first_key_inspected) break;
        first_key_inspected = true;
      }
    }
    while (true) {
      final Among w = v[i];
      if (common_i >= w.s_size) {
        cursor = c + w.s_size;
        if (w.method == null) return w.result;
        boolean res;
        try {
          final Object resobj = w.method.invoke(w.methodobject, ArrayTools.EMPTY_OBJECT_ARRAY);
          res = resobj.toString().equals("true");
        }
        catch (InvocationTargetException e) {
          res = false;
          // FIXME - debug message
        }
        catch (IllegalAccessException e) {
          res = false;
          // FIXME - debug message
        }
        cursor = c + w.s_size;
        if (res) return w.result;
      }
      i = w.substring_i;
      if (i < 0) return 0;
    }
  }

  // find_among_b is for backwards processing. Same comments apply
  protected final int find_among_b(final Among[] v, final int v_size) {
    int i = 0;
    int j = v_size;

    final CharSequence current1 = current;
    final int c = cursor;
    final int cMinus1 = c - 1;
    final int lb = limit_backward;
    final int cursorAndLimit = c - lb;

    int common_i = 0;
    int common_j = 0;

    boolean first_key_inspected = false;

    while (true) {
      final int k = i + ((j - i) >> 1);
      int diff = 0;
      int common = common_i < common_j ? common_i : common_j;
      final Among w = v[k];
      final String s = w.s;
      for (int i2 = w.s_size - 1 - common; i2 > -1; --i2) {
        if (cursorAndLimit == common) {
          diff = -1;
          break;
        }
        diff = current1.charAt(cMinus1 - common) - s.charAt(i2);
        if (diff != 0) break;
        ++common;
      }

      if (diff < 0) {
        j = k;
        common_j = common;
      }
      else {
        i = k;
        common_i = common;
      }

      if (j - i < 2) {
        if (i > 0 || j == i || first_key_inspected) break;
        first_key_inspected = true;
      }
    }

    while (true) {
      final Among w = v[i];
      if (common_i >= w.s_size) {
        cursor = c - w.s_size;
        return w.result;
      }
      i = w.substring_i;
      if (i < 0) {
        return 0;
      }
    }
  }

  /* to replace chars between c_bra and c_ket in current by the
  * chars in s.
  */
  protected int replace_s(final int c_bra, final int c_ket, final String s) {
    final int adjustment = s.length() - (c_ket - c_bra);
    current = new CharSeqComposite(new CharSequence[]{
      current.subSequence(0, c_bra), s,
      current.subSequence(c_ket, current.length())
    });
    limit += adjustment;
    if (cursor >= c_ket) cursor += adjustment;
    else if (cursor > c_bra) cursor = c_bra;
    return adjustment;
  }

  protected void slice_check() {
    if (bra < 0 ||
        bra > ket ||
        ket > limit ||
        limit > current.length())   // this line could be removed
    {
      System.err.println("faulty slice operation");
      // FIXME: report error somehow.
      /*
         fprintf(stderr, "faulty slice operation:\n");
         debug(z, -1, 0);
         exit(1);
         */
    }
  }

  protected void slice_from(final String s) {
    slice_check();
    replace_s(bra, ket, s);
  }

  protected void slice_from(final StringBuffer s) {
    slice_from(s.toString());
  }

  protected void slice_del() {
    slice_from("");
  }

  protected void insert(final int c_bra, final int c_ket, final String s) {
    final int adjustment = replace_s(c_bra, c_ket, s);
    if (c_bra <= bra) bra += adjustment;
    if (c_bra <= ket) ket += adjustment;
  }

  protected void insert(final int c_bra, final int c_ket, final StringBuffer s) {
    insert(c_bra, c_ket, s.toString());
  }

  /*
extern void debug(struct SN_env * z, int number, int line_count)
{   int i;
    int limit = SIZE(z->p);
    //if (number >= 0) printf("%3d (line %4d): '", number, line_count);
    if (number >= 0) printf("%3d (line %4d): [%d]'", number, line_count,limit);
    for (i = 0; i <= limit; i++)
    {   if (z->lb == i) printf("{");
        if (z->bra == i) printf("[");
        if (z->c == i) printf("|");
        if (z->ket == i) printf("]");
        if (z->l == i) printf("}");
        if (i < limit)
        {   int ch = z->p[i];
            if (ch == 0) ch = '#';
            printf("%c", ch);
        }
    }
    printf("'\n");
}
*/

}

