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
package org.kuali.kfs.sec.service.impl;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sec.SecConstants;
import org.kuali.kfs.sec.service.AccessPermissionEvaluator;
import org.kuali.rice.kim.api.identity.Person;


/**
 * @see org.kuali.kfs.sec.service.AccessPermissionEvaluator
 */
public class AccessPermissionEvaluatorImpl implements AccessPermissionEvaluator {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AccessPermissionEvaluatorImpl.class);

    protected String constraintCode;
    protected String operatorCode;
    protected String propertyValue;
    protected Map<String, Object> otherKeyFieldValues;
    protected Person person;
    protected String[] matchValues;
    protected boolean performEqualMatch;
    protected boolean performLessThanMatch;
    protected boolean performGreaterThanMatch;
    protected boolean allowConstraint;
    protected boolean notOperator;

    public AccessPermissionEvaluatorImpl() {
        super();

        performEqualMatch = false;
        performLessThanMatch = false;
        performGreaterThanMatch = false;
        allowConstraint = false;
        notOperator = false;
    }

    /**
     * @see org.kuali.kfs.sec.service.AccessPermissionEvaluator#valueIsAllowed(java.lang.String)
     */
    public boolean valueIsAllowed(String value) {
        boolean allowed = false;

        initializeAfterPropsSet();

        boolean match = false;
        for (int i = 0; i < matchValues.length; i++) {
            String matchValue = matchValues[i];

            if (isMatch(matchValue, value)) {
                match = true;
                break;
            }
        }

        if ((allowConstraint && notOperator) || (!allowConstraint && !notOperator)) {
            allowed = !match;
        }
        else {
            allowed = match;
        }

        return allowed;
    }

    /**
     * Determines whether two values match performing an equal, greater than, or less than check and also considering wildcards
     * 
     * @param matchValue String value to match, can contain the * wildcard
     * @param value String value to compare
     * @return boolean true if values match, false otherwise
     */
    protected boolean isMatch(String matchValue, String value) {
        boolean match = false;

        boolean performWildcardMatch = false;
        if (StringUtils.contains(matchValue, SecConstants.SecurityValueSpecialCharacters.WILDCARD_CHARACTER)) {
            matchValue = StringUtils.remove(matchValue, SecConstants.SecurityValueSpecialCharacters.WILDCARD_CHARACTER);
            performWildcardMatch = true;
        }

        if (performEqualMatch) {
            if (performWildcardMatch) {
                match = value.startsWith(matchValue);
            }
            else {
                match = value.equals(matchValue);
            }
        }

        if (!match && performLessThanMatch) {
            match = value.compareTo(matchValue) < 0;
        }

        if (!match && performGreaterThanMatch) {
            match = value.compareTo(matchValue) > 0;
        }

        return match;
    }

    /**
     * Hooks for permission evaluators to do additional setup after properties have been set
     */
    protected void initializeAfterPropsSet() {
        if (StringUtils.contains(constraintCode, SecConstants.SecurityConstraintCodes.ALLOWED)) {
            allowConstraint = true;
        }

        if (SecConstants.SecurityDefinitionOperatorCodes.EQUAL.equals(operatorCode) || SecConstants.SecurityDefinitionOperatorCodes.NOT_EQUAL.equals(operatorCode) || SecConstants.SecurityDefinitionOperatorCodes.LESS_THAN_EQUAL.equals(operatorCode)
                || SecConstants.SecurityDefinitionOperatorCodes.GREATER_THAN_EQUAL.equals(operatorCode)) {
            performEqualMatch = true;
        }

        if (SecConstants.SecurityDefinitionOperatorCodes.LESS_THAN.equals(operatorCode) || SecConstants.SecurityDefinitionOperatorCodes.LESS_THAN_EQUAL.equals(operatorCode)) {
            performLessThanMatch = true;
        }

        if (SecConstants.SecurityDefinitionOperatorCodes.GREATER_THAN.equals(operatorCode) || SecConstants.SecurityDefinitionOperatorCodes.GREATER_THAN_EQUAL.equals(operatorCode)) {
            performGreaterThanMatch = true;
        }
        
        if (SecConstants.SecurityDefinitionOperatorCodes.NOT_EQUAL.equals(operatorCode)) {
            notOperator = true;
        }

        setMatchValues();
    }

    /**
     * Sets the values to match on based on given value and other properties
     */
    protected void setMatchValues() {
        if (StringUtils.contains(propertyValue, SecConstants.SecurityValueSpecialCharacters.MULTI_VALUE_SEPERATION_CHARACTER)) {
            matchValues = StringUtils.split(propertyValue, SecConstants.SecurityValueSpecialCharacters.MULTI_VALUE_SEPERATION_CHARACTER);
        }
        else {
            matchValues = new String[1];
            matchValues[0] = propertyValue;
        }
    }

    /**
     * @see org.kuali.kfs.sec.service.AccessPermissionEvaluator#setConstraintCode(java.lang.String)
     */
    public void setConstraintCode(String constraintCode) {
        this.constraintCode = constraintCode;
    }

    /**
     * @see org.kuali.kfs.sec.service.AccessPermissionEvaluator#setOperatorCode(java.lang.String)
     */
    public void setOperatorCode(String operatorCode) {
        this.operatorCode = operatorCode;
    }

    /**
     * @see org.kuali.kfs.sec.service.AccessPermissionEvaluator#setPropertyValue(java.lang.String)
     */
    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    /**
     * @see org.kuali.kfs.sec.service.AccessPermissionEvaluator#setOtherKeyFieldValueMap(java.util.Map)
     */
    public void setOtherKeyFieldValueMap(Map<String, Object> otherKeyFieldValues) {
        this.otherKeyFieldValues = otherKeyFieldValues;
    }

    /**
     * @see org.kuali.kfs.sec.service.AccessPermissionEvaluator#setPerson(org.kuali.rice.kim.api.identity.Person)
     */
    public void setPerson(Person person) {
        this.person = person;
    }

}
