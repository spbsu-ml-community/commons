package com.expleague.commons.text;

import com.expleague.commons.FileTestCase;
import com.expleague.commons.seq.CharSeqTools;
import com.expleague.commons.text.parser.TextParser;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TextParserTest extends FileTestCase {
    public void testEnum() {
        final TextParser parser = new TextParser();
        {
            final StringBuilder builder = new StringBuilder();
            TextParser.normalizeCodePoint('℃', builder);
            assertEquals("°c", builder.toString());
        }
        {
            final Text text = parser.parse("拜杰（Baijie）电子计时器 多功能电子定时器 烘焙倒计时器磁吸 桌面时钟提醒器大屏幕 迷你学生闹钟cyd-96");
            System.out.println(text);
        }
        {
            final Text text = parser.parse("недоперепил");
            System.out.println(text);
        }
        {
            final Text text = parser.parse("Баал–Цафон");
            System.out.println(text);
        }
        {
            final Text text = parser.parse("41°18’14.1°n 81°54’06.1″W");
            System.out.println(text);
        }
        {
            final Text text = parser.parse("И т.д.");
            System.out.println(text);
        }
        {
            final Text text = parser.parse("sentence end»");
            System.out.println(text);
        }
        {
            final Text text = parser.parse("VII) В случае");
            System.out.println(text);
        }
        {
            final Text text = parser.parse("а также: многое");
            System.out.println(text);
        }
    }

    @Override
    protected String getInputFileExtension() {
        return ".in.txt";
    }

    @Override
    protected String getResultFileExtension() {
        return ".out.txt";
    }

    @Override
    protected String getTestDataPath() {
        try {
            final URI classResource = Objects.requireNonNull(TextParserTest.class.getResource("/")).toURI();
            final Path resourceRoot = Paths.get(classResource).getParent().getParent();
            return resourceRoot.resolve("src/test/resources/com/huawei/sparklesearch/rri/ique/text/parser/")
                .toAbsolutePath() + "/";
        } catch (final URISyntaxException urise) {
            throw new RuntimeException(urise);
        }
    }
}
