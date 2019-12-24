package ru.mtuci.controller;


import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.service.GameService;

/**
 * Контроллер используется для создания игровой сессии. HTTP запросы обрабатываются контроллером.
 */
@RestController
@RequestMapping("/connection")
public class ConnectionController {

  private static final Logger log = LoggerFactory.getLogger(ConnectionController.class);

  private final GameService gameService;

  public ConnectionController(GameService gameService) {
    this.gameService = gameService;
  }

  /**
   * Создает новую игровую сессию и возвращает ее идентификатор.
   *
   * @return Возвращает JSON строку, содержащую идентификатор игры.
   */
  @GetMapping
  public ResponseEntity<String> connect() {
    log.info("New connection");
      String newGameId = gameService.createGame();
      return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(new JSONObject().put("gameId", newGameId).toString());
  }

  /**
   * Проверяет существует ли игровая сессия с принятым <code>gameId</code>.
   *
   * @return возвращается статус 200 ОК если игровая сессия существует, иначе 404 NOT FOUND.
   */
  @GetMapping("{gameId}")
  public ResponseEntity<String> connect(@PathVariable("gameId") String gameId) {
    log.info("Connection by gameId={}", gameId);
    //TODO Что будет если 3ий игelрок захочет подключиться?
    if (gameService.hasGame(gameId) && !gameService.isReadyStartGame(gameId)) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
  }
}
