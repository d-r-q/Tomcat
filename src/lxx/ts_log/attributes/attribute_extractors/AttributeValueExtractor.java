/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors;

import lxx.EnemySnapshot;
import lxx.MySnapshot;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public interface AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshot enemy, MySnapshot me);

}
