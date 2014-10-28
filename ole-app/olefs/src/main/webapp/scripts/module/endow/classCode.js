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

 function loadClassCodeDesc(classCodeFieldName){
	var elPrefix = findElPrefix( classCodeFieldName.name );
	var classCodeDescriptionFieldName = elPrefix + ".classCode.name";
 	setClassCodeDescription(classCodeFieldName, classCodeDescriptionFieldName);
 }
  
 function setClassCodeDescription(classCodeFieldName, classCodeDescriptionFieldName){
 
	var classCode = DWRUtil.getValue(classCodeFieldName);
    
	if (classCode =='') {
		clearRecipients(classCodeDescriptionFieldName);
	} else {
		classCode = classCode.toUpperCase();
		
		var dwrReply = {
			callback:function(data) {
			if ( data != null && typeof data == 'object' ) {
				setRecipientValue( classCodeDescriptionFieldName, data.name );
			} else {
				setRecipientValue( classCodeDescriptionFieldName, wrapError( "class code description not found" ), true );			
			} },
			errorHandler:function( errorMessage ) { 
				setRecipientValue( classCodeDescriptionFieldName, wrapError( "class code description not found" ), true );
			}
		};
		ClassCodeService.getByPrimaryKey( classCode, dwrReply );
	}
}
