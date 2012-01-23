/*
 * Copyright 2009 The Kuali Foundation
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
package org.kuali.kfs.module.purap.document.workflow;

import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.rice.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.rice.krad.workflow.attribute.DataDictionaryDocumentSearchCustomizer;
import org.kuali.rice.krad.util.MessageMap;
public class PurchaseOrderWarningSearchGenerator extends DataDictionaryDocumentSearchCustomizer {


    public MessageMap getMessageMap(DocumentSearchCriteria searchCriteria) {
        //RICE20 no replacement for getMessageMap. can this be removed?
        MessageMap messageMap = super.getMessageMap(searchCriteria);
        if (messageMap == null) {
            messageMap = new MessageMap();
        }
        messageMap.putWarning("documentNumber", PurapConstants.WARNING_PURCHASEORDER_NUMBER_DONT_DISCLOSE);
        return messageMap;
    }
}
