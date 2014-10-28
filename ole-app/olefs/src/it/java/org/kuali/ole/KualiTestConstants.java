package org.kuali.ole;

import org.kuali.rice.core.api.util.type.KualiDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: pvsubrah
 * Date: 10/2/13
 * Time: 11:58 AM
 * To change this template use File | Settings | File Templates.
 */
public interface KualiTestConstants {

    /**
     * contains Test related constants
     */
    public final class TestConstants {
        private static final String HOST = "localhost";
        private static final String PORT = "8080";
        public static final String BASE_PATH = "http://" + HOST + ":" + PORT + "/";
        public static final String MESSAGE = "JUNIT test entry. If this exist after the tests are not cleaning up correctly. Created by class";
        public static final String TEST_BATCH_STAGING_DIRECTORY = "/java/projects/kuali_project/test/unit/src/org/kuali/test/staging/";

        /**
         * group of data values that should work if used togther
         */
        public static class Data1 {
            public final static String ACCOUNT_NUMBER = "9544900";
            public final static String CHART_OF_ACCOUNTS_CODE = "BA";
            public final static String OBJECT_CODE = "9912";
            public final static String ORGANIZATION_CODE = "PARK";
            public final static String PERSON_UNIVERSAL_IDENTIFIER = "4219606069";
            public final static Integer UNIVERSITY_FISCAL_YEAR = new Integer(2004);

            public static final String OBJECT_TYPE_CODE = "AS";
        }

        public static class Data2 {
            public final static String AUTHENTICATION_USER_ID = "ole-khuntley";
            public final static String AUTHENTICATION_USER_ID_THAT_IS_SUPERVISOR = "ole-heagle";
        }

        public static class Data3 {
            public static final Long DOC_HDR_ID = new Long(100000);
            public static final String CHART = "UA";
            public static final String ACCOUNT = "1912610";
            public static final String SUBACCOUNT = "AUCAP";
            public static final String OBJCODE_SOURCE = "4166";
            public static final String SUBOBJCODE_SOURCE = "FIS";
            public static final String OBJCODE_TARGET = "5000";
            public static final String SUBOBJCODE_TARGET = "A/R";
            public static final String PROJECT = "BOB";
            public static final KualiDecimal LINEAMT = new KualiDecimal("21.12");
            public static final String OBJECT_TYPE_CODE = "AS";
            public static final String DEBIT_CREDIT_CODE = "D";
            public static final String ENCUMBRANCE_UPDATE_CODE = "Y";

            public static final Integer BILLING_ITEM_QUANTITY = new Integer(5);
            public static final String BILLING_ITEM_STOCK_DESCRIPTION = "steer";
            public static final String BILLIING_ITEM_STOCK_NUMBER = "M000";
            public static final Double BILLING_ITEM_UNIT_AMOUNT = new Double("2.0");
            public static final String BILLING_ITEM_UNIT_OF_MEASUREMENT_CODE = "hd";
            public static final Integer POSTING_YEAR = new Integer(2004);
            public static final Integer SEQUENCE_NUMBER = new Integer(1);
        }

        public static class Data4 {
            public static final String ACCOUNT = "1031400";
            public static final String ACCOUNT2 = "5731402";
            public static final KualiDecimal AMOUNT = new KualiDecimal("2.50");
            public static final String BALANCE_TYPE_CODE = "AC";
            public static final String CHART_CODE = "BL";
            public static final String CHART_CODE_UA = "UA";
            public static final String CHART_CODE_BA = "BA";
            public static final String DOC_HDR_ID = "1005";
            public static final String OBJECT_CODE = "3000";
            public static final String OBJECT_CODE2 = "5099";
            public static final Integer POSTING_YEAR = org.kuali.ole.TestUtils.getFiscalYearForTesting();
            public static final String PROJECT_CODE = "KUL";
            public static final Integer SEQUENCE_NUMBER = new Integer(1);
            public static final String SUBACCOUNT = "AUCAP";
            public static final String SUBACCOUNT2 = "ADV";
            public static final String SUBOBJECT_CODE = "WTS";
            public static final String OBJECT_TYPE_CODE = "AS";
            public static final String DEBIT_CREDIT_CODE = "D";
            public static final String ENCUMBRANCE_UPDATE_CODE = "Y";
            public static final String ORG_REFERENCE_ID = "12345678";
            public static final String OVERRIDE_CODE = "O";
            public static final String REF_NUMBER = "123456789";
            public static final String REF_ORIGIN_CODE = "AB";
            public static final String REF_TYPE_CODE = "ABCD";
            public static final String USER_ID1 = "ole-vputman";
            public static final String USER_ID2 = "ole-khuntley";
        }

        public static class Data5 {
            public static final String BUDGET_AGGREGATION_CODE1 = "O";
            public static final String BUDGET_AGGREGATION_NAME1 = "OBJECT";
            public static final String BUDGET_AGGREGATION_CODE2 = "L";
            public static final String BUDGET_AGGREGATION_NAME2 = "OBJECT LEVEL";

            public static final String FEDERAL_FUNDED_CODE1 = "F";
            public static final String FEDERAL_FUNDED_NAME1 = "FEDERALLY FUNDED AND OWNED";
            public static final String FEDERAL_FUNDED_CODE_BAD = "A";
            public static final String FEDERAL_FUNDED_NAME_BAD = "This is a bad code name";
        }

        public static class PositionObjectTestData {
            public static final String UNIVERSITY_FISCAL_YEAR = "2011";
            public static final String CHART_OF_ACCOUNTS_CODE = "SB";
            public static final String FINANCIAL_OBJECT_CODE = "2504";
        }


        public static class BenefitsCalculationServiceImplTest {
            public static final String FISCAL_YEAR = "2009";
            public static final String CHART = "BA";
            public static final String POSITION_TYPE_CODE = "B";
        }

        public static class BankCodeTestData {
            public static final String BANK_CODE = "TEST";
        }
    }
}
