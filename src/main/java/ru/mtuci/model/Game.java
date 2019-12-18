package ru.mtuci.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранит состояние игры
 * <p>
 * Project rps-game
 */
public class Game {

  public static final int PLAYERS_IN_GAME = 2;

  private final String id;
  /**
   * Мапа игроков
   */
  private final Map<String, Player> players = new ConcurrentHashMap<>(PLAYERS_IN_GAME);

  public Game() {
    this.id = generateGameId();
  }

  public String getId() {
    return id;
  }

  public void addPlayer(Player player) {
    players.put(player.getId(), player);
  }

  public int getPlayersNumber() {
    return players.size();
  }

  public Player getPlayer(String playerId) {
    return players.get(playerId);
  }

  public  List<Player> getPlayers() {
    return new ArrayList<>(players.values());
  }

  public boolean haveChoiceAllPlayers() {
    return players.values().stream()
        .allMatch(player -> player.getChoice() != null);
  }

  public Player getOpponent(String playerId) {
    return players.entrySet().stream()
        .filter(playerEntry -> !playerEntry.getKey().equals(playerId))
        .map(Entry::getValue)
        .findFirst()
        .orElseThrow();
  }

  private String generateGameId() {
    return Long.toHexString(UUID.randomUUID().getMostSignificantBits());
  }

  @Override
  public String toString() {
    return "Game{" +
        "gameId='" + id + '\'' +
        ", players=" + players +
        '}';
  }
}
