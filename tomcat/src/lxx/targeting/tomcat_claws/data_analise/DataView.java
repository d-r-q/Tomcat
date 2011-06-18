package lxx.targeting.tomcat_claws.data_analise;

import lxx.ts_log.TurnSnapshot;

import java.util.Set;

/**
 * User: jdev
 * Date: 17.06.11
 */
public interface DataView {

    Set<TurnSnapshot> getDataSet(TurnSnapshot ts);

    void addEntry(TurnSnapshot ts);

}
