package com.crawling.java.service;

import com.crawling.java.mapper.CrawlingMapper;
import com.crawling.java.vo.KeywordInfluencerInfoVO;
import com.crawling.java.vo.KeywordVO;
import com.crawling.java.vo.LogVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CrawlingServiceImpl implements CrawlingService{

    private final CrawlingMapper crawlingMapper;

    public CrawlingServiceImpl(CrawlingMapper crawlingMapper) {
        this.crawlingMapper = crawlingMapper;
    }

    @Override
    public int addKeyword(KeywordVO keywordVO) throws Exception{

        int result = 0;
        result = crawlingMapper.addKeyword(keywordVO);
        return result;
    }
    @Override
    public int updateKeyword(KeywordVO keywordVO) throws Exception{

        int result = 0;
        result = crawlingMapper.updateKeyword(keywordVO);
        return result;
    }

    public List<KeywordVO> getKeyword() throws Exception {

        List<KeywordVO> result = crawlingMapper.getKeyword();
        return result;
    }

    public int addLog(LogVO logVO) throws Exception{
        int result = crawlingMapper.addLog(logVO);
        return result;
    }
    public int updateLog(LogVO logVO) throws Exception {
        int result = crawlingMapper.updateLog(logVO);
        return result;

    }
    @Override
    public int addKeywordInfluencerInfo(KeywordInfluencerInfoVO keywordInfluencerInfoVO) throws Exception {

        int result = 0;
        result = crawlingMapper.addKeywordInfluencerInfo(keywordInfluencerInfoVO);
        return result;
    }

    @Override
    public int addKeywordInfluencerDetail(String filePath) throws Exception {
        int result = 0;
        result = crawlingMapper.addKeywordInfluencerDetail(filePath);
        return result;
    }

    @Override
    public int addRecommandedInfluencer(String filePath) throws Exception{
        int result = 0;
        result = crawlingMapper.addRecommandedInfluencer(filePath);
        return result;
    }

    @Override
    public int addInfluencerContent(String filePath) throws Exception{
        int result = 0;
        result = crawlingMapper.addInfluencerContent(filePath);
        return result;
    }

    @Override
    public int addInfluencerContentImage(String filePath) throws Exception{

        int result = 0;
        result = crawlingMapper.addInfluencerContentImage(filePath);
        return result;
    }

    @Override
    public int addRelatedContent(String filePath) throws Exception {
        int result = 0;
        result = crawlingMapper.addRelatedContent(filePath);
        return result;
    }

    @Override
    public int addPopularTopic(String filePath) throws Exception {
        int result = 0;
        result = crawlingMapper.addPopularTopic(filePath);
        return result;
    }


}
