package com.idme.auth.networking

import com.idme.auth.utilities.Base64URL
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class Base64URLTests {

    @Test
    fun `encodes empty data`() {
        assertEquals("", Base64URL.encode(byteArrayOf()))
    }

    @Test
    fun `round-trip encode and decode`() {
        val original = "Hello, World! This is a test string for Base64URL encoding."
        val data = original.toByteArray(Charsets.UTF_8)
        val encoded = Base64URL.encode(data)
        val decoded = Base64URL.decode(encoded)
        assertArrayEquals(data, decoded)
    }

    @Test
    fun `no padding characters in output`() {
        val data = byteArrayOf(0x01, 0x02, 0x03)
        val encoded = Base64URL.encode(data)
        assertFalse(encoded.contains("="))
    }

    @Test
    fun `uses URL-safe characters`() {
        val data = byteArrayOf(0xFB.toByte(), 0xFF.toByte(), 0xFE.toByte())
        val encoded = Base64URL.encode(data)
        assertFalse(encoded.contains("+"))
        assertFalse(encoded.contains("/"))
    }

    @Test
    fun `decodes strings without padding`() {
        val decoded = Base64URL.decode("YQ")
        assertArrayEquals("a".toByteArray(), decoded)
    }

    @Test
    fun `known test vectors`() {
        assertEquals("Zg", Base64URL.encode("f".toByteArray()))
        assertEquals("Zm8", Base64URL.encode("fo".toByteArray()))
        assertEquals("Zm9v", Base64URL.encode("foo".toByteArray()))
    }
}
