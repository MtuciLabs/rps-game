"use strict";

let socket;
let chatInput;
let urlInput;
let currentChoice;
let playerId;

window.onload = function init() {
  //инициализация
  urlInput = document.querySelector('#url');
  chatInput = document.querySelector('#chat-input > input');

  //установка обработчиков
  urlInput.onfocus = function (event) {
    let r = document.createRange();
    r.selectNode(event.target);
    window.getSelection().addRange(r);
  };

  chatInput.onkeydown = function (event) {
    if (event.keyCode === 13) {
      clickSend();
    }
  };

  document.querySelector('#chat-input > span').onclick = clickSend;
  document.querySelector('#select-box').onclick = clickImage;

  connectToGame().then(createWebSocketConnection)
};

async function connectToGame() {
  let gameId = location.hash.replace('#', '');

  if (await isExistGame(gameId)) {
    return gameId;
  }

  return await createNewGame();
}

async function isExistGame(gameId) {
  if (gameId !== "") {
    let response = await fetch(
        "http://" + location.host + "/connection/" + gameId);

    return response.status === 200;
  }
  return false;
}

async function createNewGame() {
  let response = await fetch(
      "http://" + location.host + "/connection");

  let json = await response.json();
  window.location.hash = json.gameId;
  urlInput.value = window.location.href;
  return json.gameId;
}

function createWebSocketConnection(gameId) {
  let url = "ws://" + location.host + "/game/" + gameId;
  socket = new WebSocket(url);
  socket.onclose = onClose;
  socket.onerror = onError;
  socket.onmessage = onMessage;
}

//============================================
//    Методы жизненного цикла WebSocket'а
//============================================

// вызывается при закрытии WebSocket сессии
function onClose(event) {
  if (event.wasClean) {
    console.log('Соединение закрыто чисто');
  } else {
    console.log('Обрыв соединения'); // например, "убит" процесс сервера
  }
  console.log('Код: ' + event.code + ' причина: ' + event.reason);

  if (event.reason) {
    showCover('You were inactive for too long.');
  } else {
    showCover('Your opponent is disconnected');
  }
}

// вызывается при каких-либо ошибках в WebSocket сессии
function onError(error) {
  console.log("Ошибка " + error.message);
}

// вызывается, когда приходят сообщения
function onMessage(event) {
  console.log('onMessage: ' + event.data.toString());

  let incomingMessage = JSON.parse(event.data);
  switch (incomingMessage.type) {
    case 'MESSAGE':
      showMessage(incomingMessage.message, false);
      break;
    case 'RESULT':
      showResult(incomingMessage);
      break;
    case 'CONNECTION':
      playerId = incomingMessage.id;
      showConnection(incomingMessage.connection);
      break;
  }
}

//===========================================
//     Методы для обработки сообщений
//===========================================

function showMessage(message, isYour) {
  let newMessageElem = document.createElement('div');
  newMessageElem.classList.add('message-style');
  newMessageElem.classList.add(isYour ? 'your-message' : 'opp-message');
  newMessageElem.appendChild(document.createTextNode(message));

  let parentElem = document.createElement('div');
  parentElem.classList.add('media');
  parentElem.appendChild(newMessageElem);

  let block = document.getElementById('message-body');
  block.appendChild(parentElem);
  block.scrollTop = block.scrollHeight; //чтобы прокручивалось в конец
}

function showResult(resultObj) {
  let header = document.querySelector('.result-header');

  header.textContent = resultObj.result;

  header.hidden = false;
  document.getElementById('opp-choice-image').src = 'images/'
      + resultObj.playerChoice.toLowerCase() + '.png';

  if (resultObj.result === 'WIN') {
    document.getElementById('your-score').textContent++;
  } else if (resultObj.result === "LOSE") {
    document.getElementById('opp-score').textContent++;
  }

  setTimeout(restartGame, 2000);
}

function restartGame() {
  let children = document.querySelector('#select-box').children;

  toggleHidden(children, 1, 4);
  children[0].firstElementChild.hidden = false;
  children[0].lastElementChild.hidden = true;
  currentChoice.parentNode.hidden = false;

  currentChoice.parentNode.classList.toggle('icon-animate');
  currentChoice.style.cssFloat = '';
  currentChoice.parentNode.style.left = '';

  currentChoice.classList.toggle('icon');
  document.querySelector('#select-box').onclick = clickImage;

  document.getElementById('opp-choice-image').src = 'images/preloader.gif';

  toggleHidden(children, 4, children.length);
}

function toggleHidden(elements, start, end) {
  for (let i = start; i < end; i++) {
    elements[i].hidden = !elements[i].hidden;
  }
}

function showConnection(connection) {
  if (connection) {
    if (!document.getElementById('cover')) {
      document.getElementById('main-box').hidden = false;
      document.getElementById('url-box').hidden = true;
    }
  }
}

//=========================================
//   Методы обработки клиентских событий
//=========================================

// обработка отправки сообщения в чат
function clickSend() {
  let message = chatInput.value.trim();
  if (!message) {
    return;
  }

  //очистить поле ввода
  chatInput.value = '';

  showMessage(message, true);
  sendChatMessage(message);
}

// обработка клика по картинке (камень, ножницы, бумага)
function clickImage(event) {
  let target = event.target;

  let currentTarger = target;
  while (currentTarger.tagName !== 'IMG') {
    if (currentTarger == this) {
      return;
    }
    currentTarger = currentTarger.parentNode;
  }

  currentChoice = target;

  let children = document.querySelector('#select-box').children;

  toggleHidden(children, 1, 4);
  children[0].firstElementChild.hidden = true;
  currentChoice.parentNode.hidden = false;

  currentChoice.parentNode.classList.toggle('icon-animate');
  currentChoice.parentNode.style.left = 0 + 'px';
  currentChoice.style.cssFloat = 'right';

  currentChoice.classList.toggle('icon');
  document.querySelector('#select-box').onclick = null;

  if (currentChoice.parentNode == children[1]) {
    toggleHidden(children, 4, children.length);
  } else {
    setTimeout(toggleHidden, 500, children, 4, children.length);
  }

  setTimeout(sendChoice, 500, target.getAttribute('data-choice'));
}

function sendChoice(choice) {
  let choiceMessage = {
    id: playerId,
    type: "RESULT",
    choice: choice
  };

  socket.send(JSON.stringify(choiceMessage));
}

function sendChatMessage(message) {
  let msgJObj = {
    id: playerId,
    type: "MESSAGE",
    message: message
  };

  try {
    socket.send(JSON.stringify(msgJObj));
  } catch (exp) {
    console.log(exp)
  }
}

// Показать полупрозрачный DIV, затеняющий всю страницу
function showCover(reason) {
  let cover = document.createElement('div');
  cover.id = 'cover';
  cover.classList.add('cover');

  let windowDiv = document.createElement('div');
  windowDiv.classList.add('window');
  windowDiv.textContent = reason;
  cover.appendChild(windowDiv);

  document.body.appendChild(cover);
}
