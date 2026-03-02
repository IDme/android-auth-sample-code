package com.idme.auth.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Response from the ID.me `/api/public/v3/userinfo` endpoint (OAuth mode).
 *
 * This format is returned when using `OAUTH` or `OAUTH_PKCE` auth modes.
 * The endpoint returns a JWT whose payload contains `attributes` and `status` arrays.
 * OIDC mode returns standard UserInfo instead.
 */
@Serializable(with = AttributeResponseSerializer::class)
data class AttributeResponse(
    /** The user's verified attributes (e.g. name, email, zip). */
    val attributes: List<Attribute>,

    /** The user's verification statuses by group. */
    val status: List<VerificationStatus>
) {
    @Serializable
    data class Attribute(
        /** Machine-readable key (e.g. "fname", "lname", "email", "uuid", "zip"). */
        val handle: String,

        /** Human-readable label (e.g. "First Name"). */
        val name: String,

        /** The attribute value. */
        val value: String? = null
    )

    @Serializable
    data class VerificationStatus(
        /** The verification group (e.g. "military", "student"). */
        val group: String,

        /** Subgroups within the group (e.g. ["Service Member", "Veteran"]). */
        val subgroups: List<String>? = null,

        /** Whether the user is verified for this group. */
        val verified: Boolean
    )
}

internal object AttributeResponseSerializer : KSerializer<AttributeResponse> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("AttributeResponse")

    override fun serialize(encoder: Encoder, value: AttributeResponse) {
        // Default serialization for encoding
        val jsonEncoder = encoder as? kotlinx.serialization.json.JsonEncoder ?: return
        val json = kotlinx.serialization.json.buildJsonObject {
            put("attributes", kotlinx.serialization.json.Json.encodeToJsonElement(
                kotlinx.serialization.builtins.ListSerializer(AttributeResponse.Attribute.serializer()),
                value.attributes
            ))
            put("status", kotlinx.serialization.json.Json.encodeToJsonElement(
                kotlinx.serialization.builtins.ListSerializer(AttributeResponse.VerificationStatus.serializer()),
                value.status
            ))
        }
        jsonEncoder.encodeJsonElement(json)
    }

    override fun deserialize(decoder: Decoder): AttributeResponse {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return AttributeResponse(emptyList(), emptyList())

        val element = jsonDecoder.decodeJsonElement()
        if (element !is JsonObject) return AttributeResponse(emptyList(), emptyList())

        val obj = element.jsonObject

        // Try the expected { "attributes": [...], "status": [...] } format first
        val attributesElement = obj["attributes"]
        if (attributesElement != null) {
            val attrs = try {
                attributesElement.jsonArray.map { attrElement ->
                    val attrObj = attrElement.jsonObject
                    AttributeResponse.Attribute(
                        handle = attrObj["handle"]?.jsonPrimitive?.content ?: "",
                        name = attrObj["name"]?.jsonPrimitive?.content ?: "",
                        value = attrObj["value"]?.jsonPrimitive?.contentOrNull
                    )
                }
            } catch (_: Exception) {
                emptyList()
            }

            val statuses = try {
                obj["status"]?.jsonArray?.map { statusElement ->
                    val statusObj = statusElement.jsonObject
                    AttributeResponse.VerificationStatus(
                        group = statusObj["group"]?.jsonPrimitive?.content ?: "",
                        subgroups = statusObj["subgroups"]?.jsonArray?.map {
                            it.jsonPrimitive.content
                        },
                        verified = statusObj["verified"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
                    )
                } ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }

            return AttributeResponse(attrs, statuses)
        }

        // Fallback: the payload may be flat JWT claims -- wrap all string values as attributes
        val attrs = obj.entries.mapNotNull { (key, value) ->
            val strValue = value.jsonPrimitive.contentOrNull
            if (strValue != null) {
                AttributeResponse.Attribute(handle = key, name = key, value = strValue)
            } else {
                null
            }
        }

        return AttributeResponse(attrs, emptyList())
    }
}
