package ru.jdev.rc.drc.server;

import robocode.control.BattlefieldSpecification;

import javax.xml.ws.Endpoint;
import java.io.IOException;

/**
 * User: jdev
 * Date: 13.08.11
 */
public class RobocodeServer {

    private final CodeManager codeManager = new CodeManager();
    private RCBattlesExecutor rcBattlesExecutor = new RCBattlesExecutor();

    public void registerCode(Competitor competitor) throws IOException {
        codeManager.storeCompetitor(competitor);
    }

    public RSBattleResults executeBattle(Competitor[] competitors, BattlefieldSpecification bfSpec, int rounds) throws IOException {
        for (Competitor c : competitors) {
            codeManager.loadCompetitor(c);
        }
        return rcBattlesExecutor.executeBattle(competitors, bfSpec, rounds);
    }

}
