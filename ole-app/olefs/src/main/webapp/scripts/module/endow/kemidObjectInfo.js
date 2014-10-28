/*
 * Copyright 2007-2008 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function setTypeCodeRelatedInfo(typeCodeFieldName) {
	var elPrefix = findElPrefix(typeCodeFieldName.name);
	var cashSweepModelIdFieldName = elPrefix + ".cashSweepModelId";
	var incomeACIModelIdFieldName = elPrefix + ".incomeACIModelId";
	var principalACIModelIdFieldName = elPrefix + ".principalACIModelId";
	var typeCode = DWRUtil.getValue(typeCodeFieldName);
	
	if (typeCode == "") {
		clearRecipients(cashSweepModelIdFieldName);
		clearRecipients(incomeACIModelIdFieldName);
		clearRecipients(principalACIModelIdFieldName);
	} else {
		var dwrReply = {callback:function (data) {
		
			if (data != null && typeof data == "object") {
			
				setRecipientValue(cashSweepModelIdFieldName, data.cashSweepModelId);
				setRecipientValue(incomeACIModelIdFieldName, data.incomeACIModelId);
				setRecipientValue(principalACIModelIdFieldName, data.principalACIModelId);
			} else {
			
				setRecipientValue(cashSweepModelIdFieldName, wrapError("Type Code not found1"), true);
				setRecipientValue(incomeACIModelIdFieldName, wrapError("Type Code not found1"), true);
				setRecipientValue(principalACIModelIdFieldName, wrapError("Type Code not found1"), true);
			}
		}, errorHandler:function (errorMessage) {
		
			window.status = errorMessage;
			alert(errorMessage);
			setRecipientValue(cashSweepModelIdFieldName, wrapError("Type Code not found"), true);
			setRecipientValue(incomeACIModelIdFieldName, wrapError("Type Code not found"), true);
			setRecipientValue(principalACIModelIdFieldName, wrapError("Type Code not found"), true);
		}};
		
		TypeCodeService.getByPrimaryKey(typeCode, dwrReply);
	}
}

