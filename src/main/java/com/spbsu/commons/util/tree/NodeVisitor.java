package com.spbsu.commons.util.tree;

import com.spbsu.commons.util.tree.impl.node.InternalNode;
import com.spbsu.commons.util.tree.impl.node.LeafNode;

/**
 * User: qdeee
 * Date: 14.07.14
 */
public interface NodeVisitor<R> {
  R visit(InternalNode node);
  R visit(LeafNode node);
}
