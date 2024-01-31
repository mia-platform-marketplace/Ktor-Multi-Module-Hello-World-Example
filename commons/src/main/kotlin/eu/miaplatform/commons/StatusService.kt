package eu.miaplatform.commons

import java.util.*

object StatusService {
    private val versionProperties = Properties().apply {
        try {
            load(javaClass.getResourceAsStream("/gradle.properties"))
        } catch (e: Exception) {
            println(e.localizedMessage)
        }
    }

    fun getVersion() : String {
        return versionProperties.getProperty("version") ?: "no version"
    }
}