/*
 * Copyright 2005-2006 The Kuali Foundation
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
package org.kuali.ole.gl.dataaccess.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.ole.gl.OJBUtility;
import org.kuali.ole.gl.businessobject.Encumbrance;
import org.kuali.ole.gl.businessobject.Transaction;
import org.kuali.ole.gl.dataaccess.EncumbranceDao;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

/**
 * An OJB implementation of the EncumbranceDao
 */
public class EncumbranceDaoOjb extends PlatformAwareDaoBaseOjb implements EncumbranceDao {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(EncumbranceDaoOjb.class);

    /**
     * Returns an encumbrance that would be affected by the given transaction
     * 
     * @param t the transaction to find the affected encumbrance for
     * @return an Encumbrance that would be affected by the posting of the transaction, or null
     * @see org.kuali.ole.gl.dataaccess.EncumbranceDao#getEncumbranceByTransaction(org.kuali.ole.gl.businessobject.Transaction)
     */
    public Encumbrance getEncumbranceByTransaction(Transaction t) {
        LOG.debug("getEncumbranceByTransaction() started");

        Criteria crit = new Criteria();
        crit.addEqualTo(OLEPropertyConstants.UNIVERSITY_FISCAL_YEAR, t.getUniversityFiscalYear());
        crit.addEqualTo(OLEPropertyConstants.CHART_OF_ACCOUNTS_CODE, t.getChartOfAccountsCode());
        crit.addEqualTo(OLEPropertyConstants.ACCOUNT_NUMBER, t.getAccountNumber());
        crit.addEqualTo(OLEPropertyConstants.SUB_ACCOUNT_NUMBER, t.getSubAccountNumber());
        crit.addEqualTo(OLEPropertyConstants.OBJECT_CODE, t.getFinancialObjectCode());
        crit.addEqualTo(OLEPropertyConstants.SUB_OBJECT_CODE, t.getFinancialSubObjectCode());
        crit.addEqualTo(OLEPropertyConstants.BALANCE_TYPE_CODE, t.getFinancialBalanceTypeCode());
        crit.addEqualTo(OLEPropertyConstants.ENCUMBRANCE_DOCUMENT_TYPE_CODE, t.getFinancialDocumentTypeCode());
        crit.addEqualTo(OLEPropertyConstants.ORIGIN_CODE, t.getFinancialSystemOriginationCode());
        crit.addEqualTo(OLEPropertyConstants.DOCUMENT_NUMBER, t.getDocumentNumber());

        QueryByCriteria qbc = QueryFactory.newQuery(Encumbrance.class, crit);
        return (Encumbrance) getPersistenceBrokerTemplate().getObjectByQuery(qbc);
    }

    /**
     * Returns an Iterator of all encumbrances that need to be closed for the fiscal year
     * 
     * @param fiscalYear a fiscal year to find encumbrances for
     * @return an Iterator of encumbrances to close
     * @see org.kuali.ole.gl.dataaccess.EncumbranceDao#getEncumbrancesToClose(java.lang.Integer)
     */
    public Iterator getEncumbrancesToClose(Integer fiscalYear) {

        Criteria criteria = new Criteria();
        criteria.addEqualTo(OLEPropertyConstants.UNIVERSITY_FISCAL_YEAR, fiscalYear);

        QueryByCriteria query = new QueryByCriteria(Encumbrance.class, criteria);
        query.addOrderByAscending(OLEPropertyConstants.CHART_OF_ACCOUNTS_CODE);
        query.addOrderByAscending(OLEPropertyConstants.ACCOUNT_NUMBER);
        query.addOrderByAscending(OLEPropertyConstants.SUB_ACCOUNT_NUMBER);
        query.addOrderByAscending(OLEPropertyConstants.OBJECT_CODE);
        query.addOrderByAscending(OLEPropertyConstants.SUB_OBJECT_CODE);
        query.addOrderByAscending(OLEPropertyConstants.BALANCE_TYPE_CODE);

        return getPersistenceBrokerTemplate().getIteratorByQuery(query);
    }

    /**
     * Purges the database of all those encumbrances with the given chart and year 
     * 
     * @param chartOfAccountsCode the chart of accounts code purged encumbrances will have
     * @param year the university fiscal year purged encumbrances will have
     * @see org.kuali.ole.gl.dataaccess.EncumbranceDao#purgeYearByChart(java.lang.String, int)
     */
    public void purgeYearByChart(String chartOfAccountsCode, int year) {
        LOG.debug("purgeYearByChart() started");

        Criteria criteria = new Criteria();
        criteria.addEqualTo(OLEPropertyConstants.CHART, chartOfAccountsCode);
        criteria.addLessThan(OLEPropertyConstants.UNIVERSITY_FISCAL_YEAR, new Integer(year));

        getPersistenceBrokerTemplate().deleteByQuery(new QueryByCriteria(Encumbrance.class, criteria));

        // This is required because if any deleted account balances are in the cache, deleteByQuery
        // doesn't
        // remove them from the cache so a future select will retrieve these deleted account
        // balances from
        // the cache and return them. Clearing the cache forces OJB to go to the database again.
        getPersistenceBrokerTemplate().clearCache();
    }

    /**
     * fetch all encumbrance records from GL open encumbrance table
     * 
     * @return an Iterator with all encumbrances currently in the database
     * @see org.kuali.ole.gl.dataaccess.EncumbranceDao#getAllEncumbrances()
     */
    public Iterator getAllEncumbrances() {
        Criteria criteria = new Criteria();
        QueryByCriteria query = QueryFactory.newQuery(Encumbrance.class, criteria);
        return getPersistenceBrokerTemplate().getIteratorByQuery(query);
    }

    /**
     * group all encumbrances with/without the given document type code by fiscal year, chart, account, sub-account, object code,
     * sub object code, and balance type code, and summarize the encumbrance amount and the encumbrance close amount.
     * 
     * @param documentTypeCode the given document type code
     * @param included indicate if all encumbrances with the given document type are included in the results or not
     * @return an Iterator of arrays of java.lang.Objects holding summarization data about qualifying encumbrances 
     * @see org.kuali.ole.gl.dataaccess.EncumbranceDao#getSummarizedEncumbrances(String, boolean)
     */
    public Iterator getSummarizedEncumbrances(String documentTypeCode, boolean included) {
        Criteria criteria = new Criteria();

        if (included) {
            criteria.addEqualTo(OLEPropertyConstants.ENCUMBRANCE_DOCUMENT_TYPE_CODE, documentTypeCode);
        }
        else {
            criteria.addNotEqualTo(OLEPropertyConstants.ENCUMBRANCE_DOCUMENT_TYPE_CODE, documentTypeCode);
        }

        ReportQueryByCriteria query = QueryFactory.newReportQuery(Encumbrance.class, criteria);

        // set the selection attributes
        List attributeList = buildAttributeList();
        String[] attributes = (String[]) attributeList.toArray(new String[attributeList.size()]);
        query.setAttributes(attributes);

        // add the group criteria into the selection statement
        List groupByList = buildGroupByList();
        String[] groupBy = (String[]) groupByList.toArray(new String[groupByList.size()]);
        query.addGroupBy(groupBy);

        return getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(query);
    }

    /**
     * Queries the database to find all open encumbrances that qualify by the given keys
     * 
     * @param fieldValues the input fields and values
     * @return a collection of open encumbrances
     * @see org.kuali.ole.gl.dataaccess.EncumbranceDao#findOpenEncumbrance(java.util.Map)
     */
    public Iterator findOpenEncumbrance(Map fieldValues, boolean includeZeroEncumbrances) {
        LOG.debug("findOpenEncumbrance() started");

        Query query = this.getOpenEncumbranceQuery(fieldValues, includeZeroEncumbrances);
        OJBUtility.limitResultSize(query);
        return getPersistenceBrokerTemplate().getIteratorByQuery(query);
    }

    /**
     * Counts the number of open encumbrances that have the keys given in the map
     * 
     * @param fieldValues the input fields and values
     * @return the number of the open encumbrances
     * @see org.kuali.ole.gl.dataaccess.EncumbranceDao#getOpenEncumbranceRecordCount(java.util.Map)
     */
    public Integer getOpenEncumbranceRecordCount(Map fieldValues, boolean includeZeroEncumbrances) {
        LOG.debug("getOpenEncumbranceRecordCount() started");

        Query query = this.getOpenEncumbranceQuery(fieldValues, includeZeroEncumbrances);
        return getPersistenceBrokerTemplate().getCount(query);
    }

    /**
     * build the query for encumbrance search
     * 
     * @param fieldValues a Map of values to use as keys for the query
     * @param includeZeroEncumbrances should the query include encumbrances which have zeroed out?
     * @return an OJB query
     */
    protected Query getOpenEncumbranceQuery(Map fieldValues, boolean includeZeroEncumbrances) {
        Criteria criteria = OJBUtility.buildCriteriaFromMap(fieldValues, new Encumbrance());
        criteria.addIn(OLEPropertyConstants.BALANCE_TYPE_CODE, Arrays.asList(OLEConstants.ENCUMBRANCE_BALANCE_TYPE));
        if (!includeZeroEncumbrances) {
            Criteria nonZeroEncumbranceCriteria = new Criteria();
            nonZeroEncumbranceCriteria.addNotEqualToField(OLEPropertyConstants.ACCOUNT_LINE_ENCUMBRANCE_AMOUNT, OLEPropertyConstants.ACCOUNT_LINE_ENCUMBRANCE_CLOSED_AMOUNT);
            criteria.addAndCriteria(nonZeroEncumbranceCriteria);
        }
        return QueryFactory.newQuery(Encumbrance.class, criteria);
    }

    /**
     * This method builds the atrribute list used by balance searching
     * 
     * @return a List of encumbrance attributes that need to be summed
     */
    protected List buildAttributeList() {
        List attributeList = this.buildGroupByList();

        attributeList.add("sum(" + OLEPropertyConstants.ACCOUNT_LINE_ENCUMBRANCE_AMOUNT + ")");
        attributeList.add("sum(" + OLEPropertyConstants.ACCOUNT_LINE_ENCUMBRANCE_CLOSED_AMOUNT + ")");

        return attributeList;
    }

    /**
     * This method builds group by attribute list used by balance searching
     * 
     * @return a List of encumbrance attributes to search on
     */
    protected List buildGroupByList() {
        List attributeList = new ArrayList();

        attributeList.add(OLEPropertyConstants.UNIVERSITY_FISCAL_YEAR);
        attributeList.add(OLEPropertyConstants.CHART_OF_ACCOUNTS_CODE);
        attributeList.add(OLEPropertyConstants.ACCOUNT_NUMBER);
        attributeList.add(OLEPropertyConstants.SUB_ACCOUNT_NUMBER);
        attributeList.add(OLEPropertyConstants.OBJECT_CODE);
        attributeList.add(OLEPropertyConstants.SUB_OBJECT_CODE);
        attributeList.add(OLEPropertyConstants.BALANCE_TYPE_CODE);

        return attributeList;
    }
    
    /**
     * @see org.kuali.ole.gl.dataaccess.EncumbranceDao#findCountGreaterOrEqualThan(java.lang.Integer)
     */
    public Integer findCountGreaterOrEqualThan(Integer year) {
        Criteria criteria = new Criteria();
        criteria.addGreaterOrEqualThan(OLEPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
        
        ReportQueryByCriteria query = QueryFactory.newReportQuery(Encumbrance.class, criteria);
        
        return getPersistenceBrokerTemplate().getCount(query);
    }
}
