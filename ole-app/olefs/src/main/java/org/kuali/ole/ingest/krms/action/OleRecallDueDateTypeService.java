package org.kuali.ole.ingest.krms.action;

import org.apache.log4j.Logger;
import org.kuali.ole.DataCarrierService;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.OLEParameterConstants;
import org.kuali.ole.deliver.processor.LoanProcessor;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krms.api.engine.ExecutionEnvironment;
import org.kuali.rice.krms.api.repository.action.ActionDefinition;
import org.kuali.rice.krms.framework.engine.Action;
import org.kuali.rice.krms.impl.type.ActionTypeServiceBase;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: vivekb
 * Date: 02/05/13
 * Time: 3:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class OleRecallDueDateTypeService extends ActionTypeServiceBase {
    private static final Logger LOG = Logger.getLogger(OleRecallDueDateTypeService.class);
    @Override
    public Action loadAction(ActionDefinition actionDefinition) {
        String minimumLoanPeriod= actionDefinition.getAttributes().get(OLEConstants.MINIMUM_LOAN_PERIOD);
        String recallLoanPeriod= actionDefinition.getAttributes().get(OLEConstants.RECALL_LOAN_PERIOD);
        return  new OleRecallDueDate(minimumLoanPeriod,recallLoanPeriod);
    }
    public class OleRecallDueDate implements Action {
        private String minimumLoanPeriod;
        private String recallLoanPeriod;


        public OleRecallDueDate(String minimumLoanPeriod, String recallLoanPeriod) {
            this.minimumLoanPeriod = minimumLoanPeriod;
            this.recallLoanPeriod = recallLoanPeriod;
        }

        @Override
        public void execute(ExecutionEnvironment environment) {
            Timestamp recallDueDate = null;
            LoanProcessor loanProcessor = new LoanProcessor();
            DataCarrierService dataCarrierService = GlobalResourceLoader.getService(OLEConstants.DATA_CARRIER_SERVICE);
            Calendar calendar = Calendar.getInstance();
            Date loanedDate  = (Date)dataCarrierService.getData(OLEConstants.LOANED_DATE);
            Date loanDueDate=(Date)dataCarrierService.getData(OLEConstants.DUE_DATE);
            if(loanedDate!=null && minimumLoanPeriod!=null && recallLoanPeriod!=null){
                calendar.setTime(loanedDate);
                Timestamp minimumLoanDueDate = calculateLoanDueDate(calendar,minimumLoanPeriod);
                if(minimumLoanDueDate.compareTo(new Date())>0)   {
                    recallDueDate = calculateLoanDueDate(calendar,recallLoanPeriod);
                } else{
                    calendar = Calendar.getInstance();
                    recallDueDate = calculateLoanDueDate(calendar,recallLoanPeriod);
                }
                String defaultCloseTime = loanProcessor.getParameter(OLEParameterConstants.DEF_CLOSE_TIME);
                recallDueDate = Timestamp.valueOf(new SimpleDateFormat(OLEConstants.CHECK_IN_DATE_TIME_FORMAT).
                        format(recallDueDate).concat(" ").concat(defaultCloseTime));
                if(recallDueDate!=null && loanDueDate!=null && recallDueDate.compareTo(loanDueDate)>=0){
                    environment.getEngineResults().setAttribute(OLEConstants.RECALL_DUE_DATE,loanDueDate);
                }else{
                    environment.getEngineResults().setAttribute(OLEConstants.RECALL_DUE_DATE,recallDueDate);
                }
                LOG.info("minimumLoanPeriod---------->"+minimumLoanPeriod);
                LOG.info("recallLoanPeriod---------->"+recallLoanPeriod);
            }
        }

        @Override
        public void executeSimulation(ExecutionEnvironment environment) {
            execute(environment);
        }
        private Timestamp calculateLoanDueDate(Calendar calendar,String loanPeriod) {
            String loanPeriodType[]=null;
            Timestamp dueDate = null;
            if(loanPeriod != null && loanPeriod.trim().length()>0){
                loanPeriodType =  loanPeriod.split("-");
                int loanPeriodValue =  Integer.parseInt(loanPeriodType[0].toString());
                String loanPeriodTypeValue =  loanPeriodType[1].toString();
                if(loanPeriodTypeValue.equalsIgnoreCase("M")){
                    calendar.add(Calendar.MINUTE, loanPeriodValue);
                } else if(loanPeriodTypeValue.equalsIgnoreCase("H")) {
                    calendar.add(Calendar.HOUR, loanPeriodValue);
                } else if(loanPeriodTypeValue.equalsIgnoreCase("W")) {
                    calendar.add(Calendar.WEEK_OF_MONTH, loanPeriodValue);
                } else {
                    calendar.add(Calendar.DATE, loanPeriodValue);
                }
                dueDate =  new Timestamp(calendar.getTime().getTime());
            }
            return dueDate;
        }
    }
}
