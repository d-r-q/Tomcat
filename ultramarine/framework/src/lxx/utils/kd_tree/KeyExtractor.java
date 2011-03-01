package lxx.utils.kd_tree;

import lxx.targeting.Target;

/**
 * User: jdev
 * Date: 31.10.2009
 */
public interface KeyExtractor<T> {

    String extractKey(T t, int level);
    boolean canExtract(int level);
    
}
