// vim: sw=4:ts=4:nu:nospell:fdc=4
/**
* The Skos Lens Box 
*
* @author    Adianto Wibisono
* @copyright (c) 2010
* @date      2 April 2010
* @version   $Id$
*
* @license 
*/

//Available lenses are already stored in lensesmap. This initialization is done in utils.js. Here we only extracted it and put it in combo.
//Only the labels are used, synching are in interpretDetect combo. I need to do something about this later, the store should use all the fields not only labels.
function getAvailableLenses(combo, ns, server_url, repository, username, password) {

		lensMap = loadObject('lensesmap');
		if(!lensMap) lensMap = getLensesMap();

		lenses = new Array(0);

		for(label in lensMap) lenses.add([label]);
	
		var lstore = new Ext.data.SimpleStore({
							 fields: ['label'],
							 data: lenses 
						   });		 

		Ext.apply(combo, {store : lstore} );
   
 }



////////////////////////////////////////////////////////////////////////////////
// Skos Lens Combo Box 
////////////////////////////////////////////////////////////////////////////////
Ext.app.SkoslensCombo = Ext.extend(Ext.form.ComboBox, {
	
  initComponent:function() {

  	  // Not setting any store here
      Ext.apply( this, {
		id:'skoslens_combo',
        fieldLabel : 'SkosLens',
		name : 'skoslens',
		enableKeyEvents: true,
		maxListWidth : 150,
		width:150,
		resizable:true,
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		emptyText : 'Select skos lens...',
		selectOnFocus : true,
		displayField : 'label',
		lazyInit : true
      });

	  // Store for this combo box is applied (Ext.appy) within this function
	  getAvailableLenses(this, this.baseParams.ns, this.baseParams.server_url, this.baseParams.repository, this.baseParams.username, this.baseParams.password);

   	  Ext.app.SkoslensCombo.superclass.initComponent.call(this);


	
	  this.on({
			 'select' : {
				 fn: function(cb, record, index) {
				 		lensMap = loadObject('lensesmap');
						if(!lensMap) lensMap = getLensesMap();

						curLens = lensMap[this.getValue()];

						saveObject("curLens", curLens);	
						saveState(this.componentId(), this.getValue());

						setUserLensInfo(this.browser);

						this.browser.showRoot();
						// for narroweralts to have updated/latest info on lens selected by the user we need to call this.
					 }
			 }
	  });


  }, // end of function initComponent

  componentId : function(){
		return this.baseParams.server_url + "-"+this.baseParams.repository;
  },


});
