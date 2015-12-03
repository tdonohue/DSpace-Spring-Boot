/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Web Application Security Configuration, via Spring Security
 * @author Tim Donohue
 */
@Configuration
@EnableWebSecurity
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
    
    @Override
    public void configure(WebSecurity web) throws Exception 
    {
        // Ensure all public or theme files are never checked for access rights
        web.ignoring()
           .antMatchers("/css/**")
           .antMatchers("/images/**")
           .antMatchers("/webjars/**")
           .antMatchers("/robots.txt");
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception 
    {
        http
            // Require authorization for /edit paths
            // Require ADMIN role for /admin paths
            .authorizeRequests()
                .antMatchers("/edit/**").authenticated()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
                .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll();
    }

    /**
     * A hack for now. We're going to hardcode a single valid user here.
     * OBVIOUSLY, this isn't the way to do things, but prototypes the idea.
     * In reality, Spring Security supports:
     *    * Database AuthN
     *    * OAuth: http://projects.spring.io/spring-security-oauth/
     *    * LDAP: https://spring.io/guides/gs/authenticating-ldap/
     *    * Shibboleth (SAML): http://projects.spring.io/spring-security-saml/
     *    * (and many many more)
     * 
     * @param auth
     * @throws Exception 
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            // Define a two valid user accts / roles
            auth.inMemoryAuthentication()
                .withUser("admin").password("dspace").roles("ADMIN","USER").and()
                .withUser("user").password("dspace").roles("USER");
    }
}
