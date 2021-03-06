/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 * 
 * Copyright 2005-2014 The Kuali Foundation
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.purap.document.validation.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.UnitOfMeasure;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

public class PurchasingUnitOfMeasureValidation extends GenericValidation {

    private PurApItem itemForValidation;
    private DataDictionaryService dataDictionaryService;
    private BusinessObjectService businessObjectService;
    
    /**
     * Validates that if the item type is quantity based, the unit of measure is required.
     */
    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = true;
        PurchasingItemBase purItem = (PurchasingItemBase) itemForValidation;
        
        // Validations for quantity based item type
        if (purItem.getItemType().isQuantityBasedGeneralLedgerIndicator()) {
            
            String errorPrefix = KFSPropertyConstants.DOCUMENT + "." + PurapPropertyConstants.ITEM + "[" + (itemForValidation.getItemLineNumber() - 1) + "]." + PurapPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE;
            String uomCode = purItem.getItemUnitOfMeasureCode();
            if (StringUtils.isEmpty(uomCode)) {
                valid = false;
                String attributeLabel = dataDictionaryService.getDataDictionary().getBusinessObjectEntry(purItem.getClass().getName()).
                                        getAttributeDefinition(KFSPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE).
                                        getLabel();
               GlobalVariables.getMessageMap().putError(errorPrefix, KFSKeyConstants.ERROR_REQUIRED, attributeLabel + " in " + purItem.getItemIdentifierString());
            }
            else {
                //Find out whether the unit of measure code has existed in the database
                Map<String,String> fieldValues = new HashMap<String, String>();
                fieldValues.put(KFSPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE, purItem.getItemUnitOfMeasureCode());
                if (businessObjectService.countMatching(UnitOfMeasure.class, fieldValues) != 1) {
                    //This is the case where the unit of measure code on the item does not exist in the database.
                    valid = false;
                    GlobalVariables.getMessageMap().putError(errorPrefix, PurapKeyConstants.PUR_ITEM_UNIT_OF_MEASURE_CODE_INVALID,  " in " + purItem.getItemIdentifierString());
                }
                //Validate UOM for active check.
                if(ObjectUtils.isNotNull(purItem.getItemUnitOfMeasure())){
                    if(!purItem.getItemUnitOfMeasure().isActive()){
                        valid = false;
                        GlobalVariables.getMessageMap().putError(errorPrefix, PurapKeyConstants.ERROR_ITEM_UOM_INACTIVE,  " in " + purItem.getItemIdentifierString());
                    }
                }
            }            
        }
               
       if (purItem.getItemType().isAmountBasedGeneralLedgerIndicator() && StringUtils.isNotBlank(purItem.getItemUnitOfMeasureCode())) {
            valid = false;
            String errorPrefix = KFSPropertyConstants.DOCUMENT + "." + PurapPropertyConstants.ITEM + "[" + (itemForValidation.getItemLineNumber() - 1) + "]." + PurapPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE;
            String attributeLabel = dataDictionaryService.getDataDictionary().getBusinessObjectEntry(purItem.getClass().getName()).
                                    getAttributeDefinition(PurapPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE).getLabel();
            GlobalVariables.getMessageMap().putError(errorPrefix, PurapKeyConstants.ERROR_ITEM_UOM_NOT_ALLOWED, attributeLabel + " in " + purItem.getItemIdentifierString());
        }
             

        return valid;
    }

    public PurApItem getItemForValidation() {
        return itemForValidation;
    }

    public void setItemForValidation(PurApItem itemForValidation) {
        this.itemForValidation = itemForValidation;
    }

    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
