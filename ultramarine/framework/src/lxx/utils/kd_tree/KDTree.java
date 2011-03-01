package lxx.utils.kd_tree;

import lxx.targeting.Target;
import lxx.utils.LXXPoint;

import java.awt.*;

/**
 * User: jdev
 * Date: 22.10.2009
 */
public class KDTree<D extends KDData, T> {

    private final KDNode<D, T> root;

    public KDTree(KeyExtractor<T> keyExtractor, D data) {
        root = new KDNode<D, T>((byte)0, keyExtractor, (D) data.createInstance());
    }

    public void addStat(T t, Object... data) {
        root.addStat(t, data);
    }

    public KDNode<D, T> getNode(T ts) {
        return root.getNode(ts);
    }

    public void visit(KDTreeVisitor kdTreeVisitor) {
        root.visit(kdTreeVisitor);
    }

    public int getNodeCount() {
        return root.getChildCount() + 1;
    }

}
