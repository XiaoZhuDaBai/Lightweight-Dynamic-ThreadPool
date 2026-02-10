package com.xiaozhu.parser;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 配置解析器接口
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/22 16:55
 */
public interface ConfigParser {

    /**
     * 判断是否支持指定类型的配置文件解析
     *
     * @param type 配置文件类型枚举
     * @return 是否支持该类型
     */
    boolean supports(ConfigFileTypeEnum type);

    /**
     * 解析配置内容字符串为键值对 Map
     *
     * @param content 配置文件内容字符串
     * @return 解析后的键值对 Map
     * @throws IOException 解析失败时抛出
     */
    Map<Object, Object> doParse(String content) throws IOException;

    /**
     * 获取当前解析器支持的配置文件类型列表
     *
     * @return 支持的配置文件类型集合
     */
    List<ConfigFileTypeEnum> getConfigFileTypes();
}
