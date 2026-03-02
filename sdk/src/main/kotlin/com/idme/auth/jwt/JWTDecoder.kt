package com.idme.auth.jwt

import com.idme.auth.errors.IDmeAuthError
import com.idme.auth.utilities.Base64URL
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/** Decoded JWT components. */
data class DecodedJWT(
    val header: JWTHeader,
    val payload: Map<String, Any>,
    val signatureData: ByteArray,
    /** "header.payload" for signature verification */
    val signedPortion: String
) {
    data class JWTHeader(
        val alg: String,
        val kid: String?
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DecodedJWT) return false
        return header == other.header && payload == other.payload &&
            signatureData.contentEquals(other.signatureData) &&
            signedPortion == other.signedPortion
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + payload.hashCode()
        result = 31 * result + signatureData.contentHashCode()
        result = 31 * result + signedPortion.hashCode()
        return result
    }
}

/** Decodes a JWT string into its header, payload, and signature components. */
object JWTDecoder {

    fun decode(token: String): DecodedJWT {
        val parts = token.split(".")
        if (parts.size != 3) {
            throw IDmeAuthError.InvalidJWT("JWT must have 3 parts, found ${parts.size}")
        }

        val headerPart = parts[0]
        val payloadPart = parts[1]
        val signaturePart = parts[2]

        // Decode header
        val headerData = Base64URL.decode(headerPart)
            ?: throw IDmeAuthError.InvalidJWT("Failed to decode JWT header")
        val headerJSON = try {
            Json.parseToJsonElement(String(headerData, Charsets.UTF_8)) as JsonObject
        } catch (e: Exception) {
            throw IDmeAuthError.InvalidJWT("Failed to decode JWT header")
        }

        val alg = (headerJSON["alg"] as? JsonPrimitive)?.content
            ?.ifEmpty { null }
            ?: throw IDmeAuthError.InvalidJWT("Missing 'alg' in JWT header")
        val kid = (headerJSON["kid"] as? JsonPrimitive)?.content

        // Decode payload
        val payloadData = Base64URL.decode(payloadPart)
            ?: throw IDmeAuthError.InvalidJWT("Failed to decode JWT payload")
        val payloadJSON = try {
            Json.parseToJsonElement(String(payloadData, Charsets.UTF_8)) as JsonObject
        } catch (e: Exception) {
            throw IDmeAuthError.InvalidJWT("Failed to decode JWT payload")
        }

        val payloadMap = mutableMapOf<String, Any>()
        for ((key, value) in payloadJSON) {
            payloadMap[key] = (value as? JsonPrimitive)?.content ?: value.toString()
        }

        // Decode signature
        val signatureData = Base64URL.decode(signaturePart)
            ?: throw IDmeAuthError.InvalidJWT("Failed to decode JWT signature")

        val signedPortion = "$headerPart.$payloadPart"

        return DecodedJWT(
            header = DecodedJWT.JWTHeader(alg = alg, kid = kid),
            payload = payloadMap,
            signatureData = signatureData,
            signedPortion = signedPortion
        )
    }
}
