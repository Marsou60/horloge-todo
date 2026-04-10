package com.marsou.notesfrais.ui.camera

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object OcrProcessor {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    data class OcrResult(
        val rawText: String,
        val extractedAmount: Double?,
        val extractedTitle: String?
    )

    suspend fun processImage(bitmap: Bitmap): OcrResult = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val rawText = visionText.text
                val amount = extractAmount(rawText)
                val title = extractTitle(rawText)
                cont.resume(OcrResult(rawText, amount, title))
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }

    /**
     * Tries to detect the largest monetary amount in the text.
     * Handles patterns like: 12,50€ / 12.50 EUR / TOTAL 45,00
     */
    private fun extractAmount(text: String): Double? {
        // Patterns for amounts in French receipts
        val amountPatterns = listOf(
            // TOTAL or MONTANT followed by amount
            Regex("""(?:TOTAL|MONTANT|À PAYER|NET À PAYER|MONTANT TTC)\s*:?\s*(\d+[,.]?\d*)\s*€?""", RegexOption.IGNORE_CASE),
            // Amount followed by € sign
            Regex("""(\d{1,6}[,.]?\d{0,2})\s*€"""),
            // Amount followed by EUR
            Regex("""(\d{1,6}[,.]?\d{0,2})\s*EUR""", RegexOption.IGNORE_CASE),
            // General large amount
            Regex("""(\d{1,6}[,.]\d{2})""")
        )

        val candidates = mutableListOf<Double>()

        for (pattern in amountPatterns) {
            val matches = pattern.findAll(text)
            for (match in matches) {
                val amountStr = match.groupValues[1].replace(",", ".")
                amountStr.toDoubleOrNull()?.let { candidates.add(it) }
            }
            if (candidates.isNotEmpty()) break
        }

        return if (candidates.isNotEmpty()) candidates.maxOrNull() else null
    }

    /**
     * Tries to extract a merchant name from the first non-empty lines of the text.
     */
    private fun extractTitle(text: String): String? {
        val lines = text.lines()
            .map { it.trim() }
            .filter { it.length > 2 && !it.matches(Regex("""^\d+.*""")) }

        return lines.firstOrNull()?.take(50)
    }
}
