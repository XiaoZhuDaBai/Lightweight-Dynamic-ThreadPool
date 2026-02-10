package com.xiaozhu.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/22 16:58
 */
public class ConfigParserHandler {
    private static final List<ConfigParser> PARSERS = new ArrayList<>();

    private ConfigParserHandler() {
        PARSERS.add(new YamlConfigParser());
        PARSERS.add(new PropertiesConfigParser());
    }

    public Map<Object, Object> parseConfig(String content, ConfigFileTypeEnum type) throws IOException {
        for (ConfigParser parser : PARSERS) {
            // 根据配置文件类型执行相应的解析逻辑
            if (parser.supports(type)) {
                return parser.doParse(content);
            }
        }
        return Collections.emptyMap();
    }

    /**
     * 单例模式获取配置解析器
     * @return
     */
    public static ConfigParserHandler getInstance() {
        return ConfigParserHandlerHolder.INSTANCE;
    }
    private static class ConfigParserHandlerHolder {
        private static final ConfigParserHandler INSTANCE = new ConfigParserHandler();
    }
}
