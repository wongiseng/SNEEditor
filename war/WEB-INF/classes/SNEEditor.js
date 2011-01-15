/**
 * The wiring editor is overriden to add a TAB
 */
SNE.WiringEditor = function(options) {
	SNE.WiringEditor.superclass.constructor.call(this, options);

};

YAHOO.lang.extend(SNE.WiringEditor, WireIt.ComposableWiringEditor, {
	
	updateOWLView : function() {
		
		working = this.editor.getValue().working;
	
		
		/**
		 * Story behind this loop is that when i generated information for modules, each container for modules already has an information about field types.
		 * But when saved, the working map from WireIt code which trigger saving does not have any information about this type.
		 * So I am coding this loop to send datatype property into the server so that RDF/OWL can be generated properly.
		 */
	
		for(var i=0;i<working.modules.length;i++){
			// To make it easier to work with later on I am turning this into a map of name -> type hash map that can be used later
			var fieldArray = SNE.editor.modulesByID[working.modules[i].id].container.fields;
			var fieldMap = {}
			for(var j=0;j<fieldArray.length;j++){
				fieldMap[fieldArray[j].name] = fieldArray[j].type;
			}
			working.modules[i].fields = fieldMap;
		}
		
		//	console.log(working);
		// 	Backend end point to do POST request. 
		var sURL = "/rest/owl/getFormattedOWLRDF";
		
		// It is necessary to :
		// 			- Stringify (getting string of the object) + 
		//			- encodeURI the whole request to make it looks like FORM POST   
		var postData = encodeURI("objString="+YAHOO.lang.JSON.stringify(working));
		
		// Set current view to be loading
		this.get("contentEl").innerHTML = SNEUtil.loadingMessage("Loading RDF/OWL");
	
		var handleSuccess = function(o) {
			if (o.responseText != undefined) {
				if(SNE.editor.isSaved() && working.properties.name){
					// Append a download link only if the current network topology is marked as saved and already has a name.
					o.responseText = o.responseText.replace("Generated OWL</h1>","Generated OWL (<a target=\"_blank\" href=\"rest/owl/view/"+working.properties.name+".owl\">download</a>) </h1>");
				}
				o.argument.cur.get("contentEl").innerHTML=o.responseText;
			}
		}

		var handleFailure = function(o) {
			alert("OWL/RDF can not be generated, make sure all the Data Property Fields are not empty.");
		}
		
		// Current Tab object
		var tab = this;

		var callback = {
			success : handleSuccess,
			failure : handleFailure,
			argument : {cur : tab	}
		};

		var request = YAHOO.util.Connect.asyncRequest("POST", sURL, callback, postData);
		
	},
	
	updateModView : function() {
		working = this.editor.getValue().working;
		contentEl = this.get("contentEl");
		if(contentEl.childElementCount)
			contentEl.removeChild(contentEl.childNodes[0]);
		contentEl.appendChild(prettyPrint(working.modules));
	},
	
	updateWireView : function() {
		working = this.editor.getValue().working;
		contentEl = this.get("contentEl");
		if(contentEl.childElementCount)
			contentEl.removeChild(contentEl.childNodes[0]);
		contentEl.appendChild(prettyPrint(working.wires));
	}
});