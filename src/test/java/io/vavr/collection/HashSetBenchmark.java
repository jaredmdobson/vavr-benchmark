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
import static scala.collection.JavaConverters.asScalaBuffer;

public class HashSetBenchmark {
  static final Array<Class<?>> CLASSES = Array.of(
      HashSetAdd.class,
      HashSetIterate.class,
      HashSetRemove.class
  );

  public static void main(String... args) {
    JmhRunner.runNormalNoAsserts(CLASSES);
  }

  public static class Base extends CollectionBenchmarkBase {

    int EXPECTED_AGGREGATE;
    Integer[] ELEMENTS;
    Set<Integer> SET;

    scala.collection.immutable.Set<Integer> scalaImmutable;
    org.pcollections.PSet<Integer> pcollectionsImmutable;
    io.usethesource.capsule.Set.Immutable<Integer> capsuleImmutable;
    io.vavr.collection.Set<Integer> vavrImmutable;

    @Setup
    @SuppressWarnings("unchecked")
    public void setup() {
      ELEMENTS = getRandomValues(CONTAINER_SIZE, 0);

      SET = TreeSet.of(ELEMENTS);
      EXPECTED_AGGREGATE = SET.reduce(JmhRunner::aggregate);

      scalaImmutable = create(v -> (scala.collection.immutable.Set<Integer>) scala.collection.immutable.HashSet$.MODULE$.apply(asScalaBuffer(v)), SET.toJavaList(), SET.size(), v -> SET.forAll(v::contains));
      pcollectionsImmutable = create(org.pcollections.HashTreePSet::from, SET.toJavaList(), SET.size(), v -> SET.forAll(v::contains));
      capsuleImmutable = create(io.usethesource.capsule.util.collection.AbstractSpecialisedImmutableSet::setOf, SET.toJavaSet(), SET.size(), v -> SET.forAll(v::contains));
      vavrImmutable = create(io.vavr.collection.HashSet::ofAll, SET, SET.size(), v -> SET.forAll(v::contains));
    }
  }

  public static class HashSetAdd extends Base {
    @Benchmark
    public Object pcollections_immutable() {
      org.pcollections.PSet<Integer> values = org.pcollections.HashTreePSet.empty();
      for (Integer element : ELEMENTS) {
        values = values.plus(element);
      }
      assert SET.forAll(values::contains);
      return values;
    }

    @Benchmark
    public Object scala_immutable() {
      scala.collection.immutable.HashSet<Integer> values = new scala.collection.immutable.HashSet<>();
      for (Integer element : ELEMENTS) {
        values = values.$plus(element);
      }
      assert SET.forAll(values::contains);
      return values;
    }

    @Benchmark
    public Object capsule_immutable() {
      io.usethesource.capsule.Set.Immutable<Integer> values = io.usethesource.capsule.core.PersistentTrieSet.of();
      for (Integer element : ELEMENTS) {
        values = values.__insert(element);
      }
      assert SET.forAll(values::contains);
      return values;
    }

    @Benchmark
    public Object vavr_immutable() {
      io.vavr.collection.Set<Integer> values = io.vavr.collection.HashSet.empty();
      for (Integer element : ELEMENTS) {
        values = values.add(element);
      }
      assert SET.forAll(values::contains);
      return values;
    }
  }

  public static class HashSetRemove extends Base {
    @Benchmark
    public Object pcollections_immutable() {
      org.pcollections.PSet<Integer> values = pcollectionsImmutable;
      for (Integer element : ELEMENTS) {
        values = values.minus(element);
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object capsule_immutable() {
      io.usethesource.capsule.Set.Immutable<Integer> values = capsuleImmutable;
      for (Integer element : ELEMENTS) {
        values = values.__remove(element);
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object vavr_immutable() {
      io.vavr.collection.Set<Integer> values = vavrImmutable;
      for (Integer element : ELEMENTS) {
        values = values.remove(element);
      }
      assert values.isEmpty();
      return values;
    }
  }

  @SuppressWarnings("ForLoopReplaceableByForEach")
  public static class HashSetIterate extends Base {
    @Benchmark
    public int scala_immutable() {
      int aggregate = 0;
      for (final scala.collection.Iterator<Integer> iterator = scalaImmutable.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int pcollections_immutable() {
      int aggregate = 0;
      for (final java.util.Iterator<Integer> iterator = pcollectionsImmutable.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int capsule_immutable() {
      int aggregate = 0;
      for (final java.util.Iterator<Integer> iterator = capsuleImmutable.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
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
