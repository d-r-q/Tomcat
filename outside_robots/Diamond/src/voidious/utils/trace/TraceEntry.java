package voidious.utils.trace;

/**
 * Copyright (c) 2009-2010 - Voidious
 * <p/>
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * <p/>
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * <p/>
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software.
 * <p/>
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * <p/>
 * 3. This notice may not be removed or altered from any source
 * distribution.
 */

public class TraceEntry {
    private String _className;
    private String _methodName;
    private String _params;
    private long _nanoTime;

    private boolean _entering;

    public TraceEntry(String className, String methodName, String params,
                      boolean entering) {

        _className = className;
        _methodName = methodName;
        _params = params;
        _nanoTime = System.nanoTime();
        _entering = entering;
    }

    public String getClassName() {
        return _className;
    }

    public String getMethodName() {
        return _methodName;
    }

    public String getParams() {
        return _params;
    }

    public long getNanoTime() {
        return _nanoTime;
    }

    public boolean isEntering() {
        return _entering;
    }
}
