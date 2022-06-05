package edu.andrews.cas.physics.inventory.server.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import java.util.*
import javax.annotation.PostConstruct

@Configuration
open class KeystoreConfiguration @Autowired constructor(private val config: Properties) {

    @PostConstruct
    open fun setupKeyTrustStores() {
        System.setProperty("javax.net.ssl.keyStore", config.getProperty("truststore.path"))
        System.setProperty("javax.net.ssl.keyStorePassword", config.getProperty("truststore.pass"))
        System.setProperty("javax.net.ssl.trustStore", config.getProperty("truststore.path"))
        System.setProperty("javax.net.ssl.trustStorePassword", config.getProperty("truststore.pass"))
    }
}