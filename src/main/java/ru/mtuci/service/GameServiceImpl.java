package ru.mtuci.service;

import static ru.mtuci.model.Game.PLAYERS_IN_GAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import ru.mtuci.model.Game;
import ru.mtuci.model.GameResult;
import ru.mtuci.model.Player;
import ru.mtuci.websocket.PlayerChoice;
import ru.mtuci.websocket.Result;

/**
 * Реализация игрового сервиса.
 *
 * Project: rps-game
 */
@Service
public class GameServiceImpl implements GameService {

  /**
   * Хранит игровые сессии
   */
  private final Map<String, Game> games = new ConcurrentHashMap<>();

  @Override
  public String createGame() {
    Game game = new Game();
    String gameId = game.getId();
    games.put(gameId, game);
    return gameId;
  }

  @Override
  public void addPlayer(String gameId, Player player) {
    Game game = games.get(gameId);
    game.addPlayer(player);
  }

  @Override
  public boolean hasGame(String gameId) {
    if (gameId != null) {
      return games.containsKey(gameId);
    }
    return false;
  }

  @Override
  public Game getGame(String gameId) {
    return games.get(gameId);
  }

  @Override
  public List<GameResult> play(Game game) {
    List<GameResult> results = new ArrayList<>(PLAYERS_IN_GAME);
    List<Player> players = game.getPlayers();

    Player firstPlayer = players.get(0);
    Player secondPlayer = players.get(1);
    PlayerChoice firstChoice = firstPlayer.getChoice();
    PlayerChoice secondChoice = secondPlayer.getChoice();
    if (firstChoice == secondChoice) {
      results.add(new GameResult(firstPlayer, secondChoice, Result.DRAW));
      results.add(new GameResult(secondPlayer, firstChoice, Result.DRAW));
    } else {
      if ((firstChoice == PlayerChoice.ROCK && secondChoice == PlayerChoice.SCISSORS) ||
          (firstChoice == PlayerChoice.PAPER && secondChoice == PlayerChoice.ROCK) ||
          (firstChoice == PlayerChoice.SCISSORS && secondChoice == PlayerChoice.PAPER)) {
        results.add(new GameResult(firstPlayer, secondChoice, Result.WIN));
        results.add(new GameResult(secondPlayer, firstChoice, Result.LOSE));
      } else {
        results.add(new GameResult(firstPlayer, secondChoice, Result.LOSE));
        results.add(new GameResult(secondPlayer, firstChoice, Result.WIN));
      }
    }
    return results;
  }

  @Override
  public Game remove(String gameId) {
    return games.remove(gameId);
  }

  @Override
  public boolean isReadyStartGame(String gameId) {
    if (gameId != null && games.containsKey(gameId)) {
      return games.get(gameId).getPlayersNumber() == PLAYERS_IN_GAME;
      }
    return false;
  }
}
