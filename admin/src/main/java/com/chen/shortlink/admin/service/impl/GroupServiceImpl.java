package com.chen.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.shortlink.admin.common.biz.user.UserContext;
import com.chen.shortlink.admin.dao.entity.GroupDo;
import com.chen.shortlink.admin.dao.mapper.GroupMapper;
import com.chen.shortlink.admin.dto.req.GroupAddReqDTO;
import com.chen.shortlink.admin.dto.req.GroupSortReqDTO;
import com.chen.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.chen.shortlink.admin.dto.resp.GroupRespDTO;
import com.chen.shortlink.admin.service.GroupService;
import com.chen.shortlink.admin.util.BeanUtil;
import com.chen.shortlink.admin.util.RandomCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 短链接分组实现层
 */
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper,GroupDo> implements GroupService {


    @Override
    public void saveGroup(GroupAddReqDTO groupAddReqDTO) {
        String gid =null;
        do{
            gid = RandomCodeGenerator.generateRandomString();
        }while (hasGid(gid));
        GroupDo groupDo = new GroupDo();
        groupDo.setGid(gid);
        groupDo.setName(groupAddReqDTO.getName());
        groupDo.setSortOrder(groupAddReqDTO.getSortOrder());
        groupDo.setUsername(UserContext.getUsername());
        baseMapper.insert(groupDo);
    }

    @Override
    public List<GroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDo> queryWrapper = Wrappers.lambdaQuery(GroupDo.class)
                .eq(GroupDo::getDelFlag, 0)
                .eq(GroupDo::getUsername, UserContext.getUsername())
                .orderByDesc(GroupDo::getSortOrder, GroupDo::getUpdateTime);
        List<GroupDo> groupDoList = baseMapper.selectList(queryWrapper);
        return BeanUtil.convert(groupDoList, GroupRespDTO.class);
    }

    @Override
    public void updateGroup(GroupUpdateReqDTO groupUpdateReqDTO) {
        LambdaQueryWrapper<GroupDo> wrapper = Wrappers.lambdaQuery(GroupDo.class)
                .eq(GroupDo::getUsername, UserContext.getUsername())
                .eq(GroupDo::getGid, groupUpdateReqDTO.getGid());
        GroupDo groupDo = BeanUtil.convert(groupUpdateReqDTO, GroupDo.class);
        baseMapper.update(groupDo,wrapper);
    }

    @Override
    public void deleteGroup(String gid) {
        LambdaQueryWrapper<GroupDo> queryWrapper = Wrappers.lambdaQuery(GroupDo.class)
                .eq(GroupDo::getUsername, UserContext.getUsername())
                .eq(GroupDo::getGid, gid)
                .eq(GroupDo::getDelFlag, 0);
        GroupDo groupDo = new GroupDo();
        groupDo.setDelFlag(1);
        baseMapper.update(groupDo,queryWrapper);
    }

    @Override
    public void sortGroup(List<GroupSortReqDTO> groupSortReqDTOList) {
        groupSortReqDTOList.forEach(item->{
            GroupDo groupDo = new GroupDo();
            groupDo.setSortOrder(item.getSortOrder());
            LambdaQueryWrapper<GroupDo> queryWrapper = Wrappers.lambdaQuery(GroupDo.class)
                    .eq(GroupDo::getGid, item.getGid())
                    .eq(GroupDo::getUsername, UserContext.getUsername())
                    .eq(GroupDo::getDelFlag, 0);
            baseMapper.update(groupDo,queryWrapper);
        });
    }

    private boolean hasGid(String gid){
        LambdaQueryWrapper<GroupDo> queryWrapper = Wrappers.lambdaQuery(GroupDo.class)
                .eq(GroupDo::getGid, gid)
                .eq(GroupDo::getUsername, UserContext.getUsername());
        return baseMapper.selectCount(queryWrapper)>0;
    }


}
