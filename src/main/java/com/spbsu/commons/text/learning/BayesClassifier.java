package com.spbsu.commons.text.learning;



import java.util.*;

import com.spbsu.commons.util.Holder;
import com.spbsu.commons.func.Processor;
import gnu.trove.function.TDoubleFunction;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.procedure.TObjectDoubleProcedure;
import gnu.trove.procedure.TObjectProcedure;

/**
 * User: terry
 * Date: 04.10.2009
 */
public class BayesClassifier<T> extends BaseTextClassifier<T> {

  final TObjectDoubleHashMap<T> class2Pclass;
  final Map<T, TObjectDoubleHashMap<CharSequence>> class2PtermInClass;

  public BayesClassifier() {
    class2Pclass = new TObjectDoubleHashMap<T>();
    class2PtermInClass = new HashMap<T, TObjectDoubleHashMap<CharSequence>>();
  }

  public T classify(CharSequence charSequence) {
    final Map<T, Holder<Double>> class2Wieght = new HashMap<T, Holder<Double>>();
    class2Pclass.forEachEntry(new TObjectDoubleProcedure<T>() {
      public boolean execute(T t, double v) {
        class2Wieght.put(t, new Holder<Double>(v));
        return true;
      }
    });
    processText(charSequence, new Processor<CharSequence>() {
      public void process(CharSequence data) {
        for (Map.Entry<T, TObjectDoubleHashMap<CharSequence>> entry : class2PtermInClass.entrySet()) {
          final double ptremInClass = entry.getValue().get(data);
          final Holder<Double> classWieght = class2Wieght.get(entry.getKey());
          classWieght.setValue(classWieght.getValue() * ptremInClass);
        }
      }
    });
    return Collections.max(class2Wieght.entrySet(), new Comparator<Map.Entry<T, Holder<Double>>>() {
      public int compare(Map.Entry<T, Holder<Double>> o1, Map.Entry<T, Holder<Double>> o2) {
        if (o1.getValue().getValue() > o2.getValue().getValue()) return -1;
        if (o1.getValue().getValue() < o2.getValue().getValue()) return 1;
        return 0;
      }
    }).getKey();
  }

  public void learn(Map<T, Collection<CharSequence>> dataSet) {
    double denominator = 0.0;
    for (Map.Entry<T, Collection<CharSequence>> entry : dataSet.entrySet()) {
      denominator += entry.getValue().size();
    }
    for (Map.Entry<T, Collection<CharSequence>> entry : dataSet.entrySet()) {
      final Collection<CharSequence> textsCollection = entry.getValue();
      class2Pclass.put(entry.getKey(), textsCollection.size() / denominator);
      final TObjectDoubleHashMap<CharSequence> ptermInClass = new TObjectDoubleHashMap<CharSequence>();
      class2PtermInClass.put(entry.getKey(), ptermInClass);
      final TObjectDoubleHashMap<CharSequence> textVector = new TObjectDoubleHashMap<CharSequence>();
      for (CharSequence text : entry.getValue()) {
        processText(text, new Processor<CharSequence>() {
          public void process(CharSequence term) {
            textVector.adjustOrPutValue(term, 1, 1);
          }
        });
      }
      textVector.forEachKey(new TObjectProcedure<CharSequence>() {
        public boolean execute(CharSequence term) {
          ptermInClass.adjustOrPutValue(term, 1, 1);
          return true;
        }
      });
      ptermInClass.transformValues(new TDoubleFunction() {
        public double execute(double v) {
          return v / textsCollection.size();
        }
      });
    }
  }
}
