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

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.vavr.JmhRunner.create;
import static io.vavr.JmhRunner.getRandomValues;
import static io.vavr.collection.Collections.areEqual;
import static java.util.Arrays.asList;
import static scala.collection.JavaConverters.asJavaCollection;
import static scala.collection.JavaConverters.asScalaBuffer;

public class ListBenchmark {
  static final Array<Class<?>> CLASSES = Array.of(
      Create.class,
      Head.class,
      Tail.class,
      Get.class,
      Update.class,
      Prepend.class,
      Append.class,
      GroupBy.class,
      Iterate.class
      , Fill.class
  );

  public static void main(String... args) {
    JmhRunner.runNormalNoAsserts(CLASSES);
  }

  public static class Base extends CollectionBenchmarkBase {

    int EXPECTED_AGGREGATE;
    Integer[] ELEMENTS;

    /* Only use these for non-mutating operations */
    java.util.ArrayList<Integer> javaMutable;
    java.util.LinkedList<Integer> javaMutableLinked;
    scala.collection.mutable.MutableList<Integer> scalaMutable;

    fj.data.List<Integer> fjavaImmutable;
    org.pcollections.PStack<Integer> pcollectionsImmutable;
    scala.collection.immutable.List<Integer> scalaImmutable;
    clojure.lang.IPersistentList clojureImmutable;
    io.vavr.collection.List<Integer> vavrImmutable;
    org.eclipse.collections.api.list.MutableList<Integer> eclipseMutable;
    org.eclipse.collections.api.list.ImmutableList<Integer> eclipseImmutable;

    @Setup
    @SuppressWarnings("unchecked")
    public void setup() {
      ELEMENTS = getRandomValues(CONTAINER_SIZE, 0);
      EXPECTED_AGGREGATE = Iterator.of(ELEMENTS).reduce(JmhRunner::aggregate);

      javaMutable = create(java.util.ArrayList::new, asList(ELEMENTS), v -> areEqual(v, asList(ELEMENTS)));
      javaMutableLinked = create(java.util.LinkedList::new, asList(ELEMENTS), v -> areEqual(v, asList(ELEMENTS)));
      scalaMutable = create(v -> (scala.collection.mutable.MutableList<Integer>) scala.collection.mutable.MutableList$.MODULE$.apply(asScalaBuffer(v)), asList(ELEMENTS), v -> areEqual(asJavaCollection(v), javaMutable));

      scalaImmutable = JmhRunner.<java.util.List<Integer>, scala.collection.immutable.List<Integer>>create(
          v -> scala.collection.immutable.List$.MODULE$.apply(asScalaBuffer(v)),
          javaMutable,
          v -> areEqual(asJavaCollection(v), javaMutable)
      );
      clojureImmutable = create(clojure.lang.PersistentList::create, javaMutable, v -> areEqual((Iterable<?>) v, javaMutable));
      fjavaImmutable = create(v -> fj.data.List.fromIterator(v.iterator()), javaMutable, v -> areEqual(v, javaMutable));
      pcollectionsImmutable = create(org.pcollections.ConsPStack::from, javaMutable, v -> areEqual(v, javaMutable));
      vavrImmutable = create(io.vavr.collection.List::ofAll, javaMutable, v -> areEqual(v, javaMutable));
      eclipseMutable = create(org.eclipse.collections.api.factory.Lists.mutable::withAll, javaMutable, v -> areEqual(v, javaMutable));
      eclipseImmutable = create(org.eclipse.collections.api.factory.Lists.immutable::withAll, javaMutable, v -> areEqual(v, javaMutable));
    }
  }

  public static class Create extends Base {
    @Benchmark
    public Object java_mutable() {
      final ArrayList<Integer> values = new ArrayList<>(javaMutable);
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object scala_immutable() {
      final scala.collection.immutable.List<?> values = scala.collection.immutable.List$.MODULE$.apply(scalaMutable);
      assert Objects.equals(values, scalaImmutable);
      return values;
    }

    @Benchmark
    public Object clojure_immutable() {
      final clojure.lang.IPersistentStack values = clojure.lang.PersistentList.create(javaMutable);
      assert Objects.equals(values, clojureImmutable);
      return values;
    }

    @Benchmark
    public Object fjava_immutable() {
      final fj.data.List<Integer> values = fj.data.List.fromIterator(javaMutable.iterator());
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object pcollections_immutable() {
      final org.pcollections.PStack<Integer> values = org.pcollections.ConsPStack.from(javaMutable);
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object vavr_immutable() {
      final io.vavr.collection.List<Integer> values = io.vavr.collection.List.ofAll(javaMutable);
      assert areEqual(values, javaMutable);
      return values.head();
    }

    @Benchmark
    public Object ecollections_immutable() {
      final org.eclipse.collections.api.list.ImmutableList<Integer> values = org.eclipse.collections.api.factory.Lists.immutable.withAll(javaMutable);
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object ecollections_mutable() {
      final org.eclipse.collections.api.list.MutableList<Integer> values = org.eclipse.collections.api.factory.Lists.mutable.withAll(javaMutable);
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object guava_immutable() {
      final com.google.common.collect.ImmutableList<Integer> values = com.google.common.collect.ImmutableList.copyOf(javaMutable);
      assert areEqual(values, javaMutable);
      return values;
    }
  }

  public static class Head extends Base {
    @Benchmark
    public Object java_mutable() {
      final Object head = javaMutable.get(0);
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object scala_immutable() {
      final Object head = scalaImmutable.head();
      assert Objects.equals(head, javaMutable.get(0));
      return head;
    }

    @Benchmark
    public Object clojure_immutable() {
      final Object head = clojureImmutable.peek();
      assert Objects.equals(head, javaMutable.get(0));
      return head;
    }

    @Benchmark
    public Object fjava_immutable() {
      final Object head = fjavaImmutable.head();
      assert Objects.equals(head, javaMutable.get(0));
      return head;
    }

    @Benchmark
    public Object pcollections_immutable() {
      final Object head = pcollectionsImmutable.get(0);
      assert Objects.equals(head, javaMutable.get(0));
      return head;
    }

    @Benchmark
    public Object vavr_immutable() {
      final Object head = vavrImmutable.head();
      assert Objects.equals(head, javaMutable.get(0));
      return head;
    }

    @Benchmark
    public Object ecollections_immutable() {
      final Object head = eclipseImmutable.get(0);
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object ecollections_mutable() {
      final Object head = eclipseMutable.get(0);
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }
  }

  @SuppressWarnings("Convert2MethodRef")
  public static class Tail extends Base {
    @State(Scope.Thread)
    public static class Initialized {
      final java.util.ArrayList<Integer> javaMutable = new java.util.ArrayList<>();
      final java.util.LinkedList<Integer> javaMutableLinked = new java.util.LinkedList<>();

      @Setup(Level.Invocation)
      public void initializeMutable(Base state) {
        java.util.Collections.addAll(javaMutable, state.ELEMENTS);
        javaMutableLinked.addAll(javaMutable);
        assert areEqual(javaMutable, asList(state.ELEMENTS))
            && areEqual(javaMutableLinked, javaMutable);
      }

      @TearDown(Level.Invocation)
      public void tearDown() {
        javaMutable.clear();
        javaMutableLinked.clear();
      }
    }

    @Benchmark
    public Object java_mutable(Initialized state) {
      final java.util.ArrayList<Integer> values = state.javaMutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.remove(0);
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object java_linked_mutable(Initialized state) {
      final java.util.LinkedList<Integer> values = state.javaMutableLinked;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.remove(0);
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    @SuppressWarnings({"unchecked", "RedundantCast"})
    public Object scala_immutable() {
      scala.collection.immutable.List<Integer> values = scalaImmutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = (scala.collection.immutable.List<Integer>) values.tail();
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object clojure_immutable() {
      clojure.lang.IPersistentStack values = clojureImmutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.pop();
      }
      assert Objects.equals(values, clojure.lang.PersistentList.EMPTY);
      return values;
    }

    @Benchmark
    public Object fjava_immutable() {
      fj.data.List<Integer> values = fjavaImmutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.tail();
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object pcollections_immutable() {
      org.pcollections.PStack<Integer> values = pcollectionsImmutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.minus(0);
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object vavr_immutable() {
      io.vavr.collection.List<Integer> values = vavrImmutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.tail();
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object ecollections_immutable() {
      org.eclipse.collections.api.list.ImmutableList<Integer> values = eclipseImmutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.subList(1, values.size());
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object ecollections_mutable() {
      org.eclipse.collections.api.list.MutableList<Integer> values = eclipseMutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.subList(1, values.size());
      }
      assert values.isEmpty();
      return values;
    }
  }


  public static class Get extends Base {
    @Benchmark
    public int java_mutable() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        aggregate ^= javaMutable.get(i);
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int java_linked_mutable() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        aggregate ^= javaMutableLinked.get(i);
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int scala_immutable() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        aggregate ^= scalaImmutable.apply(i);
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int fjava_immutable() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        aggregate ^= fjavaImmutable.index(i);
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int pcollections_immutable() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        aggregate ^= pcollectionsImmutable.get(i);
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int vavr_immutable() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        aggregate ^= vavrImmutable.get(i);
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int ecollections_immutable() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        aggregate ^= eclipseImmutable.get(i);
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int ecollections_mutable() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        aggregate ^= eclipseMutable.get(i);
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }
  }

  public static class Update extends Base {
    @State(Scope.Thread)
    public static class Initialized {
      final java.util.ArrayList<Integer> javaMutable = new java.util.ArrayList<>();
      final java.util.LinkedList<Integer> javaMutableLinked = new java.util.LinkedList<>();
      final scala.collection.mutable.MutableList<Integer> scalaMutable = new scala.collection.mutable.MutableList<>();

      @Setup(Level.Invocation)
      public void initializeMutable(Base state) {
        java.util.Collections.addAll(javaMutable, state.ELEMENTS);
        java.util.Collections.addAll(javaMutableLinked, state.ELEMENTS);
        for (int i = state.CONTAINER_SIZE - 1; i >= 0; i--) {
          scalaMutable.prependElem(state.ELEMENTS[i]);
        }

        assert areEqual(javaMutable, asList(state.ELEMENTS))
            && areEqual(javaMutableLinked, javaMutable)
            && areEqual(asJavaCollection(scalaMutable), javaMutable);
      }

      @TearDown(Level.Invocation)
      public void tearDown() {
        javaMutable.clear();
        javaMutableLinked.clear();
        scalaMutable.clear();
      }
    }

    @Benchmark
    public Object java_mutable(Initialized state) {
      final java.util.ArrayList<Integer> values = state.javaMutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.set(i, 0);
      }
      assert Array.ofAll(values).forAll(e -> e == 0);
      return values;
    }

    @Benchmark
    public Object java_linked_mutable(Initialized state) {
      final java.util.LinkedList<Integer> values = state.javaMutableLinked;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.set(i, 0);
      }
      assert Array.ofAll(values).forAll(e -> e == 0);
      return values;
    }

    @Benchmark
    public Object scala_mutable(Initialized state) {
      final scala.collection.mutable.MutableList<Integer> values = state.scalaMutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.update(i, 0);
      }
      assert Array.ofAll(asJavaCollection(values)).forAll(e -> e == 0);
      return values;
    }

    @Benchmark
    public Object pcollections_immutable() {
      org.pcollections.PStack<Integer> values = pcollectionsImmutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.with(i, 0);
      }
      assert Array.ofAll(values).forAll(e -> e == 0);
      return values;
    }

    @Benchmark
    public Object vavr_immutable() {
      io.vavr.collection.List<Integer> values = vavrImmutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.update(i, 0);
      }
      assert values.forAll(e -> e == 0);
      return values;
    }

    @Benchmark
    public Object ecollections_immutable() {
      org.eclipse.collections.api.list.ImmutableList<Integer> values = eclipseImmutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.subList(0, i)
            .newWith(0)
            .newWithAll(values.subList(i + 1, values.size()));
      }
      assert values.allSatisfy(e -> e == 0);
      return values;
    }

    @Benchmark
    public Object ecollections_mutable() {
      org.eclipse.collections.api.list.MutableList<Integer> values = eclipseMutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.set(i, 0);
      }
      assert values.allSatisfy(e -> e == 0);
      return values;
    }
  }

  @SuppressWarnings("ManualArrayToCollectionCopy")
  public static class Prepend extends Base {
    @Benchmark
    public Object java_mutable() {
      final java.util.ArrayList<Integer> values = new java.util.ArrayList<>(CONTAINER_SIZE);
      for (Integer element : ELEMENTS) {
        values.add(0, element);
      }
      assert areEqual(Array.ofAll(values).reverse(), javaMutable);
      return values;
    }

    @Benchmark
    public Object java_linked_mutable() {
      final java.util.LinkedList<Integer> values = new java.util.LinkedList<>();
      for (Integer element : ELEMENTS) {
        values.addFirst(element);
      }
      assert areEqual(Array.ofAll(values).reverse(), javaMutable);
      return values;
    }

    @Benchmark
    public Object scala_mutable() {
      final scala.collection.mutable.MutableList<Integer> values = new scala.collection.mutable.MutableList<>();
      for (Integer element : ELEMENTS) {
        values.prependElem(element);
      }
      assert areEqual(Array.ofAll(asJavaCollection(values)).reverse(), javaMutable);
      return values;
    }

    @Benchmark
    public Object scala_immutable() {
      scala.collection.immutable.List<Integer> values = scala.collection.immutable.List$.MODULE$.empty();
      for (Integer element : ELEMENTS) {
        values = values.$colon$colon(element);
      }
      assert areEqual(Array.ofAll(asJavaCollection(values)).reverse(), javaMutable);
      return values;
    }

    @Benchmark
    public Object fjava_immutable() {
      fj.data.List<Integer> values = fj.data.List.list();
      for (Integer element : ELEMENTS) {
        values = values.cons(element);
      }
      assert areEqual(Array.ofAll(values).reverse(), javaMutable);
      return values;
    }

    @Benchmark
    public Object pcollections_immutable() {
      org.pcollections.PStack<Integer> values = org.pcollections.ConsPStack.empty();
      for (Integer element : ELEMENTS) {
        values = values.plus(element);
      }
      assert areEqual(Array.ofAll(values).reverse(), javaMutable);
      return values;
    }

    @Benchmark
    public Object vavr_immutable() {
      io.vavr.collection.List<Integer> values = io.vavr.collection.List.empty();
      for (Integer element : ELEMENTS) {
        values = values.prepend(element);
      }
      assert areEqual(values.reverse(), javaMutable);
      return values;
    }

    @Benchmark
    public Object ecollections_immutable() {
      org.eclipse.collections.api.list.ImmutableList<Integer> values = org.eclipse.collections.api.factory.Lists.immutable.empty();
      for (Integer element : ELEMENTS) {
        values = values.newWith(element);
      }
      assert areEqual(values.toReversed(), javaMutable);
      return values;
    }

    @Benchmark
    public Object ecollections_mutable() {
      org.eclipse.collections.api.list.MutableList<Integer> values = org.eclipse.collections.api.factory.Lists.mutable.empty();
      for (Integer element : ELEMENTS) {
        values.add(0, element);
      }
      assert areEqual(values, javaMutable);
      return values;
    }
  }

  @SuppressWarnings("ManualArrayToCollectionCopy")
  public static class Append extends Base {
    @Benchmark
    public Object java_mutable() {
      final java.util.ArrayList<Integer> values = new java.util.ArrayList<>(CONTAINER_SIZE);
      for (Integer element : ELEMENTS) {
        values.add(element);
      }
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object java_linked_mutable() {
      final java.util.LinkedList<Integer> values = new java.util.LinkedList<>();
      for (Integer element : ELEMENTS) {
        values.addLast(element);
      }
      assert values.size() == CONTAINER_SIZE;
      return values;
    }

    @Benchmark
    public Object scala_mutable() {
      final scala.collection.mutable.MutableList<Integer> values = new scala.collection.mutable.MutableList<>();
      for (Integer element : ELEMENTS) {
        values.appendElem(element);
      }
      assert areEqual(asJavaCollection(values), javaMutable);
      return values;
    }

    @Benchmark
    public Object fjava_immutable() {
      fj.data.List<Integer> values = fj.data.List.list();
      for (Integer element : ELEMENTS) {
        values = values.snoc(element);
      }
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object pcollections_immutable() {
      org.pcollections.PStack<Integer> values = org.pcollections.ConsPStack.empty();
      for (Integer element : ELEMENTS) {
        values = values.plus(values.size(), element);
      }
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object vavr_immutable() {
      io.vavr.collection.List<Integer> values = io.vavr.collection.List.empty();
      for (Integer element : ELEMENTS) {
        values = values.append(element);
      }
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object ecollections_immutable() {
      org.eclipse.collections.api.list.ImmutableList<Integer> values = org.eclipse.collections.api.factory.Lists.immutable.empty();
      for (Integer element : ELEMENTS) {
        values = values.newWith(element);
      }
      assert areEqual(values.toReversed(), javaMutable);
      return values;
    }

    @Benchmark
    public Object ecollections_mutable() {
      org.eclipse.collections.api.list.MutableList<Integer> values = org.eclipse.collections.api.factory.Lists.mutable.empty();
      for (Integer element : ELEMENTS) {
        values.add(0, element);
      }
      assert areEqual(values, javaMutable);
      return values;
    }
  }

  public static class GroupBy extends Base {
    @Benchmark
    public Object java_mutable() {
      return javaMutable.stream().collect(Collectors.groupingBy(Integer::bitCount));
    }

    @Benchmark
    public Object scala_immutable() {
      return scalaImmutable.groupBy(Integer::bitCount);
    }

    @Benchmark
    public Object fjava_immutable() {
      return fjavaImmutable.groupBy(Integer::bitCount);
    }

    @Benchmark
    public Object vavr_immutable() {
      return vavrImmutable.groupBy(Integer::bitCount);
    }

    @Benchmark
    public Object ecollections_immutable() {
      return eclipseImmutable.groupBy(Integer::bitCount);
    }

    @Benchmark
    public Object ecollections_mutable() {
      return eclipseMutable.groupBy(Integer::bitCount);
    }
  }

  @SuppressWarnings("ForLoopReplaceableByForEach")
  public static class Iterate extends Base {
    @Benchmark
    public int java_mutable() {
      int aggregate = 0;
      for (final java.util.Iterator<Integer> iterator = javaMutable.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int java_linked_mutable() {
      int aggregate = 0;
      for (final java.util.Iterator<Integer> iterator = javaMutableLinked.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int scala_mutable() {
      int aggregate = 0;
      for (final scala.collection.Iterator<Integer> iterator = scalaMutable.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

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
    public int fjava_immutable() {
      int aggregate = 0;
      for (final java.util.Iterator<Integer> iterator = fjavaImmutable.iterator(); iterator.hasNext(); ) {
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
    public int vavr_immutable() {
      int aggregate = 0;
      for (final java.util.Iterator<Integer> iterator = vavrImmutable.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int ecollections_immutable() {
      int aggregate = 0;
      for (java.util.Iterator<Integer> iterator = eclipseImmutable.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int ecollections_mutable() {
      int aggregate = 0;
      for (java.util.Iterator<Integer> iterator = eclipseMutable.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }
  }

  public static class Fill extends Base {
    @Benchmark
    public Object scala_immutable() {
      final scala.collection.immutable.List<?> values = scala.collection.immutable.List$.MODULE$.fill(CONTAINER_SIZE, () -> ELEMENTS[0]);
      final Object head = values.head();
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object vavr_immutable_constant_supplier() {
      final io.vavr.collection.List<Integer> values = io.vavr.collection.List.fill(CONTAINER_SIZE, () -> ELEMENTS[0]);
      final Integer head = values.head();
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object vavr_immutable_constant_object() {
      final io.vavr.collection.List<Integer> values = io.vavr.collection.List.fill(CONTAINER_SIZE, ELEMENTS[0]);
      final Integer head = values.head();
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object ecollections_immutable_constant_supplier() {
      final org.eclipse.collections.api.list.ImmutableList<Integer> values = org.eclipse.collections.api.factory.Lists.immutable.withAll(java.util.Collections.nCopies(CONTAINER_SIZE, null));
      final Integer head = values.get(0);
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object ecollections_mutable_constant_object() {
      final org.eclipse.collections.api.list.MutableList<Integer> values = org.eclipse.collections.api.factory.Lists.mutable.withAll(java.util.Collections.nCopies(CONTAINER_SIZE, null));
      final Integer head = values.get(0);
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }
  }
}
