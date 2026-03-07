package net.canfar.verbinal.data.repository

import net.canfar.verbinal.data.model.StorageQuota
import net.canfar.verbinal.data.remote.StorageApi
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository
@Inject
constructor(
    private val storageApi: StorageApi,
) {
    suspend fun getQuota(username: String): StorageQuota? {
        val response = storageApi.getStorageQuota(username)
        if (!response.isSuccessful) {
            throw Exception("Storage returned ${response.code()} ${response.message()}")
        }
        val xml = response.body() ?: return null
        return parseVoSpaceXml(xml)
    }

    private fun parseVoSpaceXml(xml: String): StorageQuota? = try {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var quota = 0L
        var size = 0L
        var date: String? = null
        var depth = 0
        var inProperty = false
        var currentUri = ""

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "node" || parser.name == "Node") depth++
                    if (parser.name == "property" && depth <= 1) {
                        inProperty = true
                        currentUri = parser.getAttributeValue(null, "uri") ?: ""
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inProperty && depth <= 1) {
                        val value = parser.text?.trim() ?: ""
                        when {
                            currentUri.contains("core#quota") -> quota = value.toLongOrNull() ?: 0
                            currentUri.contains("core#length") -> size = value.toLongOrNull() ?: 0
                            currentUri.contains("core#date") -> date = value
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "property") inProperty = false
                    if (parser.name == "node" || parser.name == "Node") depth--
                }
            }
            parser.next()
        }

        StorageQuota(quotaBytes = quota, usedBytes = size, lastModified = date)
    } catch (e: Exception) {
        null
    }
}
