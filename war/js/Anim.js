WireIt.util.Anim = function( terminals, el, attributes, duration, method) {
   if(!terminals) {
      throw new Error("WireIt.util.Anim needs at least terminals and id");
   }
   
   /**
    * List of the contained terminals
    * @property _WireItTerminals
    * @type {Array}
    */
   this._WireItTerminals = terminals;
   
   WireIt.util.Anim.superclass.constructor.call(this, el, attributes, duration, method);
   
   // Subscribe the onTween event to move the wires
   this.onTween.subscribe(this.moveWireItWires, this, true);
   this.onComplete.subscribe(this.updateMiniMap, this, true);
};

YAHOO.extend(WireIt.util.Anim, YAHOO.util.Anim, {
   
   /**
    * Listen YAHOO.util.Anim.onTween events to redraw the wires
    * @method moveWireItWires
    */
   moveWireItWires: function(e) {
      // Make sure terminalList is an array
      var terminalList = YAHOO.lang.isArray(this._WireItTerminals) ? this._WireItTerminals : (this._WireItTerminals.isWireItTerminal ? [this._WireItTerminals] : []);
      // Redraw all the wires
      for(var i = 0 ; i < terminalList.length ; i++) {
         if(terminalList[i].wires) {
            for(var k = 0 ; k < terminalList[i].wires.length ; k++) {
               terminalList[i].wires[k].redraw();
            }
         }
      }
   },
   
   updateMiniMap: function(e){
	 SNE.editor.layer.layermap.draw();  
   },

   /**
    * In case you change the terminals since you created the WireIt.util.Anim:
    * @method setTerminals
    * @param {Array} terminals
    */
   setTerminals: function(terminals) {
      this._WireItTerminals = terminals;
   }

});
