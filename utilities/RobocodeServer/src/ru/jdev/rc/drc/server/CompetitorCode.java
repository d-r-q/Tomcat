/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import java.util.Arrays;

public class CompetitorCode {

    public final byte[] code;
    public final byte[] hash;

    public CompetitorCode(byte[] code, byte[] hash) {
        this.code = code;
        this.hash = hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompetitorCode that = (CompetitorCode) o;

        if (!Arrays.equals(hash, that.hash)) return false;
        if (!Arrays.equals(code, that.code)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return hash != null ? Arrays.hashCode(hash) : 0;
    }
}
