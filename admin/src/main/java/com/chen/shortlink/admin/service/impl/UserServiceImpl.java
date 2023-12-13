package com.chen.shortlink.admin.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.shortlink.admin.common.convention.exception.ClientException;
import com.chen.shortlink.admin.dao.entity.UserDo;
import com.chen.shortlink.admin.dao.mapper.UserMapper;
import com.chen.shortlink.admin.dto.req.UserLonginReqDTO;
import com.chen.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.chen.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.chen.shortlink.admin.dto.resp.UserInfoDTO;
import com.chen.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.chen.shortlink.admin.dto.resp.UserRespDTO;
import com.chen.shortlink.admin.service.UserService;
import com.chen.shortlink.admin.util.BeanUtil;
import com.chen.shortlink.admin.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.chen.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.chen.shortlink.admin.common.enums.UserErrorCodeEnum.*;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDo> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDo> queryWrapper = Wrappers.lambdaQuery(UserDo.class)
                .eq(UserDo::getUsername, username);
        UserDo userDo = baseMapper.selectOne(queryWrapper);
        if(userDo==null){
            throw new ClientException(USER_NULL);
        }
        return BeanUtil.convert(userDo,UserRespDTO.class);
    }

    @Override
    public Boolean hasUserName(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO userRegisterReqDTO) {
        if(hasUserName(userRegisterReqDTO.getUsername())){
            throw new ClientException(USER_NAME_EXIST);
        }
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + userRegisterReqDTO.getUsername());
        if(!lock.tryLock()){
            throw new ClientException(USER_NAME_EXIST);
        }
        try{
            int row=baseMapper.insert(BeanUtil.convert(userRegisterReqDTO,UserDo.class));
            if(row==0){
                throw new ClientException(USER_SAVE_FAIL);
            }
            userRegisterCachePenetrationBloomFilter.add(userRegisterReqDTO.getUsername());
        }catch (DuplicateKeyException e){
            throw new ClientException(USER_NAME_EXIST);
        }
        finally {
            lock.unlock();
        }

    }

    @Override
    public void update(UserUpdateReqDTO userUpdateReqDTO) {
        //TODO 需要用户验证用户信息
        LambdaQueryWrapper<UserDo> queryWrapper = Wrappers.lambdaQuery(UserDo.class)
                .eq(UserDo::getUsername, userUpdateReqDTO.getUsername());
        int row = baseMapper.update(BeanUtil.convert(userUpdateReqDTO, UserDo.class), queryWrapper);
        if(row==0){
            throw new ClientException(USER_UPDATE_FAIL);
        }
    }

    @Override
    public UserLoginRespDTO login(UserLonginReqDTO userLonginReqDTO) {
        if(!hasUserName(userLonginReqDTO.getUsername())){
            throw new ClientException(USER_NAME_EXIST);
        }
        LambdaQueryWrapper<UserDo> queryWrapper = Wrappers.lambdaQuery(UserDo.class)
                .eq(UserDo::getUsername, userLonginReqDTO.getUsername())
                .eq(UserDo::getPassword, userLonginReqDTO.getPassword());
        UserDo userDo = baseMapper.selectOne(queryWrapper);
        if(userDo==null){
            throw new ClientException(USER_PASSWORD_ERROR);
        }
        UserInfoDTO userInfoDTO = BeanUtil.convert(userDo, UserInfoDTO.class);
        String token = JWTUtil.generateAccessToken(userInfoDTO);
        UserLoginRespDTO userLoginRespDTO = new UserLoginRespDTO();
        userLoginRespDTO.setToken(token);
        userLoginRespDTO.setUserInfo(userInfoDTO);
        stringRedisTemplate.opsForValue().set(token, JSONUtil.toJsonStr(userInfoDTO),30, TimeUnit.DAYS);
        return userLoginRespDTO;
    }

    @Override
    public void logout(String token) {
        Boolean hasLogin = stringRedisTemplate.hasKey(token);
        if(hasLogin!=null&&!hasLogin){
            throw new ClientException(USER_TOKEN_ERROR);
        }
        stringRedisTemplate.delete(token);
    }
}
