/*
 * Copyright 2008 The Kuali Foundation
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
package org.kuali.ole.module.purap.document.web.struts;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.ole.module.purap.PurapKeyConstants;
import org.kuali.ole.module.purap.document.ReceivingDocument;
import org.kuali.ole.module.purap.util.ReceivingQuestionCallback;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.web.struts.FinancialSystemTransactionalDocumentActionBase;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.kns.question.ConfirmationQuestion;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.TreeMap;

public class ReceivingBaseAction extends FinancialSystemTransactionalDocumentActionBase {

    /**
     * A wrapper method which prompts for a reason to hold a payment request or credit memo.
     *
     * @param mapping      An ActionMapping
     * @param form         An ActionForm
     * @param request      The HttpServletRequest
     * @param response     The HttpServletResponse
     * @param questionType A String used to distinguish which question is being asked
     * @param notePrefix   A String explaining what action was taken, to be prepended to the note containing the reason, which gets
     *                     written to the document
     * @param operation    A one-word String description of the action to be taken, to be substituted into the message. (Can be an
     *                     empty String for some messages.)
     * @param messageKey   A key to the message which will appear on the question screen
     * @param callback     A PurQuestionCallback
     * @return An ActionForward
     * @throws Exception
     */
    protected ActionForward askQuestionWithInput(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response, String questionType, String notePrefix, String operation, String messageKey, ReceivingQuestionCallback callback) throws Exception {
        TreeMap<String, ReceivingQuestionCallback> questionsAndCallbacks = new TreeMap<String, ReceivingQuestionCallback>();
        questionsAndCallbacks.put(questionType, callback);

        return askQuestionWithInput(mapping, form, request, response, questionType, notePrefix, operation, messageKey, questionsAndCallbacks, "", mapping.findForward(OLEConstants.MAPPING_BASIC));
    }

    /**
     * Builds and asks questions which require text input by the user for a payment request or a credit memo.
     *
     * @param mapping               An ActionMapping
     * @param form                  An ActionForm
     * @param request               The HttpServletRequest
     * @param response              The HttpServletResponse
     * @param questionType          A String used to distinguish which question is being asked
     * @param notePrefix            A String explaining what action was taken, to be prepended to the note containing the reason, which gets
     *                              written to the document
     * @param operation             A one-word String description of the action to be taken, to be substituted into the message. (Can be an
     *                              empty String for some messages.)
     * @param messageKey            A (whole) key to the message which will appear on the question screen
     * @param questionsAndCallbacks A TreeMap associating the type of question to be asked and the type of callback which should
     *                              happen in that case
     * @param messagePrefix         The most general part of a key to a message text to be retrieved from ConfigurationService,
     *                              Describes a collection of questions.
     * @param redirect              An ActionForward to return to if done with questions
     * @return An ActionForward
     * @throws Exception
     */
    protected ActionForward askQuestionWithInput(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response, String questionType, String notePrefix, String operation, String messageKey, TreeMap<String, ReceivingQuestionCallback> questionsAndCallbacks, String messagePrefix, ActionForward redirect) throws Exception {
        KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        ReceivingDocument receivingDocument = (ReceivingDocument) kualiDocumentFormBase.getDocument();

        String question = (String) request.getParameter(OLEConstants.QUESTION_INST_ATTRIBUTE_NAME);
        String reason = request.getParameter(OLEConstants.QUESTION_REASON_ATTRIBUTE_NAME);
        String noteText = "";

        ConfigurationService kualiConfiguration = SpringContext.getBean(ConfigurationService.class);
        String firstQuestion = questionsAndCallbacks.firstKey();
        ReceivingQuestionCallback callback = null;
        Iterator questions = questionsAndCallbacks.keySet().iterator();
        String mapQuestion = null;
        String key = null;

        // Start in logic for confirming the close.
        if (question == null) {
            key = getQuestionProperty(messageKey, messagePrefix, kualiConfiguration, firstQuestion);
            String message = StringUtils.replace(key, "{0}", operation);

            // Ask question if not already asked.
            return this.performQuestionWithInput(mapping, form, request, response, firstQuestion, message, OLEConstants.CONFIRMATION_QUESTION, questionType, "");
        } else {
            // find callback for this question
            while (questions.hasNext()) {
                mapQuestion = (String) questions.next();

                if (StringUtils.equals(mapQuestion, question)) {
                    callback = questionsAndCallbacks.get(mapQuestion);
                    break;
                }
            }
            key = getQuestionProperty(messageKey, messagePrefix, kualiConfiguration, mapQuestion);

            Object buttonClicked = request.getParameter(OLEConstants.QUESTION_CLICKED_BUTTON);
            if (question.equals(mapQuestion) && buttonClicked.equals(ConfirmationQuestion.NO)) {
                // If 'No' is the button clicked, just reload the doc

                String nextQuestion = null;
                // ask another question if more left
                if (questions.hasNext()) {
                    nextQuestion = (String) questions.next();
                    key = getQuestionProperty(messageKey, messagePrefix, kualiConfiguration, nextQuestion);

                    return this.performQuestionWithInput(mapping, form, request, response, nextQuestion, key, OLEConstants.CONFIRMATION_QUESTION, questionType, "");
                } else {

                    return mapping.findForward(OLEConstants.MAPPING_BASIC);
                }
            }
            // Have to check length on value entered.
            String introNoteMessage = notePrefix + OLEConstants.BLANK_SPACE;

            // Build out full message.
            noteText = introNoteMessage + reason;
            int noteTextLength = noteText.length();

            // Get note text max length from DD.
            int noteTextMaxLength = SpringContext.getBean(DataDictionaryService.class).getAttributeMaxLength(Note.class, OLEConstants.NOTE_TEXT_PROPERTY_NAME).intValue();
            if (StringUtils.isBlank(reason) || (noteTextLength > noteTextMaxLength)) {
                // Figure out exact number of characters that the user can enter.
                int reasonLimit = noteTextMaxLength - noteTextLength;
                if (reason == null) {
                    // Prevent a NPE by setting the reason to a blank string.
                    reason = "";
                }

                return this.performQuestionWithInputAgainBecauseOfErrors(mapping, form, request, response, mapQuestion, key, OLEConstants.CONFIRMATION_QUESTION, questionType, "", reason, PurapKeyConstants.ERROR_PAYMENT_REQUEST_REASON_REQUIRED, OLEConstants.QUESTION_REASON_ATTRIBUTE_NAME, new Integer(reasonLimit).toString());
            }
        }

        // make callback
        if (ObjectUtils.isNotNull(callback)) {
            ReceivingDocument refreshedReceivingDocument = callback.doPostQuestion(receivingDocument, noteText);
            kualiDocumentFormBase.setDocument(refreshedReceivingDocument);
        }
        String nextQuestion = null;
        // ask another question if more left
        if (questions.hasNext()) {
            nextQuestion = (String) questions.next();
            key = getQuestionProperty(messageKey, messagePrefix, kualiConfiguration, nextQuestion);

            return this.performQuestionWithInput(mapping, form, request, response, nextQuestion, key, OLEConstants.CONFIRMATION_QUESTION, questionType, "");
        }

        return redirect;
    }

    /**
     * Used to look up messages to be displayed, from the ConfigurationService, given either a whole key or two parts of a key
     * that may be concatenated together.
     *
     * @param messageKey         String. One of the message keys in PurapKeyConstants.
     * @param messagePrefix      String. A prefix to the question key, such as "ap.question." that, concatenated with the question,
     *                           comprises the whole key of the message.
     * @param kualiConfiguration An instance of ConfigurationService
     * @param question           String. The most specific part of the message key in PurapKeyConstants.
     * @return The message to be displayed given the key
     */
    protected String getQuestionProperty(String messageKey, String messagePrefix, ConfigurationService kualiConfiguration, String question) {

        return kualiConfiguration.getPropertyValueAsString((StringUtils.isEmpty(messagePrefix)) ? messageKey : messagePrefix + question);
    }

}
