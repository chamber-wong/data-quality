// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.datamasking.functions;

/**
 * created by jgonzalez on 19 juin 2015. See GenerateCreditCardFormat.
 *
 */
public class GenerateCreditCardFormatString extends GenerateCreditCardFormat<String> {

    private static final long serialVersionUID = 3682663337119470753L;

    protected boolean keepFormat = ("true").equals(parameters[0]); //$NON-NLS-1$

    @Override
    protected String doGenerateMaskedField(String str) {
        String strWithoutSpaces = super.replaceSpacesInString(str);
        CreditCardType cct_format = null;
        String res;
        if (strWithoutSpaces == null || EMPTY_STRING.equals(strWithoutSpaces)) {
            cct_format = super.chooseCreditCardType();
            res = super.generateCreditCard(cct_format).toString();
        } else {
            try {
                cct_format = super.getCreditCardType(Long.parseLong(replaceSpacesInString(strWithoutSpaces))); // $NON-NLS-1$
            } catch (NumberFormatException e) {
                cct_format = super.chooseCreditCardType();
                res = super.generateCreditCard(cct_format).toString();
            }
            if (cct_format != null) {
                res = super.generateCreditCardFormat(cct_format, strWithoutSpaces, keepFormat);
            } else {
                cct_format = super.chooseCreditCardType();
                res = super.generateCreditCard(cct_format).toString();
            }
        }
        return super.insertSpacesInString(str, res);
    }
}
