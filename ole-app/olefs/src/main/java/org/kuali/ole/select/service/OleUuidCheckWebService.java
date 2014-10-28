/*
 * Copyright 2012 The Kuali Foundation.
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
package org.kuali.ole.select.service;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(name = "oleUuidCheckWebService", targetNamespace = "http://service.select.ole.kuali.org/")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public interface OleUuidCheckWebService {

    /**
     * This method takes uuid as parameter and checks whether uuid exists in RequisitionItem,POItem,ReceivingLineItem,PaymentRequest,Loan,Request,Serial Receiving
     * returns non linked uuids with pipe and linked constants
     *
     * @param uuid
     * @return String
     */
    public String checkUuidExsistence(String uuid);
}
