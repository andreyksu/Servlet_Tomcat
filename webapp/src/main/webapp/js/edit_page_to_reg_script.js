//RegistrationRadio  AuthorizationRadio

$(document).ready(function() {
	$("#AuthorizationRadio").attr('checked', true);
});

function doRegistrationForm() {
	// $("#FormTable tr:nth-child(2)").after();
	// $("#HeadOfLoginPage").css("background-color", "blue");

	if (!($('#NameMember').length)) {
		$('#PasswordMember').after('<tr id="NameMember"><td align="left">Your Name:</td><td align="left"><input type="text" maxlength="20" minlength="3" name="NameMember" required /></td></tr>');
	} else {
		$('#NameMember').show()
	}
}

function doAuthorizationForm() {
	if (($('#NameMember').length)) {
		$('#NameMember').remove();
	} else {
		//Не получилось просто прятать, такак поля обязательные и при отправке, браузер требует их заполнить, но они скрыты.
		//Когда перейду полностью на JS можно будет просто скрывать, без удаления.
		$('#NameMember').hide();
	}

}
