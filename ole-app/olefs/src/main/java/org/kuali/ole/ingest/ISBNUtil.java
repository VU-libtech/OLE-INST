package org.kuali.ole.ingest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ISBNUtil is for processing the request based on ISBN values
 */
public class ISBNUtil {
    /**
     *  This method returns normalized isbn value based on isbn object.
     * @param isbn
     * @return  value
     * @throws Exception
     */
    public String normalizeISBN(Object isbn) throws Exception {
        String value = (String) isbn;
        if (value != null) {
            String modifiedValue = getModifiedString(value);
            int len = modifiedValue.length();
            if (len == 13) {
                return modifiedValue;
            } else if (len == 10) {
                String regex = "[0-9]{9}[xX]{1}";
                String regexNum = "[0-9]{10}";
                value = getIsbnRegexValue(regexNum, modifiedValue);
                if (value.length() == 0) {
                    value = getIsbnRegexValue(regex, modifiedValue);
                }
                if (value.length() > 0) {
                    value = calculateIsbnValue(value);
                }
            } else {
                throw new Exception("Invalid input" + isbn);
            }
            if (value.length() == 0) {
                throw new Exception("Normalization failed" + isbn);
            }
        }
        return value;
    }

    /**
     *  This method returns isbn value with in the parentheses of parameter value.
     * @param value
     * @return  modifiedValue.
     */
    private String getModifiedString(String value) {
        String modifiedValue = value;
        if (modifiedValue.contains("(") && modifiedValue.contains(")")) {
            String parenthesesValue = modifiedValue
                    .substring(modifiedValue.indexOf("("), modifiedValue.lastIndexOf(")") + 1);
            modifiedValue = modifiedValue.replace(parenthesesValue, "");
        }
        modifiedValue = modifiedValue.replaceAll("[-:\\s]", "");
        return modifiedValue;
    }

    /**
     *  This method gets the isbn value based on matching regular expressions.
     * @param regex
     * @param value
     * @return  matchingValue.
     */
    private String getIsbnRegexValue(String regex, String value) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        String matchingValue = null;
        if (matcher.find()) {
            matchingValue = matcher.group(0);
            matchingValue = value.substring(0, 9);
        } else {
            matchingValue = "";
        }
        return matchingValue;
    }

    /**
     * This method returns isbn number.
     *  This method will calculate the isbn value if the length of the isbn value is 9.
     * @param value
     * @return  num.
     */
    private String calculateIsbnValue(String value) {
        String num = value;
        if (num.length() == 9) {
            num = "978" + num;
            num = getNormalizedIsbn(num);
        }
        return num;
    }

    /**
     *  This method returns normalized isbn value.
     * @param value
     * @return normalizeIsbn.
     */
    private String getNormalizedIsbn(String value) {
        String normalizeIsbn = value;
        int count = 0;
        int multiple = 1;
        for (int i = 0; i < value.length(); i++) {
            Character c = new Character(value.charAt(i));
            int j = Integer.parseInt(c.toString());
            int sum = j * multiple;
            count = count + sum;
            if (i != 0 && i % 2 != 0) {
                multiple = 1;
            } else {
                multiple = 3;
            }
        }
        count = count % 10;
        if (count == 0) {
            count = 0;
        } else {
            count = 10 - count;
        }
        normalizeIsbn = normalizeIsbn + Integer.toString(count);
        return normalizeIsbn;
    }
}
