
var SNE  = {

	language : {

		languageName : "SNEModules",
		
	},
	
	// List of repositories html element, repositoryName, label and sesameURL which we wanted to use in current editor. Modules/forms will be generated automatically for this.
	repositories : [
	                {el : "module-cdl",  			repositoryName : "CinegridOWL", 		label : "Cinegrid OWL",		sesameURL: "http://ducktape.wongiseng.com/openrdf-sesame"},
	               // {el : "module-ndl_topology", 	repositoryName : "sne_ndl_topology",	label : "NDL Topology",		sesameURL: "http://ducktape.wongiseng.com/openrdf-sesame"},
	                {el : "module-ndl_layer", 		repositoryName : "sne_ndl_layer",		label : "NDL Layer",		sesameURL: "http://ducktape.wongiseng.com/openrdf-sesame"},
	                {el : "module-ndl_capability", 	repositoryName : "sne_ndl_capability",	label : "NDL Capability",	sesameURL: "http://ducktape.wongiseng.com/openrdf-sesame"},
	                {el : "module-ndl_domain", 		repositoryName : "sne_ndl_domain",		label : "NDL Domain",		sesameURL: "http://ducktape.wongiseng.com/openrdf-sesame"},
	                {el : "module-ndl_physical", 	repositoryName : "sne_ndl_physical",	label : "NDL Physical",		sesameURL: "http://ducktape.wongiseng.com/openrdf-sesame"},
	                {el : "module-ndl_vpn",			repositoryName : "ndl_vpn_rdf",		label : "NDL VPN ",			sesameURL: "http://ducktape.wongiseng.com/openrdf-sesame"},
	                {el : "module-wgs84", 			repositoryName : "wgs84_pos",		label : "WGS84 Position",	sesameURL: "http://ducktape.wongiseng.com/openrdf-sesame"},
	                {el : "geyser",		 			repositoryName : "Geyser",			label : "Geyser",			sesameURL: "http://ducktape.wongiseng.com/openrdf-sesame"}

	    //{el : "module-category-pizza", 	repositoryName : "Pizza",				label : "Pizza Ontology",	sesameURL: "http://ducktape.wongiseng.com/openrdf-sesame"},
	    //{el : "module-category-qosawf", 	repositoryName : "sne_cine_qosawf",		label : "QoS AWF",			sesameURL: "http://ducktape.wongiseng.com/openrdf-sesame"}
	],
    
	/**
	 * @method initialization function
	 * @static
	 */
	ready : function() {
		var Event = YAHOO.util.Event;
		
		/**
		 * Generates the module trees on the left side which will be rendered within accordionView 
		 */
		this.moduleTrees = {}
	
		for(i=0;i<this.repositories.length;i++){
			
			this.moduleTrees[i] = new SNEModTree({	
				HTML_ELEMENT 	: this.repositories[i].el,    
				REPOSITORY_NAME : this.repositories[i].repositoryName,
				SESAME_URL		: this.repositories[i].sesameURL 
			});
			
			Event.onDOMReady(this.moduleTrees[i].init,  this.moduleTrees[i], true);
		}		
		
		
		/**
		 * NGAWUR: Periksa lagi apa bener begeneh
		 */
		this.theUploader = new SNEOWLUploader();
		Event.onDOMReady(this.theUploader.init, this.theUploader,true);
	},
	
	pokeCallBack : {
    	success : function (o){
    		this.ready();
      	},
     	
     	failure : function(){
     		alert("Failed to start backend");
     		return;
     	}
	}, 
	
	registerResizePanel : function(){
		var Event = YAHOO.util.Event;
		/**
		 * FIXME: This resizing the tab view, probably this is better done at CSS level.
		 */
		var resizePanel = function(){
			var strHeight = document.getElementById("layout-doc").style.height;			
			strHeight = strHeight.substr(0,strHeight.length-2); // removing suffix px.
			var newHeight = (parseInt(strHeight) - 105)+"px"; 
			document.getElementById("graph-content").style.height  = newHeight;
			document.getElementById("owl-content")  .style.height  = newHeight;
			//document.getElementById("mod-content")  .style.height  = newHeight;
			//document.getElementById("wire-content") .style.height  = newHeight;
		};
		
		// Registers the resize onDomReady and window resize
		Event.onDOMReady(resizePanel, this);
		Event.addListener(window, 'resize', resizePanel);
	},
	
	init : function(){
		
		this.editor = new SNE.WiringEditor(this.language);
		
		this.registerResizePanel();
		
		this.pokeCallBack.scope = this;
		YAHOO.util.Connect.asyncRequest('GET', "rest/module/poke", this.pokeCallBack); 
	}

};
