package com.expleague.commons.seq.regexp.converters;

import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.seq.regexp.NFA;
import com.expleague.commons.func.Converter;
import com.expleague.commons.math.vectors.impl.basis.IntBasis;
import com.expleague.commons.math.vectors.impl.vectors.SparseVec;
import com.expleague.commons.seq.regexp.Alphabet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: solar
 * Date: 28.05.12
 * Time: 16:15
 * To change this template use File | Settings | File Templates.
 */
public class NFA2VectorConverter implements Converter<NFA<Character>,Vec>{
  private final Alphabet<Character> alpha;

  public NFA2VectorConverter(final Alphabet<Character> alpha) {
    this.alpha = alpha;
  }

  @Override
  public Vec convertTo(final NFA<Character> source) {
    final int statesCount = source.states();
    final Vec result = new SparseVec(new IntBasis(alpha.size() * (statesCount * (statesCount + 1) / 2)).size());
    int index = 0;
    for (int stateA = 0; stateA < statesCount - 1; stateA++) {
      for (int stateB = stateA; stateB < statesCount; stateB++) {
        for (int i = 0; i < alpha.size(); i++) {
          if (source.connected(stateA, stateB, i))
            result.set(index, 1);
          index++;
        }
      }
    }
    return result;
  }

  @Override
  public NFA<Character> convertFrom(final Vec source) {
    final TIntHashSet finalStates = new TIntHashSet();
    final int a = source.dim() / alpha.size();
    final int statesCount = (int) ((Math.sqrt(8 * a + 1) - 1) / 2.); //
    finalStates.add(statesCount-1);
    final ArrayList<TLongHashSet> programs = new ArrayList<TLongHashSet>();
    int index = 0;
    for (int from = 0; from < statesCount - 1; from++) {
      final TLongHashSet program = new TLongHashSet();
      programs.add(program);
      for (int to = from; to < statesCount; to++) {
        for (int c = 0; c < alpha.size(); c++) {
          if (source.get(index) > 0)
            program.add(((long)to << 32) | c);
          index++;
        }
      }
    }
    return new NFA<Character>(programs, finalStates, alpha);
  }
}
