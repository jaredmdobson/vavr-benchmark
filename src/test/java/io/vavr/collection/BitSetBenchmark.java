/*  __    __  __  __    __  ___
 * \  \  /  /    \  \  /  /  __/
 *  \  \/  /  /\  \  \/  /  /
 *   \____/__/  \__\____/__/
 *
 * Copyright 2014-2019 Vavr, http://vavr.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vavr.collection;

import io.vavr.JmhRunner;
import org.openjdk.jmh.annotations.*;

import static io.vavr.JmhRunner.create;
import static io.vavr.JmhRunner.getRandomValues;
import static io.vavr.collection.Collections.areEqual;
import static scala.collection.JavaConverters.asJavaCollection;
import static scala.collection.JavaConverters.asScalaBuffer;

public class BitSetBenchmark {
  static final Array<Class<?>> CLASSES = Array.of(
      AddAll.class,
      Iterate.class
  );

  public static void main(String... args) {
    JmhRunner.runNormalNoAsserts(CLASSES);
  }

  public static class Base extends CollectionBenchmarkBase {

    int EXPECTED_AGGREGATE;
    int[] ELEMENTS;
    TreeSet<Integer> DISTINCT;

    scala.collection.immutable.BitSet scalaImmutable;
    io.vavr.collection.BitSet<Integer> vavrImmutable;

    @Setup
    @SuppressWarnings("RedundantCast")
    public void setup() {
      final Integer[] values = getRandomValues(CONTAINER_SIZE, 0, true);
      ELEMENTS = new int[CONTAINER_SIZE];
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        ELEMENTS[i] = values[i];
      }

      DISTINCT = TreeSet.ofAll(ELEMENTS);
      EXPECTED_AGGREGATE = DISTINCT.reduce(JmhRunner::aggregate);

      scalaImmutable = create(v -> (scala.collection.immutable.BitSet) scala.collection.immutable.BitSet$.MODULE$.apply(asScalaBuffer(v)), DISTINCT.toJavaList(), v -> areEqual(asJavaCollection(v), DISTINCT));
      vavrImmutable = create(io.vavr.collection.BitSet::ofAll, ELEMENTS, ELEMENTS.length, v -> areEqual(v, DISTINCT));
    }
  }

  public static class AddAll extends Base {
    @Benchmark
    public Object scala_immutable() {
      scala.collection.immutable.BitSet values = new scala.collection.immutable.BitSet.BitSet1(0L);
      for (int element : ELEMENTS) {
        values = values.$plus(element);
      }
      assert values.equals(scalaImmutable);
      return values;
    }

    @Benchmark
    public Object vavr_immutable() {
      io.vavr.collection.Set<Integer> values = io.vavr.collection.BitSet.empty();
      for (Integer element : ELEMENTS) {
        values = values.add(element);
      }
      assert values.equals(vavrImmutable);
      return values;
    }
  }

  @SuppressWarnings("ForLoopReplaceableByForEach")
  public static class Iterate extends Base {
    @Benchmark
    public int scala_immutable() {
      int aggregate = 0;
      for (final scala.collection.Iterator<Object> iterator = scalaImmutable.iterator(); iterator.hasNext(); ) {
        aggregate ^= (Integer) iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int vavr_immutable() {
      int aggregate = 0;
      for (final io.vavr.collection.Iterator<Integer> iterator = vavrImmutable.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }
  }
}
