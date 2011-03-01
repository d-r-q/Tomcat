/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.robocode.benchmark2;

import robocode.control.RobocodeEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BotBenchmark2 {

    public static final DateFormat dateFormat = new SimpleDateFormat("mm:ss SSSS");
    public static final int ROUNDS = 35;

    private BenchmarkResults benchmarkResults = new BenchmarkResults();

    private void run(String challengerBotName, String[] rrReferenceBotNames, int seasons) {
        System.out.println("Rounds: " + ROUNDS);
        RobocodeEngine engine = new RobocodeEngine(new File("D:\\my\\rc\\"));
        long startTime = System.currentTimeMillis();
        List<Challenge> challenges = new ArrayList<Challenge>();
        for (String referenceBotName : rrReferenceBotNames) {
            try {
                Challenge c = new Challenge(challengerBotName, referenceBotName, benchmarkResults, seasons);
                c.execute(engine);
                challenges.add(c);
                System.gc();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        for (Challenge c : challenges) {
            c.printBattleResults();
        }

        System.out.println("APS: " + benchmarkResults.avgPS.getCurrentValue());
        System.out.println("AS: " + benchmarkResults.avgScore.getCurrentValue());
        System.out.println("ABDS: " + benchmarkResults.avgBulletDamage.getCurrentValue());
        System.out.println("ASS: " + benchmarkResults.avgSS.getCurrentValue());
        System.out.println("Avg exec time: " + dateFormat.format(new Date((long) benchmarkResults.avgExecTime.getCurrentValue())));
        long globalExecTime = System.currentTimeMillis() - startTime;
        System.out.println("Global exec time: " + dateFormat.format(new Date(globalExecTime)));
    }

    public static void main(String[] args) throws IOException {
        BotBenchmark2 bb2 = new BotBenchmark2();
        bb2.run(args[0], getBotList(args[1]), Integer.valueOf(args[2]));
    }

    private static String[] getBotList(String listName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(".\\bot_lists\\" + listName + ".bl"));
        final List<String> res = new ArrayList<String>();
        String botName;
        try {
            while ((botName = br.readLine()) != null) {
                res.add(botName);
            }
        } finally {
            br.close();
        }
        String[] resArr = new String[res.size()];
        return res.toArray(resArr);
    }

}