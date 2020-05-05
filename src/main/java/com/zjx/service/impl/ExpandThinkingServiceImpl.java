package com.zjx.service.impl;

import com.zjx.mapper.MindMapMapper;
import com.zjx.mapper.ScoringRecordMapper;
import com.zjx.model.MindMap;
import com.zjx.model.ScoringRecord;
import com.zjx.service.ExpandThinkingService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ExpandThinkingServiceImpl implements ExpandThinkingService {

    @Resource
    private MindMapMapper mindMapMapper;

    @Resource
    private ScoringRecordMapper scoringRecordMapper;


    /**
     * 获得分享的图谱列表
     * @return
     */
    @Override
    public List<MindMap> getShareMapList(){
        return mindMapMapper.getShareMapAllList();
    }

    /**
     * 获得某个图谱的评论列表
     */
    @Override
    public List<ScoringRecord> getSeeComment(String rootId){
        return scoringRecordMapper.getSeeComment(rootId);
    }

    /**
     * 获得某个导图分数
     */
    @Override
    public MindMap getMapGrade(String rootId){
        return mindMapMapper.getMapGrade(rootId);
    }

    /**
     * 查询思维导图
     * @param searchMessage
     * @return
     */
    @Override
    public List<MindMap> getSearchMindByName(String searchMessage){
        return mindMapMapper.getSearchMindByName(searchMessage);
    }

    /**
     * 用户位思维导图打分
     */
    @Override
    public Integer insertMark(ScoringRecord scoringRecord){
        return scoringRecordMapper.insertMark(scoringRecord);
    }
}
