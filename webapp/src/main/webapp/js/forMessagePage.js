/** +++
* Скрывает и добавляет div блок с пользователиями при уменьшении окна.
*
*/
var expandCollapse = function(){
	if ($(window).width() < 766) {
		$("#hidedElement").css('display', 'none');//$("#hideElement").hide();
	}
	else {
		$("#hidedElement").css('display', 'block');//$("#hideElement").show();
	}
}
$(window).resize(expandCollapse);

/** +++
* Добавляет в класс String новый метод, для формирования строки.
*
*/
String.prototype.printf = function(){
	var formatted = this;
	for( var arg in arguments ) {
		formatted = formatted.replace("{" + arg + "}", arguments[arg]);
	}
	return formatted;
};

//-----------------------------------------------------------------------------------------------------------------------------//
var liElementThatContainsRecipientsList = "spaceForRecipients";
var classForMarkCurrentRecipient = "currentElementIsTarget";
var cssSelectorTogetNameOfCurrentRecipient = "li > a.{0}".printf(classForMarkCurrentRecipient);

/** +++
* Из cookie возвращает в виде String значение переданного параметра. Undefinded - если такого параметра нет. Не найден.
*
*/
function getCookie(name){
	var matches = document.cookie.match(new RegExp("(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"));
	return matches ? decodeURIComponent(matches[1]) : undefined;
}

/** +++
* Отправляет полученный сформированный запрос и возвращает ответ в виде String
*
*/
function sendQuery(theUrl){
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.open("GET", theUrl, false);
	xmlHttp.send(null);
	return xmlHttp.responseText;
}

/** +++
* Возвращает HTML элемент что выделен как текущий (выделенный) получатель/собеседник.
*
*/
function getElementsMarkedAsCurrentRecipient(){
	var ulElemenForRecipient = document.getElementById(liElementThatContainsRecipientsList);
	var nameElements = ulElemenForRecipient.querySelectorAll(cssSelectorTogetNameOfCurrentRecipient);
	return nameElements;
}

/** +++
* Из полученного HTML элемента выдергивает имя получателя/собеседника и возвращает в виде строки.
*
*/
//TODO: Нужно принудительно ставить у первого элемента значение текущий, иначе падаем с ошибокой.
function getNameOfCurrentRecipient(){
	var firstElement = getElementsMarkedAsCurrentRecipient()[0];
	var nameOfCurrentUser='undefined';
	if (firstElement){
		nameOfCurrentUser = firstElement.getAttribute("name");
	}
	return nameOfCurrentUser;
}

/** +++
* По клику делает получателя/собеседника текущим/активным.
*
*/
function doCurrentRecipient(event){
	var nameElements = getElementsMarkedAsCurrentRecipient()
	for (var index = 0; index < nameElements.length; index++){
		nameElements[index].classList.remove(classForMarkCurrentRecipient);
	}
	targetElement = event.target;
	targetElement.setAttribute("class", classForMarkCurrentRecipient);
}

/** +++
* Возвращает сообщение из поля ввода для отправки сообщения.
*
*/
function getMessageFromInputField(){
	var textForSend = document.getElementById("messageTextField").value;
	return textForSend;
}

/** ---
* Добавляет сообщение на страницу переписки.
*
*/
function addMessageToPage(fromUser, textOfMessage){
	var textForLogger = ">>> fromUser = {0} | textOfMessage = {1} >>>".printf(fromUser, textOfMessage);
	console.log(">>>" + textForLogger + ">>> addMessageToPage( ... )");
}

/** +++
* Добавляет на страницу пользователей, предварительно проверяте, есть ли уже такой пользователь на странице. Если на странице такой пользователь уже есть то добавление не происходит.
*
*/
function addOneUserToPage(nameOfUser){
	if(nameOfUser.length < 4){
		console.warn(">>>" + "У имени пользователя длина меньше 4x символов. Выходим." + ">>>");
		return;
	}
	var ulElemenForUser = document.getElementById(liElementThatContainsRecipientsList);
	var liElement  = document.createElement("li");
	var aElement = document.createElement("a");
	if(! isPresentRowForRecipient(nameOfUser)){
		console.log(">>>" + "Такого пользователя в списке нет, будем добавлять на страничку!" + ">>>");
		aElement.href = "#";
		aElement.name = nameOfUser;
		aElement.innerHTML = nameOfUser;
		//aElement.onclick = doCurrentRecipient; //aElement.onclick = doCurrentRecipient.call();
		aElement.addEventListener("click", doCurrentRecipient, false);
		liElement.appendChild(aElement);
		ulElemenForUser.appendChild(liElement);
	} else{
		console.warn(">>>" + "Такой пользователь в списке уже есть!" + ">>>");
	}
}

/** +++
* Проверяет присутствует ли пользователь на странице.
*
*/
function isPresentRowForRecipient(nameOfUser){
	var ulElemenForUser = document.getElementById(liElementThatContainsRecipientsList);
	var aElements = ulElemenForUser.getElementsByTagName("a");
	for (var index = 0; index < aElements.length; index++){
		var nameAttribute = aElements[index].getAttribute("name");
		if (nameAttribute == nameOfUser){
			return true;
		}
	}
	return false;
}

/** +++
* Выполняет разавторизацию и производите перевод на страницу логина.
*
*/
function unAuthorized(){
	var nameOfCurrentUser = getCookie("LoginMemeber");
	var theUrl = "/ChatOnServlet/chat/?mode=UnLogin&LoginMemeber={0}".printf(nameOfCurrentUser);
	var resultOfRequest = sendQuery(theUrl);

	var textForLogger = ">>> nameOfCurrentUser = {0} | resultOfRequest = {1} >>>".printf(nameOfCurrentUser, resultOfRequest);
	console.log(textForLogger);

	window.location.replace(resultOfRequest);
}

/** ???
* Отправляет сообщение на сервер.
*
*/
function sendMessageOnServer(){
	var nameOfCurrentUser = getCookie("LoginMemeber");
	var nameOfCurrentRecipient = getNameOfCurrentRecipient();
	if (nameOfCurrentRecipient == 'undefined'){
		return 'undefined';
	}
	var theUrl = "/ChatOnServlet/chat/?mode=Message&Direct=send&NameOfCurrentUser={0}&Recipient={1}&textMessage={2}".printf(nameOfCurrentUser, nameOfCurrentRecipient, getMessageFromInputField());
	var resultOfRequest = sendQuery(theUrl);

	//var textForLogger = ">>> theUrl = {0} | resultOfRequest = {1} >>>".printf(theUrl, resultOfRequest);
	//console.log(">>>" + textForLogger + ">>> sendMessageOnServer()");

	addMessageToPage(forUser, textOfMessage);
}

/** ???
* Получает сообщение с сервера.
*
*/
function getMessagesFromServer(){
	var nameOfCurrentUser = getCookie("LoginMemeber");
	var nameOfCurrentRecipient = getNameOfCurrentRecipient();
	if (nameOfCurrentRecipient == 'undefined'){
		return 'undefined';
	}
	var theUrl = "/ChatOnServlet/chat/?mode=Message&Direct=get&NameOfCurrentUser={0}&AuthorMessage={1}".printf(nameOfCurrentUser, nameOfCurrentRecipient);
	var resultOfRequest = sendQuery(theUrl);

	//console.log(">>>" + resultOfRequest + ">>> getMessagesFromServer()");
	//а чего тут делаем?
}

var interval = setInterval(getMessagesFromServer, 10000);

/** ???
* 
* Добавляет список пользователей на страницу.
* Используется при загрузке страницы.
*/
function getListOfUsers(){
	var nameOfCurrentUser = getCookie("LoginMemeber");
	var theUrl = "/ChatOnServlet/chat/?mode=Message&Direct=get&ListUser=active&NameOfCurrentUser={0}".printf(nameOfCurrentUser);
	var resultOfRequest = sendQuery(theUrl);

	console.log(">>>" + resultOfRequest + ">>> getListOfUsers()");
	var parsedJsonFromReceivedString = JSON.parse(resultOfRequest);
	for(var index=0; index < parsedJsonFromReceivedString.length; index++){
		addOneUserToPage(parsedJsonFromReceivedString[index]);
	}
}

//window.onload = getListOfUsers();