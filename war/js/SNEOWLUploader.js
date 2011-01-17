/**
	Event handler for dealing with uploading owl files
 */


SNEOWLUploader = function(){
	
		this.init = function(){
			//var logger = new YAHOO.widget.LogReader("logger-info");

			// The button that will be overlayed
			var uiLayer = YAHOO.util.Dom.getRegion('WiringEditor-uploadButton-button');
			
			// Create the overlay DOM element   
			var overlay = document.createElement("div");
			overlay.id 	= "uploaderOverlay";
			
			// Setup style for this overlay. zIndex high means it is on top of everything else
			YAHOO.util.Dom.setStyle(overlay, 'zIndex', 999);
			
			// We are going to position this overlay to cover underlaying button
			YAHOO.util.Dom.setStyle(overlay, 'position', "absolute");
			
			YAHOO.util.Dom.setStyle(overlay, 'width', uiLayer.right-uiLayer.left + "px");
			YAHOO.util.Dom.setStyle(overlay, 'height', uiLayer.bottom-uiLayer.top + "px");
			
			// Insert the layer as first child of the button
			var button = document.getElementById("WiringEditor-uploadButton");
			button.insertBefore(overlay, button.firstChild);
			
			// Add event listeners to various events on the uploader.
			// Methods on the uploader should only be called once the 
			// contentReady event has fired.
			YAHOO.widget.Uploader.SWFURL = "assets/uploader.swf"; 

			// Transparent overlay does not work yet, I'll use this sprite approach for now
			this.uploader = new YAHOO.widget.Uploader("uploaderOverlay");
			
			// Funny that within all these handlers, 'this' means this.uploader
			this.uploader.addListener('contentReady', this.handleContentReady);
			this.uploader.addListener('fileSelect', this.onFileSelect)
			this.uploader.addListener('uploadStart', this.onUploadStart);
			this.uploader.addListener('uploadProgress', this.onUploadProgress);
			this.uploader.addListener('uploadCancel', this.onUploadCancel);
		
			//this.uploader.addListener('uploadComplete', this.onUploadComplete);
			this.uploader.addListener('uploadCompleteData', this.onUploadResponse);
			this.uploader.addListener('uploadError', this.onUploadError);
		}
		
		
		// Variable for holding the selected file ID.
		this.fileID="";
	
		this.handleClearFiles= function () {
			this.clearFileList();
			this.enable();
			this.fileID = null;
	
//			var filename = document.getElementById("fileName");
//			filename.innerHTML = "";
//	
//			var progressbar = document.getElementById("progressBar");
//			progressbar.innerHTML = "";
		}
	
		// When contentReady event is fired, you can call methods on the uploader.
		this.handleContentReady=function () {
			// Allows the uploader to send log messages to trace, as well as to YAHOO.log
			YAHOO.log("Start Content Ready");
			this.setAllowLogging(true);
	
			// Restrict selection to a single file (that's what it is by default,
			// just demonstrating how).
			this.setAllowMultipleFiles(false);
			
			var ff = new Array({description:"OWL Files", extensions:"*.owl"}, 
							   {description:"RDF Files", extensions:"*.rdf"});
			
			this.setFileFilters(ff); 

		}
	
		// Initiate the file upload. Since there's only one file, 
		// we can use either upload() or uploadAll() call. fileList 
		// needs to have been populated by the user.
		this.upload=function () {
			if (this.fileID != null) {
				this.upload(fileID,
						// I think this should be a call to the server
						"../rest/upload/owl");
				this.fileID = null;
			}
		}
	
		// Fired when the user selects files in the "Browse" dialog
		// and clicks "Ok".
		this.onFileSelect=function(event) {
			for ( var item in event.fileList) {
				if (YAHOO.lang.hasOwnProperty(event.fileList, item)) {
					YAHOO.log(event.fileList[item]);
					YAHOO.log(event.fileList[item].id);
					this.fileID = event.fileList[item].id;
					// Directly upload instead of having separate upload button
					this.upload(this.fileID, "../rest/upload/owl", "POST");  
				}
			}
			
//			this.disable();
	
//			var filename = document.getElementById("fileName");
//			filename.innerHTML = event.fileList[fileID].name;
//	
//			var progressbar = document.getElementById("progressBar");
//			progressbar.innerHTML = "";
		}
	
		// Do something on each file's upload start.
		this.onUploadStart=function (event) {
			
		}
	
		// Do something on each file's upload progress event.
		this.onUploadProgress=function (event) {
//			prog = Math.round(300 * (event["bytesLoaded"] / event["bytesTotal"]));
//			progbar = "<div style=\"background-color: #f00; height: 5px; width: "
//					+ prog + "px\"/>";
//	
//			var progressbar = document.getElementById("progressBar");
//			progressbar.innerHTML = progbar;
			
		}
	
		// Do something when each file's upload is complete.
		this.onUploadComplete=function (event) {
		
			
//			progbar = "<div style=\"background-color: #f00; height: 5px; width: 300px\"/>";
//			var progressbar = document.getElementById("progressBar");
//			progressbar.innerHTML = progbar;
		}
	
		// Do something if a file upload throws an error.
		// (When uploadAll() is used, the Uploader will
		// attempt to continue uploading.
		this.onUploadError=function (event) {
			
		}
	
		// Do something if an upload is cancelled.
		this.onUploadCancel=function (event) {
			
		}
			
		// Do something when data is received back from the server.
		this.onUploadResponse = function(event) {
			var wirings = YAHOO.lang.JSON.parse(event.data);
			// This is the place responsible for loading Wiring.
			
			SNE.editor.loadThisWiring(wirings);
		}
		
		
}
