package ru.jdev.rc.drc.client;

import robocode.control.BattlefieldSpecification;
import ru.jdev.rc.drc.server.Competitor;
import ru.jdev.rc.drc.server.RSBattleResults;
import ru.jdev.rc.drc.server.RobocodeServer;

import java.io.IOException;

/**
 * User: jdev
 * Date: 13.08.11
 */
public class RobocodeClient {

    private void run() {
        final RobocodeServer server = new RobocodeServer();
        final CompetitorCodeFactory competitorCodeFactory = new CompetitorCodeFactory();
        try {
            final Competitor competitor = competitorCodeFactory.getCompetitorCode("lxx.Tomcat", "3.13.152");
            server.registerCode(competitor);
            competitor.code = null;
            RSBattleResults br = server.executeBattle(new Competitor[]{competitor, competitor}, new BattlefieldSpecification(800, 600), 10);
            System.out.println(br.roundResults.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        RobocodeClient client = new RobocodeClient();
        client.run();
    }

}
