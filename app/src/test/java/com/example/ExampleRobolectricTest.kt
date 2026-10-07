package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Claude Galaxy", appName)
  }

  @Test
  fun `repository saves and retrieves text file correctly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.repository.ClaudeS40Repository(context)
    val file = repo.saveTextFile("TEST_MEMO", "Nokia 6300 nostalgia content")

    val allFiles = repo.savedFilesFlow.value
    org.junit.Assert.assertTrue(allFiles.any { it.id == file.id })
  }

  @Test
  fun `repository saves and toggles task correctly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.repository.ClaudeS40Repository(context)
    repo.addTask("Test Task on A53", "Keypad test")

    val tasks = repo.tasksFlow.value
    val created = tasks.find { it.title == "Test Task on A53" }
    org.junit.Assert.assertNotNull(created)
    repo.toggleTask(created!!.id)
    val updated = repo.tasksFlow.value.find { it.id == created.id }
    org.junit.Assert.assertTrue(updated!!.isCompleted)
  }

  @Test
  fun `room database message repository persists and retrieves chat messages`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val messageRepo = com.example.repository.MessageRepository(context)

    val testMsg = com.example.model.ChatMessage(
      id = "test-msg-123",
      conversationId = "default-chat",
      role = "user",
      content = "Hello from Room persistence test",
      agentName = "Web Searcher"
    )

    val savedList = messageRepo.saveMessage(testMsg)
    org.junit.Assert.assertTrue(savedList.any { it.id == "test-msg-123" })

    val fetched = messageRepo.getMessagesSync("default-chat")
    val found = fetched.find { it.id == "test-msg-123" }
    org.junit.Assert.assertNotNull(found)
    assertEquals("Hello from Room persistence test", found?.content)
    assertEquals("Web Searcher", found?.agentName)
  }

  @Test
  fun `speech recognizer helper initializes with idle state`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val speechHelper = com.example.speech.OneUiSpeechRecognizer(context)
    org.junit.Assert.assertEquals(com.example.speech.SpeechState.Idle, speechHelper.speechState.value)
  }

  @Test
  fun `smart reply engine produces three context-aware suggestions`() {
    val codeMsg = com.example.model.ChatMessage(
      role = "assistant",
      content = "Here is the Kotlin code:\n```kotlin\nfun calculate(): Int = 42\n```"
    )
    val suggestions = com.example.util.SmartReplyEngine.generateSuggestions(codeMsg, "en")
    assertEquals(3, suggestions.size)
    org.junit.Assert.assertTrue(suggestions.any { it.contains("step-by-step", ignoreCase = true) || it.contains("test", ignoreCase = true) })

    val questionMsg = com.example.model.ChatMessage(
      role = "assistant",
      content = "Would you like to proceed with the Room database setup?"
    )
    val qSuggestions = com.example.util.SmartReplyEngine.generateSuggestions(questionMsg, "en")
    assertEquals(3, qSuggestions.size)
    org.junit.Assert.assertTrue(qSuggestions.any { it.contains("Yes", ignoreCase = true) || it.contains("recommend", ignoreCase = true) })
  }

  @Test
  fun `audio capture helper initializes with idle state and default amplitude`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val audioCaptureHelper = com.example.audio.AudioCaptureHelper(context)
    org.junit.Assert.assertFalse(audioCaptureHelper.isRecording.value)
    assertEquals(0f, audioCaptureHelper.audioAmplitude.value, 0.001f)
    audioCaptureHelper.stopRecording()
    audioCaptureHelper.release()
  }

  @Test
  fun `audio capture helper handles PCM conversions and WAV packaging`() {
    val dummyPcm = byteArrayOf(0, 0, 100, 10, -50, -5, 50, 20)
    val shorts = com.example.audio.AudioCaptureHelper.pcmToShortArray(dummyPcm)
    assertEquals(4, shorts.size)

    val floats = com.example.audio.AudioCaptureHelper.pcmToFloatArray(dummyPcm)
    assertEquals(4, floats.size)
    org.junit.Assert.assertTrue(floats.all { it >= -1.0f && it <= 1.0f })

    val reconverted = com.example.audio.AudioCaptureHelper.shortArrayToPcm(shorts)
    org.junit.Assert.assertArrayEquals(dummyPcm, reconverted)

    val wavData = com.example.audio.AudioCaptureHelper.pcmToWav(dummyPcm)
    assertEquals(44 + dummyPcm.size, wavData.size)
    assertEquals('R'.code.toByte(), wavData[0])
    assertEquals('I'.code.toByte(), wavData[1])
    assertEquals('F'.code.toByte(), wavData[2])
    assertEquals('F'.code.toByte(), wavData[3])
  }

  @Test
  fun `open source models default list includes curated models with PirateFace source`() {
    val models = com.example.model.DEFAULT_OPEN_SOURCE_MODELS
    org.junit.Assert.assertTrue(models.isNotEmpty())
    org.junit.Assert.assertTrue(models.any { it.name.contains("Llama") })
    org.junit.Assert.assertTrue(models.any { it.name.contains("DeepSeek") })
    org.junit.Assert.assertTrue(models.all { it.sourceCatalog.contains("PirateFace") })
  }
}
