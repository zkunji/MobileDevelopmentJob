package com.util;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;

/**
 * @author Zhangkunji
 * @date 2024/12/12
 * @Description
 */
public class TokenParseUtil {
    public static String getUID() {
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        String UID = (String) tokenInfo.loginId;
        if (!UID.isEmpty()) {
            return UID;
        }
        return "Empty";
    }
}
