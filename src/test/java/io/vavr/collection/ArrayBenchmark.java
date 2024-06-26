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

import com.carrotsearch.hppc.cursors.IntCursor;
import io.vavr.JmhRunner;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Objects;

import static io.vavr.JmhRunner.*;
import static io.vavr.collection.Collections.areEqual;
import static java.util.Arrays.asList;

public class ArrayBenchmark {
  static final Array<Class<?>> CLASSES = Array.of(
      ArrayCreate.class,
      ArrayHead.class,
      ArrayTail.class,
      ArrayGet.class,
      ArrayUpdate.class,
      ArrayPrepend.class,
      ArrayAppend.class,
      ArrayIterate.class,
      ArrayFill.class
  );

  public static void main(String... args) {
    JmhRunner.runNormalNoAsserts(CLASSES);
  }

  public static class Base extends CollectionBenchmarkBase {

    int EXPECTED_AGGREGATE;
    Integer[] ELEMENTS;

    java.util.ArrayList<Integer> javaMutable;
    fj.data.Array<Integer> fjavaMutable;
    io.vavr.collection.Array<Integer> vavrImmutable;
    org.pcollections.PVector<Integer> pcollVector;
    org.eclipse.collections.api.list.MutableList<Integer> eclipseMutable;
    org.eclipse.collections.impl.list.mutable.FastList<Integer> eclipseFastList;
    org.jctools.queues.MpscArrayQueue<Integer> jctoolsMpscArrayQueue;
    org.apache.commons.collections4.list.SetUniqueList<Integer> apacheCommonsSetUniqueList;
    org.agrona.concurrent.ManyToOneConcurrentArrayQueue<Integer> agronaManyToOneConcurrentArrayQueue;
    com.carrotsearch.hppc.IntArrayList hppcIntArrayList;
    it.unimi.dsi.fastutil.ints.IntArrayList fastutilIntArrayList;

    public static <T> org.jctools.queues.MpscArrayQueue<T> createAndPopulateMpscArrayQueue(int capacity, ArrayList<T> elements) {
      org.jctools.queues.MpscArrayQueue<T> queue = new org.jctools.queues.MpscArrayQueue<>(capacity);
      elements.forEach(queue::offer);
      return queue;
    }

    public static <T> org.agrona.concurrent.ManyToOneConcurrentArrayQueue<T> createAndPopulateManyToOneConcurrentArrayQueue(int capacity, ArrayList<T> elements) {
      org.agrona.concurrent.ManyToOneConcurrentArrayQueue<T> queue = new org.agrona.concurrent.ManyToOneConcurrentArrayQueue<>(capacity);
      elements.forEach(queue::offer);
      return queue;
    }

    public static com.carrotsearch.hppc.IntArrayList createAndPopulateIntArrayList(int capacity, ArrayList<Integer> elements) {
      com.carrotsearch.hppc.IntArrayList list = new com.carrotsearch.hppc.IntArrayList(capacity);
      elements.forEach(list::add);
      return list;
    }

    @Setup
    public void setup() {
      ELEMENTS = fillArrayWithSize(CONTAINER_SIZE);
      EXPECTED_AGGREGATE = Iterator.of(ELEMENTS).reduce(JmhRunner::aggregate);

      javaMutable = create(java.util.ArrayList::new, asList(ELEMENTS), v -> areEqual(v, asList(ELEMENTS)));
      fjavaMutable = create(fj.data.Array::array, ELEMENTS, ELEMENTS.length, v -> areEqual(v, asList(ELEMENTS)));
      vavrImmutable = create(io.vavr.collection.Array::ofAll, new ArrayList<>(javaMutable), v -> areEqual(v, javaMutable));
      pcollVector = create(org.pcollections.TreePVector::from, new ArrayList<>(javaMutable), v -> areEqual(v, javaMutable));
      eclipseMutable = create(org.eclipse.collections.api.factory.Lists.mutable::withAll, new ArrayList<>(javaMutable), v -> areEqual(v, javaMutable));
      eclipseFastList = create((ArrayList<Integer> s) -> {
        return org.eclipse.collections.impl.list.mutable.FastList.newList(s.stream().collect(java.util.stream.Collectors.toList()));
      }, new ArrayList<>(javaMutable), v -> areEqual(v, javaMutable));
      apacheCommonsSetUniqueList = create(org.apache.commons.collections4.list.SetUniqueList::setUniqueList, new ArrayList<>(javaMutable), v -> areEqual(v, javaMutable));
      fastutilIntArrayList = create(it.unimi.dsi.fastutil.ints.IntArrayList::new, new ArrayList<>(javaMutable), v -> areEqual(v, javaMutable));

      jctoolsMpscArrayQueue = createAndPopulateMpscArrayQueue(CONTAINER_SIZE, new ArrayList<>(javaMutable));
      agronaManyToOneConcurrentArrayQueue = createAndPopulateManyToOneConcurrentArrayQueue(CONTAINER_SIZE, new ArrayList<>(javaMutable));
      hppcIntArrayList = createAndPopulateIntArrayList(CONTAINER_SIZE, new ArrayList<>(javaMutable));
    }
  }

  public static class ArrayCreate extends Base {

    @Benchmark
    public Object java_mutable() {
      final ArrayList<Integer> values = new ArrayList<>(javaMutable);
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object fjava_immutable() {
      final fj.data.Array<Integer> values = fj.data.Array.iterableArray(new ArrayList<>(javaMutable));
      assert areEqual(values, fjavaMutable);
      return values;
    }

    @Benchmark
    public Object vavr_immutable() {
      final io.vavr.collection.Array<Integer> values = io.vavr.collection.Array.ofAll(new ArrayList<>(javaMutable));
      assert areEqual(values, vavrImmutable);
      return values.head();
    }

    @Benchmark
    public Object pcollections_vector() {
      final org.pcollections.PVector<Integer> values = org.pcollections.TreePVector.from(new ArrayList<>(javaMutable));
      assert areEqual(values, pcollVector);
      return values;
    }

    @Benchmark
    public Object ecollections_fastlist() {
      final org.eclipse.collections.impl.list.mutable.FastList<Integer> values = org.eclipse.collections.impl.list.mutable.FastList.newList(new ArrayList<>(javaMutable));
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object ecollections_mutable() {
      final org.eclipse.collections.api.list.MutableList<Integer> values = org.eclipse.collections.api.factory.Lists.mutable.withAll(new ArrayList<>(javaMutable));
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object jctools_mpscarrayqueue() {
      final org.jctools.queues.MpscArrayQueue<Integer> values = new org.jctools.queues.MpscArrayQueue<>(CONTAINER_SIZE);
      javaMutable.forEach(values::offer);
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object apache_commons_setuniquelist() {
      final org.apache.commons.collections4.list.SetUniqueList<Integer> values = org.apache.commons.collections4.list.SetUniqueList.setUniqueList(new ArrayList<>(javaMutable));
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object agrona_manytooneconcurrentarrayqueue() {
      final org.agrona.concurrent.ManyToOneConcurrentArrayQueue<Integer> values = new org.agrona.concurrent.ManyToOneConcurrentArrayQueue<>(CONTAINER_SIZE);
      javaMutable.forEach(values::offer);
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object hppc_intarraylist() {
      final com.carrotsearch.hppc.IntArrayList values = new com.carrotsearch.hppc.IntArrayList();
      javaMutable.forEach(values::add);
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object fastutil_intarraylist() {
      final it.unimi.dsi.fastutil.ints.IntArrayList values = new it.unimi.dsi.fastutil.ints.IntArrayList();
      javaMutable.forEach(values::add);
      assert areEqual(values, javaMutable);
      return values;
    }
  }

  public static class ArrayHead extends Base {


    @Benchmark
    public Object java_mutable() {
      final Object head = javaMutable.get(0);
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object fjava_mutable() {
      final Object head = fjavaMutable.get(0);
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object vavr_immutable() {
      final Object head = vavrImmutable.get(0);
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object pcollections_vector() {
      final Object head = pcollVector.get(0);
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object ecollections_fastlist() {
      final Object head = eclipseFastList.getFirst();
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object ecollections_mutable() {
      final Object head = eclipseMutable.getFirst();
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object jctools_mpscarrayqueue() {
      final Object head = jctoolsMpscArrayQueue.poll();
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object apache_commons_setuniquelist() {
      final Object head = apacheCommonsSetUniqueList.get(0);
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object agrona_manytooneconcurrentarrayqueue() {
      final Object head = agronaManyToOneConcurrentArrayQueue.poll();
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object hppc_intarraylist() {
      final Object head = hppcIntArrayList.get(0);
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object fastutil_intarraylist() {
      final Object head = fastutilIntArrayList.get(0);
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }
  }

  @SuppressWarnings("Convert2MethodRef")
  public static class ArrayTail extends Base {
    @State(Scope.Thread)
    public static class Initialized {
      final java.util.ArrayList<Integer> javaMutable = new java.util.ArrayList<>();

      @Setup(Level.Invocation)
      public void initializeMutable(Base state) {
        java.util.Collections.addAll(javaMutable, state.ELEMENTS);
        assert areEqual(javaMutable, asList(state.ELEMENTS));
      }

      @TearDown(Level.Invocation)
      public void tearDown() {
        javaMutable.clear();
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
    public Object vavr_immutable() {
      io.vavr.collection.Array<Integer> values = vavrImmutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.tail();
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object pcollections_vector() {
      org.pcollections.PVector<Integer> values = pcollVector;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.minus(1);
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object ecollections_fastlist() {
      org.eclipse.collections.impl.list.mutable.FastList<Integer> values = eclipseFastList;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.remove(0);
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

    @Benchmark
    public Object jctools_mpscarrayqueue() {
      org.jctools.queues.MpscArrayQueue<Integer> values = jctoolsMpscArrayQueue;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.poll();
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object apache_commons_setuniquelist() {
      org.apache.commons.collections4.list.SetUniqueList<Integer> values = org.apache.commons.collections4.list.SetUniqueList.setUniqueList(apacheCommonsSetUniqueList);
      for (int i = CONTAINER_SIZE - 1; i >= 0; i--) {
        values.remove(i);
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object agrona_manytooneconcurrentarrayqueue() {
      org.agrona.concurrent.ManyToOneConcurrentArrayQueue<Integer> values = agronaManyToOneConcurrentArrayQueue;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.poll();
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object hppc_intarraylist() {
      com.carrotsearch.hppc.IntArrayList values = hppcIntArrayList;
      for (int i = CONTAINER_SIZE - 1; i >= 0; i--) {
        values.remove(i);
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object fastutil_intarraylist() {
      it.unimi.dsi.fastutil.ints.IntArrayList values = fastutilIntArrayList;
      for (int i = CONTAINER_SIZE - 1; i >= 0; i--) {
        values.remove(i);
      }
      assert values.isEmpty();
      return values;
    }
  }

  public static class ArrayGet extends Base {
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
    public int fjava_mutable() {
      int aggregate = 0;
      for (int i = 0; i < ELEMENTS.length; i++) {
        aggregate ^= fjavaMutable.get(i);
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
    public Object pcollections_vector() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        aggregate ^= pcollVector.get(i);
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int ecollections_fastlist() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        aggregate ^= eclipseFastList.get(i);
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

    @Benchmark
    public int jctools_mpscarrayqueue() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        aggregate ^= jctoolsMpscArrayQueue.poll();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int apache_commons_setuniquelist() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        aggregate ^= apacheCommonsSetUniqueList.get(i);
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int agrona_manytooneconcurrentarrayqueue() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        Integer polledValue = agronaManyToOneConcurrentArrayQueue.poll();
        if (polledValue != null) {
          aggregate ^= polledValue;
        }
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int hppc_intarraylist() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        aggregate ^= hppcIntArrayList.get(i);
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int fastutil_intarraylist() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        aggregate ^= fastutilIntArrayList.get(i);
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }
  }

  public static class ArrayUpdate extends Base {
    @Benchmark
    public Object java_mutable() {
      final java.util.ArrayList<Integer> values = javaMutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.set(i, 0);
      }
      assert Iterator.ofAll(values).forAll(e -> e == 0);
      return javaMutable;
    }

    @Benchmark
    public Object fjava_mutable() {
      final fj.data.Array<Integer> values = fjavaMutable;
      for (int i = 0; i < ELEMENTS.length; i++) {
        values.set(i, 0);
      }
      assert values.forall(e -> e == 0);
      return fjavaMutable;
    }

    @Benchmark
    public Object vavr_immutable() {
      io.vavr.collection.Array<Integer> values = vavrImmutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.update(i, 0);
      }
      assert values.forAll(e -> e == 0);
      return values;
    }

    @Benchmark
    public Object pcollections_vector() {
      org.pcollections.PVector<Integer> values = pcollVector;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.with(i, 0);
      }
      assert Iterator.ofAll(values).forAll(e -> e == 0);
      return values;
    }

    @Benchmark
    public Object ecollections_fastlist() {
      org.eclipse.collections.impl.list.mutable.FastList<Integer> values = eclipseFastList;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.set(i, 0);
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

    @Benchmark
    public Object jctools_mpscarrayqueue() {
      org.jctools.queues.MpscArrayQueue<Integer> values = jctoolsMpscArrayQueue;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.poll();
        values.offer(0);
      }
      assert values.stream().allMatch(e -> e == 0);
      return values;
    }

    @Benchmark
    public Object apache_commons_setuniquelist() {
      org.apache.commons.collections4.list.SetUniqueList<Integer> values = apacheCommonsSetUniqueList;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.set(i, 0);
      }
      assert values.stream().allMatch(e -> e == 0);
      return values;
    }

    @Benchmark
    public Object agrona_manytooneconcurrentarrayqueue() {
      org.agrona.concurrent.ManyToOneConcurrentArrayQueue<Integer> values = agronaManyToOneConcurrentArrayQueue;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.poll();
        values.offer(0);
      }
      assert values.stream().allMatch(e -> e == 0);
      return values;
    }

    @Benchmark
    public Object hppc_intarraylist() {
      com.carrotsearch.hppc.IntArrayList values = hppcIntArrayList;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.set(i, 0);
      }
      assert java.util.stream.IntStream.range(0, values.size()).allMatch(i -> values.get(i) == 0);
      return values;
    }

    @Benchmark
    public Object fastutil_intarraylist() {
      it.unimi.dsi.fastutil.ints.IntArrayList values = fastutilIntArrayList;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.set(i, 0);
      }
      assert values.stream().allMatch(e -> e == 0);
      return values;
    }
  }

  public static class ArrayPrepend extends Base {
    @Benchmark
    public Object java_mutable() {
      final java.util.ArrayList<Integer> values = new java.util.ArrayList<>(CONTAINER_SIZE);
      for (Integer element : ELEMENTS) {
        values.add(0, element);
      }
      assert areEqual(List.ofAll(values).reverse(), javaMutable);
      return values;
    }

    @Benchmark
    public Object fjava_mutable() {
      fj.data.Array<Integer> values = fj.data.Array.empty();
      for (Integer element : ELEMENTS) {
        values = fj.data.Array.array(element).append(values);
      }
      assert areEqual(values.reverse(), javaMutable);
      return values;
    }

    @Benchmark
    public Object vavr_immutable() {
      io.vavr.collection.Array<Integer> values = io.vavr.collection.Array.empty();
      for (Integer element : ELEMENTS) {
        values = values.prepend(element);
      }
      assert areEqual(values.reverse(), javaMutable);
      return values;
    }

    @Benchmark
    public Object pcollections_vector() {
      org.pcollections.PVector<Integer> values = org.pcollections.TreePVector.empty();
      for (Integer element : ELEMENTS) {
        values = values.plus(0, element);
      }
      assert areEqual(List.ofAll(values).reverse(), javaMutable);
      return values;
    }

    @Benchmark
    public Object ecollections_fastlist() {
      org.eclipse.collections.impl.list.mutable.FastList<Integer> values = org.eclipse.collections.impl.list.mutable.FastList.newList();
      for (Integer element : ELEMENTS) {
        values.add(0, element);
      }
      assert areEqual(values.reverseThis(), javaMutable);
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

    @Benchmark
    public Object jctools_mpscarrayqueue() {
      org.jctools.queues.MpscArrayQueue<Integer> values = new org.jctools.queues.MpscArrayQueue<>(CONTAINER_SIZE);
      for (Integer element : ELEMENTS) {
        values.offer(element);
      }
      assert areEqual(List.ofAll(values).reverse(), javaMutable);
      return values;
    }

    @Benchmark
    public Object apache_commons_setuniquelist() {
      org.apache.commons.collections4.list.SetUniqueList<Integer> values = org.apache.commons.collections4.list.SetUniqueList.setUniqueList(new ArrayList<>());
      for (Integer element : ELEMENTS) {
        values.add(0, element);
      }
      assert areEqual(List.ofAll(values).reverse(), javaMutable);
      return values;
    }

    @Benchmark
    public Object agrona_manytooneconcurrentarrayqueue() {
      org.agrona.concurrent.ManyToOneConcurrentArrayQueue<Integer> values = new org.agrona.concurrent.ManyToOneConcurrentArrayQueue<>(CONTAINER_SIZE);
      for (Integer element : ELEMENTS) {
        values.offer(element);
      }
      assert areEqual(List.ofAll(values).reverse(), javaMutable);
      return values;
    }

    @Benchmark
    public Object hppc_intarraylist() {
      com.carrotsearch.hppc.IntArrayList values = new com.carrotsearch.hppc.IntArrayList();
      for (Integer element : ELEMENTS) {
        values.add(0, element);
      }
      assert areEqual(List.ofAll(values).reverse(), javaMutable);
      return values;
    }

    @Benchmark
    public Object fastutil_intarraylist() {
      it.unimi.dsi.fastutil.ints.IntArrayList values = new it.unimi.dsi.fastutil.ints.IntArrayList();
      for (Integer element : ELEMENTS) {
        values.add(0, element);
      }
      assert areEqual(List.ofAll(values).reverse(), javaMutable);
      return values;
    }
  }

  public static class ArrayAppend extends Base {
    @SuppressWarnings("ManualArrayToCollectionCopy")
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
    public Object fjava_mutable() {
      fj.data.Array<Integer> values = fj.data.Array.empty();
      for (Integer element : ELEMENTS) {
        values = values.append(fj.data.Array.array(element));
      }
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object vavr_immutable() {
      io.vavr.collection.Array<Integer> values = io.vavr.collection.Array.empty();
      for (Integer element : ELEMENTS) {
        values = values.append(element);
      }
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object pcollections_vector() {
      org.pcollections.PVector<Integer> values = org.pcollections.TreePVector.empty();
      for (Integer element : ELEMENTS) {
        values = values.plus(element);
      }
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object ecollections_fastlist() {
      org.eclipse.collections.impl.list.mutable.FastList<Integer> values = org.eclipse.collections.impl.list.mutable.FastList.newList();
      for (Integer element : ELEMENTS) {
        values.add(element);
      }
      assert areEqual(values, javaMutable);
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

    @Benchmark
    public Object jctools_mpscarrayqueue() {
      org.jctools.queues.MpscArrayQueue<Integer> values = new org.jctools.queues.MpscArrayQueue<>(CONTAINER_SIZE);
      for (Integer element : ELEMENTS) {
        values.offer(element);
      }
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object apache_commons_setuniquelist() {
      org.apache.commons.collections4.list.SetUniqueList<Integer> values = org.apache.commons.collections4.list.SetUniqueList.setUniqueList(new ArrayList<>());
      for (Integer element : ELEMENTS) {
        values.add(element);
      }
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object agrona_manytooneconcurrentarrayqueue() {
      org.agrona.concurrent.ManyToOneConcurrentArrayQueue<Integer> values = new org.agrona.concurrent.ManyToOneConcurrentArrayQueue<>(CONTAINER_SIZE);
      for (Integer element : ELEMENTS) {
        values.offer(element);
      }
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object hppc_intarraylist() {
      com.carrotsearch.hppc.IntArrayList values = new com.carrotsearch.hppc.IntArrayList();
      for (Integer element : ELEMENTS) {
        values.add(element);
      }
      assert areEqual(values, javaMutable);
      return values;
    }

    @Benchmark
    public Object fastutil_intarraylist() {
      it.unimi.dsi.fastutil.ints.IntArrayList values = new it.unimi.dsi.fastutil.ints.IntArrayList();
      for (Integer element : ELEMENTS) {
        values.add(element);
      }
      assert areEqual(values, javaMutable);
      return values;
    }
  }

  @SuppressWarnings("ForLoopReplaceableByForEach")
  public static class ArrayIterate extends Base {
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
    public int fjava_mutable() {
      int aggregate = 0;
      for (final java.util.Iterator<Integer> iterator = fjavaMutable.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int vavr_immutable() {
      int aggregate = 0;
      for (final Iterator<Integer> iterator = vavrImmutable.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int pcollections_vector() {
      int aggregate = 0;
      for (final java.util.Iterator<Integer> iterator = pcollVector.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int ecollections_fastlist() {
      int aggregate = 0;
      for (java.util.Iterator<Integer> iterator = eclipseFastList.iterator(); iterator.hasNext(); ) {
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

    @Benchmark
    public int jctools_mpscarrayqueue() {
      int aggregate = 0;
      for (java.util.Iterator<Integer> iterator = jctoolsMpscArrayQueue.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int apache_commons_setuniquelist() {
      int aggregate = 0;
      for (java.util.Iterator<Integer> iterator = apacheCommonsSetUniqueList.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int agrona_manytooneconcurrentarrayqueue() {
      int aggregate = 0;
      while (true) {
        Integer value = agronaManyToOneConcurrentArrayQueue.poll();
        if (value == null) {
          break;
        }
        aggregate ^= value;
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int hppc_intarraylist() {
      int aggregate = 0;
      for (java.util.Iterator<IntCursor> iterator = hppcIntArrayList.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next().value;
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int fastutil_intarraylist() {
      int aggregate = 0;
      for (java.util.Iterator<Integer> iterator = fastutilIntArrayList.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }
  }

  public static class ArrayFill extends Base {
    @Benchmark
    public Object vavr_immutable_constant_supplier() {
      final io.vavr.collection.Array<Integer> values = io.vavr.collection.Array.fill(CONTAINER_SIZE, () -> ELEMENTS[0]);
      final Integer head = values.head();
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object vavr_immutable_constant_object() {
      final io.vavr.collection.Array<Integer> values = io.vavr.collection.Array.fill(CONTAINER_SIZE, ELEMENTS[0]);
      final Integer head = values.head();
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object ecollections_fastlist_constant_supplier() {
      final org.eclipse.collections.impl.list.mutable.FastList<Integer> values = org.eclipse.collections.impl.list.mutable.FastList.newListWith(java.util.Collections.nCopies(CONTAINER_SIZE, ELEMENTS[0]).toArray(new Integer[0]));
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

    @Benchmark
    public Object hppc_intarraylist_constant_supplier() {
      final com.carrotsearch.hppc.IntArrayList values = new com.carrotsearch.hppc.IntArrayList(CONTAINER_SIZE);
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.add(ELEMENTS[0]);
      }
      final Integer head = values.get(0);
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object fastutil_intarraylist_constant_object() {
      final it.unimi.dsi.fastutil.ints.IntArrayList values = new it.unimi.dsi.fastutil.ints.IntArrayList(CONTAINER_SIZE);
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values.add(ELEMENTS[0]);
      }
      final Integer head = values.get(0);
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }
  }
}
