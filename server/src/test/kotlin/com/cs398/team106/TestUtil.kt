package com.cs398.team106

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cs398.team106.authentication.UserAuthentication
import java.util.*

class TestUtil {
    companion object {
        fun getJWT(email: String, userID: Int): String {
            return JWT.create()
                .withIssuer("http://0.0.0.0:8080/")
                .withClaim(UserAuthentication.emailClaim, email)
                .withClaim(UserAuthentication.userIdClaim, userID)
                .withExpiresAt(Date(System.currentTimeMillis() + UserAuthentication.jwtTTL))
                .sign(Algorithm.HMAC256("secret"))
        }
    }
}