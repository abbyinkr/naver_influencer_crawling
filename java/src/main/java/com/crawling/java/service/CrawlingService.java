package com.crawling.java.service;

import com.crawling.java.vo.KeywordInfluencerInfoVO;
import com.crawling.java.vo.KeywordVO;
import com.crawling.java.vo.LogVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CrawlingService {

    public int addKeyword(KeywordVO keywordVO) throws Exception;
    public int updateKeyword(KeywordVO keywordVO) throws Exception;
    public List<KeywordVO> getKeyword() throws Exception;
    public int addLog(LogVO logVO) throws Exception;
    public int updateLog(LogVO logVO) throws Exception;
    public int addKeywordInfluencerInfo(KeywordInfluencerInfoVO keywordInfluencerInfoVO) throws Exception;
    public int addKeywordInfluencerDetail(String filePath) throws Exception;

    public int addRecommandedInfluencer(String filePath) throws Exception;
    public int addInfluencerContent(String filePath) throws Exception;
    public int addInfluencerContentImage(String filePath) throws Exception;

    public int addRelatedContent(String filePath) throws Exception;

    public int addPopularTopic(String filePath) throws Exception;





}
