package com.spbsu.commons.seq;

import com.spbsu.commons.math.vectors.Vec;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vkokarev on 19.02.15.
 */
public class VecSeqBuilder implements SeqBuilder<Vec> {
    final List<Vec> vecs = new ArrayList<>();
    @Override
    public SeqBuilder<Vec> add(final Vec vec) {
        vecs.add(vec);
        return this;
    }

    @Override
    public SeqBuilder<Vec> addAll(final Seq<Vec> values) {
        for (int i = 0; i < values.length(); ++i) {
            add(values.at(i));
        }
        return this;
    }

    @Override
    public VecSeq build() {
        return new VecSeq(vecs.toArray(new Vec[vecs.size()]));
    }
}
