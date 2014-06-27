package com.spbsu.commons.text.charset;

import org.jetbrains.annotations.NotNull;

/**
 * @author lyadzhin
 */
public interface TextDecoder {
  CharSequence decodeText(@NotNull byte[] bytes);
  CharSequence decodeText(@NotNull CharSequence bytes);
}