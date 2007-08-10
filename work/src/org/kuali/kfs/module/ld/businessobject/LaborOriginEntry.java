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

package org.kuali.module.labor.bo;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.kuali.core.bo.DocumentHeader;
import org.kuali.core.bo.DocumentType;
import org.kuali.core.util.GlobalVariables;
import org.kuali.core.util.KualiDecimal;
import org.kuali.kfs.KFSKeyConstants;
import org.kuali.kfs.KFSPropertyConstants;
import org.kuali.kfs.bo.GeneralLedgerPendingEntry;
import org.kuali.kfs.bo.OriginationCode;
import org.kuali.module.chart.bo.AccountingPeriod;
import org.kuali.module.gl.bo.OriginEntry;
import org.kuali.module.gl.exception.LoadException;
import org.kuali.module.labor.LaborConstants;
import org.springframework.util.StringUtils;

/**
 * @author Kuali Nervous System Team (kualidev@oncourse.iu.edu)
 */
public class LaborOriginEntry extends OriginEntry implements LaborTransaction {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(LaborOriginEntry.class);
    private static String SPACES = "                                                                                                              ";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    

    private String positionNumber;
    private Date transactionPostingDate;
    private Date payPeriodEndDate;
    private KualiDecimal transactionTotalHours;
    private Integer payrollEndDateFiscalYear;
    private String payrollEndDateFiscalPeriodCode;
    private String financialDocumentApprovedCode;
    private String transactionEntryOffsetCode;
    private Date transactionEntryProcessedTimestamp;
    private String emplid;
    private Integer employeeRecord;
    private String earnCode;
    private String payGroup;
    private String salaryAdministrationPlan;
    private String grade;
    private String runIdentifier;
    
    private String laborLedgerOriginalChartOfAccountsCode;
    private String laborLedgerOriginalAccountNumber;
    private String laborLedgerOriginalSubAccountNumber;
    private String laborLedgerOriginalFinancialObjectCode;
    private String laborLedgerOriginalFinancialSubObjectCode;
    private String hrmsCompany;
    private String setid;
    private Date transactionDateTimeStamp;

    private DocumentHeader financialDocument;
    private DocumentType referenceFinancialDocumentType;
    private OriginationCode referenceFinancialSystemOrigination;
    private AccountingPeriod payrollEndDateFiscalPeriod;
   
    
    /**
     * Default constructor.
     */
    public LaborOriginEntry(GeneralLedgerPendingEntry glpe) {
        super();        
    }
    
    public LaborOriginEntry(String financialDocumentTypeCode, String financialSystemOriginationCode) {
        super(financialDocumentTypeCode, financialSystemOriginationCode);
    }
    
    public LaborOriginEntry() {
        this(null, null);
    }
    
    public LaborOriginEntry(LaborTransaction t) {
        this();
        copyFieldsFromTransaction(t);
        
        setPositionNumber(t.getPositionNumber());
        setTransactionPostingDate(t.getTransactionPostingDate());
        setPayPeriodEndDate(t.getPayPeriodEndDate());
        setTransactionTotalHours(t.getTransactionTotalHours());
        setPayrollEndDateFiscalYear(t.getPayrollEndDateFiscalYear());
        setPayrollEndDateFiscalPeriodCode(t.getPayrollEndDateFiscalPeriodCode());
        setFinancialDocumentApprovedCode(t.getFinancialDocumentApprovedCode());
        setTransactionEntryOffsetCode(t.getTransactionEntryOffsetCode());
        setTransactionEntryProcessedTimestamp(t.getTransactionEntryProcessedTimestamp());
        setEmplid(t.getEmplid());
        setEmployeeRecord(t.getEmployeeRecord());
        setEarnCode(t.getEarnCode());
        setPayGroup(t.getPayGroup());
        setSalaryAdministrationPlan(t.getSalaryAdministrationPlan());
        setGrade(t.getGrade());
        setRunIdentifier(t.getRunIdentifier());
        setLaborLedgerOriginalChartOfAccountsCode(t.getLaborLedgerOriginalChartOfAccountsCode());
        setLaborLedgerOriginalAccountNumber(t.getLaborLedgerOriginalAccountNumber());
        setLaborLedgerOriginalSubAccountNumber(t.getLaborLedgerOriginalSubAccountNumber());
        setLaborLedgerOriginalFinancialObjectCode(t.getLaborLedgerOriginalFinancialObjectCode());
        setLaborLedgerOriginalFinancialSubObjectCode(t.getLaborLedgerOriginalFinancialSubObjectCode());
        setHrmsCompany(getHrmsCompany());
        setSetid(t.getSetid());
        //public Date transactionDateTimeStamp;
       
        setFinancialDocument(t.getFinancialDocument());
        setReferenceFinancialDocumentType(t.getReferenceFinancialDocumentType());
        setReferenceFinancialSystemOrigination(t.getReferenceFinancialSystemOrigination());
        setPayrollEndDateFiscalPeriod(t.getPayrollEndDateFiscalPeriod());
        
    }

    public LaborOriginEntry(String line) {
        try {
            setFromTextFile(line, 0);
        }
            catch (LoadException e) {
                LOG.error("OriginEntry() Error loading line", e);
            }
     
    }
    
    /**
     * Gets the positionNumber attribute.
     * 
     * @return Returns the positionNumber
     * 
     */
    public String getPositionNumber() { 
        return positionNumber;
    }

    /**
     * Sets the positionNumber attribute.
     * 
     * @param positionNumber The positionNumber to set.
     * 
     */
    public void setPositionNumber(String positionNumber) {
        this.positionNumber = positionNumber;
    }


    /**
     * Gets the projectCode attribute.
     * 
     * @return Returns the projectCode
     * 
     */

    /**
     * Gets the transactionPostingDate attribute.
     * 
     * @return Returns the transactionPostingDate
     * 
     */
    public Date getTransactionPostingDate() { 
        return transactionPostingDate;
    }

    /**
     * Sets the transactionPostingDate attribute.
     * 
     * @param transactionPostingDate The transactionPostingDate to set.
     * 
     */
    public void setTransactionPostingDate(Date transactionPostingDate) {
        this.transactionPostingDate = transactionPostingDate;
    }


    /**
     * Gets the payPeriodEndDate attribute.
     * 
     * @return Returns the payPeriodEndDate
     * 
     */
    public Date getPayPeriodEndDate() { 
        return payPeriodEndDate;
    }

    /**
     * Sets the payPeriodEndDate attribute.
     * 
     * @param payPeriodEndDate The payPeriodEndDate to set.
     * 
     */
    public void setPayPeriodEndDate(Date payPeriodEndDate) {
        this.payPeriodEndDate = payPeriodEndDate;
    }


    /**
     * Gets the transactionTotalHours attribute.
     * 
     * @return Returns the transactionTotalHours
     * 
     */
    public KualiDecimal getTransactionTotalHours() { 
        return transactionTotalHours;
    }

    /**
     * Sets the transactionTotalHours attribute.
     * 
     * @param transactionTotalHours The transactionTotalHours to set.
     * 
     */
    public void setTransactionTotalHours(KualiDecimal transactionTotalHours) {
        this.transactionTotalHours = transactionTotalHours;
    }


    /**
     * Gets the payrollEndDateFiscalYear attribute.
     * 
     * @return Returns the payrollEndDateFiscalYear
     * 
     */
    public Integer getPayrollEndDateFiscalYear() { 
        return payrollEndDateFiscalYear;
    }

    /**
     * Sets the payrollEndDateFiscalYear attribute.
     * 
     * @param payrollEndDateFiscalYear The payrollEndDateFiscalYear to set.
     * 
     */
    public void setPayrollEndDateFiscalYear(Integer payrollEndDateFiscalYear) {
        this.payrollEndDateFiscalYear = payrollEndDateFiscalYear;
    }


    /**
     * Gets the payrollEndDateFiscalPeriodCode attribute.
     * 
     * @return Returns the payrollEndDateFiscalPeriodCode
     * 
     */
    public String getPayrollEndDateFiscalPeriodCode() { 
        return payrollEndDateFiscalPeriodCode;
    }

    /**
     * Sets the payrollEndDateFiscalPeriodCode attribute.
     * 
     * @param payrollEndDateFiscalPeriodCode The payrollEndDateFiscalPeriodCode to set.
     * 
     */
    public void setPayrollEndDateFiscalPeriodCode(String payrollEndDateFiscalPeriodCode) {
        this.payrollEndDateFiscalPeriodCode = payrollEndDateFiscalPeriodCode;
    }


    /**
     * Gets the financialDocumentApprovedCode attribute.
     * 
     * @return Returns the financialDocumentApprovedCode
     * 
     */
    public String getFinancialDocumentApprovedCode() { 
        return financialDocumentApprovedCode;
    }

    /**
     * Sets the financialDocumentApprovedCode attribute.
     * 
     * @param financialDocumentApprovedCode The financialDocumentApprovedCode to set.
     * 
     */
    public void setFinancialDocumentApprovedCode(String financialDocumentApprovedCode) {
        this.financialDocumentApprovedCode = financialDocumentApprovedCode;
    }


    /**
     * Gets the transactionEntryOffsetCode attribute.
     * 
     * @return Returns the transactionEntryOffsetCode
     * 
     */
    public String getTransactionEntryOffsetCode() { 
        return transactionEntryOffsetCode;
    }

    /**
     * Sets the transactionEntryOffsetCode attribute.
     * 
     * @param transactionEntryOffsetCode The transactionEntryOffsetCode to set.
     * 
     */
    public void setTransactionEntryOffsetCode(String transactionEntryOffsetCode) {
        this.transactionEntryOffsetCode = transactionEntryOffsetCode;
    }


    /**
     * Gets the transactionEntryProcessedTimestamp attribute.
     * 
     * @return Returns the transactionEntryProcessedTimestamp
     * 
     */
    public Date getTransactionEntryProcessedTimestamp() { 
        return transactionEntryProcessedTimestamp;
    }

    /**
     * Sets the transactionEntryProcessedTimestamp attribute.
     * 
     * @param transactionEntryProcessedTimestamp The transactionEntryProcessedTimestamp to set.
     * 
     */
    public void setTransactionEntryProcessedTimestamp(Date transactionEntryProcessedTimestamp) {
        this.transactionEntryProcessedTimestamp = transactionEntryProcessedTimestamp;
    }


    /**
     * Gets the emplid attribute.
     * 
     * @return Returns the emplid
     * 
     */
    public String getEmplid() { 
        return emplid;
    }

    /**
     * Sets the emplid attribute.
     * 
     * @param emplid The emplid to set.
     * 
     */
    public void setEmplid(String emplid) {
        this.emplid = emplid;
    }


    /**
     * Gets the employeeRecord attribute.
     * 
     * @return Returns the employeeRecord
     * 
     */
    public Integer getEmployeeRecord() { 
        return employeeRecord;
    }

    /**
     * Sets the employeeRecord attribute.
     * 
     * @param employeeRecord The employeeRecord to set.
     * 
     */
    public void setEmployeeRecord(Integer employeeRecord) {
        this.employeeRecord = employeeRecord;
    }


    /**
     * Gets the earnCode attribute.
     * 
     * @return Returns the earnCode
     * 
     */
    public String getEarnCode() { 
        return earnCode;
    }

    /**
     * Sets the earnCode attribute.
     * 
     * @param earnCode The earnCode to set.
     * 
     */
    public void setEarnCode(String earnCode) {
        this.earnCode = earnCode;
    }


    /**
     * Gets the payGroup attribute.
     * 
     * @return Returns the payGroup
     * 
     */
    public String getPayGroup() { 
        return payGroup;
    }

    /**
     * Sets the payGroup attribute.
     * 
     * @param payGroup The payGroup to set.
     * 
     */
    public void setPayGroup(String payGroup) {
        this.payGroup = payGroup;
    }


    /**
     * Gets the salaryAdministrationPlan attribute.
     * 
     * @return Returns the salaryAdministrationPlan
     * 
     */
    public String getSalaryAdministrationPlan() { 
        return salaryAdministrationPlan;
    }

    /**
     * Sets the salaryAdministrationPlan attribute.
     * 
     * @param salaryAdministrationPlan The salaryAdministrationPlan to set.
     * 
     */
    public void setSalaryAdministrationPlan(String salaryAdministrationPlan) {
        this.salaryAdministrationPlan = salaryAdministrationPlan;
    }


    /**
     * Gets the grade attribute.
     * 
     * @return Returns the grade
     * 
     */
    public String getGrade() { 
        return grade;
    }

    /**
     * Sets the grade attribute.
     * 
     * @param grade The grade to set.
     * 
     */
    public void setGrade(String grade) {
        this.grade = grade;
    }


    /**
     * Gets the runIdentifier attribute.
     * 
     * @return Returns the runIdentifier
     * 
     */
    public String getRunIdentifier() { 
        return runIdentifier;
    }

    /**
     * Sets the runIdentifier attribute.
     * 
     * @param runIdentifier The runIdentifier to set.
     * 
     */
    public void setRunIdentifier(String runIdentifier) {
        this.runIdentifier = runIdentifier;
    }
    
    /**
     * Gets the laborLedgerOriginalChartOfAccountsCode attribute.
     * 
     * @return Returns the laborLedgerOriginalChartOfAccountsCode
     * 
     */
    public String getLaborLedgerOriginalChartOfAccountsCode() { 
        return laborLedgerOriginalChartOfAccountsCode;
    }

    /**
     * Sets the laborLedgerOriginalChartOfAccountsCode attribute.
     * 
     * @param laborLedgerOriginalChartOfAccountsCode The laborLedgerOriginalChartOfAccountsCode to set.
     * 
     */
    public void setLaborLedgerOriginalChartOfAccountsCode(String laborLedgerOriginalChartOfAccountsCode) {
        this.laborLedgerOriginalChartOfAccountsCode = laborLedgerOriginalChartOfAccountsCode;
    }

    

    /**
     * Gets the laborLedgerOriginalAccountNumber attribute.
     * 
     * @return Returns the laborLedgerOriginalAccountNumber
     * 
     */
    public String getLaborLedgerOriginalAccountNumber() { 
        return laborLedgerOriginalAccountNumber;
    }

    /**
     * Sets the laborLedgerOriginalAccountNumber attribute.
     * 
     * @param laborLedgerOriginalAccountNumber The laborLedgerOriginalAccountNumber to set.
     * 
     */
    public void setLaborLedgerOriginalAccountNumber(String laborLedgerOriginalAccountNumber) {
        this.laborLedgerOriginalAccountNumber = laborLedgerOriginalAccountNumber;
    }


    /**
     * Gets the laborLedgerOriginalSubAccountNumber attribute.
     * 
     * @return Returns the laborLedgerOriginalSubAccountNumber
     * 
     */
    public String getLaborLedgerOriginalSubAccountNumber() { 
        return laborLedgerOriginalSubAccountNumber;
    }

    /**
     * Sets the laborLedgerOriginalSubAccountNumber attribute.
     * 
     * @param laborLedgerOriginalSubAccountNumber The laborLedgerOriginalSubAccountNumber to set.
     * 
     */
    public void setLaborLedgerOriginalSubAccountNumber(String laborLedgerOriginalSubAccountNumber) {
        this.laborLedgerOriginalSubAccountNumber = laborLedgerOriginalSubAccountNumber;
    }


    /**
     * Gets the laborLedgerOriginalFinancialObjectCode attribute.
     * 
     * @return Returns the laborLedgerOriginalFinancialObjectCode
     * 
     */
    public String getLaborLedgerOriginalFinancialObjectCode() { 
        return laborLedgerOriginalFinancialObjectCode;
    }

    /**
     * Sets the laborLedgerOriginalFinancialObjectCode attribute.
     * 
     * @param laborLedgerOriginalFinancialObjectCode The laborLedgerOriginalFinancialObjectCode to set.
     * 
     */
    public void setLaborLedgerOriginalFinancialObjectCode(String laborLedgerOriginalFinancialObjectCode) {
        this.laborLedgerOriginalFinancialObjectCode = laborLedgerOriginalFinancialObjectCode;
    }


    /**
     * Gets the laborLedgerOriginalFinancialSubObjectCode attribute.
     * 
     * @return Returns the laborLedgerOriginalFinancialSubObjectCode
     * 
     */
    public String getLaborLedgerOriginalFinancialSubObjectCode() { 
        return laborLedgerOriginalFinancialSubObjectCode;
    }

    /**
     * Sets the laborLedgerOriginalFinancialSubObjectCode attribute.
     * 
     * @param laborLedgerOriginalFinancialSubObjectCode The laborLedgerOriginalFinancialSubObjectCode to set.
     * 
     */
    public void setLaborLedgerOriginalFinancialSubObjectCode(String laborLedgerOriginalFinancialSubObjectCode) {
        this.laborLedgerOriginalFinancialSubObjectCode = laborLedgerOriginalFinancialSubObjectCode;
    }


    /**
     * Gets the hrmsCompany attribute.
     * 
     * @return Returns the hrmsCompany
     * 
     */
    public String getHrmsCompany() { 
        return hrmsCompany;
    }

    /**
     * Sets the hrmsCompany attribute.
     * 
     * @param hrmsCompany The hrmsCompany to set.
     * 
     */
    public void setHrmsCompany(String hrmsCompany) {
        this.hrmsCompany = hrmsCompany;
    }


    /**
     * Gets the setid attribute.
     * 
     * @return Returns the setid
     * 
     */
    public String getSetid() { 
        return setid;
    }

    /**
     * Sets the setid attribute.
     * 
     * @param setid The setid to set.
     * 
     */
    public void setSetid(String setid) {
        this.setid = setid;
    }


    /**
     * Gets the transactionDateTimeStamp attribute.
     * 
     * @return Returns the transactionDateTimeStamp
     * 
     */
    public Date getTransactionDateTimeStamp() { 
        return transactionDateTimeStamp;
    }

    /**
     * Sets the transactionDateTimeStamp attribute.
     * 
     * @param transactionDateTimeStamp The transactionDateTimeStamp to set.
     * 
     */
    public void setTransactionDateTimeStamp(Date transactionDateTimeStamp) {
        this.transactionDateTimeStamp = transactionDateTimeStamp;
    }

    /**
     * Gets the financialDocument attribute. 
     * @return Returns the financialDocument.
     */
    public DocumentHeader getFinancialDocument() {
        return financialDocument;
    }

    /**
     * Sets the financialDocument attribute value.
     * @param financialDocument The financialDocument to set.
     */
    @Deprecated
    public void setFinancialDocument(DocumentHeader financialDocument) {
        this.financialDocument = financialDocument;
    }

    /**
     * Gets the payrollEndDateFiscalPeriod attribute. 
     * @return Returns the payrollEndDateFiscalPeriod.
     */
    public AccountingPeriod getPayrollEndDateFiscalPeriod() {
        return payrollEndDateFiscalPeriod;
    }

    /**
     * Sets the payrollEndDateFiscalPeriod attribute value.
     * @param payrollEndDateFiscalPeriod The payrollEndDateFiscalPeriod to set.
     */
    @Deprecated
    public void setPayrollEndDateFiscalPeriod(AccountingPeriod payrollEndDateFiscalPeriod) {
        this.payrollEndDateFiscalPeriod = payrollEndDateFiscalPeriod;
    }

    /**
     * Gets the referenceFinancialDocumentType attribute. 
     * @return Returns the referenceFinancialDocumentType.
     */
    public DocumentType getReferenceFinancialDocumentType() {
        return referenceFinancialDocumentType;
    }

    /**
     * Sets the referenceFinancialDocumentType attribute value.
     * @param referenceFinancialDocumentType The referenceFinancialDocumentType to set.
     */
    @Deprecated
    public void setReferenceFinancialDocumentType(DocumentType referenceFinancialDocumentType) {
        this.referenceFinancialDocumentType = referenceFinancialDocumentType;
    }

    /**
     * Gets the referenceFinancialSystemOrigination attribute. 
     * @return Returns the referenceFinancialSystemOrigination.
     */
    public OriginationCode getReferenceFinancialSystemOrigination() {
        return referenceFinancialSystemOrigination;
    }

    /**
     * Sets the referenceFinancialSystemOrigination attribute value.
     * @param referenceFinancialSystemOrigination The referenceFinancialSystemOrigination to set.
     */
    @Deprecated
    public void setReferenceFinancialSystemOrigination(OriginationCode referenceFinancialSystemOrigination) {
        this.referenceFinancialSystemOrigination = referenceFinancialSystemOrigination;
    }
    
    public void clearTransactionTotalHours(){
        this.transactionTotalHours = null;
    }
    

    /*  *//**
     * @see org.kuali.core.bo.BusinessObjectBase#toStringMapper()
     *//*
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();      
        if (super.getEntryId() != null) {
            m.put("entryId", super.getEntryId().toString());
        }
        return m;
    }
    */
 
    public String getLine() {
        StringBuffer sb = new StringBuffer();
        if (universityFiscalYear == null) {
            sb.append("    ");
        }
        else {
            sb.append(universityFiscalYear);
        }

        sb.append(getField(2, chartOfAccountsCode));
        sb.append(getField(7, accountNumber));
        sb.append(getField(5, subAccountNumber));
        sb.append(getField(4, financialObjectCode));
        sb.append(getField(3, financialSubObjectCode));
        sb.append(getField(2, financialBalanceTypeCode));
        sb.append(getField(2, financialObjectTypeCode));
        sb.append(getField(2, universityFiscalPeriodCode));
        sb.append(getField(4, financialDocumentTypeCode));
        sb.append(getField(2, financialSystemOriginationCode));
        sb.append(getField(14, documentNumber));

        // This is the cobol code for transaction sequence numbers.
        // 3025 019280 IF TRN-ENTR-SEQ-NBR OF GLEN-RECORD NOT NUMERIC
        // 3026 019290 MOVE ZEROES TO TRN-ENTR-SEQ-NBR OF GLEN-RECORD
        // 3027 019300 END-IF
        // 3028 019310 IF TRN-ENTR-SEQ-NBR OF GLEN-RECORD = SPACES
        // 3029 019320 MOVE ZEROES
        // 3030 019330 TO TRN-ENTR-SEQ-NBR OF ALT-GLEN-RECORD
        // 3031 019340 ELSE
        // 3032 019350 MOVE TRN-ENTR-SEQ-NBR OF GLEN-RECORD
        // 3033 019360 TO TRN-ENTR-SEQ-NBR OF ALT-GLEN-RECORD
        // 3034 019370 END-IF

        if (transactionLedgerEntrySequenceNumber == null) {
            sb.append("00000");
        }
        else {
            // Format to a length of 5
            String seqNum = transactionLedgerEntrySequenceNumber.toString();
            while (5 > seqNum.length()) {
                seqNum = "0" + seqNum;
            }
            sb.append(seqNum);
        }
        
        
        //Labor Specified fields
               
        sb.append(getField(8, positionNumber));
        sb.append(getField(10, projectCode));
        sb.append(getField(40, transactionLedgerEntryDescription));
        
        //The length of Labor's transactionLedgerEntryAmount is 19
        //GL's transactionLedgerEntryAmount is 17
        if (transactionLedgerEntryAmount == null) {
            sb.append("                   ");
        }
        else {
            String a = transactionLedgerEntryAmount.toString();
            sb.append("                   ".substring(0, 19 - a.length()));
            sb.append(a);
        }
        
        sb.append(getField(1, transactionDebitCreditCode));
        sb.append(formatDate(transactionDate));
        sb.append(getField(10, organizationDocumentNumber));
        sb.append(getField(8, organizationReferenceId));
        sb.append(getField(4, referenceFinancialDocumentTypeCode));
        sb.append(getField(2, referenceFinancialSystemOriginationCode));
        sb.append(getField(14, referenceFinancialDocumentNumber));
        sb.append(formatDate(financialDocumentReversalDate));
        sb.append(getField(1, transactionEncumbranceUpdateCode));
        
        
        sb.append(formatDate(transactionPostingDate));
        sb.append(formatDate(payPeriodEndDate));
        
        if (transactionTotalHours == null){
            sb.append("         ");
        } else {
            sb.append(getField(9, transactionTotalHours.toString()));
        }
        
        if (payrollEndDateFiscalYear == null){
            sb.append("   ");
        } else {
            sb.append(getField(4, payrollEndDateFiscalYear.toString()));
        }
        
       
        sb.append(getField(2, payrollEndDateFiscalPeriodCode));
        sb.append(getField(11, emplid));
        
        if (employeeRecord == null){
            sb.append("   ");
        } else {
            sb.append(getField(3, employeeRecord.toString()));
        }
        
        sb.append(getField(3, earnCode));
        sb.append(getField(3, payGroup));
        sb.append(getField(4, salaryAdministrationPlan));
        sb.append(getField(3, grade));
        sb.append(getField(10, runIdentifier));
        sb.append(getField(2, laborLedgerOriginalChartOfAccountsCode));
        sb.append(getField(7, laborLedgerOriginalAccountNumber));
        sb.append(getField(5, laborLedgerOriginalSubAccountNumber));
        sb.append(getField(4, laborLedgerOriginalFinancialObjectCode));
        sb.append(getField(3, laborLedgerOriginalFinancialSubObjectCode));
        sb.append(getField(3, hrmsCompany));
        sb.append(getField(5, setid));
        
        //TODO: Ask to Sterling about Pad. 
        // pad to full length of 173 chars.
        /*while (173 > sb.toString().length()) {
            sb.append(' ');
        }*/
        
        return sb.toString();
      
        
    }
    
    
    
    public void setFromTextFile(String line, int lineNumber) throws LoadException {

        // Just in case
        line = line + SPACES;

        if (!"    ".equals(line.substring(0, 4))) {
            try {
                setUniversityFiscalYear(new Integer(line.substring(0, 4)));
            }
            catch (NumberFormatException e) {
                GlobalVariables.getErrorMap().putError("fileUpload", KFSKeyConstants.ERROR_NUMBER_FORMAT_ORIGIN_ENTRY_FROM_TEXT_FILE, new String[] { new Integer(lineNumber).toString(), "University Fiscal Year" });
                throw new LoadException("Invalid university fiscal year");
            }

        }
        else {
            setUniversityFiscalYear(null);
        }

        setChartOfAccountsCode(getValue(line, 4, 6));
        setAccountNumber(getValue(line, 6, 13));
        setSubAccountNumber(getValue(line, 13, 18));
        setFinancialObjectCode(getValue(line, 18, 22));
        setFinancialSubObjectCode(getValue(line, 22, 25));
        setFinancialBalanceTypeCode(getValue(line, 25, 27));
        setFinancialObjectTypeCode(getValue(line, 27, 29));
        setUniversityFiscalPeriodCode(getValue(line, 29, 31));
        setFinancialDocumentTypeCode(getValue(line, 31, 35));
        setFinancialSystemOriginationCode(getValue(line, 35, 37));
        setDocumentNumber(getValue(line, 37, 51));
        if (!"     ".equals(line.substring(51, 56)) && !"00000".equals(line.substring(51, 56))) {
            try {
                setTransactionLedgerEntrySequenceNumber(new Integer(line.substring(51, 56).trim()));
        }
            catch (NumberFormatException e) {
                GlobalVariables.getErrorMap().putError("fileUpload", KFSKeyConstants.ERROR_NUMBER_FORMAT_ORIGIN_ENTRY_FROM_TEXT_FILE, new String[] { new Integer(lineNumber).toString(), "Sequence Number" });
                throw new LoadException("Invalid sequence number");
            }
        }
        else {
            setTransactionLedgerEntrySequenceNumber(null);
        }
        
        setPositionNumber(getValue(line, 56, 64));
        setProjectCode(getValue(line, 64, 74));
        setTransactionLedgerEntryDescription(getValue(line, 74, 114));
        
        try {
            setTransactionLedgerEntryAmount(new KualiDecimal(getValue(line, 114, 133).trim()));
            
        }
        catch (NumberFormatException e) {
            //TODO: change the error constants 
            GlobalVariables.getErrorMap().putError("fileUpload", KFSKeyConstants.ERROR_NUMBER_FORMAT_ORIGIN_ENTRY_FROM_TEXT_FILE, new String[] { new Integer(lineNumber).toString(), "Transaction Ledger Entry Amount" });
            throw new LoadException("Invalid Entry Amount");
        }
        
        setTransactionDebitCreditCode(getValue(line, 133, 134));
        
        try {
            setTransactionDate(parseDate(getValue(line, 134, 144), false));
        }
        catch (ParseException e) {
            //TODO: change the error constants 
            GlobalVariables.getErrorMap().putError("fileUpload", KFSKeyConstants.ERROR_NUMBER_FORMAT_ORIGIN_ENTRY_FROM_TEXT_FILE, new String[] { new Integer(lineNumber).toString(), "Transaction Date" });
            throw new LoadException("Invalid Transaction Date");
        }
        
                
        setOrganizationDocumentNumber(getValue(line, 144, 154));
        setOrganizationReferenceId(getValue(line, 154, 162));
        setReferenceFinancialDocumentTypeCode(getValue(line, 162, 166));
        setReferenceFinancialSystemOriginationCode(getValue(line, 166, 168));
        setReferenceFinancialDocumentNumber(getValue(line, 168, 182));
        
        
        
        setTransactionEncumbranceUpdateCode(getValue(line, 192, 193));
        
        try {
            setTransactionPostingDate(parseDate(getValue(line, 193, 203), false));
        }
        catch (ParseException e) {
            //TODO: change the error constants 
            GlobalVariables.getErrorMap().putError("fileUpload", KFSKeyConstants.ERROR_NUMBER_FORMAT_ORIGIN_ENTRY_FROM_TEXT_FILE, new String[] { new Integer(lineNumber).toString(), "Transaction Date" });
            throw new LoadException("Invalid Transaction Date");
        }
        
        try {
            setPayPeriodEndDate(parseDate(getValue(line, 203, 213), false));
        }
        catch (ParseException e) {
            //TODO: change the error constants 
            GlobalVariables.getErrorMap().putError("fileUpload", KFSKeyConstants.ERROR_NUMBER_FORMAT_ORIGIN_ENTRY_FROM_TEXT_FILE, new String[] { new Integer(lineNumber).toString(), "Transaction Date" });
            throw new LoadException("Invalid Transaction Date");
        }
        
        if (getValue(line, 213, 222).equals("")){
            setTransactionTotalHours(new KualiDecimal(0));
        } else {
            try {
                setTransactionTotalHours(new KualiDecimal(getValue(line, 213, 222)));
                
            }
            catch (NumberFormatException e) {
                //TODO: change the error constants 
                GlobalVariables.getErrorMap().putError("fileUpload", KFSKeyConstants.ERROR_NUMBER_FORMAT_ORIGIN_ENTRY_FROM_TEXT_FILE, new String[] { new Integer(lineNumber).toString(), "Transaction Total Hours" });
                throw new LoadException("Invalid Transaction Total Hours");
            }
        }
        
        if (getValue(line, 222, 226).equals("")){
            setPayrollEndDateFiscalYear(new Integer(0));
        } else {
            try {
                setPayrollEndDateFiscalYear(new Integer(getValue(line, 222, 226)));
                
            }
            catch (NumberFormatException e) {
                //TODO: change the error constants 
                GlobalVariables.getErrorMap().putError("fileUpload", KFSKeyConstants.ERROR_NUMBER_FORMAT_ORIGIN_ENTRY_FROM_TEXT_FILE, new String[] { new Integer(lineNumber).toString(), "Payroll End Date Fiscal Year" });
                throw new LoadException("Invalid Payroll EndDate Fiscal Year");
            }
        }
        
        setPayrollEndDateFiscalPeriodCode(getValue(line, 226, 228));
        setEmplid(getValue(line, 228, 239));
        
        if (getValue(line, 239, 242).equals("")){
            setEmployeeRecord(new Integer(0));
        } else {
            try {
                setEmployeeRecord(new Integer(getValue(line, 239, 242)));
                
            }
            catch (NumberFormatException e) {
                //TODO: change the error constants 
                GlobalVariables.getErrorMap().putError("fileUpload", KFSKeyConstants.ERROR_NUMBER_FORMAT_ORIGIN_ENTRY_FROM_TEXT_FILE, new String[] { new Integer(lineNumber).toString(), "Employee Record" });
                throw new LoadException("Invalid Employee Record");
            }
        }
        
        setEarnCode(getValue(line, 242, 245));
        setPayGroup(getValue(line, 245, 248));
        setSalaryAdministrationPlan(getValue(line, 248, 252));
        setGrade(getValue(line, 252, 255));
        setRunIdentifier(getValue(line, 255, 265));
        setLaborLedgerOriginalChartOfAccountsCode(getValue(line, 265, 267));
        setLaborLedgerOriginalAccountNumber(getValue(line, 267, 274));
        setLaborLedgerOriginalSubAccountNumber(getValue(line, 274, 279));
        setLaborLedgerOriginalFinancialObjectCode(getValue(line, 279, 283));
        setLaborLedgerOriginalFinancialSubObjectCode(getValue(line, 283, 286));
        //TODO: is COMPANY HrmsCompany? 
        setHrmsCompany(getValue(line, 286, 289));
        setSetid(getValue(line, 289, 294));
    
       
        
    }
    
    public Object getFieldValue(String fieldName) {
        if ( "universityFiscalYear".equals(fieldName) ) {
            return getUniversityFiscalYear();
        } else if ( "chartOfAccountsCode".equals(fieldName) ) {
            return getChartOfAccountsCode();
        } else if ( "accountNumber".equals(fieldName) ) {
            return getAccountNumber();
        } else if ( "subAccountNumber".equals(fieldName) ) {
            return getSubAccountNumber();
        } else if ( "financialObjectCode".equals(fieldName) ) {
            return getFinancialObjectCode();
        } else if ( "financialSubObjectCode".equals(fieldName) ) {
            return getFinancialSubObjectCode();
        } else if ( "financialBalanceTypeCode".equals(fieldName) ) {
            return getFinancialBalanceTypeCode();
        } else if ( "financialObjectTypeCode".equals(fieldName) ) {
            return getFinancialObjectTypeCode();
        } else if ( "universityFiscalPeriodCode".equals(fieldName) ) {
            return getUniversityFiscalPeriodCode();
        } else if ( "financialDocumentTypeCode".equals(fieldName) ) {
            return getFinancialDocumentTypeCode();
        } else if ( "financialSystemOriginationCode".equals(fieldName) ) {
            return getFinancialSystemOriginationCode();
        } else if ( KFSPropertyConstants.DOCUMENT_NUMBER.equals(fieldName) ) {
            return getDocumentNumber();
        } else if ( "transactionLedgerEntrySequenceNumber".equals(fieldName) ) {
            return getTransactionLedgerEntrySequenceNumber();
        } else if ( "positionNumber".equals(fieldName) ) {
            return getPositionNumber();
        } else if ( "transactionLedgerEntryDescription".equals(fieldName) ) {
            return getTransactionLedgerEntryDescription();
        } else if ( "transactionLedgerEntryAmount".equals(fieldName) ) {
            return getTransactionLedgerEntryAmount();
        } else if ( "transactionDebitCreditCode".equals(fieldName) ) {
            return getTransactionDebitCreditCode();
        } else if ( "transactionDate".equals(fieldName) ) {
            return getTransactionDate();
        } else if ( "organizationDocumentNumber".equals(fieldName) ) {
            return getOrganizationDocumentNumber();
        } else if ( "projectCode".equals(fieldName) ) {
            return getProjectCode();
        } else if ( "organizationReferenceId".equals(fieldName) ) {
            return getOrganizationReferenceId();
        } else if ( "referenceFinancialDocumentTypeCode".equals(fieldName) ) {
            return getReferenceFinancialDocumentTypeCode();
        } else if ( "referenceFinancialSystemOriginationCode".equals(fieldName) ) {
            return getReferenceFinancialSystemOriginationCode();
        } else if ( "referenceFinancialDocumentNumber".equals(fieldName) ) {
            return getReferenceFinancialDocumentNumber();
        } else if ( "financialDocumentReversalDate".equals(fieldName) ) {
            return getFinancialDocumentReversalDate();
        } else if ( "transactionEncumbranceUpdateCode".equals(fieldName) ) {
            return getTransactionEncumbranceUpdateCode();   
        } else if ( "transactionPostingDate".equals(fieldName) ) {
            return getTransactionPostingDate();   
        } else if ( "payPeriodEndDate".equals(fieldName) ) {
            return getPayPeriodEndDate();   
        } else if ( "transactionTotalHours".equals(fieldName) ) {
            return getTransactionTotalHours();   
        } else if ( "payrollEndDateFiscalYear".equals(fieldName) ) {
            return getPayrollEndDateFiscalYear();   
        } else if ( "payrollEndDateFiscalPeriodCode".equals(fieldName) ) {
            return getPayrollEndDateFiscalPeriodCode();   
        } else if ( "emplid".equals(fieldName) ) {
            return getEmplid();   
        } else if ( "employeeRecord".equals(fieldName) ) {
            return getEmployeeRecord();   
        } else if ( "earnCode".equals(fieldName) ) {
            return getEarnCode();   
        } else if ( "payGroup".equals(fieldName) ) {
            return getPayGroup();
        } else if ( "salaryAdministrationPlan".equals(fieldName) ) {
            return getSalaryAdministrationPlan();   
        } else if ( "grade".equals(fieldName) ) {
            return getGrade();   
        } else if ( "runIdentifier".equals(fieldName) ) {
            return getRunIdentifier();   
        } else if ( "laborLedgerOriginalChartOfAccountsCode".equals(fieldName) ) {
            return getLaborLedgerOriginalChartOfAccountsCode();   
        } else if ( "laborLedgerOriginalAccountNumber".equals(fieldName) ) {
            return getLaborLedgerOriginalAccountNumber();   
        } else if ( "laborLedgerOriginalSubAccountNumber".equals(fieldName) ) {
            return getLaborLedgerOriginalSubAccountNumber();   
        } else if ( "laborLedgerOriginalFinancialObjectCode".equals(fieldName) ) {
            return getLaborLedgerOriginalFinancialObjectCode();   
        } else if ( "laborLedgerOriginalFinancialSubObjectCode".equals(fieldName) ) {
            return getLaborLedgerOriginalFinancialObjectCode();  
        } else if ( "hrmsCompany".equals(fieldName) ) {
            return getHrmsCompany();  
        } else if ( "setid".equals(fieldName) ) {
            return getSetid();  
        } else {
            throw new IllegalArgumentException("Invalid Field Name " + fieldName);
}
    }
    
    
    public void setFieldValue(String fieldName,String fieldValue) {
        if ( "universityFiscalYear".equals(fieldName) ) {
            if ( StringUtils.hasText(fieldValue) ) {
                setUniversityFiscalYear(Integer.parseInt(fieldValue));
            } else {
                setUniversityFiscalYear(null);
            }
        } else if ( "chartOfAccountsCode".equals(fieldName) ) {
            setChartOfAccountsCode(fieldValue);
        } else if ( "accountNumber".equals(fieldName) ) {
            setAccountNumber(fieldValue);
        } else if ( "subAccountNumber".equals(fieldName) ) {
            setSubAccountNumber(fieldValue);
        } else if ( "financialObjectCode".equals(fieldName) ) {
            setFinancialObjectCode(fieldValue);
        } else if ( "financialSubObjectCode".equals(fieldName) ) {
            setFinancialSubObjectCode(fieldValue);
        } else if ( "financialBalanceTypeCode".equals(fieldName) ) {
            setFinancialBalanceTypeCode(fieldValue);
        } else if ( "financialObjectTypeCode".equals(fieldName) ) {
            setFinancialObjectTypeCode(fieldValue);
        } else if ( "universityFiscalPeriodCode".equals(fieldName) ) {
            setUniversityFiscalPeriodCode(fieldValue);
        } else if ( "financialDocumentTypeCode".equals(fieldName) ) {
            setFinancialDocumentTypeCode(fieldValue);
        } else if ( "financialSystemOriginationCode".equals(fieldName) ) {
            setFinancialSystemOriginationCode(fieldValue);
        } else if ( KFSPropertyConstants.DOCUMENT_NUMBER.equals(fieldName) ) {
            setDocumentNumber(fieldValue);
        } else if ( "transactionLedgerEntrySequenceNumber".equals(fieldName) ) {
            if ( StringUtils.hasText(fieldValue) ) {
                setTransactionLedgerEntrySequenceNumber(Integer.parseInt(fieldValue));
            } else {
                setTransactionLedgerEntrySequenceNumber(null);
            }
        } else if ( "positionNumber".equals(fieldName) ) {
            setPositionNumber(fieldValue);
        } else if ( "projectCode".equals(fieldName) ) {
            setProjectCode(fieldValue);
        } else if ( "transactionLedgerEntryDescription".equals(fieldName) ) {
            setTransactionLedgerEntryDescription(fieldValue);
        } else if ( "transactionLedgerEntryAmount".equals(fieldName) ) {
            if ( StringUtils.hasText(fieldValue) ) {
                setTransactionLedgerEntryAmount(new KualiDecimal(fieldValue));
            } else {
                clearTransactionLedgerEntryAmount();
            }
        } else if ( "transactionDebitCreditCode".equals(fieldName) ) {
            setTransactionDebitCreditCode(fieldValue);
        } else if ( "transactionDate".equals(fieldName) ) {
            if ( StringUtils.hasText(fieldValue) ) {
                try {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    setTransactionDate(new java.sql.Date( (df.parse(fieldValue)).getTime() ) );
                } catch (ParseException e) {
                    setTransactionDate(null);
                }
            } else {
                setTransactionDate(null);
            }
        } else if ( "organizationDocumentNumber".equals(fieldName) ) {
            setOrganizationDocumentNumber(fieldValue);
        } else if ( "organizationReferenceId".equals(fieldName) ) {
            setOrganizationReferenceId(fieldValue);
        } else if ( "referenceFinancialDocumentTypeCode".equals(fieldName) ) {
            setReferenceFinancialDocumentTypeCode(fieldValue);
        } else if ( "referenceFinancialSystemOriginationCode".equals(fieldName) ) {
            setReferenceFinancialSystemOriginationCode(fieldValue);
        } else if ( "referenceFinancialDocumentNumber".equals(fieldName) ) {
            setReferenceFinancialDocumentNumber(fieldValue);
        } else if ( "financialDocumentReversalDate".equals(fieldName) ) {
            if ( StringUtils.hasText(fieldValue) ) {
                try {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    setFinancialDocumentReversalDate(new java.sql.Date( (df.parse(fieldValue)).getTime() ) );
                } catch (ParseException e) {
                    setFinancialDocumentReversalDate(null);
                }
            } else {
                setFinancialDocumentReversalDate(null);
            }
        } else if ( "transactionEncumbranceUpdateCode".equals(fieldName) ) {
            setTransactionEncumbranceUpdateCode(fieldValue);
        
        } else if ( "transactionPostingDate".equals(fieldName) ) {
            if ( StringUtils.hasText(fieldValue) ) {
                try {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    setTransactionPostingDate(new java.sql.Date( (df.parse(fieldValue)).getTime() ) );
                } catch (ParseException e) {
                    setTransactionPostingDate(null);
                }
            } else {
                setTransactionPostingDate(null);
            }
        } else if ( "payPeriodEndDate".equals(fieldName) ) {
            if ( StringUtils.hasText(fieldValue) ) {
                try {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    setPayPeriodEndDate(new java.sql.Date( (df.parse(fieldValue)).getTime() ) );
                } catch (ParseException e) {
                    setPayPeriodEndDate(null);
                }
            } else {
                setPayPeriodEndDate(null);
            }
        } else if ( "transactionTotalHours".equals(fieldName) ) {
            if ( StringUtils.hasText(fieldValue) ) {
                setTransactionTotalHours(new KualiDecimal(fieldValue));
            } else {
                clearTransactionTotalHours();
            }   
        } else if ( "payrollEndDateFiscalYear".equals(fieldName) ) {
            if ( StringUtils.hasText(fieldValue) ) {
                setPayrollEndDateFiscalYear(Integer.parseInt(fieldValue));
            } else {
                setPayrollEndDateFiscalYear(null);
            }    
        } else if ( "payrollEndDateFiscalPeriodCode".equals(fieldName) ) {
            setPayrollEndDateFiscalPeriodCode(fieldValue);
        } else if ( "emplid".equals(fieldName) ) {
            setEmplid(fieldValue);
        } else if ( "employeeRecord".equals(fieldName) ) {
            if ( StringUtils.hasText(fieldValue) ) {
                setEmployeeRecord(Integer.parseInt(fieldValue));
            } else {
                setEmployeeRecord(null);
            }
        } else if ( "earnCode".equals(fieldName) ) {
            setEarnCode(fieldValue);
        } else if ( "payGroup".equals(fieldName) ) {
            setPayGroup(fieldValue);
        } else if ( "salaryAdministrationPlan".equals(fieldName) ) {
            setSalaryAdministrationPlan(fieldValue);
        } else if ( "grade".equals(fieldName) ) {
            setGrade(fieldValue);
        } else if ( "runIdentifier".equals(fieldName) ) {
            setRunIdentifier(fieldValue);
        } else if ( "laborLedgerOriginalChartOfAccountsCode".equals(fieldName) ) {
            setLaborLedgerOriginalChartOfAccountsCode(fieldValue);
        } else if ( "laborLedgerOriginalAccountNumber".equals(fieldName) ) {
            setLaborLedgerOriginalAccountNumber(fieldValue);
        } else if ( "laborLedgerOriginalSubAccountNumber".equals(fieldName) ) {
            setLaborLedgerOriginalSubAccountNumber(fieldValue);
        } else if ( "laborLedgerOriginalFinancialObjectCode".equals(fieldName) ) {
            setLaborLedgerOriginalFinancialObjectCode(fieldValue);
        } else if ( "laborLedgerOriginalFinancialSubObjectCode".equals(fieldName) ) {
            setLaborLedgerOriginalFinancialSubObjectCode(fieldValue);
        } else if ( "hrmsCompany".equals(fieldName) ) {
            setHrmsCompany(fieldValue);
        } else if ( "setid".equals(fieldName) ) {
            setSetid(fieldValue);
        } else {
            throw new IllegalArgumentException("Invalid Field Name " + fieldName);
        }        
    }
    
    protected String formatDate(Date date) {
        if (date == null) {
            return LaborConstants.getSpaceTransactionDate();
        }
        else {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            return sdf.format(date);
        }
    }
    
}

