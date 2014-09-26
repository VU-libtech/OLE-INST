package org.kuali.ole.externalds;
// NOTE: Uncomment this file to enable Searching external Z39.50 data sources.

//import net.sf.jz3950.Association;
//import net.sf.jz3950.RecordResultSet;
//import net.sf.jz3950.query.PrefixQuery;
//import org.kuali.ole.docstore.OleException;
//import org.kuali.ole.docstore.discovery.model.SearchParams;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by IntelliJ IDEA.
// * User: ND6967
// * Date: 2/19/13
// * Time: 12:42 PM
// * To change this template use File | Settings | File Templates.
// */
public class Z3950DataSource
        extends AbstractExternalDataSource {
//    private DataSourceConfig dataSourceConfig;
//    private static final int RESULT_SET_ITERATION_LIMIT = 80;
//
//    @Override
//    public List<String> searchForBibs(SearchParams searchParams, DataSourceConfig dataSourceConfigInfo)
//            throws Exception, OleException {
//
//        Association association = new Association();
//        String domainName = dataSourceConfigInfo.getDomainName();
//        String port = dataSourceConfigInfo.getPortNum();
//        System.out.println("searchForBibs getDomainName " + dataSourceConfigInfo.getDomainName());
//        System.out.println("searchForBibs getPortNum " + dataSourceConfigInfo.getPortNum());
//        association.connect(domainName, Integer.parseInt(port));
//        System.out.println("connected..........");
//        //String query="@attrset bib-1 @attr 1=4 \"advanced java\"";
//        Z3950QueryBuilder z3950QueryBuilde = new Z3950QueryBuilder();
//        String query = z3950QueryBuilde.buildQuery(searchParams);
//        System.out.println("Z3950DataSource : searchForBibs : query " + query);
//        List<String> results = iterateOverPartOfResultSet(association.search(new PrefixQuery("Voyager", query)));
//        System.out.println("searchForBibs: results size " + results.size());
//        association.disconnect();
//        return results;
//    }
//
//    private List iterateOverPartOfResultSet(RecordResultSet resultSet) {
//        List<String> results = new ArrayList<String>();
//        System.out.println(("Iterating over maximum of " + RESULT_SET_ITERATION_LIMIT + " record(s) from "
//                + resultSet.getTotalResults() + " record(s) in total"));
//
//        int recordCount = 0;
//
//        //         while(resultSet.hasNext()){
//        //             results.add(resultSet.next().getData());
//        //           //  System.out.println("iterateOverPartOfResultSet ");
//        //
//        //
//        //             if (++recordCount == RESULT_SET_ITERATION_LIMIT) {
//        //                 break;
//        //             }
//        //
//        //        }
//        for (net.sf.jz3950.record.Record result : resultSet) {
//            //  System.out.println("iterateOverPartOfResultSet "+result);
//            results.add(result.getData());
//
//            if (++recordCount == RESULT_SET_ITERATION_LIMIT) {
//                break;
//            }
//        }
//        return results;
//    }
}
