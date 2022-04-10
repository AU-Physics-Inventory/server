package edu.andrews.cas.physics.inventory.server

import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.util.*
import javax.crypto.SecretKey

@Configuration
open class Configuration {
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
}