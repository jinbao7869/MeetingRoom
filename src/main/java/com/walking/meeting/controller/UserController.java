package com.walking.meeting.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.walking.meeting.Service.MeetingService;
import com.walking.meeting.Service.TokenService;
import com.walking.meeting.Service.UserService;
import com.walking.meeting.common.*;
import com.walking.meeting.dataobject.dao.MeetingDO;
import com.walking.meeting.dataobject.dao.UserDO;
import com.walking.meeting.dataobject.dto.UserDTO;
import com.walking.meeting.dataobject.query.UserQuery;
import com.walking.meeting.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Objects;

import static com.walking.meeting.utils.DateUtils.FORMAT_YYYY_MM_DD_HH_MM;

@CrossOrigin
@Slf4j
@Api(tags = "UserController", description = "用户模块")
@RestController("UserController")
@RequestMapping("/user")
public class UserController {
    // TODO 人脸识别登录接口

    @Autowired
    private UserService userService;
    @Autowired
    private MeetingService meetingService;
    @Autowired
    private TokenService tokenService;

    @ApiOperation(value = "人脸识别用户登录", notes = "人脸识别用户登录")
    @PostMapping(value = "/faceLogin")
    public Response userFaceLogin() {
        // TODO 人脸识别登录接口
        // 加到session里去
//        request.getSession().setAttribute(Const.CURRENT_USER,userDO);
        return ResponseUtils.returnDefaultSuccess();
    }

    @ApiOperation(value = "用户登录", notes = "用户登录")
    @PassToken
    @PostMapping(value = "/login")
    public Response userLogin(
            @ApiParam(name = "login_name", value = "用户名") @RequestParam(value = "login_name") String loginName,
            @ApiParam(name = "password", value = "密码") @RequestParam(value = "password") String password,
            @ApiParam(name = "user_role", value = "0:管理员 1:普通用户") @RequestParam(
                    value = "user_role",required=false,defaultValue="1") Integer userRole,
            HttpServletRequest request) {
        log.info("用户登录, loginName:{}, password:{}, userRole:{}", loginName, password, userRole);
        if (StringUtils.isBlank(loginName) || StringUtils.isBlank(password) || Objects.isNull(userRole)) {
            throw new ResponseException(StatusCodeEnu.PORTION_PARAMS_NULL_ERROR);
        }
        UserQuery userQuery = new UserQuery();
        userQuery.setUserName(loginName);
        UserDO userDO = userService.getUserByUserQuery(userQuery);
        // 判username是否存在
        if (Objects.isNull(userDO)){
            throw new ResponseException(StatusCodeEnu.USERNAME_NOT_EXIST);
        }
        // 判账号密码正确性，密码md5,不相等就抛异常
        if (!StringUtils.equals(userDO.getPswd(),MD5Encrypt.md5Encrypt(password))){
            log.info("数据库里的:{},加密完的数据:{}",userDO.getPswd(),MD5Encrypt.md5Encrypt(password));
            throw new ResponseException(StatusCodeEnu.USERNAME_OR_PSWD_ERROR);
        }
        // 判断user_role
        if (!userRole.equals(userDO.getRoleId())) {
            throw new ResponseException(StatusCodeEnu.USER_ROLE_ERROR);
        }
        // 加到session里去
//        request.getSession().setAttribute(Const.CURRENT_USER,userDO);
        // 生成token
        JSONObject jsonObject=new JSONObject();
        String token = tokenService.generateToken(loginName);
        jsonObject.put("token", token);
        jsonObject.put("user", userDO);
        tokenService.save(token,userDO.getUsername());
        return ResponseUtils.returnSuccess(jsonObject);
    }

    @ApiOperation(value = "测试登录是否成功", notes = "测试登录是否成功")
    @UserLogin
    @GetMapping("/getMessage")
    public String getMessage(){
        return "你已通过验证";
    }


    @ApiOperation(value = "用户注册", notes = "用户注册")
    @PassToken
    @PostMapping(value = "/register")
    public Response userRegister(
            @ApiParam(name = "login_name", value = "用户名") @RequestParam(value = "login_name") String loginName,
            @ApiParam(name = "password", value = "密码") @RequestParam(value = "password") String password,
            @ApiParam(name = "question", value = "密保问题") @RequestParam(value = "question") String question,
            @ApiParam(name = "email", value = "email") @RequestParam(value = "email") String email,
            @ApiParam(name = "answer", value = "密保问题回答") @RequestParam(value = "answer") String answer) {
        log.info("用户注册,username:{},password:{},question:{},answer:{},email:{}",loginName,password,question,answer,email);
        // 参数判空
        if (Objects.isNull(loginName)||Objects.isNull(password) || Objects.isNull(question) ||
                Objects.isNull(email) || Objects.isNull(answer)) {
            throw new ResponseException(StatusCodeEnu.PORTION_PARAMS_NULL_ERROR);
        }
        // 数据库判username是否存在
        UserQuery userQuery = new UserQuery();
        userQuery.setUserName(loginName);
        UserDO userDO = userService.getUserByUserQuery(userQuery);
        // 判username是否存在
        if (!Objects.isNull(userDO)){
            throw new ResponseException(StatusCodeEnu.USERNAME_EXIST);
        }
        // 数据库录入操作，密码md5
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(loginName);
        userDTO.setPswd(MD5Encrypt.md5Encrypt(password));
        userDTO.setQuestion(question);
        userDTO.setAnswer(answer);
        userDTO.setEmail(email);
        userService.addUser(userDTO);
        return ResponseUtils.returnDefaultSuccess();
    }

    @ApiOperation(value = "用户删除", notes = "用户删除")
    @UserLogin
    @PostMapping(value = "/del")
    public Response userRegister(
            @ApiParam(name = "login_name", value = "用户名") @RequestParam(value = "login_name") String loginName,
            HttpServletRequest request){
        String username;
        try {
            username = tokenService.getUsername(request.getHeader("token"));
        } catch (Exception e) {
            throw new ResponseException(StatusCodeEnu.TOKEN_ERROR);
        }
        UserQuery userQuery = new UserQuery();
        userQuery.setUserName(username);
        UserDO isUserRoleExist = userService.getUserByUserQuery(userQuery);
        // 不是管理员无法删除用户
        if (isUserRoleExist.getRoleId()!=0) {
            throw new ResponseException(StatusCodeEnu.NO_RIGHT);
        }
        UserDO userDO = new UserDO();
        userDO.setUsername(loginName);
        userDO.setDeleteTime(DateUtils.formatDate(new Date(), FORMAT_YYYY_MM_DD_HH_MM));
        userService.updateUserSelective(userDO);

        // room_booking表也要删除username相关字段
        MeetingDO meetingDO = new MeetingDO();
        meetingDO.setUsername(loginName);
        meetingDO.setDeleteTime(DateUtils.formatDate(new Date(), FORMAT_YYYY_MM_DD_HH_MM));
        meetingService.updateMeetingSelective(meetingDO);

        return ResponseUtils.returnDefaultSuccess();
    }

    @ApiOperation(value = "用户退出", notes = "用户退出")
    @UserLogin
    @PostMapping(value = "/logout")
    public Response userLogout(HttpServletRequest request){
        String username;
        try {
            username = tokenService.getUsername(request.getHeader("token"));
        } catch (Exception e) {
            throw new ResponseException(StatusCodeEnu.TOKEN_ERROR);
        }
//        UserDO userDo = (UserDO) request.getSession().getAttribute(Const.CURRENT_USER);
        tokenService.delToken(request.getHeader("token"));
        log.info("用户 " + username + " 退出");
//        request.getSession().removeAttribute(Const.CURRENT_USER);
        return ResponseUtils.returnDefaultSuccess();
    }

    @ApiOperation(value = "获取用户信息", notes = "获取用户信息")
    @UserLogin
    @PostMapping(value = "/info")
    public Response getUserInfo(HttpServletRequest request){
        String username;
        try {
            username = tokenService.getUsername(request.getHeader("token"));
        } catch (Exception e) {
            throw new ResponseException(StatusCodeEnu.TOKEN_ERROR);
        }
        UserQuery userQuery = new UserQuery();
        userQuery.setUserName(username);
        UserDO user = userService.getUserByUserQuery(userQuery);
//        UserDO userDo = (UserDO) request.getSession().getAttribute(Const.CURRENT_USER);
        if (ObjectUtils.isNotEmpty(user)) {
            log.info("用户 "+ user.getUsername() +" 获取其信息");
            return ResponseUtils.returnSuccess(user);
        }
        return ResponseUtils.returnDefaultError();
    }

    @ApiOperation(value = "登录状态下修改密码", notes = "登录状态下修改密码")
    @UserLogin
    @PostMapping(value = "/reset/online")
    public Response resetPasswordOnline(
        @ApiParam(name = "new_password", value = "新密码") @RequestParam(value = "new_password") String newPassword,
        @ApiParam(name = "new_password_confirm", value = "确认新密码")
        @RequestParam(value = "new_password_confirm") String newPasswordConfirm, HttpServletRequest request){
        if (!newPasswordConfirm.equals(newPassword)) {
            throw new ResponseException(StatusCodeEnu.TWO_PSWD_NOT_SAME);
        }
        // 取出已经登录的DO
//        UserDO userDo = (UserDO) request.getSession().getAttribute(Const.CURRENT_USER);
        String username;
        try {
            username = tokenService.getUsername(request.getHeader("token"));
        } catch (Exception e) {
            throw new ResponseException(StatusCodeEnu.TOKEN_ERROR);
        }
        log.info("用户 "+ username +" 登录状态下修改密码");
        UserQuery userQuery = new UserQuery();
        userQuery.setUserName(username);
        UserDO user = userService.getUserByUserQuery(userQuery);
        // 新老密码不能一样
        if (StringUtils.equals(user.getPswd(),MD5Encrypt.md5Encrypt(newPassword))){
            log.info("数据库里的:{},加密完的数据:{}",user.getPswd(),MD5Encrypt.md5Encrypt(newPassword));
            throw new ResponseException(StatusCodeEnu.TWO_PSWD_SAME);
        }
        user.setPswd(MD5Encrypt.md5Encrypt(newPassword));
        userService.updateUserSelective(user);
        return ResponseUtils.returnDefaultSuccess();
    }

    @ApiOperation(value = "未登录状态下修改密码", notes = "未登录状态下修改密码")
    @PassToken
    @PostMapping(value = "/reset/offline")
    public Response resetPasswordOnline(
            @ApiParam(name = "username", value = "用户名") @RequestParam(value = "username") String username,
            @ApiParam(name = "new_password", value = "新密码") @RequestParam(value = "new_password") String newPassword,
            @ApiParam(name = "question", value = "密保问题") @RequestParam(value = "question") String question,
            @ApiParam(name = "answer", value = "密保问题答案") @RequestParam(value = "answer") String answer){
        log.info("未登录状态下修改密码,username:{},password:{},question:{},answer:{}",username,newPassword,question,answer);
        // 参数判空
        if (Objects.isNull(username)||Objects.isNull(newPassword) || Objects.isNull(question) || Objects.isNull(answer)) {
            throw new ResponseException(StatusCodeEnu.PORTION_PARAMS_NULL_ERROR);
        }
        UserQuery userQuery = new UserQuery();
        userQuery.setUserName(username);
        UserDO userDO = userService.getUserByUserQuery(userQuery);
        if (ObjectUtils.isEmpty(userDO)) {
            throw new ResponseException(StatusCodeEnu.USERNAME_NOT_EXIST);
        }
        if (!userDO.getQuestion().equals(question)) {
            throw new ResponseException(StatusCodeEnu.QUESTION_NOT_RIGHT);
        }
        if (!userDO.getAnswer().equals(answer)) {
            throw new ResponseException(StatusCodeEnu.ANSWER_NOT_RIGHT);
        }
        userDO.setPswd(MD5Encrypt.md5Encrypt(newPassword));
        userService.updateUserSelective(userDO);
        return ResponseUtils.returnDefaultSuccess();
    }


    // TODO 给用户添加部门

//    public static void main(String[] args) {
//        String password = "qwe123";
//        String EnPassword = MD5Encrypt.md5Encrypt(password);
//        boolean isTrue = MD5Encrypt.verify(password, EnPassword);
//        System.out.println(EnPassword+"   "+isTrue);
//    }


}
