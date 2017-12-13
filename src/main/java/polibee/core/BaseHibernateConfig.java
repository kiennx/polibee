package polibee.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public abstract class BaseHibernateConfig {
    protected final ApplicationContext context;

    @Autowired
    public BaseHibernateConfig(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Get the resource Hibernate configuration
     * @return Resource
     */
    public abstract Resource getConfigLocation();

    /**
     * Get list of annotated classes in this application
     * @return Array of Annotated Class
     */
    public abstract Class[] getAnnotatedClasses();

    @Bean
    public LocalSessionFactoryBean getSessionFactory() {
        LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();
        factoryBean.setConfigLocation(getConfigLocation());
        factoryBean.setAnnotatedClasses(getAnnotatedClasses());
        return factoryBean;
    }

    @Bean
    public HibernateTransactionManager getTransactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(getSessionFactory().getObject());
        return transactionManager;
    }
}