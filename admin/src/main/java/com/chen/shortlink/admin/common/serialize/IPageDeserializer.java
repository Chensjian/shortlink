package com.chen.shortlink.admin.common.serialize;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class IPageDeserializer extends StdDeserializer<IPage> {
    public IPageDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Page deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String s  = node.toString();
        ObjectMapper om = new ObjectMapper();
        Page page = om.readValue(s,Page.class);
        return page;
    }
}

