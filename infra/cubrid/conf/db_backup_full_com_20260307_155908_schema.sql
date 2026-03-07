call [find_user]('PUBLIC') on class [db_user] to [g_public];


CREATE CLASS [dba].[comtnroleinfo] REUSE_OID, COLLATE utf8_bin COMMENT '롤정보';

CREATE CLASS [dba].[comtnauthorrolerelate] REUSE_OID, COLLATE utf8_bin COMMENT '권한롤관계';

CREATE CLASS [dba].[comtnbbsmaster] REUSE_OID, COLLATE utf8_bin COMMENT '게시판마스터';

CREATE CLASS [dba].[comtnbloguser] REUSE_OID, COLLATE utf8_bin COMMENT '블로그사용자';

CREATE CLASS [dba].[msatnroleinfo] REUSE_OID, COLLATE utf8_bin COMMENT '롤정보';

CREATE CLASS [dba].[msatnauthorrolerelate] REUSE_OID, COLLATE utf8_bin COMMENT '권한롤관계';

CREATE CLASS [dba].[msatnroles_hierarchy] REUSE_OID, COLLATE utf8_bin COMMENT '롤 계층구조';

CREATE CLASS [dba].[msatnemplyrscrtyestbs] REUSE_OID, COLLATE utf8_bin COMMENT '사용자보안설정';

CREATE CLASS [dba].[msatnmenucreatdtls] REUSE_OID, COLLATE utf8_bin COMMENT '메뉴생성내역';

CREATE CLASS [dba].[comtnloginpolicy] REUSE_OID, COLLATE utf8_bin COMMENT '로그인정책';

CREATE CLASS [dba].[comtnfile] REUSE_OID, COLLATE utf8_bin COMMENT '파일속성';

CREATE CLASS [dba].[comtnfiledetail] REUSE_OID, COLLATE utf8_bin COMMENT '파일상세정보';

CREATE CLASS [dba].[comtnmenucreatdtls] REUSE_OID, COLLATE utf8_bin COMMENT '메뉴생성내역';

CREATE CLASS [dba].[comtnqustnrtmplat] REUSE_OID, COLLATE utf8_bin COMMENT '설문템플릿';

CREATE CLASS [dba].[comtecopseq] REUSE_OID, COLLATE utf8_bin COMMENT 'COMTECOPSEQ';

CREATE CLASS [dba].[comtccmmnclcode] REUSE_OID, COLLATE utf8_bin COMMENT '공통분류코드';

CREATE CLASS [dba].[comtccmmncode] REUSE_OID, COLLATE utf8_bin COMMENT '공통코드';

CREATE CLASS [dba].[comtccmmndetailcode] REUSE_OID, COLLATE utf8_bin COMMENT '공통상세코드';

CREATE CLASS [dba].[comtnauthorgroupinfo] REUSE_OID, COLLATE utf8_bin COMMENT '권한그룹정보';

CREATE CLASS [dba].[comtnauthorinfo] REUSE_OID, COLLATE utf8_bin COMMENT '권한정보';

CREATE CLASS [dba].[comtnbbs] REUSE_OID, COLLATE utf8_bin COMMENT '게시판';

CREATE CLASS [dba].[comtnbbsmasteroptn] REUSE_OID, COLLATE utf8_bin COMMENT '게시판마스터옵션';

CREATE CLASS [dba].[comtnbbssynclog] REUSE_OID, COLLATE utf8_bin COMMENT '동기화이력로그';

CREATE CLASS [dba].[comtnblog] REUSE_OID, COLLATE utf8_bin COMMENT '블로그게시판';

CREATE CLASS [dba].[comtncmmnty] REUSE_OID, COLLATE utf8_bin COMMENT '커뮤니티 속성';

CREATE CLASS [dba].[comtncmmntyuser] REUSE_OID, COLLATE utf8_bin COMMENT '커뮤니티사용자';

CREATE CLASS [dba].[comtncomment] REUSE_OID, COLLATE utf8_bin COMMENT '댓글';

CREATE VCLASS [dba].[comvnusermaster];

CREATE CLASS [dba].[comtnemplyrinfo] REUSE_OID, COLLATE utf8_bin COMMENT '업무사용자정보';

CREATE CLASS [dba].[comtnemplyrscrtyestbs] REUSE_OID, COLLATE utf8_bin COMMENT '사용자보안설정';

CREATE CLASS [dba].[comtnqestnrinfo] REUSE_OID, COLLATE utf8_bin COMMENT '설문지정보';

CREATE CLASS [dba].[comtnentrprsmber] REUSE_OID, COLLATE utf8_bin COMMENT '기업회원';

CREATE CLASS [dba].[comtngnrlmber] REUSE_OID, COLLATE utf8_bin COMMENT '일반회원';

CREATE CLASS [dba].[comtnqustnrqesitm] REUSE_OID, COLLATE utf8_bin COMMENT '설문문항';

CREATE CLASS [dba].[comtnqustnriem] REUSE_OID, COLLATE utf8_bin COMMENT '설문항목';

CREATE CLASS [dba].[comtnqustnrrespondinfo] REUSE_OID, COLLATE utf8_bin COMMENT '설문응답자정보';

CREATE CLASS [dba].[comtnqustnrrspnsresult] REUSE_OID, COLLATE utf8_bin COMMENT '설문응답결과';

CREATE CLASS [dba].[comtnstsfdg] REUSE_OID, COLLATE utf8_bin COMMENT '만족도';

CREATE CLASS [dba].[comtntmplatinfo] REUSE_OID, COLLATE utf8_bin COMMENT '템플릿';

CREATE CLASS [dba].[msatnauthorgroupinfo] REUSE_OID, COLLATE utf8_bin COMMENT '권한그룹정보';

CREATE CLASS [dba].[msatnauthorinfo] REUSE_OID, COLLATE utf8_bin COMMENT '권한정보';

CREATE CLASS [dba].[comtninsttinfo] REUSE_OID, COLLATE utf8_bin;

CREATE CLASS [dba].[comtninsttinfo_bak_20260302_212335] REUSE_OID, COLLATE utf8_bin;

CREATE CLASS [dba].[comtnentrprsmberfile] REUSE_OID, COLLATE utf8_bin;

CREATE CLASS [dba].[comtninsttfile] REUSE_OID, COLLATE utf8_bin;




ALTER CLASS [dba].[comtnroleinfo] ADD ATTRIBUTE
       [role_code] character varying(50) COLLATE utf8_bin NOT NULL COMMENT '롤코드',
       [role_nm] character varying(60) COLLATE utf8_bin NOT NULL COMMENT '롤명',
       [role_pttrn] character varying(300) COLLATE utf8_bin COMMENT '롤패턴',
       [role_dc] character varying(200) COLLATE utf8_bin COMMENT '롤설명',
       [role_ty] character varying(80) COLLATE utf8_bin COMMENT '롤유형',
       [role_sort] character varying(10) COLLATE utf8_bin COMMENT '롤정렬',
       [role_creat_de] character(20) COLLATE utf8_bin NOT NULL COMMENT '롤생성일';
ALTER CLASS [dba].[comtnroleinfo] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnroleinfo_role_code] PRIMARY KEY([role_code]),
       CONSTRAINT [comtnroleinfo_pk] UNIQUE([role_code]);

ALTER CLASS [dba].[comtnauthorrolerelate] ADD ATTRIBUTE
       [author_code] character varying(30) COLLATE utf8_bin NOT NULL COMMENT '권한코드',
       [role_code] character varying(50) COLLATE utf8_bin NOT NULL COMMENT '롤코드',
       [creat_dt] datetime COMMENT '생성일';
ALTER CLASS [dba].[comtnauthorrolerelate] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnauthorrolerelate_author_code_role_code] PRIMARY KEY([author_code], [role_code]),
       CONSTRAINT [comtnauthorrolerelate_pk] UNIQUE([author_code], [role_code]);

ALTER CLASS [dba].[comtnbbsmaster] ADD ATTRIBUTE
       [bbs_id] character(30) COLLATE utf8_bin NOT NULL COMMENT '게시판ID',
       [bbs_nm] character varying(255) COLLATE utf8_bin NOT NULL COMMENT '게시판명',
       [bbs_intrcn] character varying(2400) COLLATE utf8_bin COMMENT '게시판소개',
       [bbs_ty_code] character(6) COLLATE utf8_bin NOT NULL COMMENT '게시판유형코드',
       [reply_posbl_at] character(1) COLLATE utf8_bin COMMENT '답장가능여부',
       [file_atch_posbl_at] character(1) COLLATE utf8_bin NOT NULL COMMENT '파일첨부가능여부',
       [atch_posble_file_number] numeric(2,0) NOT NULL,
       [atch_posble_file_size] numeric(8,0),
       [use_at] character(1) COLLATE utf8_bin NOT NULL COMMENT '사용여부',
       [tmplat_id] character(20) COLLATE utf8_bin COMMENT '템플릿ID',
       [cmmnty_id] character(20) COLLATE utf8_bin,
       [frst_register_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '최초등록자ID',
       [frst_regist_pnttm] datetime NOT NULL COMMENT '최초등록시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID',
       [last_updt_pnttm] datetime COMMENT '최종수정시점',
       [blog_id] character(20) COLLATE utf8_bin,
       [blog_at] character(2) COLLATE utf8_bin;
ALTER CLASS [dba].[comtnbbsmaster] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnbbsmaster_bbs_id] PRIMARY KEY([bbs_id]),
       CONSTRAINT [comtnbbsmaster_pk] UNIQUE([bbs_id]);

ALTER CLASS [dba].[comtnbloguser] ADD ATTRIBUTE
       [blog_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '블로그ID',
       [emplyr_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '업무사용자ID',
       [mngr_at] character(1) COLLATE utf8_bin NOT NULL COMMENT '관리자여부',
       [mber_sttus] character varying(15) COLLATE utf8_bin COMMENT '회원상태',
       [sbscrb_de] datetime COMMENT '가입일',
       [secsn_de] character(20) COLLATE utf8_bin COMMENT '탈퇴일',
       [use_at] character(1) COLLATE utf8_bin COMMENT '사용여부',
       [frst_regist_pnttm] datetime NOT NULL COMMENT '최초등록시점',
       [frst_register_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '최초등록자ID',
       [last_updt_pnttm] datetime COMMENT '최종수정시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID';
ALTER CLASS [dba].[comtnbloguser] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnbloguser_blog_id_emplyr_id] PRIMARY KEY([blog_id], [emplyr_id]);

ALTER CLASS [dba].[msatnroleinfo] ADD ATTRIBUTE
       [role_code] character varying(50) COLLATE utf8_bin NOT NULL,
       [role_nm] character varying(60) COLLATE utf8_bin NOT NULL,
       [role_pttrn] character varying(300) COLLATE utf8_bin,
       [role_dc] character varying(200) COLLATE utf8_bin,
       [role_ty] character varying(80) COLLATE utf8_bin,
       [role_sort] character varying(10) COLLATE utf8_bin,
       [role_creat_de] character(20) COLLATE utf8_bin NOT NULL;
ALTER CLASS [dba].[msatnroleinfo] ADD ATTRIBUTE
       CONSTRAINT [pk_msatnroleinfo_role_code] PRIMARY KEY([role_code]),
       CONSTRAINT [msatnroleinfo_pk] UNIQUE([role_code]);

ALTER CLASS [dba].[msatnauthorrolerelate] ADD ATTRIBUTE
       [author_code] character varying(30) COLLATE utf8_bin NOT NULL,
       [role_code] character varying(50) COLLATE utf8_bin NOT NULL,
       [creat_dt] datetime;
ALTER CLASS [dba].[msatnauthorrolerelate] ADD ATTRIBUTE
       CONSTRAINT [pk_msatnauthorrolerelate_author_code_role_code] PRIMARY KEY([author_code], [role_code]),
       CONSTRAINT [msatnauthorrolerelate_pk] UNIQUE([author_code], [role_code]);

ALTER CLASS [dba].[msatnroles_hierarchy] ADD ATTRIBUTE
       [parnts_role] character varying(30) COLLATE utf8_bin NOT NULL,
       [chldrn_role] character varying(30) COLLATE utf8_bin NOT NULL;
ALTER CLASS [dba].[msatnroles_hierarchy] ADD ATTRIBUTE
       CONSTRAINT [pk_msatnroles_hierarchy_parnts_role_chldrn_role] PRIMARY KEY([parnts_role], [chldrn_role]),
       CONSTRAINT [msatnroles_hierarchy_pk] UNIQUE([parnts_role], [chldrn_role]);

ALTER CLASS [dba].[msatnemplyrscrtyestbs] ADD ATTRIBUTE
       [scrty_dtrmn_trget_id] character varying(20) COLLATE utf8_bin NOT NULL,
       [mber_ty_code] character(5) COLLATE utf8_bin,
       [author_code] character varying(30) COLLATE utf8_bin NOT NULL;
ALTER CLASS [dba].[msatnemplyrscrtyestbs] ADD ATTRIBUTE
       CONSTRAINT [pk_msatnemplyrscrtyestbs_scrty_dtrmn_trget_id] PRIMARY KEY([scrty_dtrmn_trget_id]),
       CONSTRAINT [msatnemplyrscrtyestbs_pk] UNIQUE([scrty_dtrmn_trget_id]);

ALTER CLASS [dba].[msatnmenucreatdtls] ADD ATTRIBUTE
       [menu_no] numeric(20,0) NOT NULL,
       [author_code] character varying(30) COLLATE utf8_bin NOT NULL,
       [mapng_creat_id] character varying(30) COLLATE utf8_bin;
ALTER CLASS [dba].[msatnmenucreatdtls] ADD ATTRIBUTE
       CONSTRAINT [pk_msatnmenucreatdtls_menu_no_author_code] PRIMARY KEY([menu_no], [author_code]),
       CONSTRAINT [msatnmenucreatdtls_pk] UNIQUE([menu_no], [author_code]);

ALTER CLASS [dba].[comtnloginpolicy] ADD ATTRIBUTE
       [emplyr_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '업무사용자ID',
       [ip_info] character varying(23) COLLATE utf8_bin NOT NULL COMMENT 'IP정보',
       [dplct_perm_at] character(1) COLLATE utf8_bin NOT NULL COMMENT '중복허용여부',
       [lmtt_at] character(1) COLLATE utf8_bin NOT NULL COMMENT '제한여부',
       [frst_register_id] character varying(20) COLLATE utf8_bin COMMENT '최초등록자ID',
       [frst_regist_pnttm] datetime COMMENT '최초등록시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID',
       [last_updt_pnttm] datetime COMMENT '최종수정시점';
ALTER CLASS [dba].[comtnloginpolicy] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnloginpolicy_emplyr_id] PRIMARY KEY([emplyr_id]);

ALTER CLASS [dba].[comtnfile] ADD ATTRIBUTE
       [atch_file_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '첨부파일ID',
       [creat_dt] datetime NOT NULL COMMENT '생성일시',
       [use_at] character(1) COLLATE utf8_bin COMMENT '사용여부';
ALTER CLASS [dba].[comtnfile] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnfile_atch_file_id] PRIMARY KEY([atch_file_id]),
       CONSTRAINT [comtnfile_pk] UNIQUE([atch_file_id]);

ALTER CLASS [dba].[comtnfiledetail] ADD ATTRIBUTE
       [atch_file_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '첨부파일ID',
       [file_sn] numeric(10,0) NOT NULL COMMENT '파일순번',
       [file_stre_cours] character varying(2000) COLLATE utf8_bin NOT NULL COMMENT '파일저장경로',
       [stre_file_nm] character varying(255) COLLATE utf8_bin NOT NULL COMMENT '저장파일명',
       [orignl_file_nm] character varying(255) COLLATE utf8_bin COMMENT '원파일명',
       [file_extsn] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '파일확장자',
       [file_cn] character varying(32000) COLLATE utf8_bin COMMENT '파일내용',
       [file_size] numeric(8,0) COMMENT '파일사이즈';
ALTER CLASS [dba].[comtnfiledetail] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnfiledetail_atch_file_id_file_sn] PRIMARY KEY([atch_file_id], [file_sn]),
       CONSTRAINT [comtnfiledetail_pk] UNIQUE([atch_file_id], [file_sn]);

ALTER CLASS [dba].[comtnmenucreatdtls] ADD ATTRIBUTE
       [menu_no] numeric(20,0) NOT NULL COMMENT '메뉴번호',
       [author_code] character varying(30) COLLATE utf8_bin NOT NULL COMMENT '권한코드',
       [mapng_creat_id] character varying(30) COLLATE utf8_bin COMMENT '맵생성ID';
ALTER CLASS [dba].[comtnmenucreatdtls] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnmenucreatdtls_menu_no_author_code] PRIMARY KEY([menu_no], [author_code]),
       CONSTRAINT [comtnmenucreatdtls_pk] UNIQUE([menu_no], [author_code]);

ALTER CLASS [dba].[comtnqustnrtmplat] ADD ATTRIBUTE
       [qustnr_tmplat_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '설문템플릿ID',
       [qustnr_tmplat_ty] character varying(100) COLLATE utf8_bin COMMENT '설문템플릿유형',
       [qustnr_tmplat_dc] character varying(2000) COLLATE utf8_bin COMMENT '설문템플릿설명',
       [qustnr_tmplat_path_nm] character varying(100) COLLATE utf8_bin COMMENT '설문템플릿경로명',
       [frst_regist_pnttm] datetime COMMENT '최초등록시점',
       [frst_register_id] character varying(20) COLLATE utf8_bin COMMENT '최초등록자ID',
       [last_updt_pnttm] datetime COMMENT '최종수정시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID',
       [qustnr_tmplat_image_info] blob COMMENT '설문템플릿이미지정보';
ALTER CLASS [dba].[comtnqustnrtmplat] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnqustnrtmplat_qustnr_tmplat_id] PRIMARY KEY([qustnr_tmplat_id]),
       CONSTRAINT [comtnqustnrtmplat_pk] UNIQUE([qustnr_tmplat_id]);

ALTER CLASS [dba].[comtecopseq] ADD ATTRIBUTE
       [table_name] character varying(20) COLLATE utf8_bin NOT NULL,
       [next_id] numeric(30,0);
ALTER CLASS [dba].[comtecopseq] ADD ATTRIBUTE
       CONSTRAINT [pk_comtecopseq_table_name] PRIMARY KEY([table_name]),
       CONSTRAINT [comtecopseq_pk] UNIQUE([table_name]);

ALTER CLASS [dba].[comtccmmnclcode] ADD ATTRIBUTE
       [cl_code] character(3) COLLATE utf8_bin NOT NULL COMMENT '분류코드',
       [cl_code_nm] character varying(60) COLLATE utf8_bin COMMENT '분류코드명',
       [cl_code_dc] character varying(200) COLLATE utf8_bin COMMENT '분류코드설명',
       [use_at] character(1) COLLATE utf8_bin COMMENT '사용여부',
       [frst_regist_pnttm] datetime COMMENT '최초등록시점',
       [frst_register_id] character varying(20) COLLATE utf8_bin COMMENT '최초등록자ID',
       [last_updt_pnttm] datetime COMMENT '최종수정시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID';
ALTER CLASS [dba].[comtccmmnclcode] ADD ATTRIBUTE
       CONSTRAINT [pk_comtccmmnclcode_cl_code] PRIMARY KEY([cl_code]),
       CONSTRAINT [comtccmmnclcode_pk] UNIQUE([cl_code]);

ALTER CLASS [dba].[comtccmmncode] ADD ATTRIBUTE
       [code_id] character varying(6) COLLATE utf8_bin NOT NULL COMMENT '코드ID',
       [code_id_nm] character varying(60) COLLATE utf8_bin COMMENT '코드ID명',
       [code_id_dc] character varying(200) COLLATE utf8_bin COMMENT '코드ID설명',
       [use_at] character(1) COLLATE utf8_bin COMMENT '사용여부',
       [cl_code] character(3) COLLATE utf8_bin COMMENT '분류코드',
       [frst_regist_pnttm] datetime COMMENT '최초등록시점',
       [frst_register_id] character varying(20) COLLATE utf8_bin COMMENT '최초등록자ID',
       [last_updt_pnttm] datetime COMMENT '최종수정시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID';
ALTER CLASS [dba].[comtccmmncode] ADD ATTRIBUTE
       CONSTRAINT [pk_comtccmmncode_code_id] PRIMARY KEY([code_id]),
       CONSTRAINT [comtccmmncode_pk] UNIQUE([code_id]);

ALTER CLASS [dba].[comtccmmndetailcode] ADD ATTRIBUTE
       [code_id] character varying(6) COLLATE utf8_bin NOT NULL COMMENT '코드ID',
       [code] character varying(15) COLLATE utf8_bin NOT NULL COMMENT '코드',
       [code_nm] character varying(60) COLLATE utf8_bin COMMENT '코드명',
       [code_dc] character varying(200) COLLATE utf8_bin COMMENT '코드설명',
       [use_at] character(1) COLLATE utf8_bin COMMENT '사용여부',
       [frst_regist_pnttm] datetime COMMENT '최초등록시점',
       [frst_register_id] character varying(20) COLLATE utf8_bin COMMENT '최초등록자ID',
       [last_updt_pnttm] datetime COMMENT '최종수정시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID';
ALTER CLASS [dba].[comtccmmndetailcode] ADD ATTRIBUTE
       CONSTRAINT [pk_comtccmmndetailcode_code_id_code] PRIMARY KEY([code_id], [code]),
       CONSTRAINT [comtccmmndetailcode_pk] UNIQUE([code_id], [code]);

ALTER CLASS [dba].[comtnauthorgroupinfo] ADD ATTRIBUTE
       [group_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '그룹ID',
       [group_nm] character varying(60) COLLATE utf8_bin NOT NULL COMMENT '그룹명',
       [group_creat_de] character(20) COLLATE utf8_bin NOT NULL COMMENT '그룹생성일',
       [group_dc] character varying(100) COLLATE utf8_bin COMMENT '그룹설명';
ALTER CLASS [dba].[comtnauthorgroupinfo] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnauthorgroupinfo_group_id] PRIMARY KEY([group_id]),
       CONSTRAINT [comtnauthorgroupinfo_pk] UNIQUE([group_id]);

ALTER CLASS [dba].[comtnauthorinfo] ADD ATTRIBUTE
       [author_code] character varying(30) COLLATE utf8_bin NOT NULL COMMENT '권한코드',
       [author_nm] character varying(60) COLLATE utf8_bin NOT NULL COMMENT '권한명',
       [author_dc] character varying(200) COLLATE utf8_bin COMMENT '권한설명',
       [author_creat_de] character(20) COLLATE utf8_bin NOT NULL COMMENT '권한생성일';
ALTER CLASS [dba].[comtnauthorinfo] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnauthorinfo_author_code] PRIMARY KEY([author_code]),
       CONSTRAINT [comtnauthorinfo_pk] UNIQUE([author_code]);

ALTER CLASS [dba].[comtnbbs] ADD ATTRIBUTE
       [ntt_id] numeric(20,0) NOT NULL COMMENT '게시물ID',
       [bbs_id] character(30) COLLATE utf8_bin NOT NULL COMMENT '게시판ID',
       [ntt_no] numeric(20,0) COMMENT '게시물번호',
       [ntt_sj] character varying(2000) COLLATE utf8_bin COMMENT '게시물제목',
       [ntt_cn] character varying(32000) COLLATE utf8_bin COMMENT '게시물내용',
       [answer_at] character(1) COLLATE utf8_bin COMMENT '답글여부',
       [parntsctt_no] numeric(10,0) COMMENT '부모글번호',
       [answer_lc] numeric(8,0) COMMENT '답글위치',
       [sort_ordr] numeric(8,0) COMMENT '정렬순서',
       [rdcnt] numeric(10,0) COMMENT '조회수',
       [use_at] character(1) COLLATE utf8_bin NOT NULL COMMENT '사용여부',
       [ntce_bgnde] character(20) COLLATE utf8_bin COMMENT '게시시작일',
       [ntce_endde] character(20) COLLATE utf8_bin COMMENT '게시종료일',
       [ntcr_id] character varying(20) COLLATE utf8_bin COMMENT '게시자ID',
       [ntcr_nm] character varying(20) COLLATE utf8_bin COMMENT '게시자명',
       [password] character varying(200) COLLATE utf8_bin COMMENT '비밀번호',
       [atch_file_id] character(20) COLLATE utf8_bin COMMENT '첨부파일ID',
       [notice_at] character(1) COLLATE utf8_bin,
       [sj_bold_at] character(1) COLLATE utf8_bin,
       [secret_at] character(1) COLLATE utf8_bin,
       [frst_regist_pnttm] datetime NOT NULL COMMENT '최초등록시점',
       [frst_register_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '최초등록자ID',
       [last_updt_pnttm] datetime COMMENT '최종수정시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID',
       [blog_id] character(20) COLLATE utf8_bin;
ALTER CLASS [dba].[comtnbbs] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnbbs_ntt_id_bbs_id] PRIMARY KEY([ntt_id], [bbs_id]),
       CONSTRAINT [comtnbbs_pk] UNIQUE([ntt_id], [bbs_id]);

ALTER CLASS [dba].[comtnbbsmasteroptn] ADD ATTRIBUTE
       [bbs_id] character(30) COLLATE utf8_bin NOT NULL COMMENT '게시판ID',
       [answer_at] character(1) COLLATE utf8_bin NOT NULL COMMENT '댓글여부',
       [stsfdg_at] character(1) COLLATE utf8_bin NOT NULL COMMENT '만족도여부',
       [frst_regist_pnttm] datetime NOT NULL COMMENT '최초등록시점',
       [last_updt_pnttm] datetime COMMENT '최종수정시점',
       [frst_register_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '최초등록자ID',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID';
ALTER CLASS [dba].[comtnbbsmasteroptn] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnbbsmasteroptn_bbs_id] PRIMARY KEY([bbs_id]),
       CONSTRAINT [comtnbbsmasteroptn_pk] UNIQUE([bbs_id]);

ALTER CLASS [dba].[comtnbbssynclog] ADD ATTRIBUTE
       [sync_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '싱크ID',
       [ntt_id] numeric(20,0) NOT NULL,
       [bbs_id] character(40) COLLATE utf8_bin NOT NULL,
       [sync_sttus_code] character(1) COLLATE utf8_bin,
       [regist_pnttm] datetime,
       [sync_pnttm] datetime,
       [error_pnttm] datetime;
ALTER CLASS [dba].[comtnbbssynclog] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnbbssynclog_sync_id] PRIMARY KEY([sync_id]);

ALTER CLASS [dba].[comtnblog] ADD ATTRIBUTE
       [blog_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '블로그ID',
       [blog_nm] character varying(255) COLLATE utf8_bin NOT NULL COMMENT '블로그명',
       [blog_intrcn] character varying(2400) COLLATE utf8_bin COMMENT '블로그소개',
       [use_at] character(1) COLLATE utf8_bin NOT NULL COMMENT '사용여부',
       [regist_se_code] character(6) COLLATE utf8_bin COMMENT '등록구분코드',
       [tmplat_id] character(20) COLLATE utf8_bin COMMENT '템플릿ID',
       [frst_regist_pnttm] datetime NOT NULL COMMENT '최초등록시점',
       [frst_register_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '최초등록자ID',
       [last_updt_pnttm] datetime COMMENT '최종수정시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID',
       [bbs_id] character(30) COLLATE utf8_bin,
       [blog_at] character(2) COLLATE utf8_bin;
ALTER CLASS [dba].[comtnblog] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnblog_blog_id] PRIMARY KEY([blog_id]);

ALTER CLASS [dba].[comtncmmnty] ADD ATTRIBUTE
       [cmmnty_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '커뮤니티ID',
       [cmmnty_nm] character varying(255) COLLATE utf8_bin NOT NULL COMMENT '커뮤니티명',
       [cmmnty_intrcn] character varying(2400) COLLATE utf8_bin COMMENT '커뮤니티소개',
       [use_at] character(1) COLLATE utf8_bin NOT NULL COMMENT '사용여부',
       [regist_se_code] character(6) COLLATE utf8_bin COMMENT '등록구분코드',
       [tmplat_id] character(20) COLLATE utf8_bin COMMENT '템플릿ID',
       [frst_regist_pnttm] datetime NOT NULL COMMENT '최초등록시점',
       [frst_register_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '최초등록자ID',
       [last_updt_pnttm] datetime COMMENT '최종수정시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID';
ALTER CLASS [dba].[comtncmmnty] ADD ATTRIBUTE
       CONSTRAINT [pk_comtncmmnty_cmmnty_id] PRIMARY KEY([cmmnty_id]),
       CONSTRAINT [comtncmmnty_pk] UNIQUE([cmmnty_id]);

ALTER CLASS [dba].[comtncmmntyuser] ADD ATTRIBUTE
       [cmmnty_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '커뮤니티ID',
       [emplyr_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '업무사용자ID',
       [mngr_at] character(1) COLLATE utf8_bin NOT NULL COMMENT '관리자여부',
       [mber_sttus] character varying(15) COLLATE utf8_bin COMMENT '회원상태',
       [sbscrb_de] datetime COMMENT '가입일',
       [secsn_de] character(20) COLLATE utf8_bin COMMENT '탈퇴일',
       [use_at] character(1) COLLATE utf8_bin COMMENT '사용여부',
       [frst_regist_pnttm] datetime NOT NULL COMMENT '최초등록시점',
       [frst_register_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '최초등록자ID',
       [last_updt_pnttm] datetime COMMENT '최종수정시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID';
ALTER CLASS [dba].[comtncmmntyuser] ADD ATTRIBUTE
       CONSTRAINT [pk_comtncmmntyuser_cmmnty_id_emplyr_id] PRIMARY KEY([cmmnty_id], [emplyr_id]),
       CONSTRAINT [comtncmmntyuser_pk] UNIQUE([cmmnty_id], [emplyr_id]);

ALTER CLASS [dba].[comtncomment] ADD ATTRIBUTE
       [ntt_id] numeric(20,0) NOT NULL COMMENT '게시물ID',
       [bbs_id] character(30) COLLATE utf8_bin NOT NULL COMMENT '게시판ID',
       [answer_no] numeric(20,0) NOT NULL COMMENT '댓글번호',
       [wrter_id] character varying(20) COLLATE utf8_bin COMMENT '작성자ID',
       [wrter_nm] character varying(20) COLLATE utf8_bin COMMENT '작성자명',
       [answer] character varying(200) COLLATE utf8_bin COMMENT '댓글',
       [use_at] character(1) COLLATE utf8_bin NOT NULL COMMENT '사용여부',
       [frst_regist_pnttm] datetime NOT NULL COMMENT '최초등록시점',
       [frst_register_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '최초등록자ID',
       [last_updt_pnttm] datetime COMMENT '최종수정시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID',
       [password] character varying(200) COLLATE utf8_bin COMMENT '비밀번호';
ALTER CLASS [dba].[comtncomment] ADD ATTRIBUTE
       CONSTRAINT [pk_comtncomment_ntt_id_bbs_id_answer_no] PRIMARY KEY([ntt_id], [bbs_id], [answer_no]),
       CONSTRAINT [comtncomment_pk] UNIQUE([ntt_id], [bbs_id], [answer_no]);

ALTER VCLASS [dba].[comvnusermaster] ADD ATTRIBUTE
       [esntl_id] character(20) COLLATE utf8_bin,
       [user_id] character varying(20) COLLATE utf8_bin,
       [password] character varying(200) COLLATE utf8_bin,
       [user_nm] character varying(60) COLLATE utf8_bin,
       [user_zip] character varying(6) COLLATE utf8_bin,
       [user_adres] character varying(100) COLLATE utf8_bin,
       [user_email] character varying(50) COLLATE utf8_bin,
       [group_id] character varying(1073741823) COLLATE utf8_bin,
       [user_se] character varying(3) COLLATE utf8_bin,
       [orgnzt_id] character varying(1073741823) COLLATE utf8_bin;

ALTER CLASS [dba].[comtnemplyrinfo] ADD ATTRIBUTE
       [emplyr_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '업무사용자ID',
       [orgnzt_id] character(20) COLLATE utf8_bin COMMENT '조직ID',
       [user_nm] character varying(60) COLLATE utf8_bin NOT NULL COMMENT '사용자명',
       [password] character varying(200) COLLATE utf8_bin NOT NULL COMMENT '비밀번호',
       [empl_no] character varying(20) COLLATE utf8_bin COMMENT '사원번호',
       [ihidnum] character varying(200) COLLATE utf8_bin COMMENT '주민등록번호',
       [sexdstn_code] character(1) COLLATE utf8_bin COMMENT '성별코드',
       [brthdy] character(20) COLLATE utf8_bin COMMENT '생일',
       [fxnum] character varying(20) COLLATE utf8_bin COMMENT '팩스번호',
       [house_adres] character varying(100) COLLATE utf8_bin NOT NULL COMMENT '자택주소',
       [password_hint] character varying(100) COLLATE utf8_bin NOT NULL COMMENT '비밀번호힌트',
       [password_cnsr] character varying(100) COLLATE utf8_bin NOT NULL COMMENT '비밀번호정답',
       [house_end_telno] character varying(4) COLLATE utf8_bin NOT NULL COMMENT '자택끝전화번호',
       [area_no] character varying(4) COLLATE utf8_bin NOT NULL COMMENT '지역번호',
       [detail_adres] character varying(100) COLLATE utf8_bin COMMENT '상세주소',
       [zip] character varying(6) COLLATE utf8_bin NOT NULL COMMENT '우편번호',
       [offm_telno] character varying(20) COLLATE utf8_bin COMMENT '사무실전화번호',
       [mbtlnum] character varying(20) COLLATE utf8_bin COMMENT '이동전화번호',
       [email_adres] character varying(50) COLLATE utf8_bin COMMENT '이메일주소',
       [ofcps_nm] character varying(60) COLLATE utf8_bin COMMENT '직위명',
       [house_middle_telno] character varying(4) COLLATE utf8_bin NOT NULL COMMENT '자택중간전화번호',
       [group_id] character(20) COLLATE utf8_bin COMMENT '그룹ID',
       [pstinst_code] character(8) COLLATE utf8_bin COMMENT '소속기관코드',
       [emplyr_sttus_code] character(1) COLLATE utf8_bin NOT NULL COMMENT '사용자상태코드',
       [esntl_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '고유ID',
       [crtfc_dn_value] character varying(100) COLLATE utf8_bin COMMENT '인증DN값',
       [sbscrb_de] datetime COMMENT '가입일',
       [lock_at] character(1) COLLATE utf8_bin COMMENT '잠금여부',
       [lock_cnt] numeric(3,0) COMMENT '잠금회수',
       [lock_last_pnttm] datetime COMMENT '잠금일자',
       [chg_pwd_last_pnttm] datetime COMMENT '비밀번호변경일자',
       [auth_ty] character varying(20) COLLATE utf8_bin,
       [auth_dn] character varying(200) COLLATE utf8_bin,
       [auth_ci] character varying(200) COLLATE utf8_bin,
       [auth_di] character varying(200) COLLATE utf8_bin,
       [auth_email] character varying(50) COLLATE utf8_bin,
       [marketing_yn] character(1) COLLATE utf8_bin;
ALTER CLASS [dba].[comtnemplyrinfo] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnemplyrinfo_emplyr_id] PRIMARY KEY([emplyr_id]),
       CONSTRAINT [comtnemplyrinfo_pk] UNIQUE([emplyr_id]);

ALTER CLASS [dba].[comtnemplyrscrtyestbs] ADD ATTRIBUTE
       [scrty_dtrmn_trget_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '보안결정대상ID',
       [mber_ty_code] character(5) COLLATE utf8_bin COMMENT '회원유형코드',
       [author_code] character varying(30) COLLATE utf8_bin NOT NULL COMMENT '권한코드';
ALTER CLASS [dba].[comtnemplyrscrtyestbs] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnemplyrscrtyestbs_scrty_dtrmn_trget_id] PRIMARY KEY([scrty_dtrmn_trget_id]),
       CONSTRAINT [comtnemplyrscrtyestbs_pk] UNIQUE([scrty_dtrmn_trget_id]);

ALTER CLASS [dba].[comtnqestnrinfo] ADD ATTRIBUTE
       [qustnr_tmplat_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '설문템플릿ID',
       [qestnr_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '설문지ID',
       [qustnr_sj] character varying(255) COLLATE utf8_bin COMMENT '설문제목',
       [qustnr_purps] character varying(1000) COLLATE utf8_bin COMMENT '설문목적',
       [qustnr_writng_guidance_cn] character varying(2000) COLLATE utf8_bin COMMENT '설문작성안내내용',
       [qustnr_trget] character varying(1000) COLLATE utf8_bin COMMENT '설문대상',
       [qustnr_bgnde] character(20) COLLATE utf8_bin COMMENT '설문시작일',
       [qustnr_endde] character(20) COLLATE utf8_bin COMMENT '설문종료일',
       [frst_regist_pnttm] datetime COMMENT '최초등록시점',
       [frst_register_id] character varying(20) COLLATE utf8_bin COMMENT '최초등록자ID',
       [last_updt_pnttm] datetime COMMENT '최종수정시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID';
ALTER CLASS [dba].[comtnqestnrinfo] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnqestnrinfo_qustnr_tmplat_id_qestnr_id] PRIMARY KEY([qustnr_tmplat_id], [qestnr_id]),
       CONSTRAINT [comtnqestnrinfo_pk] UNIQUE([qustnr_tmplat_id], [qestnr_id]);

ALTER CLASS [dba].[comtnentrprsmber] ADD ATTRIBUTE
       [entrprs_mber_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '기업회원ID',
       [entrprs_se_code] character(8) COLLATE utf8_bin COMMENT '기업구분코드',
       [bizrno] character varying(10) COLLATE utf8_bin COMMENT '사업자등록번호',
       [jurirno] character varying(13) COLLATE utf8_bin COMMENT '법인등록번호',
       [cmpny_nm] character varying(60) COLLATE utf8_bin NOT NULL COMMENT '회사명',
       [cxfc] character varying(50) COLLATE utf8_bin COMMENT '대표이사',
       [zip] character varying(6) COLLATE utf8_bin NOT NULL COMMENT '우편번호',
       [adres] character varying(100) COLLATE utf8_bin NOT NULL COMMENT '주소',
       [entrprs_middle_telno] character varying(4) COLLATE utf8_bin NOT NULL COMMENT '기업중간전화번호',
       [fxnum] character varying(20) COLLATE utf8_bin COMMENT '팩스번호',
       [induty_code] character(1) COLLATE utf8_bin COMMENT '업종코드',
       [applcnt_nm] character varying(50) COLLATE utf8_bin NOT NULL COMMENT '신청자명',
       [applcnt_ihidnum] character varying(200) COLLATE utf8_bin COMMENT '신청자주민등록번호',
       [sbscrb_de] datetime COMMENT '가입일',
       [entrprs_mber_sttus] character varying(15) COLLATE utf8_bin COMMENT '기업회원상태',
       [entrprs_mber_password] character varying(200) COLLATE utf8_bin COMMENT '기업회원비밀번호',
       [entrprs_mber_password_hint] character varying(100) COLLATE utf8_bin NOT NULL COMMENT '기업회원비밀번호힌트',
       [entrprs_mber_password_cnsr] character varying(100) COLLATE utf8_bin NOT NULL COMMENT '기업회원비밀번호정답',
       [group_id] character(20) COLLATE utf8_bin COMMENT '그룹ID',
       [detail_adres] character varying(100) COLLATE utf8_bin COMMENT '상세주소',
       [entrprs_end_telno] character varying(4) COLLATE utf8_bin NOT NULL COMMENT '기업끝전화번호',
       [area_no] character varying(4) COLLATE utf8_bin NOT NULL COMMENT '지역번호',
       [applcnt_email_adres] character varying(50) COLLATE utf8_bin NOT NULL COMMENT '신청자이메일주소',
       [esntl_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '고유ID',
       [lock_at] character(1) COLLATE utf8_bin COMMENT '잠금여부',
       [lock_cnt] numeric(3,0) COMMENT '잠금회수',
       [lock_last_pnttm] datetime COMMENT '잠금일자',
       [chg_pwd_last_pnttm] datetime COMMENT '비밀번호변경일자',
       [auth_ty] character varying(20) COLLATE utf8_bin,
       [auth_dn] character varying(200) COLLATE utf8_bin,
       [auth_ci] character varying(200) COLLATE utf8_bin,
       [auth_di] character varying(200) COLLATE utf8_bin,
       [auth_email] character varying(50) COLLATE utf8_bin,
       [marketing_yn] character(1) COLLATE utf8_bin,
       [biz_reg_file_path] character varying(500) COLLATE utf8_bin,
       [instt_id] character varying(20) COLLATE utf8_bin;
ALTER CLASS [dba].[comtnentrprsmber] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnentrprsmber_entrprs_mber_id] PRIMARY KEY([entrprs_mber_id]),
       CONSTRAINT [comtnentrprsmber_pk] UNIQUE([entrprs_mber_id]);

ALTER CLASS [dba].[comtngnrlmber] ADD ATTRIBUTE
       [mber_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '회원ID',
       [password] character varying(200) COLLATE utf8_bin NOT NULL COMMENT '비밀번호',
       [password_hint] character varying(100) COLLATE utf8_bin COMMENT '비밀번호힌트',
       [password_cnsr] character varying(100) COLLATE utf8_bin COMMENT '비밀번호정답',
       [ihidnum] character varying(200) COLLATE utf8_bin COMMENT '주민등록번호',
       [mber_nm] character varying(50) COLLATE utf8_bin NOT NULL COMMENT '회원명',
       [zip] character varying(6) COLLATE utf8_bin NOT NULL COMMENT '우편번호',
       [adres] character varying(100) COLLATE utf8_bin NOT NULL COMMENT '주소',
       [area_no] character varying(4) COLLATE utf8_bin NOT NULL COMMENT '지역번호',
       [mber_sttus] character varying(15) COLLATE utf8_bin COMMENT '회원상태',
       [detail_adres] character varying(100) COLLATE utf8_bin COMMENT '상세주소',
       [end_telno] character varying(4) COLLATE utf8_bin NOT NULL COMMENT '끝전화번호',
       [mbtlnum] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '이동전화번호',
       [group_id] character(20) COLLATE utf8_bin COMMENT '그룹ID',
       [mber_fxnum] character varying(20) COLLATE utf8_bin COMMENT '회원팩스번호',
       [mber_email_adres] character varying(50) COLLATE utf8_bin COMMENT '회원이메일주소',
       [middle_telno] character varying(4) COLLATE utf8_bin NOT NULL COMMENT '중간전화번호',
       [sbscrb_de] datetime COMMENT '가입일',
       [sexdstn_code] character(1) COLLATE utf8_bin COMMENT '성별코드',
       [esntl_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '고유ID',
       [lock_at] character(1) COLLATE utf8_bin COMMENT '잠금여부',
       [lock_cnt] numeric(3,0) COMMENT '잠금회수',
       [lock_last_pnttm] datetime COMMENT '잠금일자',
       [chg_pwd_last_pnttm] datetime COMMENT '비밀번호변경일자',
       [auth_ty] character varying(20) COLLATE utf8_bin,
       [auth_dn] character varying(200) COLLATE utf8_bin,
       [auth_ci] character varying(200) COLLATE utf8_bin,
       [auth_di] character varying(200) COLLATE utf8_bin,
       [auth_email] character varying(50) COLLATE utf8_bin,
       [marketing_yn] character(1) COLLATE utf8_bin;
ALTER CLASS [dba].[comtngnrlmber] ADD ATTRIBUTE
       CONSTRAINT [pk_comtngnrlmber_mber_id] PRIMARY KEY([mber_id]),
       CONSTRAINT [comtngnrlmber_pk] UNIQUE([mber_id]);

ALTER CLASS [dba].[comtnqustnrqesitm] ADD ATTRIBUTE
       [qestnr_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '설문지ID',
       [qustnr_qesitm_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '설문문항ID',
       [qustnr_tmplat_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '설문템플릿ID',
       [qestn_sn] numeric(10,0) COMMENT '질문순번',
       [qestn_ty_code] character(1) COLLATE utf8_bin COMMENT '질문유형코드',
       [qestn_cn] character varying(2500) COLLATE utf8_bin COMMENT '질문내용',
       [mxmm_choise_co] numeric(5,0) COMMENT '최대선택수',
       [frst_regist_pnttm] datetime NOT NULL COMMENT '최초등록시점',
       [frst_register_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '최초등록자ID',
       [last_updt_pnttm] datetime NOT NULL COMMENT '최종수정시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '최종수정자ID';
ALTER CLASS [dba].[comtnqustnrqesitm] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnqustnrqesitm_qestnr_id_qustnr_qesitm_id_qustnr_tmplat_id] PRIMARY KEY([qestnr_id], [qustnr_qesitm_id], [qustnr_tmplat_id]),
       CONSTRAINT [comtnqustnrqesitm_pk] UNIQUE([qestnr_id], [qustnr_qesitm_id], [qustnr_tmplat_id]);

ALTER CLASS [dba].[comtnqustnriem] ADD ATTRIBUTE
       [qustnr_tmplat_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '설문템플릿ID',
       [qestnr_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '설문지ID',
       [qustnr_qesitm_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '설문문항ID',
       [qustnr_iem_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '설문항목ID',
       [iem_sn] numeric(5,0) COMMENT '항목순번',
       [iem_cn] character varying(1000) COLLATE utf8_bin COMMENT '항목내용',
       [etc_answer_at] character(1) COLLATE utf8_bin COMMENT '기타답변여부',
       [frst_regist_pnttm] datetime COMMENT '최초등록시점',
       [frst_register_id] character varying(20) COLLATE utf8_bin COMMENT '최초등록자ID',
       [last_updt_pnttm] datetime COMMENT '최종수정시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID';
ALTER CLASS [dba].[comtnqustnriem] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnqustnriem_qustnr_tmplat_id_qestnr_id_qustnr_qesitm_id_qustnr_iem_id] PRIMARY KEY([qustnr_tmplat_id], [qestnr_id], [qustnr_qesitm_id], [qustnr_iem_id]),
       CONSTRAINT [comtnqustnriem_pk] UNIQUE([qustnr_tmplat_id], [qestnr_id], [qustnr_qesitm_id], [qustnr_iem_id]);

ALTER CLASS [dba].[comtnqustnrrespondinfo] ADD ATTRIBUTE
       [qustnr_tmplat_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '설문템플릿ID',
       [qestnr_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '설문지ID',
       [qustnr_respond_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '설문응답자ID',
       [sexdstn_code] character(1) COLLATE utf8_bin COMMENT '성별코드',
       [occp_ty_code] character(1) COLLATE utf8_bin COMMENT '직업유형코드',
       [respond_nm] character varying(50) COLLATE utf8_bin COMMENT '응답자명',
       [brthdy] character(20) COLLATE utf8_bin COMMENT '생일',
       [area_no] character varying(4) COLLATE utf8_bin COMMENT '지역번호',
       [middle_telno] character varying(4) COLLATE utf8_bin COMMENT '중간전화번호',
       [end_telno] character varying(4) COLLATE utf8_bin COMMENT '끝전화번호',
       [frst_regist_pnttm] datetime COMMENT '최초등록시점',
       [frst_register_id] character varying(20) COLLATE utf8_bin COMMENT '최초등록자ID',
       [last_updt_pnttm] datetime COMMENT '최종수정시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID';
ALTER CLASS [dba].[comtnqustnrrespondinfo] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnqustnrrespondinfo_qustnr_tmplat_id_qestnr_id_qustnr_respond_id] PRIMARY KEY([qustnr_tmplat_id], [qestnr_id], [qustnr_respond_id]),
       CONSTRAINT [comtnqustnrrespondinfo_pk] UNIQUE([qustnr_tmplat_id], [qestnr_id], [qustnr_respond_id]);

ALTER CLASS [dba].[comtnqustnrrspnsresult] ADD ATTRIBUTE
       [qustnr_rspns_result_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '설문응답결과ID',
       [qestnr_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '설문지ID',
       [qustnr_qesitm_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '설문문항ID',
       [qustnr_tmplat_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '설문템플릿ID',
       [respond_answer_cn] character varying(1000) COLLATE utf8_bin COMMENT '응답답변내용',
       [etc_answer_cn] character varying(1000) COLLATE utf8_bin COMMENT '기타답변내용',
       [respond_nm] character varying(50) COLLATE utf8_bin COMMENT '응답자명',
       [frst_regist_pnttm] datetime COMMENT '최초등록시점',
       [frst_register_id] character varying(20) COLLATE utf8_bin COMMENT '최초등록자ID',
       [last_updt_pnttm] datetime COMMENT '최종수정시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID',
       [qustnr_iem_id] character varying(20) COLLATE utf8_bin COMMENT '설문항목ID';
ALTER CLASS [dba].[comtnqustnrrspnsresult] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnqustnrrspnsresult_qustnr_rspns_result_id_qestnr_id_qustnr_qesitm_id_qustnr_tmplat_id] PRIMARY KEY([qustnr_rspns_result_id], [qestnr_id], [qustnr_qesitm_id], [qustnr_tmplat_id]),
       CONSTRAINT [comtnqustnrrspnsresult_pk] UNIQUE([qustnr_rspns_result_id], [qestnr_id], [qustnr_qesitm_id], [qustnr_tmplat_id]);

ALTER CLASS [dba].[comtnstsfdg] ADD ATTRIBUTE
       [stsfdg_no] numeric(20,0) NOT NULL COMMENT '만족도번호',
       [ntt_id] numeric(20,0) NOT NULL COMMENT '게시물ID',
       [bbs_id] character(30) COLLATE utf8_bin NOT NULL COMMENT '게시판ID',
       [wrter_id] character varying(20) COLLATE utf8_bin COMMENT '작성자ID',
       [wrter_nm] character varying(20) COLLATE utf8_bin COMMENT '작성자명',
       [password] character varying(200) COLLATE utf8_bin COMMENT '비밀번호',
       [stsfdg] numeric(1,0) NOT NULL COMMENT '만족도',
       [stsfdg_cn] character varying(200) COLLATE utf8_bin COMMENT '만족도내용',
       [use_at] character(1) COLLATE utf8_bin NOT NULL COMMENT '사용여부',
       [frst_regist_pnttm] datetime NOT NULL COMMENT '최초등록시점',
       [last_updt_pnttm] datetime COMMENT '최종수정시점',
       [frst_register_id] character varying(20) COLLATE utf8_bin NOT NULL COMMENT '최초등록자ID',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID';
ALTER CLASS [dba].[comtnstsfdg] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnstsfdg_stsfdg_no] PRIMARY KEY([stsfdg_no]),
       CONSTRAINT [comtnstsfdg_pk] UNIQUE([stsfdg_no]);

ALTER CLASS [dba].[comtntmplatinfo] ADD ATTRIBUTE
       [tmplat_id] character(20) COLLATE utf8_bin NOT NULL COMMENT '템플릿ID',
       [tmplat_nm] character varying(255) COLLATE utf8_bin COMMENT '템플릿명',
       [tmplat_cours] character varying(2000) COLLATE utf8_bin COMMENT '템플릿경로',
       [use_at] character(1) COLLATE utf8_bin COMMENT '사용여부',
       [tmplat_se_code] character(6) COLLATE utf8_bin COMMENT '템플릿구분코드',
       [frst_register_id] character varying(20) COLLATE utf8_bin COMMENT '최초등록자ID',
       [frst_regist_pnttm] datetime COMMENT '최초등록시점',
       [last_updusr_id] character varying(20) COLLATE utf8_bin COMMENT '최종수정자ID',
       [last_updt_pnttm] datetime COMMENT '최종수정시점';
ALTER CLASS [dba].[comtntmplatinfo] ADD ATTRIBUTE
       CONSTRAINT [pk_comtntmplatinfo_tmplat_id] PRIMARY KEY([tmplat_id]),
       CONSTRAINT [comtntmplatinfo_pk] UNIQUE([tmplat_id]);

ALTER CLASS [dba].[msatnauthorgroupinfo] ADD ATTRIBUTE
       [group_id] character(20) COLLATE utf8_bin NOT NULL,
       [group_nm] character varying(60) COLLATE utf8_bin NOT NULL,
       [group_creat_de] character(20) COLLATE utf8_bin NOT NULL,
       [group_dc] character varying(100) COLLATE utf8_bin;
ALTER CLASS [dba].[msatnauthorgroupinfo] ADD ATTRIBUTE
       CONSTRAINT [pk_msatnauthorgroupinfo_group_id] PRIMARY KEY([group_id]),
       CONSTRAINT [msatnauthorgroupinfo_pk] UNIQUE([group_id]);

ALTER CLASS [dba].[msatnauthorinfo] ADD ATTRIBUTE
       [author_code] character varying(30) COLLATE utf8_bin NOT NULL,
       [author_nm] character varying(60) COLLATE utf8_bin NOT NULL,
       [author_dc] character varying(200) COLLATE utf8_bin,
       [author_creat_de] character(20) COLLATE utf8_bin NOT NULL;
ALTER CLASS [dba].[msatnauthorinfo] ADD ATTRIBUTE
       CONSTRAINT [pk_msatnauthorinfo_author_code] PRIMARY KEY([author_code]),
       CONSTRAINT [msatnauthorinfo_pk] UNIQUE([author_code]);

ALTER CLASS [dba].[comtninsttinfo] ADD ATTRIBUTE
       [instt_id] character(20) COLLATE utf8_bin NOT NULL,
       [instt_nm] character varying(100) COLLATE utf8_bin NOT NULL,
       [reprsnt_nm] character varying(50) COLLATE utf8_bin,
       [bizrno] character varying(20) COLLATE utf8_bin,
       [zip] character varying(6) COLLATE utf8_bin,
       [adres] character varying(200) COLLATE utf8_bin,
       [detail_adres] character varying(200) COLLATE utf8_bin,
       [biz_reg_file_path] character varying(500) COLLATE utf8_bin,
       [instt_sttus] character(1) COLLATE utf8_bin DEFAULT 'P',
       [frst_regist_pnttm] datetime DEFAULT SYS_DATETIME,
       [last_updt_pnttm] datetime DEFAULT SYS_DATETIME,
       [charger_nm] character varying(100) COLLATE utf8_bin,
       [charger_email] character varying(100) COLLATE utf8_bin,
       [charger_tel] character varying(20) COLLATE utf8_bin,
       [rjct_rsn] character varying(1000) COLLATE utf8_bin,
       [rjct_pnttm] datetime;
ALTER CLASS [dba].[comtninsttinfo] ADD ATTRIBUTE
       CONSTRAINT [pk_comtninsttinfo_instt_id] PRIMARY KEY([instt_id]);

ALTER CLASS [dba].[comtninsttinfo_bak_20260302_212335] ADD ATTRIBUTE
       [instt_id] character(20) COLLATE utf8_bin NOT NULL,
       [instt_nm] character varying(100) COLLATE utf8_bin NOT NULL,
       [reprsnt_nm] character varying(50) COLLATE utf8_bin,
       [bizrno] character varying(20) COLLATE utf8_bin,
       [zip] character varying(6) COLLATE utf8_bin,
       [adres] character varying(200) COLLATE utf8_bin,
       [detail_adres] character varying(200) COLLATE utf8_bin,
       [biz_reg_file_path] character varying(500) COLLATE utf8_bin,
       [instt_sttus] character(1) COLLATE utf8_bin DEFAULT 'P',
       [frst_regist_pnttm] datetime DEFAULT SYS_DATETIME,
       [last_updt_pnttm] datetime DEFAULT SYS_DATETIME,
       [charger_nm] character varying(100) COLLATE utf8_bin,
       [charger_email] character varying(100) COLLATE utf8_bin,
       [charger_tel] character varying(20) COLLATE utf8_bin,
       [rjct_rsn] character varying(1000) COLLATE utf8_bin,
       [rjct_pnttm] datetime;

ALTER CLASS [dba].[comtnentrprsmberfile] ADD ATTRIBUTE
       [file_id] character varying(60) COLLATE utf8_bin NOT NULL,
       [entrprs_mber_id] character varying(20) COLLATE utf8_bin NOT NULL,
       [file_sn] integer NOT NULL,
       [stre_file_nm] character varying(255) COLLATE utf8_bin NOT NULL,
       [orignl_file_nm] character varying(255) COLLATE utf8_bin,
       [file_stre_path] character varying(500) COLLATE utf8_bin NOT NULL,
       [file_mg] bigint,
       [file_extsn] character varying(20) COLLATE utf8_bin,
       [file_cn] character varying(200) COLLATE utf8_bin,
       [regist_pnttm] datetime DEFAULT SYS_DATETIME;
ALTER CLASS [dba].[comtnentrprsmberfile] ADD ATTRIBUTE
       CONSTRAINT [pk_comtnentrprsmberfile] PRIMARY KEY([file_id]);

ALTER CLASS [dba].[comtninsttfile] ADD ATTRIBUTE
       [file_id] character varying(60) COLLATE utf8_bin NOT NULL,
       [instt_id] character varying(20) COLLATE utf8_bin NOT NULL,
       [file_sn] integer NOT NULL,
       [stre_file_nm] character varying(255) COLLATE utf8_bin NOT NULL,
       [orignl_file_nm] character varying(255) COLLATE utf8_bin,
       [file_stre_path] character varying(500) COLLATE utf8_bin NOT NULL,
       [file_mg] bigint,
       [file_extsn] character varying(20) COLLATE utf8_bin,
       [file_cn] character varying(200) COLLATE utf8_bin,
       [regist_pnttm] datetime DEFAULT SYS_DATETIME;
ALTER CLASS [dba].[comtninsttfile] ADD ATTRIBUTE
       CONSTRAINT [pk_comtninsttfile] PRIMARY KEY([file_id]);




ALTER VCLASS [dba].[comvnusermaster] ADD QUERY ((select [dba.COMTNGNRLMBER].[ESNTL_ID], [dba.COMTNGNRLMBER].[MBER_ID], [dba.COMTNGNRLMBER].[PASSWORD],  cast( cast([dba.COMTNGNRLMBER].[MBER_NM] as varchar(60)) as varchar(60)), [dba.COMTNGNRLMBER].[ZIP], [dba.COMTNGNRLMBER].[ADRES], [dba.COMTNGNRLMBER].[MBER_EMAIL_ADRES], _utf8' ' collate utf8_bin,  cast(_utf8'GNR' collate utf8_bin as varchar(3)), _utf8' ' collate utf8_bin from [dba.COMTNGNRLMBER] [dba.COMTNGNRLMBER] union all select [dba.COMTNEMPLYRINFO].[ESNTL_ID], [dba.COMTNEMPLYRINFO].[EMPLYR_ID], [dba.COMTNEMPLYRINFO].[PASSWORD], [dba.COMTNEMPLYRINFO].[USER_NM], [dba.COMTNEMPLYRINFO].[ZIP], [dba.COMTNEMPLYRINFO].[HOUSE_ADRES], [dba.COMTNEMPLYRINFO].[EMAIL_ADRES],  cast( cast( cast( cast( cast( cast( cast( cast([dba.COMTNEMPLYRINFO].[GROUP_ID] as varchar) as varchar) as varchar) as varchar) as varchar) as varchar) as varchar) as varchar),  cast(_utf8'USR' collate utf8_bin as varchar(3)),  cast( cast( cast( cast( cast( cast( cast( cast([dba.COMTNEMPLYRINFO].[ORGNZT_ID] as varchar) as varchar) as varchar) as varchar) as varchar) as varchar) as varchar) as varchar) from [dba.COMTNEMPLYRINFO] [dba.COMTNEMPLYRINFO]) union all select [dba.COMTNENTRPRSMBER].[ESNTL_ID], [dba.COMTNENTRPRSMBER].[ENTRPRS_MBER_ID], [dba.COMTNENTRPRSMBER].[ENTRPRS_MBER_PASSWORD], [dba.COMTNENTRPRSMBER].[CMPNY_NM], [dba.COMTNENTRPRSMBER].[ZIP], [dba.COMTNENTRPRSMBER].[ADRES], [dba.COMTNENTRPRSMBER].[APPLCNT_EMAIL_ADRES], _utf8' ' collate utf8_bin,  cast(_utf8'ENT' collate utf8_bin as varchar(3)), _utf8' ' collate utf8_bin from [dba.COMTNENTRPRSMBER] [dba.COMTNENTRPRSMBER]) order by 1 ;


ALTER CLASS [dba].[comtnauthorrolerelate] ADD CONSTRAINT [comtnauthorrolerelate_fk1] FOREIGN KEY([author_code]) REFERENCES [dba].[comtnauthorinfo] ON DELETE CASCADE ON UPDATE RESTRICT ;

ALTER CLASS [dba].[comtnauthorrolerelate] ADD CONSTRAINT [comtnauthorrolerelate_fk2] FOREIGN KEY([role_code]) REFERENCES [dba].[comtnroleinfo] ON DELETE CASCADE ON UPDATE RESTRICT ;

ALTER CLASS [dba].[comtnbloguser] ADD CONSTRAINT [comtnbloguser_fk1] FOREIGN KEY([blog_id]) REFERENCES [dba].[comtnblog] ON DELETE RESTRICT ON UPDATE RESTRICT ;

ALTER CLASS [dba].[msatnauthorrolerelate] ADD CONSTRAINT [msatnauthorrolerelate_fk1] FOREIGN KEY([author_code]) REFERENCES [dba].[msatnauthorinfo] ON DELETE CASCADE ON UPDATE RESTRICT ;

ALTER CLASS [dba].[msatnauthorrolerelate] ADD CONSTRAINT [msatnauthorrolerelate_fk2] FOREIGN KEY([role_code]) REFERENCES [dba].[msatnroleinfo] ON DELETE CASCADE ON UPDATE RESTRICT ;

ALTER CLASS [dba].[msatnroles_hierarchy] ADD CONSTRAINT [msatnroles_hierarchy_fk1] FOREIGN KEY([parnts_role]) REFERENCES [dba].[msatnauthorinfo] ON DELETE CASCADE ON UPDATE RESTRICT ;

ALTER CLASS [dba].[msatnroles_hierarchy] ADD CONSTRAINT [msatnroles_hierarchy_fk2] FOREIGN KEY([chldrn_role]) REFERENCES [dba].[msatnauthorinfo] ON DELETE CASCADE ON UPDATE RESTRICT ;

ALTER CLASS [dba].[msatnmenucreatdtls] ADD CONSTRAINT [msatnmenucreatdtls_fk1] FOREIGN KEY([author_code]) REFERENCES [dba].[msatnauthorinfo] ON DELETE RESTRICT ON UPDATE RESTRICT ;

ALTER CLASS [dba].[comtnfiledetail] ADD CONSTRAINT [comtnfiledetail_fk1] FOREIGN KEY([atch_file_id]) REFERENCES [dba].[comtnfile] ON DELETE RESTRICT ON UPDATE RESTRICT ;

ALTER CLASS [dba].[comtnmenucreatdtls] ADD CONSTRAINT [comtnmenucreatdtls_fk1] FOREIGN KEY([author_code]) REFERENCES [dba].[comtnauthorinfo] ON DELETE RESTRICT ON UPDATE RESTRICT ;

ALTER CLASS [dba].[comtccmmncode] ADD CONSTRAINT [comtccmmncode_fk1] FOREIGN KEY([cl_code]) REFERENCES [dba].[comtccmmnclcode] ON DELETE RESTRICT ON UPDATE RESTRICT ;

ALTER CLASS [dba].[comtccmmndetailcode] ADD CONSTRAINT [comtccmmndetailcode_fk1] FOREIGN KEY([code_id]) REFERENCES [dba].[comtccmmncode] ON DELETE RESTRICT ON UPDATE RESTRICT ;

ALTER CLASS [dba].[comtnbbs] ADD CONSTRAINT [comtnbbs_fk1] FOREIGN KEY([bbs_id]) REFERENCES [dba].[comtnbbsmaster] ON DELETE RESTRICT ON UPDATE RESTRICT ;

ALTER CLASS [dba].[comtncmmntyuser] ADD CONSTRAINT [comtncmmntyuser_fk1] FOREIGN KEY([cmmnty_id]) REFERENCES [dba].[comtncmmnty] ON DELETE RESTRICT ON UPDATE RESTRICT ;

ALTER CLASS [dba].[comtncomment] ADD CONSTRAINT [comtncomment_fk1] FOREIGN KEY([ntt_id], [bbs_id]) REFERENCES [dba].[comtnbbs] ON DELETE RESTRICT ON UPDATE RESTRICT ;

ALTER CLASS [dba].[comtnemplyrinfo] ADD CONSTRAINT [comtnemplyrinfo_fk1] FOREIGN KEY([group_id]) REFERENCES [dba].[comtnauthorgroupinfo] ON DELETE SET NULL ON UPDATE RESTRICT ;

ALTER CLASS [dba].[comtnqestnrinfo] ADD CONSTRAINT [comtnqestnrinfo_fk1] FOREIGN KEY([qustnr_tmplat_id]) REFERENCES [dba].[comtnqustnrtmplat] ON DELETE RESTRICT ON UPDATE RESTRICT ;

ALTER CLASS [dba].[comtnentrprsmber] ADD CONSTRAINT [comtnentrprsmber_fk1] FOREIGN KEY([group_id]) REFERENCES [dba].[comtnauthorgroupinfo] ON DELETE SET NULL ON UPDATE RESTRICT ;

ALTER CLASS [dba].[comtngnrlmber] ADD CONSTRAINT [comtngnrlmber_fk1] FOREIGN KEY([group_id]) REFERENCES [dba].[comtnauthorgroupinfo] ON DELETE SET NULL ON UPDATE RESTRICT ;

ALTER CLASS [dba].[comtnqustnrqesitm] ADD CONSTRAINT [comtnqustnrqesitm_fk1] FOREIGN KEY([qustnr_tmplat_id], [qestnr_id]) REFERENCES [dba].[comtnqestnrinfo] ON DELETE RESTRICT ON UPDATE RESTRICT ;

ALTER CLASS [dba].[comtnqustnriem] ADD CONSTRAINT [comtnqustnriem_fk1] FOREIGN KEY([qestnr_id], [qustnr_qesitm_id], [qustnr_tmplat_id]) REFERENCES [dba].[comtnqustnrqesitm] ON DELETE RESTRICT ON UPDATE RESTRICT ;

ALTER CLASS [dba].[comtnqustnrrespondinfo] ADD CONSTRAINT [comtnqustnrrespondinfo_fk1] FOREIGN KEY([qustnr_tmplat_id], [qestnr_id]) REFERENCES [dba].[comtnqestnrinfo] ON DELETE RESTRICT ON UPDATE RESTRICT ;

ALTER CLASS [dba].[comtnqustnrrspnsresult] ADD CONSTRAINT [comtnqustnrrspnsresult_fk1] FOREIGN KEY([qestnr_id], [qustnr_qesitm_id], [qustnr_tmplat_id]) REFERENCES [dba].[comtnqustnrqesitm] ON DELETE RESTRICT ON UPDATE RESTRICT ;


COMMIT WORK;
