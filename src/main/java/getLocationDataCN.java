import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Iterator;

/**
 * 抓取中国省市区乡镇数据
 * 数据来源：2016年统计用区划代码和城乡划分代码(截止2016年07月31日) http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2016/
 * Created by jcl on 2018/3/5
 */
public class getLocationDataCN {
    public static void main(String[] args) throws Exception {
        long t0 = System.currentTimeMillis();
        String baseUrl = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2016/";
        Document provinceDoc = getDoc(5, baseUrl);
        Elements provinceTrs = provinceDoc.getElementsByClass("provincetr");
        Iterator<Element> iterator = provinceTrs.iterator();
        while (iterator.hasNext()) {
            Elements provinceTds = iterator.next().children();
            Iterator<Element> iterator1 = provinceTds.iterator();
            while (iterator1.hasNext()) {
                Element provinceTd = iterator1.next();
                Element aTag = provinceTd.getElementsByTag("a").first();
                String provinceHref = aTag.attr("href");
                String provinceName = aTag.text();
                Document cityDoc = getDoc(5, baseUrl + provinceHref);
                Elements cityTrs = cityDoc.getElementsByClass("citytr");
                Iterator<Element> iterator2 = cityTrs.iterator();
                while (iterator2.hasNext()) {
                    Element cityTr = iterator2.next();
                    Elements cityTds = cityTr.children();
                    Elements cityATags = cityTds.get(0).getElementsByTag("a");
                    String cityGbCode = null;
                    String cityHref = null;
                    String cityName = null;
                    if (cityATags != null && cityATags.size() > 0) {
                        cityGbCode = cityATags.first().text();
                    }
                    cityATags = cityTds.get(1).getElementsByTag("a");
                    if (cityATags != null && cityATags.size() > 0) {
                        cityHref = cityATags.first().attr("href");
                        cityName = cityATags.first().text();
                    }
                    if (cityHref != null) {
                        Document areaDoc = getDoc(5, baseUrl + cityHref);
                        Elements areaTrs = areaDoc.getElementsByClass("countytr");
                        Iterator<Element> iterator3 = areaTrs.iterator();
                        while (iterator3.hasNext()) {
                            Element areaTr = iterator3.next();
                            Elements areaTds = areaTr.children();
                            Elements areaATags = areaTds.get(0).getElementsByTag("a");
                            String areaGbCode = null;
                            String areaHref = null;
                            String areaName = null;
                            if (areaATags != null && areaATags.size() > 0) {
                                areaGbCode = areaATags.first().text();
                            }
                            areaATags = areaTds.get(1).getElementsByTag("a");
                            if (areaATags != null && areaATags.size() > 0) {
                                areaHref = areaATags.first().attr("href");
                                areaName = areaATags.first().text();
                            }
                            if (areaHref != null) {
                                Document townDoc = getDoc(5, baseUrl + provinceHref.replaceAll(".html", "") + "/" + areaHref);
                                Elements townTrs = townDoc.getElementsByClass("towntr");
                                Iterator<Element> iterator4 = townTrs.iterator();
                                while (iterator4.hasNext()) {
                                    Element townTr = iterator4.next();
                                    Elements townTds = townTr.children();
                                    Elements townATags = townTds.get(0).getElementsByTag("a");
                                    String townGbCode = null;
                                    String townHref = null;
                                    String townName = null;
                                    if (townATags != null && townATags.size() > 0) {
                                        townGbCode = townATags.first().text();
                                    }
                                    townATags = townTds.get(1).getElementsByTag("a");
                                    if (townATags != null && townATags.size() > 0) {
                                        townHref = townATags.first().attr("href");
                                        townName = townATags.first().text();
                                    }
                                    if (townHref != null) {
                                        String subUrl = cityHref.substring(cityHref.indexOf("/") + 3, cityHref.indexOf("."));
                                        String url = baseUrl + provinceHref.replaceAll(".html", "") + "/" + subUrl + "/" + townHref;
                                        Document villageDoc = getDoc(5, url);
                                        Elements villageTrs = villageDoc.getElementsByClass("villagetr");
                                        Iterator<Element> iterator5 = villageTrs.iterator();
                                        while (iterator5.hasNext()) {
                                            Element villageTr = iterator5.next();
                                            Elements villageTds = villageTr.children();
                                            String villageGbCode = villageTds.get(0).text();
                                            String villageName = villageTds.get(2).text();
                                            System.out.println(provinceName + "-" + cityName + "-" + areaName + "-" + townName + "-" + villageName);//乡镇/街道
                                        }
                                        System.out.println();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println("共耗时:" + (t1 - t0) / 1000 / 60 + " min");
    }

    static Document getDoc(int retryCount, String url)throws Exception {
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
