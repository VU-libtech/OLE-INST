/*
 * Copyright 2007 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function loadKEMIDShortTitle(kemidFieldName){
	var elPrefix = findElPrefix(kemidFieldName.name);
	var kemidShortTitleFieldName = elPrefix + ".kemidObj.shortTitle";
 	setKEMIDShortTitle(kemidFieldName, kemidShortTitleFieldName);
 }
 
function setKEMIDShortTitle( kemidFieldName, kemidShortTitleFieldName ){
	var kemid = DWRUtil.getValue( kemidFieldName );
    
	if (kemid =='') {
		clearRecipients(kemidShortTitleFieldName, "");
	} else {
		var dwrReply = {
			callback:function(data) {
			if ( data != null && typeof data == 'object' ) {
				setRecipientValue(kemidFieldName, data.kemid);				
				setRecipientValue( kemidShortTitleFieldName, data.shortTitle );
			} else {
				setRecipientValue( kemidShortTitleFieldName, wrapError( "kemid not found" ), true );			
			} },
			errorHandler:function( errorMessage ) { 
				setRecipientValue( kemidShortTitleFieldName, wrapError( "kemid not found" ), true );
			}
		};
		KEMIDService.getByPrimaryKey( kemid, dwrReply );
	}
}