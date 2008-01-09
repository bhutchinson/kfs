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
package org.kuali.module.effort.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.core.util.KualiDecimal;
import org.kuali.module.cg.bo.AwardAccount;
import org.kuali.module.chart.bo.Account;
import org.kuali.module.chart.bo.Org;
import org.kuali.module.chart.bo.SubFundGroup;
import org.kuali.module.effort.EffortConstants;
import org.kuali.module.effort.EffortKeyConstants;
import org.kuali.module.effort.util.LedgerBalanceConsolidationHelper;
import org.kuali.module.effort.util.LedgerBalanceWithMessage;
import org.kuali.module.gl.util.Message;
import org.kuali.module.labor.bo.LedgerBalance;
import org.kuali.module.labor.util.MessageBuilder;

/**
 * The validator provides a set of facilities to determine whether the given ledger balances meet the specified requirements. As a
 * pattern, null would be returned if the requirements are met; otherwise, return an error message.
 */
public class LedgerBalanceFieldValidator {

    /**
     * check if the given ledger balance has an account qualified for effort reporting
     * 
     * @param ledgerBalance the given ledger balance
     * @return null if the given ledger balance has an account qualified for effort reporting; otherwise, a message
     */
    public static Message hasValidAccount(LedgerBalance ledgerBalance) {
        if (ledgerBalance.getAccount() == null) {
            String account = new StringBuilder(ledgerBalance.getChartOfAccountsCode()).append(EffortConstants.VALUE_SEPARATOR).append(ledgerBalance.getAccountNumber()).toString();
            return MessageBuilder.buildErrorMessage(EffortKeyConstants.ERROR_ACCOUNT_NUMBER_NOT_FOUND, account);
        }
        return null;
    }

    /**
     * check if the account of the given ledger balance has higher education function
     * 
     * @param ledgerBalance the given ledger balance
     * @return null if the account of the given ledger balance has higher education function; otherwise, a message
     */
    public static Message hasHigherEdFunction(LedgerBalance ledgerBalance) {
        Account account = ledgerBalance.getAccount();
        if (account.getFinancialHigherEdFunction() == null) {
            String accountNumber = ledgerBalance.getAccountNumber();
            return MessageBuilder.buildErrorMessage(EffortKeyConstants.ERROR_HIGHER_EDUCATION_CODE_NOT_FOUND, accountNumber);
        }
        return null;
    }

    /**
     * detetermine if the fund group code associated with the given ledger balance is in the given fund group codes
     * 
     * @param ledgerBalance the given ledger balance
     * @param fundGroupCodes the given fund group codes
     * @return null if the fund group code associated with the given ledger balance is in the given fund group codes; otherwise, a message
     */
    public static Message isInFundGroups(LedgerBalance ledgerBalance, List<String> fundGroupCodes) {
        SubFundGroup subFundGroup = getSubFundGroup(ledgerBalance);

        if (subFundGroup == null || !fundGroupCodes.contains(subFundGroup.getFundGroupCode())) {
            return MessageBuilder.buildErrorMessage(EffortKeyConstants.ERROR_FUND_GROUP_NOT_FOUND, subFundGroup.getFundGroupCode());
        }
        return null;
    }

    /**
     * detetermine if the sub fund group code associated with the given ledger balance is in the given sub fund group codes
     * 
     * @param ledgerBalance the given ledger balance
     * @param subFundGroupCodes the given sub fund group codes
     * @return null if the sub fund group code associated with the given ledger balance is in the given sub fund group codes;
     *         otherwise, an error message
     */
    public static Message isInSubFundGroups(LedgerBalance ledgerBalance, List<String> subFundGroupCodes) {
        SubFundGroup subFundGroup = getSubFundGroup(ledgerBalance);

        if (subFundGroup == null || !subFundGroupCodes.contains(subFundGroup.getSubFundGroupCode())) {
            return MessageBuilder.buildErrorMessage(EffortKeyConstants.ERROR_FUND_GROUP_NOT_FOUND, subFundGroup.getSubFundGroupCode());
        }
        return null;
    }

    /**
     * determine if the total amount within the specified periods of the given ledger balance is ZERO
     * 
     * @param ledgerBalance the given ledger balance
     * @param reportPeriods the specified periods
     * @return null the total amount within the specified periods of the given ledger balance is NOT ZERO; otherwise, a message
     *         message
     */
    public static Message isNonZeroAmountBalanceWithinReportPeriod(LedgerBalance ledgerBalance, Map<Integer, Set<String>> reportPeriods) {
        KualiDecimal totalAmount = LedgerBalanceConsolidationHelper.calculateTotalAmountWithinReportPeriod(ledgerBalance, reportPeriods);

        if (totalAmount.isZero()) {
            return MessageBuilder.buildErrorMessage(EffortKeyConstants.ERROR_ZERO_PAYROLL_AMOUNT, Message.TYPE_FATAL);
        }
        return null;
    }

    /**
     * determine if the total amount within the specified periods of the given ledger balances is positive
     * 
     * @param ledgerBalance the given ledger balance
     * @param reportPeriods the specified periods
     * @return null the total amount within the specified periods of the given ledger balance is positive; otherwise, a message
     *         message
     */
    public static Message isTotalAmountPositive(Collection<LedgerBalance> ledgerBalances, Map<Integer, Set<String>> reportPeriods) {
        KualiDecimal totalAmount = LedgerBalanceConsolidationHelper.calculateTotalAmountWithinReportPeriod(ledgerBalances, reportPeriods);

        if (!totalAmount.isPositive()) {
            return MessageBuilder.buildErrorMessage(EffortKeyConstants.ERROR_NONPOSITIVE_PAYROLL_AMOUNT, Message.TYPE_FATAL);
        }
        return null;
    }

    /**
     * check if there is at least one account of the given ledger balances that has a fund group code or subfund group code that is
     * in the specifed group codes. If fundGroupDenotesCGIndictor is ture, only examine the fund group code associated with the
     * ledger balances; otherwise, the sub fund group code.
     * 
     * @param ledgerBalances the given ledger balances
     * @param fundGroupDenotesCGIndictor indicate whether the fund group code or sub fund group code would be examined
     * @param fundGroupCodes the specified fun group codes
     * @return null if one of the group codes associated with the ledger balances is in the specified codes; otherwise, a message
     *         message
     */
    public static Message hasGrantAccount(Collection<LedgerBalance> ledgerBalances, boolean fundGroupDenotesCGIndictor, List<String> fundGroupCodes) {
        for (LedgerBalance balance : ledgerBalances) {
            SubFundGroup subFundGroup = getSubFundGroup(balance);
            if (subFundGroup == null) {
                continue;
            }

            if (fundGroupDenotesCGIndictor && fundGroupCodes.contains(subFundGroup.getFundGroupCode())) {
                return null;
            }

            if (!fundGroupDenotesCGIndictor && fundGroupCodes.contains(subFundGroup.getSubFundGroupCode())) {
                return null;
            }
        }

        return MessageBuilder.buildErrorMessage(EffortKeyConstants.ERROR_FUND_GROUP_NOT_FOUND, Message.TYPE_FATAL);
    }

    /**
     * determine whether there is at least one account of the given ledger balances that is funded by a federal grant. The award
     * associated with the account must be one of the given federal agency types or have an enabled federal pass through flag.
     * 
     * @param the given labor ledger balances
     * @param federalAgencyTypeCodes the given federal agency type codes
     * @return null if there is at least one account with federal funding; otherwise, a message
     */
    public static Message hasFederalFunds(Collection<LedgerBalance> ledgerBalances, List<String> federalAgencyTypeCodes) {
        for (LedgerBalance balance : ledgerBalances) {
            List<AwardAccount> awardAccountList = balance.getAccount().getAwards();

            for (AwardAccount awardAccount : awardAccountList) {
                String agencyTypeCode = awardAccount.getAward().getAgency().getAgencyTypeCode();
                if (federalAgencyTypeCodes.contains(agencyTypeCode)) {
                    return null;
                }

                if (awardAccount.getAward().getFederalPassThroughIndicator()) {
                    return null;
                }
            }
        }
        return MessageBuilder.buildErrorMessage(EffortKeyConstants.ERROR_NOT_PAID_BY_FEDERAL_FUNDS, Message.TYPE_FATAL);
    }
    
    /**
     * determine if the given ledger balances have the accounts that belong to multiple organizations
     * 
     * @param ledgerBalance the given ledger balance
     * @return null if the given ledger balances have the accounts that belong to a single organization; otherwise, a message
     */
    public static LedgerBalanceWithMessage hasMultipleOrganizations(Collection<LedgerBalance> ledgerBalances) {
        Org tempOrganization = null;
        
        for (LedgerBalance balance : ledgerBalances) {
            Org organization = balance.getAccount().getOrganization();
            
            if(!organization.equals(tempOrganization)) {
                MessageBuilder.buildErrorMessage(EffortKeyConstants.ERROR_MULTIPLE_ORGANIZATIONS_FOUND, Message.TYPE_FATAL);
            }
            
            tempOrganization = organization;
        }
        return null;
    }

    /**
     * get the sub fund group associated with the given ledger balance
     * 
     * @param ledgerBalance the given ledger balance
     * @return the sub fund group associated with the given ledger balance
     */
    private static SubFundGroup getSubFundGroup(LedgerBalance ledgerBalance) {
        SubFundGroup subFundGroup = null;
        try {
            subFundGroup = ledgerBalance.getAccount().getSubFundGroup();
        }
        catch (NullPointerException npe) {
            return null;
        }
        return subFundGroup;
    }
}
