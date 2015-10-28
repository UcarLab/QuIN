var ccids = [];
var ccidata;
var superimposeindex;
var lastrequest;

var networkdata = null;
var vnetworkdata = null;
var go2gene = null;

var networkinfo = null;

function setCC(ccid, callback){
	setCCLinks("#cclinks", ccids, ccid);
	ccidata.ccid = ccids[ccid];
	
	$("#network").empty().append("Loading...");
	
	if(lastrequest){
		lastrequest.abort();
	}
	lastrequest = $.ajax({
		url : "getcc",
		data: ccidata,
		dataType : "json",
		cache: false,
		type : "post",
		}).success(function(data) {
			networkdata = data;
			vnetworkdata = networkdata;
			gene2go = null;
			visualizeNetwork(getCytoscapeNetwork(networkdata, true), callback);
			$("#componentinfo").empty();
			$("#componentinfo").append("<div>Nodes:"+networkdata.nodes.length+"</div>");
			$("#componentinfo").append("<div>Edges:"+networkdata.edges.length+"</div>");
			
		}).error(function(req, status, error) {
			//TODO
		});
	
	$("#goenrichment").empty().append("&nbsp;");
}

var getSquishedNetwork = function(data){
	var nodes = data.nodes.slice(0);
	var edges = data.edges.slice(0);
	
	var n1edges = new Array();
	for(var i = 0; i < edges.length; i++){
		n1edges.push(edges[i].node1);
	}
	
	var n2edges = new Array();
	for(var i = 0; i < edges.length; i++){
		n2edges.push(edges[i].node2);
	}
	
	var labels = new Array();
	var la = new Array();
	var finalnodes = new Array();
	for(var i = 0; i < nodes.length; i++){
		var cn = nodes[i];
		var label = cn.label;
		if(label != ""){
			var index = labels.indexOf(label);
			if (index == -1) {
				labels.push(label);
				la.push([cn]);
			}
			else{
				la[index].push(cn);
			}
		}
		else{
			finalnodes.push(cn);
		}
	}
	
	for(var i = 0; i < la.length; i++){
		var ca = la[i];
		if(ca.length == 1){
			finalnodes.push(ca[0]);
		}
		else{
			//Merge
			var label = labels[i];
			var id = ca[0].id
			var chr = ca[0].chr;
			var start = ca[0].start;
			var end = ca[0].end;
			//related ids
			var annotations = new Array(datasetlabels.length);
			for(var j = 0; j < annotations.length; j++){
				annotations[j] = false;
			}
			
			for(var j = 0; j < ca.length; j++){
				var sstart = ca[j].start;
				var send = ca[j].end;
				var fa = ca[j].finalannotations
				
				if(fa){
					start = Math.min(sstart, start);
					end = Math.max(send, end);
					for(var k = 0; k < fa.length; k++){
						annotations[datasetlabelindex.indexOf(fa[k][0])] = true;
					}
				
					var sid = ca[j].id;
					var ei = n1edges.indexOf(sid);
					while(ei != -1){
						edges[ei].node1 = id;
						ei = n1edges.indexOf(sid, ei+1);
					}
					
					var ei = n2edges.indexOf(sid);
					while(ei != -1){
						edges[ei].node2 = id;
						ei = n2edges.indexOf(sid, ei+1);
					}
				}
			}
			
			var finalannotations = new Array();
			for(var j = 0; j < annotations.length; j++){
				if(annotations[j] == true){
					finalannotations.push([datasetlabelindex[j], 1]);
				}
			}
			
			finalnodes.push({"id": id, "label": label, "chr": chr, "start": start, "end": end, "finalannotations": finalannotations});
		}
	}
	
	return {"nodes": finalnodes, "edges": edges};
	
}

getCytoscapeNetwork = function(data, merge, parent, nids) {
	if(false){
		data = getSquishedNetwork(data);
	}
	var nodes = data.nodes;
	var edges = data.edges;

	var cnodes = new Array();
	var cedges = new Array();

	
	for (var i = 0; i < nodes.length; i++) {
		var cur = nodes[i];

		var label = ""
			
		var bgcolor = "#ccc";
		var size = "25px";
		var backgroundblacken = 0;
		var borderwidth = "0px";
		if(cur.genesymbols.length > 0){
			label += cur.genesymbols[0];
			for(var j = 1; j < cur.genesymbols.length; j++){
				label += ","+cur.genesymbols[j];
			}
			label += " ";
			//bgcolor = "#909090";
			size = "35px";
			//backgroundblacken = 0.07;
		}
		label += cur.chr+":"+cur.start+"-"+cur.end;
		
		var data = {id : "n_"+cur.id, label: label, bgcolor: bgcolor, size: size }
		
		if(parent && nids.indexOf(cur.id) != -1){
			data.parent = parent;
		}
		
		var fa = cur.finalannotations;
		var facount = fa.length;
		var percent = Math.round(100*(1/facount));
		for(var j = 0; j < facount; j++){
			var cur = fa[j];
			var index = superimposeindex.indexOf(cur[0]);
			if(index > -1){
				data["c"+index] = percent;
			}
		}
		
		cnodes.push({ data: data });

	}
	
	if(parent){
		cnodes.push({ data: {id : parent, label: parent, bgcolor: "#ccc", size: "50px"} })
	}

	var minpet = 1;
	var maxpet = 1;
	
	if(edges.length > 0){
		minpet = edges[0].petcount;
		maxpet = edges[0].petcount;
		for(var i = 1; i < edges.length; i++){
			minpet = Math.min(minpet, edges[i].petcount)
			maxpet = Math.max(maxpet, edges[i].petcount)
		}
	}
	var diff = maxpet-minpet;
	if(minpet == maxpet){
		diff=1;
	}

	for (var i = 0; i < edges.length; i++) {
		var cur = edges[i];
		var weight = parseInt((1+Math.round(9*((cur.petcount-minpet)/diff))));
		cedges.push({ data: {
			id: "e_"+cur.id,
			label : cur.petcount+" ("+cur.interactioncount+")",
			source : "n_"+cur.node1,
			target : "n_"+cur.node2,
			weight : weight
		}});
	}

	return {
		nodes : cnodes,
		edges : cedges
	};
};

function visualizeNetwork(ndata, callback){
	$("#network").empty();
	  var nodescss = {
		        'content': 'data(label)',
		        'background-color': 'data(bgcolor)',
		        'width': 'data(size)',
		        'height': 'data(size)',
		        'font-size': '0.72em',
		        'text-valign': 'center',
		        'text-halign': 'center',        
			    'pie-size': '100%',
		      }

	for(var i = 0; i < superimposeindex.length; i++){
		nodescss['pie-'+(i+1)+'-background-color'] = colorindex[i];
	    nodescss['pie-'+(i+1)+'-background-size'] = 'mapData(c'+i+', 0, 100, 0, 100)';
	}
	  
	var cy = cytoscape({
		  container: document.getElementById('network'),
		
		  style: [
		    {
		      selector: 'node',
		      css: nodescss
		    },
		    {
		      selector: '$node > node',
		      css: {
		        'padding-top': '10px',
		        'padding-left': '10px',
		        'padding-bottom': '10px',
		        'padding-right': '10px',
		        'text-valign': 'top',
		        'text-halign': 'center'
		      }
		    },
		    {
		      selector: '.highlight',
		      css: {
		        'line-color': '#88aadd'
		      }
		    },
		    {
			      selector: '.hidelabel',
			      css: {
			        'text-opacity': 0
			      }
			    },
		    {
		      selector: 'edge',
		      css: {
		    	  'content': 'data(label)',
			      'font-size': '0.42em',
			      'width': 'data(weight)',
		      }
		    },
		    {
		      selector: ':selected',
		      css: {
		    	'border-color': 'black',
		    	'border-width': '2px',
		    	'line-color': 'black',
		        'text-outline-color': '#ccc',
		        'text-outline-width': 0.5
		      }
		    }
		  ],
		  
				elements: {
				  nodes: ndata.nodes, 
				  edges: ndata.edges
				},
		  
		  layout: getNetworkLayout(),
		  ready: callback
				
				
		});
	
		cy.$('node').on('select', function(e){
		  var ele = e.cyTarget;
		  ele.connectedEdges().toggleClass("highlight", true);
		});
		
		cy.$('node').on('unselect', function(e){
			  var ele = e.cyTarget;
			  ele.connectedEdges().toggleClass("highlight", false);
			});
	
		cy.$('node').on('cxttap', function(e){
		  var ele = e.cyTarget;
		  setNodeInfo(ele.id().split("_")[1]);
		});
		
		cy.$('edge').on('cxttap', function(e){
			  var ele = e.cyTarget;
			  setEdgeInfo(ele.id());
		});
		
		setNodeLabels();
		setEdgeLabels();


}

function setLegend(ui, siindex){
	$(ui).empty();
	for(var i = 0; i < siindex.length; i++){
		$(ui).append('<div class="legendelement"><div class="legendcolorcontainter"><div class="legendcolor" style="background: '+colorindex[i]+';"></div></div><div class="legendlabel">'+datasetlabels[datasetlabelindex.indexOf(siindex[i])]+'</div></div>');
	}
}

function setCCIds(pccids, sidata){
	ccids = pccids;
	ccidata = sidata;
	
	setSuperImposeIndex(sidata);
	setLegend("#legend", superimposeindex);
	setCCLinks("#cclinks", ccids, 0);
	
	if(ccids.length > 0){
		setCC(0);
		setCentrality();
	}
	else{
		$("#network").empty().append("No components found that match the explore network criteria.");

	}
	
}

function setNetworkInfoHTML(ui, data){
	networkinfo = data;
	$("#networkinfo").empty();
	$("#networkinfo").append("<div>Connected Components:"+data[0]+"</div>");
	$("#networkinfo").append("<div>Nodes:"+data[1]+"</div>");
	$("#networkinfo").append("<div>Edges:"+data[2]+"</div>");
	
	$("#ns_comp").empty().append(data[0].toString().replace(/\B(?=(\d{3})+(?!\d))/g, ","));
	$("#ns_node").empty().append(data[1].toString().replace(/\B(?=(\d{3})+(?!\d))/g, ","));
	$("#ns_edge").empty().append(data[2].toString().replace(/\B(?=(\d{3})+(?!\d))/g, ","));
	$("#ns_avgpet").empty().append(data[3].toFixed(2));
	
	$("#ns_extend").empty().append(Math.max(data[4],0));
	$("#ns_minpet").empty().append(data[5]);
	$("#ns_intra").empty().append(data[6]);
	var inter = parseInt(data[7])
	$("#ns_inter").empty().append(inter > 0 ? "Intra-Chromosome" : inter < 0 ? "Inter-Chromosome" : "Both");
	$("#ns_mincompsize").empty().append(ccidata.minsize);
	$("#ns_maxcompsize").empty().append(ccidata.maxsize);

}

function setSuperImposeIndex(sidata){
	superimposeindex = [];
	
	var regions = sidata.regions;
	var genes = sidata.genes;
	var diseases = sidata.diseases;
	var snps = sidata.snps;
	
	for(var i = 0; i < regions.length; i++){
		superimposeindex.push('1_'+regions[i]);
	}
	for(var i = 0; i < genes.length; i++){
		superimposeindex.push('2_'+genes[i]);
	}
	for(var i = 0; i < diseases.length; i++){
		superimposeindex.push('3_'+diseases[i]);
	}
	for(var i = 0; i < snps.length; i++){
		superimposeindex.push('4_'+snps[i]);
	}
}

function setNetworkInfo(ccidata){
	
	$.ajax({
		url : "getnetworkinfo",
		data: ccidata,
		dataType : "json",
		cache: false,
		type : "post",
		}).success(function(data) {
			setNetworkInfoHTML("#networkinfo", data);
		}).error(function(req, status, error) {
			//TODO
		});
}


function getIndex(array, id){
	for(var i = 0; i < array.length; i++){
		if(id == array[i].id){
			return i;
		}
	}
}

function setNodeInfo(nid){
	var nodes = vnetworkdata.nodes;
	var id = getIndex(nodes, nid);
	
	var nnodesm1 = networkinfo[0]-1;
	var nnodesm2 = nnodesm1-1;
	var nnpairs = (nnodesm1*nnodesm2);

	var cnodesm1 = (networkdata.nodes.length-1);
	var cnodesm2 = cnodesm1-1;
	var cnpairs = (cnodesm1*cnodesm2);
	if(id != null){
		var cn = nodes[id];
		var chr = cn.chr;
		var start  = cn.start;
		var end = cn.end;
		
		var link = 'https://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&position='+chr+'%3A'+start+'-'+end
		$("#nlocationlink").empty().append('<a href="'+link+'" target="_blank">'+chr+" "+start+" - "+end+'</a></div>')
		$("#npetcount").empty().append(cn.petcount);
		$("#nintercount").empty().append(cn.interactioncount);
		
		$("#nnondegree").empty().append((cn.degree));
		$("#nnoncloseness").empty().append((cn.closeness));
		$("#nnonharmonic").empty().append((cn.harmonic));
		$("#nnonbetweenness").empty().append((cn.betweenness)/2); //Betweenness is calculated assuming directed, so divide by 2 here
		
		$("#nndegree").empty().append((cn.degree/nnodesm1).toPrecision(4));
		$("#nncloseness").empty().append((cn.closeness*nnodesm1).toPrecision(4));
		$("#nnharmonic").empty().append((cn.harmonic/nnodesm1).toPrecision(4));
		$("#nnbetweenness").empty().append((cn.betweenness/nnpairs).toPrecision(4)); //normalize assuming directed

		$("#ncdegree").empty().append((cn.degree/cnodesm1).toPrecision(4));
		$("#nccloseness").empty().append((cn.closeness*cnodesm1).toPrecision(4));
		$("#ncharmonic").empty().append((cn.harmonic/cnodesm1).toPrecision(4));
		$("#ncbetweenness").empty().append((cn.betweenness/cnpairs).toPrecision(4)); //normalize assuming directed
		
		getSNPTraits(chr, start, end);
	}

	
	$("#nodeinfodialog").dialog("open");
}

function setEdgeInfo(eid){
	var nodes = vnetworkdata.nodes;
	var edges = vnetworkdata.edges;
	var id = getIndex(edges, eid.split("_")[1]);
	
	if(id != null){
		var ce = edges[id];
		
		var cn = nodes[getIndex(nodes, ce.node1)];
		var chr = cn.chr;
		var start = cn.start;
		var end = cn.end;
		var link = 'https://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&position='+chr+'%3A'+start+'-'+end;
		$("#elocationlink1").empty().append('<a href="'+link+'" target="_blank">'+chr+" "+start+" - "+end+'</a></div>')
		
		cn = nodes[getIndex(nodes, ce.node2)];
		var chr2 = cn.chr;
		var start2 = cn.start;
		var end2 = cn.end;
		
		if(chr == chr2){
			var sstart = Math.min(start,start2);
			var send = Math.max(end, end2);
			link = 'https://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&position='+chr+'%3A'+sstart+'-'+send;
			$("#elocationospan").empty().append('<a href="'+link+'" target="_blank">'+chr+" "+sstart+" - "+send+'</a></div>')
			
			sstart = Math.min(end,end2);
			send = Math.max(start, start2);
			link = 'https://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&position='+chr+'%3A'+sstart+'-'+send;
			$("#elocationispan").empty().append('<a href="'+link+'" target="_blank">'+chr+" "+sstart+" - "+send+'</a></div>')
			
		}
		else{
			$("#elocationospan").empty().append('Interchromosome')
			$("#elocationispan").empty().append('Interchromosome')

		}

		link = 'https://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&position='+chr2+'%3A'+start2+'-'+end2;	
		$("#elocationlink2").empty().append('<a href="'+link+'" target="_blank">'+chr2+" "+start2+" - "+end2+'</a></div>')
		
		$("#epetcount").empty().append(ce.petcount);
		$("#eintercount").empty().append(ce.interactioncount);
	}
	
	$("#edgeinfodialog").dialog("open");
}

function getSNPTraits(chr, start, end){
	$("#nsnps").empty().append('<div class="tcaption">Loading SNP information...</div>');
	var data = {
			chr: chr,
			start: start,
			end: end
	}
	
	$.ajax({
		url : "getsnps",
		data: data,
		dataType : "json",
		cache: true,
		type : "post",
		}).success(function(data) {
			var html = '<div class="tcaption">SNP &amp; Trait Information</div>';
			html += '<div class="tr ui-state-active">';
				html += '<div class="tc label">RefSNP Id</div>';
				html += '<div class="tc label">Chr</div>';
				html += '<div class="tc label">Start</div>';
				html += '<div class="tc label">End</div>';
				html += '<div class="tc label">GWAS</div>';
				html += '<div class="tc label">Clinvar (May not be validated)</div>';
			html += '</div>';
			$("#nsnps").empty().append(html);
			
			for(var i = 0; i < data.length; i++){
				var cur = data[i];
				var html = 	'<div class="tr">';
				html += '<div class="tc"><a href="http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?rs='+cur.rsid+'" target="_blank">'+cur.rsid+'</a></div>';
				html += '<div class="tc">'+cur.chr+'</div>';
				html += '<div class="tc">'+cur.start+'</div>';
				html += '<div class="tc">'+cur.end+'</div>';
				var gtraitinfo = '&nbsp;';
				if(cur.pmid && cur.pmid != null){
					gtraitinfo = '<a href="http://www.ncbi.nlm.nih.gov/pubmed/'+cur.pmid+'" target="_blank">'+cur.gwastrait+'</a>';
				}
				html += '<div class="tc">'+gtraitinfo+'</div>';
				var traitinfo = '&nbsp;';
				if(cur.cvid && cur.cvid != null){
					traitinfo = '<a href="http://www.ncbi.nlm.nih.gov/clinvar/?term=rs'+cur.rsid+'" target="_blank">'+cur.trait+'</a>';
				}
				html += '<div class="tc">'+traitinfo+'</div>';
				html += '</div>';
				$("#nsnps").append(html);
			}
		})
		
		
}


function setCentrality(){

	$("#degreeboxplot").empty().append("");
	$("#closenessboxplot").empty().append("");
	$("#harmonicboxplot").empty().append("");
	$("#betweennessboxplot").empty().append("");
}

$(function(){
	$("#loadnm").click(function(){
		ccidata.labels = [];
		for(var i = 0; i < ccidata.regions.length; i++){
			ccidata.labels.push(datasetlabels[datasetlabelindex.indexOf("1_"+ccidata.regions[i])]);
		}
		for(var i = 0; i < ccidata.genes.length; i++){
			ccidata.labels.push(datasetlabels[datasetlabelindex.indexOf("2_"+ccidata.genes[i])]);
		}
		for(var i = 0; i < ccidata.diseases.length; i++){
			ccidata.labels.push(datasetlabels[datasetlabelindex.indexOf("3_"+ccidata.diseases[i])]);
		}
		for(var i = 0; i < ccidata.snps.length; i++){
			ccidata.labels.push(datasetlabels[datasetlabelindex.indexOf("4_"+ccidata.snps[i])]);
		}
		ccidata.measure = 1;
		
		$("#degreeboxplot").empty().append("Loading...");
		$.ajax({
			url : "cboxplot",
			data: ccidata,
			dataType : "json",
			cache: false,
			type : "post",
			}).success(function(data) {
				$("#degreeboxplot").empty().append('<img src="data:image/png;base64,'+data.rBoxPlot+'">');
				$("#degreeboxplot").append(getTable(data.labels, data.mannWhitneyTable));
			}).error(function(req, status, error) {
				//TODO
			});
		
		ccidata.measure = 2;
		
		$("#closenessboxplot").empty().append("Loading...");
		$.ajax({
			url : "cboxplot",
			data: ccidata,
			dataType : "json",
			cache: false,
			type : "post",
			}).success(function(data) {
				$("#closenessboxplot").empty().append('<img src="data:image/png;base64,'+data.rBoxPlot+'">');
				$("#closenessboxplot").append(getTable(data.labels, data.mannWhitneyTable));
			}).error(function(req, status, error) {
				//TODO
			});
		
		ccidata.measure = 3;
		
		$("#harmonicboxplot").empty().append("Loading...");
		$.ajax({
			url : "cboxplot",
			data: ccidata,
			dataType : "json",
			cache: false,
			type : "post",
			}).success(function(data) {
				$("#harmonicboxplot").empty().append('<img src="data:image/png;base64,'+data.rBoxPlot+'">');
				$("#harmonicboxplot").append(getTable(data.labels, data.mannWhitneyTable));
			}).error(function(req, status, error) {
				//TODO
			});
		
		ccidata.measure = 4;
		
		$("#betweennessboxplot").empty().append("Loading...");
		$.ajax({
			url : "cboxplot",
			data: ccidata,
			dataType : "json",
			cache: false,
			type : "post",
			}).success(function(data) {
				$("#betweennessboxplot").empty().append('<img src="data:image/png;base64,'+data.rBoxPlot+'">');
				$("#betweennessboxplot").append(getTable(data.labels, data.mannWhitneyTable));
			}).error(function(req, status, error) {
				//TODO
			});
	});
})

function getTable(labels, data){
	var html = '<table border="1" cellpadding="0", cellspacing="0">';
	html += '<tr><td>&nbsp;</td>';
	for(var i = 0; i < labels.length; i++){
		html += '<td>'+labels[i]+'</td>';
	}
	html += '</tr>';
	
	for(var i = 0; i < labels.length; i++){
		html += '<tr><td>'+labels[i]+'</td>';
		for(var j = 0; j < labels.length; j++){
			html += '<td>'+parseFloat(data[i+1][j+1]).toExponential(4)+'</td>';
		}
		html += '</tr>'
	}
	
	html += '</table>'
	return html;
}