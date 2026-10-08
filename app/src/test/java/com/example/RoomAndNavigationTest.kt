package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.TaskEntity
import com.example.model.ChatMessage
import com.example.model.Conversation
import com.example.repository.MessageRepository
import com.example.ui.navigation.GalaxyScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomAndNavigationTest {

    private lateinit var db: AppDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testRoomChatMessagesAndConversationPersistence() = runBlocking {
        val convDao = db.conversationDao()
        val msgDao = db.chatMessageDao()

        val conv = ConversationEntity(
            id = "test-conv-1",
            title = "Test AI Session",
            lastMessage = "Hello from Room",
            updatedAt = System.currentTimeMillis(),
            isPinned = true,
            messageCount = 1
        )
        convDao.insertConversation(conv)

        val retrievedConv = convDao.getConversationById("test-conv-1")
        assertNotNull(retrievedConv)
        assertEquals("Test AI Session", retrievedConv?.title)
        assertTrue(retrievedConv?.isPinned == true)

        val msg = ChatMessageEntity(
            id = "msg-101",
            conversationId = "test-conv-1",
            role = "user",
            content = "Can we test Room storage?",
            timestamp = System.currentTimeMillis(),
            sources = listOf("https://developer.android.com/training/data-storage/room")
        )
        msgDao.insertMessage(msg)

        val retrievedMessages = msgDao.getMessagesByConversationSync("test-conv-1")
        assertEquals(1, retrievedMessages.size)
        assertEquals("Can we test Room storage?", retrievedMessages.first().content)
        assertEquals(1, retrievedMessages.first().sources.size)
    }

    @Test
    fun testRoomTaskEntityAndDaoOperations() = runBlocking {
        val taskDao = db.taskDao()

        val task = TaskEntity(
            id = "task-test-1",
            title = "Review Jetpack Navigation Compose",
            details = "Ensure NavHost handles all tabs cleanly",
            dateStr = "08.10.2026",
            isCompleted = false
        )
        taskDao.insertTask(task)

        val tasks = taskDao.getAllTasksSync()
        assertEquals(1, tasks.size)
        assertEquals("Review Jetpack Navigation Compose", tasks.first().title)
        assertFalse(tasks.first().isCompleted)

        // Toggle task
        taskDao.toggleTaskCompletion("task-test-1")
        val updated = taskDao.getTaskById("task-test-1")
        assertNotNull(updated)
        assertTrue(updated?.isCompleted == true)

        // Delete task
        taskDao.deleteTaskById("task-test-1")
        assertEquals(0, taskDao.getTaskCount())
    }

    @Test
    fun testMessageRepositoryIntegrationWithRoom() = runBlocking {
        val repo = MessageRepository(db.conversationDao(), db.chatMessageDao(), db.taskDao())

        val savedTask = repo.addTask("Test Task from Repo", "Task details", "08.10.2026")
        assertNotNull(savedTask.id)
        assertEquals("Test Task from Repo", savedTask.title)

        val tasks = repo.getTasksSync()
        assertTrue(tasks.any { it.title == "Test Task from Repo" })

        val newMsg = ChatMessage(
            conversationId = "default-chat",
            role = "assistant",
            content = "This response is safely saved in local Room DB."
        )
        repo.saveMessage(newMsg)

        val messages = repo.getMessagesSync("default-chat")
        assertTrue(messages.any { it.content.contains("safely saved in local Room DB") })
    }

    @Test
    fun testGalaxyNavigationScreensAndRoutes() {
        val screens = GalaxyScreen.bottomNavScreens
        assertEquals(5, screens.size)

        assertEquals(GalaxyScreen.Chat, GalaxyScreen.fromRoute("chat"))
        assertEquals(GalaxyScreen.Conversations, GalaxyScreen.fromRoute("conversations"))
        assertEquals(GalaxyScreen.Agents, GalaxyScreen.fromRoute("agents"))
        assertEquals(GalaxyScreen.NotesTasks, GalaxyScreen.fromRoute("notes_tasks"))
        assertEquals(GalaxyScreen.Settings, GalaxyScreen.fromRoute("settings"))
        assertEquals(GalaxyScreen.Chat, GalaxyScreen.fromRoute("unknown_route"))
    }
}
