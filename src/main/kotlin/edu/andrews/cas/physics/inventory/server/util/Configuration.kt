package edu.andrews.cas.physics.inventory.server.util

import edu.andrews.cas.physics.inventory.server.interceptor.AuthenticationInterceptor
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.*
import javax.crypto.SecretKey
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import javax.mail.Session

@Configuration
open class Configuration @Autowired constructor(private val authenticationInterceptor: AuthenticationInterceptor) : WebMvcConfigurer {
    @Primary
    @Bean("configProperties")
    open fun configProperties(): Properties {
        val config = Properties()
        config.load(ClassLoader.getSystemResourceAsStream("config.properties"))
        return config
    }

    @Bean
    open fun secretKey(): SecretKey {
        return Keys.secretKeyFor(SignatureAlgorithm.HS256)
    }

    @Bean("emailProperties")
    open fun emailProperties() : Properties {
        val mailConfig = Properties()
        mailConfig.load(ClassLoader.getSystemResourceAsStream("email.properties"))
        return mailConfig
    }

    @Autowired
    @Bean
    open fun emailSession(@Qualifier("emailProperties") mailConfig: Properties): Session {
        return Session.getInstance(mailConfig, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(mailConfig.getProperty("mail.smtp.user"), mailConfig.getProperty("mail.smtp.password"))
            }
        })
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
    }
}