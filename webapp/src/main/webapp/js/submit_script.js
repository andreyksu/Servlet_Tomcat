function tmp() {
	var req = new XMLHttpRequest();
	req.open('GET', 'http://localhost:8090/ChatOnServlet/chat/', true);
	req.onload = function() {
		console.log(req.responseText);
		console.log(req.status, req.statusText);
		console.log(req.getResponseHeader("content-type"));

	}
	req.addEventListener("load", function() {
		console.log("Done:", req.status);
	});	
	req.send(null);
}

function tmp1() {
	$.get("somepage.php", {
		paramOne : 1,
		paramX : 'abc'
	}, function(data) {
		alert('page content: ' + data);
	});
}

function tmp2() {
	window.open('html/DebagPage.html', '_self');	
}


