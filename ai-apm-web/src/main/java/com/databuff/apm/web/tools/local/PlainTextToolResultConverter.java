package com.databuff.apm.web.tools.local;

import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.tool.DefaultToolResultConverter;
import io.agentscope.core.tool.ToolResultConverter;

import java.lang.reflect.Type;

/**
 * Keeps JSON strings returned by local DataTools as tool result text.
 */
public class PlainTextToolResultConverter implements ToolResultConverter {

    private final DefaultToolResultConverter delegate = new DefaultToolResultConverter();

    @Override
    public ToolResultBlock convert(Object result, Type returnType) {
        if (result instanceof String text) {
            return ToolResultBlock.text(text);
        }
        return delegate.convert(result, returnType);
    }
}
