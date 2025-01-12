/*
 *
 * This file is part of aEventos, licensed under the MIT License.
 *
 * Copyright (c) Ars3ne
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.ars3ne.eventos.manager;

import com.ars3ne.eventos.aEventos;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class ConnectionManager {

    private final ConnectionPoolManager pool;

    private final JSONParser parser = new JSONParser();

    private Connection sqlite_connection;
    private final boolean is_sqlite = !aEventos.getInstance().getConfig().getBoolean("MySQL.Enabled");

    public ConnectionManager() {
        pool = new ConnectionPoolManager();
    }

    public void close() {

        if(is_sqlite) {
            try {
                sqlite_connection.close();
            } catch (SQLException ignored) { }
        }else {
            pool.closePool();
        }

    }

    public boolean setup(){

        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;

        try {

            if(is_sqlite) {
                conn = getSQLiteConnection();
            }else {
                conn = pool.getConnection();
            }

            if(aEventos.getInstance().getConfig().getBoolean("MySQL.Enabled")) {

                ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `aEventos_users` ( `id` integer PRIMARY KEY AUTO_INCREMENT NOT NULL, `username` TEXT NOT NULL , `uuid` TEXT NOT NULL , `total_wins` INT NOT NULL , `total_participations` INT NOT NULL , `wins` TEXT NOT NULL , `participations` TEXT NOT NULL )");
                ps2 = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `aEventos_eventos` ( `id` integer PRIMARY KEY AUTO_INCREMENT NOT NULL, `name` TEXT NOT NULL , `current_winners` TEXT NOT NULL )");
                ps3 = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `aEventos_eventos_guild` ( `id` integer PRIMARY KEY AUTO_INCREMENT NOT NULL, `name` TEXT NOT NULL , `current_guild_winner` TEXT NOT NULL, `total_kills` TEXT NOT NULL, `current_winners` TEXT NOT NULL )");

            }else {

                ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `aEventos_users` ( `id` integer PRIMARY KEY AUTOINCREMENT NOT NULL, `username` TEXT NOT NULL , `uuid` TEXT NOT NULL , `total_wins` INT NOT NULL , `total_participations` INT NOT NULL , `wins` TEXT NOT NULL , `participations` TEXT NOT NULL )");
                ps2 = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `aEventos_eventos` ( `id` integer PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL , `current_winners` TEXT NOT NULL )");
                ps3 = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `aEventos_eventos_guild` ( `id` integer PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL , `current_guild_winner` TEXT NOT NULL, `total_kills` TEXT NOT NULL, `current_winners` TEXT NOT NULL )");

            }

            ps.executeUpdate();
            ps2.executeUpdate();
            ps3.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }finally {
            pool.close(is_sqlite, conn, ps, null);
            pool.close(is_sqlite, conn, ps2, null);
            pool.close(is_sqlite, conn, ps3, null);
        }

        return true;
    }

    public void createEvento(String name) {

        Bukkit.getScheduler().runTaskAsynchronously(aEventos.getInstance(), () -> {

            try {

                Connection conn;
                if(is_sqlite) {
                    conn = getSQLiteConnection();
                }else {
                    conn = pool.getConnection();
                }

                PreparedStatement statement = conn
                        .prepareStatement("SELECT name FROM aEventos_eventos WHERE name=?");
                statement.setString(1,name);
                ResultSet results = statement.executeQuery();

                if(!results.next()) {
                    PreparedStatement insert = conn
                            .prepareStatement("INSERT INTO aEventos_eventos (name, current_winners) VALUES (?,?)");
                    insert.setString(1, name);
                    insert.setString(2, "[]");
                    insert.executeUpdate();
                    insert.close();
                }

                pool.close(is_sqlite, conn, statement, results);

            }catch (SQLException e) {

                Bukkit.getConsoleSender().sendMessage(e.getMessage());

            }

        });

    }

    public void createEventoGuild(String name) {

        Bukkit.getScheduler().runTaskAsynchronously(aEventos.getInstance(), () -> {

            try {

                Connection conn;
                if(is_sqlite) {
                    conn = getSQLiteConnection();
                }else {
                    conn = pool.getConnection();
                }

                PreparedStatement statement = conn
                        .prepareStatement("SELECT name FROM aEventos_eventos_guild WHERE name=?");
                statement.setString(1,name);
                ResultSet results = statement.executeQuery();

                if(!results.next()) {
                    PreparedStatement insert = conn
                            .prepareStatement("INSERT INTO aEventos_eventos_guild (name, current_guild_winner, total_kills, current_winners) VALUES (?,?,?,?)");
                    insert.setString(1, name);
                    insert.setString(2, "[]");
                    insert.setString(3, "[]");
                    insert.setString(4, "[]");
                    insert.executeUpdate();
                    insert.close();
                }

                pool.close(is_sqlite, conn, statement, results);

            }catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
            }

        });

    }

    public void insertUser(UUID uuid) {

        Bukkit.getScheduler().runTaskAsynchronously(aEventos.getInstance(), () -> {

            if(aEventos.getCacheManager().getPlayerTopWinsList().containsKey(Bukkit.getOfflinePlayer(uuid))) return;
            if(aEventos.getCacheManager().getTopParticipations().containsKey(Bukkit.getOfflinePlayer(uuid))) return;

            try {

                Connection conn;
                if(is_sqlite) {
                    conn = getSQLiteConnection();
                }else {
                    conn = pool.getConnection();
                }

                PreparedStatement insert = conn
                        .prepareStatement("INSERT INTO aEventos_users (username, uuid, total_wins, total_participations, wins, participations) VALUES (?,?,?,?,?,?)");
                insert.setString(1, Bukkit.getOfflinePlayer(uuid).getName());
                insert.setString(2, uuid.toString());
                insert.setInt(3, 0);
                insert.setInt(4, 0);
                insert.setString(5, "{}");
                insert.setString(6, "{}");
                insert.executeUpdate();
                insert.close();

                pool.close(is_sqlite, conn, null, null);

            }catch (SQLException e) {

                Bukkit.getConsoleSender().sendMessage(e.getMessage());

            }

        });

    }


    public String getWins(UUID uuid) {

        try {

            Connection conn;
            if(is_sqlite) {
                conn = getSQLiteConnection();
            }else {
                conn = pool.getConnection();
            }

            PreparedStatement statement = conn
                    .prepareStatement("SELECT wins FROM aEventos_users WHERE uuid=?");
            statement.setString(1,uuid.toString());
            ResultSet results = statement.executeQuery();
            String result;
            if(results.next()) {
                result = results.getString("wins");
            }else {
                result =  "{}";
            }

            pool.close(is_sqlite, conn, statement, results);
            return result;

        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
        }

        return null;

    }

    public void addWin(String name, UUID uuid) {

        Bukkit.getScheduler().runTaskAsynchronously(aEventos.getInstance(), () -> {

            try {

                Connection conn;
                if(is_sqlite) {
                    conn = getSQLiteConnection();
                }else {
                    conn = pool.getConnection();
                }

                JSONObject json = (JSONObject) parser.parse(getWins(uuid));

                if(!json.containsKey(name)) json.put(name, 0);

                int wins = Integer.parseInt(json.get(name).toString());
                json.remove(name);
                json.put(name, wins + 1);

                PreparedStatement update = conn
                        .prepareStatement("UPDATE aEventos_users SET wins=?,total_wins=total_wins+1 WHERE uuid=?");
                update.setObject(1, json.toString());
                update.setString(2, uuid.toString());
                update.executeUpdate();

                pool.close(is_sqlite, conn, update, null);

            } catch (ParseException | SQLException e) {

                Bukkit.getConsoleSender().sendMessage(e.getMessage());

            }

        });

    }

    public void addWins(String name, UUID uuid, int qtd) {

        Bukkit.getScheduler().runTaskAsynchronously(aEventos.getInstance(), () -> {

            try {

                Connection conn;
                if(is_sqlite) {
                    conn = getSQLiteConnection();
                }else {
                    conn = pool.getConnection();
                }

                JSONObject json = (JSONObject) parser.parse(getWins(uuid));

                if(!json.containsKey(name)) json.put(name, 0);

                int wins = Integer.parseInt(json.get(name).toString());
                json.remove(name);
                json.put(name, wins + qtd);

                PreparedStatement update = conn
                        .prepareStatement("UPDATE aEventos_users SET wins=?,total_wins=total_wins+? WHERE uuid=?");
                update.setObject(1, json.toString());
                update.setInt(2, qtd);
                update.setString(3, uuid.toString());
                update.executeUpdate();

                pool.close(is_sqlite, conn, update, null);

            } catch (ParseException | SQLException e) {
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
            }

        });

    }

    public String getParticipations(UUID uuid) {

        try {

            Connection conn;
            if(is_sqlite) {
                conn = getSQLiteConnection();
            }else {
                conn = pool.getConnection();
            }

            PreparedStatement statement = conn
                    .prepareStatement("SELECT participations FROM aEventos_users WHERE uuid=?");
            statement.setString(1,uuid.toString());
            ResultSet results = statement.executeQuery();
            String result;

            if(results.next()) {
                result = results.getString("participations");
            }else {
                result = "{}";
            }

            pool.close(is_sqlite, conn, statement, results);
            return result;

        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
        }

        return null;

    }

    public void addParticipation(String name, UUID uuid) {

        Bukkit.getScheduler().runTaskAsynchronously(aEventos.getInstance(), () -> {

            try {

                Connection conn;
                if(is_sqlite) {
                    conn = getSQLiteConnection();
                }else {
                    conn = pool.getConnection();
                }

                String participations = getParticipations(uuid);
                JSONObject json = (JSONObject) parser.parse( participations == null ? "{}" : participations );

                if(!json.containsKey(name)) json.put(name, 0);

                int wins = Integer.parseInt(json.get(name).toString());
                json.remove(name);
                json.put(name, wins + 1);

                PreparedStatement update = conn
                        .prepareStatement("UPDATE aEventos_users SET participations=?,total_participations=total_participations+1 WHERE uuid=?");
                update.setObject(1, json.toString());
                update.setString(2, uuid.toString());
                update.executeUpdate();

                pool.close(is_sqlite, conn, update, null);

            } catch (ParseException | SQLException e) {
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
            }

        });

    }

    public void addParticipations(String name, UUID uuid, int qtd) {

        Bukkit.getScheduler().runTaskAsynchronously(aEventos.getInstance(), () -> {

            try {

                Connection conn;
                if(is_sqlite) {
                    conn = getSQLiteConnection();
                }else {
                    conn = pool.getConnection();
                }

                JSONObject json = (JSONObject) parser.parse(getParticipations(uuid));

                if(!json.containsKey(name)) json.put(name, 0);

                int wins = Integer.parseInt(json.get(name).toString());
                json.remove(name);
                json.put(name, wins + qtd);

                PreparedStatement update = conn
                        .prepareStatement("UPDATE aEventos_users SET participations=?,total_participations=total_participations+? WHERE uuid=?");
                update.setObject(1, json.toString());
                update.setInt(2, qtd);
                update.setString(3, uuid.toString());
                update.executeUpdate();

                pool.close(is_sqlite, conn, update, null);

            } catch (ParseException | SQLException e) {
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
            }

        });

    }

    public String getEventoWinners(String name) {

        try {

            Connection conn;
            if(is_sqlite) {
                conn = getSQLiteConnection();
            }else {
                conn = pool.getConnection();
            }

            PreparedStatement statement = conn
                    .prepareStatement("SELECT current_winners FROM aEventos_eventos WHERE name=?");
            statement.setString(1,name);
            ResultSet results = statement.executeQuery();

            String result;
            if(results.next()) {
                result = results.getString("current_winners");
            }else {
                result = "[]";
            }

            pool.close(is_sqlite, conn, statement, results);
            return result;

        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
        }

        return null;
    }

    public String getEventoGuildWinners(String name) {

        try {

            Connection conn;
            if(is_sqlite) {
                conn = getSQLiteConnection();
            }else {
                conn = pool.getConnection();
            }

            PreparedStatement statement = conn
                    .prepareStatement("SELECT current_winners FROM aEventos_eventos_guild WHERE name=?");
            statement.setString(1,name);
            ResultSet results = statement.executeQuery();
            String result;
            if(results.next()) {
                result = results.getString("current_winners");
            }else {
                result = "[]";
            }

            pool.close(is_sqlite, conn, statement, results);
            return result;

        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
        }

        return null;
    }

    public String getEventoGuildKills(String name) {

        Gson gson = new Gson();

        try {

            Connection conn;
            if(is_sqlite) {
                conn = getSQLiteConnection();
            }else {
                conn = pool.getConnection();
            }

            PreparedStatement statement = conn
                    .prepareStatement("SELECT total_kills FROM aEventos_eventos_guild WHERE name=?");
            statement.setString(1,name);
            ResultSet results = statement.executeQuery();
            String result;
            if(results.next()) {
                result = gson.toJson(results.getString("total_kills"));
            }else {
                result = "[]";
            }

            pool.close(is_sqlite, conn, statement, results);
            return result;

        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
        }

        return null;
    }

    public void setEventoWinner(String name, List<String> winner) {

        Bukkit.getScheduler().runTaskAsynchronously(aEventos.getInstance(), () -> {

            try {

                Connection conn;
                if(is_sqlite) {
                    conn = getSQLiteConnection();
                }else {
                    conn = pool.getConnection();
                }

                PreparedStatement update = conn
                        .prepareStatement("UPDATE aEventos_eventos SET current_winners=? WHERE name=?");
                update.setString(1, String.valueOf(winner));
                update.setString(2, name);
                update.executeUpdate();

                aEventos.updateTags();
                pool.close(is_sqlite, conn, update, null);

            }catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
            }

        });

    }

    public void setEventoGuildWinner(String name, String guild_name, HashMap<OfflinePlayer, Integer> total_kills, List<String> winner) {

        Gson gson = new Gson();

        Bukkit.getScheduler().runTaskAsynchronously(aEventos.getInstance(), () -> {

            try {

                Connection conn;
                if(is_sqlite) {
                    conn = getSQLiteConnection();
                }else {
                    conn = pool.getConnection();
                }

                PreparedStatement update = conn
                        .prepareStatement("UPDATE aEventos_eventos_guild SET current_guild_winner=?,total_kills=?,current_winners=? WHERE name=?");
                update.setString(1, guild_name);
                update.setString(2, gson.toJson(total_kills));
                update.setString(3, String.valueOf(winner));
                update.setString(4, name);
                update.executeUpdate();

                aEventos.updateTags();
                pool.close(is_sqlite, conn, update, null);

            }catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
            }

        });

    }

    public Map<String, Integer> getPlayerWins(UUID uuid) {

        Map<String, Integer> wins = new HashMap<>();

        try {

            Connection conn;
            if(is_sqlite) {
                conn = getSQLiteConnection();
            }else {
                conn = pool.getConnection();
            }

            PreparedStatement statement = conn
                    .prepareStatement("SELECT wins FROM aEventos_users WHERE uuid=?");
            statement.setString(1,uuid.toString());
            ResultSet results = statement.executeQuery();

            if(results.next()) {

                JsonObject jsonObject = (new JsonParser()).parse(results.getString("wins")).getAsJsonObject();

                Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                for(Map.Entry<String,JsonElement> entry : entrySet){
                    wins.put(entry.getKey(), jsonObject.get(entry.getKey()).getAsInt());
                }

                pool.close(is_sqlite, conn, statement, results);
                return wins;

            }else {
                pool.close(is_sqlite, conn, statement, results);
                return null;
            }

        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
        }

        return null;

    }

    public Map<String, Integer> getPlayerParticipations(UUID uuid) {

        Map<String, Integer> participations = new HashMap<>();

        try {

            Connection conn;
            if(is_sqlite) {
                conn = getSQLiteConnection();
            }else {
                conn = pool.getConnection();
            }

            PreparedStatement statement = conn
                    .prepareStatement("SELECT participations FROM aEventos_users WHERE uuid=?");
            statement.setString(1,uuid.toString());
            ResultSet results = statement.executeQuery();
            if(results.next()) {

                JsonObject jsonObject = (new JsonParser()).parse(results.getString("participations")).getAsJsonObject();

                Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                for(Map.Entry<String,JsonElement> entry : entrySet){
                    participations.put(entry.getKey(), jsonObject.get(entry.getKey()).getAsInt());
                }

                pool.close(is_sqlite, conn, statement, results);
                return participations;

            }else {
                pool.close(is_sqlite, conn, statement, results);
                return null;
            }

        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
        }

        return null;

    }

    public void getPlayersWins() {

        Bukkit.getScheduler().runTaskAsynchronously(aEventos.getInstance(), () -> {

            try {

                Connection conn;
                if(is_sqlite) {
                    conn = getSQLiteConnection();
                }else {
                    conn = pool.getConnection();
                }

                PreparedStatement statement = conn
                        .prepareStatement("SELECT uuid,wins FROM aEventos_users");
                ResultSet results = statement.executeQuery();

                while(results.next()) {

                    Map<String, Integer> wins = new HashMap<>();

                    OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(results.getString("uuid")));
                    JsonObject jsonObject = (new JsonParser()).parse(results.getString("wins")).getAsJsonObject();

                    Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                    for(Map.Entry<String,JsonElement> entry : entrySet){
                        if(jsonObject.get(entry.getKey()) == null || jsonObject.get(entry.getKey()).isJsonNull()) continue;
                        wins.put(entry.getKey(), jsonObject.get(entry.getKey()).getAsInt());
                    }

                    aEventos.getCacheManager().getPlayerWinsList().put(player, wins);

                }

                aEventos.getCacheManager().calculateTopWins();
                pool.close(is_sqlite, conn, statement, results);

            }catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
            }

        });

    }

    public void getPlayersParticipations() {

        Bukkit.getScheduler().runTaskAsynchronously(aEventos.getInstance(), () -> {

            try {

                Connection conn;
                if(is_sqlite) {
                    conn = getSQLiteConnection();
                }else {
                    conn = pool.getConnection();
                }

                PreparedStatement statement = conn
                        .prepareStatement("SELECT uuid,participations FROM aEventos_users");
                ResultSet results = statement.executeQuery();
                while(results.next()) {

                    Map<String, Integer> participations = new HashMap<>();

                    OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(results.getString("uuid")));
                    JsonObject jsonObject = (new JsonParser()).parse(results.getString("participations")).getAsJsonObject();

                    Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                    for(Map.Entry<String,JsonElement> entry : entrySet){
                        if(jsonObject.get(entry.getKey()) == null || jsonObject.get(entry.getKey()).isJsonNull()) continue;
                        participations.put(entry.getKey(), jsonObject.get(entry.getKey()).getAsInt());
                    }

                    aEventos.getCacheManager().getPlayerParticipationsList().put(player, participations);

                }

                aEventos.getCacheManager().calculateTopParticipations();
                pool.close(is_sqlite, conn, statement, results);

            }catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
            }

        });

    }

    public void setTotalWins(UUID uuid, int wins) {

        Bukkit.getScheduler().runTaskAsynchronously(aEventos.getInstance(), () -> {

            try {

                Connection conn;
                if(is_sqlite) {
                    conn = getSQLiteConnection();
                }else {
                    conn = pool.getConnection();
                }

                PreparedStatement update = conn
                        .prepareStatement("UPDATE aEventos_users SET wins=?,total_wins=? WHERE uuid=?");
                update.setString(1, "{\"converted\": " + wins + "}");
                update.setInt(2, wins);
                update.setString(3, uuid.toString());
                update.executeUpdate();

                pool.close(is_sqlite, conn, update, null);

            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
            }

        });

    }

    public void setTotalParticipations(UUID uuid, int participations) {

        Bukkit.getScheduler().runTaskAsynchronously(aEventos.getInstance(), () -> {

            try {

                Connection conn;
                if(is_sqlite) {
                    conn = getSQLiteConnection();
                }else {
                    conn = pool.getConnection();
                }

                PreparedStatement update = conn
                        .prepareStatement("UPDATE aEventos_users SET participations=?,total_participations=? WHERE uuid=?");
                update.setString(1, "{\"converted\": " + participations + "}");
                update.setInt(2, participations);
                update.setString(3, uuid.toString());
                update.executeUpdate();

                pool.close(is_sqlite, conn, update, null);

            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
            }

        });

    }

    public boolean isEmpty() {

        try {

            Connection conn = pool.getConnection();

            PreparedStatement statement = conn
                    .prepareStatement("SELECT id from aEventos_users WHERE id=1");
            ResultSet results = statement.executeQuery();

            boolean has_results = results.next();

            pool.close(is_sqlite, conn, statement, results);
            return has_results;

        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
        }

        return true;
    }

    private Connection getSQLiteConnection() {

            if(sqlite_connection == null) {
                try {
                    Class.forName("org.sqlite.JDBC");
                    File database_file = new File(aEventos.getInstance().getDataFolder(), "storage.db");
                    sqlite_connection = DriverManager.getConnection("jdbc:sqlite:" + database_file);
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
            }


        return sqlite_connection;
    }
}