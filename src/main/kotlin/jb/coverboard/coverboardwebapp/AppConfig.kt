package jb.coverboard.coverboardwebapp

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.session.data.redis.config.ConfigureRedisAction




@Configuration
@EnableScheduling
class AppConfig(val requestLogger: RequestLogger, val customAuthProvider: CustomAuthProvider): WebMvcConfigurer, WebSecurityConfigurerAdapter() {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(requestLogger)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**").allowCredentials(true)
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(customAuthProvider)
    }

    override fun configure(http: HttpSecurity) {
        http
                .antMatcher("/**").cors()
                .and()
                .authorizeRequests()
                .antMatchers("/*.js").permitAll()
                .antMatchers("/*.css").permitAll()
                .antMatchers("/").permitAll()
                .antMatchers("/reg").permitAll()
                .antMatchers("/sp_callback").permitAll()
                .anyRequest().authenticated()
        //super.configure(http)
    }

    @Bean
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

    @Bean
    fun configureRedisAction(): ConfigureRedisAction {
        return ConfigureRedisAction.NO_OP
    }
}