package jb.coverboard.coverboardwebapp

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.WebSecurityConfigurer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.CorsRegistry


@Configuration
class Intercept(val authInterceptor: AuthInterceptor): WebMvcConfigurer, WebSecurityConfigurerAdapter() {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**").allowCredentials(true)
    }

    override fun configure(http: HttpSecurity) {
        http.antMatcher("/**").anonymous()
        http.antMatcher("/**").cors()
        //super.configure(http)
    }
}