/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import robocode.control.BattlefieldSpecification;

import java.util.Map;

public class BattleRequest {

    public final long requestId;
    public final String secureToken;

    public final Competitor[] competitors;
    public final Map<Competitor, CompetitorCode> competitorsCode;
    public final int rounds;
    public final BattlefieldSpecification bfSpec;

    public BattleRequest(long requestId, String secureToken, Competitor[] competitors, Map<Competitor, CompetitorCode> competitorsCode,
                         int rounds, BattlefieldSpecification bfSpec) {
        this.requestId = requestId;
        this.secureToken = secureToken;

        this.competitors = competitors;
        this.competitorsCode = competitorsCode;
        this.rounds = rounds;
        this.bfSpec = bfSpec;
    }
}
