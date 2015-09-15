$(function(){
	$("#pann, #pans, #pane, #panw, #zoomin, #zoomout, #subgraph, #reset, #searchcomponents, #componentenrichment, #exportnetwork, #exportmhdialog, #exportmh").button();
	
	$("#layoutselect").change( function(){
		$('#network').cytoscape('get').layout(getNetworkLayout()); });
	
	$("#pann").click(function(){
		var cy = $('#network').cytoscape('get');
		cy.panBy({ x: 0, y: -25 });
	});
	
	$("#pans").click(function(){
		var cy = $('#network').cytoscape('get');
		cy.panBy({ x: 0, y: 25 });
	});
	
	$("#pane").click(function(){
		var cy = $('#network').cytoscape('get');
		cy.panBy({ x: 25, y: 0 });
	});
	
	$("#panw").click(function(){
		var cy = $('#network').cytoscape('get');
		cy.panBy({ x: -25, y: 0 });
	});
	
	$("#zoomin").click(function(){
		var cy = $('#network').cytoscape('get');
		cy.zoom({
			level: cy.zoom()*1.1,
			renderedPosition: { x: cy.width()/2, y: cy.height()/2 }
		});
	});
	
	$("#zoomout").click(function(){
		var cy = $('#network').cytoscape('get');
		cy.zoom({
			level: cy.zoom()/1.1,
			renderedPosition: { x: cy.width()/2, y: cy.height()/2 }
		});	
	});
	
	$("#subgraph").click(function(){
		var cy = $('#network').cytoscape('get');
		cy.load(cy.$('node:selected'));
	});
	
	$("#reset").click(function(){
		var cy = $('#network').cytoscape('get');
		cy.reset();
	});
	
	$("#nlabels").change(function(){
		setNodeLabels();
	});
	
	$("#elabels").change(function(){
		setEdgeLabels();
	});
	
	$("#searchcomponents").click(function(){
		var searchterm = $("#searchterm").val();
		search(searchterm);
	});
	
	$("#componentenrichment").click(function(){
		GOEnrichment();
	});
	
	$("#exportimagedialog").dialog({
		width: 800,
		height: 600,
		autoOpen: false,
		modal: true,
		close: function(){
			$("#exportimage-image").attr('src', '');
		}
	});
	$("#exportimagedialog").parent().find(".ui-dialog-titlebar-close").css("display", "inline");


	$("#exportimagebutton").button();
	$("#exportimagebutton").click(function(){
		var cy = $('#network').cytoscape('get');
		var imgdata = cy.png({scale: 10});
		$("#exportimage-image").attr('src', imgdata);
		$("#exportimagedialog").dialog("open");
	});
	
	$("#searchresults").hide();

	$("#searchclose").click(function(e){
		e.preventDefault();
		$("#searchresults").hide();
	});
	
	
	$("#exportnetwork").click(function(){
		$("#exportnetwork_network").val(ccidata.network);
		$("#exportnetwork_minsize").val(ccidata.minsize);
		$("#exportnetwork_maxsize").val(ccidata.maxsize);

		$("#exportnetworkform").submit();
	});
	
	
	$("#exportminhopdialog").dialog({
		autoOpen: false,
		modal: true,
		width: 'auto',
		height: 'auto',
		open: function(){
			$("#minhoptarget").empty();
			$("#minhoptarget").append('<option value="promoter">Gene Promoters</option>');
			$("#minhoptarget").append($(".superimposedsets").first().children().slice(1).clone());

		}
	});
	
	$("#exportminhopdialog").parent().find(".ui-dialog-titlebar-close").css("display", "inline");

	$("#exportmhdialog").click(function(){
		$("#exportminhopdialog").dialog("open");
	});
	
	
	$("#exportmh").click(function(){
		$("#exportmh_network").val(ccidata.network);
		$("#exportmh_genes").val(JSON.stringify(ccidata.genes));
		$("#exportmh_diseases").val(JSON.stringify(ccidata.diseases));
		$("#exportmh_regions").val(JSON.stringify(ccidata.regions));
		$("#exportmh_snps").val(JSON.stringify(ccidata.snps));
		$("#exportmh_minsize").val(ccidata.minsize);
		$("#exportmh_maxsize").val(ccidata.maxsize);
		$("#exportmhform").submit();
		$("#exportminhopdialog").dialog("close");
	});
	
	$("#loadsph").click(function(){
		$.ajax({
			url : "shortestpathheatmap",
			data: {
				network: ccidata.network,
				genes: ccidata.genes,
				diseases: ccidata.diseases,
				regions: ccidata.regions,
				snps: ccidata.snps,
				minsize: ccidata.minsize,
				maxsize: ccidata.maxsize
			},
			dataType : "json",
			cache: false,
			type : "post",
			}).success(function(data) {
				$("#sph").empty().append('<img src="data:image/png;base64,'+data+'">');
			}).error(function(req, status, error) {
				//TODO
			});
	});
	
	$("#exportca").click(function(){
		$("#exportca_network").val(ccidata.network);
		$("#exportca_genes").val(JSON.stringify(ccidata.genes));
		$("#exportca_diseases").val(JSON.stringify(ccidata.diseases));
		$("#exportca_regions").val(JSON.stringify(ccidata.regions));
		$("#exportca_snps").val(JSON.stringify(ccidata.snps));
		$("#exportca_minsize").val(ccidata.minsize);
		$("#exportca_maxsize").val(ccidata.maxsize);
		$("#exportcaform").submit();
	});
	
	$("#gotoccb").button();
	$("#gotoccb").click(function(){
		var cc = parseInt($("#gotocc").val().trim());
		if(!isNaN(cc)){
			setCC(cc-1);
		}
	})
});

var setNodeLabels = function(){
	var cy = $('#network').cytoscape('get');
	var nodes = cy.nodes();
	nodes.toggleClass("hidelabel", !$("#nlabels").prop("checked"));
}


var setEdgeLabels = function(){
	var cy = $('#network').cytoscape('get');
	var edges = cy.nodes().connectedEdges();
	edges.toggleClass("hidelabel", !$("#elabels").prop("checked"));
}

var getNetworkLayout = function(){
	var val = parseInt($("#layoutselect").val());
	
	var options;
	var animate = false;
	
	switch(val){
		case 1:
			options = {name: 'random', animate: animate}
		break;
		
		case 2:
			options = {name: 'grid', animate: animate}
		break;
		
		case 3:
			options = {name: 'breadthfirst', animate: animate}
		break;
		
		case 4:
			options = {name: 'dagre', animate: animate}
		break;
			
		case 5:
			options = {name: 'circle', animate: animate}
		break;
			
		case 6:
			options = {name: 'concentric', animate: animate}
		break;
			
		case 7:
			options = {name: 'cose', animate: animate};
			break;
			
		case 8:
			options = {name: 'arbor', animate: animate};
			break;
			
		case 9:
			options = {name: 'springy', animate: animate};
			break;
			
		case 10:
			options = {name: 'cola', animate: animate};
		break;
		
		default:
			options = {name: 'concentric', animate: animate};
	}
	return options;
}

var setCCLinks = function(ui, ccids, index){
	var max = ccids.length-1;
	if(max == -1){
		$(ui).empty().append('<span class="currentcc">0</span>');
	}
	
	var createLink = function(li){
		return '<a href="javascript:setCC('+li+')" class="cclink">'+(li+1)+'</a>';
	}
	
	var beforestart = Math.max(index-4, 0);
	var beforeend = index-1;
	
	var afterstart = index+1;
	var afterend = Math.min(index+4, max);
	
	var html = "";
	
	if(beforestart > 0){
		html += createLink(0);
		if(beforestart > 1){
			html += '...';
		}
	}
	
	for(var i = beforestart; i <= beforeend; i++){
		html += createLink(i);
	}
	
	html += '<span class="currentcc">'+(index+1)+'</span>'
	
	for(var i = afterstart; i <= afterend; i++){
		html += createLink(i);
	}
	
	if(afterend < max){
		if(afterend < max-1){
			html += '...';
		}
		html += createLink(max);
	}
	$(ui).empty().append(html);
}

function getGeneIds(){
	var nodes = networkdata.nodes;
	var genes = [];
	for(var i = 0; i < nodes.length; i++){
		var names = nodes[i].geneids;
		for(var j = 0; j < names.length; j++){
			var gene = names[j];
			if(genes.indexOf(gene) == -1){
				genes.push(gene);
			}
		}
	}
	return genes;
}

function GOEnrichment(){
	$("#goenrichment").empty()
	if(networkdata && networkdata != null){
		var genes = getGeneIds();

		$("#goenrichment").append("Loading...");
		if(genes.length > 0){
			var input = { geneids: genes }
			$.ajax({
				url : "go",
				data: input,
				dataType: "json",
				type : "POST"
			}).success(function(data){
				$("#goenrichment").empty();
				var html = '<div class="enrichmenttable">';
				html += '<div class="enrichmentrow">';
					html += '<div class="enrichmentcol ui-state-active networkmenu-header">&nbsp;</div>';
					html += '<div class="enrichmentcol ui-state-active networkmenu-header">GO ID</div>';
					html += '<div class="enrichmentcol ui-state-active networkmenu-header">Description</div>';
					html += '<div class="enrichmentcol ui-state-active networkmenu-header">Genes In GO Term</div>';
					html += '<div class="enrichmentcol ui-state-active networkmenu-header">Genes In Component</div>';
					html += '<div class="enrichmentcol ui-state-active networkmenu-header">Expected</div>';
					html += '<div class="enrichmentcol ui-state-active networkmenu-header">Fisher</div>';
				html += '</div>';
				var l = data.goids.length;
				for(var i = 0; i < l; i++){
					var id = data.goids[i];
					var label = data.terms[i];
					var numref = data.genesinterm[i];
					var numlist = data.genesincomponent[i];
					var expected = data.expected[i];
					var pvalue = data.fisher[i];
					go2gene = data.go2gene;
					if(id){
						html += '<div class="enrichmentrow">';
						html += '<div class="enrichmentcol"><a href="javascript:groupNodes(\''+id+'\',\''+i+'\');"">View</a>&nbsp;</div>';
						html += '<div class="enrichmentcol"><a href="http://amigo.geneontology.org/amigo/term/'+id+'" target="_blank">'+id+'</a></div>';
						html += '<div class="enrichmentcol">'+label+'</div>';
						html += '<div class="enrichmentcol">'+numref+'</div>';
						html += '<div class="enrichmentcol">'+numlist+'</div>';
						html += '<div class="enrichmentcol">'+expected+'</div>';
						html += '<div class="enrichmentcol">'+pvalue+'</div>';
						html += '</div>';
					}
				}
				html += '</div>';
				$("#goenrichment").append(html);
			}).error(function(){
				$("#goenrichment").empty().append("Server Error.");
			});		
		}

	}
}

function groupNodes(goid, index){
	var geneids = go2gene[index];
	var nodes = networkdata.nodes;
	var nodeids = [];
	for(var i = 0; i < nodes.length; i++){
		var ids = nodes[i].geneids;
		for(var j = 0; j < ids.length; j++){
			var geneid = ids[j];
			if(geneids.indexOf(geneid) != -1){
				nodeids.push(nodes[i].id)
				break;
			}
		}
	}
	visualizeNetwork(getCytoscapeNetwork(networkdata, true, goid, nodeids));

}

function selectSearch(nodeid, ccid){
	var n = "n_"+nodeid;
	var callback = function(){
		var cy = $("#network").cytoscape('get');
		var nodes = cy.nodes();
		for(var k = 0; k < nodes.length; k++){
			var nextid = nodes[k].id();
			if(nextid == n){
				nodes[k].select();
			}
		}
	}
	setCC(ccid, callback);
}

function search(searchterm){
	$("#searchresultsresults").empty();
	ccidata.search = searchterm;
	
	$.ajax({
		url : "search",
		data: ccidata,
		dataType : "json",
		cache: false,
		type : "post",
		}).success(function(data) {
			var nodeids = [];
			var components = [];
			for(var i = 0; i < data.length; i++){
				for(var j = 0; j < ccids.length; j++){
					if(data[i][0] == ccids[j]){
						nodeids.push(data[i][1])
						components.push(j);
					}
				}
			}
			
			if(nodeids.length == 0){
				$("#searchresultsresults").append("No results.");
			}
			else{
				for(var i = 0; i < nodeids.length; i++){
					var link = '<a href="javascript:selectSearch('+nodeids[i]+', '+components[i]+')">Component '+(components[i]+1)+'</a><br/>';
					$("#searchresultsresults").append(link);
				}
			}
			$("#searchresults").show();

		}).error(function(req, status, error) {
			//TODO
		});
}