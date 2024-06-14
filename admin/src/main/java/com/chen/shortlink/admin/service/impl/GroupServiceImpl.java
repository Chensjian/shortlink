package com.chen.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.shortlink.admin.common.biz.user.UserContext;
import com.chen.shortlink.admin.common.convention.exception.ClientException;
import com.chen.shortlink.admin.common.convention.result.Result;
import com.chen.shortlink.admin.dao.entity.GroupDo;
import com.chen.shortlink.admin.dao.mapper.GroupMapper;
import com.chen.shortlink.admin.dto.req.GroupAddReqDTO;
import com.chen.shortlink.admin.dto.req.GroupSortReqDTO;
import com.chen.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.chen.shortlink.admin.dto.resp.GroupRespDTO;
import com.chen.shortlink.admin.remote.ShortLinkRemoteService;
import com.chen.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.chen.shortlink.admin.service.GroupService;
import com.chen.shortlink.admin.util.BeanUtil;
import com.chen.shortlink.admin.util.RandomCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.chen.shortlink.admin.common.constant.RedisCacheConstant.LOCK_GROUP_CREATE_KEY;

/**
 * 短链接分组实现层
 */
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper,GroupDo> implements GroupService {

    private final ShortLinkRemoteService shortLinkRemoteService;
    private final RedissonClient redissonClient;

    @Value("${short-link.group.max-group-num}")
    private Long maxGroupNum;

    @Override
    public void saveGroup(GroupAddReqDTO groupAddReqDTO) {
        saveGroup(UserContext.getUsername(),groupAddReqDTO);
    }

    @Override
    public void saveGroup(String username, GroupAddReqDTO groupAddReqDTO) {
        RLock lock = redissonClient.getLock(String.format(LOCK_GROUP_CREATE_KEY,username));
        lock.lock();
        try {
            LambdaQueryWrapper<GroupDo> queryWrapper = Wrappers.lambdaQuery(GroupDo.class)
                    .eq(GroupDo::getUsername, username)
                    .eq(GroupDo::getDelFlag,0);
            Long count = baseMapper.selectCount(queryWrapper);
            if(count.equals(maxGroupNum)){
                throw new ClientException(String.format("已超出最大分组数：%d", maxGroupNum));
            }
            String gid =null;
            do{
                gid = RandomCodeGenerator.generateRandomString();
            }while (hasGid(gid,username));
            GroupDo groupDo = new GroupDo();
            groupDo.setGid(gid);
            groupDo.setName(groupAddReqDTO.getName());
            groupDo.setSortOrder(groupAddReqDTO.getSortOrder());
            groupDo.setUsername(username);
            groupDo.setSortOrder(1);
            baseMapper.insert(groupDo);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<GroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDo> queryWrapper = Wrappers.lambdaQuery(GroupDo.class)
                .eq(GroupDo::getDelFlag, 0)
                .eq(GroupDo::getUsername, UserContext.getUsername())
                .orderByDesc(GroupDo::getSortOrder, GroupDo::getUpdateTime);
        List<GroupDo> groupDoList = baseMapper.selectList(queryWrapper);
        Result<List<ShortLinkGroupCountQueryRespDTO>> listResult = shortLinkRemoteService.listGroupShortLinkCount(groupDoList.stream().map(GroupDo::getGid).collect(Collectors.toList()));
        List<GroupRespDTO> groupRespDTOS = BeanUtil.convert(groupDoList, GroupRespDTO.class);
        groupRespDTOS.forEach(each->{
            Optional<ShortLinkGroupCountQueryRespDTO> first=listResult.getData().stream()
                    .filter(item->Objects.equals(item.getGid(),each.getGid()))
                    .findFirst();
            each.setShortLinkCount(0);
            first.ifPresent(item->each.setShortLinkCount(first.get().getShortLinkCount()));
        });

        return groupRespDTOS;
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

    private boolean hasGid(String gid,String username){
        LambdaQueryWrapper<GroupDo> queryWrapper = Wrappers.lambdaQuery(GroupDo.class)
                .eq(GroupDo::getGid, gid)
                .eq(GroupDo::getUsername, username);
        return baseMapper.selectCount(queryWrapper)>0;
    }


}
