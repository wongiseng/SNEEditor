// vim: sw=4:ts=4:nu:nospell:fdc=4
/**
* The function which draws the Server panel
*
* @author    Edgar Meij
* @copyright (c) 2009
* @date      5 April 2009
* @version   $Id$
*
* @license 
*/

function ThesaurusServerPanel() {
	
	document.getElementById('loading-msg').innerHTML = 'Initializing Thesaurus Server ...';
  
  /*
  var task = {
    run: function() {
        console.log("repository: " + loadState('repository', ''));
    },
    interval: 900 //.3 seconds
  }
  
  Ext.TaskMgr.start(task);
  */
function openTab(repoID){
		tp = Ext.getCmp('thesauruspanel');
		server_url = loadState('server_url');
	    var b = new Ext.app.ThesaurusBrowser({
				   //loader : tloader,
				   id : server_url + '-' + repoID,
				   ns : loadState('ns'),
				   server_url : server_url,
				   repository : repoID,
				   username : loadState('username'),
				   password : loadState('password') 
				 });

				setUserLensInfo(b);

				tp.add(b);
				tp.setActiveTab(b);
				b.detectAndShow(b);
   }


	function checkAlreadyLoaded(repositoryURL, repoID){
		checkLoaded = new Ext.data.Connection();  
	
		checkConnection  = new Ext.data.Connection();  
		checkConnection.request({
			url     : baseURL + '/Services/rest/remote/alreadyLoaded',
			params  : {url:repositoryURL, repositoryID:repoID},
			success : 
				function(result, options) {
						response=result.responseText; 
						if(response){
							Ext.Msg.show({
								title:"URL Already Loaded", 
								msg: repositoryURL + " already loaded as " + response,
								buttons: Ext.Msg.OK
								});
							openTab(""+response);
						} else {

							loadRemoteConnection = new Ext.data.Connection();  
						
							loadRemoteConnection.request({
								url     : baseURL + '/Services/rest/remote/loadrdfnew',
								params  : {url:repositoryURL, repositoryID:repoID},
								success : 
									function(response, options) {
										loadRepositories();
										openTab(repoID);
									},

								failure : function(response, options) {
										alert("Failed to load remote repository"+response);
									}
							});



						}
				},

			failure : loadFailed,
		});
	}

   function loadRemoteRepository(repositoryURL, repoID){

	    // Sanitize repoID removing non Words characters, replace it with dash
		repoID = (""+repoID).replace(/\W+/g,"_");

		checkAlreadyLoaded(repositoryURL, repoID);
  }
	
  function repositorySelect(item, checked){
    
    var repo = item.value;
    if (repo === '') {
      return;
    }

    saveState('repository', repo);
    
    // add repo to the cache of opened repos
    var repoHash = loadObject('openrepositorylist2', {});
    if (! repoHash[repo] ) {
      repoHash[repo] = 1;
    }
    saveObject('openrepositorylist2', repoHash);
    
    // check to see whether we already have a tab for this server and repository
    var repotab = Ext.getCmp(loadState('server_url') + '-' + loadState('repository'));
    if (repotab) {
    	thesauruspanel.activate(Ext.getCmp(loadState('server_url') + '-' + loadState('repository')));
    	formpanel.collapse(true);
    	return;
    }
   
    var tb = new Ext.app.ThesaurusBrowser({
      ns : loadState('ns'),
      server_url : loadState('server_url'),
      repository : loadState('repository'),
      username : loadState('username'),
      password : loadState('password')
    });
   	

    var tab = thesauruspanel.add(tb);
    thesauruspanel.setActiveTab(tab);
    formpanel.collapse(true);


	tb.detectAndShow(tb);

  }
  
	var sstore = new Ext.data.SimpleStore({
    fields: ['url'],
    data: loadServers(),
    listeners : {
      beforeload : function (th, opts) {
        //showWindow('servers',loadServers());
      	return false;
      }
    }
  });

  // Loading direct server url if provided, if it is not there, then default base server_url
  currentURL = loadState('server_url',getBaseURL() + '/openrdf-sesame');
  currentURL = loadState('direct_server_url', currentURL)

  /// the server combobox
  var serverbox = new Ext.form.ComboBox({
    id : 'server_url',
    fieldLabel : "Server",
		name : 'server_url',
		// Just in case we're using direct link, select it
		value : currentURL,
		enableKeyEvents: true,
		width : 290,
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		emptyText : 'Select or type in a server...',
		selectOnFocus : true,
		displayField : 'url',
		store : sstore,
		lazyInit : false,
		listeners : {
		  
		  render : function(th) {
		    var url = ("" +th.value).trim();
			// If we use direct link, current server_url will be overwritten
			var dirurl = loadState("direct_server_url");
			if(dirurl) url = dirurl;

		    formpanel.getForm().setValues({server_url : url});
		    loadRepositories();
		    //saveState('repository', null);
		  },
		
			select : function(cb, record, index) {
				var server_url = record.get('url');

				if (server_url !== '' && server_url !== loadState('server_url', '')) {
					loadRepositories();
					saveState('repository', '');
					repositoryBox.clearValue();
        		}
			},
			focus : function(f) {
			  f.setValue('');
			  sstore.load(loadServers(), false);
			},
			blur : function(f) {
				if (!f.getValue() && loadState('server_url')) {
					f.setValue(loadState('server_url'));
        }
			},
      specialkey : function(th, e) {
        if (e.getKey() == e.ENTER) {
          if (th.getValue() !== '') {
			th.setValue(th.getValue().trim());
          	loadRepositories();
          	saveState('repository', '');
          	sstore.load(loadServers(), false);
          }
        }
      }
		}
	});
	
	
  /// the repository combobox
	var repositoryBox = new Ext.form.ComboBox({
		value : loadState('direct_repository', loadState('repository','dodol')),
		id : 'repository-box',
		fieldLabel : 'Repository',
		forceSelection: true,
		displayField: 'repository', 
		valueField : 'repository-uri',
		mode: 'local',
		width : 290,
		typeAhead : true,    
		lazyInit : false,
  		tpl: '<tpl for="."><div ext:qtip="{repository-uri}" class="x-combo-list-item">{repository}</div></tpl>',
    
		store: new Ext.data.SimpleStore({
			//proxy : new Ext.data.MemoryProxy(repositories),
			id : 'repositoryDS',
			data: [],
			fields: ['repository', 'repository-uri'],
			listeners : {
			  beforeload : function (th, opts) {
					//loadRepositories();
					this.loadData(loadObject('storedrepositorylist', []), false);
					this.sort('repository','ASC');
					return false;
			  }
			}
	    }),
		listeners : {
			blur : function(f) {
				if (!f.getValue() || f.getValue() === '') {
					this.setValue(loadState('repository', ''));
	      } 
			}, 
			focus : function(f) {
				this.clearValue();
				this.store.reload();
			},
	    select : repositorySelect
		}
    /*
		//value : loadState('server_url', ''),
		enableKeyEvents: true,
		width : 260,
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		emptyText : 'Select or type in a server...',
		selectOnFocus : true,
		displayField : 'url',
		store : serverds,
		listeners : {
			select : function(cb, record, index) {
				server_url = record.get('url');
				if (server_url !== '') { 
					saveState('server_url', server_url);
					repository = '';
					tabpanel.activate('repository-tab');
        }
				
			},
			focus : function(f) {
				f.setValue('');
				//repositoriestab.remove('repositorylist');
			},
			blur : function(f) {
				if (!f.getValue() && this.server_url) {
					f.setValue(this.server_url);
        }
        
        if (f.getValue() !== '') { 
        	saveState('server_url', f.getValue());
          addToStore(serverds, f.getValue());
          saveStore('server_url_ds', serverds);
        }
			},
      specialkey : function(th, e) {
        if (e.getKey() == e.ENTER) {
        	
          if (th.getValue() !== '') {	
          	server_url = th.getValue();
          	saveState('server_url', server_url);
          	addToStore(serverds, th.getValue());
          	saveStore('server_url_ds', serverds);
          	repository = '';
            tabpanel.activate('repository-tab');
          }
        }
      }
		}
		*/
	});
  
  // the password field
  // TODO: toggle clear password
  var passwordfield = new Ext.form.TextField({
    fieldLabel : 'Password',
    name : 'password',
    enableKeyEvents: true,
    id : 'password',
    allowBlank : false,
    inputType : 'password',
    value : loadState('password', 'testuser'),
    width : 290,
    listeners : {
      change : function(th, newVal, oldVal) {
        saveState('password', newVal);
      },
      specialkey : function(th, e) {
        if (e.getKey() == e.ENTER) {
          loadRepositories();
        }
      }
    }
  });
  var usernamefield = new Ext.form.TextField(
	{
				id : 'username',
				enableKeyEvents: true,
				fieldLabel : 'Username',
				name : 'username',
				allowBlank : false,
				width: 290,
				value : loadState('username', 'testuser'),
				listeners : {
				  change : function(th, newVal, oldVal) {
					saveState('username', newVal);
				  },
				  specialkey : function(th, e) {
					if (e.getKey() == e.ENTER) {
						saveState('username', th.getValue());
					  loadRepositories();
					}
				  }
				}
  });

  var loadRemoteURL = new Ext.form.TextField({
		fieldLabel : 'URL to load ',
		name : 'remoteurl',
		enableKeyEvents : true,
		id : 'remoteurl',
		allowBlank: true,
		inputType : 'text',
		value : loadState('remoteurl',''),
		width: 290,
		listeners : {
			change : function(th, newVal, oldVal){
				saveState('remoteurl', newVal);
			},
			specialkey : function(th, e) {
				if(e.getKey() == e.ENTER){
					saveState('remoteurl', this.getValue());
					loadRemoteRepository(loadState('remoteurl'), loadState('remoteID'));
	  				saveState('server_url', 'http://dev.adaptivedisclosure.org/openrdf-sesame');
					loadRepositories();
				}
			}
		}

	});

  var loadRemoteID = new Ext.form.TextField({
		fieldLabel : 'ID to load',
		name : 'remoteID',
		enableKeyEvents : true,
		id : 'remoteID',
		allowBlank: true,
		inputType : 'text',
		value : loadState('remoteID',''),
		width: 290,
		listeners : {
			change : function(th, newVal, oldVal){
				saveState('remoteID', newVal);
			},
			specialkey : function(th, e) {
				if(e.getKey() == e.ENTER){
					saveState('remoteID', this.getValue());
					loadRemoteRepository(loadState('remoteurl'), loadState('remoteID'));
	  				saveState('server_url', 'http://dev.adaptivedisclosure.org/openrdf-sesame');
					loadRepositories();

				}
			}
		}

	});
  // the actual panel
  var formpanel = new Ext.FormPanel({
  	id : 'sesame-panel',
  	bodyStyle : 'padding:5px',
    defaults : {
      bodyStyle : 'padding:5px',
      autoHeight : true
    },
    defaultType : 'textfield',
    labelWidth : 75,
    collapsible : true,
    // titleCollapse : true,
    // Must keep this to false, otherwise the comboboxes get mixed up
    collapsed : false, //loadState('sesame-panel-collapsed', false),
    autoScroll: true,
    split : true,
    margins : '0 5 30 0',
    cmargins : '5 5 5 0',
    title: 'AIDA Repository Server',
    height: 260,
    url : getBaseURL() + '/Services/RepositoryGetRepositoriesSVL',
    monitorValid : true,
    region : 'south',
    lazyInit : false,
    items : [{
      xtype : 'fieldset',
      title : 'Server & Repository ',
      defaultType : 'textfield',
      name : 'serverinfo',
      id : 'serverinfo',
      items : [
        {name : 'read_write', value : 'r', inputType : 'hidden'}, 
        {name : 'json', value : 'true', inputType : 'hidden'},
		serverbox, repositoryBox
	  ]
    } ,
	{ 
	  xtype : 'fieldset',
      title : 'Load Remote Repository',
	  collapsible : true,
      items : [ loadRemoteURL, loadRemoteID] 
	},
	{ 
	  xtype : 'fieldset',
      title : 'Security Info',
	  collapsible : true,
	  collapsed : true,
      items : [ usernamefield,  passwordfield] } 
    ],

      listeners : {
		//  resize : function (th, adjWidth, adjHeight, rawWidth, rawHeight) {
		//    Ext.get('server_url').setWidth(adjWidth);
		//  }
		  collapse : function (panel) {
			//saveState('sesame-panel-collapsed', true);
		  },
		  expand : function (panel) {
			//saveState('sesame-panel-collapsed', false);
		  },
		  // Let's ignore state for this panel, otherwise the comboboxes get mixed up.
		  beforestaterestore : function (th, state) {
			return false;
		  }

    }
  });
  
  //formpanel.doLayout();
  
  return formpanel;
	
}
