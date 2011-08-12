/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.robocode.benchmark2;

import robocode.control.RobocodeEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BotBenchmark2 {

    public static final int ROUNDS = 35;

    private BenchmarkResults benchmarkResults = new BenchmarkResults();

    private void run(String challengerBotName, Pairing[] pairings) throws SQLException {
        System.out.println("Rounds: " + ROUNDS);
        RobocodeEngine engine = new RobocodeEngine(new File("D:\\my\\rc1730\\"));
        DAO dao = new DAO();
        try {
            Challenge c = new Challenge(challengerBotName, pairings);
            c.execute(engine);
            dao.storeChallenge(c);
            System.out.println("Challenge id: " + c.getStartTime());
            List<String[]> results = dao.getResults(c.getStartTime());

            for (String[] row : results) {
                for (String value : row) {
                    System.out.print(value + "; ");
                }
                System.out.println();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        dao.dispose();
    }

    public static void main(String[] args) throws IOException, SQLException {
        System.setProperty("PARALLEL", "true");
        BotBenchmark2 bb2 = new BotBenchmark2();
        bb2.run(args[0], getPairings(args[1]));
    }

    private static Pairing[] getPairings(String listName) throws IOException {

        final List<Pairing> res = new ArrayList<>();
        String pairing;
        try (
                BufferedReader br = new BufferedReader(new FileReader(".\\bot_lists\\" + listName + ".bl"));
        ) {
            while ((pairing = br.readLine()) != null) {
                String[] line = pairing.split(";");
                res.add(new Pairing(line[0], Integer.parseInt(line[1])));
            }
        }
        Pairing[] resArr = new Pairing[res.size()];
        return res.toArray(resArr);
    }

}