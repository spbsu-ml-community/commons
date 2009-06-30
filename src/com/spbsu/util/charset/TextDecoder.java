package com.spbsu.util.charset;

import org.jetbrains.annotations.NotNull;

/**
 * @author lyadzhin
 */
public interface TextDecoder {
  CharSequence decodeText(@NotNull byte[] bytes);
  CharSequence decodeText(@NotNull CharSequence bytes);
}