/**
 * Tree Layout Plugin 
 * TODO: provide an AbstractLayout Class
 * @module layout-plugin
 */

/**
 * Calculate the new position for the given layout and animate the layer to this position
 */
WireIt.Layer.prototype.layoutAnim = function( sLayout, duration) {
	
	var layout = new WireIt.Layout[sLayout || "SimpleTree"](this);
	
	var newPositions = layout.run(),
	 	 n = this.containers.length;
	
	for(var i = 0 ; i < n ; i++) {
		var c = this.containers[i],
			 p = newPositions[i],
		anim = new WireIt.util.Anim( c.terminals, c.el, {  left: { to: p[0] }, top: {to: p[1]} }, duration || 1, YAHOO.util.Easing.easeOut);
		anim.animate();
	}	
	
	delete layout;
};

/**
 * Start a dynamic layout
 */
WireIt.Layer.prototype.startDynamicLayout = function( sLayout, interval) {
	
	if(this.dynamicLayout) {
		this.stopDynamicLayout();
	}
	
	this.dynamicLayout = new WireIt.Layout[sLayout || "SimpleTree"](this);

	var that = this;
	this.dynamicTimer = setInterval(function() { 
		that._runDynamicLayout(); 
	}, interval || 50);
	
	this._runDynamicLayout();
};

WireIt.Layer.prototype._runDynamicLayout = function( ) {
	
	var newPositions = this.dynamicLayout.run(),
	    n = this.containers.length;

	for(var i = 0 ; i < n ; i++) {
		var c = this.containers[i],
			 p = newPositions[i];
			
			// TODO: this test should be: isDragging && container focused
			if(! YAHOO.util.Dom.hasClass(c.el, "WireIt-Container-focused") ) {
				c.el.style.left = p[0]+"px";
				c.el.style.top = p[1]+"px";
				c.redrawAllWires();
			}
	}
};


/**
 * Stop the dynamic layout
 */
WireIt.Layer.prototype.stopDynamicLayout = function() {
	clearInterval(this.dynamicTimer);
	this.dynamicTimer = null;
	this.dynamicLayout = null;
};




/**
 * @static
 */
WireIt.Layout = {};
	
	
/** 
 * SimpleTree Layout 
 * @class WireIt.Layout.SimpleTree
 * @constructor
 */
WireIt.Layout.SimpleTree = function(layer) {	
	this.layer = layer;
	this.init();
};

WireIt.Layout.SimpleTree.prototype = {
		
	/**
	 * Init the default structure
	 */
	init: function() {
		this.NNodes = this.layer.containers.length ;
		this.nodes = [];
		
		this.children	= new Array(this.NNodes);
		
		// Initialize nodes positions and tree level
		for(var i = 0 ; i < this.NNodes; i++) {
			var pos = this.layer.containers[i].getXY();
			this.nodes.push({
				posX 	: pos[0],
				posY	: pos[1],
				level 	: 0
			});			
			
			//console.log(i+  "  "+ this.layer.containers[i].title);
			this.children[i] = new Array();
		}
		
		// Edges list.
		this.edges = [];
		
		this.isRoot 	= new Array(this.NNodes);
		this.visited 	= new Array(this.NNodes);
		
		//Initialize isRoot into true for all nodes. Falsified when some edge point to it.
		//And everything is not visited.
		for(var i=0;i<this.NNodes;i++) {
			this.isRoot[i] = true;
			this.visited[i] = false;
		}
		
		// Extract wires
		for(var i = 0 ; i < this.layer.wires.length ; i++) {
			var wire 	= 	this.layer.wires[i], 
			srcNode		=	this.layer.containers.indexOf(wire.terminal1.container),
			tgtNode 	= 	this.layer.containers.indexOf(wire.terminal2.container) ;
			
			this.isRoot[tgtNode] = false;
			this.edges.push([srcNode, tgtNode]);
			this.children[srcNode].push(tgtNode);
		}	
		
		// Array keeping last position used in this level.
		this.lastLevel = Array(1000);
		for(var i=0;i<1000;i++) this.lastLevel[i] = 0;
	},
	
	doTreeLayout: function(nodeId, level) {
		if(this.visited[nodeId]) return;

		this.visited[nodeId] = true;
		
		curNode = this.nodes[nodeId];
		curNode.posX = this.lastLevel[level]*300;
		curNode.posY = level * 200;
		
		this.lastLevel[level]++;
		var spc="";
		for(var j=0;j<level;j++) spc += "  ";
		//console.log(spc + " "+nodeId+"  "+curNode.posX+"  "+curNode.posY);
		
		for(var i=0;i<this.children[nodeId].length;i++){
			this.doTreeLayout(this.children[nodeId][i],level+1);
		}
		
	},
	
	run: function() {
		
		// Do Tree layout for all root nodes
		for(var i=0;i<this.NNodes;i++)
			if(this.isRoot[i]) {
				this.doTreeLayout(i,0);
				// this following is for separating trees vertically
				for(var j=0;j<1000;j++) this.lastLevel[j]++;
			}
		// Just in case there were cycles, they will not be dealt with 
		// in the code above since none of them are roots. Here we deal with them
		for(var i=0;i<this.NNodes;i++){
			if(!this.visited[i])
				this.doTreeLayout(i,0);
		}
		
		var newPositions = [];
		for( i = 0 ; i < this.nodes.length ; i++) {
			node = this.nodes[i];
			newPositions.push([node.posX,node.posY]);
		}

		return newPositions;		
	}
	
};