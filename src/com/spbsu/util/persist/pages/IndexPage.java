package com.spbsu.util.persist.pages;

import com.spbsu.util.persist.BTreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: igorkuralenok
 * Date: 18.04.2008
 * Time: 0:20:39
 * To change this template use File | Settings | File Templates.
 */
public abstract class IndexPage extends BTreePage{
  public IndexPage(BTreeMap owner, int id) {
    super(owner, id);
  }

  public IndexPage(BTreeMap owner) {
    super(owner);
  }
}
