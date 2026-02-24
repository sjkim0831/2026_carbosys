select r.role_pttrn, a.author_code from msatnroleinfo r join msatnauthorrolerelate a on r.role_code=a.role_code where a.author_code='ROLE_ADMIN';
