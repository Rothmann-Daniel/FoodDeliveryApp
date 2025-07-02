package com.example.fooddelivery

import android.content.Context
import android.content.Intent
import android.widget.Toast


object EmailUtils {
    fun sendSupportEmail(context: Context) {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(context.getString(R.string.support_email)))
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.support_email_subject))
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.support_email_body))
        }

        when {
            context.packageManager.queryIntentActivities(emailIntent, 0).isEmpty() -> {
                Toast.makeText(context, R.string.no_email_app, Toast.LENGTH_LONG).show()
            }
            else -> try {
                context.startActivity(
                    Intent.createChooser(
                        emailIntent,
                        context.getString(R.string.choose_email_app)
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(context, R.string.email_send_error, Toast.LENGTH_LONG).show()
            }
        }
    }
}