package lxx.debug;

import lxx.office.Office;

/**
 * Created by IntelliJ IDEA.
 * User: 1
 * Date: 21.02.11
 * Time: 23:22
 * To change this template use File | Settings | File Templates.
 */
public class PTGDebugger implements Debugger {
    @Override
    public void roundStarted(Office office) {
        /*PatternTreeGun ptg = new PatternTreeGun(office.getBattleSnapshotManager(), office.getTargetManager());
        office.getPrimarch().addListener(ptg);*/
    }

    @Override
    public void roundEnded() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void battleEnded() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void tick() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
