package com.idme.auth.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull

/** User profile information returned from the ID.me UserInfo endpoint. */
@Serializable
data class UserInfo(
    /** The user's unique identifier. */
    val sub: String? = null,

    /** The user's email address. */
    val email: String? = null,

    /** Whether the user's email is verified. */
    @SerialName("email_verified")
    @Serializable(with = FlexibleBooleanSerializer::class)
    val emailVerified: Boolean? = null,

    /** The user's given (first) name. */
    @SerialName("given_name")
    val givenName: String? = null,

    /** The user's family (last) name. */
    @SerialName("family_name")
    val familyName: String? = null,

    /** The user's full name. */
    val name: String? = null,

    /** The user's profile picture URL. */
    val picture: String? = null
)

/**
 * Serializer that handles `email_verified` as either a Boolean or a String ("true"/"false").
 */
internal object FlexibleBooleanSerializer : KSerializer<Boolean?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleBoolean", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Boolean?) {
        if (value != null) encoder.encodeBoolean(value)
    }

    override fun deserialize(decoder: Decoder): Boolean? {
        return when (decoder) {
            is JsonDecoder -> {
                val element = decoder.decodeJsonElement()
                if (element is JsonPrimitive) {
                    element.booleanOrNull ?: element.contentOrNull?.lowercase()?.toBooleanStrictOrNull()
                } else {
                    null
                }
            }
            else -> try {
                decoder.decodeBoolean()
            } catch (_: Exception) {
                try {
                    decoder.decodeString().lowercase().toBooleanStrictOrNull()
                } catch (_: Exception) {
                    null
                }
            }
        }
    }
}
