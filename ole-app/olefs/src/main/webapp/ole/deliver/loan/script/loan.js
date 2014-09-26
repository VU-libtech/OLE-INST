jq(function() {
    jq("#ReturnViewPage_tab").live("click",function(){
        jq("#showReturn_control").val("true")
    });

    jq("#LoanViewPage_tab").live("click",function(){
        jq("#showReturn_control").val("false")
    });

    if(jq("#showReturn_control").val()=="true"){
        jq("#DeliverTabSection_tabs").tabs("select","#ReturnViewPage_tab");
    }

});

function getProxyName(firstName,lastName){
    jq("#headerMsgId span label").html("The current patron is a proxy borrower for \""+firstName+" "+lastName+"\". Please select the correct one for this transaction and then click OK.");
}

function displayCopyMessage() {
    if (jq("#copyCheck input[type='radio']:checked").val() == "false") {
        jq("#check-in_msg").children('span').eq(0).html("Verify whether copy request has been fulfilled </br> <b>Keep the item in the copy section for processing the copy request</b>");
    }
    else{
        jq("#check-in_msg").children('span').eq(0).html("Verify whether copy request has been fulfilled </br>");
    }

}

function getCurrentProxyName(){
    jq("#headerMsgId span label").html("The current patron is a proxy borrower. Please select the correct one for this transaction and click OK." );
}

function validateRealPatron(){
    if(!jq(".patronCheckBoxClass:checked").length == 0){
        jq('#realPatronBtn').focus().click();
    }
}

function openFastAdd(){
    if(jq("#fastAddItemIndicatorDialog_control").val()=='true'){
        _initAndOpenLightbox({type: 'iframe', href: jq("#fastAddItemUrlFlag_control").val(), height: 'auto', width: '70%', autoSize: true},
            undefined);
        return false;
    }
}

function displayDialogWindow(divID){
    jq(divID).fadeIn(300);
    jq(divID).fadeIn(300);
    var popMargTop = (jq(divID).height() + 24) / 2;
    var popMargLeft = (jq(divID).width() + 24) / 2;
    var left=(jq(document).width()/2)-(jq(divID).width()/2)
    var top='300px';

    if(jq(divID).height()>300){
        top='250px';
    }
    if(jq(divID).height()>500){
        top='210px';
    }
    jq(divID).css({
        /* 'margin-top' : '100px',
         'margin-left' : '100px',*/
        'top': top,
        'left':left+"px",
        'position':'fixed',
        'align':'center'
    })
    jq(divID).draggable();
    jq('body').append('<div id="mask"></div>');
    jq('#mask').fadeIn(300);
    jq('body').scrollTop(0);
    jq(window.parent).scrollTop(0);
}


var checkOutIdleTime = 0;
var checkInIdleTime = 0;
jq(document).ready(function(){
     if(jq("#showReturn_control").val()=='true') {
         jq("#ReturnItemView").append("<audio id='sound' preload='auto'><source src='../ole/deliver/loan/script/alert.wav'type='audio/wav'/></audio>");
         jq("#showReturn_control").val("true");
     } else{
          jq("#PatronItemView").append("<audio id='sound' preload='auto'><source src='../ole/deliver/loan/script/alert.wav'type='audio/wav'/></audio>");
         jq("#showReturn_control").val("false");
     }
    validationsForPop();

    jq(".fancybox-close").live("click" ,function(e) {
        jq('#Patron-item_control').focus();
    });


    var checkOutIdleInterval = setInterval(function(){
        var counterCheckOut = jq("#maxSessionTime_control").val();
        checkOutIdleTime = checkOutIdleTime + 1;
        if (parseInt(checkOutIdleTime) > parseInt(counterCheckOut)) {
            clearInterval(checkOutIdleInterval);
            jq('#SessionReset').focus().click();;
        }
    }, 60000); // 1 minute

    //Zero the idle timer on mouse movement.
    jq(this).mousemove(function (e) {
        checkOutIdleTime = 0;
        checkInIdleTime = 0;
    });
    //Zero the idle timer on key press.
    jq(this).keypress(function (e) {
        checkOutIdleTime = 0;
        checkInIdleTime = 0;
    });

    var checkInIdleInterval = setInterval(function(){
        var counterCheckIn = jq("#mapMaxTimeForCheckInDate_control").val();
        checkInIdleTime = checkInIdleTime + 1;
        if (parseInt(checkInIdleTime) > parseInt(counterCheckIn)) {
            clearInterval(checkInIdleInterval);
            jq("#showReturn_control").val("true")
            jq("#endSessionButton").focus().click();
        }
    }, 60000); // 1 minute


/*
    var counter = jq("#maxSessionTime_control").val();

    timer = setInterval(function() {
        jq("#maxSessionTime_control").val(--counter);
        if (counter == 0) {
            clearInterval(timer);
            jq('#SessionReset').focus().click();
        };
    }, 60000);*/

    jq("#DeliverTabSection").width(jq("form").width());

    jq("#LoanCirculationDesk_control").live("change",function() {
        jq('#ChangeLocationButton').focus().click();
    });

    jq("#ReturnCirculationDesk_control").live("change",function() {
        jq('#ChangeReturnLocationButton').focus().click();
    });


    jq(".patronCheckBoxClass").live("click",function(){
        jq(".patronCheckBoxClass").removeAttr("checked");
        jq("#"+jq(this).attr('id')).attr("checked","true");
    });

    jq(document).keypress(function(e) {
        if(e.which == 13) {
            if(e.target.id == "Patron-barcode_control"){
                if(jq( "#Patron-barcode_control").val()!=""){
                    jq(this).blur();
                    jq('#SecondarySearchPatron').focus().click();
                }else{
                    jq("#Patron-barcode_control").focus();
                    return false;
                }
            }else if(e.target.id == "Patron-item_control"){
                if(jq( "#Patron-item_control").val()!=""){
                    jq(this).blur();
                    if(jq( "#Patron-barcode_control").val()!=""){
                        jq('#SecondarySearchItem').focus().click();
                    }else{
                        jq("#LoanMessageFieldSection").text("Please enter patron barcode.");
                        jq("#Patron-barcode_control").focus();
                        return false;
                    }
                }else{
                    jq('#Patron-item_control').focus();
                    return false;
                }
            }
            return false;
        }
    });


    if(jq("#hdnItemFocus_control").val()=="true"){
        jq('#Patron-item_control').focus();
    }

    if(jq("#hdnPatronFocus_control").val()=="true"){
        jq('#Patron-barcode_control').focus();
    }


    jq('#BillPaymentOption_control').click(function(){
        if(jq("#BillPaymentOption_control").is(':checked')){
            jq("div#AlertBoxSectionForBill-HorizontalBoxSection").fadeOut(300);
        }else{
            jq("div#AlertBoxSectionForBill-HorizontalBoxSection").fadeIn(300);
        }
    });

    jq('#CheckInItem_control').live("keypress" ,function(e) {
        if(e.which == 13) {
            if(jq( "#CheckInItem_control").val()!=""){
                jq('#validateItem').trigger('click');
                jq("#DeliverTabSection_tabs").tabs("select","#ReturnViewPage_tab");
            }
        }
    });

    jq( "#CheckInItem_control").focus();


    if(jq("#RenewalDueDateCollection-HorizontalBoxSection").length > 0) {
        jq("#RenewalDueDateCollection-HorizontalBoxSection th.col1").children().append("<input type='checkbox' value='false' class='selectAllRenewCB' name='selectLoanedAll' id='selectLoanedAll' style='float:left;margin-left:0.5em'>");
    }


    if(jq("#Patron-ExistingLoanItemListSection-HorizontalBoxSection").length > 0) {
        jq("#Patron-ExistingLoanItemListSection-HorizontalBoxSection th.col1").children().append("<input type='checkbox' value='false' class='selectAllLoanedCB' name='selectLoanedAll' id='selectLoanedAll' style='float:left;margin-left:0.5em'>");
    }

    jq("#CheckInDate_control").live("change",function(){
        var diff = validateDate(jq("#mapCurrentDate_control").val(), jq("#CheckInDate_control").val());
        if(diff==-1){
            if(jq("#audioForPastDate_control").val()=="true"){
                jq("#audioOption_control").val("true");
            }
            playAudio();
            jq("div#AlertBoxSectionForDate-HorizontalBoxSection").fadeIn(300);
            jq("div#AlertBoxSectionForTime-HorizontalBoxSection").fadeOut(300);
        }else if(diff==1){
            jq("#CheckInDate_control").val('');
            jq("#CheckInDate_control").focus();
            jq("div#AlertBoxSectionForDate-HorizontalBoxSection").fadeOut(300);
        }else{
            jq("div#AlertBoxSectionForDate-HorizontalBoxSection").fadeOut(300);
            validateCheckInTime();
        }
    });

    jq('#CheckInTime_control').live("change",function(){
        validateCheckInTime();
    });

    if (jq("#LoanLoginMessage_control").val() == 'true') {
       jq(parent.document).find(".searchbox").focus();
    }
    jq(parent.document).find("#search form:eq(0)").submit(function() {
        jq( "#loanLoginName_control").val(jq(parent.document).find(".searchbox").val());
        jq("#loanLoginBtn").focus().click();
        return false;
    });
    jq(parent.document).find("#search form:eq(1)").submit(function() {
        jq("#loanLogOutBtn").focus().click();
        return false;
    });

    jq("input#ReturnView-missingPieceCount_control").live('keydown',function(event) {
            if ( event.keyCode == 46 || event.keyCode == 8 || event.keyCode == 9 || event.keyCode == 27 || event.keyCode == 13 ||
                (event.keyCode == 65 && event.ctrlKey === true) ||
                (event.keyCode >= 35 && event.keyCode <= 39)) {
                return;
            }
            else {
                if (event.shiftKey || (event.keyCode < 48 || event.keyCode > 57) && (event.keyCode < 96 || event.keyCode > 105 )) {
                    event.preventDefault();
                }
            }
    });
    jq("input#LoanView-missingPieceCount_control").live('keydown',function(event) {
        if ( event.keyCode == 46 || event.keyCode == 8 || event.keyCode == 9 || event.keyCode == 27 || event.keyCode == 13 ||
            (event.keyCode == 65 && event.ctrlKey === true) ||
            (event.keyCode >= 35 && event.keyCode <= 39)) {
            return;
        }
        else {
            if (event.shiftKey || (event.keyCode < 48 || event.keyCode > 57) && (event.keyCode < 96 || event.keyCode > 105 )) {
                event.preventDefault();
            }
        }
    });
    jq("input#MissingPiece-dialogMissingPieceCount-Section_control").live('keydown',function(event) {
        if ( event.keyCode == 46 || event.keyCode == 8 || event.keyCode == 9 || event.keyCode == 27 || event.keyCode == 13 ||
            (event.keyCode == 65 && event.ctrlKey === true) ||
            (event.keyCode >= 35 && event.keyCode <= 39)) {
            return;
        }
        else {
            if (event.shiftKey || (event.keyCode < 48 || event.keyCode > 57) && (event.keyCode < 96 || event.keyCode > 105 )) {
                event.preventDefault();
            }
        }
    });
    jq("input#MissingPiece-dialogItemNoOfPieces-Section_control").live('keydown',function(event) {
        if ( event.keyCode == 46 || event.keyCode == 8 || event.keyCode == 9 || event.keyCode == 27 || event.keyCode == 13 ||
            (event.keyCode == 65 && event.ctrlKey === true) ||
            (event.keyCode >= 35 && event.keyCode <= 39)) {
            return;
        }
        else {
            if (event.shiftKey || (event.keyCode < 48 || event.keyCode > 57) && (event.keyCode < 96 || event.keyCode > 105 )) {
                event.preventDefault();
            }
        }
    });
    jq("input#numberOfPieces_control").live('keydown',function(event) {
        if ( event.keyCode == 46 || event.keyCode == 8 || event.keyCode == 9 || event.keyCode == 27 || event.keyCode == 13 ||
            (event.keyCode == 65 && event.ctrlKey === true) ||
            (event.keyCode >= 35 && event.keyCode <= 39)) {
            return;
        }
        else {
            if (event.shiftKey || (event.keyCode < 48 || event.keyCode > 57) && (event.keyCode < 96 || event.keyCode > 105 )) {
                event.preventDefault();
            }
        }
    });
    setMissingPieceCount();
    displayMissingDamagedBox();

});

function selectAllLoanedItem(){
    jq(".loaningItemCBClass").attr("checked","true");
}

function deSelectAllLoanedItem(){
    jq(".loaningItemCBClass").removeAttr("checked");
}

function selectAllExistingLoanedItem(){
    jq(".loanedItemCBClass").attr("checked","true");

}

function deSelectAllExistingLoanedItem(){
    jq(".loanedItemCBClass").removeAttr("checked");
}

function selectAllRenewItem(){
    jq(".renewItemCBClass").attr("checked","true");
}

function deSelectAllRenewItem(){
    jq(".renewItemCBClass").removeAttr("checked");
}

function setDueDate(){
    jq( "#mapDate_control").val(jq( "#popUpDate_control").val());
    jq('#hdnLoanBtn').focus().click();
    closeMessageBox();
}
function returnLoan(){
    jq( "#mapDate_control").val(jq( "#popUpDate").val());
    jq('#hdnReturnLoanBtn').focus().click();
    jq("div#MessagePopupSectionForLoan").fadeOut(300);
    jq('#mask').fadeOut(300);
    jq("#showReturn_control").val("true");
}

function doNotReturnLoan(){
    jq('#hdnNoReturnLoanBtn').focus().click();
    jq("div#MessagePopupSectionForLoan").fadeOut(300);
    jq('#mask').fadeOut(300);
    jq("#showReturn_control").val("true");
}
function setAlterDueDateLoanList(){
    jq( "div#AlterDueDateSection-HorizontalBoxSection" ).fadeOut('fast');
}

function alterDueDate(){
    if(jq("#showAlterDueDateDialog_control").val()=='true'){
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow("div#AlterDueDateSection-HorizontalBoxSection");
        return false;
    }
}



function claimsReturn(){
    if(jq("#showClaimsReturnDialog_control").val()=='true'){
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow("div#ClaimsReturn-HorizontalBoxSection");
        jq('#mask').fadeOut(300);
    }
}
function setRenewalDueDateLoanList(){
    jq('#hdnrenewLoanDueDateBtn').focus().click();
    jq( "div#RenewalDueDateSection-HorizontalBoxSection" ).fadeOut('fast');
    jq('#mask').fadeOut(300);
}

function setPatronUserNoteDelete(){
    jq('#hdnPatronDeleteBtn').focus().click();
    jq( "div#PatronUserNote-HorizontalBoxSection" ).fadeOut(300);
    jq('#mask').fadeOut(300);
}

function saveFastAddItem(){
    jq('#addFastAddItemBtn').focus().click();
}


function setClaimsReturnList(){
    if (jq("#claimsNote_control").val().length > 0) {
        jq("#mapClaimsReturnNote_control").val(jq("#claimsNote").val());
        jq('#hdnClaimsReturnBtn').focus().click();
        jq("div#ClaimsReturn-HorizontalBoxSection").fadeOut(300);
        jq('#mask').fadeOut(300);
    } else {
        return false;
    }
}

function removeClaimsReturnList(){
    jq( "#showClaimsReturnDialog_control").val(false);
    jq( "#removeClaimsReturnFlag_control").val(true);
    jq('#hdnClaimsReturnBtn').focus().click();
    jq( "div#ClaimsReturn-HorizontalBoxSection" ).fadeOut(300);
    jq('#mask').fadeOut(300);
}

function closeClaimsReturnDialog(){
    jq( "#showClaimsReturnDialog_control").val(false);
    jq( "div#ClaimsReturn-HorizontalBoxSection" ).fadeOut(300);
    jq('#mask').fadeOut(300);
    focusItem();
}

function closeAlterDueDateDialog(){
   if(jq('#AlterDueDateInformationSection span').length==0){
        jq( "#showAlterDueDateDialog_control").val(false);
        jq('#hdnAlterLoanCloseBtn').focus().click();
        jq( "div#AlterDueDateSection-HorizontalBoxSection" ).fadeOut('fast');
        jq('#mask').fadeOut(300);
    }  else {
       jq('body').scrollTop(0);
       jq(window.parent).scrollTop(0);
        displayDialogWindow("div#AlterDueDateSection-HorizontalBoxSection");
    }
    focusItem();
}

function closeRenewalDueDateDialog(){
    jq( "#showRenewDueDateDialog_control").val(false);
    jq('#hdnrenewLoanCloseBtn').focus().click();
    jq( "div#RenewalDueDateSection-HorizontalBoxSection" ).fadeOut('fast');
    jq('#mask').fadeOut(300);
}

function closeRenewOverrideDialog(){
    jq( "#showRenewDueDateDialog_attribute").val(false);
    jq('#hdnrenewCloseBtn').focus().click();
    jq( "div#RenewalDueDateSection-HorizontalBoxSection_div" ).fadeOut('fast');
}

function closeRealPatronDialog(){
    jq( "#showRealPatronDialog_control").val(false);
    jq('#hdnRealPatronCloseBtn').focus().click();
    jq( "div#RealPatronSection-HorizontalBoxSection" ).fadeOut('fast');
    jq('#mask').fadeOut(300);
}

function closeRealPatronUpdate(){
    jq( "#showRealPatronDialog_control").val(false);
    jq( "div#RealPatronSection-HorizontalBoxSection" ).fadeOut('fast');
    jq('#mask').fadeOut(300);
    focusItem();
}

function closePatronUserNoteDialog(){
    jq( "#showPatronUserNoteDialog_control").val(false);
    jq( "div#PatronUserNote-HorizontalBoxSection" ).fadeOut(300);
    jq('#mask').fadeOut(300);
}

function closeCirculationLocationDialog(){
    jq( "#showChangeLocationDialog_control").val(false);
    jq( "div#ConfirmCirculationLocationChange" ).fadeOut(300);
    jq('#mask').fadeOut(300);
}
function closeReturnCirculationLocationDialog(){
    jq( "#showChangeReturnLocationDialog_control").val(false);
    jq( "div#ConfirmReturnCirculationLocationChange" ).fadeOut(300);
    jq('#mask').fadeOut(300);
}

function setUser(){
    if(jq( "#newPrincipalId_control").val() != null && jq( "#newPrincipalId_control").val() != ""){
       jq(parent.document).find("#login-info").children('strong').eq(1).html("  Impersonating User: "+jq( "#newPrincipalId_control").val());
    }
    jq('#hdlogInUser').focus().click();
    jq( "#OverRideLogInSectionLink" ).fadeOut(300);
    jq('#mask').fadeOut(300);
}

function refreshUserLogin(){
    if(jq( "#oldPrincipalId_control").val() != null && jq( "#oldPrincipalId_control").val() != ""){
       jq(parent.document).find("#login-info").children('strong').eq(1).html("  Impersonating User: "+jq( "#oldPrincipalId_control").val());
    }
}


function setDescription(){
    jq('#hdnContinueBtn').focus().click();
    closeMessageBoxForReturn();
}
function continueBtn(){
    jq('#hdnLoanContinueBtn').focus().click();
    closeMessageBox();
}

function validateDate(currDate, checkInDate){
    var currentDate = new Date(currDate);
    var newCheckInDate = new Date(checkInDate);
    if(currentDate>newCheckInDate){
        var counter = jq("#mapMaxTimeForCheckInDate_control").val();
        timer = setInterval(function() {
          //  alert(counter);
           // jq("#mapMaxTimeForCheckInDate_control").val(--counter);
            if (counter == 0) {
                clearInterval(timer);
              //  jq("#CheckInDate").val(currDate);
              //  jq("#mapMaxTimeForCheckInDate_control").val(jq("#mapCheckInDateMaxTime_control").val());
              //  jq("div#AlertBoxSectionForDate-HorizontalBoxSection").fadeOut(300);
              //  jq("#endSessionButton").focus().click();
                return 0;
            };
        }, 1000);
        return -1;
    } else if(currentDate<newCheckInDate){
        return 1;
    } else{
        return 0;
    }
}
function validPatronItem(){

    if(jq("#showOverRideLogin_control").val()=='true'){
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow("div#OverRideLogInSectionLink");
        return false;
    }

    if(jq("#showMessage_control").val()=='false' && jq("#showRenewalMessage_control").val()=='false'){
        playAudio();
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow("div#MessagePopupSection");
        jq('body').scrollTop(0);
        jq(window).scrollTop(0);
        if (jq("loanClaimsOption_control_0")) {
            jq("#loanClaimsOption_control_0").attr('checked', true);
        }
        return false;
    }

    if(jq("#showMessage_control").val()=='false' && jq("#showRenewalMessage_control").val()=='true'){
        playAudio();
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow("div#MessagePopupSection");
        jq('body').scrollTop(0);
        jq(window).scrollTop(0);
        return false;
    }

    if(jq("#showClaimsReturnDialog_control").val()=='true'){
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow("div#ClaimsReturn-HorizontalBoxSection" );
        return false;
    }


    if(jq("#showRealPatronDialog_control").val()=='true'){
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow("div#RealPatronSection-HorizontalBoxSection");
        return false;
    }



    if(jq("#showPatronUserNoteDialog_control").val()=='true'){
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow( "div#PatronUserNote-HorizontalBoxSection" );
        return false;
    }
    displayDueDateInfo();
    playAudio();
    focusItem();

}
function displayDueDateInfo(){
    if(jq("#dueDateInfoMsg_control").val() !=null && jq("#dueDateInfoMsg_control").val()!=""){
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow("div#DueDateInformationSection");
    }
}
function validateItem(){
    if(jq("#showMessageForReturn_control").val()=='false' && jq("#showCheckOut_control").val()=="true"){
        playAudio();
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow("div#MessagePopupSectionForLoan" );
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
    }
    if(jq("#showMessageForReturn_control").val()=='false' && (jq("#showContinue_control").val()=="true"||jq("#mapCheckInNoteExists_control").val()=="true"||jq("#showContinueForCopy_control").val()=="true"||jq("#showCheckOut_control").val()=="false" || jq("mapDamagedCheckIn_control").val()=="true")){
        playAudio();
        if(jq("#hdnBackGroundCheckIn_control").val()=="true"){
            setMissingPieceCount();
            jq('body').scrollTop(0);
            jq(window.parent).scrollTop(0);
            displayDialogWindow("div#MessagePopupSectionForBackGroundReturn" );
            if (jq("input#OLELoanView-MissingPiece-RecordNote_control_0") != undefined) {
                jq("input#OLELoanView-MissingPiece-RecordNote_control_0").attr('checked', true);
            }
        }else{
            setMissingPieceCount();
            jq('body').scrollTop(0);
            jq(window.parent).scrollTop(0);
            displayDialogWindow("div#MessagePopupSectionForReturn" );
            jq('body').scrollTop(0);
            jq(window.parent).scrollTop(0);
            if (jq("returnClaimsOption_control_0")) {
                jq("#returnClaimsOption_control_0").attr('checked', true);
            }
            if (jq("input#OLEReturnView-MissingPiece-RecordNote_control_0") != undefined) {
                jq("input#OLEReturnView-MissingPiece-RecordNote_control_0").attr('checked', true);
            }
        }
    }
    dateValidation();
    validateCheckInTime();
    displayMissingDamagedBox();
    if(jq("#mapCheckInNoteExists_control").val()=="false"){
        playAudio();
        printBill();
    }
}
function closeDueDateInfo(){
    jq("div#DueDateInformationSection").fadeOut(300);
    jq('#mask').fadeOut(300);

}
function dateValidation(){
    var diff = validateDate(jq("#mapCurrentDate_control").val(), jq("#CheckInDate_control").val());
    if(diff==-1){
        jq("div#AlertBoxSectionForDate-HorizontalBoxSection").fadeIn(300);
    }else if(diff==1){
        jq("#CheckInDate_control").val('');
        jq("#CheckInDate_control").focus();
        jq("div#AlertBoxSectionForDate-HorizontalBoxSection").fadeOut(300);
    }else{
        jq("div#AlertBoxSectionForDate-HorizontalBoxSection").fadeOut(300);
    }
}
function validationsForPop(){
    validPatronItem();
    validateItem();

}
function validateRenew(){
    if(jq("#showRenewDueDateDialog_control").val()=='true'){
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow("div#RenewalDueDateSection-HorizontalBoxSection");
        jq('#mask').fadeOut(300);
        return false;
    }
    if(jq("showOverrideRenewalItemMessage").val()=="true"){

    }
}
function doNotLoan(){
    jq('#hdnNoLoanBtn').focus().click();
    closeMessageBox();
}
function proceed(){
    jq('#hdnProceedRenewBtn').focus().click();
    closeMessageBox();

}
function doNotProceed(){
    jq('#hdnNoProceedRenewBtn').focus().click();
    closeMessageBox();
}
function closeMessageBox(){
    jq("div#MessagePopupSection").fadeOut(300);
    jq('#mask').fadeOut(300);
    jq("#showReturn_control").val("false");
    focusItem();
}

function closeOverrideDialog(){
    jq("div#OverRideLogInSectionLink").fadeOut(300);
    jq('#mask').fadeOut(300);
}


function focusItem(){
    jq("input#damagedItemDialog_control").val('false');
    jq("input#missingPieceDialog_control").val('false');
    jq('#Patron-item_control').focus();

}

function focusPatron(){
    jq('#Patron-barcode_control').focus();
}

function closeMessageBoxForReturn(){
    jq("div#MessagePopupSectionForReturn").fadeOut(300);
    jq('#mask').fadeOut(300);
    validationsForPop();
    jq( "#CheckInItem_control").focus();
    jq("#showReturn_control").val("true");
}
function changeLocation(){
    if(jq("#showChangeLocationDialog_control").val()=='true'){
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow( "div#ConfirmCirculationLocationChange" );
    }
}
function sessionFadeOut(){
    jq('#mask').fadeOut(300);
}
function changeReturnLocation(){
    if(jq("#showChangeReturnLocationDialog_control").val()=='true'){
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow( "div#ConfirmReturnCirculationLocationChange" );
        dateValidation();
        validateCheckInTime();
    }
}
/* commented for jira -4470
   function printBill() {
    if (jq("#mapLoanBill_control").val() == 'true') {
        jq("#mapLoanBill_control").val("false");
        var url=window.parent.location.href;
        if (!url.toLowerCase().indexOf("retriveForm") >= 0) {
            var url = url + "&retriveForm=" +jq("#mapFormKey_control").val();
        }
        alert("@@@"+url);
        if (jq("#OLEReturnView-itemStatus-for-print_control").val() == 'true') {
            window.open("loancontroller?viewId=PatronItemView&methodToCall=printLoanBill&formKey=" + jq("#mapFormKey_control").val());
        }
        if (jq("#OLEReturnView-itemStatus-for-print_control").val() == 'false') {
            window.open("loancontroller?viewId=PatronItemView&methodToCall=printLoanBill&formKey=" + jq("#mapFormKey_control").val(), '_self');
            window.open(url, '_blank');
        }
    }
    var values=jq("#hdnBackGroundCheckIn_control").val().split(",");
    jq("#hdnBackGroundCheckIn_control").val(values[0]);
    if (jq("#mapBillAvailability_control").val() == 'true' && jq("#hdnBackGroundCheckIn_control").val() == 'false') {
        var url=window.parent.location.href;
        if (!url.toLowerCase().indexOf("retriveForm") >= 0) {
            var url = url + "&retriveForm=" + jq("#mapFormKey_control").val()+"&billAvailability=false";
        }
        alert(url);
        if (jq("#OLEReturnView-itemStatus-for-print_control").val() == 'true') {
            window.open("loancontroller?viewId=PatronItemView&methodToCall=printBill&formKey=" + jq("#mapFormKey_control").val());
        }
        if (jq("#OLEReturnView-itemStatus-for-print_control").val() == 'false') {
            window.open("loancontroller?viewId=PatronItemView&methodToCall=printBill&formKey=" + jq("#mapFormKey_control").val(), '_self');
            window.open(url, '_blank');
        }
    }
}*/
function printBill(){
    if(jq("#mapLoanBill_control").val()=="true"){
        window.open("loancontroller?viewId=PatronItemView&methodToCall=printLoanBill&formKey="+jq("#mapFormKey_control").val());
        jq("#mapLoanBill_control").val("false");
    }

    if(jq("#mapBillAvailability_control").val()=="true" && jq("#hdnBackGroundCheckIn_control").val()=="false"){
        window.open("loancontroller?viewId=PatronItemView&methodToCall=printBill&formKey="+jq("#mapFormKey_control").val());
        jq("#mapBillAvailability_control").val("false");
        setTimeout(function(){
            jq("button#refreshExport_button").focus().click();
        },300);

    }
}
function printHoldSlips(){
    if(jq("#mapPrintBill_control").val()=="true"){
        window.open("loancontroller?viewId=PatronItemView&methodToCall=printHoldSlips&formKey="+jq("#mapFormKey_control").val());
        jq("#mapPrintBill_control").val("false");
    }
}
function playAudio(){
    if(jq("#showReturn_control").val()=='true') {
        var audio = jq("#sound")[0];
        if(jq("#audioOption_control").val()=="true"){
            audio.play();
            jq("#audioOption_control").val("false");
        }
    }
    else{
        if(jq("#loanaudioOption_control").val()=="true"){
            var audio = jq("#sound")[0];
            audio.play();
            jq("#loanaudioOption_control").val("false");
        }
    }

}
function backGroundCheckOut(){
    printBill();
    playAudio();
}

function setCheckinItemFocus(){
    jq("#CheckInItem_control").focus();
}

function callFastAddClose(){
    if(jq("#loanItemFlag_control").val()=="false"){
        jq("#closeFastAddItemBtn").focus().click();
    }
}

function setItemBarcode(){
    if(jq("#FastAddMessageFieldSection > .uif-message").text()==""){
      parent.jq.fancybox.close();
      parent.jq("#FastAddItemBarcode").focus().click();
    }

}

function refreshLoanScreen(){
    if(jq( "#loanLoginName_control").val() != null && jq( "#loanLoginName_control").val() != "" && jq("#validLogin_control").val()=="true"){
        if(jq(parent.document).find("#login-info").children('strong').eq(1).length>0){
            jq(parent.document).find("#login-info").children('strong').eq(1).html("  Impersonating User: "+jq( "#loanLoginName_control").val());
        } else{
            jq(parent.document).find("#login-info").html(jq(parent.document).find("#login-info").html()+("<strong>  Impersonating User: "+jq( "#loanLoginName_control").val()+"</strong>"));
        }
        jq(parent.document).find(".searchbox").val("");
      //  parent.location.reload();
    }
}

function closeMessageBoxForBackGroundReturn(){
    jq("div#MessagePopupSectionForBackGroundReturn").fadeOut(300);
    jq('#mask').fadeOut(300);
    validationsForPop();
}

function refreshScreen(){
    if(jq( "#loginUser_control").val() != null && jq( "#loginUser_control").val() != ""){
        if(jq(parent.document).find("#login-info").children('strong').eq(1).length>0){
            jq(parent.document).find("#login-info").children('strong').eq(1).html("  Impersonating User: "+jq( "#loginUser_control").val());
        }
    }
}

function checkDamagedCheckIn(){
    if(jq("#OLEReturnView-Damaged-status-flag_control").val()=='true' && jq("#OLEReturnView-skipDamagedCheckIn-flag_control").val()=='false'){
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow("div#OLEReturnView-DamagedItem-MessageBox");
        if (jq("input#OLEReturnView-DamagedItem-RecordNote_control_0") != undefined) {
            jq("input#OLEReturnView-DamagedItem-RecordNote_control_0").attr("checked", "true");
        }
    }
}

function closeDamagedCheckIn(){
    jq("div#OLEReturnView-DamagedItem-MessageBox").fadeOut(300);
    jq('#mask').fadeOut(300);
}

function continueDamagedCheckIn(){
    jq("div#OLEReturnView-DamagedItem-MessageBox").fadeOut(300);
    jq('#mask').fadeOut(300);
    jq("input#OLEReturnView-skipDamagedCheckIn-flag_control").val('true');
    jq('#validateItem').trigger('click');
}

function setMissingPieceCount(){
    if(jq("select#matchCheck_control option:selected").val()=='true'){
        jq("div#ReturnView-missingPieceCount").hide();
        jq("div#descText").hide();
        jq("div#OLEReturnView-MissingPiece-RecordNote").hide();
    }
    jq("select#matchCheck_control").live('change',function() {
        if(jq("select#matchCheck_control option:selected").val()=='false'){
            jq("div#ReturnView-missingPieceCount").show();
            jq("div#OLEReturnView-MissingPiece-RecordNote").show();
            jq("div#descText").show();
        } else {
            jq("div#ReturnView-missingPieceCount").hide();
            jq("div#descText").hide();
            jq("div#OLEReturnView-MissingPiece-RecordNote").hide();
        }
    })
    if(jq("select#matchCheck_loan_control option:selected").val()=='true'){
        jq("div#LoanView-missingPieceCount").hide();
        jq("div#descText_loan").hide();
        jq("div#OLELoanView-MissingPiece-RecordNote").hide();
    }
    jq("select#matchCheck_loan_control").live('change',function() {
        if(jq("select#matchCheck_loan_control option:selected").val()=='false'){
            jq("div#LoanView-missingPieceCount").show();
            jq("div#descText_loan").show();
            jq("div#OLELoanView-MissingPiece-RecordNote").show();
        } else {
            jq("div#LoanView-missingPieceCount").hide();
            jq("div#descText_loan").hide();
            jq("div#OLEReturnView-MissingPiece-RecordNote").hide();
        }
    })

}

function showMissingPieceDialog() {
    if (jq("input#missingPieceDialog_control").val() == 'true') {
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow("div#MissingPiece-HorizontalBoxSection");
    } else{
        deactivateAllDialogs();
    }
}

function showDamagedItemDialog() {
    if (jq("input#damagedItemDialog_control").val() == 'true') {
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow("div#DamagedItem-HorizontalBoxSection");
    }
}

function closeDamagedItemDialog() {
    jq("div#DamagedItem-HorizontalBoxSection").fadeOut(300);
    jq('#mask').fadeOut(300);
    jq("input#damagedItemDialog_control").val('false');
}

function closeMissingPieceDialog() {
    jq("div#MissingPiece-HorizontalBoxSection").fadeOut(300);
    jq('#mask').fadeOut(300);
    jq("input#missingPieceDialog_control").val('false');
}

function deactivateAllDialogs() {
    closeDamagedItemDialog();
    closeMissingPieceDialog();
}

function closeDamagedItemBox(){
    jq("div#OLELoanView-RecordNote-Damaged-MessageBox").fadeOut(300);
    jq('#mask').fadeOut(300);
}

function closeMissingPieceBox(){
    jq("div#OLELoanView-RecordNote-MissingPiece-MessageBox").fadeOut(300);
    jq('#mask').fadeOut(300);
}

function displayMissingDamagedBox(){
    if (jq("input#OLELoanView-RecordNote-Damaged-status-flag_control").val() == 'true') {
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow("div#OLELoanView-RecordNote-Damaged-MessageBox");
        if (jq("input#OLELoanView-RecordNote-Damaged-MessageBoxOption_control_0") != undefined) {
            jq("input#OLELoanView-RecordNote-Damaged-MessageBoxOption_control_0").attr("checked", "true");
         }
    }
    else if (jq("input#OLELoanView-RecordNote-MissingPiece-status-flag_control").val() == 'true') {
        jq('body').scrollTop(0);
        jq(window.parent).scrollTop(0);
        displayDialogWindow("div#OLELoanView-RecordNote-MissingPiece-MessageBox");
        if (jq("input#OLELoanView-RecordNote-MissingPiece-MessageBoxOption_control_0") != undefined) {
            jq("input#OLELoanView-RecordNote-MissingPiece-MessageBoxOption_control_0").attr("checked", "true");
        }
    }else {
        displayDueDateInfo();
    }
}

function ValidateTime(inputVal){
    var t = inputVal.split(':');
    var regxWithSeconds=/^\d\d:\d\d:\d\d$/;
    var regxWithOutSeconds=/^\d\d:\d\d$/;
    if(regxWithOutSeconds.test(inputVal)){
       return t[0] >= 0 && t[0] < 25 && t[1] >= 0 && t[1] < 60;
    } else if(regxWithSeconds.test(inputVal)){
        return t[0] >= 0 && t[0] < 25 && t[1] >= 0 && t[1] < 60 && t[2] >= 0 && t[2] < 60;
    } else {
         return false;
    }
}

function validateCheckInTime(){
    var inputVal = jq('#CheckInTime_control').val();
    var d=new Date();
    jq("div#AlertBoxSectionForInvalidTime-HorizontalBoxSection").fadeOut(300);
    if (!jq.trim(inputVal)=="") {
        if (!ValidateTime(inputVal)) {
            jq("div#AlertBoxSectionForInvalidTime-HorizontalBoxSection").fadeIn(300);
            jq("div#AlertBoxSectionForTime-HorizontalBoxSection").fadeOut(300);
        } else {
            var end = d.getHours() + ":" + d.getMinutes();
            var start = inputVal;
            s = start.split(':');
            e = end.split(':');
            min = e[1]-s[1];
            hour = e[0]-s[0];
            hour_carry = 0;
            if(min < 0){
                min += 60;
                var min_carry=hour*60;
                min_carry=min_carry-min;
                min = min_carry % 60 ;
                hour = Math.floor(min_carry/ 60);
            }
            diff = hour + ":" + min;
            if(hour>0 && hour<10){
                hour="0"+hour;
            }
            if(min>0 && min<10){
                min="0"+min;
            }
            var total_min=hour*60+min;
            if(total_min!=0){
                var diff = validateDate(jq("#mapCurrentDate_control").val(), jq("#CheckInDate_control").val());
                if (diff == 0) {
                    if (jq("#audioForPastDate_control").val() == "true") {
                        jq("#audioOption_control").val("true");
                    }
                    playAudio();
                    jq("div#AlertBoxSectionForTime-HorizontalBoxSection").fadeIn(300);
                }
            }
        }
    } else{
        jq("div#AlertBoxSectionForInvalidTime-HorizontalBoxSection").fadeOut(300);
        jq("div#AlertBoxSectionForTime-HorizontalBoxSection").fadeOut(300);
        var diff = validateDate(jq("#mapCurrentDate_control").val(), jq("#CheckInDate_control").val());
        if(diff==0){
            jq("div#AlertBoxSectionForDate-HorizontalBoxSection").fadeOut(300);
        }
    }
}
function refreshLoanList(){
    jq('#OLEReturnView-refreshReturnLoanList').focus().click();

}

function refreshExport(){
    var diff = validateDate(jq("#mapCurrentDate_control").val(), jq("#CheckInDate_control").val());
    if(diff==-1){
        if(jq("#audioForPastDate_control").val()=="true"){
            jq("#audioOption_control").val("true");
        }
        playAudio();
        jq("div#AlertBoxSectionForDate-HorizontalBoxSection").fadeIn(300);
        jq("div#AlertBoxSectionForTime-HorizontalBoxSection").fadeOut(300);
    }else if(diff==1){
        jq("#CheckInDate_control").val('');
        jq("#CheckInDate_control").focus();
        jq("div#AlertBoxSectionForDate-HorizontalBoxSection").fadeOut(300);
    }else{
        jq("div#AlertBoxSectionForDate-HorizontalBoxSection").fadeOut(300);
        validateCheckInTime();
    }
}