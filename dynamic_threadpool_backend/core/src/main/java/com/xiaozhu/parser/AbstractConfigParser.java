package com.xiaozhu.parser;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/22 16:55
 */
public  abstract class AbstractConfigParser implements ConfigParser{
    @Override
    public boolean supports(ConfigFileTypeEnum type) {
        // List.contains
        return getConfigFileTypes().contains(type);
    }
}
