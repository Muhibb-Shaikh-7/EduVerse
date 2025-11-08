package com.example.eduverse.data.firebase

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toFile
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OCR Service using Google ML Kit
 *
 * Provides text recognition capabilities for:
 * - Images (JPG, PNG)
 * - PDF pages (converted to images)
 * - Camera captures
 */
@Singleton
class OCRService @Inject constructor(
    private val textRecognizer: TextRecognizer,
    @ApplicationContext private val context: Context
) {
    /**
     * Extract text from an image URI
     */
    suspend fun extractTextFromUri(uri: Uri): Result<String> {
        return try {
            val inputImage = InputImage.fromFilePath(context, uri)
            val result = textRecognizer.process(inputImage).await()
            val text = result.text

            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extract text from a Bitmap image
     */
    suspend fun extractTextFromBitmap(bitmap: Bitmap): Result<OCRResult> {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(inputImage).await()

            val blocks = result.textBlocks.map { block ->
                TextBlock(
                    text = block.text,
                    confidence = 1.0f, // ML Kit Text Recognition doesn't provide confidence scores
                    boundingBox = block.boundingBox,
                    lines = block.lines.map { line ->
                        TextLine(
                            text = line.text,
                            confidence = 1.0f // ML Kit Text Recognition doesn't provide confidence scores
                        )
                    }
                )
            }

            val ocrResult = OCRResult(
                fullText = result.text,
                blocks = blocks,
                confidence = if (blocks.isNotEmpty()) 1.0f else 0f
            )

            Result.success(ocrResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extract text from a file
     */
    suspend fun extractTextFromFile(file: File): Result<String> {
        return try {
            val inputImage = InputImage.fromFilePath(context, Uri.fromFile(file))
            val result = textRecognizer.process(inputImage).await()
            val text = result.text

            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extract structured text with formatting preservation
     */
    suspend fun extractStructuredText(bitmap: Bitmap): Result<StructuredText> {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(inputImage).await()

            val paragraphs = mutableListOf<Paragraph>()
            var currentParagraph = mutableListOf<String>()

            result.textBlocks.forEach { block ->
                block.lines.forEach { line ->
                    currentParagraph.add(line.text)
                }

                if (currentParagraph.isNotEmpty()) {
                    paragraphs.add(
                        Paragraph(
                            lines = currentParagraph.toList(),
                            fullText = currentParagraph.joinToString("\n")
                        )
                    )
                    currentParagraph = mutableListOf()
                }
            }

            val structuredText = StructuredText(
                paragraphs = paragraphs,
                fullText = result.text
            )

            Result.success(structuredText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clean up resources
     */
    fun close() {
        textRecognizer.close()
    }
}

/**
 * Data class for OCR result with detailed information
 */
data class OCRResult(
    val fullText: String,
    val blocks: List<TextBlock>,
    val confidence: Float
)

/**
 * Data class for text block
 */
data class TextBlock(
    val text: String,
    val confidence: Float,
    val boundingBox: android.graphics.Rect?,
    val lines: List<TextLine>
)

/**
 * Data class for text line
 */
data class TextLine(
    val text: String,
    val confidence: Float
)

/**
 * Data class for structured text with paragraphs
 */
data class StructuredText(
    val paragraphs: List<Paragraph>,
    val fullText: String
)

/**
 * Data class for paragraph
 */
data class Paragraph(
    val lines: List<String>,
    val fullText: String
)
