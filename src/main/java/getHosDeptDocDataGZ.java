import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Iterator;

/**
 * 抓取科室和医生数据
 * 数据来源：广州市统一预约挂号系统 http://www.guahao.gov.cn
 * Created by jcl on 2018/3/16
 */
@SuppressWarnings("ALL")
public class getHosDeptDocDataGZ {
    private static final String BASE_URL = "http://www.guahao.gov.cn";//首页
    private static final String DEPT_URL = "/deplist.xhtml";//科室列表,参数:HIS_CD
    private static final int[] hosIds = {1051, 1004, 100201, 100203, 100202};//医院id

    public static void main(String[] args) throws Exception {
        long t0 = System.currentTimeMillis();
        System.out.println("begin......");
        int deptCount = 0;
        int doctorCount = 0;
        for (int hosId : hosIds) {
            Document deptDoc = getDoc(5, BASE_URL + DEPT_URL + "?HIS_CD=" + hosId);
            Elements deptUls = deptDoc.getElementsByClass("dept");
            Iterator<Element> ulIter = deptUls.iterator();
            while (ulIter.hasNext()) {
                Elements deptLis = ulIter.next().children();
                Iterator<Element> liIter = deptLis.iterator();
                while (liIter.hasNext()) {
                    Element liChild = liIter.next().children().get(0);
                    String deptName = liChild.attr("title");
                    if (liChild.tagName().equals("a")) {
                        int pageSize = 10;
                        int pageIndex = 1;
                        String docListUrl = liChild.attr("href");
                        Document docListDoc = getDoc(5, BASE_URL + docListUrl);
                        int pages = Integer.valueOf(docListDoc.getElementsByAttributeValue("name", "PAG_CNT").get(0).attr("value"));
                        do {
                            if (pageIndex > 1) {
                                docListDoc = getDoc(5, BASE_URL + docListUrl + "&PAG_NO=" + pageIndex);
                            }
                            Elements docInfoDivs = docListDoc.getElementsByClass("docInfo");
                            Iterator<Element> docIter = docInfoDivs.iterator();
                            while (docIter.hasNext()) {
                                Elements docInfo = docIter.next().children();
                                String docName = docInfo.get(0).children().get(0).text();
                                String lczcName = docInfo.get(1).text();
                                String intro = docInfo.get(3).text().replaceFirst("简介：", "");
                                System.out.println("dept：" + deptName + ",doctor：" + docName + ",intro:" + intro);
                                doctorCount++;
                            }
                            pageIndex++;
                        } while (pageIndex <= pages);
                    }
                    deptCount++;
                }
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println("end......cost:" + (t1 - t0) + "ms,deptCount:" + deptCount + ",doctorCount:" + doctorCount);
    }

    static Document getDoc(int retryCount, String url) throws Exception {
        while (retryCount > 0) {
            try {
                return Jsoup.connect(url).get();
            } catch (Exception e) {
                retryCount--;
                getDoc(retryCount, url);
            }
        }
        throw new Exception("获取文档失败，重试次数已用完");
    }
}
