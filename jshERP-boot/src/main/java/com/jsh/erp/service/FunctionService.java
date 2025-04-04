package com.jsh.erp.service;

import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.datasource.entities.*;
import com.jsh.erp.datasource.mappers.FunctionMapper;
import com.jsh.erp.datasource.mappers.FunctionMapperEx;
import com.jsh.erp.exception.JshException;
import com.jsh.erp.utils.PageUtils;
import com.jsh.erp.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class FunctionService {
    private Logger logger = LoggerFactory.getLogger(FunctionService.class);

    @Resource
    private FunctionMapper functionsMapper;

    @Resource
    private FunctionMapperEx functionMapperEx;

    @Resource
    private UserService userService;

    @Resource
    private UserBusinessService userBusinessService;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private LogService logService;

    public Function getFunction(long id)throws Exception {
        Function result=null;
        try{
            result=functionsMapper.selectByPrimaryKey(id);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<Function> getFunctionListByIds(String ids)throws Exception {
        List<Long> idList = StringUtil.strToLongList(ids);
        List<Function> list = new ArrayList<>();
        try{
            FunctionExample example = new FunctionExample();
            example.createCriteria().andIdIn(idList);
            list = functionsMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<Function> getFunction()throws Exception {
        FunctionExample example = new FunctionExample();
        example.createCriteria().andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Function> list=null;
        try{
            list=functionsMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<FunctionEx> select(String name, String type)throws Exception {
        List<FunctionEx> list=null;
        try{
            if(BusinessConstants.DEFAULT_MANAGER.equals(userService.getCurrentUser().getLoginName())) {
                PageUtils.startPage();
                list = functionMapperEx.selectByConditionFunction(name, type);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertFunction(JSONObject obj, HttpServletRequest request)throws Exception {
        Function functions = JSONObject.parseObject(obj.toJSONString(), Function.class);
        int result=0;
        try{
            if(BusinessConstants.DEFAULT_MANAGER.equals(userService.getCurrentUser().getLoginName())) {
                functions.setState(false);
                functions.setType("电脑版");
                result = functionsMapper.insertSelective(functions);
                logService.insertLog("功能",
                        new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_ADD).append(functions.getName()).toString(), request);
            }
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateFunction(JSONObject obj, HttpServletRequest request) throws Exception{
        Function functions = JSONObject.parseObject(obj.toJSONString(), Function.class);
        int result=0;
        try{
            if(BusinessConstants.DEFAULT_MANAGER.equals(userService.getCurrentUser().getLoginName())) {
                result = functionsMapper.updateByPrimaryKeySelective(functions);
                logService.insertLog("功能",
                        new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_EDIT).append(functions.getName()).toString(), request);
            }
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteFunction(Long id, HttpServletRequest request)throws Exception {
        return batchDeleteFunctionByIds(id.toString());
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteFunction(String ids, HttpServletRequest request)throws Exception {
        return batchDeleteFunctionByIds(ids);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteFunctionByIds(String ids)throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(BusinessConstants.LOG_OPERATION_TYPE_DELETE);
        List<Function> list = getFunctionListByIds(ids);
        for(Function functions: list){
            sb.append("[").append(functions.getName()).append("]");
        }
        User userInfo=userService.getCurrentUser();
        String [] idArray=ids.split(",");
        int result=0;
        try{
            if(BusinessConstants.DEFAULT_MANAGER.equals(userService.getCurrentUser().getLoginName())) {
                result = functionMapperEx.batchDeleteFunctionByIds(new Date(), userInfo == null ? null : userInfo.getId(), idArray);
                logService.insertLog("功能", sb.toString(),
                        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
            }
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public int checkIsNameExist(Long id, String name)throws Exception {
        FunctionExample example = new FunctionExample();
        example.createCriteria().andIdNotEqualTo(id).andNameEqualTo(name).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Function> list=null;
        try{
            list = functionsMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list==null?0:list.size();
    }

    public int checkIsNumberExist(Long id, String number)throws Exception {
        FunctionExample example = new FunctionExample();
        example.createCriteria().andIdNotEqualTo(id).andNumberEqualTo(number).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Function> list=null;
        try{
            list = functionsMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list==null?0:list.size();
    }

    public List<Function> getRoleFunction(String pNumber)throws Exception {
        FunctionExample example = new FunctionExample();
        example.createCriteria().andEnabledEqualTo(true).andParentNumberEqualTo(pNumber)
                .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        example.setOrderByClause("Sort");
        List<Function> list=null;
        try{
            list = functionsMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<Function> findRoleFunction(String pnumber, List<Long> funIdList)throws Exception{
        List<Function> list=null;
        try{
            Boolean multiLevelApprovalFlag = systemConfigService.getMultiLevelApprovalFlag();
            FunctionExample example = new FunctionExample();
            FunctionExample.Criteria criteria = example.createCriteria();
            criteria.andEnabledEqualTo(true).andParentNumberEqualTo(pnumber)
                    .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
            if("0".equals(pnumber)) {
                if(!multiLevelApprovalFlag) {
                    criteria.andUrlNotEqualTo("/workflow");
                }
            }
            if(funIdList!=null && funIdList.size()>0) {
                criteria.andIdIn(funIdList);
            }
            example.setOrderByClause("Sort");
            list =functionsMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<Function> findByIds(String functionsIds)throws Exception{
        List<Long> idList = StringUtil.strToLongList(functionsIds);
        FunctionExample example = new FunctionExample();
        example.createCriteria().andEnabledEqualTo(true).andIdIn(idList).andPushBtnIsNotNull().andPushBtnNotEqualTo("")
                .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        example.setOrderByClause("Sort asc");
        List<Function> list=null;
        try{
            list =functionsMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    /**
     * 获取当前用户所属的租户所拥有的功能id列表
     * @return
     */
    public List<Long> getCurrentTenantFunIdList() throws Exception {
        List<Long> funIdList = new ArrayList<>();
        Long roleId = 0L;
        String fc = "";
        User userInfo = userService.getCurrentUser();
        //获取当前用户所有的角色id
        List<UserBusiness> roleList = userBusinessService.getBasicData(userInfo.getTenantId().toString(), "UserRole");
        if(roleList!=null && roleList.size()>0){
            String value = roleList.get(0).getValue();
            if(StringUtil.isNotEmpty(value)){
                String roleIdStr = value.replace("[", "").replace("]", "");
                roleId = Long.parseLong(roleIdStr);
            }
        }
        //当前用户所拥有的功能列表，格式如：[1][2][5]
        List<UserBusiness> funList = userBusinessService.getBasicData(roleId.toString(), "RoleFunctions");
        if(funList!=null && funList.size()>0){
            fc = funList.get(0).getValue();
        }
        if(StringUtil.isNotEmpty(fc)) {
            fc = fc.substring(1, fc.length() - 1);
            fc = fc.replace("][",",");
            funIdList = StringUtil.strToLongList(fc);
        }
        return funIdList;
    }

    /**
     * 获取当前用户所属的租户所拥有的功能id的map
     * @return
     */
    public Map<Long, Long> getCurrentTenantFunIdMap() throws Exception {
        Map<Long, Long> funIdMap = new HashMap<>();
        List<Long> list = getCurrentTenantFunIdList();
        if(list.size()>0) {
            for (Long funId : list) {
                funIdMap.put(funId, funId);
            }
            return funIdMap;
        } else {
            return null;
        }
    }

    /**
     * 获取当前用户所拥有的功能id列表
     * @return
     */
    public List<Long> getCurrentUserFunIdList() throws Exception {
        List<Long> funIdList = new ArrayList<>();
        Long roleId = 0L;
        String fc = "";
        User userInfo = userService.getCurrentUser();
        //获取当前用户所有的角色id
        List<UserBusiness> roleList = userBusinessService.getBasicData(userInfo.getId().toString(), "UserRole");
        if(roleList!=null && roleList.size()>0){
            String value = roleList.get(0).getValue();
            if(StringUtil.isNotEmpty(value)){
                String roleIdStr = value.replace("[", "").replace("]", "");
                roleId = Long.parseLong(roleIdStr);
            }
        }
        //当前用户所拥有的功能列表，格式如：[1][2][5]
        List<UserBusiness> funList = userBusinessService.getBasicData(roleId.toString(), "RoleFunctions");
        if(funList!=null && funList.size()>0){
            fc = funList.get(0).getValue();
        }
        if(StringUtil.isNotEmpty(fc)) {
            fc = fc.substring(1, fc.length() - 1);
            fc = fc.replace("][",",");
            funIdList = StringUtil.strToLongList(fc);
        }
        return funIdList;
    }

    /**
     * 获取当前用户所拥有的功能id的map
     * @return
     */
    public Map<Long, Long> getCurrentUserFunIdMap() throws Exception {
        Map<Long, Long> funIdMap = new HashMap<>();
        List<Long> list = getCurrentUserFunIdList();
        if(list.size()>0) {
            for(Long funId: list) {
                funIdMap.put(funId, funId);
            }
            return funIdMap;
        } else {
            return null;
        }
    }
}
