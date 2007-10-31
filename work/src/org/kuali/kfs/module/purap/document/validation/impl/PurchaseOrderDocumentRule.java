/*
 * Copyright 2006-2007 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.module.purap.rules;

import static org.kuali.kfs.KFSConstants.GL_DEBIT_CODE;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.RicePropertyConstants;
import org.kuali.core.datadictionary.validation.fieldlevel.ZipcodeValidationPattern;
import org.kuali.core.document.AmountTotaling;
import org.kuali.core.util.ErrorMap;
import org.kuali.core.util.GlobalVariables;
import org.kuali.core.util.KualiDecimal;
import org.kuali.core.util.ObjectUtils;
import org.kuali.core.workflow.service.KualiWorkflowDocument;
import org.kuali.kfs.KFSConstants;
import org.kuali.kfs.KFSKeyConstants;
import org.kuali.kfs.bo.AccountingLine;
import org.kuali.kfs.bo.GeneralLedgerPendingEntry;
import org.kuali.kfs.context.SpringContext;
import org.kuali.kfs.document.AccountingDocument;
import org.kuali.module.purap.PurapConstants;
import org.kuali.module.purap.PurapKeyConstants;
import org.kuali.module.purap.PurapPropertyConstants;
import org.kuali.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.module.purap.PurapConstants.PurapDocTypeCodes;
import org.kuali.module.purap.PurapWorkflowConstants.PurchaseOrderDocument.NodeDetailEnum;
import org.kuali.module.purap.bo.PurchaseOrderItem;
import org.kuali.module.purap.bo.PurchaseOrderVendorStipulation;
import org.kuali.module.purap.bo.PurApItem;
import org.kuali.module.purap.document.PurchaseOrderDocument;
import org.kuali.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.module.purap.document.PurchasingDocument;
import org.kuali.module.purap.service.PurapGeneralLedgerService;
import org.kuali.module.vendor.VendorPropertyConstants;
import org.kuali.module.vendor.bo.VendorDetail;
import org.kuali.module.vendor.service.PhoneNumberService;
import org.kuali.module.vendor.service.VendorService;

/**
 * Business rule(s) applicable to Purchase Order document.
 */
public class PurchaseOrderDocumentRule extends PurchasingDocumentRuleBase {

    /**
     * Overrides the method in PurchasingDocumentRuleBase class in order to add validation for
     * the Vendor Stipulation Tab.
     * Tab included on Purchase Order Documents is Vendor Stipulation.
     * 
     * @param purapDocument  the purchase order document to be validated
     * @return               boolean false when an error is found in any validation.
     * @see                  org.kuali.module.purap.rules.PurchasingAccountsPayableDocumentRuleBase#processValidation(org.kuali.module.purap.document.PurchasingAccountsPayableDocument)
     */
    @Override
    public boolean processValidation(PurchasingAccountsPayableDocument purapDocument) {
        boolean valid = super.processValidation(purapDocument);
        valid &= processAdditionalValidation((PurchasingDocument) purapDocument);
        valid &= processVendorStipulationValidation((PurchaseOrderDocument) purapDocument);
        
        return valid;
    }

    /**
     * Performs any validation for the Additional tab, but currently it only returns true. Someday we might be able to
     * just remove this.
     * 
     * @param purDocument  the purchase order document to be validated
     * @return             boolean true (always return true for now)
     */
    public boolean processAdditionalValidation(PurchasingDocument purDocument) {
        boolean valid = true;

        return valid;
    }

    /**
     * Overrides the method in PurchasingDocumentRuleBase in order to call validateEmptyItemWithAccounts, validateItemForAmendment
     * and validateTradeInAndDiscountCoexistence in addition to what the superclass method has already provided.
     * 
     * @param purapDocument     the purchase order document to be validated
     * @return                  boolean false when an error is found in any validation.
     * @see                     org.kuali.module.purap.rules.PurchasingDocumentRuleBase#processItemValidation(org.kuali.module.purap.document.PurchasingDocument)
     */
    @Override
    public boolean processItemValidation(PurchasingAccountsPayableDocument purapDocument) {
        boolean valid = super.processItemValidation(purapDocument);
        for (PurApItem item : purapDocument.getItems()) {
            String identifierString = (item.getItemType().isItemTypeAboveTheLineIndicator() ? "Item " + item.getItemLineNumber().toString() : item.getItemType().getItemTypeDescription());
            valid &= validateEmptyItemWithAccounts((PurchaseOrderItem) item, identifierString);
            if (purapDocument.getDocumentHeader().getWorkflowDocument() != null && purapDocument.getDocumentHeader().getWorkflowDocument().getDocumentType().equals(PurapConstants.PurchaseOrderDocTypes.PURCHASE_ORDER_AMENDMENT_DOCUMENT)) {
                valid &= validateItemForAmendment((PurchaseOrderItem) item, identifierString);
            }
        }
        valid &= validateTradeInAndDiscountCoexistence((PurchasingDocument)purapDocument);
        
        return valid;
    }

    /**
     * 
     * Validates items for amendment. 
     * 
     * @param item               the item to be validated
     * @param identifierString   the identifier string of the item to be validated
     * @return                   boolean true if it passes the validation and false otherwise.
     */
    private boolean validateItemForAmendment(PurchaseOrderItem item, String identifierString) {
        boolean valid = true;
        if ((item.getItemInvoicedTotalQuantity() != null) && (!(item.getItemInvoicedTotalQuantity()).isZero())) {
            if (item.getItemQuantity() == null) {
                valid = false;
                GlobalVariables.getErrorMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY , PurapKeyConstants.ERROR_ITEM_AMND_NULL, "Item Quantity", identifierString);
            }
            else if (item.getItemQuantity().compareTo(item.getItemInvoicedTotalQuantity()) < 0) {
                valid = false;
                GlobalVariables.getErrorMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY , PurapKeyConstants.ERROR_ITEM_AMND_INVALID, "Item Quantity", identifierString);
            }
        }

        if (item.getItemInvoicedTotalAmount() != null) {
            KualiDecimal total = item.getExtendedPrice();
            if ((total == null) || total.compareTo(item.getItemInvoicedTotalAmount()) < 0) {
                valid = false;
                GlobalVariables.getErrorMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY , PurapKeyConstants.ERROR_ITEM_AMND_INVALID_AMT, "Item Extended Price", identifierString);
            }
        }
        
        return valid;
    }

    /**
     * Validates that the item detail must not be empty if its account is not empty and its item type is ITEM.
     * 
     * @param item              the item to be validated
     * @param identifierString  the identifier string of the item to be validated
     * @return                   boolean false if it is an above the line item and the item detail is empty and the account list is not empty.
     */
    boolean validateEmptyItemWithAccounts(PurchaseOrderItem item, String identifierString) {
        boolean valid = true;
        if (item.getItemType().isItemTypeAboveTheLineIndicator() && item.isItemDetailEmpty() && !item.isAccountListEmpty()) {
            valid = false;
            GlobalVariables.getErrorMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY, PurapKeyConstants.ERROR_ITEM_ACCOUNTING_NOT_ALLOWED, identifierString);
        }
        
        return valid;
    }

    /**
     * Validates that the purchase order cannot have both trade in and discount item.
     * 
     * @param purDocument   the purchase order document to be validated
     * @return              boolean false if trade in and discount both exist.
     */
    boolean validateTradeInAndDiscountCoexistence(PurchasingDocument purDocument) {
        boolean discountExists = false;
        boolean tradeInExists = false;

        for (PurApItem item : purDocument.getItems()) {
            if (item.getItemTypeCode().equals(ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE)) {
                discountExists = true;
                if (tradeInExists) {
                    GlobalVariables.getErrorMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY, PurapKeyConstants.ERROR_ITEM_TRADEIN_DISCOUNT_COEXISTENCE);
                    
                    return false;
                }
            }
            else if (item.getItemTypeCode().equals(ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE)) {
                tradeInExists = true;
                if (discountExists) {
                    GlobalVariables.getErrorMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY, PurapKeyConstants.ERROR_ITEM_TRADEIN_DISCOUNT_COEXISTENCE);
                    
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * Validation for the Stipulation tab. 
     * 
     * @param poDocument   the purchase order document to be validated
     * @return             boolean false if the vendor stipulation description is blank.
     */
    public boolean processVendorStipulationValidation(PurchaseOrderDocument poDocument) {
        boolean valid = true;
        List<PurchaseOrderVendorStipulation> stipulations = poDocument.getPurchaseOrderVendorStipulations();
        for (int i = 0; i < stipulations.size(); i++) {
            PurchaseOrderVendorStipulation stipulation = stipulations.get(i);
            if (StringUtils.isBlank(stipulation.getVendorStipulationDescription())) {
                GlobalVariables.getErrorMap().putError(PurapPropertyConstants.VENDOR_STIPULATION + "[" + i + "]." + PurapPropertyConstants.VENDOR_STIPULATION_DESCRIPTION, PurapKeyConstants.ERROR_STIPULATION_DESCRIPTION);
                valid = false;
            }
        }
        
        return valid;
    }

    /**
     * Overrides the method in PurchasingDocumentRuleBase in order to add validations that are specific for
     * Purchase Orders that aren't required for Requisitions.
     * 
     * @param purapDocument   the purchase order document to be validated
     * @return                boolean false when there is a failed validation.
     * @see                   org.kuali.module.purap.rules.PurchasingDocumentRuleBase#processVendorValidation(org.kuali.module.purap.document.PurchasingAccountsPayableDocument)
     */
    @Override
    public boolean processVendorValidation(PurchasingAccountsPayableDocument purapDocument) {
        ErrorMap errorMap = GlobalVariables.getErrorMap();
        errorMap.clearErrorPath();
        errorMap.addToErrorPath(RicePropertyConstants.DOCUMENT);
        boolean valid = super.processVendorValidation(purapDocument);
        PurchaseOrderDocument poDocument = (PurchaseOrderDocument) purapDocument;
        // check to see if the vendor exists in the database, i.e. its ID is not null
        Integer vendorHeaderID = poDocument.getVendorHeaderGeneratedIdentifier();
        if (ObjectUtils.isNull(vendorHeaderID)) {
            valid = false;
            errorMap.putError(VendorPropertyConstants.VENDOR_NAME, PurapKeyConstants.ERROR_NONEXIST_VENDOR);
        }
        if (StringUtils.isBlank(poDocument.getVendorCountryCode())) {
            // TODO can't this be done by the data dictionary?
            valid = false;
            errorMap.putError(PurapPropertyConstants.VENDOR_COUNTRY_CODE, KFSKeyConstants.ERROR_REQUIRED);
        }
        else if (poDocument.getVendorCountryCode().equals(KFSConstants.COUNTRY_CODE_UNITED_STATES)) {
            if (StringUtils.isBlank(poDocument.getVendorStateCode())) {
                valid = false;
                errorMap.putError(PurapPropertyConstants.VENDOR_STATE_CODE, KFSKeyConstants.ERROR_REQUIRED_FOR_US);
            }
            ZipcodeValidationPattern zipPattern = new ZipcodeValidationPattern();
            if (StringUtils.isBlank(poDocument.getVendorPostalCode())) {
                valid = false;
                errorMap.putError(PurapPropertyConstants.VENDOR_POSTAL_CODE, KFSKeyConstants.ERROR_REQUIRED_FOR_US);
            }
            else if (!zipPattern.matches(poDocument.getVendorPostalCode())) {
                valid = false;
                errorMap.putError(PurapPropertyConstants.VENDOR_POSTAL_CODE, PurapKeyConstants.ERROR_POSTAL_CODE_INVALID);
            }
        }
        errorMap.clearErrorPath();
        
        return valid;
    }

    /**
     * Validate that if Vendor Id (VendorHeaderGeneratedId) is not empty, and tranmission method is fax, vendor fax number cannot be
     * empty and must be valid. 
     * 
     * @param purDocument   the purchase order document to be validated
     * @return              boolean false if VendorHeaderGeneratedId is not empty, tranmission method is fax, and VendorFaxNumber is empty or invalid.
     *         
     */
    private boolean validateFaxNumberIfTransmissionTypeIsFax(PurchasingDocument purDocument) {
        boolean valid = true;
        GlobalVariables.getErrorMap().clearErrorPath();
        GlobalVariables.getErrorMap().addToErrorPath(RicePropertyConstants.DOCUMENT);
        if (ObjectUtils.isNotNull(purDocument.getVendorHeaderGeneratedIdentifier()) && purDocument.getPurchaseOrderTransmissionMethodCode().equals(PurapConstants.POTransmissionMethods.FAX)) {
            if (ObjectUtils.isNull(purDocument.getVendorFaxNumber()) || !SpringContext.getBean(PhoneNumberService.class).isValidPhoneNumber(purDocument.getVendorFaxNumber())) {
                GlobalVariables.getErrorMap().putError(PurapPropertyConstants.VENDOR_FAX_NUMBER, PurapKeyConstants.ERROR_FAX_NUMBER_PO_TRANSMISSION_TYPE);
                valid &= false;
            }
        }
        GlobalVariables.getErrorMap().clearErrorPath();
        
        return valid;
    }

    /**
     * Validate that if the PurchaseOrderTotalLimit is not null then the TotalDollarAmount cannot be greater than the
     * PurchaseOrderTotalLimit.
     * 
     * @param purDocument   the purchase order document to be validated
     * @return              True if the TotalDollarAmount is less than the PurchaseOrderTotalLimit. False otherwise.
     */
    public boolean validateTotalDollarAmountIsLessThanPurchaseOrderTotalLimit(PurchasingDocument purDocument) {
        boolean valid = true;
        KualiDecimal totalAmount = ((AmountTotaling) purDocument).getTotalDollarAmount();
        if (ObjectUtils.isNotNull(purDocument.getPurchaseOrderTotalLimit()) && ObjectUtils.isNotNull(totalAmount)) {           
            if (totalAmount.isGreaterThan(purDocument.getPurchaseOrderTotalLimit())) {
                valid &= false;
                GlobalVariables.getMessageList().add(PurapKeyConstants.PO_TOTAL_GREATER_THAN_PO_TOTAL_LIMIT);
            }
        }
        
        return valid;
    }

    /**
     * Overrides the method in PurapAccountingDocumentRuleBase in order to check that if the document will stop in
     * Internal Purchasing Review node, then return true.
     * 
     * @param financialDocument   the purchase order document to be validated
     * @param accountingLine      the accounting line to be validated
     * @param action              the AccountingLineAction enum that indicates what is being done to an accounting line
     * @return                    boolean true if the document will stop in Internal Purchasing Review node, otherwise return the
     *                            result of the checkAccountingLineAccountAccessibility in PurapAccountingDocumentRuleBase.
     * @see                       org.kuali.module.purap.rules.PurapAccountingDocumentRuleBase#checkAccountingLineAccountAccessibility(org.kuali.kfs.document.AccountingDocument, org.kuali.kfs.bo.AccountingLine, org.kuali.module.purap.rules.PurapAccountingDocumentRuleBase.AccountingLineAction)
     */
    @Override
    protected boolean checkAccountingLineAccountAccessibility(AccountingDocument financialDocument, AccountingLine accountingLine, AccountingLineAction action) {
        KualiWorkflowDocument workflowDocument = financialDocument.getDocumentHeader().getWorkflowDocument();
        List currentRouteLevels = getCurrentRouteLevels(workflowDocument);

        if (((PurchaseOrderDocument)financialDocument).isDocumentStoppedInRouteNode(NodeDetailEnum.INTERNAL_PURCHASING_REVIEW)) {
            // DO NOTHING: do not check that user owns acct lines; at this level, approvers can edit all detail on PO
            return true;
        }
        else {
            
            return super.checkAccountingLineAccountAccessibility(financialDocument, accountingLine, action);
        }
    }

    /**
     * Overrides the method in PurapAccountingDocumentRuleBase to call the customizeGeneralLedgerPendingEntry 
     * of the PurapGeneralLedgerService and set the financialDocumentTypeCode of the explicitEntry to "PO".
     * 
     * @param accountingDocument  
     * @param accountingLine      
     * @param explicitEntry        
     * @see org.kuali.module.purap.rules.PurapAccountingDocumentRuleBase#customizeExplicitGeneralLedgerPendingEntry(org.kuali.kfs.document.AccountingDocument, 
     *      org.kuali.kfs.bo.AccountingLine, org.kuali.kfs.bo.GeneralLedgerPendingEntry)
     */
    @Override
    protected void customizeExplicitGeneralLedgerPendingEntry(AccountingDocument accountingDocument, AccountingLine accountingLine, GeneralLedgerPendingEntry explicitEntry) {
        super.customizeExplicitGeneralLedgerPendingEntry(accountingDocument, accountingLine, explicitEntry);
        PurchaseOrderDocument po = (PurchaseOrderDocument)accountingDocument;

        SpringContext.getBean(PurapGeneralLedgerService.class).customizeGeneralLedgerPendingEntry(po, 
                accountingLine, explicitEntry, po.getPurapDocumentIdentifier(), GL_DEBIT_CODE, PurapDocTypeCodes.PO_DOCUMENT, true);

        explicitEntry.setFinancialDocumentTypeCode(PurapDocTypeCodes.PO_DOCUMENT);  //don't think i should have to override this, but default isn't getting the right PO doc
    }


}
