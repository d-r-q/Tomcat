package voidious.utils.trace;

import java.util.Stack;
import java.util.Vector;
import java.util.regex.Pattern;

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

public class TraceLogger {
    protected static final int INDENT_AMOUNT = 2;

    protected Pattern[] _includePatterns;
    protected Pattern[] _excludePatterns;

    protected Stack<Long> _times;
    protected Vector<TraceEntry> _trace;
    protected int indent;

    public TraceLogger(String[] includeStrs, String[] excludeStrs) {
        _includePatterns = new Pattern[includeStrs.length];
        _excludePatterns = new Pattern[excludeStrs.length];

        fillPattern(_includePatterns, includeStrs);
        fillPattern(_excludePatterns, excludeStrs);

        _times = new Stack<Long>();
        _trace = new Vector<TraceEntry>();
        indent = 0;
    }

    public void log(String className, String methodName, String params,
                    boolean entering) {

        String classMethod = className + "." + methodName;
        boolean doTrace = false;

        for (int x = 0; x < _includePatterns.length; x++) {
            if (_includePatterns[x].matcher(classMethod).matches()) {
                doTrace = true;
                break;
            }
        }

        if (doTrace) {
            for (int x = 0; x < _excludePatterns.length; x++) {
                if (_excludePatterns[x].matcher(classMethod).matches()) {
                    doTrace = false;
                    break;
                }
            }
        }

        if (doTrace) {
            TraceEntry newEntry = new TraceEntry(className, methodName, params,
                    entering);

            _trace.add(newEntry);

            printTraceEntry(newEntry);

            if (entering) {
                _times.push(newEntry.getNanoTime());
            }
        }
    }

    public void printTraceEntry(TraceEntry entry) {
        String traceString = "";

        if (entry.isEntering()) {
            for (int y = 0; y < indent * INDENT_AMOUNT; y++) {
                traceString += " ";
            }
            indent++;
            traceString += "+ "
                    + entry.getClassName() + "." + entry.getMethodName()
                    + "(" + entry.getParams() + ") @ "
                    + entry.getNanoTime();
        } else {
            indent--;
            long deltaTime = entry.getNanoTime() - _times.pop();
            for (int y = 0; y < indent * INDENT_AMOUNT; y++) {
                traceString += " ";
            }
            traceString += "- "
                    + entry.getClassName() + "." + entry.getMethodName()
                    + "(" + entry.getParams() + ") > "
                    + deltaTime;
        }

        System.out.println(traceString);
    }

    protected static void fillPattern(Pattern[] patterns, String[] patternStrs) {
        for (int x = 0; x < patterns.length; x++) {
            patterns[x] = Pattern.compile(patternStrs[x]);
        }
    }
}
