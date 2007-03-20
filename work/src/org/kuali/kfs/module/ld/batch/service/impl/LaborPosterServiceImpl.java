/*
 * Copyright 2007 The Kuali Foundation.
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
package org.kuali.module.labor.service.impl;

import static org.kuali.Constants.ParameterGroups.SYSTEM;
import static org.kuali.Constants.SystemGroupParameterNames.LABOR_POSTER_BALANCE_TYPES_NOT_PROCESSED;
import static org.kuali.Constants.SystemGroupParameterNames.LABOR_POSTER_OBJECT_CODES_NOT_PROCESSED;
import static org.kuali.Constants.SystemGroupParameterNames.LABOR_POSTER_PERIOD_CODES_NOT_PROCESSED;
import static org.kuali.module.gl.bo.OriginEntrySource.LABOR_MAIN_POSTER_ERROR;
import static org.kuali.module.gl.bo.OriginEntrySource.LABOR_MAIN_POSTER_VALID;
import static org.kuali.module.gl.bo.OriginEntrySource.LABOR_SCRUBBER_VALID;
import static org.kuali.module.labor.LaborConstants.DestinationNames.ORIGN_ENTRY;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.kuali.Constants;
import org.kuali.core.service.DateTimeService;
import org.kuali.core.service.KualiConfigurationService;
import org.kuali.module.gl.batch.poster.PostTransaction;
import org.kuali.module.gl.batch.poster.VerifyTransaction;
import org.kuali.module.gl.bo.OriginEntryGroup;
import org.kuali.module.gl.bo.Transaction;
import org.kuali.module.gl.service.OriginEntryGroupService;
import org.kuali.module.gl.service.impl.NightlyOutServiceImpl;
import org.kuali.module.gl.util.Message;
import org.kuali.module.gl.util.Summary;
import org.kuali.module.labor.LaborConstants;
import org.kuali.module.labor.LaborConstants.OperationType;
import org.kuali.module.labor.bo.LaborOriginEntry;
import org.kuali.module.labor.service.LaborOriginEntryService;
import org.kuali.module.labor.service.LaborPosterService;
import org.kuali.module.labor.service.LaborReportService;
import org.kuali.module.labor.util.ReportRegistry;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Labor Ledger Poster accepts pending entries generated by Labor Ledger e-docs (such as Salary Expense Transfer and Benefit
 * Expense Transfer), and combines them with entries from external systems. It edits the entries for validity. Invalid entries can
 * be marked for Labor Ledger Error Correction process. The Poster writes valid entries to the Labor Ledger Entry table, updates
 * balances in the Labor Ledger Balance table, and summarizes the entries for posting to the General Ledger.
 */
@Transactional
public class LaborPosterServiceImpl implements LaborPosterService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(LaborPosterServiceImpl.class);
    
    private LaborOriginEntryService laborOriginEntryService;
    private OriginEntryGroupService originEntryGroupService;

    private LaborReportService reportService;
    private DateTimeService dateTimeService;
    private VerifyTransaction laborPosterTransactionValidator;
    private KualiConfigurationService kualiConfigurationService;

    private PostTransaction laborLedgerEntryPoster;
    private PostTransaction laborLedgerBalancePoster;
    private PostTransaction laborGLLedgerEntryPoster;

    private final static int STEP = 1;
    private final static int LINE_INTERVAL = 2;

    /**
     * @see org.kuali.module.labor.service.LaborPosterService#postMainEntries()
     */
    public void postMainEntries() {
        Date runDate = dateTimeService.getCurrentSqlDate();

        OriginEntryGroup validGroup = originEntryGroupService.createGroup(runDate, LABOR_MAIN_POSTER_VALID, true, false, false);
        OriginEntryGroup invalidGroup = originEntryGroupService.createGroup(runDate, LABOR_MAIN_POSTER_ERROR, false, true, false);

        this.postLaborLedgerEntries(validGroup, invalidGroup, runDate);
        this.postLaborGLEntries(validGroup, runDate);
    }

    // post the qualified origin entries into Labor Ledger tables
    private void postLaborLedgerEntries(OriginEntryGroup validGroup, OriginEntryGroup invalidGroup, Date runDate) {
        String reportsDirectory = this.getReportsDirectory();
        Map<Transaction, List<Message>> errorMap = new HashMap<Transaction, List<Message>>();
        List<Summary> reportSummary = this.buildReportSummaryForLaborLedgerPosting();
        int numberOfOriginEntry = 0;

        Collection<OriginEntryGroup> postingGroups = originEntryGroupService.getGroupsToPost(LABOR_SCRUBBER_VALID);
        for (OriginEntryGroup entryGroup : postingGroups) {
            Iterator<LaborOriginEntry> entries = laborOriginEntryService.getEntriesByGroup(entryGroup);
            while (entries != null && entries.hasNext()) {
                LaborOriginEntry originEntry = entries.next();
                if(postSingleEntryIntoLaborLedger(originEntry, reportSummary, errorMap, validGroup, invalidGroup, runDate)){
                    numberOfOriginEntry++;
                    originEntry = null;
                }
            }
            // reset the process flag of the group so that it cannot be handled any more
            entryGroup.setProcess(Boolean.FALSE);
            originEntryGroupService.save(entryGroup);
        }
        updateReportSummary(reportSummary, ORIGN_ENTRY, OperationType.SELECT, numberOfOriginEntry, 0);
        
        reportService.generatePosterStatisticsReport(reportSummary, errorMap, ReportRegistry.LABOR_POSTER_STATISTICS, reportsDirectory, runDate);
        reportService.generatePosterInputSummaryReport(postingGroups, ReportRegistry.LABOR_POSTER_INPUT, reportsDirectory, runDate);
        reportService.generatePosterOutputSummaryReport(validGroup, ReportRegistry.LABOR_POSTER_OUTPUT, reportsDirectory, runDate);
        reportService.generatePosterErrorTransactionListing(invalidGroup, ReportRegistry.LABOR_POSTER_ERROR, reportsDirectory, runDate);
    }
    
    // post the given entry into the labor ledger tables if the entry is qualified; otherwise report error
    private boolean postSingleEntryIntoLaborLedger(LaborOriginEntry originEntry, List<Summary> reportSummary, Map<Transaction, List<Message>> errorMap, OriginEntryGroup validGroup, OriginEntryGroup invalidGroup, Date runDate){
        try {
            // reject the entry that is not postable
            if (!isPostableEntry(originEntry)) {
                return false;
            }

            // reject the invalid entry so that it can be available for error correction
            List<Message> errors = this.validateEntry(originEntry);
            if (!errors.isEmpty()) {
                errorMap.put(originEntry, errors);
                postAsProcessedOriginEntry(originEntry, invalidGroup, runDate);
                return false;
            }

            // post the current origin entry as a valid origin entry, ledger entry and ledger balance
            postAsProcessedOriginEntry(originEntry, validGroup, runDate);

            String operationOnLedgerEntry = postAsLedgerEntry(originEntry, runDate);
            updateReportSummary(reportSummary, laborLedgerEntryPoster.getDestinationName(), operationOnLedgerEntry, STEP, 0);

            String operationOnLedgerBalance = updateLedgerBalance(originEntry, runDate);
            updateReportSummary(reportSummary, laborLedgerBalancePoster.getDestinationName(), operationOnLedgerBalance, STEP, 0);
        }
        catch (Exception e) {
            LOG.error(e);
            return false;
        }     
        return true;
    }

    // determine if the given origin entry need to be posted
    private boolean isPostableEntry(LaborOriginEntry originEntry) {
        if (originEntry.getTransactionLedgerEntryAmount() == null || originEntry.getTransactionLedgerEntryAmount().isZero()) {
            return false;
        }
        else if (ArrayUtils.contains(this.getObjectsNotProcessed(), originEntry.getFinancialObjectCode())) {
            return false;
        }
        return true;
    }

    // validate the given entry, and generate an error list if the entry cannot meet the business rules
    private List<Message> validateEntry(LaborOriginEntry originEntry) {
        return laborPosterTransactionValidator.verifyTransaction(originEntry);
    }

    // post the processed entry into the approperiate group, either valid or invalid group
    private void postAsProcessedOriginEntry(LaborOriginEntry originEntry, OriginEntryGroup entryGroup, Date postDate) {
        originEntry.setEntryGroupId(entryGroup.getId());
        originEntry.setTransactionPostingDate(postDate);
        laborOriginEntryService.save(originEntry);
    }

    // post the given entry to the labor entry table
    private String postAsLedgerEntry(LaborOriginEntry originEntry, Date postDate) {
        return laborLedgerEntryPoster.post(originEntry, 0, postDate);
    }

    // update the labor ledger balance for the given entry
    private String updateLedgerBalance(LaborOriginEntry originEntry, Date postDate) {
        return laborLedgerBalancePoster.post(originEntry, 0, postDate);
    }

    // post the valid origin entries in the given group into General Ledger
    private void postLaborGLEntries(OriginEntryGroup validGroup, Date runDate) {
        String reportsDirectory = this.getReportsDirectory();
        List<Summary> reportSummary = this.buildReportSummaryForLaborGLPosting();
        Map<Transaction, List<Message>> errorMap = new HashMap<Transaction, List<Message>>();

        int numberOfOriginEntry = 0;
        Collection<LaborOriginEntry> entries = laborOriginEntryService.getConsolidatedEntryCollectionByGroup(validGroup);
        for (LaborOriginEntry originEntry : entries) {

            List<Message> errors = this.isPostableForLaborGLEntry(originEntry);
            if (!errors.isEmpty()) {
                errorMap.put(originEntry, errors);
                continue;
            }
            String operationType = laborGLLedgerEntryPoster.post(originEntry, 0, runDate);
            this.updateReportSummary(reportSummary, laborGLLedgerEntryPoster.getDestinationName(), operationType, STEP, 0);

            numberOfOriginEntry++;
        }
        updateReportSummary(reportSummary, ORIGN_ENTRY, OperationType.SELECT, numberOfOriginEntry, 0);
        reportService.generatePosterStatisticsReport(reportSummary, errorMap, ReportRegistry.LABOR_POSTER_GL_SUMMARY, reportsDirectory, runDate);
    }

    // determine if the given origin entry can be posted back to Labor GL entry
    private List<Message> isPostableForLaborGLEntry(LaborOriginEntry originEntry) {
        List<Message> errors = new ArrayList<Message>();
        if (ArrayUtils.contains(this.getPeriodCodesNotProcessed(), originEntry.getUniversityFiscalPeriodCode())) {
            errors.add(new Message("Cannot process the PERIOD CODES", 0));
        }
        else if (ArrayUtils.contains(this.getBalanceTypesNotProcessed(), originEntry.getFinancialBalanceTypeCode())) {
            errors.add(new Message("Cannot process the BALANCE TYPES", 0));
        }
        else if (originEntry.getTransactionLedgerEntryAmount().isZero()) {
            errors.add(new Message("Amount cannot be ZERO", 0));
        }
        return errors;
    }

    // build a report summary list for labor ledger posting
    private List<Summary> buildReportSummaryForLaborLedgerPosting() {
        List<Summary> reportSummary = new ArrayList<Summary>();

        String destination = laborLedgerEntryPoster.getDestinationName();
        reportSummary.addAll(buildDefualtReportSummary(destination, reportSummary.size() + LINE_INTERVAL));

        reportSummary.add(new Summary(reportSummary.size() + LINE_INTERVAL, "", 0));

        destination = laborLedgerBalancePoster.getDestinationName();
        reportSummary.addAll(buildDefualtReportSummary(destination, reportSummary.size() + LINE_INTERVAL));

        return reportSummary;
    }

    // build a report summary list for labor general ledger posting
    private List<Summary> buildReportSummaryForLaborGLPosting() {
        List<Summary> reportSummary = new ArrayList<Summary>();

        String destination = laborGLLedgerEntryPoster.getDestinationName();
        reportSummary.addAll(buildDefualtReportSummary(destination, reportSummary.size() + LINE_INTERVAL));

        return reportSummary;
    }

    // build a report summary list for labor general ledger posting
    private List<Summary> buildDefualtReportSummary(String destination, int startingOrder) {
        List<Summary> reportSummary = new ArrayList<Summary>();
        updateReportSummary(reportSummary, destination, OperationType.INSERT, 0, startingOrder++);
        updateReportSummary(reportSummary, destination, OperationType.UPDATE, 0, startingOrder++);
        updateReportSummary(reportSummary, destination, OperationType.DELETE, 0, startingOrder++);
        return reportSummary;
    }

    // update the report summary with the given information
    private void updateReportSummary(List<Summary> reportSummary, String destinationName, String operationType, int count, int order) {
        StringBuilder summaryDescription = this.buildSummaryDescription(destinationName, operationType);
        Summary inputSummary = new Summary(order, summaryDescription.toString(), count);

        int index = reportSummary.indexOf(inputSummary);
        if (index >= 0) {
            Summary summary = reportSummary.get(index);
            summary.setCount(summary.getCount() + count);
        }
        else {
            reportSummary.add(inputSummary);
        }
    }

    // build the description of summary with the given information
    private StringBuilder buildSummaryDescription(String destinationName, String operationType) {
        StringBuilder summaryDescription = new StringBuilder();
        summaryDescription.append("Number of ").append(destinationName).append(" records ").append(operationType).append(":");
        return summaryDescription;
    }

    public String getReportsDirectory() {
        return kualiConfigurationService.getPropertyString(Constants.REPORTS_DIRECTORY_KEY);
    }

    public String[] getBalanceTypesNotProcessed() {
        return kualiConfigurationService.getApplicationParameterValues(SYSTEM, LABOR_POSTER_BALANCE_TYPES_NOT_PROCESSED);
    }

    public String[] getObjectsNotProcessed() {
        return kualiConfigurationService.getApplicationParameterValues(SYSTEM, LABOR_POSTER_OBJECT_CODES_NOT_PROCESSED);
    }

    public String[] getPeriodCodesNotProcessed() {
        return kualiConfigurationService.getApplicationParameterValues(SYSTEM, LABOR_POSTER_PERIOD_CODES_NOT_PROCESSED);
    }

    /**
     * Sets the dateTimeService attribute value.
     * 
     * @param dateTimeService The dateTimeService to set.
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    /**
     * Sets the kualiConfigurationService attribute value.
     * 
     * @param kualiConfigurationService The kualiConfigurationService to set.
     */
    public void setKualiConfigurationService(KualiConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    /**
     * Sets the laborLedgerBalancePoster attribute value.
     * 
     * @param laborLedgerBalancePoster The laborLedgerBalancePoster to set.
     */
    public void setLaborLedgerBalancePoster(PostTransaction laborLedgerBalancePoster) {
        this.laborLedgerBalancePoster = laborLedgerBalancePoster;
    }

    /**
     * Sets the laborGLLedgerEntryPoster attribute value.
     * 
     * @param laborGLLedgerEntryPoster The laborGLLedgerEntryPoster to set.
     */
    public void setLaborGLLedgerEntryPoster(PostTransaction laborGLLedgerEntryPoster) {
        this.laborGLLedgerEntryPoster = laborGLLedgerEntryPoster;
    }

    /**
     * Sets the laborLedgerEntryPoster attribute value.
     * 
     * @param laborLedgerEntryPoster The laborLedgerEntryPoster to set.
     */
    public void setLaborLedgerEntryPoster(PostTransaction laborLedgerEntryPoster) {
        this.laborLedgerEntryPoster = laborLedgerEntryPoster;
    }

    /**
     * Sets the laborOriginEntryService attribute value.
     * 
     * @param laborOriginEntryService The laborOriginEntryService to set.
     */
    public void setLaborOriginEntryService(LaborOriginEntryService laborOriginEntryService) {
        this.laborOriginEntryService = laborOriginEntryService;
    }

    /**
     * Sets the originEntryGroupService attribute value.
     * 
     * @param originEntryGroupService The originEntryGroupService to set.
     */
    public void setOriginEntryGroupService(OriginEntryGroupService originEntryGroupService) {
        this.originEntryGroupService = originEntryGroupService;
    }

    /**
     * Sets the reportService attribute value.
     * 
     * @param reportService The reportService to set.
     */
    public void setReportService(LaborReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Sets the laborPosterTransactionValidator attribute value.
     * 
     * @param laborPosterTransactionValidator The laborPosterTransactionValidator to set.
     */
    public void setLaborPosterTransactionValidator(VerifyTransaction laborPosterTransactionValidator) {
        this.laborPosterTransactionValidator = laborPosterTransactionValidator;
    }
}