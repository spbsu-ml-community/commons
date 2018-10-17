package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.CharSeq;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public enum GrammaticalCase {
    NOM("им", "именительный"),
    GEN("род", "родительный"),
    DAT("дат", "дательный"),
    ACC("вин", "винительный"),
    INS("твор", "творительный"),
    ABL("пр", "предложный"),
    PART("парт", "партитив (второй родительный)"),
    LOC("местн", "местный (второй предложный)"),
    VOC("зват", "звательный");

    private final String description;
    private final String shortName;

    GrammaticalCase(String shortName, String description) {
        this.shortName = shortName;
        this.description = description;
    }

    public String description() {
        return this.description;
    }

    @Nullable
    protected static GrammaticalCase parse(CharSeq shortName) {
        //noinspection EqualsBetweenInconvertibleTypes
        return Stream.of(GrammaticalCase.values()).filter(p -> shortName.equals(p.shortName)).findAny().orElse(null);
    }
}
