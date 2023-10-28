package com.doubtnut.core.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UtilsTest {

    @Test
    fun isAlphaNumeric_providedAlphaNumeric_returnsTrue() {
        val testString = "abc123"
        val result = Utils.isAlphaNumeric(testString)
        assertThat(result).isTrue()
    }

    @Test
    fun isAlphaNumeric_providedStringWithSpecialCharacters_returnsFalse() {
        val testString = "abc123#&("
        val result = Utils.isAlphaNumeric(testString)
        assertThat(result).isFalse()
    }

    @Test
    fun isAlphaNumeric_providedStringWithValidCharacterAndWhiteSpaces_returnsTrue() {
        val testString = "abc abc"
        val testString2 = "abc abc  abc   abc"
        val result1 = Utils.isAlphaNumeric(testString)
        val result2 = Utils.isAlphaNumeric(testString2)
        assertThat(result1).isTrue()
        assertThat(result2).isTrue()
    }

    @Test
    fun isAlphaNumeric_providedStringWithOnlySpecialCharacters_returnsFalse() {
        val testString = "%&)$"
        val result = Utils.isAlphaNumeric(testString)
        assertThat(result).isFalse()
    }

    @Test
    fun isAlphaNumeric_providedStringWithSpecialCharactersAndSpaces_returnsFalse() {
        val testString = "%&)$ ^@{\n "
        val result = Utils.isAlphaNumeric(testString)
        assertThat(result).isFalse()
    }
}