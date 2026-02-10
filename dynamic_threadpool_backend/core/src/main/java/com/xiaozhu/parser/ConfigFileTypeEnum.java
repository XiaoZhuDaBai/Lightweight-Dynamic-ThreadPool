package com.xiaozhu.parser;

import lombok.Getter;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/18 16:11
 */
@Getter
public enum ConfigFileTypeEnum {

    /**
     * PROPERTIES
     */
    PROPERTIES("properties"),

    /**
     * YML
     */
    YML("yml"),

    /**
     * YAML
     */
    YAML("yaml");

    private final String value;

    ConfigFileTypeEnum(String value) {
        this.value = value;
    }

    public static ConfigFileTypeEnum of(String value) {
        for (ConfigFileTypeEnum typeEnum : ConfigFileTypeEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        return PROPERTIES;
    }
}