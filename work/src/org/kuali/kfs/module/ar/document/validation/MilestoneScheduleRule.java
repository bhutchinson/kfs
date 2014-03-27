/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.kfs.module.ar.document.validation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.businessobject.MilestoneSchedule;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.validation.impl.KfsMaintenanceDocumentRuleBase;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.krad.util.ObjectUtils;

/**
 * Rules for the MilestoneSchedule maintenance document.
 */
public class MilestoneScheduleRule extends KfsMaintenanceDocumentRuleBase {
    protected static Logger LOG = org.apache.log4j.Logger.getLogger(MilestoneScheduleRule.class);
    protected MilestoneSchedule newMilestoneScheduleCopy;


    /**
     * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomSaveDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
     */
    @Override
    protected boolean processCustomSaveDocumentBusinessRules(MaintenanceDocument document) {
        LOG.debug("Entering MilestoneScheduleRule.processCustomSaveDocumentBusinessRules");
        processCustomRouteDocumentBusinessRules(document);
        LOG.info("Leaving MilestoneScheduleRule.processCustomSaveDocumentBusinessRules");
        return true; // save despite error messages
    }

    /**
     * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
     */
    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        LOG.debug("Entering MilestoneScheduleRule.processCustomRouteDocumentBusinessRules");
        boolean success = true;
        success &= checkAwardBillingFrequency();
        LOG.info("Leaving MilestoneScheduleRule.processCustomRouteDocumentBusinessRules");
        return success;
    }

    /**
     * checks to see if the billing frequency on the award is Milestone
     *
     * @return
     */
    protected boolean checkAwardBillingFrequency() {
        boolean success = false;

        if (ObjectUtils.isNotNull(newMilestoneScheduleCopy.getAward().getBillingFrequency())) {
            if (StringUtils.equals(newMilestoneScheduleCopy.getAward().getBillingFrequency().getFrequency(), ArConstants.MILESTONE_BILLING_SCHEDULE_CODE)) {
                success = true;
            }
        }

        if (!success) {
            putFieldError(KFSPropertyConstants.PROPOSAL_NUMBER, ArKeyConstants.ERROR_AWARD_MILESTONE_SCHEDULE_INCORRECT_BILLING_FREQUENCY, new String[] { newMilestoneScheduleCopy.getProposalNumber().toString() });
        }

        return success;
    }

    /**
     * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#setupConvenienceObjects()
     */
    @Override
    public void setupConvenienceObjects() {
        newMilestoneScheduleCopy = (MilestoneSchedule) super.getNewBo();
    }

}