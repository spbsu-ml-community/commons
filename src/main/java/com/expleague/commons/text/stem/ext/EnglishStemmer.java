package com.expleague.commons.text.stem.ext;
// This file was generated automatically by the Snowball to Java compiler

import com.expleague.commons.text.stem.Among;
import com.expleague.commons.text.stem.AbstractStemmer;

/**
 * Generated class implementing code defined by a snowball script.
 */
public class EnglishStemmer extends AbstractStemmer {

  private final Among[] a_0 = {
      new Among("commun", -1, -1, "", this),
      new Among("gener", -1, -1, "", this)
  };

  private final Among[] a_1 = {
      new Among("'", -1, 1, "", this),
      new Among("'s'", 0, 1, "", this),
      new Among("'s", -1, 1, "", this)
  };

  private final Among[] a_2 = {
      new Among("ied", -1, 2, "", this),
      new Among("s", -1, 3, "", this),
      new Among("ies", 1, 2, "", this),
      new Among("sses", 1, 1, "", this),
      new Among("ss", 1, -1, "", this),
      new Among("us", 1, -1, "", this)
  };

  private final Among[] a_3 = {
      new Among("", -1, 3, "", this),
      new Among("bb", 0, 2, "", this),
      new Among("dd", 0, 2, "", this),
      new Among("ff", 0, 2, "", this),
      new Among("gg", 0, 2, "", this),
      new Among("bl", 0, 1, "", this),
      new Among("mm", 0, 2, "", this),
      new Among("nn", 0, 2, "", this),
      new Among("pp", 0, 2, "", this),
      new Among("rr", 0, 2, "", this),
      new Among("at", 0, 1, "", this),
      new Among("tt", 0, 2, "", this),
      new Among("iz", 0, 1, "", this)
  };

  private final Among[] a_4 = {
      new Among("ed", -1, 2, "", this),
      new Among("eed", 0, 1, "", this),
      new Among("ing", -1, 2, "", this),
      new Among("edly", -1, 2, "", this),
      new Among("eedly", 3, 1, "", this),
      new Among("ingly", -1, 2, "", this)
  };

  private final Among[] a_5 = {
      new Among("anci", -1, 3, "", this),
      new Among("enci", -1, 2, "", this),
      new Among("ogi", -1, 13, "", this),
      new Among("li", -1, 16, "", this),
      new Among("bli", 3, 12, "", this),
      new Among("abli", 4, 4, "", this),
      new Among("alli", 3, 8, "", this),
      new Among("fulli", 3, 14, "", this),
      new Among("lessli", 3, 15, "", this),
      new Among("ousli", 3, 10, "", this),
      new Among("entli", 3, 5, "", this),
      new Among("aliti", -1, 8, "", this),
      new Among("biliti", -1, 12, "", this),
      new Among("iviti", -1, 11, "", this),
      new Among("tional", -1, 1, "", this),
      new Among("ational", 14, 7, "", this),
      new Among("alism", -1, 8, "", this),
      new Among("ation", -1, 7, "", this),
      new Among("ization", 17, 6, "", this),
      new Among("izer", -1, 6, "", this),
      new Among("ator", -1, 7, "", this),
      new Among("iveness", -1, 11, "", this),
      new Among("fulness", -1, 9, "", this),
      new Among("ousness", -1, 10, "", this)
  };

  private final Among[] a_6 = {
      new Among("icate", -1, 4, "", this),
      new Among("ative", -1, 6, "", this),
      new Among("alize", -1, 3, "", this),
      new Among("iciti", -1, 4, "", this),
      new Among("ical", -1, 4, "", this),
      new Among("tional", -1, 1, "", this),
      new Among("ational", 5, 2, "", this),
      new Among("ful", -1, 5, "", this),
      new Among("ness", -1, 5, "", this)
  };

  private final Among[] a_7 = {
      new Among("ic", -1, 1, "", this),
      new Among("ance", -1, 1, "", this),
      new Among("ence", -1, 1, "", this),
      new Among("able", -1, 1, "", this),
      new Among("ible", -1, 1, "", this),
      new Among("ate", -1, 1, "", this),
      new Among("ive", -1, 1, "", this),
      new Among("ize", -1, 1, "", this),
      new Among("iti", -1, 1, "", this),
      new Among("al", -1, 1, "", this),
      new Among("ism", -1, 1, "", this),
      new Among("ion", -1, 2, "", this),
      new Among("er", -1, 1, "", this),
      new Among("ous", -1, 1, "", this),
      new Among("ant", -1, 1, "", this),
      new Among("ent", -1, 1, "", this),
      new Among("ment", 15, 1, "", this),
      new Among("ement", 16, 1, "", this)
  };

  private final Among[] a_8 = {
      new Among("e", -1, 1, "", this),
      new Among("l", -1, 2, "", this)
  };

  private final Among[] a_9 = {
      new Among("succeed", -1, -1, "", this),
      new Among("proceed", -1, -1, "", this),
      new Among("exceed", -1, -1, "", this),
      new Among("canning", -1, -1, "", this),
      new Among("inning", -1, -1, "", this),
      new Among("earring", -1, -1, "", this),
      new Among("herring", -1, -1, "", this),
      new Among("outing", -1, -1, "", this)
  };

  private final Among[] a_10 = {
      new Among("andes", -1, -1, "", this),
      new Among("atlas", -1, -1, "", this),
      new Among("bias", -1, -1, "", this),
      new Among("cosmos", -1, -1, "", this),
      new Among("dying", -1, 3, "", this),
      new Among("early", -1, 9, "", this),
      new Among("gently", -1, 7, "", this),
      new Among("howe", -1, -1, "", this),
      new Among("idly", -1, 6, "", this),
      new Among("lying", -1, 4, "", this),
      new Among("news", -1, -1, "", this),
      new Among("only", -1, 10, "", this),
      new Among("singly", -1, 11, "", this),
      new Among("skies", -1, 2, "", this),
      new Among("skis", -1, 1, "", this),
      new Among("sky", -1, -1, "", this),
      new Among("tying", -1, 5, "", this),
      new Among("ugly", -1, 8, "", this)
  };

  private static final char g_v[] = {17, 65, 16, 1};

  private static final char g_v_WXY[] = {1, 17, 65, 208, 1};

  private static final char g_valid_LI[] = {55, 141, 2};

  private boolean B_Y_found;
  private int I_p2;
  private int I_p1;

  private void copy_from(final EnglishStemmer other) {
    B_Y_found = other.B_Y_found;
    I_p2 = other.I_p2;
    I_p1 = other.I_p1;
    super.copy_from(other);
  }

  private boolean r_prelude() {
    final int v_1;
    final int v_2;
    final int v_3;
    int v_4;
    int v_5;
    // (, line 25
    // unset Y_found, line 26
    B_Y_found = false;
    // do, line 27
    v_1 = cursor;
    lab0:
    do {
      // (, line 27
      // [, line 27
      bra = cursor;
      // literal, line 27
      if (!(eq_s(1, "'"))) {
        break lab0;
      }
      // ], line 27
      ket = cursor;
      // delete, line 27
      slice_del();
    }
    while (false);
    cursor = v_1;
    // do, line 28
    v_2 = cursor;
    lab1:
    do {
      // (, line 28
      // [, line 28
      bra = cursor;
      // literal, line 28
      if (!(eq_s(1, "y"))) {
        break lab1;
      }
      // ], line 28
      ket = cursor;
      // <-, line 28
      slice_from("Y");
      // set Y_found, line 28
      B_Y_found = true;
    }
    while (false);
    cursor = v_2;
    // do, line 29
    v_3 = cursor;
    lab2:
    do {
      // repeat, line 29
      replab3:
      while (true) {
        v_4 = cursor;
        lab4:
        do {
          // (, line 29
          // goto, line 29
          golab5:
          while (true) {
            v_5 = cursor;
            lab6:
            do {
              // (, line 29
              if (!(in_grouping(g_v, 97, 121))) {
                break lab6;
              }
              // [, line 29
              bra = cursor;
              // literal, line 29
              if (!(eq_s(1, "y"))) {
                break lab6;
              }
              // ], line 29
              ket = cursor;
              cursor = v_5;
              break golab5;
            }
            while (false);
            cursor = v_5;
            if (cursor >= limit) {
              break lab4;
            }
            cursor++;
          }
          // <-, line 29
          slice_from("Y");
          // set Y_found, line 29
          B_Y_found = true;
          continue replab3;
        }
        while (false);
        cursor = v_4;
        break replab3;
      }
    }
    while (false);
    cursor = v_3;
    return true;
  }

  private boolean r_mark_regions() {
    final int v_1;
    final int v_2;
    // (, line 32
    I_p1 = limit;
    I_p2 = limit;
    // do, line 35
    v_1 = cursor;
    lab0:
    do {
      // (, line 35
      // or, line 40
      lab1:
      do {
        v_2 = cursor;
        lab2:
        do {
          // among, line 36
          if (find_among(a_0, 2) == 0) {
            break lab2;
          }
          break lab1;
        }
        while (false);
        cursor = v_2;
        // (, line 40
        // gopast, line 40
        golab3:
        while (true) {
          lab4:
          do {
            if (!(in_grouping(g_v, 97, 121))) {
              break lab4;
            }
            break golab3;
          }
          while (false);
          if (cursor >= limit) {
            break lab0;
          }
          cursor++;
        }
        // gopast, line 40
        golab5:
        while (true) {
          lab6:
          do {
            if (!(out_grouping(g_v, 97, 121))) {
              break lab6;
            }
            break golab5;
          }
          while (false);
          if (cursor >= limit) {
            break lab0;
          }
          cursor++;
        }
      }
      while (false);
      // setmark p1, line 41
      I_p1 = cursor;
      // gopast, line 42
      golab7:
      while (true) {
        lab8:
        do {
          if (!(in_grouping(g_v, 97, 121))) {
            break lab8;
          }
          break golab7;
        }
        while (false);
        if (cursor >= limit) {
          break lab0;
        }
        cursor++;
      }
      // gopast, line 42
      golab9:
      while (true) {
        lab10:
        do {
          if (!(out_grouping(g_v, 97, 121))) {
            break lab10;
          }
          break golab9;
        }
        while (false);
        if (cursor >= limit) {
          break lab0;
        }
        cursor++;
      }
      // setmark p2, line 42
      I_p2 = cursor;
    }
    while (false);
    cursor = v_1;
    return true;
  }

  private boolean r_shortv() {
    final int v_1;
    // (, line 48
    // or, line 50
    lab0:
    do {
      v_1 = limit - cursor;
      lab1:
      do {
        // (, line 49
        if (!(out_grouping_b(g_v_WXY, 89, 121))) {
          break lab1;
        }
        if (!(in_grouping_b(g_v, 97, 121))) {
          break lab1;
        }
        if (!(out_grouping_b(g_v, 97, 121))) {
          break lab1;
        }
        break lab0;
      }
      while (false);
      cursor = limit - v_1;
      // (, line 51
      if (!(out_grouping_b(g_v, 97, 121))) {
        return false;
      }
      if (!(in_grouping_b(g_v, 97, 121))) {
        return false;
      }
      // atlimit, line 51
      if (cursor > limit_backward) {
        return false;
      }
    }
    while (false);
    return true;
  }

  private boolean r_R1() {
    if (!(I_p1 <= cursor)) {
      return false;
    }
    return true;
  }

  private boolean r_R2() {
    if (!(I_p2 <= cursor)) {
      return false;
    }
    return true;
  }

  private boolean r_Step_1a() {
    int among_var;
    final int v_1;
    final int v_2;
    // (, line 57
    // try, line 58
    v_1 = limit - cursor;
    lab0:
    do {
      // (, line 58
      // [, line 59
      ket = cursor;
      // substring, line 59
      among_var = find_among_b(a_1, 3);
      if (among_var == 0) {
        cursor = limit - v_1;
        break lab0;
      }
      // ], line 59
      bra = cursor;
      switch (among_var) {
        case 0:
          cursor = limit - v_1;
          break lab0;
        case 1:
          // (, line 61
          // delete, line 61
          slice_del();
          break;
      }
    }
    while (false);
    // [, line 64
    ket = cursor;
    // substring, line 64
    among_var = find_among_b(a_2, 6);
    if (among_var == 0) {
      return false;
    }
    // ], line 64
    bra = cursor;
    switch (among_var) {
      case 0:
        return false;
      case 1:
        // (, line 65
        // <-, line 65
        slice_from("ss");
        break;
      case 2:
        // (, line 67
        // or, line 67
        lab1:
        do {
          v_2 = limit - cursor;
          lab2:
          do {
            // (, line 67
            // hop, line 67
            {
              final int c = cursor - 2;
              if (limit_backward > c || c > limit) {
                break lab2;
              }
              cursor = c;
            }
            // <-, line 67
            slice_from("i");
            break lab1;
          }
          while (false);
          cursor = limit - v_2;
          // <-, line 67
          slice_from("ie");
        }
        while (false);
        break;
      case 3:
        // (, line 68
        // next, line 68
        if (cursor <= limit_backward) {
          return false;
        }
        cursor--;
        // gopast, line 68
        golab3:
        while (true) {
          lab4:
          do {
            if (!(in_grouping_b(g_v, 97, 121))) {
              break lab4;
            }
            break golab3;
          }
          while (false);
          if (cursor <= limit_backward) {
            return false;
          }
          cursor--;
        }
        // delete, line 68
        slice_del();
        break;
    }
    return true;
  }

  private boolean r_Step_1b() {
    int among_var;
    final int v_1;
    final int v_3;
    final int v_4;
    // (, line 73
    // [, line 74
    ket = cursor;
    // substring, line 74
    among_var = find_among_b(a_4, 6);
    if (among_var == 0) {
      return false;
    }
    // ], line 74
    bra = cursor;
    switch (among_var) {
      case 0:
        return false;
      case 1:
        // (, line 76
        // call R1, line 76
        if (!r_R1()) {
          return false;
        }
        // <-, line 76
        slice_from("ee");
        break;
      case 2:
        // (, line 78
        // test, line 79
        v_1 = limit - cursor;
        // gopast, line 79
        golab0:
        while (true) {
          lab1:
          do {
            if (!(in_grouping_b(g_v, 97, 121))) {
              break lab1;
            }
            break golab0;
          }
          while (false);
          if (cursor <= limit_backward) {
            return false;
          }
          cursor--;
        }
        cursor = limit - v_1;
        // delete, line 79
        slice_del();
        // test, line 80
        v_3 = limit - cursor;
        // substring, line 80
        among_var = find_among_b(a_3, 13);
        if (among_var == 0) {
          return false;
        }
        cursor = limit - v_3;
        switch (among_var) {
          case 0:
            return false;
          case 1:
            // (, line 82
            // <+, line 82
          {
            final int c = cursor;
            insert(cursor, cursor, "e");
            cursor = c;
          }
          break;
          case 2:
            // (, line 85
            // [, line 85
            ket = cursor;
            // next, line 85
            if (cursor <= limit_backward) {
              return false;
            }
            cursor--;
            // ], line 85
            bra = cursor;
            // delete, line 85
            slice_del();
            break;
          case 3:
            // (, line 86
            // atmark, line 86
            if (cursor != I_p1) {
              return false;
            }
            // test, line 86
            v_4 = limit - cursor;
            // call shortv, line 86
            if (!r_shortv()) {
              return false;
            }
            cursor = limit - v_4;
            // <+, line 86
          {
            final int c = cursor;
            insert(cursor, cursor, "e");
            cursor = c;
          }
          break;
        }
        break;
    }
    return true;
  }

  private boolean r_Step_1c() {
    final int v_1;
    final int v_2;
    // (, line 92
    // [, line 93
    ket = cursor;
    // or, line 93
    lab0:
    do {
      v_1 = limit - cursor;
      lab1:
      do {
        // literal, line 93
        if (!(eq_s_b(1, "y"))) {
          break lab1;
        }
        break lab0;
      }
      while (false);
      cursor = limit - v_1;
      // literal, line 93
      if (!(eq_s_b(1, "Y"))) {
        return false;
      }
    }
    while (false);
    // ], line 93
    bra = cursor;
    if (!(out_grouping_b(g_v, 97, 121))) {
      return false;
    }
    // not, line 94
    {
      v_2 = limit - cursor;
      lab2:
      do {
        // atlimit, line 94
        if (cursor > limit_backward) {
          break lab2;
        }
        return false;
      }
      while (false);
      cursor = limit - v_2;
    }
    // <-, line 95
    slice_from("i");
    return true;
  }

  private boolean r_Step_2() {
    final int among_var;
    // (, line 98
    // [, line 99
    ket = cursor;
    // substring, line 99
    among_var = find_among_b(a_5, 24);
    if (among_var == 0) {
      return false;
    }
    // ], line 99
    bra = cursor;
    // call R1, line 99
    if (!r_R1()) {
      return false;
    }
    switch (among_var) {
      case 0:
        return false;
      case 1:
        // (, line 100
        // <-, line 100
        slice_from("tion");
        break;
      case 2:
        // (, line 101
        // <-, line 101
        slice_from("ence");
        break;
      case 3:
        // (, line 102
        // <-, line 102
        slice_from("ance");
        break;
      case 4:
        // (, line 103
        // <-, line 103
        slice_from("able");
        break;
      case 5:
        // (, line 104
        // <-, line 104
        slice_from("ent");
        break;
      case 6:
        // (, line 106
        // <-, line 106
        slice_from("ize");
        break;
      case 7:
        // (, line 108
        // <-, line 108
        slice_from("ate");
        break;
      case 8:
        // (, line 110
        // <-, line 110
        slice_from("al");
        break;
      case 9:
        // (, line 111
        // <-, line 111
        slice_from("ful");
        break;
      case 10:
        // (, line 113
        // <-, line 113
        slice_from("ous");
        break;
      case 11:
        // (, line 115
        // <-, line 115
        slice_from("ive");
        break;
      case 12:
        // (, line 117
        // <-, line 117
        slice_from("ble");
        break;
      case 13:
        // (, line 118
        // literal, line 118
        if (!(eq_s_b(1, "l"))) {
          return false;
        }
        // <-, line 118
        slice_from("og");
        break;
      case 14:
        // (, line 119
        // <-, line 119
        slice_from("ful");
        break;
      case 15:
        // (, line 120
        // <-, line 120
        slice_from("less");
        break;
      case 16:
        // (, line 121
        if (!(in_grouping_b(g_valid_LI, 99, 116))) {
          return false;
        }
        // delete, line 121
        slice_del();
        break;
    }
    return true;
  }

  private boolean r_Step_3() {
    final int among_var;
    // (, line 125
    // [, line 126
    ket = cursor;
    // substring, line 126
    among_var = find_among_b(a_6, 9);
    if (among_var == 0) {
      return false;
    }
    // ], line 126
    bra = cursor;
    // call R1, line 126
    if (!r_R1()) {
      return false;
    }
    switch (among_var) {
      case 0:
        return false;
      case 1:
        // (, line 127
        // <-, line 127
        slice_from("tion");
        break;
      case 2:
        // (, line 128
        // <-, line 128
        slice_from("ate");
        break;
      case 3:
        // (, line 129
        // <-, line 129
        slice_from("al");
        break;
      case 4:
        // (, line 131
        // <-, line 131
        slice_from("ic");
        break;
      case 5:
        // (, line 133
        // delete, line 133
        slice_del();
        break;
      case 6:
        // (, line 135
        // call R2, line 135
        if (!r_R2()) {
          return false;
        }
        // delete, line 135
        slice_del();
        break;
    }
    return true;
  }

  private boolean r_Step_4() {
    final int among_var;
    final int v_1;
    // (, line 139
    // [, line 140
    ket = cursor;
    // substring, line 140
    among_var = find_among_b(a_7, 18);
    if (among_var == 0) {
      return false;
    }
    // ], line 140
    bra = cursor;
    // call R2, line 140
    if (!r_R2()) {
      return false;
    }
    switch (among_var) {
      case 0:
        return false;
      case 1:
        // (, line 143
        // delete, line 143
        slice_del();
        break;
      case 2:
        // (, line 144
        // or, line 144
        lab0:
        do {
          v_1 = limit - cursor;
          lab1:
          do {
            // literal, line 144
            if (!(eq_s_b(1, "s"))) {
              break lab1;
            }
            break lab0;
          }
          while (false);
          cursor = limit - v_1;
          // literal, line 144
          if (!(eq_s_b(1, "t"))) {
            return false;
          }
        }
        while (false);
        // delete, line 144
        slice_del();
        break;
    }
    return true;
  }

  private boolean r_Step_5() {
    final int among_var;
    final int v_1;
    final int v_2;
    // (, line 148
    // [, line 149
    ket = cursor;
    // substring, line 149
    among_var = find_among_b(a_8, 2);
    if (among_var == 0) {
      return false;
    }
    // ], line 149
    bra = cursor;
    switch (among_var) {
      case 0:
        return false;
      case 1:
        // (, line 150
        // or, line 150
        lab0:
        do {
          v_1 = limit - cursor;
          lab1:
          do {
            // call R2, line 150
            if (!r_R2()) {
              break lab1;
            }
            break lab0;
          }
          while (false);
          cursor = limit - v_1;
          // (, line 150
          // call R1, line 150
          if (!r_R1()) {
            return false;
          }
          // not, line 150
          {
            v_2 = limit - cursor;
            lab2:
            do {
              // call shortv, line 150
              if (!r_shortv()) {
                break lab2;
              }
              return false;
            }
            while (false);
            cursor = limit - v_2;
          }
        }
        while (false);
        // delete, line 150
        slice_del();
        break;
      case 2:
        // (, line 151
        // call R2, line 151
        if (!r_R2()) {
          return false;
        }
        // literal, line 151
        if (!(eq_s_b(1, "l"))) {
          return false;
        }
        // delete, line 151
        slice_del();
        break;
    }
    return true;
  }

  private boolean r_exception2() {
    // (, line 155
    // [, line 157
    ket = cursor;
    // substring, line 157
    if (find_among_b(a_9, 8) == 0) {
      return false;
    }
    // ], line 157
    bra = cursor;
    // atlimit, line 157
    if (cursor > limit_backward) {
      return false;
    }
    return true;
  }

  private boolean r_exception1() {
    final int among_var;
    // (, line 167
    // [, line 169
    bra = cursor;
    // substring, line 169
    among_var = find_among(a_10, 18);
    if (among_var == 0) {
      return false;
    }
    // ], line 169
    ket = cursor;
    // atlimit, line 169
    if (cursor < limit) {
      return false;
    }
    switch (among_var) {
      case 0:
        return false;
      case 1:
        // (, line 173
        // <-, line 173
        slice_from("ski");
        break;
      case 2:
        // (, line 174
        // <-, line 174
        slice_from("sky");
        break;
      case 3:
        // (, line 175
        // <-, line 175
        slice_from("die");
        break;
      case 4:
        // (, line 176
        // <-, line 176
        slice_from("lie");
        break;
      case 5:
        // (, line 177
        // <-, line 177
        slice_from("tie");
        break;
      case 6:
        // (, line 181
        // <-, line 181
        slice_from("idl");
        break;
      case 7:
        // (, line 182
        // <-, line 182
        slice_from("gentl");
        break;
      case 8:
        // (, line 183
        // <-, line 183
        slice_from("ugli");
        break;
      case 9:
        // (, line 184
        // <-, line 184
        slice_from("earli");
        break;
      case 10:
        // (, line 185
        // <-, line 185
        slice_from("onli");
        break;
      case 11:
        // (, line 186
        // <-, line 186
        slice_from("singl");
        break;
    }
    return true;
  }

  private boolean r_postlude() {
    int v_1;
    int v_2;
    // (, line 202
    // Boolean test Y_found, line 202
    if (!(B_Y_found)) {
      return false;
    }
    // repeat, line 202
    replab0:
    while (true) {
      v_1 = cursor;
      lab1:
      do {
        // (, line 202
        // goto, line 202
        golab2:
        while (true) {
          v_2 = cursor;
          lab3:
          do {
            // (, line 202
            // [, line 202
            bra = cursor;
            // literal, line 202
            if (!(eq_s(1, "Y"))) {
              break lab3;
            }
            // ], line 202
            ket = cursor;
            cursor = v_2;
            break golab2;
          }
          while (false);
          cursor = v_2;
          if (cursor >= limit) {
            break lab1;
          }
          cursor++;
        }
        // <-, line 202
        slice_from("y");
        continue replab0;
      }
      while (false);
      cursor = v_1;
      break replab0;
    }
    return true;
  }

  @Override
  public boolean stem() {
    final int v_1;
    final int v_2;
    final int v_3;
    final int v_4;
    final int v_5;
    final int v_6;
    final int v_7;
    final int v_8;
    final int v_9;
    final int v_10;
    final int v_11;
    final int v_12;
    final int v_13;
    // (, line 204
    // or, line 206
    lab0:
    do {
      v_1 = cursor;
      lab1:
      do {
        // call exception1, line 206
        if (!r_exception1()) {
          break lab1;
        }
        break lab0;
      }
      while (false);
      cursor = v_1;
      lab2:
      do {
        // not, line 207
        {
          v_2 = cursor;
          lab3:
          do {
            // hop, line 207
            {
              final int c = cursor + 3;
              if (0 > c || c > limit) {
                break lab3;
              }
              cursor = c;
            }
            break lab2;
          }
          while (false);
          cursor = v_2;
        }
        break lab0;
      }
      while (false);
      cursor = v_1;
      // (, line 207
      // do, line 208
      v_3 = cursor;
      lab4:
      do {
        // call prelude, line 208
        if (!r_prelude()) {
          break lab4;
        }
      }
      while (false);
      cursor = v_3;
      // do, line 209
      v_4 = cursor;
      lab5:
      do {
        // call mark_regions, line 209
        if (!r_mark_regions()) {
          break lab5;
        }
      }
      while (false);
      cursor = v_4;
      // backwards, line 210
      limit_backward = cursor;
      cursor = limit;
      // (, line 210
      // do, line 212
      v_5 = limit - cursor;
      lab6:
      do {
        // call Step_1a, line 212
        if (!r_Step_1a()) {
          break lab6;
        }
      }
      while (false);
      cursor = limit - v_5;
      // or, line 214
      lab7:
      do {
        v_6 = limit - cursor;
        lab8:
        do {
          // call exception2, line 214
          if (!r_exception2()) {
            break lab8;
          }
          break lab7;
        }
        while (false);
        cursor = limit - v_6;
        // (, line 214
        // do, line 216
        v_7 = limit - cursor;
        lab9:
        do {
          // call Step_1b, line 216
          if (!r_Step_1b()) {
            break lab9;
          }
        }
        while (false);
        cursor = limit - v_7;
        // do, line 217
        v_8 = limit - cursor;
        lab10:
        do {
          // call Step_1c, line 217
          if (!r_Step_1c()) {
            break lab10;
          }
        }
        while (false);
        cursor = limit - v_8;
        // do, line 219
        v_9 = limit - cursor;
        lab11:
        do {
          // call Step_2, line 219
          if (!r_Step_2()) {
            break lab11;
          }
        }
        while (false);
        cursor = limit - v_9;
        // do, line 220
        v_10 = limit - cursor;
        lab12:
        do {
          // call Step_3, line 220
          if (!r_Step_3()) {
            break lab12;
          }
        }
        while (false);
        cursor = limit - v_10;
        // do, line 221
        v_11 = limit - cursor;
        lab13:
        do {
          // call Step_4, line 221
          if (!r_Step_4()) {
            break lab13;
          }
        }
        while (false);
        cursor = limit - v_11;
        // do, line 223
        v_12 = limit - cursor;
        lab14:
        do {
          // call Step_5, line 223
          if (!r_Step_5()) {
            break lab14;
          }
        }
        while (false);
        cursor = limit - v_12;
      }
      while (false);
      cursor = limit_backward;                // do, line 226
      v_13 = cursor;
      lab15:
      do {
        // call postlude, line 226
        if (!r_postlude()) {
          break lab15;
        }
      }
      while (false);
      cursor = v_13;
    }
    while (false);
    return true;
  }

}

