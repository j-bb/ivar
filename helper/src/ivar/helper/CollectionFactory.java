/*
   Copyright (c) 2004-2020, Jean-Baptiste BRIAUD. All Rights Reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License
 */
package ivar.helper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CollectionFactory {

    private CollectionFactory() {
    }

    public static <K, V> Map<K, V> newMapWithInsertionOrderPreserved() {
        return new LinkedHashMap<K, V>();
    }

    public static <K, V> Map<K, V> newConcurrentMap() {
        return new ConcurrentHashMap<K, V>();
    }

    public static <K, V> Map<K, V> newMapWithInsertionOrderPreserved(final int size) {
        return new LinkedHashMap<K, V>(size);
    }

    public static <K, V> Map<K, V> newMap(final Map<K, V> original) {
        return new HashMap<K, V>(original);
    }

    public static <K, V> Map<K, V> newMap() {
        return new HashMap<K, V>();
    }

    public static <K, V> Map<K, V> newMap(final int size) {
        return new HashMap<K, V>(size);
    }

    public static <V> List<V> newList() {
        return new ArrayList<V>();
    }

    public static <V> List<V> newList(final int size) {
        return new ArrayList<V>(size);
    }

    public static <V> List<V> newList(final Collection<V> original) {
        return new ArrayList<V>(original);
    }

    public static <V> Set<V> newSet() {
        //return new HashSet<V>();
        return newSetWithInsertionOrderPreserved();
    }

    public static <V> Set<V> newSet(final int size) {
        //return new HashSet<V>(size);
        return newSetWithInsertionOrderPreserved(size);
    }

    public static <V> Set<V> newSet(final Set<V> original) {
        //return new HashSet<V>(original);
        return newSetWithInsertionOrderPreserved(original);
    }

    public static <V> Set<V> newSetWithInsertionOrderPreserved() {
        return new LinkedHashSet<V>();
    }

    public static <V> Set<V> newSetWithInsertionOrderPreserved(final Set<V> original) {
        return new LinkedHashSet<V>(original);
    }

    public static <V> Set<V> newSetWithInsertionOrderPreserved(final int size) {
        return new LinkedHashSet<V>(size);
    }

    public static <V> Stack<V> newStack() {
        return new Stack<V>();
    }
}
