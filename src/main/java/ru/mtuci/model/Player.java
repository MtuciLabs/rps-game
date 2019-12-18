package ru.mtuci.model;

import java.util.Objects;
import java.util.UUID;
import org.springframework.web.socket.WebSocketSession;
import ru.mtuci.websocket.PlayerChoice;

/**
 * Класс хранит свойства игрока, которые необходимы для данного приложения.
 * <p>
 * Project: rps-game
 */
public class Player {

  private final String id;
  private final WebSocketSession session;
  //выбор игрока (камень, ножницы или бумага)
  private PlayerChoice choice;
  //счет за несколько игр
  private int score = 0;

  public Player(WebSocketSession session) {
    this.id = generatePlayerId();
    this.session = session;
  }

  //=========================================
  //=               Methods                 =
  //=========================================
  private String generatePlayerId() {
    return UUID.randomUUID().toString();

  }

  public int incrementScore() {
    return score++;
  }

  //==========================================
  //=            Getter & Setter             =
  //==========================================

  public String getId() {
    return id;
  }

  public WebSocketSession getSession() {
    return session;
  }

  public int incrementAndGetScore() {
    return score++;
  }

  public int getScore() {
    return score;
  }

  public PlayerChoice getChoice() {
    return choice;
  }

  public void setChoice(PlayerChoice choice) {
    this.choice = choice;
  }

  //=============================================
  //=        equals, hashcode, toString         =
  //=============================================
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Player)) {
      return false;
    }
    Player player = (Player) o;
    return score == player.score &&
        Objects.equals(id, player.id) &&
        Objects.equals(session, player.session) &&
        choice == player.choice;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, session, choice, score);
  }

  @Override
  public String toString() {
    return "Player{" +
        "id='" + id + '\'' +
        ", choice=" + choice +
        ", score=" + score +
        '}';
  }
}

