package com.icbc.shcpe_system.dao;

import com.icbc.shcpe_system.model.ShcpeDealInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface ShcpeDealInfoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ShcpeDealInfo record);

    int insertSelective(ShcpeDealInfo record);

    ShcpeDealInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ShcpeDealInfo record);

    int updateByPrimaryKey(ShcpeDealInfo record);

    String selectNewestXmlByQuoteIdAndMsgType(String quoteId,String msgType);
}