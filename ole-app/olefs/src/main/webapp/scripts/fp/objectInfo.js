/*
 * Copyright 2007 The Kuali Foundation
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

 /*
 * Given an form and an element name, returns the uppercased, trimmed value of that element
 */
function cleanupElementValue( kualiForm, name ) {
    var element = kualiForm.elements[ name ];

    if ( typeof element == 'undefined' ) {
        alert( 'undefined element "' + name + '"' );
    }

    element.value = element.value.toUpperCase().trim();

    return element.value;
}

/*
 * Clears the value of the given target.
 */
function clearTarget(kualiForm, targetBase) {
    setTargetValue(kualiForm, targetBase, '');
}


/*
 * Sets the value contained by the named div to the given value, or to a non-breaking space if the given value is empty.
 */
function setTargetValue(kualiForm, targetBase, value, isError) {
    var containerHidden = kualiForm.elements[targetBase];
    var containerName = targetBase + '.div';

    var containerDiv = document.getElementById( containerName );

    if (containerDiv) {
        if (value == '') {
			dwr.util.setValue( containerDiv.id, " " );
        } else {
        	setRecipientValue(containerDiv.id, value, isError?null:{escapeHtml:true});            	
		//	dwr.util.setValue( containerDiv.id, value, isError?null:{escapeHtml:true} );
        }
    }
    if (containerHidden) {
        containerHidden.value = value;
    }
}


function loadBankInfo(kualiForm, bankCodeFieldName, bankNameFieldName ) {
	var bankCode = cleanupElementValue( kualiForm, bankCodeFieldName );

	if (bankCode=='') {
		clearTarget(kualiForm, bankNameFieldName);
	} else {
		var dwrReply = {
			callback:function(data) {
				if ( data != null && typeof data == 'object' ) {
					setTargetValue( kualiForm, bankNameFieldName, data.bankName );
				} else {
					setTargetValue( kualiForm, bankNameFieldName, wrapError( "bank name not found" ), true );			
				}
			},
			errorHandler:function( errorMessage ) { 
				window.status = errorMessage;
				setTargetValue( kualiForm, bankNameFieldName, wrapError( "bank name not found" ), true );
			}
		};
		BankService.getByPrimaryId( bankCode, dwrReply );
	}
}
