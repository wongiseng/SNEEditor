/*
 * In order to be able to use JSONP, here we are mixing a bit of codes from YUI 3 into current YUI2.
 */
SNEModTree = function(options) {
	
		this.SESAME_URL 		= options.SESAME_URL 		|| "http://dev.adaptivedisclosure.org";
		this.REPOSITORY_NAME 	= options.REPOSITORY_NAME 	|| "NOVI-IM";
		this.HTML_ELEMENT 		= options.HTML_ELEMENT 		|| "module-category-cdl";
		
		this.tree = null;
		this.loadingNode = null;
		this.ddNodes = [];
		this.SNEGeneratedModules = null;
		
		// Not really loading from AIDA
		this.loadTreeRootFromAIDA = function(root, initTreeFunction){
			
			var rootCallback = {on : {
										success : function(o){
											//console.log(o.responseText);
											initTreeFunction(YAHOO.lang.JSON.parse(o.responseText), root, this);
										},
										failure : function(){
											alert("Failed to initialize tree");
										},
										scope : this
					   		  		},
					   		  	context:this
							   };
			
			var requestString = SNEUtil.getRootQS(this.SESAME_URL, this.REPOSITORY_NAME);
			
			
			YAHOO.util.Connect.asyncRequest( 'GET', 
					"rest/module/root?sesameURL=http://dev.adaptivedisclosure.org/openrdf-workbench&repositoryName="+this.REPOSITORY_NAME, rootCallback.on); 
			
		} // end loadRootTree 
		
		/**
		 * For dynamic loading of the tree nodes.
		 */
		this.loadNodeData  = function (node, fnLoadComplete)  {
			
			var curScope = this.tree.scope;
			
			var narrowerCallback =  
				{on : {
							success : function(results){
				   					for(i=0;i<results.length;i++){
				   					   // Last true parameter means we wanted tree to be expanded
				   				       var curNode = new YAHOO.widget.TextNode(results[i].text, node, true);
				                       curNode.id = results[i].id;
				                       curNode.expand();
				   					}
				   					fnLoadComplete();
				   					curScope.makeDraggable();
			   				},
							failure : function(){
									alert("Failed to expand node");
									fnLoadComplete();
							},
							context : this
			   		  } 
			   	 }; 
			
			var requestString = SNEUtil.getNarrowerQS(node, curScope.SESAME_URL, curScope.REPOSITORY_NAME);
			
	    	Y = YUI().use("jsonp", "substitute", "transition",function (Y) {;
		    	var myRequest =  new Y.JSONPRequest(requestString, narrowerCallback);    
		    	myRequest.send();
	    	});
		} // end loadNodeData
	   
		this.initializeRootTree = function (rootNodes, root, scope){
		        for (var i=0, j=rootNodes.length; i<j; i++) {
		            if(rootNodes[i].term.match("^_")) continue;
		        	var curNode = new YAHOO.widget.TextNode(rootNodes[i].term, root, true);
		            curNode.id = rootNodes[i].id;
		        }
		        // render tree with these toplevel nodes; all descendants of these
		        // will be generated as needed by the dynamic loader.
		        scope.tree.render();
		        scope.makeDraggable();  
		} // end initializeRootTree
		
		this.makeDraggable = function(){
			for (var i = 0, l = this.ddNodes.length;i < l;i++) {
				this.ddNodes[i].unreg();
			}
			this.ddNodes = [];
			var nodes = this.tree.getNodesBy(function(){return true;});
			if (nodes) {
				for (i = 0,l = nodes.length;i<l;i++) {
			        var ddProxy = new WireIt.ModuleProxy(nodes[i].getContentEl(), SNE.editor);
			        ddProxy ._module = SNE.editor.modulesByID[nodes[i].id];
			        this.ddNodes.push(ddProxy);
			        
				}
			}
		} // end MakeDraggable	
	

		this.moduleGeneratorCallBack = {
	        // Successful XHR response handler
	        success : function (o) {
	            var messages = [];
	            
	           	// Remove loading node
	            if(this.loadingNode)
	            	this.tree.popNode(this.loadingNode);
	            
	            // Use the JSON Utility to parse the data returned from the server
	            try {

	            	this.SNEGeneratedModules = YAHOO.lang.JSON.parse(o.responseText);
	                
	                // This will be useful later on for rendering saved modules;
	                // Ini sekarang harusnya di append, bukan cuman direplace.
	                if(SNE.editor.modulesByID == null){
	                	SNE.editor.modulesByID={};
	                }
	                
	                // Ditampung dulu ke tempat yang global
	                for(var id in this.SNEGeneratedModules){
	                	SNE.editor.modulesByID[id] = this.SNEGeneratedModules[id];
	                }

	                // turn dynamic loading on for entire tree using loadNodeData, with currentIconMode = 1
	                this.tree.setDynamicLoad(this.loadNodeData, 1);
	                
	                // start to get tree root from aida
	                this.loadTreeRootFromAIDA(this.tree.getRoot(), this.initializeRootTree);
	
	            }
	            catch (x) {
	                alert("JSON Parse failed! "+x);
	                return;
	            }
	        },
	        failure : function(){
	        	alert("Something wrong while loading module definitions from repository");
	        }
	     } // End moduleGeneratorCallBack
	
	     this.init = function() {

		   	   
	    	// Draw tree with loading nodes first before long call to retrieve modules
	    	this.tree = new YAHOO.widget.TreeView(this.HTML_ELEMENT);
	    	this.loadingNode = new YAHOO.widget.HTMLNode(SNEUtil.loadingMessage("Generating form fields"), this.tree.getRoot(), false, false);
	    	this.tree.render();
	    	
	    	// Some scoping to help me get hold of this object when Ajaxing
	    	this.tree.scope = this;   // This one is used in loadNodeData, getting narrower when tree node is expanded
	    	//Setup proper scope just within this ModTree
	    	this.moduleGeneratorCallBack.scope = this;
	    	   
	 	   	// Retrieve all modules information modules and then later on start building tree
	 	   	YAHOO.util.Connect.asyncRequest('GET', "rest/module/get?sesameURL=http://dev.adaptivedisclosure.org/openrdf-workbench&repositoryName="+this.REPOSITORY_NAME, this.moduleGeneratorCallBack); 
	 	   	
	     } // end Init()
	   
};

