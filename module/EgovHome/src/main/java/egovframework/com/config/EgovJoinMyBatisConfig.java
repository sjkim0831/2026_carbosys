package egovframework.com.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = {
        "egovframework.com.uss.umt.service.impl" }, sqlSessionFactoryRef = "egov.sqlSession")
public class EgovJoinMyBatisConfig {

    @Bean(name = "egov.sqlSession")
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(resolver.getResources("classpath:/egovframework/mapper/com/**/*.xml"));

        sessionFactory.setTypeAliasesPackage("egovframework.com.cmm,egovframework.com.uss.umt.service");
        sessionFactory.setTypeAliases(new Class[] { org.egovframe.rte.psl.dataaccess.util.EgovMap.class });

        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        sessionFactory.setConfiguration(configuration);

        return sessionFactory.getObject();
    }
}
