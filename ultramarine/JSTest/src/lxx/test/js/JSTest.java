package lxx.test.js;

import robocode.AdvancedRobot;
import com.sun.script.javascript.RhinoScriptEngine;

import javax.script.ScriptException;
import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

/**
 * User: jdev
 * Date: 11.11.2009
 */
public class JSTest extends AdvancedRobot {

    public void run() {
        RhinoScriptEngine rse = new RhinoScriptEngine();
        try {
            ScriptContext ctx = new SimpleScriptContext();
            rse.eval("var a = 100;", ctx);
            Double i = (Double) ctx.getAttribute("a");
            ahead(i);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}
