/**
 * Created with IntelliJ IDEA.
 * User: ?
 * Date: 11/29/12
 * Time: 5:57 PM
 * To change this template use File | Settings | File Templates.
 */

jq(document).ready(function(){
    jq(document).keypress(function(e) {
        if(e.which == 13) {
            if(e.target.id == "Item-barcode_control"){
            if(jq( "#Item-barcode_control").val()!=""){
                jq(this).blur();
                jq('#SecondarySearchItems').focus().click();
            }else{
                jq('#Item-barcode_control').focus();
                return false;
            }
        }
            return false;
        }

    });

    jq("input#billView_transactionNumber_control").live('keydown',function(event) {

        if (event.shiftKey || event.ctrlKey || event.altKey) {
            event.preventDefault();
        } else {
            var key = event.keyCode;
            if (!((key == 8) || (key == 32) || (key == 46) || (key >= 35 && key <= 40) || (key >= 65 && key <= 90) || (key >= 48 && key <= 57) || (key >= 96 && key <= 105))) {
                event.preventDefault();
            }
        }
    });

    if(jq("#ItemBillView h1").hasClass("uif-headerText")){
        jq("#ItemBillView h1").removeClass();
    }

    jq("button#Accept_Button").live("click",function(event){
        submitForm('accept', null, null, null, null);
        jq("#paidAmount_control").prop("value","0.0");
        jq("#billpayment_doubleSubmit_control").prop("value","true");
    })
    jq("button#Mail_Button").live("click",function(event){
        submitForm('mailToPatron', null, null, null, null);
    })
    jq("button#BillView_selectBill").live("click",function(event){
        submitForm('selectAllBill', null, null, null, null);
    })
    jq("button#BillView_deselectBill").live("click",function(event){
        submitForm('deSelectAll', null, null, null, null);
    })
    jq("button#BillView_selectItem").live("click",function(event){
        submitForm('selectAllItem', null, null, null, null);
    })
    jq("button#BillView_deselectItem").live("click",function(event){
        submitForm('deSelectAll', null, null, null, null);
    })
    if(jq("#ReviewBillSection_div").length > 0) {
        jq("#ReviewBillSection_div th.col1").children().append("<input type='checkbox' value='false' class='selectAllCB' name='selectAll' id='selectAll' style='float:left;margin-left:0.5em'>");
    }

    jq(".selectAllCB").click(function(){
        if(jq(".selectAllCB").is(":checked")){
            jq(".billReviewClass").attr("checked","true");
        }else{
            jq(".billReviewClass").removeAttr("checked");
        }
    });

    if(jq("#ReviewedBillSection_div").length > 0) {
        jq("#ReviewedBillSection_div th.col1").children().append("<input type='checkbox' value='false' class='selectAllRDB' name='selectAll' id='selectAll' style='float:left;margin-left:0.5em'>");
    }

    jq(".selectAllRDB").click(function(){
        if(jq(".selectAllRDB").is(":checked")){
            jq(".billReviewedClass").attr("checked","true");
        }else{
            jq(".billReviewedClass").removeAttr("checked");
        }
    });

    jq(".fancybox-close").live("click" ,function(e) {
        if(jq.find("div#cancelNoteDialog")!=""){
            submitForm('start', null, null, null, null);
        }
    });
    jq("div#DetailedView-BillSection .dataTables_filter").live("keypress",function(e){
        jq(this).find("input").attr("id","DetailedView-BillSection _disclosureContent_Search_d");
    })

    jq("div#BillSection .dataTables_filter").live("keypress",function(e){
        jq(this).find("input").attr("id","BillSection_disclosureContent_Search_d");
    })
    jq("input#billpayment_userAmount_control").val(parseFloat("0"));
    removeCurrencyPattern();

});
function calculateTotal(){
    jq("#calculate").focus().click();

}

function addFee(){
if(jq("#feeAmt_add_control").val()!=null) {
    jq("#addFeeHiddenValue_add").focus().click();
} else{
    jq("#addFeeHidden_add").focus().click();
}
}

function closeForgiveNote(){
    if(jq("#forgiveNote_control").val().length > 0){
        jQuery.fancybox.close();
        submitForm('forgive', null, null, null, null);
    }else{
        jq("#forgiveNote_control").focus().click();
    }
}

function closeErrorNote(){
    if(jq("#errorNote_control").val().length > 0){
        jQuery.fancybox.close();
        submitForm('error', null, null, null, null);
    }else{
        jq("#errorNote_control").focus().click();
    }
}

function closeCancelNote(){
    if(jq("#cancellationNote_cancel_control").val().length > 0){
        jQuery.fancybox.close();
        submitForm('cancellationNote', null, null, null, null);
    }else{
        jq("#cancellationNote_cancel_control").focus().click();
    }
}

function selectBill(amt, obj) {
    var totalAmt = parseFloat("0");
    var userAmount=parseFloat(jq("input#billpayment_userAmount_control").val());
    if (jq("input#billView_paymentDetails_paidAmount_control").val() != "") {
        totalAmt = parseFloat(jq("input#billView_paymentDetails_paidAmount_control").val());
    }
    if (jq("#hdBillPay_control").val() == 'itemwise') {
        jq("input#billView_paymentDetails_paidAmount_control").val(parseFloat("0"));
        jq("input#billpayment_userAmount_control").val(parseFloat("0"));
        userAmount=parseFloat(jq("input#billpayment_userAmount_control").val());
        totalAmt=parseFloat("0");
    }
    if (jq(obj).prop('checked')) {
        totalAmt = parseFloat(totalAmt) + parseFloat(amt);
        userAmount=userAmount+parseFloat(amt);
    } else {
        totalAmt = parseFloat(totalAmt) - parseFloat(amt);
        userAmount=userAmount-parseFloat(amt);
    }
    totalAmt=userAmount;
    jq("input#billpayment_userAmount_control").val(userAmount);
    jq("input#billView_paymentDetails_paidAmount_control").val(totalAmt.toFixed(2));
    if (jq(".selectBill").is(":checked")) {
        jq(".selectItem").removeAttr("checked");
        jq("#hdBillPay_control").val("billwise");

    } else {
        jq("#hdBillPay_control").val("default");

    }

}
function selectItem(amt, obj) {
    var totalAmt = parseFloat("0");
    var userAmount=parseFloat(jq("input#billpayment_userAmount_control").val());
    if (jq("input#billView_paymentDetails_paidAmount_control").val() != "") {
        totalAmt = parseFloat(jq("input#billView_paymentDetails_paidAmount_control").val());
    }
    if (jq("#hdBillPay_control").val() == 'billwise') {
        jq("input#billView_paymentDetails_paidAmount_control").val(parseFloat("0"));
        jq("input#billpayment_userAmount_control").val(parseFloat("0"));
        userAmount=parseFloat(jq("input#billpayment_userAmount_control").val());
        totalAmt=parseFloat("0");
    }
    if (jq(obj).prop('checked')) {
        totalAmt = parseFloat(totalAmt) + parseFloat(amt);
        userAmount=userAmount+parseFloat(amt);
    } else {
        totalAmt = parseFloat(totalAmt) - parseFloat(amt);
        userAmount=userAmount-parseFloat(amt);
    }
    totalAmt=userAmount;
    jq("input#billpayment_userAmount_control").val(userAmount);
    jq("input#billView_paymentDetails_paidAmount_control").val(totalAmt.toFixed(2));
    if (jq(".selectItem").is(":checked")) {
        jq(".selectBill").removeAttr("checked");
        jq("#hdBillPay_control").val("itemwise");
    } else {
        jq("#hdBillPay_control").val("default");
    }
}
function printPatronBill(){
    removeCurrencyPattern();
    if(jq("#hdnPatronBillPrint_control").val() == "true" && jq("#hdnPrintBillReview_control").val() == "true") {
        window.open("patronbill?viewId=BillView&methodToCall=print&formKey="+jq("#printFormKey_control").val());
    }
}

function closePatronBillWindow(){
    self.window.close();
}
function removeCurrencyPattern(){
    if (jq("input#billView_paymentDetails_paidAmount_control").val()!=undefined) {
        var amt = jq("input#billView_paymentDetails_paidAmount_control").val().replace(/[^\d.]/g,"");
        jq("input#billView_paymentDetails_paidAmount_control").val(amt);
    }
}