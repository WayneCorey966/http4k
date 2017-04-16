package org.reekwest.http.contract

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import org.junit.Test
import org.reekwest.http.core.ContentType.Companion.APPLICATION_FORM_URLENCODED
import org.reekwest.http.core.body.toBody
import org.reekwest.http.core.contract.*
import org.reekwest.http.core.contract.ContractBreach.Companion.Invalid
import org.reekwest.http.core.contract.Header.Common.CONTENT_TYPE
import org.reekwest.http.core.get

class FormTest {

    private val emptyRequest = get("")

    @Test
    fun `can get form body`() {
        val request = emptyRequest.copy(
            headers = listOf("Content-Type" to APPLICATION_FORM_URLENCODED.value),
            body = "hello=world&another=planet".toBody())
        val expected = mapOf("hello" to listOf("world"), "another" to listOf("planet"))
        assertThat(Body.form()(request), equalTo(expected))
    }

    @Test
    fun `form body blows up if not URL content type`() {
        val request = emptyRequest.copy(
            headers = listOf("Content-Type" to "unknown"),
            body = "hello=world&another=planet".toBody())
        assertThat({ Body.form()(request) }, throws(equalTo(Invalid(CONTENT_TYPE))))
    }

    @Test
    fun `validating form blows up if not URL content type`() {
        val request = emptyRequest.copy(
            headers = listOf("Content-Type" to "unknown"),
            body = "hello=world&another=123".toBody())

        assertThat({
            Body.validatingForm(
                FormField.required("hello"),
                FormField.int().required("another")
            )(request)
        }, throws(equalTo(Invalid(CONTENT_TYPE))))
    }

    @Test
    fun `validating form extracts ok form values`() {
        val request = emptyRequest.copy(
            headers = listOf("Content-Type" to APPLICATION_FORM_URLENCODED.value),
            body = "hello=world&another=123".toBody())

        val expected = mapOf("hello" to listOf("world"), "another" to listOf("123"))

        assertThat(Body.validatingForm(
            FormField.required("hello"),
            FormField.int().required("another")
        )(request), equalTo(ValidatingForm(expected)))
    }

    @Test
    fun `validating form blows up with invalid form values`() {
        val request = emptyRequest.copy(
            headers = listOf("Content-Type" to APPLICATION_FORM_URLENCODED.value),
            body = "another=notANumber".toBody())

        val stringRequiredField = FormField.required("hello")
        val intRequiredField = FormField.int().required("another")
        assertThat(
            { Body.validatingForm(stringRequiredField, intRequiredField)(request) },
            throws(equalTo(ContractBreach(Missing(stringRequiredField.meta), Invalid(intRequiredField.meta))))
        )
    }
}


