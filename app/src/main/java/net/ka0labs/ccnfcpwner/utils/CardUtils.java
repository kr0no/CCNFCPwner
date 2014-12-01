package net.ka0labs.ccnfcpwner.utils;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;

import org.apache.commons.lang3.StringUtils;

public class CardUtils {
    public static String formatCardNumber(final String pCardNumber, final EmvCardScheme pType) {
        String ret = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(pCardNumber)) {
            // format amex
            if (pType != null && pType == EmvCardScheme.AMERICAN_EXPRESS) {
                ret = StringUtils.deleteWhitespace(pCardNumber).replaceFirst("\\d{4}", "$0 ").replaceFirst("\\d{6}", "$0 ")
                        .replaceFirst("\\d{5}", "$0").trim();
            } else {
                ret = StringUtils.deleteWhitespace(pCardNumber).replaceAll("\\d{4}", "$0 ").trim();
            }
        } else {
            ret = "0000 0000 0000 0000";
        }
        return ret;
    }

    public static String formatAid(final String pAid) {
        String ret = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(pAid)) {
            ret = StringUtils.deleteWhitespace(pAid).replaceAll("[A-F0-9]{2}", "$0 ").trim();
        }
        return ret;
    }

    private CardUtils() { }

}
