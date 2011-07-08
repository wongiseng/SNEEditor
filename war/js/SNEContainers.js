
/**
 * Container class used by the SNE module (automatically sets terminals depending on the number of arguments)
 * @class Container
 * @namespace SNE
 * @constructor
 */
SNE.Container = function(options, layer) {

	SNE.Container.superclass.constructor.call(this, options, layer);

	// Reposition the terminals when the SNE is being resized
	this.ddResize.eventResize.subscribe(function(e, args) {
		this.positionTerminals();
		YAHOO.util.Dom.setStyle(this.textarea, "height", (args[0][1] - 70) + "px");
	}, this, true);
};

YAHOO.extend(
				SNE.Container,
				WireIt.FormContainer,
				{


					/**
					 * Reposition the terminals
					 * @method positionTerminals
					 */
					positionTerminals : function() {
						var width = WireIt.getIntStyle(this.el, "width");
						var inputsIntervall = Math.floor(width
								/ (this.nParams + 1));
						for ( var i = 1; i < this.terminals.length; i++) {
							var term = this.terminals[i];
							YAHOO.util.Dom.setStyle(term.el, "left",
									(inputsIntervall * (i)) - 15 + "px");
							for ( var j = 0; j < term.wires.length; j++) {
								term.wires[j].redraw();
							}
						}
						
						// Output terminal
						WireIt.sn(this.outputTerminal.el, null, {
							position : "absolute",
							bottom : "-15px",
							left : (Math.floor(width / 2) - 15) + "px"
						});
						for ( var j = 0; j < this.outputTerminal.wires.length; j++) {
							this.outputTerminal.wires[j].redraw();
						}
					},


				});
