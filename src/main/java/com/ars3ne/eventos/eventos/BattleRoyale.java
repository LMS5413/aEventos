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

package com.ars3ne.eventos.eventos;

import com.ars3ne.eventos.aEventos;
import com.ars3ne.eventos.api.Evento;
import com.ars3ne.eventos.api.EventoType;
import com.ars3ne.eventos.api.events.PlayerJoinEvent;
import com.ars3ne.eventos.api.events.PlayerLoseEvent;
import com.ars3ne.eventos.listeners.eventos.BattleRoyaleListener;
import com.ars3ne.eventos.utils.Cuboid;
import com.cryptomorin.xseries.XItemStack;
import com.ars3ne.eventos.utils.colors.IridiumColorAPI;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class BattleRoyale extends Evento {

    private final YamlConfiguration config;
    private final BattleRoyaleListener listener = new BattleRoyaleListener();

    private final int enable_pvp, max_players;
    private boolean pvp_enabled, ended = false;
    private final boolean defined_items, remove_blocks, multiple_spawns, border_enabled;
//
//    private yClansAPI yclans_api;

    private final ArrayList<ClanPlayer> simpleclans_clans = new ArrayList<>();
//    private final HashMap<MPlayer, Faction> massivefactions_factions = new HashMap<>();
//    private final HashMap<yclans.model.ClanPlayer, Clan> yclans_clans = new HashMap<>();

    private final WorldBorder border;
    private final int border_delay, border_damage, border_time, border_size;

    private final List<Block> blocks_to_remove = new ArrayList<>();
    private final Map<Block, Map<Integer, ItemStack>> chests = new HashMap<>();

    private int position_index;

    public BattleRoyale(YamlConfiguration config) {

        super(config);
        this.config = config;
        this.enable_pvp = config.getInt("Evento.Time");
        this.remove_blocks = config.getBoolean("Evento.Remove blocks");
        this.multiple_spawns = config.getBoolean("Evento.Multiple spawns");
        boolean refill_chests = config.getBoolean("Evento.Refill chests");
        this.defined_items = config.getBoolean("Itens.Enabled");
        World world = aEventos.getInstance().getServer().getWorld(this.config.getString("Locations.Pos1.world"));
        this.border = world.getWorldBorder();
        this.border_enabled = config.getBoolean("Border.Enabled");
        this.border_size = config.getInt("Border.Size");
        this.border_delay = config.getInt("Border.Delay");
        this.border_time = config.getInt("Border.Time");
        this.border_damage = config.getInt("Border.Damage");
        this.position_index = refill_chests ? 2 : 0;
        this.max_players = (config.getConfigurationSection("Locations").getKeys(false).size() - 5) - this.position_index;

//        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("yclans")) {
//            yclans_api = yClansAPI.yclansapi;
//        }

        if(refill_chests) {
            // Obtenha o cuboid e os baús dentro dele.
            Location pos1 = new Location(world, this.config.getDouble("Locations.Pos1.x"), this.config.getDouble("Locations.Pos1.y"), this.config.getDouble("Locations.Pos1.z"));
            Location pos2 = new Location(world, this.config.getDouble("Locations.Pos2.x"), this.config.getDouble("Locations.Pos2.y"), this.config.getDouble("Locations.Pos2.z"));
            Cuboid cuboid = new Cuboid(pos1, pos2);

            for(Block block: cuboid.getBlocks()) {

                if(block.getType() != Material.CHEST) continue;
                Chest chest = (Chest) block.getState();

                Map<Integer, ItemStack> insert = new HashMap<>();
                for(int i = 0; i < chest.getInventory().getSize(); i++) {
                    ItemStack item = chest.getInventory().getItem(i);
                    if(item == null || item.getType() == Material.AIR) continue;
                    item = chest.getInventory().getItem(i).clone();
                    insert.put(i, item);
                }

                chests.put(block, insert);

            }

        }

    }

    @Override
    public void start() {

        // Registre o listener do evento
        aEventos.getInstance().getServer().getPluginManager().registerEvents(listener, aEventos.getInstance());
        listener.setEvento();

        // Caso os spawns infinitos estejam ativos, então teleporte os jogadores.
        if(multiple_spawns) {
            Collections.shuffle(getPlayers());
            position_index++;
            for(Player p: getPlayers()) {
                if(config.getConfigurationSection("Locations.Pos" + position_index) != null) {
                    Location teleport = new Location(border.getCenter().getWorld(), this.config.getDouble("Locations.Pos" + position_index + ".x"), this.config.getDouble("Locations.Pos" + position_index + ".y"), this.config.getDouble("Locations.Pos" + position_index + ".z"), this.config.getLong("Locations.Pos" + position_index + ".Yaw"), this.config.getLong("Locations.Pos" + position_index + ".Pitch"));
                    p.teleport(teleport);
                    position_index++;
                }
            }
        }

        // Se os itens setados estão ativados, então os obtenha.
        if(defined_items) {
            for(Player p: getPlayers()) {

                p.getInventory().setHelmet(XItemStack.deserialize(config.getConfigurationSection("Itens.Helmet")));
                p.getInventory().setChestplate(XItemStack.deserialize(config.getConfigurationSection("Itens.Chestplate")));
                p.getInventory().setLeggings(XItemStack.deserialize(config.getConfigurationSection("Itens.Leggings")));
                p.getInventory().setBoots(XItemStack.deserialize(config.getConfigurationSection("Itens.Boots")));

                for(String item: config.getConfigurationSection("Itens.Inventory").getKeys(false)) {
                    p.getInventory().setItem(Integer.parseInt(item), XItemStack.deserialize(config.getConfigurationSection("Itens.Inventory." + item)));
                }

            }
        }

        // Se o servidor tiver SimpleClans, então ative o friendly fire.
        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("simpleclans") && aEventos.getInstance().getSimpleClans() != null) {
            for (Player p : getPlayers()) {
                if (aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p) != null) {
                    simpleclans_clans.add(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p));
                    Objects.requireNonNull(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p)).setFriendlyFire(true);
                }
            }
        }

//        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("massivefactions") && aEventos.getInstance().isHookedMassiveFactions()) {
//            for (Player p : getPlayers()) {
//                massivefactions_factions.put(MPlayer.get(p), MPlayer.get(p).getFaction());
//                MPlayer.get(p).getFaction().setFlag(MFlag.ID_FRIENDLYFIRE, true);
//            }
//        }
//
//        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("yclans") && aEventos.getInstance().isHookedyClans()) {
//            for(Player p: getPlayers()) {
//                if(yclans_api == null || yclans_api.getPlayer(p) == null) continue;
//                yclans.model.ClanPlayer clan_player = yclans_api.getPlayer(p);
//                if(!clan_player.hasClan()) continue;
//                yclans_clans.put(clan_player, clan_player.getClan());
//                clan_player.getClan().setFriendlyFireAlly(true);
//                clan_player.getClan().setFriendlyFireMember(true);
//            }
//        }

        // Mande a mensagem de que o PvP será ativado.
        List<String> starting_st = config.getStringList("Messages.Enabling");

        for (Player player : getPlayers()) {
            for(String s : starting_st) {
                player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@time", String.valueOf(enable_pvp)).replace("@name", config.getString("Evento.Title"))));
            }
        }

        for (Player player : getSpectators()) {
            for(String s : starting_st) {
                player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@time", String.valueOf(enable_pvp)).replace("@name", config.getString("Evento.Title"))));
            }
        }

        // Depois do tempo especificado na config, ative o PvP.
        aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> {

            if(!isHappening()) return;

            this.pvp_enabled = true;

            // Mande a mensagem de que o PvP está ativado.
            List<String> enabled_st = config.getStringList("Messages.Enabled");

            for (Player player : getPlayers()) {
                for(String s : enabled_st) {
                    player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", config.getString("Evento.Title"))));
                }
            }

            for (Player player : getSpectators()) {
                for(String s : enabled_st) {
                    player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", config.getString("Evento.Title"))));
                }
            }

            // Se a borda estiver ativa, então a diminua.
            aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> {

                if(!isHappening()) return;
                if(ended) return;
                if(!border_enabled) return;

                border.setDamageAmount(border_damage);
                border.setSize(1, border_time);

            }, border_delay * 20L);

        }, enable_pvp * 20L);

    }

    @Override
    public void join(Player p) {

        if(getPlayers().size() > max_players && this.multiple_spawns) {
            p.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Max players").replace("&", "§").replace("@name", config.getString("Evento.Title"))));
            return;
        }

        if(requireEmptyInventory()) p.getInventory().clear();

        p.setFoodLevel(20);
        getPlayers().add(p);
        this.teleport(p, "lobby");

        for(PotionEffect potion: p.getActivePotionEffects()) {
            p.removePotionEffect(potion.getType());
        }

        for (Player player : getPlayers()) {
            player.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Joined").replace("&", "§").replace("@player", p.getName())));
        }

        for (Player player : getSpectators()) {
            player.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Joined").replace("&", "§").replace("@player", p.getName())));
        }

        PlayerJoinEvent join = new PlayerJoinEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), EventoType.BATTLE_ROYALE);
        Bukkit.getPluginManager().callEvent(join);

    }

    @Override
    public void leave(Player p) {
        if(getPlayers().contains(p)) {
            for (Player player : getPlayers()) {
                player.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Leave").replace("&", "§").replace("@player", p.getName())));
            }
            for (Player player : getSpectators()) {
                player.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Leave").replace("&", "§").replace("@player", p.getName())));
            }
        }

        // Desative o friendly-fire do jogador.
        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("simpleclans") && aEventos.getInstance().getSimpleClans() != null) {
            simpleclans_clans.remove(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p));
            if(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p) != null) Objects.requireNonNull(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p)).setFriendlyFire(false);
        }

//        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("massivefactions") && aEventos.getInstance().isHookedMassiveFactions()) {
//            massivefactions_factions.remove(MPlayer.get(p));
//            if(getClanMembers(p) < 1) MPlayer.get(p).getFaction().setFlag(MFlag.ID_FRIENDLYFIRE, false);
//        }
//
//        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("yclans") && aEventos.getInstance().isHookedyClans() && !isOpen()) {
//            if(yclans_api == null || yclans_api.getPlayer(p) == null || isOpen()) return;
//            yclans.model.ClanPlayer clan_player = yclans_api.getPlayer(p);
//            if(getClanMembers(p) < 1) {
//                yclans_clans.get(clan_player).setFriendlyFireMember(false);
//                yclans_clans.get(clan_player).setFriendlyFireAlly(false);
//                yclans_clans.remove(clan_player);
//            }
//        }

        PlayerLoseEvent lose = new PlayerLoseEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), getType());
        Bukkit.getPluginManager().callEvent(lose);

        // Se os itens forem setados, então limpe o inventário do jogador.
        if(defined_items) {
            p.getInventory().clear();
            p.getInventory().setHelmet(null);
            p.getInventory().setChestplate(null);
            p.getInventory().setLeggings(null);
            p.getInventory().setBoots(null);
        }

        this.remove(p);
    }

    @Override
    public void winner(Player p) {

        if(this.ended) return;
        this.ended = true;

        // Mande a mensagem de vitória.
        List<String> broadcast_messages = config.getStringList("Messages.Winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@winner", p.getName()).replace("@name", config.getString("Evento.Title"))));
        }

        // Adicionar vitória e dar a tag no LegendChat.
        this.setWinner(p);

        // Encerre o evento.
        this.stop();

        // Execute todos os comandos de vitória.
        List<String> commands = config.getStringList("Rewards.Commands");
        for(String s : commands) {
            executeConsoleCommand(p, s.replace("@winner", p.getName()));
        }

    }

    @Override
    public void stop() {

        // Coloque a borda no seu lugar.
        if(border_enabled) border.setSize(border_size, 0);

        // Remova os blocos colocados pelos jogadores.
        for(Block block: blocks_to_remove) {
            block.setType(Material.AIR);
        }
        blocks_to_remove.clear();

        // Restaure os baús.
        for(Block block: chests.keySet()) {

            Chest chest = (Chest) block.getState();
            Map<Integer, ItemStack> items = chests.get(block);

            for(int pos: items.keySet()) {
                chest.getInventory().setItem(pos, items.get(pos));
            }

        }
        chests.clear();

        // Desative o friendly-fire dos jogadores.
        for (ClanPlayer p : simpleclans_clans) {
            p.setFriendlyFire(false);
        }

//        for(MPlayer p: massivefactions_factions.keySet()) {
//            p.getFaction().setFlag(MFlag.ID_FRIENDLYFIRE, false);
//        }
//
//        for(yclans.model.ClanPlayer p: yclans_clans.keySet()) {
//            p.getClan().setFriendlyFireMember(false);
//            p.getClan().setFriendlyFireAlly(false);
//        }

        simpleclans_clans.clear();
//        massivefactions_factions.clear();
//        yclans_clans.clear();

        // Se o evento for de itens setados, limpe o inventário dos jogadores.
        if(defined_items) {
            for(Player p: getPlayers()) {
                p.getInventory().clear();
                p.getInventory().setHelmet(null);
                p.getInventory().setChestplate(null);
                p.getInventory().setLeggings(null);
                p.getInventory().setBoots(null);
            }
        }

        // Remova o listener do evento e chame a função cancel.
        HandlerList.unregisterAll(listener);
        this.removePlayers();
    }

    public void eliminate(Player p) {
        p.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Eliminated").replace("&", "§")));
        remove(p);
        notifyLeave(p);
        PlayerLoseEvent lose = new PlayerLoseEvent(p, getConfig().getString("filename").substring(0, getConfig().getString("filename").length() - 4), getType());
        Bukkit.getPluginManager().callEvent(lose);
    }

    public boolean isPvPEnabled() { return this.pvp_enabled; }
    public boolean removePlayerPlacedBlocks() { return this.remove_blocks; }
    public List<Block> getBlocksToRemove() { return this.blocks_to_remove; }

//    private int getClanMembers(Player p) {
//
//        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("massivefactions")) {
//            return (int) massivefactions_factions.keySet()
//                    .stream()
//                    .filter(map -> map.getFaction() == MPlayer.get(p).getFaction())
//                    .count();
//        }
//
//        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("yclans")) {
//            return (int) yclans_clans.keySet()
//                    .stream()
//                    .filter(map -> map.getClan() == yclans_api.getPlayer(p).getClan())
//                    .count();
//        }
//
//        return -1;
//    }

}
