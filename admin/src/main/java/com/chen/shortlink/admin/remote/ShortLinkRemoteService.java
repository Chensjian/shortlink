package com.chen.shortlink.admin.remote;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chen.shortlink.admin.common.convention.result.Result;
import com.chen.shortlink.admin.dto.req.RecycleBinSaveReqDTO;
import com.chen.shortlink.admin.remote.dto.req.*;
import com.chen.shortlink.admin.remote.dto.resp.ShortLinkAddRespDTO;
import com.chen.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.chen.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接中台远程调用服务
 */
public interface ShortLinkRemoteService {
    default  Result<ShortLinkAddRespDTO> addShortLink(ShortLinkAddReqDTO shortLinkAddReqDTO){
        String resultJson = HttpUtil.post("http://127.0.0.1:8002/api/short-link/v1/create", JSON.toJSONString(shortLinkAddReqDTO));
        return JSON.parseObject(resultJson, new TypeReference<>() {
        });
    }

    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO){
        Map<String,Object> requestMap=new HashMap<>();
        requestMap.put("gid",shortLinkPageReqDTO.getGid());
        requestMap.put("current",shortLinkPageReqDTO.getCurrent());
        requestMap.put("size",shortLinkPageReqDTO.getSize());
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8002/api/short-link/v1/page", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    default Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(List<String> requestParam){
        Map<String,Object> requestMap=new HashMap<>();
        requestMap.put("requestParam",requestParam);
        String resultJson = HttpUtil.get("http://127.0.0.1:8002/api/short-link/v1/count", requestMap);
        return JSON.parseObject(resultJson, new TypeReference<>(){});
    }

    /**
     * 修改短链接
     * @param shortLinkAddReqDTO
     */
    default void updateShortLink(ShortLinkUpdateReqDTO shortLinkAddReqDTO){
        HttpUtil.post("http://127.0.0.1:8002/api/short-link/v1/update",JSON.toJSONString(shortLinkAddReqDTO));
    }


    /**
     * 根据 URL 获取title
     * @param url
     */
    default Result getTitleByUrl(String url){
        String resultJson = HttpUtil.get("http://127.0.0.1:8002/api/short-link/v1/title?url=" + url);
        return JSON.parseObject(resultJson, new TypeReference<>(){});
    }

    /**
     * 短链接添加到回收站
     * @param recycleBinSaveReqDTO
     */
    default void saveRecycleBin(RecycleBinSaveReqDTO recycleBinSaveReqDTO) {
        HttpUtil.post("http://127.0.0.1:8002/api/short-link/v1/recycle-bin/save",JSON.toJSONString(recycleBinSaveReqDTO));
    }

    /**
     * 分页查询回收站短链接
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO shortLinkRecycleBinPageReqDTO){
        Map<String,Object> requestMap=new HashMap<>();
        requestMap.put("gidList",shortLinkRecycleBinPageReqDTO.getGidList());
        requestMap.put("current",shortLinkRecycleBinPageReqDTO.getCurrent());
        requestMap.put("size",shortLinkRecycleBinPageReqDTO.getSize());
        String resultJson = HttpUtil.get("http://127.0.0.1:8002/api/short-link/v1/recycle-bin/page", requestMap);
        return JSON.parseObject(resultJson, new TypeReference<>(){});
    }

    /**
     * 移除短链接
     */
    default void removeRecycleBin(RecycleBinRemoveReqDTO recycleBinRemoveReqDTO){
        HttpUtil.post("http://127.0.0.1:8002/api/short-link/v1/recycle-bin/remove",JSON.toJSONString(recycleBinRemoveReqDTO));
    }

    /**
     * 恢复短链接
     */
    default void recoverRecycleBin(RecycleBinRecoverReqDTO recycleBinRecoverReqDTO){
        HttpUtil.post("http://127.0.0.1:8002/api/short-link/v1/recycle-bin/recover",JSON.toJSONString(recycleBinRecoverReqDTO));
    }


}
