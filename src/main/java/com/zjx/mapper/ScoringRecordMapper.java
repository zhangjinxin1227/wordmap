package com.zjx.mapper;

import com.zjx.model.ScoringRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScoringRecordMapper {

    List<ScoringRecord> getSeeComment(@Param("rootId") String rootId);

    ScoringRecord getScoringRecord(@Param("rootId") String rootId, @Param("weekFirstDate")String weekFirstDate, @Param("weekLastDate")String weekLastDate);

    Integer insertMark(ScoringRecord scoringRecord);

    List<ScoringRecord> getUserScoringRecord(@Param("userId") Integer userId);
}
