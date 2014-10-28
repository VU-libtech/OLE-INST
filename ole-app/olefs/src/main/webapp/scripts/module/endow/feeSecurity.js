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

 function loadFeeSecurityCodeDesc(feeSecurityCodeFieldName){
	var elPrefix = findElPrefix(feeSecurityCodeFieldName.name);
	var feeSecurityCodeDescriptionFieldName = elPrefix + ".security.description";
 	setFeeSecurityCodeDescription(feeSecurityCodeFieldName, feeSecurityCodeDescriptionFieldName);
 }
  
 function setFeeSecurityCodeDescription(feeSecurityCodeFieldName, feeSecurityCodeDescriptionFieldName){
 
	var feeSecurityCode = DWRUtil.getValue(feeSecurityCodeFieldName);
    
	if (feeSecurityCode =='') {
		clearRecipients(feeSecurityCodeDescriptionFieldName);
	} else {
		feeSecurityCode = feeSecurityCode.toUpperCase();
		
		var dwrReply = {
			callback:function(data) {
			if (data != null && typeof data == 'object') {
				setRecipientValue(feeSecurityCodeDescriptionFieldName, data.description);
			} else {
				setRecipientValue(feeSecurityCodeDescriptionFieldName, wrapError("Security code description not found"), true);			
			} },
			errorHandler:function(errorMessage ) { 
				setRecipientValue(feeSecurityCodeDescriptionFieldName, wrapError("Security code description not found"), true);
			}
		};
		SecurityService.getByPrimaryKey(feeSecurityCode, dwrReply);
	}
}
