package com.imashnake.animite.social.util

import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * Converts a Firestore / network failure into a message that tells the user (and the developer)
 * the real reason instead of failing silently.
 */
fun Throwable.toSocialErrorMessage(fallback: String): String {
    val firestoreError = this as? FirebaseFirestoreException
        ?: (cause as? FirebaseFirestoreException)
    return when (firestoreError?.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "تم رفض الصلاحية من قاعدة البيانات. تأكد من تسجيل الدخول ومن نشر قواعد Firestore الأخيرة."
        FirebaseFirestoreException.Code.UNAVAILABLE ->
            "تعذر الاتصال بالخادم. تحقق من الإنترنت ثم حاول مرة أخرى."
        FirebaseFirestoreException.Code.FAILED_PRECONDITION ->
            "الاستعلام يحتاج إلى فهرس (Index) غير منشور في Firestore."
        FirebaseFirestoreException.Code.UNAUTHENTICATED ->
            "انتهت الجلسة. سجّل الدخول من جديد."
        else -> localizedMessage?.takeIf { it.isNotBlank() } ?: fallback
    }
}
