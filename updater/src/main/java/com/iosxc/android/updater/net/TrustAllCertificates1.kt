package com.iosxc.android.updater.net

import javax.net.ssl.*
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

/**
 * Created by Crazz on 2017/5/16.
 */

class TrustAllCertificates : X509TrustManager, HostnameVerifier {
    override fun verify(hostname: String, session: SSLSession): Boolean {
        return true
    }

    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {

    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {

    }

    override fun getAcceptedIssuers(): Array<X509Certificate>? {
        return null
    }

    companion object {

        fun install() {
            try {
                val trustAll = TrustAllCertificates()

                val sc = SSLContext.getInstance("SSL")
                sc.init(
                    null,
                    arrayOf<TrustManager>(trustAll),
                    java.security.SecureRandom()
                )
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)

                HttpsURLConnection.setDefaultHostnameVerifier(trustAll)
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("Failed setting up all thrusting certificate manager.", e)
            } catch (e: KeyManagementException) {
                throw RuntimeException("Failed setting up all thrusting certificate manager.", e)
            }

        }
    }
}
