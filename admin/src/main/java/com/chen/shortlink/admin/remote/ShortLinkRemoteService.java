package com.chen.shortlink.admin.remote;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chen.shortlink.admin.common.convention.result.Result;
import com.chen.shortlink.admin.remote.req.ShortLinkPageReqDTO;
import com.chen.shortlink.admin.remote.resp.ShortLinkPageRespDTO;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 短链接中台远程调用服务
 */
public interface ShortLinkRemoteService {

    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO){
        Map<String,Object> requestMap=new HashMap<>();
        requestMap.put("gid",shortLinkPageReqDTO.getGid());
        requestMap.put("current",shortLinkPageReqDTO.getCurrent());
        requestMap.put("size",shortLinkPageReqDTO.getSize());
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8002/api/short-link/v1/page", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }
}
