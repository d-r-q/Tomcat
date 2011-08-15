/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import robocode.control.BattlefieldSpecification;

import java.util.Map;

public class BattleRequest {

    public final String secureToken;

    public final Competitor[] competitors;
    public final int rounds;
    public final BattlefieldSpecification bfSpec;

    public long requestId;

    public BattleRequest(String secureToken, Competitor[] competitors,
                         int rounds, BattlefieldSpecification bfSpec) {
        this.secureToken = secureToken;

        this.competitors = competitors;
        this.rounds = rounds;
        this.bfSpec = bfSpec;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }
}
