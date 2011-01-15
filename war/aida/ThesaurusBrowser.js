// vim: sw=4:ts=4:nu:nospell:fdc=4
/**
* An extended TreePanel which displays the tree-like structure of a repository
*
* @author    Edgar Meij, Adianto Wibisono
* @copyright (c) 2009
* @date      20 March 2009
* @version   $Id$
*
* @license 
*/

// namespaces
Ext.ns('Ext.app');
var Tree = Ext.tree;
  
// application main entry point
Ext.app.ThesaurusBrowser = Ext.extend(Tree.TreePanel, {
	
  initComponent : function() {
  	
  		this.rootID = this.rootID ? this.rootID : Ext.id();
  	
   
  		this.skoslenscombo = new Ext.app.SkoslensCombo({
                  browser : this,
                  baseParams : {
                      ns : this.ns,
                      server_url : this.server_url,
                      repository : this.repository,
                      username : this.username,
                      password : this.password
                  },
                  emptyText : 'Select Skos Lens...'
      
        });

  		var conceptcompl = new Ext.app.ConceptCompletion({
	    	browser : this,
		    baseParams : {
	        ns : this.ns,
	        server_url : this.server_url,
	        repository : this.repository,
	        username : this.username,
	        password : this.password
		    },
		    emptyText : 'Start typing in a concept...'
		}); 

	this.directLink=getBaseURL()+"/search-dev/?server_url="+this.server_url+"&repository_name="+this.repository;

	// This is for maximum worker node on checking for leaves
	this.wait_queue = new Array();
	this.currently_running = 0;
    var bah = this;	
	Ext.apply( this, {
      loader : 
        new Tree.TreeLoader({
          //preloadChildren : true,
          dataUrl : baseURL + '/Services/rest/skoslens/narroweraltsp',
          
          baseParams : {
            ns : '',
            server_url : this.server_url,
            repository : this.repository,
            username : this.username,
            password : this.password,
			top_concept 		: this.top_concept,
			narrower_predicate 	: this.narrower_predicate,
			skos_version		: this.skos_version,
			virtuoso_namedgraph : this.virtuoso_namedgraph,
	        
            json : 'true',
            target : 'ThesaurusBrowser'
          },
          clearOnLoad : false,
          listeners : {
		   beforeload : function(th, n, cb) { // listener, catches empty requests
              var id = '' + n.id; 
			  if (id.startsWith('ext-gen') || id.endsWith('-root')) { 
				  //  Ext.get('loading').remove();
				  //  Ext.get('loading-mask').fadeOut({
				  //    remove : true
				  //  });
				  //
					return false;
              }
              
              // make sure we send the URI as request to the webservice
              if (n.attributes.URI) {
                th.baseParams.uri = n.attributes.URI;
              } else if (n.URI) {
                th.baseParams.uri = n.URI;
              } else {
                th.baseParams.uri = n.id;
              }
			  
			  setUserLensInfo(th);

              if (! th.baseParams.repository || th.baseParams.repository === '' ) {
              	return false;
              }
			  // Avoid background process of checking node leaves to take precedence
			  bah.currently_expanding = 1;
            },
            load: function(th, n, r) {
			  // Release back to checking
			  bah.currently_expanding = 1;
			  bah.attemptChecking();
    
            }
          }
        }),
      closable : true,
      title : getShortURL(this.repository),
	  tabTip: this.directLink,
      animate : false,
      border : false,
      enableDrag : true,
      //ddGroup : 'advquery',
      containerScroll : true,
      autoScroll : true,
      root : new Tree.AsyncTreeNode({
        expanded : false,
        draggable : false,
        iconCls : 'broader-icon',
        id : this.rootID,
        loader : this.loader
      }),
      rootVisible : false,
     

    }
    ); // end apply
    
    Ext.app.ThesaurusBrowser.superclass.initComponent.call(this, arguments);
    
    // listeners
    this.on({
		append : function(tree, dad, node, idx){
				/* when showing root the first time this will be called*/
				//this.checkThisNode(node);
				//setTimeout(this.checkThisNode(node),idx*5000);
				this.wait_queue.push(node);
			  
				// Don't check root
				if(node.id.endsWith("-root"))
					return;
				
				// Check top nodes later
				if(dad.id.endsWith("-root"))
					return;


				//this.attemptChecking();
		}, 

	   	'beforechildrenrendered' : {

    		fn : function(node) {
			node.select();
    			node.eachChild(
		        function(n) {
		          if (n) {
		          	n.plainText = n.text;
		            //n.setText(n.text + ' - <div class="concept-id"> &lt;<i>'+n.id+'</i>&gt;</div>');
                n.setText(n.text);
		          }
		        } 
		      );
    		}
    	},
   
      // Listener - context menu
      'contextmenu' : { 
        fn : function(n, e) {
          n.expand(false);
          
          function deleteNode (item) {
            item.node.remove();
          }
          
					function collapseAll (item) {
						var node = item.node;
						if(node && !node.isLeaf()) {
							node.select();
							node.collapse.defer(1, node, [true]);
						}
					}
          
          function expandAll (item) {
						var node = item.node;
						if ( node && !node.isLeaf() ) {
						  node.select();
						  node.expand.defer(1, node, [true]);
						}
					}
					
          function expandOne (item) {
						var node = item.node;
						if ( node && !node.isLeaf() ) {
							
						  node.select();
							node.eachChild(
				        function(n) {
				          if (n) {
				            n.expand.defer(1, n, [false]);
				          }
				        } 
				      );
						}
					}

          var contextMenu = new Ext.menu.Menu({
            items : [
	            new Ext.menu.Item({
	              id : n.id,
	              disabled : true,
	              //text : n.text,
	              // to also show the id:
	              text : n.text + ' &lt;<i>' + n.id + '</i>&gt;',
	              cls : 'nodename'
	            }), 
	            new Ext.menu.Separator(),
	            new Ext.menu.Item({
	              id : 'expand-one-node',
	              text : 'Expand one level deep',
	              icon : 'icons/expand-all.gif',
	              handler : expandOne,
	              node : n
	            }),
	            new Ext.menu.Item({
	              id : 'expand-all-node',
	              text : 'Expand all children',
	              icon : 'icons/expand-all.gif',
	              handler : expandAll,
	              node : n
	            }),
	            new Ext.menu.Item({
	              id : 'collapse-one-node',
	              text : 'Collapse all children',
	              icon : 'icons/collapse-all.gif',
	              handler : collapseAll,
	              node : n
	            }),
	            new Ext.menu.Separator(), 
	            new Ext.menu.Item({
	              id : 'delete-node',
	              text : 'Remove this concept from the tree',
	              icon : 'icons/chart_organisation_delete.png',
	              handler : deleteNode,
	              node : n
	            })
            ]
          });
          
          contextMenu.show(n.ui.getAnchor());
        }
      }
    });
  },
  clear : function() {
  	
  	var old_root = this.root;
  	
    if (old_root) {
      while(old_root.firstChild) {
        old_root.removeChild(old_root.firstChild);
      }
    }
    
   
  },

  detectAndShow: function(browser){

		var conn = new Ext.data.Connection({ defaultHeaders : { 'Accept': 'application/json' } ,method : 'POST' });

	    browser.root.appendChild(new Ext.tree.AsyncTreeNode({
	      expanded : true,
	      draggable : false,
	      disabled : true,
	      iconCls : 'loading-icon',
	      text : browser.repository,
	      leaf : false,
	      id : browser.repository + '-root',
	      cls : 'focusnode'
	    })); 
	   	 
		conn.request({
	      url: getBaseURL() + '/Services/rest/repository/detectp/',
	      method : 'GET',
	      timeout : 60000,
		  scriptTag : true,
	      params : {
	        ns          : browser.ns,
	        server_url  : browser.server_url,
	        repository  : browser.repository,
	        username    : browser.username,
	        password    : browser.password,
	      },
	      success : function(response, options) {
	        try {
	          var decoded = Ext.util.JSON.decode(response.responseText);
			  /* detectInfo is per repository, avoiding confusion when opening more tabs*/
			  saveState('repository', browser.repository);

			  saveObject('detectInfo'+loadState('repository'),decoded);

			  browser.interpretAndStoreDetectInfo(decoded, browser.server_url+"-"+browser.repository, browser.skoslenscombo); 

			  setUserLensInfo(browser);

			  browser.showRoot(browser);

	        } catch (err) {
	          Ext.MessageBox.alert("Failed to detect", "Failed to detect " + err);
	        }
	      }
	      ,failure : loadFailed
	    });    
  },

  // Helper related to Skos Lens
  // cbb is the skoslens combo box
  // This function stores the current detected values of the respository
  // Saving Current lens information in curLens
  // Setting the state of the id (which refers to the repository) into the current lens
  interpretAndStoreDetectInfo: function(decoded, id, cbb){
					  lensmap = getLensesMap();
					  if(decoded.skosVersion == "Not Skos") {
							if(decoded.skosLens && decoded.skosLens.indexOf("owl")>=0){
								cbb.setValue(["OWL Classes"]);
								saveObject('curLens', lensmap["OWL Classes"]);
								saveState(id, cbb.getValue());
							}
							else {
								cbb.setValue(["RDFS Classes"]);
								saveObject('curLens', lensmap["RDFS Classes"]);
								saveState(id, cbb.getValue());
							}
					  } else {
		
							if(decoded.skosVersion && decoded.skosVersion.indexOf("2004")>=0) {
								cbb.setValue(["SKOS 2004"]);
								saveObject('curLens', lensmap["SKOS 2004"]);
								saveState(id, cbb.getValue());
							}
							else{
								cbb.setValue(["SKOS 2008"]);
								saveObject('curLens', lensmap["SKOS 2008"]);
								saveState(id, cbb.getValue());
							}
					  }
							
  },

  showRoot : function (th) {
  
    if (this.repository !== '') {
  	
	    var repository = this.repository;
	    
	    var old_root = this.root.findChild('id', repository + '-root');
	    if (old_root) {
	      old_root.remove();
	    }
	    
	    this.root.appendChild(new Ext.tree.AsyncTreeNode({
	      expanded : true,
	      draggable : false,
	      disabled : true,
	      iconCls : 'loading-icon',
	      text : repository,
	      leaf : false,
	      id : repository + '-root',
	      cls : 'focusnode'
	    })); 
	    
	    var new_root = this.root; 
	    var tloader = this.loader;
    
	    var conn = new Ext.data.Connection({
	      defaultHeaders : {
	        'Accept': 'application/json'
	      }
	      ,method : 'POST'
	      
	    });

		setUserLensInfo(this);
		var browser = this;

		// What i meant by this code is that if the repository is an abbreviated
		// namegraph from virtuoso repository we will use it as named graph.
		if( (getShortURL(repository)+"") != ""+repository)
			this.virtuoso_namedgraph = repository;
		
	    conn.request({
	      url : baseURL + '/Services/rest/skoslens/rootnodesp',
	      method : 'POST',
	      timeout : 60000,
	      params : {
	        ns          : this.ns,
	        server_url  : this.server_url,
      		repository  : this.repository,
	        username    : this.username,
	        password    : this.password,
			top_concept 		: this.top_concept,
			narrower_predicate 	: this.narrower_predicate,
			skos_version		: this.skos_version,
			virtuoso_namedgraph : this.virtuoso_namedgraph,
	        rootnodes : 'true',
	        json : 'true',
	        target : 'ThesaurusBrowser'  
	      },
	      
	      success : function(response, options) {
	        
	        // remove loading icon
	        new_root.findChild('id', repository + '-root').remove();
	        
	        try {
	          var decoded = Ext.util.JSON.decode(response.responseText);
	          var _topterms = decoded.topterms;
	          
	          for (var i = 0; i < _topterms.length; i++) {
	        	  // Hiding some internal nodes created by sesame	  
	        	  if(_topterms[i].term.startsWith("_")) continue;
	        	  	  
			      newNode = new_root.appendChild(new Ext.tree.AsyncTreeNode({
						  expanded : true,
						  loader : tloader,
						  draggable : true,
						  iconCls : 'broader-icon',
						  //text : _topterms[i].term + ' - <div class="concept-id"> &lt;<i>'+_topterms[i].id+'</i>&gt;</div>',
						  text : _topterms[i].term,
						  plainText : _topterms[i].term,
						  id : _topterms[i].id,
						  URI : _topterms[i].id,
						  leaf : false,
						  allowDrop : false
						}));
	          }	          
	        } catch (err) {
	          Ext.MessageBox.alert('ERROR', err);
	        }
			browser.attemptChecking();
	      }
	      ,failure : loadFailed
	    });
    }
  },

   attemptChecking: function(){
		if(this.wait_queue == undefined) return;
		if(this.wait_queue.length == 0) return; // we're done
		if(this.currently_checking > 1 || this.currently_expanding) {
			setTimeout(this.attemptChecking,200);
		}
		this.currently_running ++;
		var curNode = this.wait_queue.pop();

		this.checkThisNode(curNode);

   },

   checkThisNode: function(node){

		var conn = new Ext.data.Connection({ defaultHeaders : { 'Accept': 'application/json' } ,
											 method : 'POST' });
		var browser = this;
		
		conn.request({ 
          url :  getBaseURL() + '/Services/rest/skoslens/narrowerp',
	      method : 'POST',
	      timeout : 60000,
	      params : {
	     	ns : '',
			uri : node.id,
            server_url : this.server_url,
            repository : this.repository,
            username : this.username,
            password : this.password,
			top_concept 		: this.top_concept,
			narrower_predicate 	: this.narrower_predicate,
			skos_version		: this.skos_version,
			virtuoso_namedgraph : this.virtuoso_namedgraph,
	        
            json : 'true',
            target : 'ThesaurusBrowser'
           },
		   success : function(response, options){
				  var decoded = Ext.util.JSON.decode(response.responseText);

				  //browser.loader.processResponse(response, node, function(n){},node);

				  if(decoded.length == 0){
						node.leaf = true;
						if(node)
						node.ui.updateExpandIcon();
				  } 
				  // Decrease counter of running and call again next perform checking
				  browser.currently_checking--;
				  browser.attemptChecking();

			},
			failure :function(response,options){ 
			} 

		});
	}
 
}); 

Ext.reg('thesaurusbrowser', Ext.app.ThesaurusBrowser);
