package um.edu.spring.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.zaxxer.hikari.HikariDataSource;

import agentproject.my.agents.SchedulerAgent;
import agentproject.my.agents.EmailAgent;
import jade.core.Agent;
import net.sf.jade4spring.JadeBean;
@Configuration
@ComponentScan (basePackages={"agentproject.my.controller","agentproject.my.delegate"})
@PropertySource("classpath:email.properties")

//Spring Data JPA
@EnableJpaRepositories(basePackages ={"agentproject.my.persisitance.domain"})
@EnableTransactionManagement 
@Transactional
public class SpringContextConfig {
	@Autowired
    private Environment env;
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource){
		LocalContainerEntityManagerFactoryBean entityManagerFactory=new LocalContainerEntityManagerFactoryBean();
		Properties  jpaProperties=new Properties ();
		jpaProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
		jpaProperties.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
		jpaProperties.setProperty("show_sql", "true");
		jpaProperties.setProperty("hibernate.hbm2ddl.auto","update");
		//jpaProperties.setProperty("hibernate.connection.url", "jdbc:oracle://localhost:1522/orcl11g");
		entityManagerFactory.setJpaProperties(jpaProperties);
		entityManagerFactory.setPackagesToScan("agentproject.my.persistance.model");
		entityManagerFactory.setPersistenceProvider(new HibernatePersistenceProvider());
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(false);
		vendorAdapter.setShowSql(true);
		entityManagerFactory.setJpaVendorAdapter(vendorAdapter);
		entityManagerFactory.setDataSource(dataSource);
		return entityManagerFactory;
	}
	
	
	@Bean
	public DataSource dataSource(){
		HikariDataSource dataSource=new HikariDataSource();
		dataSource.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
		//dataSource.setJdbcUrl("jdbc:oracle:thin:@localhost:1521:orcl11g");
		dataSource.addDataSourceProperty("serverName", "localhost");
		dataSource.addDataSourceProperty("portNumber", "3306");
		dataSource.addDataSourceProperty("databaseName", "mydb");
		//dataSource.addDataSourceProperty("driverType", "thin");
		dataSource.setUsername("root");
		dataSource.setPassword("root");
		return dataSource;
	}
	@Bean
	public JpaTransactionManager transactionManager(DataSource dataSource, EntityManagerFactory entityManagerFactory){
		JpaTransactionManager jpaTransactionManager=new JpaTransactionManager(entityManagerFactory);
		jpaTransactionManager.setDataSource(dataSource);
		return jpaTransactionManager;
		
	}
	//TODO without this datasource is currently being created would be raise and bean was not created???
	@Bean 
		PersistenceExceptionTranslationPostProcessor exceptionTranslation(){
			return new PersistenceExceptionTranslationPostProcessor();	
		}
	
	@Bean(initMethod="startContainer",destroyMethod="stopContainer") 
	JadeBean getJadeBean(){
		System.out.println("Jade is starting");
		JadeBean jadeBean=new JadeBean();
		
		Map autostartAgents=new HashMap<Object, Object>();
		autostartAgents.put("scheduleAgent",getSchedulerAgent() );
		autostartAgents.put("emailAgent",getEmailAgent() );
		jadeBean.setAutostartAgents(autostartAgents);
		//jadeBean.setPropertiesFile("../../../jade.properties");
		//jadeBean.startContainer();
			return jadeBean;
	}
	
	@Bean
	public Agent getSchedulerAgent(){
		return new SchedulerAgent();
	}
	
	@Bean
	public Agent getEmailAgent(){
		return new EmailAgent();
	}
	
	 @Bean
	    public JavaMailSenderImpl javaMailSenderImpl() {
	        final JavaMailSenderImpl mailSenderImpl = new JavaMailSenderImpl();

	        try {
	            mailSenderImpl.setHost(env.getRequiredProperty("smtp.host"));
	            mailSenderImpl.setPort(env.getRequiredProperty("smtp.port", Integer.class));
	            mailSenderImpl.setProtocol(env.getRequiredProperty("smtp.protocol"));
	            mailSenderImpl.setUsername(env.getRequiredProperty("smtp.username"));
	            mailSenderImpl.setPassword(env.getRequiredProperty("smtp.password"));
	        } catch (IllegalStateException ise) {
	            //LOGGER.error("Could not resolve email.properties.  See email.properties.sample");
	            throw ise;
	        }
	        final Properties javaMailProps = new Properties();
	        javaMailProps.put("mail.smtp.auth", true);
	        javaMailProps.put("mail.smtp.starttls.enable", true);
	        mailSenderImpl.setJavaMailProperties(javaMailProps);
	        return mailSenderImpl;
	    }
}
