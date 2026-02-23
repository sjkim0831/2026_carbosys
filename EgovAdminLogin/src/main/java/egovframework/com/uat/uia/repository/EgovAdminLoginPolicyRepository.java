package egovframework.com.uat.uia.repository;

import egovframework.com.uat.uia.entity.LoginPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("uiaEgovAdminLoginPolicyRepository")
public interface EgovAdminLoginPolicyRepository extends JpaRepository<LoginPolicy, String> {
}
