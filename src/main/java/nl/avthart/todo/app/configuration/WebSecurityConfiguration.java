package nl.avthart.todo.app.configuration;

import java.util.Set;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.bind.annotation.RequestMethod;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    private static final String[] PATHS_NO_AUTH = {
            "/actuator/**",
            "/css/**", "/js/**", "/img/**",
            "/h2-console", "/h2-console/**",
            };

    private final Endpoint endpoint;

    public WebSecurityConfiguration( Endpoint endpoint ) {
        this.endpoint = endpoint;
    }

    @Override
    protected void configure( HttpSecurity http )
            throws Exception {

        for ( RequestMethod rm : RequestMethod.values() ) {
            Set<String> paths = endpoint.adminPathsFor( rm );
            if ( !paths.isEmpty() ) {
                System.out.println( rm + " (admin): " + paths );
            }
            paths = endpoint.userPathsFor( rm );
            if ( !paths.isEmpty() ) {
                System.out.println( rm + " (user): " + paths );
            }
        }

        http
                .authorizeRequests()
                .antMatchers( PATHS_NO_AUTH ).permitAll()
                .anyRequest().authenticated();

        http
                .headers()
                .frameOptions().disable();

        http
                .csrf()
                .disable()
                .formLogin()
                .defaultSuccessUrl( "/index.html" )
                .loginPage( "/login.html" )
                .failureUrl( "/login.html?error" )
                .permitAll()
                .and()
                .logout()
                .logoutSuccessUrl( "/login.html?logout" )
                .permitAll()
        ;
    }

    @Override
    protected void configure( AuthenticationManagerBuilder auth )
            throws Exception {
        auth
                .inMemoryAuthentication()
                .withUser( "albert" ).password( "{noop}1234" ).roles( "USER" ).and()
                .withUser( "foo" ).password( "{noop}bar" ).roles( "USER" );
    }
}
