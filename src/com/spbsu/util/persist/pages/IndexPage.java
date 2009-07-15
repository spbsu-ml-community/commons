package com.spbsu.util.persist.pages;

import com.spbsu.util.persist.BTreeMap;

/**
 * User: igorkuralenok
 * Date: 18.04.2008
 */
public abstract class IndexPage extends BTreePage{
  public IndexPage(BTreeMap owner, int id) {
    super(owner, id);
  }

  public IndexPage(BTreeMap owner) {
    super(owner);
  }
}
