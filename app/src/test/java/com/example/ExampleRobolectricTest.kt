package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.agent.CalculatorTool
import com.example.ai.agent.DateTimeTool
import com.example.ai.agent.IntentDetector
import com.example.ai.agent.ToolResult
import com.example.ai.model.IntentType
import com.example.ai.model.LanguageMode
import com.example.data.local.NovaDatabase
import com.example.data.repository.NovaRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    assertEquals("NOVA AI", appName)
  }

  @Test
  fun `intent detector recognizes bengali prompt`() {
    val detector = IntentDetector()
    val intent = detector.detectIntent("একটা cinematic photo বানিয়ে দাও")
    assertEquals(IntentType.IMAGE_GENERATION, intent.type)
    assertEquals(LanguageMode.BENGALI, intent.language)
  }

  @Test
  fun `intent detector recognizes hindi prompt`() {
    val detector = IntentDetector()
    val intent = detector.detectIntent("नमस्ते NOVA, आज का मौसम कैसा है?")
    assertEquals(LanguageMode.HINDI, intent.language)
  }

  @Test
  fun `calculator tool computes correctly`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = NovaRepository(NovaDatabase.getDatabase(context))
    val calc = CalculatorTool()
    val result = calc.execute("calculate 120 + 30", repo)
    assertTrue(result is ToolResult.Success)
    assertTrue((result as ToolResult.Success).output.contains("150"))
  }
}

