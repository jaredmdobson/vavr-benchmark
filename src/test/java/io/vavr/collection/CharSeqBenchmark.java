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

import java.util.Objects;
import java.util.Random;

import static java.lang.String.valueOf;
import static io.vavr.JmhRunner.create;
import static io.vavr.collection.Collections.areEqual;

public class CharSeqBenchmark {
  static final Array<Class<?>> CLASSES = Array.of(
      Head.class,
      Tail.class,
      Get.class,
      Update.class,
      Repeat.class,
      Prepend.class,
      Append.class,
      Iterate.class
  );

  public static void main(java.lang.String... args) {
    JmhRunner.runNormalNoAsserts(CLASSES);
  }

  public static class Base extends CollectionBenchmarkBase {

    int EXPECTED_AGGREGATE;
    char[] ELEMENTS;

    java.lang.String javaImmutable;
    fj.data.LazyString fjavaImmutable;
    io.vavr.collection.CharSeq vavrImmutable;

    @Setup
    public void setup() {
      final Random random = new Random(0);
      ELEMENTS = new char[CONTAINER_SIZE];
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        ELEMENTS[i] = (char) random.nextInt(Character.MAX_VALUE);
      }
      EXPECTED_AGGREGATE = Iterator.ofAll(ELEMENTS).reduce((x, y) -> (char) JmhRunner.aggregate((int) x, (int) y));

      javaImmutable = create(java.lang.String::new, ELEMENTS, ELEMENTS.length, v -> java.util.Arrays.equals(v.toCharArray(), ELEMENTS));
      fjavaImmutable = create(fj.data.LazyString::str, javaImmutable, javaImmutable.length(), v -> Objects.equals(v.toStringEager(), javaImmutable));
      vavrImmutable = create(io.vavr.collection.CharSeq::of, javaImmutable, javaImmutable.length(), v -> v.contentEquals(javaImmutable));
    }
  }

  public static class Head extends Base {
    @Benchmark
    public Object java_immutable() {
      final Object head = javaImmutable.charAt(0);
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object fjava_immutable() {
      final Object head = fjavaImmutable.head();
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }

    @Benchmark
    public Object vavr_immutable() {
      final Object head = vavrImmutable.head();
      assert Objects.equals(head, ELEMENTS[0]);
      return head;
    }
  }

  @SuppressWarnings("Convert2MethodRef")
  public static class Tail extends Base {
    @Benchmark
    public Object java_immutable() {
      java.lang.String values = javaImmutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.substring(1);
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object fjava_immutable() {
      fj.data.LazyString values = fjavaImmutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.tail();
      }
      assert values.isEmpty();
      return values;
    }

    @Benchmark
    public Object vavr_immutable() {
      io.vavr.collection.CharSeq values = vavrImmutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.tail();
      }
      assert values.isEmpty();
      return values;
    }
  }

  public static class Get extends Base {
    @Benchmark
    public int java_immutable() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        aggregate ^= javaImmutable.charAt(i);
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int fjava_immutable() {
      int aggregate = 0;
      for (int i = 0; i < ELEMENTS.length; i++) {
        aggregate ^= fjavaImmutable.charAt(i);
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int vavr_immutable() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        aggregate ^= vavrImmutable.charAt(i);
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }
  }

  public static class Update extends Base {
    final char replacement = '❤';

    @Benchmark
    public Object java_immutable() {
      java.lang.String values = javaImmutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.substring(0, i) + replacement + values.substring(i + 1);
      }
      assert Array.ofAll(values.toCharArray()).forAll(c -> c == replacement);
      return values;
    }

    @Benchmark
    public Object vavr_immutable() {
      io.vavr.collection.CharSeq values = vavrImmutable;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        values = values.update(i, replacement);
      }
      assert values.forAll(c -> c == replacement);
      return values;
    }
  }

  public static class Repeat extends Base {
    final char value = '❤';

    @Benchmark
    public Object vavr_immutable() {
      return CharSeq.of(value).repeat(CONTAINER_SIZE);
    }
  }

  public static class Prepend extends Base {
    @Benchmark
    public Object java_immutable() {
      java.lang.String values = "";
      for (int i = CONTAINER_SIZE - 1; i >= 0; i--) {
        values = ELEMENTS[i] + values;
      }
      assert Objects.equals(values, javaImmutable);
      return values;
    }

    @Benchmark
    public Object fjava_immutable() {
      fj.data.LazyString values = fj.data.LazyString.empty;
      for (int i = CONTAINER_SIZE - 1; i >= 0; i--) {
        values = fj.data.LazyString.str(valueOf(ELEMENTS[i])).append(values);
      }
      assert Objects.equals(values.eval(), javaImmutable);
      return values;
    }

    @Benchmark
    public Object vavr_immutable() {
      io.vavr.collection.CharSeq values = io.vavr.collection.CharSeq.empty();
      for (int i = CONTAINER_SIZE - 1; i >= 0; i--) {
        values = values.prepend(ELEMENTS[i]);
      }
      assert values.contentEquals(vavrImmutable);
      return values;
    }
  }

  public static class Append extends Base {
    @Benchmark
    public Object java_immutable() {
      java.lang.String values = "";
      for (char c : ELEMENTS) {
        values = values + c;
      }
      assert Objects.equals(values, javaImmutable);
      return values;
    }

    @Benchmark
    public Object fjava_immutable() {
      fj.data.LazyString values = fj.data.LazyString.empty;
      for (char c : ELEMENTS) {
        values = values.append(valueOf(c));
      }
      assert areEqual(values.toStream(), vavrImmutable);
      return values;
    }

    @Benchmark
    public Object vavr_immutable() {
      io.vavr.collection.CharSeq values = io.vavr.collection.CharSeq.empty();
      for (char c : ELEMENTS) {
        values = values.append(c);
      }
      assert values.contentEquals(vavrImmutable);
      return values;
    }
  }

  @SuppressWarnings("ForLoopReplaceableByForEach")
  public static class Iterate extends Base {
    @Benchmark
    public int java_immutable() {
      int aggregate = 0;
      for (int i = 0; i < CONTAINER_SIZE; i++) {
        aggregate ^= javaImmutable.charAt(i);
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int fjava_immutable() {
      int aggregate = 0;
      for (final java.util.Iterator<Character> iterator = fjavaImmutable.toStream().iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }

    @Benchmark
    public int vavr_immutable() {
      int aggregate = 0;
      for (final Iterator<Character> iterator = vavrImmutable.iterator(); iterator.hasNext(); ) {
        aggregate ^= iterator.next();
      }
      assert aggregate == EXPECTED_AGGREGATE;
      return aggregate;
    }
  }
}
