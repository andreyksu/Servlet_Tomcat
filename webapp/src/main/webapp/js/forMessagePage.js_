//"use strict";
/** +++
 * Скрывает и добавляет div блок с пользователиями при уменьшении окна.
 *
 */
var expandCollapse = function() {
  if ($(window).width() < 766) {
    $("#hidedElement").css('display', 'none'); //$("#hideElement").hide();
  } else {
    $("#hidedElement").css('display', 'block'); //$("#hideElement").show();
  }
}
$(window).resize(expandCollapse);

/** +++
 * Добавляет в prototype класса String новый метод, для формирования строки.
 *
 */
String.prototype.printf = function() {
  var formatted = this;
  for (var arg in arguments) {
    formatted = formatted.replace("{" + arg + "}", arguments[arg]);
  }
  return formatted;
};

//-----------------------------------------------------------------------------------------------------------------------------//
var liElementThatContainsRecipientsList = "spaceForRecipients";
var liElementThatContainsMessagesList = "spaceForMessage";
var classForMarkCurrentRecipient = "currentElementIsTarget";
var cssSelectorTogetNameOfCurrentInterlocutor = "li > a.{0}".printf(classForMarkCurrentRecipient);

var lowerBoundOfTime = 0;
var upperBoundOfTime = 0;

/** +++
 * Из cookie возвращает в виде String значение переданного параметра. Undefinded - если такого параметра нет. Не найден.
 *
 */
function getCookie(name) {
  var matches = document.cookie.match(new RegExp("(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"));
  return matches ? decodeURIComponent(matches[1]) : undefined;
}

/** +++
 * Отправляет полученный сформированный запрос и возвращает ответ в виде String
 *
 */
function sendQuery(theUrl) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", theUrl, false);
  xmlHttp.send(null);
  return xmlHttp.responseText;
}

/** +++
 * Возвращает HTML элемент что выделен как текущий (выделенный) получатель/собеседник.
 *
 */
function getElementsMarkedAsCurrentRecipient() {
  var ulElemenForRecipient = document.getElementById(liElementThatContainsRecipientsList);
  var nameElements = ulElemenForRecipient.querySelectorAll(cssSelectorTogetNameOfCurrentInterlocutor);
  //TODO: Нужно продумать, что если покаким-то причинам будет возвращено более двух элементов. Вероятно нужно сбрасывать для остальных этот признак, а потом полько взвращать.
  //TODO: Возможно стоит исключать такое в соответствующему методе при выставлении текущего пользователя.
  return nameElements;

}

/** +++
 * Из полученного HTML элемента выдергивает имя получателя/собеседника и возвращает в виде строки.
 *
 */
//TODO: Нужно принудительно ставить у первого элемента значение текущий, иначе падаем с ошибокой.
function getNameOfCurrentInterlocutor() {
  var firstElement = getElementsMarkedAsCurrentRecipient()[0];
  var nameOfCurrentUser = undefined;
  if (firstElement) {
    nameOfCurrentUser = firstElement.getAttribute("name");
  }
  return nameOfCurrentUser;
}

/** +++
 * По клику делает получателя/собеседника текущим/активным.
 *
 */
function doCurrentRecipient(event) {
  var nameElements = getElementsMarkedAsCurrentRecipient();
  var nameOfCurrenUser = getNameOfCurrentInterlocutor();

  var targetElement = event.target;
  var targetName = targetElement.getAttribute("name");
  if (!(nameElements) || !(nameOfCurrenUser)) {
    console.log("Не найден пользователь, что был выбран ранее! Делаем вновь-выбранного - текущим!");
    targetElement.setAttribute("class", classForMarkCurrentRecipient);
    targetElement.style.color = 'blue';
  } else if (nameOfCurrenUser && targetName && new String(nameOfCurrenUser).valueOf() == new String(targetName).valueOf()) {
    console.log("Выбранный пользователь уже является текущим!");
    return;
  } else {
    for (var index = 0; index < nameElements.length; index++) {
      nameElements[index].classList.remove(classForMarkCurrentRecipient);
      nameElements[index].style.color = 'black';
    }
    targetElement.setAttribute("class", classForMarkCurrentRecipient);
    targetElement.style.color = 'blue';
    clearMessages();
  }

  var date = new Date();
  var timeFromInMillisec = date.getTime();
  getMessagesFromServer("old", timeFromInMillisec);
}

/** +++
 * Возвращает сообщение из поля ввода для отправки сообщения.
 *
 */
function getMessageFromInputField() {
  var textForSend = document.getElementById("messageTextField").value;
  return textForSend;
}

function clearMessageInputField() {
  document.getElementById("messageTextField").value = '';
}

function clearMessages() {
  var ulElementForMessages = document.getElementById(liElementThatContainsMessagesList);
  var emptyCopyUlElement = ulElementForMessages.cloneNode(false);
  var parent = ulElementForMessages.parentNode;
  ulElementForMessages.remove();
  parent.appendChild(emptyCopyUlElement);
}

/** -+-
 * Добавляет сообщение на страницу переписки.
 *
 */
function addMessageToPage(fromUser, textOfMessage, date) {
  var nameFromCookie = getCookie("LoginMemeber");
  var ulElementForMessages = document.getElementById(liElementThatContainsMessagesList);
  var liElement = document.createElement("li");
  var aElement = document.createElement("a");
  var brElement = document.createElement("br");
  var subElement = document.createElement("sub");
  subElement.innerHTML=date;
  aElement.href = "#";
  aElement.innerHTML = textOfMessage;
  aElement.id = date;
  aElement.name = fromUser;
  if (new String(fromUser).valueOf() == new String(nameFromCookie).valueOf()) {
    aElement.classList.add("self");
    aElement.setAttribute("align", "left");
  } else {
    aElement.classList.add("interlocutor");
    aElement.setAttribute("align", "right");
  }
  aElement.appendChild(brElement);
  aElement.appendChild(subElement);
  liElement.appendChild(aElement);
  ulElementForMessages.appendChild(liElement);
}

/** +++
 * Добавляет на страницу пользователей, предварительно проверяте, есть ли уже такой пользователь на странице. Если на странице такой пользователь уже есть то добавление не происходит.
 *
 */
function addOneUserToPage(nameOfUser) {
  var ulElemenForUser = document.getElementById(liElementThatContainsRecipientsList);
  var liElement = document.createElement("li");
  var aElement = document.createElement("a");
  var spanForName = document.createElement("span");
  var spanStatus = document.createElement("span");
  var spanForClose = document.createElement("span");
  if (!isPresentRowForRecipient(nameOfUser)) {
    console.log(">>>" + "Такого пользователя в списке нет, будем добавлять на страничку!" + ">>>");
    aElement.href = "#";
    aElement.name = nameOfUser;
    //spanForName.innerHTML = nameOfUser;
    //spanForName.addEventListener("click", doCurrentRecipient, false);
    aElement.innerHTML = nameOfUser;
    aElement.addEventListener("click", doCurrentRecipient, false);
    //aElement.appendChild(spanForName);
    liElement.appendChild(aElement);
    ulElemenForUser.appendChild(liElement);
  } else {
    console.log(">>>" + "Такой пользователь в списке уже есть!" + ">>>");
  }
}

/** +++
 * Проверяет присутствует ли пользователь на странице.
 *
 */
function isPresentRowForRecipient(nameOfUser) {
  var ulElemenForUser = document.getElementById(liElementThatContainsRecipientsList);
  var aElements = ulElemenForUser.getElementsByTagName("a");
  for (var index = 0; index < aElements.length; index++) {
    var nameAttribute = aElements[index].getAttribute("name");
    if (nameAttribute == nameOfUser) {
      return true;
    }
  }
  return false;
}

/** +++
 * Выполняет разавторизацию и производите перевод на страницу логина.
 *
 */
function unAuthorized() {
  var loginMemeber = getCookie("LoginMemeber");
  var theUrl = "/ChatOnServlet/chat/logout/?mode=UnLogin&LoginMemeber={0}".printf(loginMemeber);
  var resultOfRequest = sendQuery(theUrl);

  var textForLogger = ">>> loginMemeber = {0} | resultOfRequest = {1} >>>".printf(loginMemeber, resultOfRequest);
  console.log(textForLogger);

  window.location.replace(resultOfRequest);
}

/** ???
 * Отправляет сообщение на сервер.
 *
 */
function sendMessageOnServer() {
  var loginMemeber = getCookie("LoginMemeber");
  var nameOfCurrentInterlocutor = getNameOfCurrentInterlocutor();
  var textOfMessage = getMessageFromInputField();
  var textOfMessageWithDeletedSpace = new String(textOfMessage).valueOf().replace(/\s+/g, '');
  if (nameOfCurrentInterlocutor == undefined) {
    alert("Пожалуйста выберите собеседника для отправки сообщения!");
    //TODO: Нужно блокировать кнопку до момента выбора пользователя и при отсутствии текста для ввода.
    return;

  } else if (textOfMessage == undefined || textOfMessageWithDeletedSpace == new String('').valueOf()) {
    alert("Пожалуйста введите текст сообщения!");
    return;
  } else {
    var date = new Date();
    var millisec = date.getTime();

    clearMessageInputField();
    var theUrl = "/ChatOnServlet/chat/messaging/?Direct=send&LoginMemeber={0}&Interlocutor={1}&textMessage={2}&timeMillisec={3}".printf(loginMemeber, nameOfCurrentInterlocutor, textOfMessage, millisec);
    var resultOfRequest = sendQuery(theUrl);
  }
  var dateAsString = "{0}-{1}-{2} {3}:{4}:{5}.{6}".printf(date.getFullYear(), date.getMonth() + 1, date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds(), date.getMilliseconds());
  addMessageToPage(loginMemeber, textOfMessage, dateAsString);
}

/** ???
 * Получает сообщение с сервера.
 *
 */
function getMessagesFromServer(typeHistoricity, timeFoQueryInMillisecond) {
  var loginMemeber = getCookie("LoginMemeber");
  var nameOfCurrentInterlocutor = getNameOfCurrentInterlocutor();
  if (nameOfCurrentInterlocutor == undefined) {
    return undefined; //TODO: Вероятно тут лучше возбуждать ислючение. Или отображать, что не выделен собеседник.
  }
  var theUrl = "/ChatOnServlet/chat/messaging/?Direct=get&LoginMemeber={0}&Interlocutor={1}&timeMillisec={2}&historical={3}".printf(loginMemeber, nameOfCurrentInterlocutor, timeFoQueryInMillisecond, typeHistoricity);
  var resultOfRequest = sendQuery(theUrl);

  var resultFromRequestAsJSON = JSON.parse(resultOfRequest);

  for (var i in resultFromRequestAsJSON) {
    var pieceOfJson = resultFromRequestAsJSON[i];
    addMessageToPage(pieceOfJson["author"], pieceOfJson["message"], pieceOfJson["messagedate"]);
  }
}

function getBoundedTimeForRequest() {
  var ulElementForMessages = document.getElementById(liElementThatContainsMessagesList);
  var firstLiElement = ulElementForMessages.querySelector("li:first-child > a");
  var lastLiElement = ulElementForMessages.querySelector("li:last-child > a");
  if (!(firstLiElement) || !(lastLiElement)) {
    return undefined;
  }
  var firstTime = firstLiElement.id;
  var lastTime = lastLiElement.id;
  var firstTimeAsMillisec = Date.parse(firstTime);
  var secondTimeAsMillisec = Date.parse(lastTime);
  return [firstTimeAsMillisec, secondTimeAsMillisec];
}

function getNewMessgaeFromServer() {
  var boundedMessage = getBoundedTimeForRequest();
  if (!boundedMessage) {
    return;
  }
  var timeFromInMillisec = boundedMessage[1];
  getMessagesFromServer("new", timeFromInMillisec);
}

var interval = setInterval(getNewMessgaeFromServer, 1000);

/**+++
 * 
 * Добавляет список пользователей на страницу.
 * Используется при загрузке страницы. См. <body onload="getListOfUsers()">
 */
function getListOfUsers() {
  var loginMemeber = getCookie("LoginMemeber");
  var theUrl = "/ChatOnServlet/chat/getusers/?Direct=get&LoginMemeber={0}".printf(loginMemeber);
  var resultOfRequest = sendQuery(theUrl);

  console.log(">>>" + resultOfRequest + ">>> getListOfUsers()");
  var parsedJsonFromReceivedString = JSON.parse(resultOfRequest);
  for (var index = 0; index < parsedJsonFromReceivedString.length; index++) {
    addOneUserToPage(parsedJsonFromReceivedString[index]);
  }
}

//window.onload = getListOfUsers();//не подходит, так как выполняется в том числе когда загрузится скрипт, а на этот момент еще сама страница не загружена и в методе addOneUserToPage() >  isPresentRowForRecipient(nameOfUser) > document.getElementById(liElementThatContainsRecipientsList) дает ошибку.