function displayDialogWindow(divID){
    jq(divID).fadeIn(300);
    var popMargTop = (jq(divID).height() + 42) / 2;
    var popMargLeft = (jq(divID).width() + 62) / 2;
    jq(divID).css({
        'margin-top' : -popMargTop,
        'margin-left' : -popMargLeft
    });
    jq(divID).draggable();
    jq('body').append('<div id="mask"></div>');
    jq('#mask').fadeIn(300);
}

function approve(){
    if(jq("#hdnduplicateFlag_control").val() == 'true' || jq('#hdnduplicateFlag span').text().trim() =='true') {
        displayDialogWindow("div#OLEInvoice-DuplicationPopUp");
    }
    else if (jq("#hdnAmountExceeds_control").val() == 'true' || jq('#hdnAmountExceeds span').text().trim() =='true'){
        displayDialogWindow("div#MessagePopupSectionForInvoiceAmountExceedsThreshold");
    }
}

function closeDuplicationMessage() {
    jq("div#OLEInvoice-DuplicationPopUp").fadeOut(300);
    jq('#mask').fadeOut(300);
    if (jq("#hdnAmountExceeds_control").val() == 'true' || jq('#hdnAmountExceeds span').text().trim() =='true'){
        displayDialogWindow("div#MessagePopupSectionForInvoiceAmountExceedsThreshold");
    }
    }

function closeDuplicationApprovalMessage() {
    jq("div#OLEInvoice-DuplicationApprovePopUp").fadeOut(300);
    jq('#mask').fadeOut(300);
    if(jq("#hdnSfcFlagForBlankApp_control").val() == 'true') {
        displayDialogWindow("div#MessagePopupSectionForInvoiceBlankApp");
    }
    else if(jq("#hdnAmountExceedsForBlanketApprove_control").val() == 'true'){
        displayDialogWindow("div#MessagePopupSectionForInvoiceAmountExceedsForBlanketApprove");
    }
    else {
        validateInvoiceSubscriptionBlanketApprove();
    }
}

jq("#invoice-vendorHeaderIdentifier_control").live("change",function() {
    jq('#invoiceVendorBtn').click();
    jq("#unsaved_control").val("true");
});

jq("#invoice-currencyType_control").live("change", function() {
    jq('#invoiceCurrencyTypeBtn').click();
});


/*function MessageInvoiceCurrencyPopup() {
    if(jq("#hiddenCurrencyOverrideBtn_control").val() == "true") {
        displayDialogWindow("div#MessagePopUpSectionForCurrencyOverride");
    }
}*/
/*jq("#invoice-invoiceNumber").live("focusout",function() {
    jq('#invoiceNumberBtn').click();
});*/

jq(".prorateByQuantityClass").live("click",function(){
    jq(".prorateByDollarClass").removeAttr("checked");
    jq(".noProrateClass").removeAttr("checked");
    jq(".prorateByManualClass").removeAttr("checked");
    jq("#"+jq(this).attr('id')).attr("checked","true");
    jq('#proratedSurchargeBtn').click();

});

jq(".prorateByDollarClass").live("click",function(){
    jq(".prorateByQuantityClass").removeAttr("checked");
    jq(".noProrateClass").removeAttr("checked");
    jq(".prorateByManualClass").removeAttr("checked");
    jq("#"+jq(this).attr('id')).attr("checked","true");
    jq('#proratedSurchargeBtn').click();

});

jq(".prorateByManualClass").live("click",function(){
    jq(".prorateByQuantityClass").removeAttr("checked");
    jq(".prorateByDollarClass").removeAttr("checked");
    jq(".noProrateClass").removeAttr("checked");
    jq("#"+jq(this).attr('id')).attr("checked","true");
    jq('#proratedSurchargeBtn').click();
});

jq(".noProrateClass").live("click",function(){
    jq(".prorateByQuantityClass").removeAttr("checked");
    jq(".prorateByDollarClass").removeAttr("checked");
    jq(".prorateByManualClass").removeAttr("checked");
    jq("#"+jq(this).attr('id')).attr("checked","true");
    jq('#proratedSurchargeBtn').click();

});

function selectNoProrate(){
    jq(".noProrateClass").removeAttr("checked");
    jq(".prorateByQuantityClass").removeAttr("checked");
    jq(".prorateByDollarClass").removeAttr("checked");
    jq("#myAccount_noProrate_control").attr("checked","true");
    if(jq(".prorateByManualClass").is(':checked')) {

        jq('#proratedSurchargeBtn').click();
    }
    jq(".prorateByManualClass").removeAttr("checked");
}


function route(){
    if(jq("#hdnSfcFlag_control").val() == 'true') {
        displayDialogWindow("div#MessagePopupSectionForInvoice");
    } 
    else{
        validateInvoiceSubscriptionApprove();
    }
}

function blanketApprove(){
    if(jq("#hdnduplicateApproveFlag_control").val() == 'true' || jq('#hdnduplicateApproveFlag span').text().trim() =='true') {
        displayDialogWindow("div#OLEInvoice-DuplicationApprovePopUp");
    }
    else if(jq("#hdnSfcFlagForBlankApp_control").val() == 'true') {
        displayDialogWindow("div#MessagePopupSectionForInvoiceBlankApp");
    }
    else if(jq("#hdnAmountExceedsForBlanketApprove_control").val() == 'true') {
        jq("#unsaved_control").val(false);
        displayDialogWindow("div#MessagePopupSectionForInvoiceAmountExceedsForBlanketApprove");
    }
    else if(jq("#hdnBlanketApproveValidationFlag_control").val() == 'true') {
        jq("#unsaved_control").val(false);
        displayDialogWindow("div#MessagePopupSectionForBlanketApproveInvoiceValidation");
        jq('#mask').fadeOut(300);
    }
    else{
        validateInvoiceSubscriptionBlanketApprove();
    }

}

function closeInvoicePopUp(){
    jq("div#MessagePopupSectionForInvoice").fadeOut(300);
    jq("#mask").fadeOut(300);
    validateInvoiceSubscriptionApprove();
}

function closeInvoiceApprovePopUp(){
    jq("div#MessagePopupSectionForInvoiceBlankApp").fadeOut(300);
    jq("#mask").fadeOut(300);
    validateInvoiceSubscriptionBlanketApprove();
}
function refreshCurrentItems(){
    jq('#hiddenButtonForCurrentItems').focus().click();
}

function closeInvoiceValidationPopUp(){
    jq("div#MessagePopupSectionForInvoiceValidation").fadeOut(300);
    jq("#mask").fadeOut(300);
}

function closeInvoiceBlanketApproveValidationPopUp(){
    jq("div#MessagePopupSectionForBlanketApproveInvoiceValidation").fadeOut(300);
    jq("#mask").fadeOut(300);
}

function closeInvoiceBlanketApproveSubscriptionDateValidationPopUp(){
    jq("div#MessagePopupSectionForBlanketApproveSubscriptionDateValidation").fadeOut(300);
    jq("#mask").fadeOut(300);
    validateInvoiceAmountBlanketApprove();
}

function closeInvoiceSubscriptionDateValidationPopUp(){
    jq("div#MessagePopupSectionForSubscriptionDateValidation").fadeOut(300);
    jq("#mask").fadeOut(300);
    validateInvoiceAmount();
}

function closeInvoiceAmountExceedsApprovalPopUp(){
    jq("div#MessagePopupSectionForInvoiceAmountExceedsThreshold").fadeOut(300);
    jq("#mask").fadeOut(300);
    }

function closeInvoiceAmountExceedsBlankApprovalPopUp(){
    jq("div#MessagePopupSectionForInvoiceAmountExceedsForBlanketApprove").fadeOut(300);
    jq("#mask").fadeOut(300);
    if(jq("#hdnSfcFlagForBlankApp_control").val() == 'true') {
        displayDialogWindow("div#MessagePopupSectionForInvoiceBlankApp");
    }
    else if(jq("#hdnBlanketApproveValidationFlag_control").val() == 'true') {
        jq("#unsaved_control").val(false);
        displayDialogWindow("div#MessagePopupSectionForBlanketApproveInvoiceValidation");
        jq('#mask').fadeOut(300);
    }
    else{
        validateInvoiceSubscriptionBlanketApprove();
    }
}

function onChangePriceScript() {
    jq("#updatePriceBtn").focus().click();
   /* writeHiddenToForm('methodToCall', 'updatePrice');
    jQuery.fancybox.close();
    jQuery('#kualiForm').submit();*/
}

function validateInvoiceAmount(){
    if(jq("#hdnValidationFlag_control").val() == 'true') {
        displayDialogWindow("div#MessagePopupSectionForInvoiceValidation");
        jq('#mask').fadeOut(300);
    }else{
        unsaved();
    }
}

function unsaved(){
    jq("#unsaved_control").val("false");
}

function validateInvoiceAmountBlanketApprove(){
	if(jq("#hdnBlanketApproveValidationFlag_control").val() == 'true') {
        displayDialogWindow("div#MessagePopupSectionForBlanketApproveInvoiceValidation");
        jq('#mask').fadeOut(300);
    }
    else{
        if(jq("#unsaved_control").val()=='false'){
        //    jq("#invoice_close_btn").focus().click();
            unsaved();
        }

    }
}

function validateInvoiceSubscriptionBlanketApprove(){
    if(jq("#hdnBlanketApproveSubscriptionDateValidationFlag_control").val() == 'true') {
        displayDialogWindow("div#MessagePopupSectionForBlanketApproveSubscriptionDateValidation");
        jq('#mask').fadeOut(300);
    }
    else{
        validateInvoiceAmountBlanketApprove();
        }


}

function validateInvoiceSubscriptionApprove(){
    if(jq("#hdnSubscriptionDateValidationFlag_control").val() == 'true') {
        displayDialogWindow("div#MessagePopupSectionForSubscriptionDateValidation");
        jq('#mask').fadeOut(300);
    }
    else{
        validateInvoiceAmount();
        }


}

function closeDocument(){
    if(jq("#unsaved_control").val()=='false' && jq("#hdnblankApproveFlag_control").val()=='true'){
        jq("#invoice_close_btn").focus().click();
        unsaved();
    }
    jq("#hdnblankApproveFlag_control").val(true);
}
function toggleCollapseInvoice() {
    if(jq("#OLEInvoiceView-vendor_disclosureContent").css("display")=="block"){
        jq("#OLEInvoiceView-vendor_toggle").focus().click();
    }
    if(jq("#OLEInvoiceView-invoiceInfo_disclosureContent").css("display")=="block"){
        jq("#OLEInvoiceView-invoiceInfo_toggle").focus().click();
    }
   /* if(jq("#OleInvoiceDocument-invoiceItems_disclosureContent").css("display")=="block"){
        jq("#OleInvoiceDocument-invoiceItems_toggle").focus().click();
    }*/
    if(jq("#OLEInvoice-ProcessItem-AdditionalCharges_disclosureContent").css("display")=="block"){
        jq("#OLEInvoice-ProcessItem-AdditionalCharges_toggle").focus().click();
    }
    if(jq("#OLEInvoiceView-accountSummary_disclosureContent").css("display")=="block"){
        jq("#OLEInvoiceView-accountSummary_toggle").focus().click();
    }
    if(jq("#Uif-Inv-DocumentNotesSection_disclosureContent").css("display")=="block"){
        jq("#Uif-Inv-DocumentNotesSection_toggle").focus().click();
    }
    if(jq("#Uif-Inv-DocumentAdHocRecipientsSection_disclosureContent").css("display")=="block"){
        jq("#Uif-Inv-DocumentAdHocRecipientsSection_toggle").focus().click();
    }
    if(jq("#Uif-Inv-DocumentRouteLogSection_disclosureContent").css("display")=="block"){
        jq("#Uif-Inv-DocumentRouteLogSection_toggle").focus().click();
    }
    if(jq("#Uif-Inv-DocumentOverviewSection_disclosureContent").css("display")=="block"){
        jq("#Uif-Inv-DocumentOverviewSection_toggle").focus().click();
    }
}


function toggleCollapseAllSections() {
    if(jq("#Uif-Inv-DocumentOverviewSection_disclosureContent").css("display")=="block" && jq( "#overviewFlag_control").val()=="false"){
        jq("#Uif-Inv-DocumentOverviewSection_toggle").focus().click();
    }
    if(jq("#OLEInvoiceView-vendor_disclosureContent").css("display")=="block" && jq( "#vendorInfoFlag_control").val()=="false"){
        jq("#OLEInvoiceView-vendor_toggle").focus().click();
    }
    if(jq("#OLEInvoiceView-invoiceInfo_disclosureContent").css("display")=="block" && jq( "#invoiceInfo_control").val()=="false"){
        jq("#OLEInvoiceView-invoiceInfo_toggle").focus().click();
    }
    if(jq("#OleInvoiceDocument-invoiceItems_disclosureContent").css("display")=="block" && jq( "#currentItemsFlag_control").val()=="false"){
        jq("#OleInvoiceDocument-invoiceItems_toggle").focus().click();
    }
    if(jq("#OLEInvoiceView-ProcessItem-AdditionalCharges_disclosureContent").css("display")=="block" && jq( "#additionalChargesFlag_control").val()=="false"){
        jq("#OLEInvoiceView-ProcessItem-AdditionalCharges_toggle").focus().click();
    }
    /* if(jq("#OLEInvoice-ProcessItem-AdditionalCharges_disclosureContent").css("display")=="block" && jq( "#vendorInfoFlag_control").val()=="false"){
     jq("#OLEInvoice-ProcessItem-AdditionalCharges_toggle").focus().click();
     }*/
    if(jq("#OLEInvoiceView-accountSummary_disclosureContent").css("display")=="block" && jq( "#accountSummaryFlag_control").val()=="false"){
        jq("#OLEInvoiceView-accountSummary_toggle").focus().click();
    }
    if(jq("#Uif-Inv-DocumentNotesSection_disclosureContent").css("display")=="block" && jq( "#notesAndAttachmentFlag_control").val()=="false"){
        jq("#Uif-Inv-DocumentNotesSection_toggle").focus().click();
    }
    if(jq("#Uif-Inv-DocumentAdHocRecipientsSection_disclosureContent").css("display")=="block" && jq( "#adHocRecipientsFlag_control").val()=="false"){
        jq("#Uif-Inv-DocumentAdHocRecipientsSection_toggle").focus().click();
    }
    if(jq("#Uif-Inv-DocumentRouteLogSection_disclosureContent").css("display")=="block" && jq( "#routeLogFlag_control").val()=="false"){
        jq("#Uif-Inv-DocumentRouteLogSection_toggle").focus().click();
    }
}

function clonePopUp(){
    if(jq("#hdnCloneFlag_control").val() == 'true' || jq('#hdnCloneFlag span').text().trim() =='true') {
        displayDialogWindow("div#OLEInvoice-ClonePopUp");
        jq("#mask").fadeOut(300);
    }
}

jq(document).keypress(function(e) {
    if(e.which == 13) {
        if(e.target.id == "OleInvoice_POLookup_control"){
            if(jq( "#OleInvoice_POLookup_control").val()!=""){
                jq(this).blur();
                jq('#addProcessItem-button').focus().click();
                return false;
            }
            else{
                jq("#OleInvoice_POLookup_control").focus();
                return false;
            }
        }
    }
});

jq(document).ready(function(){
    jq(":input").live("keypress",function(){
        jq("#unsaved_control").val("true");
    });
    function unloadPage(){
        if(jq("#unsaved_control").val()=='true'){
            return confirm( getMessage(kradVariables.MESSAGE_KEY_DIRTY_FIELDS));
        }
    }
    jq(".uif-actionImage").live("click",function(){
       unsaved();
    });
    jq(".uif-dropdownControl").live("change",function() {
        jq("#unsaved_control").val("true");
    });
    window.onbeforeunload = unloadPage;
});

jq("#invoice-paymentMethod_control").live("change",function() {
    jq('#hiddenButtonForChangeToPaymentMethod').focus().click();
});

/*function closeInvoiceCurrencyOverridePopUp(){
    jq("div#MessagePopUpSectionForCurrencyOverride").fadeOut(300);
    jq("#mask").fadeOut(300);
}*/

function updateExchangeRate() {
    jq("#updateExchangeRateBtn").focus().click();
}
jq(".uif-detailsAction").live("click",function(){
    var id =jq(this).attr("id");
    var index = id.substring(id.indexOf("_line"),id.length).replace("_line","");
    jq("#OLEInvoiceView-invoiceItem-relatedView_Btn_line"+index).focus().click();
});

jq("#Uif-Inv-DocumentRouteLogSection_toggle_col").live("click",function(){
    jq("#routeLogDisplayBtn").focus().click();
});