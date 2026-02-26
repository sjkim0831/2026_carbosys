package egovframework.com.uss.umt.service.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import egovframework.com.cmm.service.impl.EgovComAbstractDAO;
import egovframework.com.uss.umt.service.EntrprsManageVO;
import egovframework.com.uss.umt.service.InsttInfoVO;
import egovframework.com.uss.umt.service.StplatVO;
import egovframework.com.uss.umt.service.UserDefaultVO;

/**
 * 기업회원관리에 관한 데이터 접근 클래스를 정의한다.
 * 
 * @author 공통서비스 개발팀 조재영
 * @since 2009.04.10
 * @version 1.0
 * @see
 *
 *      <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.04.10  조재영          최초 생성
 *   2017.07.21  장동한 			로그인인증제한 작업
 *
 *      </pre>
 */
@Repository("entrprsManageDAO")
public class EntrprsManageDAO extends EgovComAbstractDAO {

    /**
     * 화면에 조회된 기업회원의 정보를 데이터베이스에서 삭제
     * 
     * @param delId
     */
    public void deleteEntrprsmber(String delId) {
        delete("entrprsManageDAO.deleteEntrprs_S", delId);
    }

    /**
     * 기업회원의 기본정보를 화면에서 입력하여 항목의 정합성을 체크하고 데이터베이스에 저장
     * 
     * @param entrprsManageVO 기업회원 등록정보
     * @return String 등록결과
     */
    public String insertEntrprsmber(EntrprsManageVO entrprsManageVO) {
        return String.valueOf((int) insert("entrprsManageDAO.insertEntrprs_S", entrprsManageVO));
    }

    /**
     * 기 등록된 사용자 중 검색조건에 맞는 기업회원의 정보를 데이터베이스에서 읽어와 화면에 출력
     * 
     * @param entrprsmberId 상세조회대상 기업회원아이디
     * @return EntrprsManageVO 기업회원 상세정보
     */
    public EntrprsManageVO selectEntrprsmber(String entrprsmberId) {
        return (EntrprsManageVO) selectOne("entrprsManageDAO.selectEntrprs_S", entrprsmberId);
    }

    public List<?> searchCompanyList(String searchKeyword) {
        return selectList("entrprsManageDAO.searchCompanyList", searchKeyword);
    }

    public List<?> searchCompanyListPaged(java.util.Map<String, Object> params) {
        return selectList("entrprsManageDAO.searchCompanyListPaged", params);
    }

    public int searchCompanyListTotCnt(java.util.Map<String, Object> params) {
        Object result = selectOne("entrprsManageDAO.searchCompanyListTotCnt", params);
        return result == null ? 0 : ((Number) result).intValue();
    }

    /**
     * 화면에 조회된 사용자의 기본정보를 수정하여 항목의 정합성을 체크하고 수정된 데이터를 데이터베이스에 반영
     * 
     * @param entrprsManageVO 기업회원 수정정보
     */
    public void updateEntrprsmber(EntrprsManageVO entrprsManageVO) {
        update("entrprsManageDAO.updateEntrprs_S", entrprsManageVO);
    }

    /**
     * 약관정보를 조회
     * 
     * @param stplatId 기업회원 약관아이디
     * @return List 기업회원약관정보
     */
    public List<StplatVO> selectStplat(String stplatId) {
        return selectList("entrprsManageDAO.selectStplat_S", stplatId);
    }

    /**
     * 기업회원 암호수정
     * 
     * @param passVO 기업회원수정정보(비밀번호)
     */
    public void updatePassword(EntrprsManageVO passVO) {
        update("entrprsManageDAO.updatePassword_S", passVO);
    }

    /**
     * 기업회원이 비밀번호를 기억하지 못할 때 비밀번호를 찾을 수 있도록 함
     * 
     * @param entrprsManageVO 기업회원암호 조회조건정보
     * @return EntrprsManageVO 기업회원암호정보
     */
    public EntrprsManageVO selectPassword(EntrprsManageVO entrprsManageVO) {
        return (EntrprsManageVO) selectOne("entrprsManageDAO.selectPassword_S", entrprsManageVO);
    }

    /**
     * 기 등록된 특정 기업회원의 정보를 데이터베이스에서 읽어와 화면에 출력
     * 
     * @param userSearchVO 검색조건
     * @return List<EntrprsManageVO>
     */
    public List<EntrprsManageVO> selectEntrprsMberList(UserDefaultVO userSearchVO) {
        return selectList("entrprsManageDAO.selectEntrprsMberList", userSearchVO);
    }

    /**
     * 기업회원 총 개수를 조회한다.
     * 
     * @param userSearchVO 검색조건
     * @return int 기업회원총개수
     */
    public int selectEntrprsMberListTotCnt(UserDefaultVO userSearchVO) {
        return (Integer) selectOne("entrprsManageDAO.selectEntrprsMberListTotCnt", userSearchVO);
    }

    /**
     * 로그인인증제한 해제
     * 
     * @param entrprsManageVO 기업회원정보
     */
    public void updateLockIncorrect(EntrprsManageVO entrprsManageVO) {
        update("entrprsManageDAO.updateLockIncorrect", entrprsManageVO);
    }

    /**
     * 입력한 사용자아이디의 중복여부를 체크하여 사용가능여부를 확인
     * 
     * @param checkId 중복체크대상 아이디
     * @return int 사용가능여부(아이디 사용회수 )
     */
    public int checkIdDplct(String checkId) {
        return (Integer) selectOne("entrprsManageDAO.checkIdDplct_S", checkId);
    }

    /**
     * 입력한 이메일의 중복여부를 체크하여 사용가능여부를 확인
     * 
     * @param checkEmail 중복체크대상 이메일
     * @return int 중복횟수(0이면 사용가능)
     */
    public int checkEmailDplct(String checkEmail) {
        return (Integer) selectOne("entrprsManageDAO.checkEmailDplct_S", checkEmail);
    }

    /**
     * 회원사(기관) 정보를 등록한다.
     * 
     * @param insttInfoVO 회원사정보
     */
    public void insertInsttInfo(InsttInfoVO insttInfoVO) {
        insert("entrprsManageDAO.insertInsttInfo", insttInfoVO);
    }

    /**
     * 입력한 회사명(기관명)의 중복여부를 체크하여 사용가능여부를 확인
     * 
     * @param checkNm 중복체크대상 회사명
     * @return int 중복횟수(0이면 사용가능)
     */
    public int checkCompanyNameDplct(String checkNm) {
        return (Integer) selectOne("entrprsManageDAO.checkCompanyNameDplct", checkNm);
    }
}