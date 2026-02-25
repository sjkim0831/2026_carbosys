package egovframework.com.uss.umt.service;

import java.io.Serializable;

/**
 * 회원사(기관) 정보 VO 클래스
 */
public class InsttInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 기관 ID */
    private String insttId;
    /** 기관 명 */
    private String insttNm;
    /** 대표자 명 */
    private String reprsntNm;
    /** 사업자번호 */
    private String bizrno;
    /** 우편번호 */
    private String zip;
    /** 주소 */
    private String adres;
    /** 상세주소 */
    private String detailAdres;
    /** 사업자등록증 파일 경로 */
    private String bizRegFilePath;
    /** 기관 상태 */
    private String insttSttus;
    /** 최초 등록 시점 */
    private String frstRegistPnttm;
    /** 최종 수정 시점 */
    private String lastUpdtPnttm;

    public String getInsttId() {
        return insttId;
    }

    public void setInsttId(String insttId) {
        this.insttId = insttId;
    }

    public String getInsttNm() {
        return insttNm;
    }

    public void setInsttNm(String insttNm) {
        this.insttNm = insttNm;
    }

    public String getReprsntNm() {
        return reprsntNm;
    }

    public void setReprsntNm(String reprsntNm) {
        this.reprsntNm = reprsntNm;
    }

    public String getBizrno() {
        return bizrno;
    }

    public void setBizrno(String bizrno) {
        this.bizrno = bizrno;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getAdres() {
        return adres;
    }

    public void setAdres(String adres) {
        this.adres = adres;
    }

    public String getDetailAdres() {
        return detailAdres;
    }

    public void setDetailAdres(String detailAdres) {
        this.detailAdres = detailAdres;
    }

    public String getBizRegFilePath() {
        return bizRegFilePath;
    }

    public void setBizRegFilePath(String bizRegFilePath) {
        this.bizRegFilePath = bizRegFilePath;
    }

    public String getInsttSttus() {
        return insttSttus;
    }

    public void setInsttSttus(String insttSttus) {
        this.insttSttus = insttSttus;
    }

    public String getFrstRegistPnttm() {
        return frstRegistPnttm;
    }

    public void setFrstRegistPnttm(String frstRegistPnttm) {
        this.frstRegistPnttm = frstRegistPnttm;
    }

    public String getLastUpdtPnttm() {
        return lastUpdtPnttm;
    }

    public void setLastUpdtPnttm(String lastUpdtPnttm) {
        this.lastUpdtPnttm = lastUpdtPnttm;
    }
}
