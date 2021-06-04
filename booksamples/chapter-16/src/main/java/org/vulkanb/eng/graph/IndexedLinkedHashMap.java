package org.vulkanb.eng.graph;

import java.util.*;

public class IndexedLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

    private final List<K> al_Index = new ArrayList<>();

    public int getIndexOf(K key) {
        return al_Index.indexOf(key);
    }

    public V getValueAtIndex(int i) {
        return super.get(al_Index.get(i));
    }

    @Override
    public V put(K key, V val) {
        if (!super.containsKey(key)) al_Index.add(key);
        return super.put(key, val);
    }
}
