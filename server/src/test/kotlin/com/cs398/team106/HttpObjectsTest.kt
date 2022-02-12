package com.cs398.team106

import kotlin.test.Test

class HttpObjectsTest {

    @Test
    fun loginIsValidTest() {
        assert(Login("test", "password").isValid())
    }

    @Test
    fun loginNotValidTest() {
        assert(!Login("test", "").isValid())
    }

    @Test
    fun userIsValidTest() {
        assert(User("first", "last", "test", "password").isValid())
    }

    @Test
    fun userNotValidTest() {
        assert(!User("first", "last", "test", "").isValid())
    }
}