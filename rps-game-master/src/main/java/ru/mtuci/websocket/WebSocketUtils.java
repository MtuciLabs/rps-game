package ru.mtuci.websocket;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Класс реализующий методы отправки JSON сообщений через веб-сокет.
 */
public final class WebSocketUtils {

  public static final Logger log = LoggerFactory.getLogger(WebSocketUtils.class);

  public static void sendConnectionMessage(WebSocketSession session, String playerId) {
    try {
      String connectionMessage = new JSONObject()
          .put("id", playerId)
          .put("type", Type.CONNECTION.toString())
          .put("connection", true)
          .toString();

      if (session.isOpen()) {
        session.sendMessage(new TextMessage(connectionMessage));
      }
    } catch (Exception e) {
      log.error("Ошибка при отправке сообщения через web-socket.", e);
    }
  }



  public static void sendResultMessage(WebSocketSession session, String playerId, Result result,
      PlayerChoice playerChoice) {
    try {
      String resultMessage = new JSONObject()
          .put("id", playerId)
          .put("type", Type.RESULT.toString())
          .put("result", result.toString())
          .put("playerChoice", playerChoice.toString())
          .toString();

      if (session.isOpen()) {
        session.sendMessage(new TextMessage(resultMessage));
      }
    } catch (Exception e) {
      log.error("Ошибка при отправке сообщения через web-socket.", e);
    }
  }

  public static void sendStatusMessage(WebSocketSession session) {
    try {
      String statusMessage = new JSONObject()
              .put("type", Type.STATUS.toString())
              .toString();

      if (session.isOpen()) {
        session.sendMessage(new TextMessage(statusMessage));
      }
    } catch (Exception e) {
      log.error("Ошибка при отправке сообщения через web-socket.", e);
    }
  }

  public static void sendChatMessage(WebSocketSession session, String textMessage) {
    try {
      if (session.isOpen()) {
        session.sendMessage(new TextMessage(textMessage)); //HtmlUtils.htmlEscape(message)
      }
    } catch (Exception e) {
      log.error("Ошибка при отправке сообщения через web-socket.", e);
    }
  }
}
