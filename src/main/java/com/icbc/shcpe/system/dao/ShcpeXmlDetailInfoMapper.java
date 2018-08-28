package com.icbc.shcpe.system.dao;

import com.icbc.shcpe.system.model.ShcpeXmlDetailInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ShcpeXmlDetailInfoMapper {
    int deleteByPrimaryKey(Long xmlId);

    int insert(ShcpeXmlDetailInfo record);

    int insertSelective(ShcpeXmlDetailInfo record);

    ShcpeXmlDetailInfo selectByPrimaryKey(Long xmlId);

    int updateByPrimaryKeySelective(ShcpeXmlDetailInfo record);

    int updateByPrimaryKeyWithBLOBs(ShcpeXmlDetailInfo record);
}