package com.zjx.service;

import com.zjx.model.MindMap;
import com.zjx.model.ScoringRecord;
import java.util.List;

public interface ExpandThinkingService {

    List<MindMap> getShareMapList();

    List<ScoringRecord> getSeeComment(String rootId);

    MindMap getMapGrade(String rootId);

    List<MindMap> getSearchMindByName(String searchMessage);

    Integer insertMark(ScoringRecord scoringRecord);
}
