package com.chen.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.shortlink.admin.dao.entity.GroupDo;
import com.chen.shortlink.admin.dto.req.GroupAddReqDTO;
import com.chen.shortlink.admin.dto.req.GroupSortReqDTO;
import com.chen.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.chen.shortlink.admin.dto.resp.GroupRespDTO;

import java.util.List;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDo> {

    /**
     * 新增短链接分组
     * @param groupAddReqDTO
     */
    void saveGroup(GroupAddReqDTO groupAddReqDTO);

    /**
     * 新增短链接分组
     * @param groupAddReqDTO
     */
    void saveGroup(String username,GroupAddReqDTO groupAddReqDTO);

    /**
     * 查询分组列表
     * @return
     */
    List<GroupRespDTO> listGroup();

    /**
     * 修改短链接分组
     * @param groupUpdateReqDTO
     */
    void updateGroup(GroupUpdateReqDTO groupUpdateReqDTO);

    /**
     * 删除短链接分组
     * @param gid
     */
    void deleteGroup(String gid);

    /**
     * 分组排序
     * @param groupSortReqDTOList
     */
    void sortGroup(List<GroupSortReqDTO> groupSortReqDTOList);
}
