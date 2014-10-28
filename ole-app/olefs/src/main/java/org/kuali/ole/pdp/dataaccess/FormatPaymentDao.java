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
 * Created on Sep 1, 2004
 *
 */
package org.kuali.ole.pdp.dataaccess;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import org.kuali.ole.pdp.businessobject.PaymentProcess;


/**
 * @author jsissom
 */
public interface FormatPaymentDao {
    /**
     * This method mark payments for format
     * @param proc
     * @param customers
     * @param paydate
     * @param paymentTypes
     */
    public Iterator markPaymentsForFormat(List customers, Timestamp paydate, String paymentTypes);

    /**
     * This method unmark payments that were marked for format.
     * @param proc
     */
    public Iterator unmarkPaymentsForFormat(PaymentProcess proc);
}
