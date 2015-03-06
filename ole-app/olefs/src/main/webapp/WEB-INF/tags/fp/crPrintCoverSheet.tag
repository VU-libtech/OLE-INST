<%--
 Copyright 2007-2009 The Kuali Foundation
 
 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.opensource.org/licenses/ecl2.php
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<c:if test="${KualiForm.coverSheetPrintingAllowed}">
   <div align="center">
        <a href="financialCashReceipt.do?methodToCall=printCoverSheet&${PropertyConstants.DOCUMENT_NUMBER}=${KualiForm.document.documentNumber}" target="pdf_window">
            <font color="red"><bean:message key="label.document.cashReceipt.printCoverSheet"/></font>
        </a>
        <html:img src="${ConfigProperties.externalizable.images.url}icon-pdf.png" alt="print cover sheet" title="print cover sheet" width="16" height="16"/>
   </div>
   <br>
</c:if>
