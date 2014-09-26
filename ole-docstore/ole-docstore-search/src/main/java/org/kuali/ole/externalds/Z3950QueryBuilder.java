package org.kuali.ole.externalds;

import org.kuali.ole.docstore.OleException;
import org.kuali.ole.docstore.discovery.model.SearchCondition;
import org.kuali.ole.docstore.discovery.model.SearchParams;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ND6967
 * Date: 2/20/13
 * Time: 9:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class Z3950QueryBuilder {
    static Map<String, Integer> fieldsMap = new HashMap<String, Integer>();


    static {

        fieldsMap.put("Title", 4);
        fieldsMap.put("Author", 1003);
    }

    public String buildQuery(SearchParams searchParams) throws OleException {

        StringBuffer queryStringBuffer = new StringBuffer();
        String query = "";
        String[] fieldsToIncludeInQuery = {"Title", "Author"};
        List listOfFieldsToIncludeInQuery = Arrays.asList(fieldsToIncludeInQuery);
        List<SearchCondition> finalFieldsToIncludeInQuery = new ArrayList<SearchCondition>();
        List<SearchCondition> searchConditionList = searchParams.getSearchFieldsList();
        //
        for (SearchCondition searchCondition : searchConditionList) {
            System.out.println("In for " + searchCondition.getDocField());
            if (searchCondition.getDocField() != null && !searchCondition.getDocField().equalsIgnoreCase("")) {
                System.out.println("In if one");
                if (listOfFieldsToIncludeInQuery.contains(searchCondition.getDocField())) {
                    System.out.println("In if two");
                    if (searchCondition.getSearchText() != null && !searchCondition.getSearchText()
                            .equalsIgnoreCase("")) {
                        System.out.println("In if three");
                        finalFieldsToIncludeInQuery.add(searchCondition);
                    }
                }
            }
        }
        System.out.println("finalFieldsToIncludeInQuery size " + finalFieldsToIncludeInQuery.size());
        for (SearchCondition searchCondition : finalFieldsToIncludeInQuery) {
            System.out.println("getSearchText " + searchCondition.getSearchText());
            System.out.println("getDocField " + searchCondition.getDocField());
            System.out.println("getOperator " + searchCondition.getOperator());
        }
        if (finalFieldsToIncludeInQuery.size() == 0) {
            SearchCondition searchCondition = searchParams.getSearchFieldsList().get(0);
            if (searchCondition.getSearchText() != null && !searchCondition.getSearchText()
                    .equalsIgnoreCase("")) {
                query = searchCondition.getSearchText();
            } else {
                System.out.println("No Field Selected Or Data entered");
                //query = "failure";
                throw new OleException("No Field Selected Or Data entered");
            }
        } else {
            query = prepareQuery(finalFieldsToIncludeInQuery);
            System.out.println("buildQuery " + query);
        }
        return query;
    }

    //@attrset bib-1 @attr 1=4 "XML" @attr 1=1003 "Sanderson"
    //@attrset bib-1 @or @attr 1=4 xml @attr 1=1003 Sanderson
    public String prepareQuery(List<SearchCondition> searchConditionList) {
        String partOfQuery = "@attrset bib-1";
        String query = "";
        StringBuffer sb = new StringBuffer(partOfQuery);
        if (searchConditionList.size() == 1) {
            SearchCondition searchCondition = searchConditionList.get(0);
            query = partOfQuery + " @attr 1=" + fieldsMap.get(searchCondition.getDocField()) + " " + searchCondition
                    .getSearchText();
        } else {
            for (int i = 0; i < searchConditionList.size(); i++) {
                SearchCondition searchCondition = searchConditionList.get(i);
                if (i == 0) {
                    sb.append(" ");
                    sb.append("@");
                    sb.append(searchCondition.getOperator());
                }
                sb.append(" @attr 1=" + fieldsMap.get(searchCondition.getDocField()) + " " + searchCondition.getSearchText());
            }
            query = sb.toString();
        }

        return query;
    }

}
