package com.mx.gillustrated.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate


class ValidateUtils {

    companion object{

         @Suppress("ConvertToStringTemplate", "DEPRECATION")
         @SuppressLint("PackageManagerGetSignatures")
         fun checkAppSignature(context: Context):Int{
             try {
                 val packageInfo = context.packageManager.getPackageInfo(context.packageName,
                         PackageManager.GET_SIGNATURES)
                 packageInfo.signatures.forEach {
                     val certStream = ByteArrayInputStream(it.toByteArray())
                     val certFactory = CertificateFactory.getInstance("X509")
                     val x509Cert = certFactory.generateCertificate(certStream) as X509Certificate

                     Log.d("Validate signature", "Certificate issuer: " + x509Cert.issuerDN )
                     Log.d("Validate signature", "Certificate serialNumber: " + x509Cert.serialNumber )

                     val messageDigest = MessageDigest.getInstance("SHA-256")
                     messageDigest.update(it.toByteArray())
                     val currentSignature = Base64.encodeToString(messageDigest.digest(), Base64.DEFAULT)
                     Log.d("Validate signature", "Certificate SHA Base64: " + currentSignature )
                 }

             }catch (e:Exception){
                 e.printStackTrace()
             }finally {
                 return 0
             }
         }
    }

}

