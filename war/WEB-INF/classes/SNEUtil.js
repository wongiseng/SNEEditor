
var SNEUtil = {
		
		makeQueryString : function(keyValuesMap){
			var result = "";
			for (var key in keyValuesMap){
				if(result != "") result += "&";
				result += key+"="+keyValuesMap[key]
			}
			return result;
		},
		
		getCinegridRootQS : function(){
			return "http://dev.adaptivedisclosure.org/Services/rest/skoslens/rootnodesp?"+
				this.makeQueryString({ 
		 			   callback:"{callback}",
					   ns:"",
					   server_url:"http://dev.adaptivedisclosure.org/openrdf-sesame",
					   repository:"CinegridOWL",
					   username:"",
					   password:"",
					   top_concept:"owl:Class",
					   narrower_predicate:"rdfs:subClassOf",
					   skos_version:"Not Skos",
					   virtuoso_namedgraph:"",
					   rootnodes:"true",
					   json:"true",
					   target:"ThesaurusBrowser"
				});
		},
		
		getCinegridNarrowerQS : function(node){
			return  "http://dev.adaptivedisclosure.org/Services/rest/skoslens/narroweraltsp?callback={callback}&ns=&server_url=http://dev.adaptivedisclosure.org/openrdf-sesame&repository=CinegridOWL&username=&password=&top_concept=owl:Class&narrower_predicate=rdfs:subClassOf&skos_version=Not Skos&virtuoso_namedgraph=&json=true&target=ThesaurusBrowser"+
 		   			"&uri="+node.id.replace(/#/g,"%23")
		},
		
		loadingMessage : function(messageString){
			return "<div style=\"margin:auto;text-align:center;\"><img src=images/loading.gif /><br>"+ messageString + "... </div>";
		},
		// Getting root query string. Rootnodesp is a JSONP end point provided from AIDA Developer server.
		// It will gives the list of root concepts
		getRootQS : function(SESAME_URL, REPOSITORY_NAME){
			return "http://dev.adaptivedisclosure.org/Services/rest/skoslens/rootnodesp?"+
				this.makeQueryString({ 
		 			   callback:"{callback}",
					   ns:"",
					   server_url:SESAME_URL,
					   repository:REPOSITORY_NAME,
					   username:"",
					   password:"",
					   top_concept:"owl:Class",
					   narrower_predicate:"rdfs:subClassOf",
					   skos_version:"Not Skos",
					   virtuoso_namedgraph:"",
					   rootnodes:"true",
					   json:"true",
					   target:"ThesaurusBrowser"
				});
		},
		// Getting narrower query string form AIDA. Narroweraltsp being contacted here is from AIDA dev server to get narrower concept of a term.
		getNarrowerQS : function(node, SESAME_URL, REPOSITORY_NAME){
			return  "http://dev.adaptivedisclosure.org/Services/rest/skoslens/narroweraltsp?callback={callback}&ns=&server_url="+SESAME_URL+"&repository="+REPOSITORY_NAME+"&username=&password=&top_concept=owl:Class&narrower_predicate=rdfs:subClassOf&skos_version=Not Skos&virtuoso_namedgraph=&json=true&target=ThesaurusBrowser"+
 		   			"&uri="+node.id.replace(/#/g,"%23")
		},
		
		// Just a helper to clone object
		clone : function (obj){
			        if(obj == null || typeof(obj) != 'object')
			            return obj;
			
			        var temp = new obj.constructor(); // changed (twice)
			        for(var key in obj)
			            temp[key] = this.clone(obj[key]);
			
			        return temp;
		}
}
