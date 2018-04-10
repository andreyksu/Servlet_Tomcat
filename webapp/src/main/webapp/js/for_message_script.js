var expandCollapse = function(){
	if ($(window).width() < 768) {
		$("#hideElement").css('display', 'none');//$("#hideElement").hide();
	}
	else {
		$("#hideElement").css('display', 'block');//$("#hideElement").show();
	}
}

$(window).resize(expandCollapse);