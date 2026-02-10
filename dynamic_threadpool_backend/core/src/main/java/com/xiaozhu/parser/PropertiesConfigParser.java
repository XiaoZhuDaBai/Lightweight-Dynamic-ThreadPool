package com.xiaozhu.parser;

import cn.hutool.core.collection.CollectionUtil;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/22 16:56
 */
public class PropertiesConfigParser extends AbstractConfigParser{

    /**
     * 解析配置文件
     * @param content 配置文件内容字符串
     * @return
     * @throws IOException
     */
    @Override
    public Map<Object, Object> doParse(String content) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(content));
        return properties;
    }

    @Override
    public List<ConfigFileTypeEnum> getConfigFileTypes() {
        return CollectionUtil.newArrayList(ConfigFileTypeEnum.PROPERTIES);
    }
}
