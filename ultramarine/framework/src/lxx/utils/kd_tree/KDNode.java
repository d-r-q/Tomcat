package lxx.utils.kd_tree;

import java.util.*;

/**
 * User: jdev
 * Date: 22.10.2009
 */
public class KDNode<D extends KDData, T> {

    public static int instanceCount = 0;

    private final TreeMap<String, KDNode<D, T>> children = new TreeMap<String, KDNode<D, T>>(new Comparator<String>() {

        public int compare(String s1, String s2) {
            try {
                Long l1 = new Long(s1);
                Long l2 = new Long(s2);
                return l1.compareTo(l2);
            } catch (NumberFormatException e) {
                return s1.compareTo(s2);
            }
        }
    });

    private final byte level;
    private final KeyExtractor<T> keyExtractor;
    private final D data;


    public KDNode(byte level, KeyExtractor<T> keyExtractor, D data) {
        this.level = level;
        this.keyExtractor = keyExtractor;
        this.data = data;

        instanceCount++;
    }

    public Map<String, KDNode<D, T>> getChildren() {
        return children;
    }

    public void addStat(T t, Object... data) {
        this.data.addStat(data);

        if (keyExtractor.canExtract(level + 1)) {
            final String key = keyExtractor.extractKey(t, level + 1);
            KDNode child = children.get(key);
            if (child == null) {
                child = new KDNode<D, T>((byte) (level + 1), keyExtractor, (D) this.data.createInstance());
                children.put(key, child);
            }

            child.addStat(t, data);
        }
    }

    public KDNode<D, T> getNode(T ts) {
        KDNode<D, T> child = null;
        String key = null;
        if (keyExtractor.canExtract(level + 1)) {
            key = keyExtractor.extractKey(ts, level + 1);
            child = children.get(key);
        }
        if (child != null) {
            return child.getNode(ts);
        }
        return this;
    }

    public D getData() {
        return data;
    }

    public void visit(KDTreeVisitor kdTreeVisitor) {
        kdTreeVisitor.visit(this);
        for (KDNode child : children.values()) {
            child.visit(kdTreeVisitor);
        }
    }

    public byte getLevel() {
        return level;
    }

    public int getChildCount() {
        int childCount = 1;
        for (KDNode n : children.values()) {
            childCount += n.getChildCount();
        }
        return childCount;
    }
}
