//"use strict";

/** +++ +++
 * Добавляет в prototype класса String новый метод, для формирования строки.
 * Т.е. в объект прототип добавлен метод
 */
String.prototype.printf = function() {
  let formatted = this;
  for (let arg in arguments) {
    formatted = formatted.replace("{" + arg + "}", arguments[arg]);
  }
  return formatted;
};

//--------------------------------------------------------------//
/** +++ +++
 * Из cookie возвращает в виде String значение переданного параметра. Undefinded - если такого параметра нет. Не найден.
 *
 */
function getCookie(name) {
  let matches = document.cookie.match(new RegExp("(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"));
  return matches ? decodeURIComponent(matches[1]) : undefined;
}

/** +++ +++
 * Отправляет полученный сформированный запрос и возвращает ответ в виде String
 *
 */
function sendQuery(theUrl) {
  let xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", theUrl, false);
  xmlHttp.send(null);
  return xmlHttp.responseText;
}
//fetch - почему-то не работает/не получилось. На сервер приходит запрос верный, а вот ответа нет. То ли нужно указать прямо GET толи что-то еще.

//--------------------------------------------------------------//

/** +++ +++
 * Выполняет выход и производит перевод на страницу логина.
 *
 */
function unAuthorized() {
  let loginMemeber = getCookie("LoginMemeber");
  let theUrl = "/ChatOnServlet/chat/logout/?mode=UnLogin&LoginMemeber={0}".printf(loginMemeber);
  let resultOfRequest = sendQuery(theUrl);

  let textForLogger = ">>> loginMemeber = {0} | resultOfRequest = {1} >>>".printf(loginMemeber, resultOfRequest);
  console.warn(textForLogger);

  window.location.replace(resultOfRequest);
}


//------------------------------------------------Работа с пользователями-----------------------------------------------------------------------------//
var ulElementThatContainsRecipientsList = "spaceForRecipients";
var classForMarkCurrentRecipient = "currentElementIsTarget";
var cssSelectorTogetNameOfCurrentInterlocutor = "p > a.{0}".printf(classForMarkCurrentRecipient);

/**+++ +++
 * 
 * Добавляет список пользователей на страницу.
 * Используется при загрузке страницы. См. <body onload="getListOfUsers()">
 */
function getListOfUsers() {
  let loginMemeber = getCookie("LoginMemeber");
  let theUrl = "/ChatOnServlet/chat/getusers/?Direct=get&LoginMemeber={0}".printf(loginMemeber);
  let resultOfRequest = sendQuery(theUrl);

  console.log(">>>" + resultOfRequest + ">>> getListOfUsers()");
  let parsedJsonFromReceivedString = JSON.parse(resultOfRequest);
  for (let index = 0; index < parsedJsonFromReceivedString.length; index++) {
    addOneUserToPage(parsedJsonFromReceivedString[index]);
  }
}

//window.onload = getListOfUsers();//не подходит, так как выполняется в том числе когда загрузится скрипт, а на этот момент еще сама страница не загружена и в методе addOneUserToPage() >  isPresentRowForRecipient(nameOfUser) > document.getElementById(liElementThatContainsRecipientsList) дает ошибку.


/** +++ +++
 * Добавляет на страницу пользователей, предварительно проверяте, есть ли уже такой пользователь на странице. Если на странице такой пользователь уже есть то добавление не происходит.
 *
 */

function addOneUserToPage(nameOfUser) {
  let ulElemenForUser = document.getElementById(ulElementThatContainsRecipientsList);
  let pElement = document.createElement("p");
  let imgElement = document.createElement("img");
  let aElement = document.createElement("a");
  if (!isPresentRowForRecipient(nameOfUser)) {
    aElement.href = "#";
    aElement.name = nameOfUser;
    aElement.innerHTML = nameOfUser;

    imgElement.src = "/ChatOnServlet/images/bandmember.jpg";
    imgElement.alt = "Ava";
    aElement.addEventListener("click", doCurrentRecipient, false); //???
    pElement.appendChild(imgElement);
    pElement.appendChild(aElement);
    ulElemenForUser.appendChild(pElement);
  } else {
    console.log(">>>" + "Такой пользователь в на странице уже есть! Добавлять не будем!" + ">>>");
  }
}

/** +++ +++
 * Проверяет присутствует ли пользователь на странице.
 *
 */
function isPresentRowForRecipient(nameOfUser) {
  let ulElemenForUser = document.getElementById(ulElementThatContainsRecipientsList);
  let aElements = ulElemenForUser.getElementsByTagName("a");
  for (let index = 0; index < aElements.length; index++) {
    let nameAttribute = aElements[index].getAttribute("name");
    if (nameAttribute.trim() == nameOfUser.trim()) { //А может и не нужно делать trim
      return true;
    }
  }
  return false;
}

/*
 *
 */

function changeTitleOfMessegeHeader(nameOfUser) {
  let titleElement = document.getElementById("title_for_message");
  titleElement.innerHTML = "Дилаог с "
  let memberElement = document.createElement("font");
  memberElement.size = "3";
  memberElement.style.fontStyle = "italic";
  memberElement.innerHTML = nameOfUser;
  titleElement.appendChild(memberElement);
}

/** +++ +++
 * По клику делает получателя/собеседника текущим/активным.
 *
 */
function doCurrentRecipient(event) {
  let nameElements = getElementsMarkedAsCurrentRecipient();
  let nameOfCurrenUser = getNameOfCurrentInterlocutor();

  let targetElement = event.target;
  let targetName = targetElement.getAttribute("name");
  if (!(nameElements) || !(nameOfCurrenUser)) {
    console.log("Не найден пользователь, что был выбран ранее! Делаем вновь-выбранного - текущим!");
    targetElement.setAttribute("class", classForMarkCurrentRecipient);
    targetElement.style.color = 'blue';
    clearMessages();
    changeTitleOfMessegeHeader(targetName);
  } else if (nameOfCurrenUser && targetName && (new String(nameOfCurrenUser).valueOf() == new String(targetName).valueOf())) {
    console.log("Выбранный пользователь уже является текущим!");
    return;
  } else {
    for (let index = 0; index < nameElements.length; index++) {
      nameElements[index].classList.remove(classForMarkCurrentRecipient);
      nameElements[index].style.color = 'black';
    }
    targetElement.setAttribute("class", classForMarkCurrentRecipient);
    targetElement.style.color = 'blue';
    changeTitleOfMessegeHeader(targetName);
    clearMessages();
  }

  let date = new Date();
  let timeFromInMillisec = date.getTime();
  getMessagesFromServer("old", timeFromInMillisec);
}

/** +++ +++
 * Из полученного HTML элемента выдергивает имя получателя/собеседника и возвращает в виде строки.
 *
 */
//TODO: Нужно принудительно ставить у первого элемента значение текущий, иначе падаем с ошибокой.
function getNameOfCurrentInterlocutor() {
  let firstElement = getElementsMarkedAsCurrentRecipient()[0];
  let nameOfCurrentUser = undefined;
  if (firstElement) {
    nameOfCurrentUser = firstElement.getAttribute("name");
  }
  return nameOfCurrentUser;
}

/** +++ +++
 * Возвращает набор HTML элементов что выделен как текущий (выделенный) получатель/собеседник.
 * Возвращает иначе пустой массив.
 */
function getElementsMarkedAsCurrentRecipient() {
  let ulElemenForRecipient = document.getElementById(ulElementThatContainsRecipientsList);
  let nameElements = ulElemenForRecipient.querySelectorAll(cssSelectorTogetNameOfCurrentInterlocutor);
  //TODO: Нужно продумать, что если покаким-то причинам будет возвращено более двух элементов. Вероятно нужно сбрасывать для остальных этот признак, а потом только взвращать.
  //TODO: Возможно стоит исключать такое в соответствующему методе при выставлении текущего пользователя.
  return nameElements;
}

//------------------------------------------------Работа с сообщениями-----------------------------------------------------------------------------//
var messagesSectionID = "spaceForMessage";

/* +++ +++
 * Удаляет сообщения! В основном используется при переключении пользователей. Перед тем как добавлять новые сообщения.
 */

function clearMessages() {
  let sectinNode = document.getElementById(messagesSectionID);
  let emptyCopySectionNode = sectinNode.cloneNode(false);
  let parentOfSection = sectinNode.parentNode;
  sectinNode.remove();
  parentOfSection.appendChild(emptyCopySectionNode);
}

/** +++ +++
 * Добавляет сообщение на страницу переписки.
 *
 */
function addMessageToPage(fromUser, textOfMessage, date, isFile = false) {
  let textForMessage = ''
  let fileUUID = ''
  if (isFile) {
    let result = textOfMessage.split("##");
    textForMessage = result[0];
    fileUUID = result[1];
  } else {
    textForMessage = textOfMessage;
  }

  let nameFromCookie = getCookie("LoginMemeber");
  let sectionElementForMessages = document.getElementById(messagesSectionID);
  let articleElement = document.createElement("article");
  let divUser = document.createElement("div");
  let divStuructureOfMessage = document.createElement("div");
  let imgOfUser = document.createElement("img");
  let pElement = document.createElement("p");
  let brElement = document.createElement("br");
  let spanElement = document.createElement("span");

  spanElement.innerHTML = date;
  spanElement.setAttribute("class", "message_date_time");
  spanElement.setAttribute("id", date);

  if (isFile) {
    let aElement = document.createElement("a");
    aElement.innerHTML = textForMessage;
    aElement.href = "/ChatOnServlet/chat/getFile/?Direct=get&LoginMemeber={0}&FileUUD={1}&FileName={2}".printf(nameFromCookie, fileUUID, textForMessage);
    pElement.appendChild(aElement);
  } else {
    pElement.innerHTML = textForMessage;
  }

  pElement.appendChild(brElement);
  pElement.appendChild(spanElement);

  imgOfUser.setAttribute("src", "/ChatOnServlet/images/bandmember.jpg");
  imgOfUser.setAttribute("alt", "Avatar");


  divStuructureOfMessage.setAttribute("class", "structure_of_message");
  divStuructureOfMessage.appendChild(imgOfUser);
  divStuructureOfMessage.appendChild(pElement);

  if (new String(fromUser).valueOf() == new String(nameFromCookie).valueOf()) {
    divUser.classList.add("current_user");
    articleElement.classList.add("current_user");
  } else {
    divUser.classList.add("member");
    articleElement.classList.add("member");
  }
  divUser.appendChild(divStuructureOfMessage);
  articleElement.appendChild(divUser);
  sectionElementForMessages.appendChild(articleElement);
}


/** +++ +++
 * Получает сообщение с сервера.
 *
 */
function getMessagesFromServer(typeHistoricity, timeFoQueryInMillisecond) {
  let loginMemeber = getCookie("LoginMemeber");
  let nameOfCurrentInterlocutor = getNameOfCurrentInterlocutor();
  if (nameOfCurrentInterlocutor == undefined) {
    return undefined; //TODO: Вероятно тут лучше возбуждать ислючение. Или отображать, что не выделен собеседник.
  }
  let theUrl = "/ChatOnServlet/chat/messaging/?Direct=get&LoginMemeber={0}&Interlocutor={1}&timeMillisec={2}&historical={3}".printf(loginMemeber, nameOfCurrentInterlocutor, timeFoQueryInMillisecond, typeHistoricity);
  let resultOfRequest = sendQuery(theUrl);

  let resultFromRequestAsJSON = JSON.parse(resultOfRequest);

  for (let i in resultFromRequestAsJSON) {
    let pieceOfJson = resultFromRequestAsJSON[i];
    let isFile = (pieceOfJson["isfile"] == "true") ? true : false;
    addMessageToPage(pieceOfJson["author"], pieceOfJson["message"], pieceOfJson["messagedate"], isFile);
  }
}

//----------------------------Работа с отправкой сообщения

/** +++ +++
 * Отправляет сообщение на сервер.
 *
 */
function sendMessageOnServer() {
  let loginMemeber = getCookie("LoginMemeber");
  let nameOfCurrentInterlocutor = getNameOfCurrentInterlocutor();
  let textOfMessage = getMessageFromInputField();
  let textOfMessageWithDeletedSpace = new String(textOfMessage).valueOf().replace(/\s+/g, '');
  if (nameOfCurrentInterlocutor == undefined) {
    alert("Пожалуйста выберите собеседника для отправки сообщения!");
    //TODO: Нужно блокировать кнопку до момента выбора пользователя и при отсутствии текста для ввода.
    return;

  } else if (textOfMessage == undefined || textOfMessageWithDeletedSpace == new String('').valueOf()) {
    alert("Пожалуйста введите текст сообщения!");
    return;
  } else {
    let date = new Date();
    let millisec = date.getTime();

    clearMessageInputField();
    let theUrl = "/ChatOnServlet/chat/messaging/?Direct=send&LoginMemeber={0}&Interlocutor={1}&textMessage={2}&timeMillisec={3}".printf(loginMemeber, nameOfCurrentInterlocutor, textOfMessage, millisec);
    let resultOfRequest = sendQuery(theUrl);
    console.log(resultOfRequest);
  }
  let dateAsString = "{0}-{1}-{2} {3}:{4}:{5}.{6}".printf(date.getFullYear(), date.getMonth() + 1, date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds(), date.getMilliseconds());
  addMessageToPage(loginMemeber, textOfMessage, dateAsString);
  let divv = document.getElementsByClassName("message_container")[0];
  divv.scrollTop = divv.scrollHeight;
}


/** +++ +++
 * Получает и возвращает возвращает сообщение из поля ввода для отправки сообщения.
 *
 */
function getMessageFromInputField() {
  let textForSend = document.getElementById("messageTextField").value;
  return textForSend;
}


/* +++ +++
 * Очищает поле ввода.
 */

function clearMessageInputField() {
  document.getElementById("messageTextField").value = '';
}

/* +++ +++
 * Получаем временные диапазоны для первого и последнего сообщения.
 *
 */

function getBoundedTimeForRequest() {
  let sectionForMessages = document.getElementById("spaceForMessage");
  let firstLiElement = document.querySelector("article:first-child p>span");
  let lastLiElement = document.querySelector("article:last-child p>span");
  if (!(firstLiElement) || !(lastLiElement)) {
    return undefined;
  }
  let firstTime = firstLiElement.id;
  let lastTime = lastLiElement.id;
  let firstTimeAsMillisec = Date.parse(firstTime);
  let secondTimeAsMillisec = Date.parse(lastTime);
  return [firstTimeAsMillisec, secondTimeAsMillisec];
}

/* +++ +++
 * Получает каждую секунду новые сообщения.
 */

function getNewMessgaeFromServer() {
  let boundedMessage = getBoundedTimeForRequest();
  if (!boundedMessage) {
    console.info("Не смогли получить данные по имеющимся сообщениям, для определения времени первого отображенного сообщения и последнего сообщения");
    return;
  }
  let timeFromInMillisec = boundedMessage[1];
  getMessagesFromServer("new", timeFromInMillisec);

  printPosition();
}

var interval = setInterval(getNewMessgaeFromServer, 1500);


/* +++ +++
 * Работа с оскроллом. Промативает сообщение до конца вниз. Если скролл в области 100px от низа.
 * В ином случае, если скролл выше чем 100px от низа, скролл пролистываться не буедт.
 */
function printPosition() {
  let divv = document.getElementsByClassName("message_container")[0];

  let divWidth = divv.offsetWidth;
  let divHeight = divv.offsetHeight;
  let divParam = "divWidth = {0} divHeight = {1}".printf(divWidth, divHeight);
  console.log(divParam);

  let divScrollTop = divv.scrollTop;
  let divScrollHeight = divv.scrollHeight; //Именно оно работает, так как именно у него есть скролл. Почему-то я делал не правильно, думаю что у section есть scroll.
  divParam = "scrollTop = {0} scrollHeight = {1}".printf(divScrollTop, divScrollHeight);
  console.log(divParam);

  if ((divScrollHeight - divHeight < divScrollTop + 100) && (divScrollHeight - divHeight > divScrollTop - 100)) {
    divv.scrollTop = divv.scrollHeight;
    console.log("true");
  }

}

function forWorkWithFile() {
  let selectedFile = document.getElementById("ChooseFile").files;
  Array.prototype.forEach.call(selectedFile, (file) => { console.log("Name ={0}, Size={1}".printf(file.name, file.size)) });
  console.log("-----------------------");
}

//var interval1 = setInterval(forWorkWithFile, 2500);


function handleFiles(files) {
  Array.prototype.forEach.call(files, (file) => { console.log("Name = {0}, Size = {1}".printf(file.name, file.size)) });
  console.log("-----------------------");
  let lable = document.getElementById("LableChooseFile");
  if (files.length > 0) {
    lable.innerHTML = "({0})".printf(files.length);
  } else {
    lable.innerHTML = "";
  }

}

function sendSelectedFilesXHR() {
  let selectedFile = document.getElementById("ChooseFile").files;

  let formData = new FormData();

  for (var i = 0; i < selectedFile.length; i++) {

    console.log(">>>>> Name = {0}, Size = {1}".printf(selectedFile[i].name, selectedFile[i].size));
    let str = "ChooseFile{0}".printf(i);
    formData.append(str, selectedFile[i]);
  }

  let date = new Date();
  let timeFromInMillisec = date.getTime();
  let user = { author: 'first@yandex.ru', recipient: 'second@yandex.ru', timeInMillisec: timeFromInMillisec };
  formData.append("user", JSON.stringify(user));

  let xhr = new XMLHttpRequest();

  xhr.onreadystatechange = (state) => { console.log('{0}  {1}'.printf("--------", xhr.status)); }
  xhr.upload.onload = () => { console.log('Данные полностью загружены на сервер!') };
  xhr.onprogress = (event) => { console.log('Получено с сервера ' + event.loaded + ' байт из ' + event.total) };
  xhr.upload.onprogress = (event) => { console.log('Загружено на сервер ' + event.loaded + ' байт из ' + event.total) };

  xhr.open("POST", '/ChatOnServlet/chat/messaging/');
  xhr.send(formData);

  //TODO: После отправки нужно очищать кол. выбранных файлов у кнопки, да и сбросить выбранные файлы.
  //TODO: Нужно еще проверить, выбран ли пользователь.
}


async function sendSelectedFilesFetch() {
  //Работа с пользователем.

  let loginMemeber = getCookie("LoginMemeber");
  let nameOfCurrentInterlocutor = getNameOfCurrentInterlocutor();
  if (nameOfCurrentInterlocutor == undefined) {
    alert("Пожалуйста выберите собеседника для отправки сообщения!");
    //TODO: Нужно блокировать кнопку до момента выбора пользователя и при отсутствии текста для ввода.
    return;

  }

  //Работаем с файлом
  let selectedFile = document.getElementById("ChooseFile").files;
  if (selectedFile == undefined || selectedFile.length < 1) {
    alert("Пожалуйста выберите файл для отправки!");
    return;
  }

  let formData = new FormData();

  for (var i = 0; i < selectedFile.length; i++) {

    console.log(">>>> Name = {0}, Size = {1}".printf(selectedFile[i].name, selectedFile[i].size));
    let str = "ChooseFile{0}".printf(i); //строка для part в POST запросе.
    formData.append(str, selectedFile[i]);
  }

  let date = new Date();
  let timeFromInMillisec = date.getTime();
  let user = { author: loginMemeber, ricipient: nameOfCurrentInterlocutor, timeMillisec: timeFromInMillisec };
  formData.append("InfoAboutUsers", JSON.stringify(user));

  try {
    let r = await fetch('/ChatOnServlet/chat/messaging/', { method: "POST", body: formData });
    console.log('HTTP response code:', r.status);
  } catch (e) {
    console.log('Huston we have problem...:', e);
  }
  document.getElementById("ChooseFile").value = null;
  let lable = document.getElementById("LableChooseFile");
  lable.innerHTML = "";
}