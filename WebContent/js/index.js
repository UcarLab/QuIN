var datasetlabelindex = [];
var datasetlabels = [];

var colors = ["#0099ff", "#ff0000", "#00cc00", "#ffcc00", "#663399",
			"#ffff33", "#00ffcc", "#ff3399", "#00ff33", "#ff6600", "#cc33cc",
			"#ccff66", "#006699", "#990000", "#339933", "#33cc00", "#336633",
			"#cccc33", "#0000ff", "#ff3366", "#33ff00", "#cc9900", "#cc66ff",
			"#ffff99" ];
var colorindex = [];
var pcolorindex = [];
var secolorindex = [];
var selabels = [];

$(function(){	
	$(".ureset, .uupload, #u_progressclose, #b_progressclose, #resetbuild, #build, #resetview, #view, #merge, #resetmerge, #loaddata, #exportca, #exportna, #tdexport, #td_loaddata, #td_view, #loadnm, #loadaieh").button();
	
	
	$("#uploadchiapet").click(function(){
		uploadChIAPETData();
		$("#uploadchiapetform")[0].reset();
	});
	
	$("#import_upload").click(function(){
		importNetwork();
		$("#importform")[0].reset();
	});
	
	$("#uploadbed").click(function(){
		uploadBEDFile();
		$("#uploadbedform")[0].reset();
	});
	
	$("#uploadgene").click(function(){
		uploadGeneList();
		$("#uploadgeneform")[0].reset();
	});
	
	$("#uploadtd").click(function(){
		uploadTDList();
		$("#uploadtdform")[0].reset();
	});
	
	$("#uploadsnp").click(function(){
		uploadSNPList();
		$("#uploadsnpform")[0].reset();
	});
	
	$("#resetmerge").click(function(){
		$("#mergeform")[0].reset();
	});
	
	$("#resetbuild").click(function(){
		$("#buildform")[0].reset();
	});
	
	$("#resetview").click(function(){
		$("#viewform")[0].reset();
	});
	
	$("#uploaddialog").dialog({
		autoOpen: false,
		closeOnEscape: false,
		resizable: false,
		modal: true
	});
	
	$("#u_progressbar").progressbar();
	
	$("#u_progressclose").click(function(){
		$("#uploaddialog").dialog("close");
	});
	
	$("#build").click(function(){
		buildNetwork();
	});
	
	$("#merge").click(function(){
		mergeNetwork();
	});
	
	$("#builddialog").dialog({
		autoOpen: false,
		closeOnEscape: false,
		resizable: false,
		modal: true,
	});
	
	$("#cookiemessage").dialog({
		autoOpen: false,
		closeOnEscape: false,
		resizable: false,
		modal: true
	});
	
	$("#cookiemessage").parent().find(".ui-dialog-titlebar-close").css("display", "inline");


	$.ajax({
		url : "firstvisit",
		dataType : "json",
		cache: false,
		type : "post",
		}).success(function(data) {
			if(data == true){
				$("#cookiemessage").dialog("open");
			}
		});
	
	$("#b_progressbar").progressbar();
	
	$("#b_progressclose").click(function(){
		$("#builddialog").dialog("close");
	});
	
	$("#nodeinfodialog").dialog({
		autoOpen: false,
		modal: true,
		minWidth: 500,
		maxHeight: 600
	});
	
	$("#nodeinfodialog").parent().find(".ui-dialog-titlebar-close").css("display", "inline");

	
	$("#edgeinfodialog").dialog({
		autoOpen: false,
		modal: true,
		minWidth: 500,
		maxHeight: 600
	});
	
	$("#edgeinfodialog").parent().find(".ui-dialog-titlebar-close").css("display", "inline");
	
	$("#videotutorial").dialog({
		width: "auto",
		height: "auto",
		autoOpen: false,
		modal: true,
	});
	$("#videotutorial").parent().find(".ui-dialog-titlebar-close").css("display", "inline");
	
	$("#videolink").click(function(e){
		e.preventDefault();
		$("#videotutorial").dialog("open");
	});
	
	$("#view").click(function(){
		viewNetwork();
	});
	
	$("#td_view").click(function(){
		viewTDNetwork();
	});
	
	$("#loaddata, #td_loaddata").click(function(){
		updateMenus();
	});
	
	$("#menuaccordion").accordion({
		icons: null,
		heightStyle: "content",
		autoHeight: false,
		active: 7
	});

	$("#apptabs").tabs({
		activate: function(){
			$("#network").cytoscape('get').resize();
		}
	});

	$("#divider").click(function() {
		$("#appmenu").toggle();
		$("#menutoggle").toggleClass("ui-icon-triangle-1-w");
		$("#menutoggle").toggleClass("ui-icon-triangle-1-e");
		$("#network").cytoscape('get').resize();
	});

	updateMenus();
	
	 $( "#u_tdlist" ).bind( "keydown", function( event ) {
		 if ( event.keyCode === $.ui.keyCode.TAB && $( this ).autocomplete( "instance" ).menu.active ) {
			 event.preventDefault();
		 }
	 }).autocomplete({
		 source: function( request, response ) {
			 $.getJSON( "traitautocomplete", {
				 term: request.term.split(/\n/).pop()
			 }
			 , response);
		 },
		 search: function() {
			var term = this.value.split(/\n/).pop();
			if ( term.length < 2 ) {
				return false;
			}
		 },
		 focus: function() {
			return false;
		 },
		 select: function( event, ui ) {
			 var terms = this.value.split( /\n/ );
			terms.pop();
			terms.push( ui.item.value );
			terms.push( "" );
			this.value = terms.join( "\n" );
			return false;
		 }
	 });
	 
	 
	 
	 $(".superimposedcolor, .tdt_superimposedcolor, .tds_superimposedcolor, .td_secolor, .e_sedgescolor").spectrum({
		    color: colors[0],
		    showInput: true,
		    className: "full-spectrum",
		    showInitial: true,
		    showPalette: false,
		    showSelectionPalette: false,
		    preferredFormat: "hex",
		    localStorageKey: "spectrum.demo",
		    move: function (color) {
		        
		    },
		    show: function () {
		    
		    },
		    beforeShow: function () {
		    
		    },
		    hide: function () {
		    
		    },
		    change: function() {
		        
		    }
		});
	 
	 $(".tdt_superimposedcolor").spectrum("set", colors[1]);
});

function initProgress(uidialog, uiclose, uibar, uibarlabel, message, uiinstruct, imessage){
	$(uidialog).dialog("open");
	$(uiclose).hide();
	$(uibar).progressbar("value", false);
	$(uibarlabel).empty().append(message);
	$(uibar).find(".ui-progressbar-value").css({
		"background": "#8ad"
	});
	$(uiinstruct).empty().append(imessage);
}

function progressSuccess(uibar, uibarlabel, message, uiinstruct, imessage, callback){
	$(uibar).progressbar("value", 100);
	$(uibarlabel).empty().append(message);
	$(uibar).find(".ui-progressbar-value").css({
		"background": "green"
	});
	$(uiinstruct).empty().append(imessage);
	callback();
}

function progressFail(uibar, uibarlabel, data, uiinstruct, imessage){
	$(uibar).progressbar("value", 100);
	$(uibarlabel).empty().append(data);
	$(uibar).find(".ui-progressbar-value").css({
		"background": "red"
	});
	$(uiinstruct).empty().append(imessage);
}

function progressComplete(uiclose){
	$(uiclose).show();
}

function uploadChIAPETData(){
	initProgress("#uploaddialog", "#u_progressclose", "#u_progressbar", "#u_progresslabel", "Uploading File", "#u_instructions", "");
	
	var data = new FormData();
	
	data.append('name', $('#u_chiapetlabel').val());
	data.append('file', $('#u_chiapet')[0].files[0]);

	data.append('type', $("#u_chiapet_type_gene").prop("checked") ? "g2g" :"");
	
	uploadAjax("uploadchiapet", data);
}

function importNetwork(){
	initProgress("#uploaddialog", "#u_progressclose", "#u_progressbar", "#u_progresslabel", "Uploading File", "#u_instructions", "");
	var data = new FormData();
	data.append('name', $('#import_label').val());
	data.append('file', $('#import_file')[0].files[0]);

	uploadAjax("importnetwork", data);
}

function uploadBEDFile(){
	initProgress("#uploaddialog", "#u_progressclose", "#u_progressbar", "#u_progresslabel", "Uploading File", "#u_instructions", "");
	
	var data = new FormData();
	
	data.append('name', $('#u_bedlabel').val());
	data.append('file', $('#u_bedfile')[0].files[0]);
	
	uploadAjax("uploadregions", data);
}

function uploadGeneList(){
	initProgress("#uploaddialog", "#u_progressclose", "#u_progressbar", "#u_progresslabel", "Uploading File", "#u_instructions", "");
	
	var data = new FormData();
	
	data.append('name', $('#u_genelabel').val());
	data.append('file', $('#u_genefile')[0].files[0]);
	data.append('list', $('#u_genelist').val());

	uploadAjax("uploadgenes", data);
}

function uploadTDList(){
	initProgress("#uploaddialog", "#u_progressclose", "#u_progressbar", "#u_progresslabel", "Uploading File", "#u_instructions", "");
	
	var data = new FormData();
	
	data.append('name', $('#u_tdlabel').val());
	data.append('file', $('#u_tdfile')[0].files[0]);
	data.append('list', $('#u_tdlist').val());

	uploadAjax("uploaddiseases", data);
}

function uploadSNPList(){
	initProgress("#uploaddialog", "#u_progressclose", "#u_progressbar", "#u_progresslabel", "Uploading File", "#u_instructions", "");
	
	var data = new FormData();
	
	data.append('name', $('#u_snplabel').val());
	data.append('file', $('#u_snpfile')[0].files[0]);
	data.append('list', $('#u_snplist').val());

	uploadAjax("uploadsnps", data);
}

function uploadAjax(url, idata){
	if(url != null){
		$.ajax({
			url : url,
			data: idata,
			cache: false,
			contentType: false,
			processData: false,
			type : "post",
			}).success(function(data) {
				if(data == "1"){
					//success!
					progressSuccess("#u_progressbar", "#u_progresslabel", "Upload Complete!", "#u_instructions", "Please find your data available in the Build Network and/or Explore Network menus.", function(){});
					updateMenus();
				}
				else{
					//error
					progressFail("#u_progressbar", "#u_progresslabel", data, "#u_instructions", "");
				}
				progressComplete("#u_progressclose");

			}).error(function(req, status, error) {
				progressFail("#u_progressbar", "#u_progresslabel", status+error, "#u_instructions", "");
				progressComplete("#u_progressclose");
		});
	}
}

function viewNetwork(){
	var network  = $("#e_chiapet").val();
	var name  = $("#e_chiapet :selected").text();

	var sd = [];
	var sc = [];
	
	$("#apptabs").tabs("option", "disabled", [] );
	$("#network").empty().append("Loading...");
	$("#sph").empty();

	$("#ns_name").empty().append(name);
	$("#ns_comp").empty().append("&nbsp;");
	$("#ns_node").empty().append("&nbsp;");
	$("#ns_edge").empty().append("&nbsp;");
	$("#ns_avgpet").empty().append("&nbsp;");
	$("#ns_extend").empty().append("&nbsp;");
	$("#ns_minpet").empty().append("&nbsp;");
	$("#ns_intra").empty().append("&nbsp;");
	$("#ns_inter").empty().append("&nbsp;");
	$("#ns_mincompsize").empty().append("&nbsp;");
	$("#ns_maxcompsize").empty().append("&nbsp;");

	$(".superimposedelement").each(function(){
		sd.push($(this).find(".superimposedsets").val());
		sc.push($(this).find(".superimposedcolor").spectrum("get").toString());
	});
	
	var genes = [];
	var diseases = [];
	var regions = [];
	var snps = [];
	var gci = [];
	var dci = [];
	var rci = [];
	var sci = [];
	var pci = [];
	
	for(var i = 0; i < sd.length; i++){
		var cur = sd[i].split("_");
		var type = cur[0];
		var val = cur[1];
		
		if(type == "1"){
			regions.push(val);
			rci.push(sc[i]);
		}
		else if(type == "2"){
			genes.push(val);
			gci.push(sc[i]);
		}
		else if(type == "3"){
			diseases.push(val);
			dci.push(sc[i]);
		}
		else if(type == "4"){
			snps.push(val);
			sci.push(sc[i]);
		}
		else if(type == "P"){
			pci.push(sc[i])
		}
	}
	
	
	var ci = [];
	for(var i = 0; i < rci.length; i++){
		ci.push(rci[i]);
	}
	for(var i = 0; i < gci.length; i++){
		ci.push(gci[i]);
	}
	for(var i = 0; i < dci.length; i++){
		ci.push(dci[i]);
	}
	for(var i = 0; i < sci.length; i++){
		ci.push(sci[i]);
	}
	colorindex = ci;
	pcolorindex = pci;
	
	
	//add supporting interactions
	var sil = [];
	var si = [];
	var sic = []
	$(".e_sedgeselement").each(function(){
		var seval = $(this).find(".e_se").val()
		if(seval != -1){
			sil.push($(this).find(".e_se option:selected").text());
			si.push(seval);
			sic.push($(this).find(".e_sedgescolor").spectrum("get").toString());
		}
	});
	secolorindex = sic;
	selabels = sil;
	
	var idata = {
		network: network,
		genes: genes,
		diseases: diseases,
		regions: regions,
		snps: snps,
		traitsrc: $("#traitdb").val(),
		minsize: $("#e_size1").val(),
		maxsize: $("#e_size2").val(),
		annotatedonly: $("#annotatedonly").prop("checked").toString(),
		sortby: $("#sortby").val(),
		colorindex: ci,
		supportingedges: si,
		secolorindex: sic
	}
	
	$.ajax({
		url : "getccids",
		data: idata,
		dataType : "json",
		cache: false,
		type : "post",
		}).success(function(data) {
			setCCIds(data, idata);
			setNetworkInfo(idata);
		}).error(function(req, status, error) {
			//TODO
		});
	
	$.ajax({
		url : "networkstatistics",
		data: { 
			network: network,
			binsize: 500,
			density: "true",
			minsize: $("#e_size1").val(),
			maxsize: $("#e_size2").val()
		},
		dataType : "json",
		cache: false,
		type : "post",
		}).success(function(data) {
			$("#nshistogram").empty().append('<img src="data:image/png;base64,'+data.nodespan+'">');
			$("#nshistogram").append('<img src="data:image/png;base64,'+data.interactionsep+'">');

		}).error(function(req, status, error) {
			//TODO
		});
	
}

function getTDCCIDData(){
	var network  = $("#td_network").val();
	var name  = $("#td_network :selected").text();

	$("#apptabs").tabs("option", "active", [ 0 ] );
	$("#apptabs").tabs("option", "disabled", [ 2, 3 ] );
	
	$("#network").empty().append("Loading...");

	$("#ns_name").empty().append(name);
	$("#ns_comp").empty().append("&nbsp;");
	$("#ns_node").empty().append("&nbsp;");
	$("#ns_edge").empty().append("&nbsp;");
	$("#ns_avgpet").empty().append("&nbsp;");
	$("#ns_extend").empty().append("&nbsp;");
	$("#ns_minpet").empty().append("&nbsp;");
	$("#ns_intra").empty().append("&nbsp;");
	$("#ns_inter").empty().append("&nbsp;");
	$("#ns_mincompsize").empty().append("&nbsp;");
	$("#ns_maxcompsize").empty().append("&nbsp;");

	var s_genes = [];
	var s_diseases = [];
	var s_regions = [];
	var s_snps = [];
	
	var t_genes = [];
	var t_diseases = [];
	var t_regions = [];
	var t_snps = [];
	
	var genes = [];
	var diseases = [];
	var regions = [];
	var snps = [];
	var gci = [];
	var dci = [];
	var rci = [];
	var sci = [];
	var pci = [];
	
	var sp = false;
	var tp = false;
	
	var s_sd = [];
	var s_sc = [];
	
	
	$(".tds_superimposedelement").each(function(){
		s_sd.push($(this).find(".tds_superimposedsets").val());
		s_sc.push($(this).find(".tds_superimposedcolor").spectrum("get").toString());
	});
	

	for(var i = 0; i < s_sd.length; i++){
		var cur = s_sd[i].split("_");
		var type = cur[0];
		var val = cur[1];
		
		if(type == "1"){
			regions.push(val);
			s_regions.push(val);
			rci.push(s_sc[i]);
		}
		else if(type == "2"){
			genes.push(val);
			s_genes.push(val);
			gci.push(s_sc[i]);
		}
		else if(type == "3"){
			diseases.push(val);
			s_diseases.push(val);
			dci.push(s_sc[i]);
		}
		else if(type == "4"){
			snps.push(val);
			s_snps.push(val);
			sci.push(s_sc[i]);
		}
		else if(type == "P"){
			pci.push(s_sc[i])
			sp = true;
		}
	}
	
	var t_sd = [];
	var t_sc = [];
	$(".tdt_superimposedelement").each(function(){
		t_sd.push($(this).find(".tdt_superimposedsets").val());
		t_sc.push($(this).find(".tdt_superimposedcolor").spectrum("get").toString());
	});
	

	for(var i = 0; i < t_sd.length; i++){
		var cur = t_sd[i].split("_");
		var type = cur[0];
		var val = cur[1];
		
		if(type == "1"){
			regions.push(val);
			t_regions.push(val);
			rci.push(t_sc[i]);
		}
		else if(type == "2"){
			genes.push(val);
			t_genes.push(val);
			gci.push(t_sc[i]);
		}
		else if(type == "3"){
			diseases.push(val);
			t_diseases.push(val);
			dci.push(t_sc[i]);
		}
		else if(type == "4"){
			snps.push(val);
			t_snps.push(val);
			sci.push(t_sc[i]);
		}
		else if(type == "P"){
			tp = true;
			pci.push(t_sc[i])
		}
	}
	
	
	
	
	var ci = [];
	for(var i = 0; i < rci.length; i++){
		ci.push(rci[i]);
	}
	for(var i = 0; i < gci.length; i++){
		ci.push(gci[i]);
	}
	for(var i = 0; i < dci.length; i++){
		ci.push(dci[i]);
	}
	for(var i = 0; i < sci.length; i++){
		ci.push(sci[i]);
	}
	colorindex = ci;
	pcolorindex = pci;
	
	//add supporting interactions
	var sil = [];
	var si = [];
	var sic = []
	$(".td_sedgeselement").each(function(){
		var seval = $(this).find(".td_se").val();
		if(seval != -1){
			sil.push($(this).find(".td_se option:selected").text());
			si.push(seval);
			sic.push($(this).find(".td_secolor").spectrum("get").toString());
		}
	});
	selabels = sil;
	secolorindex = sic;
	
	var idata = {
		network: network,
		genes: genes,
		diseases: diseases,
		regions: regions,
		snps: snps,
		s_genes: s_genes,
		s_diseases: s_diseases,
		s_regions: s_regions,
		s_snps: s_snps,
		t_genes: t_genes,
		t_diseases: t_diseases,
		t_regions: t_regions,
		t_snps: t_snps,
		sp: sp.toString(),
		tp: tp.toString(),
		traitsrc: $("#traitdb").val(),
		minsize: $("#td_size1").val(),
		maxsize: $("#td_size2").val(),
		annotatedonly: "TRUE",
		sortby: 1,
		colorindex: ci,
		supportingedges: si,
		secolorindex: sic
	}
	
	return idata;
}


function viewTDNetwork(){
	var idata = getTDCCIDData();
	$.ajax({
		url : "gettdccids",
		data: idata,
		dataType : "json",
		cache: false,
		type : "post",
		}).success(function(data) {
			setCCIds(data, idata);
			setNetworkInfo(idata);
		}).error(function(req, status, error) {
		});
	
	$.ajax({
		url : "networkstatistics",
		data: { 
			network: network,
			binsize: 500,
			density: "true",
			minsize: $("#td_size1").val(),
			maxsize: $("#td_size2").val()
		},
		dataType : "json",
		cache: false,
		type : "post",
		}).success(function(data) {
			$("#nshistogram").empty().append('<img src="data:image/png;base64,'+data.nodespan+'">');
			$("#nshistogram").append('<img src="data:image/png;base64,'+data.interactionsep+'">');

		}).error(function(req, status, error) {
		});
	
}

function mergeNetwork(){
	initProgress("#builddialog", "#b_progressclose", "#b_progressbar", "#b_progresslabel", "Building Network", "#b_instructions", "");
	var name = $("#m_name").val();
	var fid1 = $("#m_n1").val();
	var fid2 = $("#m_n2").val();
	
	var data = {
		name: name,
		fid1: fid1,
		fid2: fid2
	};

	$.ajax({
		url : "mergenetworks",
		data: data,
		dataType : "json",
		cache: false,
		type : "post",
		}).success(function(data) {
			if(data[0] == 1){
				//success!
				var callback = function(){
					$("#menuaccordion").accordion("option", "active", 7);
				};
				progressSuccess("#b_progressbar", "#b_progresslabel", "Network Complete!", "#b_instructions", "Please use the Explore Network menu to view this network.", callback);
				updateMenus();
			}
			else{
				//error
				progressFail("#b_progressbar", "#b_progresslabel", data[0], "#b_instructions", "");
			}
			progressComplete("#b_progressclose");
		}).error(function(req, status, error) {
			progressFail("#b_progressbar", "#b_progresslabel", error, "#b_instructions", "");
			progressComplete("#b_progressclose");
		});
}


function buildNetwork(){
	initProgress("#builddialog", "#b_progressclose", "#b_progressbar", "#b_progresslabel", "Building Network", "#b_instructions", "");

	var name = $("#b_name").val();
	var fid = $("#b_dataset").val();
	var rf = $("#b_rf").val();
	var ext = $("#b_ext").val();
	var minpet = $("#b_minpet").val();
	var maxdist = $("#b_maxdist").val();
	var intermin = $("#b_interminb").prop("checked") ? 0 : $("#b_interminter").prop("checked") ? -1 : 1;
	
	var data = {
		name: name,
		fid: fid,
		rf: rf,
		ext: ext,
		minpet: minpet,
		maxdist: maxdist,
		intermin: intermin,
	};

	$.ajax({
		url : "buildnetwork",
		data: data,
		dataType : "json",
		cache: false,
		type : "post",
		}).success(function(data) {
			if(data[0] == 1){
				//success!
				var callback = function(){
					$("#menuaccordion").accordion("option", "active", 7);
				};
				progressSuccess("#b_progressbar", "#b_progresslabel", "Network Complete!", "#b_instructions", "Please use the Explore Network menu to view this network.", callback);
				updateMenus();
			}
			else{
				//error
				progressFail("#b_progressbar", "#b_progresslabel", data[0], "#b_instructions", "");
			}
			progressComplete("#b_progressclose");
		}).error(function(req, status, error) {
			progressFail("#b_progressbar", "#b_progresslabel", error, "#b_instructions", "");
			progressComplete("#b_progressclose");
		});
}

function updateMenus(){
	$.ajax({
		url: 'getuploadeddataids',
		dataType : "json",
		cache: false
	}).success(function(data){
		$("#b_dataset").empty();
		$("#e_chiapet").empty();
		$("#td_network").empty();
		$("#m_n1").empty();
		$("#m_n2").empty();
		updateSelect("#b_dataset", data.pchiapetdata, "Available Data");


		updateSelect("#e_chiapet", data.pnetworks, "Available Networks");
		updateSelect("#td_network", data.pnetworks, "Available Networks");
		updateSelect("#m_n1", data.pnetworks, "Available Networks");
		updateSelect("#m_n2", data.pnetworks, "Available Networks");

		updateSelect("#b_dataset", data.chiapetdata, "Uploaded Data");
		
		updateSelect("#e_chiapet", data.networks, "Uploaded Networks");
		updateSelect("#td_network", data.networks, "Uploaded Networks");
		updateSelect("#m_n1", data.networks, "Uploaded Networks");
		updateSelect("#m_n2", data.networks, "Uploaded Networks");

		updateSuperImposeSelect(".superimposedsets", data, [["-1", "None"]]);
		updateSuperImposeSelect(".tds_superimposedsets", data, [["P", "Promoters (2KB from TSS)"]]);
		updateSuperImposeSelect(".tdt_superimposedsets", data, [["P", "Promoters (2KB from TSS)"]]);
		
		
		addInitVals(".e_se", [["-1", "None"]]);
		addSuperImposeSelectGroup(".e_se", "Uploaded Data", data.chiapetdata, "");
		addSuperImposeSelectGroup(".e_se", "Available Data", data.pchiapetdata, "");
		addInitVals(".td_se", [["-1", "None"]]);
		addSuperImposeSelectGroup(".td_se", "Available Data", data.pchiapetdata, "");
		addSuperImposeSelectGroup(".td_se", "Uploaded Data", data.chiapetdata, "");

		updateDatasetLabels(data);
	});
	
	$.ajax({
		url: 'getsessionurl',
		cache: false
	}).success(function(data){
		$("#sessionlink").empty()
		if(data != ""){
			var url = window.location.href;
			url = url.slice(0,url.lastIndexOf("/"));
			$("#sessionlink").append('Link to this session (This link will allow editing of this session): <input type="text" style="width: 200px" value="'+url+data+'"/>')
		}
	});
}

function updateSelect(ui, data, grouplabel){
	var html = '<optgroup label='+grouplabel+'>';
	for(var i = 0; i < data.length; i++){
		html += '<option value="'+data[i][0]+'">'+data[i][1]+'</option>';
	}
	html += "</optgroup>"
	$(ui).append(html);
}


function addSuperImposeSelectGroup(ui, grouplabel, data, dataprefix){
	var html = '<optgroup label="'+grouplabel+'">';
	for(var i = 0; i < data.length; i++){
		html += '<option value="'+dataprefix+data[i][0]+'">'+data[i][1]+'</option>';
	}
	html += '</optgroup>';
	$(ui).append(html);
}

function addInitVals(ui, initvals){
	$(ui).empty();
	if(initvals instanceof Array){
		for(var i = 0; i < initvals.length; i ++){
			$(ui).append('<option value="'+initvals[i][0]+'">'+initvals[i][1]+'</option>');
		}
	}
}

function updateSuperImposeSelect(ui, data, initvals){
	addInitVals(ui, initvals)

	addSuperImposeSelectGroup(ui, "Preloaded Region Lists", data.pregionlists, "1_");
	addSuperImposeSelectGroup(ui, "Uploaded Region Lists", data.regionlists, "1_");
	addSuperImposeSelectGroup(ui, "Preloaded Gene Lists", data.pgenelists, "2_");
	addSuperImposeSelectGroup(ui, "Uploaded Gene Lists", data.genelists, "2_");
	addSuperImposeSelectGroup(ui, "Preloaded Disease/Trait Lists", data.pdiseaselists, "3_");
	addSuperImposeSelectGroup(ui, "Uploaded Disease/Trait Lists", data.diseaselists, "3_");
	addSuperImposeSelectGroup(ui, "Preloaded SNP Lists", data.psnplists, "4_");
	addSuperImposeSelectGroup(ui, "Uploaded SNP Lists", data.snplists, "4_");
}


function updateDatasetLabels(data){
	datasetlabelindex = [];
	datasetlabels = [];
	
	var pregions = data.pregionlists;
	var pgenes = data.pgenelists;
	var pdiseases = data.pdiseaselists;
	var psnps = data.psnplists;
	for(var i = 0; i < pregions.length; i++){
		datasetlabelindex.push("1_"+pregions[i][0]);
		datasetlabels.push(pregions[i][1]);
	}
	
	for(var i = 0; i < pgenes.length; i++){
		datasetlabelindex.push("2_"+pgenes[i][0]);
		datasetlabels.push(pgenes[i][1]);
	}
	
	for(var i = 0; i < pdiseases.length; i++){
		datasetlabelindex.push("3_"+pdiseases[i][0]);
		datasetlabels.push(pdiseases[i][1]);
	}
	
	for(var i = 0; i < psnps.length; i++){
		datasetlabelindex.push("4_"+psnps[i][0]);
		datasetlabels.push(psnps[i][1]);
	}
	
	var regions = data.regionlists;
	var genes = data.genelists;
	var diseases = data.diseaselists;
	var snps = data.snplists;

	for(var i = 0; i < regions.length; i++){
		datasetlabelindex.push("1_"+regions[i][0]);
		datasetlabels.push(regions[i][1]);
	}
	
	for(var i = 0; i < genes.length; i++){
		datasetlabelindex.push("2_"+genes[i][0]);
		datasetlabels.push(genes[i][1]);
	}
	
	for(var i = 0; i < diseases.length; i++){
		datasetlabelindex.push("3_"+diseases[i][0]);
		datasetlabels.push(diseases[i][1]);
	}
	for(var i = 0; i < snps.length; i++){
		datasetlabelindex.push("4_"+snps[i][0]);
		datasetlabels.push(snps[i][1]);
	}
}


function addAnnotationSet(divid, elementclass, colordivclass, colorclass, coloroffset, initvals){
	var elements = $("."+elementclass);
	var clone = $(elements).first().clone();
	var cdiv = clone.find("."+colordivclass);
	$(cdiv).empty().append("<input type=\"text\" class=\""+colorclass+"\" />");
	var cchooser = $(cdiv).find("."+colorclass);
	$(cchooser).spectrum({
	    color: colors[coloroffset%colors.length],
	    showInput: true,
	    className: "full-spectrum",
	    showInitial: true,
	    showPalette: false,
	    showSelectionPalette: false,
	    preferredFormat: "hex",
	    localStorageKey: "spectrum.demo",
	    move: function (color) {
	        
	    },
	    show: function () {
	    
	    },
	    beforeShow: function () {
	    
	    },
	    hide: function () {
	    
	    },
	    change: function() {
	        
	    }
	});
	if(initvals instanceof Array){
		for(var i = 0; i < initvals.length; i ++){
			$(clone).find(".uiselect").prepend('<option value="'+initvals[i][0]+'">'+initvals[i][1]+'</option>');
		}
	}
	$(clone).find(".uiselect").prop('selectedIndex',0);
	$("#"+divid).append(clone);
}

function addSuperImposedSet(){
	addAnnotationSet("superimposedsets", "superimposedelement", "superimposedcolordiv", "superimposedcolor", $(".superimposedelement").length);
}

function TDColorOffset(){
	return $(".tds_superimposedelement").length+$(".tdt_superimposedelement").length;
}

function addTDSource(){
	addAnnotationSet("tds_superimposedsets", "tds_superimposedelement", "tds_superimposedcolordiv", "tds_superimposedcolor", TDColorOffset(), [["-1", "None"]]);
}

function addTDTarget(){
	addAnnotationSet("tdt_superimposedsets", "tdt_superimposedelement", "tdt_superimposedcolordiv", "tdt_superimposedcolor", TDColorOffset(), [["-1", "None"]]);
}

function addTDSE(){
	addAnnotationSet("td_sedges", "td_sedgeselement", "td_secolordiv", "td_secolor", $(".td_sedgeselement").length);
}

function addESE(){
	addAnnotationSet("e_sedges", "e_sedgeselement", "e_sedgescolordiv", "e_sedgescolor", $(".e_sedgeselement").length);
}


