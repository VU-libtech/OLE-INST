function displayDialogWindow(divID){

    jq(divID).fadeIn(300);
    var popMargTop = (jq(divID).height() + 24) / 2;
    var popMargLeft = (jq(divID).width() + 24) / 2;
    jq(divID).css({
        'margin-top' : -popMargTop,
        'margin-left' : -popMargLeft
    });
    jq(divID).draggable();
    jq('body').append('<div id="mask"></div>');
    jq('#mask').fadeIn(300);
}

function instance() {
    displayDialogWindow("div#OLEEResourceRecordView-InstanceSelectionPopUp");
    //jq("#selectInstance input[type='image']").hide();
}

function instanceLink(docNumber,eResourceId) {
    if(jq("#selectInstanceId input[type='radio']:checked").val() == "linkExistingInstance") {
        window.open("olesearchcontroller?viewId=OLEEResourceBibView&methodToCall=start&tokenId="+docNumber+"&eResourceId="+eResourceId+"&eInstance=linkExistingInstance");
        jq("#OLEEResourceRecordView-SelectFlag").val('true');
        closeLinkInstance();
    }
    else if(jq("#selectInstanceId input[type='radio']:checked").val() == "createNewInstance") {

        window.open("olesearchcontroller?viewId=OLEEResourceBibView&link=create&methodToCall=start&tokenId="+docNumber+"&eResourceId="+eResourceId);
        jq("#OLEEResourceRecordView-SelectFlag").val('true');
        closeLinkInstance();
    }
    else {

        window.open("editorcontroller?viewId=EditorView&methodToCall=load&docCategory=work&docType=bibliographic&docFormat=marc&editable=true&tokenId="+docNumber+"&eResourceId="+eResourceId);
        jq("#OLEEResourceRecordView-createNewInstanceFlag").val('true');
        closeLinkInstance();
    }
}

function closeLinkInstance() {
    jq("div#OLEEResourceRecordView-InstanceSelectionPopUp").fadeOut(300);
    jq('#mask').fadeOut(300);
}

function saveERSDocument() {
    jq("#ERSLicense-Save").focus().click();
}

function removeInstance() {
    jq("#SaveInstance-button").focus().click();
}

jq(document).ready(function () {
    jq("input:text").live("click", function () {
        if (jq(this).attr("id") == undefined) {
            if (jq(this).parent().parent().attr("class") == "dataTables_filter") {
                jq(this).attr("id", "dataTextSearchBox");
            }
        }
    })
    var unsaved = false;
    jq("#OLEEResourceRecordView").live("keypress",function(){
        unsaved = true;
    });
    jq("#OLEEResourceRecordView").live("change",function(){
        unsaved = true;
    });
    jq(":input").live("change keypress",function(){
        unsaved = true;
    });
    jq("#requestor_line0_fieldset").live("click",function(){
        unsaved = true;
    });
    jq("#selector_line0_fieldset").live("click",function(){
        unsaved = true;
    });
    jq("#u474_fieldset").live("click",function(){
        unsaved = true;
    });
    function unloadPage(){
        if(unsaved){
            var message  = "This page is asking you to confirm that you want to leave - data you have entered may not be saved.";
            return message;
        }
    }
    jq('form').bind('submit', function() { unsaved = false; });
    window.onbeforeunload = unloadPage;
});

function defaultCoverage() {
    displayDialogWindow("div#OLEEResourceRecordView-InstanceDetailPopUp");
    coverageDates();
}

function defaultPerpetualAccess() {
    displayDialogWindow("div#OLEEResourceRecordView-InstanceDetailPopUp");
    perpetualAccessDates();
}

function saveCoverageOrPerpetualAccess() {
    closeInstanceDate();
    if(jq("#defaultCovStartDateErrorMessage span").text().length == 0
        && jq("#defaultCovEndDateErrorMessage span").text().length == 0
        && jq("#defaultPerAccStartDateErrorMessage span").text().length == 0
        && jq("#defaultPerAccEndDateErrorMessage span").text().length == 0) {
        jq("#hdnRefreshDefaultDate").focus().click();
    }
}

function closeInstanceDate() {
    if(jq("#defaultCovStartDateErrorMessage span").text().length == 0
        && jq("#defaultCovEndDateErrorMessage span").text().length == 0
        && jq("#defaultPerAccStartDateErrorMessage span").text().length == 0
        && jq("#defaultPerAccEndDateErrorMessage span").text().length == 0) {
    jq("#OLEEResourceRecordView-coverageFlag").val("false");
    jq("#OLEEResourceRecordView-perpetualAccessFlag").val("false");
    jq("div#OLEEResourceRecordView-InstanceDetailPopUp").fadeOut(300);
    jq("#mask").fadeOut(300);
    } else {
        jq("#OLEEResourceRecordView-coverageFlag").val("true");
        jq("#OLEEResourceRecordView-perpetualAccessFlag").val("true");
        coverageDates();
        perpetualAccessDates();
        displayDialogWindow("div#OLEEResourceRecordView-InstanceDetailPopUp");
    }
}

function coverageDates() {
    if (jq("#default-Coverage-Date_control").val() != null) {
        var startDate = jq("#OLEEResourceRecordView-CovStartDate_control").val();
        jq("#default-Coverage-Date_control").val(null);
        jq("#default-Coverage-Date_control").val(startDate.trim());
        startDate = jq("#OLEEResourceRecordView-CovStartDate_control").val(null);
    }
    if (jq("#default-coverageEndDate_control").val() != null) {
        var endDate = jq("#OLEEResourceRecordView-CovEndDate_control").val();
        jq("#default-coverageEndDate_control").val(null);
        jq("#default-coverageEndDate_control").val(endDate.trim());
        endDate = jq("#OLEEResourceRecordView-CovEndDate_control").val(null);
    }
}

function perpetualAccessDates() {
    if (jq("#defaultPerpetual-Date_control").val() != null) {
        var startDate = jq("#OLEEResourceRecordView-PerAccStartDate_control").val();
        jq("#defaultPerpetual-Date_control").val(null);
        jq("#defaultPerpetual-Date_control").val(startDate.trim());
        startDate = jq("#OLEEResourceRecordView-PerAccStartDate_control").val(null);
    }
    if (jq("#default-perpetualAccessEndDate_control").val() != null) {
        var endDate = jq("#OLEEResourceRecordView-PerAccEndDate_control").val();
        jq("#default-perpetualAccessEndDate_control").val(null);
        jq("#default-perpetualAccessEndDate_control").val(endDate.trim());
        endDate = jq("#OLEEResourceRecordView-PerAccEndDate_control").val(null);
    }
}

function makeSeparateLink(content){
    var responseString="";
    var linkArray = content.split(',');
    for(var count=0;count<linkArray.length;count++){
        url = linkArray[count];
        responseString =  "   " +responseString  + "<a  href='" +  url + "' target='_blank'> " + url + "</a>";
    }
    return responseString;
}

function displayLink() {
    var divLength= jq("#OLEEResourceRecordView-InstanceDetails_disclosureContent tbody div").length;
    for(var count=0;count<divLength;count++){
        var value =  jq("#OLEEResourceRecordView-InstanceDetails_disclosureContent tbody div#eResource-url_line"+count+" span#eResource-url_line"+count+"_control").text();
        var returnValue = makeSeparateLink(value);
        jq("#OLEEResourceRecordView-InstanceDetails_disclosureContent tbody div#eResource-url_line"+count+" span#eResource-url_line"+count+"_control").html(returnValue);
    }
}

