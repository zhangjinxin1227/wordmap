package com.zjx.mapper;

import com.zjx.model.MindMap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MindMapMapper {

    //新建思维导图
    Boolean saveMindMap(MindMap mindMap);

    MindMap getMindMap(@Param("rootId") String rootId);

    //修改图谱名称
    void updateMapName(@Param("mapId")String rootId, @Param("mapName")String mapName);

    Integer updateMindMap(MindMap mindMap);

    Integer deleteMap(@Param("rootId") String rootId);

    List<MindMap> getMapList(@Param("userId")Integer userId);

    Integer shareMap(@Param("rootId") String rootId);

    Integer deleteshareMap(@Param("rootId") String rootId);

    List<MindMap> getShareMapList(@Param("userId")Integer userId);

    List<MindMap> getShareMapAllList();

    List<MindMap> getSearchMindByName(@Param("searchMessage") String searchMessage);

    MindMap getMapGrade(@Param("rootId") String rootId);

    //定时任务评分：获得map列表
    List<MindMap> getMapGradeList();

    void updateGrade(MindMap mindMap);
}
