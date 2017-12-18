package jetson.vision.math;

import jetson.vision.interfaces.Interpolable;
import jetson.vision.interfaces.InverseInterpolable;

import java.util.Map;
import java.util.TreeMap;

public class InterpolatingTreeMap<K extends InverseInterpolable<K> & Comparable<K>,
                                  V extends Interpolable<V>> extends TreeMap<K, V> {
    int max;

    public InterpolatingTreeMap(int maxSize) {
        max = maxSize;
    }

    public InterpolatingTreeMap() {
        this(0);
    }

    @Override
    public V put(K key, V value) {
        if (max > 0 && max <= size()) {
            K first = firstKey();
            remove(first);
        }

        super.put(key, value);

        return value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        System.out.println("Don't use this!");
    }

    public V getInterpolated(K key) {
        V gotVal = get(key);

        if (gotVal == null) {
            K topBound = ceilingKey(key);
            K bottomBound = floorKey(key);

            if (topBound == null && bottomBound == null) {
                return null;
            } else if(topBound == null) {
                return get(bottomBound);
            } else if (bottomBound == null) {
                return get(topBound);
            }

            V top = get(topBound);
            V bottom = get(bottomBound);
            return bottom.interpolate(top, bottomBound.inverseInterpolate(topBound, key));
        } else {
            return gotVal;
        }
    }
}
