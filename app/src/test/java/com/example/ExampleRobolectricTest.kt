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
    assertEquals("Claude S40", appName)
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
}
