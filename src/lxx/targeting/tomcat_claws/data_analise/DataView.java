/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws.data_analise;

import lxx.ts_log.TurnSnapshot;

import java.util.Collection;

/**
 * User: jdev
 * Date: 17.06.11
 */
public interface DataView {

    Collection<TurnSnapshot> getDataSet(TurnSnapshot ts);

    void addEntry(TurnSnapshot ts);

}
