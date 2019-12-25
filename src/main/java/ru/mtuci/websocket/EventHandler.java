package ru.mtuci.websocket;

import static ru.mtuci.websocket.WebSocketConfig.Consts.GAME_ID_ATTRIBUTE;

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.boot.web.servlet.server.Session;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.mtuci.model.Game;
import ru.mtuci.model.GameResult;
import ru.mtuci.model.Player;
import ru.mtuci.service.GameService;

/**
 * Класс реализующий обработку событий веб-сокет сессии. В этом классе выполняется
 * обработка всех сообщений от клиета отправленных через веб-сокет. А также отправка
 * веб-сокет сообщений.
 */
@Component
public class EventHandler extends TextWebSocketHandler {

  private static final Logger log = LoggerFactory.getLogger(EventHandler.class);

  /**
   * Инкапсулирует игровую бизнес локику
   */
  private GameService gameService;

  public EventHandler(GameService gameService) {
    this.gameService = gameService;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    log.info("Socket Connected");
    String gameId = getGameId(session);
    Player newPlayer = new Player(session);
    gameService.addPlayer(gameId, newPlayer);

    if (gameService.isReadyStartGame(gameId)) {
      for (Player player : gameService.getGame(gameId).getPlayers()) {
        WebSocketUtils.sendConnectionMessage(player.getSession(), player.getId());
      }
    }
  }

  @Override

  public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
    log.info("Socket Closed: [{}] {}", closeStatus.getCode(), closeStatus.getReason());

    String gameId = getGameId(session);
    Game finishedGame = gameService.remove(gameId);
    if (finishedGame != null) {
      for (Player player : finishedGame.getPlayers()) {
        player.getSession().close();
      }
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    log.info("Message received: {}", message.getPayload());
    try {
      String gameId = getGameId(session);
      JSONObject jsonMessage = new JSONObject(message.getPayload());

      Type type = Type.valueOf(jsonMessage.getString("type"));
      //TODO добавьте обработку сообщений из чата
      if (type == Type.RESULT) {
        handleResultMessage(gameId, jsonMessage);
        Game game = gameService.getGame(gameId);
        String currentPlayerId = jsonMessage.getString("id");
        Player currentPlayer = game.getOpponent(currentPlayerId);
        WebSocketSession v = currentPlayer.getSession();
        WebSocketUtils.sendStatusMessage(v);
      } if (type == Type.MESSAGE) {
        Game game = gameService.getGame(gameId);
        String currentPlayerId = jsonMessage.getString("id");
        Player opponentPlayer = game.getOpponent(currentPlayerId);
        WebSocketUtils.sendChatMessage(opponentPlayer.getSession(),message.getPayload());
        if (type == Type.STATUS){

        }
      }
    } catch (JSONException e) {
      log.error("Невалидный формат json.", e);
    } catch (IllegalArgumentException e) {
      log.error("Передан несуществующий тип сообщения", e);
    }
  }

  private void handleResultMessage(String gameId, JSONObject jsonMessage) {
    PlayerChoice choice = PlayerChoice.valueOf(jsonMessage.getString("choice"));
    String currentPlayerId = jsonMessage.getString("id");

    Game game = gameService.getGame(gameId);
    Player currentPlayer = game.getPlayer(currentPlayerId);
    currentPlayer.setChoice(choice);

    if (game.haveChoiceAllPlayers()) {
      List<GameResult> gameResults = gameService.play(game);
      for (GameResult result : gameResults) {
        Player player = result.getPlayer();
        WebSocketUtils.sendResultMessage(
            player.getSession(), player.getId(), result.getResult(), result.getOpponentChoice());
        player.setChoice(null);
      }
    }
  }

  private String getGameId(WebSocketSession session) {
    return (String) session.getAttributes().get(GAME_ID_ATTRIBUTE);
  }
}
