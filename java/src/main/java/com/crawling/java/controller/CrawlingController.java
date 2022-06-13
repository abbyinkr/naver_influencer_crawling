package com.crawling.java.controller;

import com.crawling.java.service.CrawlingServiceImpl;
import com.crawling.java.vo.KeywordInfluencerInfoVO;
import com.crawling.java.vo.KeywordVO;
import com.crawling.java.vo.LogVO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class CrawlingController {

    private final CrawlingServiceImpl crawlingService;

    private static Log logger = LogFactory.getLog(CrawlingController.class);

    // 생성자
    public CrawlingController(CrawlingServiceImpl crawlingService) {
        this.crawlingService = crawlingService;
    }

    @Scheduled(cron = "0 0 0/3 1/1 *  *")
    @GetMapping(value = "/crawling")
    public String crawling() throws Exception {


        String todayDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

        List<KeywordVO> keywordVoList = crawlingService.getKeyword();

        for (KeywordVO selectedKeyword : keywordVoList) {


            String keyword = selectedKeyword.getKeyword();

            final String url = "https://search.naver.com/search.naver?where=influencer&sm=tab_jum&query=%23"+keyword;
            
            Document document = Jsoup.connect(url).get();

            KeywordVO keywordVO = new KeywordVO();
            LogVO logVO = new LogVO();


            // 1.keyword_influencer_info

            KeywordInfluencerInfoVO keywordInfluencerInfoVO = new KeywordInfluencerInfoVO();

            Elements e1 = document.getElementsByClass("creator_wrap type_filter _tab_creator");
            try {

                logVO.setKeyword(keyword);
                logVO.setStatus(0);
                logVO.setProcess("keyword_influencer_info");
                logVO.setMessage("");
                crawlingService.addLog(logVO);

                keywordVO.setKeyword(keyword);
                keywordVO.setStatus(0); // 0: 수집시작 1: 정상 9: 비정상

                String total_count_str = e1.get(0).getElementsByClass("num_inner _num").text();
                int total_count = Integer.parseInt(total_count_str.replaceAll(" ",""));

                String type = e1.get(0).getElementsByClass("_count_title").text();
                type = type.replace("참여중이 정보가 표시된 이유","").trim();

                keywordInfluencerInfoVO.setKeyword_seq(selectedKeyword.getSeq());
                keywordInfluencerInfoVO.setType(type);
                keywordInfluencerInfoVO.setTotal_count(total_count);

                // keyword_influencer_info DB 입력
                crawlingService.addKeywordInfluencerInfo(keywordInfluencerInfoVO);

                logger.info(keyword + " keyword_influencer_info DB 입력 완료");

            } catch (Exception e) {
                e.printStackTrace();
                logger.error(keyword + " keyword_influencer_info DB 입력 실패");
                logVO.setStatus(9);
                logVO.setMessage(e.getMessage());
                crawlingService.updateLog(logVO);
                updateKeywordStatus(9,keywordVO);
                throw e;
            }

            // 1~5초 ranmdom sleep
            randomSleep();

            //2. keyword_influencer_detail 시작

            Element influencer_summary_element = document.getElementsByClass("api_flicking_wrap my_filter _major_subjects _scroll_target").get(0);

            // 수집한 데이터로 csv 파일 만들기
            String filePath = "D:/devfolder/csv/naver_crawling/keyword_influencer_detail/keyword_influencer_detail"+ keyword + "_" + todayDate+ ".csv";

            File file = null;

            BufferedWriter bw = null;

            String newLine = System.lineSeparator(); // 줄바꿈(\n)

            try {

                file = new File(filePath);
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF8"));

                bw.write("keyword_seq,type,count");
                bw.write(newLine);

                Elements name_target_elements = influencer_summary_element.getElementsByClass("name _name_target");


                for (int i = 0; i < name_target_elements.size(); i++) {
                    KeywordInfluencerInfoVO keywordInfoVO = new KeywordInfluencerInfoVO();
                    String detail_type = influencer_summary_element.getElementsByClass("name _name_target").get(i).text();
                    String type_count_str = influencer_summary_element.getElementsByTag("span").get(i).text();
                    type_count_str = type_count_str.replace("명","");
                    int type_count = Integer.parseInt(type_count_str);

                    bw.write(selectedKeyword.getSeq() +","+ detail_type +","+ type_count);
                    bw.write(newLine);
                }

                // 로그 남기기
                logger.info(keyword + " keyword_influencer_detail csv 파일 생성 완료");

            } catch (Exception e) {
                e.getStackTrace();
                logger.error(e);
                logger.error(keyword + " keyword_influencer_detail csv 파일 생성 실패");
                updateKeywordStatus(9,keywordVO);
                throw e;
            } finally {
                if (bw != null) {
                    try {
                        bw.flush();
                        //bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.error(e.getMessage());
                        throw e;

                    }
                }
            }
            // keyword_influencer_detail DB 입력
            try {

                crawlingService.addKeywordInfluencerDetail(filePath);
                logger.info(keyword + " keyword_influencer_detail csv 파일 DB 입력 완료");

            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage());
                logger.error(keyword + " keyword_influencer_detail csv파일 DB 입력 실패");
                updateKeywordStatus(9,keywordVO);
                throw e;
            }

            randomSleep();

            // 3. recommanded_influencer 시작

            Elements recommendedInfluencer_elements =  document.getElementsByClass("api_flicking_wrap creator_info_wrap _article_list");
            Elements recommendedInfluencer_profile_elements =  document.getElementsByClass("api_flicking_wrap creator_flick_wrap _profile_list");
            Elements recommendedInfluencer_info_elements = recommendedInfluencer_elements.get(0).getElementsByClass("flick_bx flick_width _item");

            // 수집한 데이터로 csv 파일 만들기
            filePath = "D:/devfolder/csv/naver_crawling/recommanded_influencer/recommanded_influencer"  +keyword + "_" +todayDate+ ".csv";

            try {

                file = new File(filePath);

                // UTF-8로 지정하면 CSV 파일 자체에서는 한글이 깨지지만 LOADDATA에서 한글로 제대로 들어간다. //MS949
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF8"));

                // csv 파일 컬럼명 입력
                bw.write("keyword_seq,influencer_img,influencer_name,influencer_type,influencer_type_detail,fan_count" +
                        ",content_title,content_desc,content_img,content_post_date");
                bw.write(newLine);

                for (int i = 0; i < recommendedInfluencer_elements.get(0).getElementsByAttribute("src").size() ; i++) {

                    Element recommentElement_i = recommendedInfluencer_info_elements.get(i);
                    Element element_detail_box = recommentElement_i.getElementsByClass("detail_box").get(0);

                    String influencer_img = recommendedInfluencer_profile_elements.get(0).getElementsByClass("thumb").get(i).getElementsByTag("img").attr("src");
                    String influencer_name = recommentElement_i.getElementsByClass("user_area").get(0).getElementsByClass("txt").text();
                    String influencer_type = recommentElement_i.getElementsByClass("etc highlight").get(0).text();
                    String influencer_type_detail = recommentElement_i.getElementsByClass("etc").next().first().text();

                    // type 값 없을 경우 빈 문자열 처리
                    if(recommentElement_i.getElementsByClass("etc").next().first().text().charAt(0) == '팬')
                        influencer_type_detail = "";

                    String fan_count = recommentElement_i.getElementsByClass("etc").last().getElementsByClass("_fan_count").text();
                    String content_title = element_detail_box.getElementsByClass("name_link").text();
                    String content_desc = element_detail_box.getElementsByClass("dsc_link").text();
                    String content_img = element_detail_box.getElementsByTag("img").attr("data-lazysrc");
                    String content_post_date = element_detail_box.getElementsByClass("date").text();


                    // , 을 빈 문자열로 처리
                    fan_count = fan_count.replaceAll(",","");
                    content_title = content_title.replaceAll(",","");
                    content_desc = content_desc.replaceAll(",","");

                    // "n일 전"으로 나오는 날짜 dateFormat 으로 변환
                   
                    if(content_post_date.endsWith("일 전")){
                        int day = Integer.parseInt(content_post_date.substring(0,1));
                        content_post_date = LocalDateTime.now().minusDays(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    } else if(content_post_date.endsWith("시간 전")) {
                        content_post_date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    }
                    //System.out.println("content_post_date : " + content_post_date);

                    bw.write(selectedKeyword.getSeq() +","+ influencer_img+","+influencer_name +","+influencer_type
                            +","+ influencer_type_detail +","+ fan_count +","+ content_title
                            +","+content_desc +","+content_img +","+content_post_date);
                    bw.write(newLine);
                    bw.flush();



                }


                // 로그 남기기
                logger.info(keyword + " recommanded_influencer csv 파일 생성 완료");


            } catch (Exception e) {
                e.getStackTrace();
                logger.error(e);
                logger.error(keyword + " recommanded_influencer csv 파일 생성 실패");
                updateKeywordStatus(9,keywordVO);
                throw e;
            } finally {
                if (bw != null) {
                    try {
                        bw.flush();
                        bw.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.error(e.getMessage());
                        throw e;

                    }
                }
            }
            // recommanded_influencer DB 입력
            try {

                crawlingService.addRecommandedInfluencer(filePath);
                logger.info(keyword + " recommanded_influencer csv 파일 DB 입력 완료");

            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage());
                logger.error(keyword + " recommanded_influencer csv파일 DB 입력 실패");
                updateKeywordStatus(9,keywordVO);
                throw e;
            }

            randomSleep();
            // recommanded_influencer DB 입력 완료

            // 4. influencer_content 시작


            Elements list_option_filter_element = document.getElementsByClass("list_option_filter");
            Elements influencer_content_element = document.getElementsByClass("keyword_challenge_list _inf_contents").first().getElementsByClass("keyword_bx _item _check_visible");

            filePath = "D:/devfolder/csv/naver_crawling/influencer_content/influencer_content" +keyword + "_" +todayDate+ ".csv";

            try {

                file = new File(filePath);
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF8"));

                // influencer_content 테이블 컬럼 입력
                bw.write("keyword_seq,content_id,channel_type,influencer_image,influencer_name,influencer_type" +
                        ",influencer_type_detail,fan_count,sns_follower_desc,influencer_home_url,content_url" +
                        ",influencer_info1_title,influencer_info2_title,influencer_info3_title" +
                        ",influencer_info1_desc,influencer_info2_desc,influencer_info3_desc" +
                        ",content_title,content_desc,content_post_date");
                bw.write(newLine);

                String channel_type = list_option_filter_element.get(0).getElementsByAttributeValue("aria-selected", "true").text();


                for (Element element : influencer_content_element) {


                    String influencer_info1_title = "";
                    String influencer_info2_title = "";
                    String influencer_info3_title = "";
                    String influencer_info1_desc = "";
                    String influencer_info2_desc = "";
                    String influencer_info3_desc = "";


                    Elements etc_element = element.getElementsByClass("user_info_bx").get(0).getElementsByClass("etc_area").get(0).getElementsByClass("etc");
                    Element dsc_area_element = element.getElementsByClass("dsc_area").get(0);

                    String influencer_image = element.getElementsByClass("user_thumb").get(0).getElementsByTag("img").attr("data-lazysrc");
                    String influencer_home_url = element.getElementsByClass("user_thumb_wrap").get(0).attr("href");
                    String content_title = dsc_area_element.getElementsByClass("name_link _foryou_trigger").text();
                    String influencer_name = element.getElementsByClass("user_info_bx").get(0).getElementsByClass("user_area").text();
                    String influencer_type = element.getElementsByClass("user_info_bx").get(0).getElementsByClass("etc_area").get(0).getElementsByClass("etc highlight").text();
                    String influencer_type_detail = etc_element.get(1).text();
                    String fan_count = etc_element.last().getElementsByClass("_fan_count").text();
                    String sns_follower_desc = element.getElementsByClass("keyword type_catch").text();
                    String content_desc = dsc_area_element.getElementsByClass("dsc_link _foryou_trigger").text();
                    String content_post_date = dsc_area_element.getElementsByClass("date").text();
                    String content_url = dsc_area_element.getElementsByClass("name_link").attr("href");
                   

                    //int a = content_url.indexOf("/internal/");
                    //int b  = content_url.lastIndexOf("?query");
                    String content_id = "";
                    //System.out.println(a +" : "+ b);
                    content_id = content_url.substring(content_url.indexOf("/internal/")+10, content_url.lastIndexOf("?query"));

                    //System.out.println(content_id);
                    // "n일 전", "n시간 전"으로 나오는 날짜 dateFormat 으로 변환
                    if(content_post_date.endsWith("일 전")){
                        int day = Integer.parseInt(content_post_date.substring(0,1));
                        content_post_date = LocalDateTime.now().minusDays(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    } else if(content_post_date.endsWith("시간 전")) {
                        content_post_date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    }

                    // type 값 없을 경우 빈 문자열 처리
                    if(influencer_type_detail.charAt(0) == '팬') {
                        influencer_type_detail = "";
                    }

                    // 쉼표 처리
                    sns_follower_desc = sns_follower_desc.replaceAll(",","");
                    fan_count = fan_count.replaceAll(",","");
                    content_title = content_title.replaceAll(",","");
                    content_desc = content_desc.replaceAll(",","");


                    if (!element.getElementsByClass("dsc_bx").isEmpty()){
                        influencer_info1_title = element.getElementsByClass("dsc_inner").get(0).getElementsByClass("tit").text();
                        influencer_info2_title = element.getElementsByClass("dsc_inner").get(1).getElementsByClass("tit").text();
                        influencer_info3_title = element.getElementsByClass("dsc_inner").get(2).getElementsByClass("tit").text();
                        influencer_info1_desc = element.getElementsByClass("dsc_inner").get(0).getElementsByClass("txt").text();
                        influencer_info2_desc = element.getElementsByClass("dsc_inner").get(1).getElementsByClass("txt").text();
                        influencer_info3_desc = element.getElementsByClass("dsc_inner").get(2).getElementsByClass("txt").text();
                    }

                    bw.write(selectedKeyword.getSeq()+","+content_id+","+channel_type +","+influencer_image +","+influencer_name +","+ influencer_type
                            +","+influencer_type_detail +","+fan_count +","+sns_follower_desc +","+influencer_home_url+","+content_url
                            +","+influencer_info1_title +","+influencer_info2_title+","+influencer_info3_title
                            +","+influencer_info1_desc +","+influencer_info2_desc +","+ influencer_info3_desc
                            +","+content_title +","+content_desc +","+content_post_date);

                    bw.write(newLine);
                    bw.flush();

                }
                // 로그 남기기
                logger.info(keyword + " influencer_content csv 파일 생성 완료");

            } catch (Exception e) {
                e.getStackTrace();
                logger.error(e);
                logger.error(keyword + " influencer_content csv 파일 생성 실패");
                updateKeywordStatus(9,keywordVO);
                throw e;
            } finally {
                if (bw != null) {
                    try {
                        bw.flush();
                        bw.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.error(e.getMessage());
                        throw e;

                    }
                }
            }
            // influencer_content DB입력
            try {

                crawlingService.addInfluencerContent(filePath);
                logger.info(keyword + " influencer_content csv 파일 DB 입력 완료");

            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage());
                logger.error(keyword + " influencer_content csv파일 DB 입력 실패");
                updateKeywordStatus(9,keywordVO);

                // 에러발생시 중단시키기
                throw e;
            }
            randomSleep();
            // influencer_content DB 입력완료

            // 5. influencer_content_image 시작

            String content_image_url = "";

            // 수집한 데이터로 csv 파일 만들기
            filePath = "D:/devfolder/csv/naver_crawling/influencer_content_image/influencer_content_image" +keyword + "_" +todayDate+ ".csv";

            try {

                file = new File(filePath);
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF8"));

                bw.write("keyword_seq,content_id,content_image_url");
                bw.write(newLine);


                for (Element element : influencer_content_element) {

                    Elements content_img_elements = element.getElementsByClass("thumb _foryou_trigger");
                    Element dsc_area_element = element.getElementsByClass("dsc_area").get(0);
                    String content_url = dsc_area_element.getElementsByClass("name_link").attr("href");
                    String content_id = content_url.substring(content_url.indexOf("/internal/")+10, content_url.lastIndexOf("?query"));

                    for (Element element2 : content_img_elements) {

                        //System.out.println(content_img_elements.get(j).getElementsByTag("img").attr("src"));
                        content_image_url = element2.getElementsByTag("img").attr("src");
                        bw.write(selectedKeyword.getSeq()+","+content_id+","+content_image_url);
                        bw.write(newLine);
                        bw.flush();
                    }

                }
                // 로그 남기기
                logger.info(keyword + " influencer_content_image csv 파일 생성 완료");

            } catch (Exception e) {
                e.getStackTrace();
                logger.error(e);
                logger.error(keyword + " influencer_content_image csv 파일 생성 실패");
                updateKeywordStatus(9,keywordVO);
                // 에러발생시 중단시키기
                throw e;
            } finally {
                if (bw != null) {
                    try {
                        bw.flush();
                        //bw.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.error(e.getMessage());
                        throw e;

                    }
                }
            }
            // influencer_content_image DB 입력
            try {

                crawlingService.addInfluencerContentImage(filePath);
                logger.info(keyword + " influencer_content_image csv 파일 DB 입력 완료");

            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage());
                logger.error(keyword + " influencer_content_image csv파일 DB 입력 실패");
                updateKeywordStatus(9,keywordVO);

                // 에러발생시 중단시키기
                throw e;
            }
            randomSleep();
            // influencer_content_image DB 입력 완료


            // 6. related_content 수집 시작

            String related_content_url= "";
            String content_title = "";

            // 수집한 데이터로 csv 파일 만들기
            filePath = "D:/devfolder/csv/naver_crawling/related_content/related_content" +keyword + "_" +todayDate+ ".csv";

            try {

                file = new File(filePath);
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF8"));

                bw.write("keyword_seq,content_id,content_url,content_title");
                bw.write(newLine);


                for (Element element : influencer_content_element) {

                    Element dsc_area_element = element.getElementsByClass("dsc_area").get(0);
                    String content_url = dsc_area_element.getElementsByClass("name_link").attr("href");
                    String content_id = content_url.substring(content_url.indexOf("/internal/")+10, content_url.lastIndexOf("?query"));


                    Elements related_elements = element.getElementsByClass("link _foryou_linkbox_trigger");
                    for (Element related_element : related_elements) {

                        content_title = related_element.getElementsByClass("name").text();
                        related_content_url = related_element.getElementsByAttribute("href").attr("href");

                        bw.write(selectedKeyword.getSeq()+","+content_id +","+related_content_url +","+ content_title.replaceAll(",",""));
                        bw.write(newLine);
                        bw.flush();
                    }
                }

                logger.info(keyword + " related_content csv 파일 생성 완료");

            } catch (Exception e) {
                e.getStackTrace();
                logger.error(e);
                logger.error(keyword + " related_content csv 파일 생성 실패");
                updateKeywordStatus(9,keywordVO);
                throw e;
            } finally {
                if (bw != null) {
                    try {
                        bw.flush();
                        bw.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.error(e.getMessage());
                        throw e;

                    }
                }
            }
            // related_content DB 입력
            try {

                crawlingService.addRelatedContent(filePath);
                logger.info(keyword + " related_content csv 파일 DB 입력 완료");

            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage());
                logger.error(keyword + " related_content csv파일 DB 입력 실패");
                updateKeywordStatus(9,keywordVO);

                // 에러발생시 중단시키기
                throw e;
            }
            finally {

                // keyword 테이블 status 1로 update
                bw.close();
                updateKeywordStatus(1,keywordVO);

            }
            //related_content DB 입력 완료

            // 7. popular_topic

            String topic= "";
            String topic_image_url = "";
            //String topic_url = "";
            String topic_count = "";

            filePath = "D:/devfolder/csv/naver_crawling/popular_topic/popular_topic"+keyword + "_" +todayDate+ ".csv";

            try {

                file = new File(filePath);
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF8"));

                bw.write("keyword_seq,topic,topic_image_url,topic_count");
                bw.write(newLine);
                bw.flush();

                Elements popular_elements = document.getElementsByClass("keyword_bx _item_popular");

                if(popular_elements.isEmpty()){
                    //System.out.println("empty");
                    logger.info(keyword + "인기주제 없음");
                    // 반복문 탈출
                    continue;
                } else {

                    Elements item_elements = popular_elements.get(0).getElementsByClass("item_link_area");

                    for (Element item_element : item_elements) {

                        Elements link_elements = item_element.getElementsByClass("item_link");

                        for (Element link_element : link_elements) {

                            //topic_url = link_element.getElementsByTag("a").attr("href");
                            topic = link_element.getElementsByClass("hash_tag").text();
                            topic_image_url = link_element.getElementsByTag("img").attr("data-lazysrc");
                            topic_count = link_element.getElementsByClass("count").text();

                            topic_count = topic_count.substring(0,topic_count.indexOf("명"));
                            //System.out.println(topic + " : " + topic_count);
                            //System.out.println("url: " + topic_url);
                            //System.out.println(topic + " : " + topic_image_url);
                            bw.write(selectedKeyword.getSeq() +"," +topic+","+topic_image_url+","+ topic_count);
                            bw.write(newLine);
                            bw.flush();

                        }
                    }
                }

                logger.info(keyword + " popular_topic csv 파일 생성 완료");

            } catch (Exception e) {
                e.getStackTrace();
                logger.error(e);
                logger.error(keyword + " influencer_content_image csv 파일 생성 실패");
                updateKeywordStatus(9,keywordVO);
                throw e;
            } finally {
                if (bw != null) {
                    try {
                        bw.flush();
                        bw.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.error(e.getMessage());
                        throw e;
                    }
                }
            }

            // DB 입력
            try {

                crawlingService.addPopularTopic(filePath);
                logger.info(keyword + " popular_topic csv 파일 DB 입력 완료");

            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage());
                logger.error(keyword + " popular_topic csv파일 DB 입력 실패");
                updateKeywordStatus(9,keywordVO);

                // 에러발생시 중단시키기
                throw e;
            }
            finally {

                // keyword 테이블 status 1로 update
                bw.close();
                updateKeywordStatus(1,keywordVO);

            }

        }

        return "index.html";


    }




    // 1~5초 randomSleep 함수
    public void randomSleep() throws Exception {

        int randomCnt = (int)(Math.random()*5 +1) * 1000;
        try {
            Thread.sleep(randomCnt);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // keyword 추가 메소드
    // 테스트 완료
    // url: http://abby-spring.crawling.com/crawling/addKeyword?keyword=%EB%B9%84%EC%8A%A4%ED%8F%AC%ED%81%AC
    //@GetMapping(value="/crawling/addKeyword")
    public String addKeyword(HttpServletRequest request)throws Exception {

        KeywordVO keywordVO = new KeywordVO();
        String keyword = request.getParameter("keyword");

        try {

            final String url = "https://search.naver.com/search.naver?where=influencer&sm=tab_jum&query=%23"+keyword;

            Document document = Jsoup.connect(url).get();
            Elements keywordValue_elements = document.getElementsByClass("box_window");

            String keywordValue = keywordValue_elements.attr("value");

            keywordValue = keywordValue.replaceAll("#", "");

            // keywordvalue(#삭제)랑 같으면 keyword테이블에 입력
            if (keyword.equals(keywordValue)){
                keywordVO.setKeyword(keyword);
                keywordVO.setStatus(1);
                crawlingService.addKeyword(keywordVO);
            }

            logger.info(keyword + " keyword 테이블 DB 입력 완료");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(keyword + " keyword 테이블 DB 입력 실패");
            updateKeywordStatus(9,keywordVO);
            throw e;
        }
        return "index.html";

    }

    public void updateKeywordStatus(int status, KeywordVO keywordVO) throws Exception {
        keywordVO.setStatus(status);
        crawlingService.updateKeyword(keywordVO);
    }

   

}
