package com.crawling.java.mapper;

import com.crawling.java.vo.KeywordInfluencerInfoVO;
import com.crawling.java.vo.KeywordVO;
import com.crawling.java.vo.LogVO;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public class CrawlingMapperImpl implements CrawlingMapper {

    @Qualifier("sqlSessionTemplate")
    @Autowired
    public SqlSessionTemplate sqlSession;

    @Override
    public int addKeyword(KeywordVO keywordVO) throws Exception {

        int result = 0;
        result = sqlSession.selectOne("com.crawling.java.mapper.CrawlingMapper.addKeyword", keywordVO);
        return result;
    }
    @Override
    public int updateKeyword(KeywordVO keywordVO) throws Exception {

        int result = 0;
        result = sqlSession.selectOne("com.crawling.java.mapper.CrawlingMapper.updateKeyword", keywordVO);
        return result;
    }

    public int addLog(LogVO logVO) throws Exception {
        int result = 0;
        result = sqlSession.selectOne("com.crawling.java.mapper.CrawlingMapper.addLog", logVO);
        return result;
    }
    public int updateLog(LogVO logVO) throws Exception {
        int result = 0;
        result = sqlSession.selectOne("com.crawling.java.mapper.CrawlingMapper.updateLog", logVO);
        return result;
    }

    public List<KeywordVO> getKeyword() throws Exception {

        List<KeywordVO> result = sqlSession.selectList("com.crawling.java.mapper.CrawlingMapper.getKeyword");
        return result;
    }

    @Override
    public int addKeywordInfluencerInfo(KeywordInfluencerInfoVO keywordInfluencerInfoVO) throws Exception {

        int result = 0;
        result = sqlSession.selectOne("com.crawling.java.mapper.CrawlingMapper.addKeywordInfluencerInfo", keywordInfluencerInfoVO);
        return result;
    }

    @Override
    public int addKeywordInfluencerDetail(String filePath) throws Exception {
        int result = 0;
        result = sqlSession.selectOne("com.crawling.java.mapper.CrawlingMapper.addKeywordInfluencerDetail", filePath);
        return result;

    }

    @Override
    public int addRecommandedInfluencer(String filePath) throws Exception {
        int result = 0;
        result = sqlSession.selectOne("com.crawling.java.mapper.CrawlingMapper.addRecommandedInfluencer", filePath);
        return result;
    }

    @Override
    public int addInfluencerContent(String filePath) throws Exception{
        int result = 0;
        result = sqlSession.selectOne("com.crawling.java.mapper.CrawlingMapper.addInfluencerContent", filePath);
        return result;
    }

    @Override
    public int addInfluencerContentImage(String filePath) throws Exception {

        int result = 0;
        result = sqlSession.selectOne("com.crawling.java.mapper.CrawlingMapper.addInfluencerContentImage", filePath);
        return result;
    }
    @Override
    public int addRelatedContent(String filePath) throws Exception {
        int result = 0;
        result = sqlSession.selectOne("com.crawling.java.mapper.CrawlingMapper.addRelatedContent", filePath);
        return result;
    }

    @Override
    public int addPopularTopic(String filePath) throws Exception {
        int result = 0;
        result = sqlSession.selectOne("com.crawling.java.mapper.CrawlingMapper.addPopularTopic", filePath);
        return result;
    }
}
