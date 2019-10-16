package com.leyou.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.constants.MQConstants;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.RegexUtils;
import com.leyou.user.DTO.UserDTO;
import com.leyou.user.entity.TbUser;
import com.leyou.user.mapper.TbUserMapper;
import com.leyou.user.service.TbUserService;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author HM
 * @since 2019-08-27
 */
@Service
public class TbUserServiceImpl extends ServiceImpl<TbUserMapper, TbUser> implements TbUserService {

    /**
     * type -1  用户名  -2 手机号
     *
     * @param data
     * @param type
     * @return true 可用 false 不可用
     */
    @Override
    public Boolean checkUser(String data, Integer type) {
        QueryWrapper<TbUser> queryWrapper = new QueryWrapper<>();
//        if(type == 1){
//            queryWrapper.lambda().eq(TbUser::getUsername,data);
//        }else{
//            queryWrapper.lambda().eq(TbUser::getPhone,data);
//        }
        switch (type){
            case 1:
                queryWrapper.lambda().eq(TbUser::getUsername,data);
                break;
            case 2:
                queryWrapper.lambda().eq(TbUser::getPhone,data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        int count = this.count(queryWrapper);
        return count==0;
    }

    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * 发送随机验证码
     * @param phone
     */
    private static final String KEY_PRE = "ly:user:code:phone:";
    @Override
    public void sendCode(String phone) {
        if(!RegexUtils.isPhone(phone)){
            throw new LyException(ExceptionEnum.INVALID_PHONE_NUMBER);
        }
//        生成随机验证码
        String code = RandomStringUtils.randomNumeric(6);
        Map<String,String> map = new HashMap();
        map.put("phone",phone);
        map.put("code",code);
        redisTemplate.opsForValue().set(KEY_PRE + phone,code,3, TimeUnit.MINUTES);
        //发送rabbitmq的消息
        amqpTemplate.convertAndSend(MQConstants.Exchange.SMS_EXCHANGE_NAME,
                MQConstants.RoutingKey.VERIFY_CODE_KEY,
                map);
    }

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    /**
     * 用户注册
     * @param user
     * @param code
     */
    @Override
    public void register(TbUser user, String code) {
//        1、先查code
        String cacheCode = redisTemplate.opsForValue().get(KEY_PRE + user.getPhone());
        if(StringUtils.isEmpty(cacheCode) || !cacheCode.equals(code)){
            throw  new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
//        2、写数据
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        boolean b = this.save(user);
        if(!b){
            throw  new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    /**
     * 用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    @Override
    public UserDTO queryUser(String username, String password) {
        //查询的时候不能放password，数据库中存的加密后的值
        QueryWrapper<TbUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbUser::getUsername,username);
        TbUser tbUser = this.getOne(queryWrapper);
        if(tbUser == null){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        boolean matches = passwordEncoder.matches(password, tbUser.getPassword());
        if(!matches){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        return BeanHelper.copyProperties(tbUser,UserDTO.class);
    }
}
