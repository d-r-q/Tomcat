/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.robocode.benchmark2;

import org.h2.Driver;

import java.sql.*;

public class DAO {

    private final Connection conn;
    private final PreparedStatement insertChallenge;

    public DAO() throws SQLException {
        DriverManager.registerDriver(new Driver());
        conn = DriverManager.getConnection("jdbc:h2:file:./database/benchmark", "bb", "bb");

        final Statement statement = conn.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS challenges (id BIGINT PRIMARY KEY, challenger_name VARCHAR, seasons INT)");
        statement.close();
        insertChallenge = conn.prepareStatement("INSERT INTO challenges (id, challenger_name, seasons) VALUES (?, ?, ?)");
    }

    public void storeChallenge(Challenge challenge) throws SQLException {
        insertChallenge.setLong(1, challenge.getStartTime());
        insertChallenge.setString(2, challenge.getChallengerBotName());
        insertChallenge.setInt(3, challenge.getSeasons());
        insertChallenge.execute();

        final Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM challenges");

        while (rs.next()) {
            System.out.println(rs.getString(1));
        }
    }

    public void dispose() throws SQLException {
        insertChallenge.close();
        conn.close();
    }

}
