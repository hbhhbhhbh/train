package com.hbh.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hbh.train.common.context.LoginMemberContext;
import com.hbh.train.common.resp.PageResp;
import com.hbh.train.common.util.SnowUtil;
import com.hbh.train.member.domain.${Domain};
import com.hbh.train.member.domain.${Domain}Example;
import com.hbh.train.member.mapper.${Domain}Mapper;
import com.hbh.train.member.req.${Domain}QueryReq;
import com.hbh.train.member.req.${Domain}SaveReq;
import com.hbh.train.member.resp.${Domain}QueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ${Domain}Service
{
    private static final Logger LOG = LoggerFactory.getLogger(${Domain}Service.class);

    @Resource
    private ${Domain}Mapper ${domain}Mapper;
    public void save(${Domain}SaveReq req)
    {
        DateTime now= DateTime.now();
        ${Domain} ${domain}=BeanUtil.copyProperties(req, ${Domain}.class);
        if(ObjectUtil.isNull(${domain}.getId()))
        {
            ${domain}.setMemberId(LoginMemberContext.getId());
            ${domain}.setId(SnowUtil.getSnowflakeNextId());
            ${domain}.setCreateTime(now);
            ${domain}.setUpdateTime(now);
            ${domain}Mapper.insert(${domain});
        }else{
            ${domain}.setUpdateTime(now);
            ${domain}Mapper.updateByPrimaryKey(${domain});
        }

    }


    public PageResp<${Domain}QueryResp> queryList(${Domain}QueryReq req)
    {

        ${Domain}Example ${domain}example=new ${Domain}Example();
        ${domain}example.setOrderByClause("id desc");
        ${Domain}Example.Criteria criteria= ${domain}example.createCriteria();
        if(ObjectUtil.isNotNull(req.getMemberId()))
        {
            criteria.andMemberIdEqualTo(req.getMemberId());
        }
        PageHelper.startPage(req.getPage(),req.getSize());//分页,查询第一条，查两条
        List<${Domain}>${domain}List=${domain}Mapper.selectByExample(${domain}example);
        PageInfo<${Domain}>pageinfo=new PageInfo<>(${domain}List);
        LOG.info("总行数：{}", pageinfo.getTotal());
        LOG.info("总页数：{}", pageinfo.getPages());
        List<${Domain}QueryResp> respList=BeanUtil.copyToList(${domain}List,${Domain}QueryResp.class);
        //生成PageResp对象
        PageResp<${Domain}QueryResp>pageResp=new PageResp<>();
        pageResp.setList(respList);
        pageResp.setTotal(pageinfo.getTotal());
        return pageResp;


    }
    public void delete(Long id)
    {
        ${domain}Mapper.deleteByPrimaryKey(id);

    }
}
