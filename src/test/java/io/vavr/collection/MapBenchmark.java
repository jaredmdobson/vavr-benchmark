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

import io.vavr.Function1;
import io.vavr.JmhRunner;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.Random;

import static io.vavr.JmhRunner.create;
import static io.vavr.JmhRunner.getRandomValues;

public class MapBenchmark {
  static final Array<Class<?>> CLASSES = Array.of(
      MapIterateKeys.class,
      MapVavrKeys.class,
      MapVavrValues.class,
      MapIterateValues.class,
      MapGet.class,
      MapMiss.class,
      MapPutOrdered.class,
      MapPutShuffled.class,
      MapReplaceSingle.class,
      MapReplaceAll.class,
      MapReplaceAllOneByOne.class,
      MapRemove.class
  );

  public static void main(String... args) {
    JmhRunner.runNormalNoAsserts(CLASSES);
  }

  public static class Base extends CollectionBenchmarkBase {

    int EXPECTED_AGGREGATE;
    Integer[] ELEMENTS;
    Integer[] KEYS;
    Integer[] REMOVAL;
    Map<Integer, Integer> sampleTreeMap;

    //        scala.collection.immutable.Map<Integer, Integer> scalaImmutable;
    org.pcollections.PMap<Integer, Integer> pcollectionsImmutable;
    io.usethesource.capsule.Map.Immutable<Integer, Integer> capsuleImmutable;
    Map<Integer, Integer> vavrHash;
    Map<Integer, Integer> vavrTreeMap;
    Map<Integer, Integer> vavrLinkedHash;

    @Setup
    public void setup() {
      ELEMENTS = getRandomValues(CONTAINER_SIZE, 0);
      sampleTreeMap = index(ELEMENTS);
      KEYS = predicableShuffle(sampleTreeMap.keySet().toJavaArray(Integer[]::new));
      REMOVAL = predicableShuffle(KEYS.clone());
      EXPECTED_AGGREGATE = sampleTreeMap.values().reduce(JmhRunner::aggregate);

      pcollectionsImmutable = create(
          org.pcollections.HashTreePMap::from,
          sampleTreeMap.toJavaMap(),
          sampleTreeMap.size(),
          v -> sampleTreeMap.forAll((e) -> v.get(e._1).equals(e._2)));
      capsuleImmutable = create(
          io.usethesource.capsule.util.collection.AbstractSpecialisedImmutableMap::mapOf,
          sampleTreeMap.toJavaMap(),
          sampleTreeMap.size(),
          v -> sampleTreeMap.forAll((e) -> v.get(e._1).equals(e._2)));
      vavrTreeMap = doCreateMap(TreeMap::ofAll, sampleTreeMap);
      vavrHash = doCreateMap(HashMap::ofAll, sampleTreeMap);
      vavrLinkedHash = doCreateMap(LinkedHashMap::ofAll, sampleTreeMap);
    }

    private TreeMap<Integer, Integer> index(Integer[] array) {
      java.util.Map<Integer, Integer> javaMap = new java.util.HashMap<>();
      for (int i = 0; i < array.length; i++) {
        javaMap.put(i, array[i]);
      }
      return TreeMap.ofAll(javaMap);
    }

    /**
     * Shuffle the array. Use a random number generator with a fixed seed.
     */
    private Integer[] predicableShuffle(Integer[] array) {
      java.util.Collections.shuffle(Arrays.asList(array), new Random(42));
      return array;
    }

    private <M extends Map<Integer, Integer>> M doCreateMap(Function1<java.util.Map<Integer, Integer>, M> factory, M prototype) {
      return create(factory, prototype.toJavaMap(), prototype.size(), v -> prototype.forAll(v::contains));
    }
  }

  @SuppressWarnings("Duplicates")
  public static class MapPutShuffled extends Base {
    @Benchmark
    public Object pcollections_immutable() {
      org.pcollections.PMap<Integer, Integer> values = org.pcollections.HashTreePMap.empty();
      Integer[] elements = ELEMENTS;
      for (Integer key : KEYS) {
        values = values.plus(key, elements[key]);
      }
      org.pcollections.PMap<Integer, Integer> result = values;
      assert vavrTreeMap.forAll((e) -> result.get(e._1).equals(e._2));
      return result;
    }

    @Benchmark
    public Object capsule_immutable() {
      io.usethesource.capsule.Map.Immutable<Integer, Integer> values = io.usethesource.capsule.core.PersistentTrieMap.of();
      Integer[] elements = ELEMENTS;
      for (Integer key : KEYS) {
        values = values.__put(key, elements[key]);
      }
      io.usethesource.capsule.Map.Immutable<Integer, Integer> result = values;
      assert vavrTreeMap.forAll((e) -> result.get(e._1).equals(e._2));
      return result;
    }

    @Benchmark
    public Object vavr_tree() {
      Map<Integer, Integer> values = TreeMap.empty();
      Integer[] elements = ELEMENTS;
      for (Integer key : KEYS) {
        values = values.put(key, elements[key]);
      }
      assert vavrTreeMap.forAll(values::contains);
      return values;
    }

    @Benchmark
    public Object vavr_hash() {
      Map<Integer, Integer> values = HashMap.empty();
      Integer[] elements = ELEMENTS;
      for (Integer key : KEYS) {
        values = values.put(key, elements[key]);
      }
      assert vavrTreeMap.forAll(values::contains);
      return values;
    }

    @Benchmark
    public Object vavr_linked_hash() {
      Map<Integer, Integer> values = LinkedHashMap.empty();
      Integer[] elements = ELEMENTS;
      for (Integer key : KEYS) {
        values = values.put(key, elements[key]);
      }
      assert vavrTreeMap.forAll(values::contains);
      return values;
    }
  }

  public static class MapPutOrdered extends Base {
    @Benchmark
    public Object pcollections_immutable() {
      org.pcollections.PMap<Integer, Integer> values = org.pcollections.HashTreePMap.empty();
      Integer[] elements = ELEMENTS;
      for (int i = 0; i < elements.length; i++) {
        values = values.plus(i, elements[i]);
      }
      org.pcollections.PMap<Integer, Integer> result = values;
      assert vavrTreeMap.forAll((e) -> result.get(e._1).equals(e._2));
      return result;
    }

    @Benchmark
    public Object capsule_immutable() {
      io.usethesource.capsule.Map.Immutable<Integer, Integer> values = io.usethesource.capsule.core.PersistentTrieMap.of();
      Integer[] elements = ELEMENTS;
      for (int i = 0; i < elements.length; i++) {
        values = values.__put(i, elements[i]);
      }
      io.usethesource.capsule.Map.Immutable<Integer, Integer> result = values;
      assert vavrTreeMap.forAll((e) -> result.get(e._1).equals(e._2));
      return result;
    }

    @Benchmark
    public Object vavr_tree() {
      Map<Integer, Integer> values = TreeMap.empty();
      Integer[] elements = ELEMENTS;
      for (int i = 0; i < elements.length; i++) {
        values = values.put(i, elements[i]);
      }
      assert vavrTreeMap.forAll(values::contains);
      return values;
    }

    @Benchmark
    public Object vavr_hash() {
      Map<Integer, Integer> values = HashMap.empty();
      Integer[] elements = ELEMENTS;
      for (int i = 0; i < elements.length; i++) {
        values = values.put(i, elements[i]);
      }
      assert vavrTreeMap.forAll(values::contains);
      return values;
    }

    @Benchmark
    public Object vavr_linked_hash() {
      Map<Integer, Integer> values = LinkedHashMap.empty();
      Integer[] elements = ELEMENTS;
      for (int i = 0; i < elements.length; i++) {
        values = values.put(i, elements[i]);
      }
      assert vavrTreeMap.forAll(values::contains);
      return values;
    }
  }

  public static class MapGet extends Base {
    @Benchmark
    public void pcollections_immutable(Blackhole bh) {
      org.pcollections.PMap<Integer, Integer> values = pcollectionsImmutable;
      for (Integer key : KEYS) {
        bh.consume(values.get(key));
      }
    }

    @Benchmark
    public void capsule_immutable(Blackhole bh) {
      io.usethesource.capsule.Map.Immutable<Integer, Integer> values = capsuleImmutable;
      for (Integer key : KEYS) {
        bh.consume(values.get(key));
      }
    }

    @Benchmark
    public void vavr_tree(Blackhole bh) {
      Map<Integer, Integer> values = vavrTreeMap;
      Integer dflt = 1;
      for (Integer key : KEYS) {
        bh.consume(values.getOrElse(key, dflt));
      }
    }

    @Benchmark
    public void vavr_hash(Blackhole bh) {
      Map<Integer, Integer> values = vavrHash;
      Integer dflt = 1;
      for (Integer key : KEYS) {
        bh.consume(values.getOrElse(key, dflt));
      }
    }

    @Benchmark
    public void vavr_linked_hash(Blackhole bh) {
      Map<Integer, Integer> values = vavrLinkedHash;
      Integer dflt = 1;
      for (Integer key : KEYS) {
        bh.consume(values.getOrElse(key, dflt));
      }
    }
  }

  public static class MapMiss extends Base {
    @Benchmark
    public Object pcollections_immutable() {
      org.pcollections.PMap<Integer, Integer> values = pcollectionsImmutable;
      return values.get(-1);
    }

    @Benchmark
    public Object capsule_immutable() {
      io.usethesource.capsule.Map.Immutable<Integer, Integer> values = capsuleImmutable;
      return values.get(-1);
    }

    @Benchmark
    public Object vavr_tree() {
      Map<Integer, Integer> values = vavrTreeMap;
      return values.get(-1);
    }

    @Benchmark
    public Object vavr_hash() {
      Map<Integer, Integer> values = vavrHash;
      return values.get(-1);
    }

    @Benchmark
    public Object vavr_linked_hash() {
      Map<Integer, Integer> values = vavrLinkedHash;
      return values.get(-1);
    }
  }

  public static class MapIterateKeys extends Base {
    @Benchmark
    public void pcollections_immutable(Blackhole bh) {
      org.pcollections.PMap<Integer, Integer> values = pcollectionsImmutable;
      for (Integer integer : values.keySet()) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void capsule_immutable(Blackhole bh) {
      io.usethesource.capsule.Map.Immutable<Integer, Integer> values = capsuleImmutable;
      for (java.util.Iterator<Integer> it = values.keyIterator(); it.hasNext(); ) {
        bh.consume(it.next());
      }
    }

    @Benchmark
    public void vavr_tree(Blackhole bh) {
      Map<Integer, Integer> values = vavrTreeMap;
      for (Integer integer : values.iterator((k, v) -> k)) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_tree_keys(Blackhole bh) {
      Map<Integer, Integer> values = vavrTreeMap;
      for (Integer integer : values.keysIterator()) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_hash(Blackhole bh) {
      Map<Integer, Integer> values = vavrHash;
      for (Integer integer : values.iterator((k, v) -> k)) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_hash_keys(Blackhole bh) {
      Map<Integer, Integer> values = vavrHash;
      for (Integer integer : values.keysIterator()) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_linked_hash(Blackhole bh) {
      Map<Integer, Integer> values = vavrLinkedHash;
      for (Integer integer : values.iterator((k, v) -> k)) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_linked_hash_keys(Blackhole bh) {
      Map<Integer, Integer> values = vavrLinkedHash;
      for (Integer integer : values.keysIterator()) {
        bh.consume(integer);
      }
    }
  }

  public static class MapVavrKeys extends Base {
    @Benchmark
    public void vavr_hash_keySet(Blackhole bh) {
      Map<Integer, Integer> values = vavrHash;
      for (Integer integer : values.keySet()) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_hash_iterator(Blackhole bh) {
      Map<Integer, Integer> values = vavrHash;
      for (Integer integer : values.iterator((k, v) -> k)) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_hash_keys(Blackhole bh) {
      Map<Integer, Integer> values = vavrHash;
      for (Integer integer : values.keysIterator()) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_tree_keySet(Blackhole bh) {
      Map<Integer, Integer> values = vavrTreeMap;
      for (Integer integer : values.keySet()) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_tree_iterator(Blackhole bh) {
      Map<Integer, Integer> values = vavrTreeMap;
      for (Integer integer : values.iterator((k, v) -> k)) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_tree_keys(Blackhole bh) {
      Map<Integer, Integer> values = vavrTreeMap;
      for (Integer integer : values.keysIterator()) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_linked_hash_keySet(Blackhole bh) {
      Map<Integer, Integer> values = vavrLinkedHash;
      for (Integer integer : values.keySet()) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_linked_hash_iterator(Blackhole bh) {
      Map<Integer, Integer> values = vavrLinkedHash;
      for (Integer integer : values.iterator((k, v) -> k)) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_linked_hash_keys(Blackhole bh) {
      Map<Integer, Integer> values = vavrLinkedHash;
      for (Integer integer : values.keysIterator()) {
        bh.consume(integer);
      }
    }

  }

  public static class MapVavrValues extends Base {
    @Benchmark
    public void vavr_hash_keySet(Blackhole bh) {
      Map<Integer, Integer> values = vavrHash;
      for (Integer integer : values.values()) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_hash_iterator(Blackhole bh) {
      Map<Integer, Integer> values = vavrHash;
      for (Integer integer : values.iterator((k, v) -> v)) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_hash_keys(Blackhole bh) {
      Map<Integer, Integer> values = vavrHash;
      for (Integer integer : values.valuesIterator()) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_tree_keySet(Blackhole bh) {
      Map<Integer, Integer> values = vavrTreeMap;
      for (Integer integer : values.values()) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_tree_iterator(Blackhole bh) {
      Map<Integer, Integer> values = vavrTreeMap;
      for (Integer integer : values.iterator((k, v) -> v)) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_tree_keys(Blackhole bh) {
      Map<Integer, Integer> values = vavrTreeMap;
      for (Integer integer : values.valuesIterator()) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_linked_hash_keySet(Blackhole bh) {
      Map<Integer, Integer> values = vavrLinkedHash;
      for (Integer integer : values.values()) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_linked_hash_iterator(Blackhole bh) {
      Map<Integer, Integer> values = vavrLinkedHash;
      for (Integer integer : values.iterator((k, v) -> v)) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_linked_hash_keys(Blackhole bh) {
      Map<Integer, Integer> values = vavrLinkedHash;
      for (Integer integer : values.valuesIterator()) {
        bh.consume(integer);
      }
    }

  }

  public static class MapIterateValues extends Base {
    @Benchmark
    public void pcollections_immutable(Blackhole bh) {
      org.pcollections.PMap<Integer, Integer> values = pcollectionsImmutable;
      for (Integer integer : values.values()) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void capsule_immutable(Blackhole bh) {
      io.usethesource.capsule.Map.Immutable<Integer, Integer> values = capsuleImmutable;
      for (java.util.Iterator<Integer> it = values.valueIterator(); it.hasNext(); ) {
        bh.consume(it.next());
      }
    }

    @Benchmark
    public void vavr_tree(Blackhole bh) {
      Map<Integer, Integer> values = vavrTreeMap;
      for (Integer integer : values.valuesIterator()) {
        bh.consume(integer);
      }
    }

    @Benchmark
    public void vavr_hash(Blackhole bh) {
      Map<Integer, Integer> values = vavrHash;
      for (Integer integer : values.valuesIterator()) {
        bh.consume(integer);
      }

    }

    @Benchmark
    public void vavr_linked_hash(Blackhole bh) {
      Map<Integer, Integer> values = vavrLinkedHash;
      for (Integer integer : values.valuesIterator()) {
        bh.consume(integer);
      }
    }
  }

  @SuppressWarnings("Duplicates")
  public static class MapRemove extends Base {
    @Benchmark
    public Object pcollections_immutable() {
      org.pcollections.PMap<Integer, Integer> values = pcollectionsImmutable;
      for (Integer removeMe : REMOVAL) {
        values = values.minus(removeMe);
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object capsule_immutable() {
      io.usethesource.capsule.Map.Immutable<Integer, Integer> values = capsuleImmutable;
      for (Integer removeMe : REMOVAL) {
        values = values.__remove(removeMe);
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object vavr_tree() {
      Map<Integer, Integer> values = vavrTreeMap;
      for (Integer removeMe : REMOVAL) {
        values = values.remove(removeMe);
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object vavr_hash() {
      Map<Integer, Integer> values = vavrHash;
      for (Integer removeMe : REMOVAL) {
        values = values.remove(removeMe);
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object vavr_linked_hash() {
      Map<Integer, Integer> values = vavrLinkedHash;
      for (Integer removeMe : REMOVAL) {
        values = values.remove(removeMe);
      }
      assert values.isEmpty();
      return values;
    }
  }

  @SuppressWarnings("Duplicates")
  public static class MapReplaceSingle extends Base {
    @Benchmark
    public Object pcollections_immutable() {
      org.pcollections.PMap<Integer, Integer> values = pcollectionsImmutable;
      Integer key = REMOVAL[0];
      Integer newValue = ELEMENTS[key] + 1;
      values = values.plus(key, newValue);
      return values;
    }

    @Benchmark
    public Object capsule_immutable() {
      io.usethesource.capsule.Map.Immutable<Integer, Integer> values = capsuleImmutable;
      Integer key = REMOVAL[0];
      Integer newValue = ELEMENTS[key] + 1;
      values = values.__put(key, newValue);
      return values;
    }

    @Benchmark
    public Object vavr_tree() {
      Map<Integer, Integer> values = vavrTreeMap;
      Integer key = REMOVAL[0];
      Integer newValue = ELEMENTS[key] + 1;
      values = values.put(key, newValue);
      return values;
    }

    @Benchmark
    public Object vavr_hash() {
      Map<Integer, Integer> values = vavrHash;
      Integer key = REMOVAL[0];
      Integer newValue = ELEMENTS[key] + 1;
      values = values.put(key, newValue);
      return values;
    }

    @Benchmark
    public Object vavr_linked_hash() {
      Map<Integer, Integer> values = vavrLinkedHash;
      Integer key = REMOVAL[0];
      Integer newValue = ELEMENTS[key] + 1;
      values = values.put(key, newValue);
      return values;
    }
  }

  @SuppressWarnings("CollectionAddedToSelf")
  public static class MapReplaceAll extends Base {
    @Benchmark
    public Object pcollections_immutable() {
      org.pcollections.PMap<Integer, Integer> values = pcollectionsImmutable;
      values = values.plusAll(values);
      org.pcollections.PMap<Integer, Integer> result = values;
      assert vavrTreeMap.forAll((e) -> result.get(e._1).equals(e._2));
      return values;
    }

    @Benchmark
    public Object capsule_immutable() {
      io.usethesource.capsule.Map.Immutable<Integer, Integer> values = capsuleImmutable;
      values = values.__putAll(values);
      io.usethesource.capsule.Map.Immutable<Integer, Integer> result = values;
      assert vavrTreeMap.forAll((e) -> result.get(e._1).equals(e._2));
      return values;
    }

    @Benchmark
    public Object vavr_tree() {
      Map<Integer, Integer> values = vavrTreeMap;
      values = values.merge(values, (l, r) -> r);
      return values;
    }

    @Benchmark
    public Object vavr_hash() {
      Map<Integer, Integer> values = vavrHash;
      values = values.merge(values, (l, r) -> r);
      return values;
    }

    @Benchmark
    public Object vavr_linked_hash() {
      Map<Integer, Integer> values = vavrLinkedHash;
      values = values.merge(values, (l, r) -> r);
      return values;
    }
  }

  @SuppressWarnings("CollectionAddedToSelf")
  public static class MapReplaceAllOneByOne extends Base {
    @Benchmark
    public Object pcollections_immutable() {
      org.pcollections.PMap<Integer, Integer> values = pcollectionsImmutable;
      Integer[] elements = ELEMENTS;
      for (int i = 0; i < elements.length; i++) {
        values = values.plus(i, elements[i] + 1);
      }
      return values;
    }

    @Benchmark
    public Object capsule_immutable() {
      io.usethesource.capsule.Map.Immutable<Integer, Integer> values = capsuleImmutable;
      Integer[] elements = ELEMENTS;
      for (int i = 0; i < elements.length; i++) {
        values = values.__put(i, elements[i] + 1);
      }
      return values;
    }

    @Benchmark
    public Object vavr_tree() {
      Map<Integer, Integer> values = vavrTreeMap;
      Integer[] elements = ELEMENTS;
      for (int i = 0; i < elements.length; i++) {
        values = values.put(i, elements[i] + 1);
      }
      return values;
    }

    @Benchmark
    public Object vavr_hash() {
      Map<Integer, Integer> values = vavrHash;
      Integer[] elements = ELEMENTS;
      for (int i = 0; i < elements.length; i++) {
        values = values.put(i, elements[i] + 1);
      }
      return values;
    }

    @Benchmark
    public Object vavr_linked_hash() {
      Map<Integer, Integer> values = vavrLinkedHash;
      Integer[] elements = ELEMENTS;
      for (int i = 0; i < elements.length; i++) {
        values = values.put(i, elements[i] + 1);
      }
      return values;
    }
  }

}
