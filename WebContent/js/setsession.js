var uid = null;
var phrase = null;
	
function setSession(){
	$.ajax({
		url : "setsession",
		data: {
			uid: uid,
			phrase: phrase
		},
		cache: false,
		}).success(function(data) {
			if(data == "set"){
				window.location = "index.html";
			}
			else{
				$("#messages").empty().append("Error loading session.");
			}
		});
}

function setParameters() {
	var params = window.location.search.substring(1).split("&");
	for(var i = 0; i < params.length; i++){
		var pv = params[i].split("=");
		if(pv[0] == "uid"){
			uid = pv[1];
		}
		else if(pv[0] == "phrase"){
			phrase = pv[1];
		}
	}
}
	
$(function(){
	setParameters();
	
	$.ajax({
		url : "getsessionurl",
		cache: false,
		}).success(function(data) {
			if(data == ""){
				setSession();
			}
			else{
				var url = window.location.href;
				url = url.slice(0,url.lastIndexOf("/"));
				
				$("#messages").append("Your current session is about to change.  Please save the following URL to retrieve it: "+url+data);
				$("#messages").append('<br /><br /><a href="#" id="continue">Click to continue</a>');
				$("#continue").click(function(){
					setSession();
				});
			}
		});
	
	
});

