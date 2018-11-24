package com.expleague.commons.text.lemmer;

import java.util.List;

public interface MyStem {

  List<WordInfo> parse(CharSequence seq);
}
