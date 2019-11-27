package ru.mtuci.model;

import ru.mtuci.websocket.PlayerChoice;
import ru.mtuci.websocket.Result;

/**
 * Результат игры
 * <p>
 * Project: rps-game
 */
public class GameResult {

  private final Player player;
  private final PlayerChoice opponentChoice;
  private final Result result;

  public GameResult(Player player, PlayerChoice opponentChoice, Result result) {
    this.player = player;
    this.opponentChoice = opponentChoice;
    this.result = result;
  }

  public Player getPlayer() {
    return player;
  }

  public PlayerChoice getOpponentChoice() {
    return opponentChoice;
  }

  public Result getResult() {
    return result;
  }
}