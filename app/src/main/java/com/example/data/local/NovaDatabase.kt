package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ConversationDao
import com.example.data.local.dao.CreativeProjectDao
import com.example.data.local.dao.ImageVersionDao
import com.example.data.local.dao.KnowledgeDao
import com.example.data.local.dao.MemoryDao
import com.example.data.local.dao.MessageDao
import com.example.data.local.dao.TemplateDao
import com.example.data.local.dao.ToolExecutionDao
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.CreativeProjectEntity
import com.example.data.local.entity.ImageVersionEntity
import com.example.data.local.entity.KnowledgeChunkEntity
import com.example.data.local.entity.KnowledgeDocEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.TemplateEntity
import com.example.data.local.entity.ToolExecutionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        CreativeProjectEntity::class,
        ImageVersionEntity::class,
        TemplateEntity::class,
        MemoryEntity::class,
        KnowledgeDocEntity::class,
        KnowledgeChunkEntity::class,
        ToolExecutionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun creativeProjectDao(): CreativeProjectDao
    abstract fun imageVersionDao(): ImageVersionDao
    abstract fun templateDao(): TemplateDao
    abstract fun memoryDao(): MemoryDao
    abstract fun knowledgeDao(): KnowledgeDao
    abstract fun toolExecutionDao(): ToolExecutionDao

    companion object {
        @Volatile
        private var INSTANCE: NovaDatabase? = null

        fun getDatabase(context: Context): NovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovaDatabase::class.java,
                    "nova_ai_studio_db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database)
                    }
                }
            }
        }

        suspend fun seedInitialData(database: NovaDatabase) {
            val templateDao = database.templateDao()
            val memoryDao = database.memoryDao()
            val conversationDao = database.conversationDao()
            val messageDao = database.messageDao()

            // Seed initial conversation
            val welcomeConvId = UUID.randomUUID().toString()
            val welcomeConv = ConversationEntity(
                id = welcomeConvId,
                title = "Welcome to NOVA AI ✨",
                isPinned = true,
                language = "AUTO"
            )
            conversationDao.insertOrUpdate(welcomeConv)

            val welcomeMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = welcomeConvId,
                role = "assistant",
                content = """
                    👋 **নমস্কার / नमस्ते / Welcome! I am NOVA AI.**
                    
                    I am your multilingual personal AI Agent and Creative Studio. I can assist you seamlessly in **Bengali, Hindi, and English** (including mixed natural phrases).
                    
                    ### 🚀 What I can do for you:
                    1. **Multilingual Chat & Reasoning**: Ask complex questions, write code, or brainstorm in বাংলা, हिन्दी, or English.
                    2. **AI Image Generation & Studio**: Create cinematic portraits, YouTube thumbnails, and digital art with custom aspect ratios.
                    3. **Non-Destructive Photo Editing**: Remove backgrounds, relight, add/remove objects, and branch version histories.
                    4. **Voice Agent**: Speak naturally with real-time Speech-to-Text and voice output.
                    5. **Personal Memory & Knowledge**: Store long-term preferences, upload files, and perform tool executions securely.
                    
                    Try asking: *"একটা cinematic photo বানিয়ে দাও but backgroundটা sunset হবে"* or tap a template below!
                """.trimIndent()
            )
            messageDao.insertMessage(welcomeMsg)

            // Seed initial templates
            val initialTemplates = listOf(
                TemplateEntity(
                    id = "tpl_bengali_wedding",
                    name = "Cinematic Bengali Wedding Portrait",
                    description = "Royal traditional Bengali bridal or groom portrait with festive marigold bokeh and dramatic warm golden hour lighting.",
                    category = "Wedding",
                    previewGradientColors = "#FF007A,#FFB703",
                    promptTemplate = "Cinematic Royal Bengali wedding portrait of {subject}, adorned in traditional Benarasi saree with intricate zari work, mukut, golden ornaments, soft glowing diya lighting, marigold floral backdrop, 8k resolution, photorealistic, ultra high detail",
                    aspectRatio = "4:5",
                    requiredInputsJson = "[\"subject\"]",
                    isPremium = false
                ),
                TemplateEntity(
                    id = "tpl_cyberpunk_kolkata",
                    name = "Cyberpunk Kolkata / Mumbai 2099",
                    description = "Futuristic neon-drenched street scene blending vintage architecture with flying vehicles and holographic signs in Bengali & Devanagari.",
                    category = "Cinematic",
                    previewGradientColors = "#00E5FF,#9D4EDD",
                    promptTemplate = "Futuristic Cyberpunk scene of {city} in year 2099, neon holograms in Bengali and Hindi script, flying yellow taxis, rain soaked reflective asphalt, glowing cyan and magenta volumetric lights, cinematic composition, Octane render, unreal engine 5",
                    aspectRatio = "16:9",
                    requiredInputsJson = "[\"city\"]",
                    isPremium = true
                ),
                TemplateEntity(
                    id = "tpl_yt_tech_thumbnail",
                    name = "High-CTR YouTube Tech Thumbnail",
                    description = "Eye-catching YouTube thumbnail layout featuring expressive person, glowing neon gadget, and bold high-contrast background.",
                    category = "YouTube Thumbnail",
                    previewGradientColors = "#FF007A,#00E5FF",
                    promptTemplate = "YouTube viral tech thumbnail background, featuring {tech_topic}, vibrant neon glowing electric accents, dramatic 3-point studio lighting, high contrast, clean negative space for typography, 4k ultra sharp",
                    aspectRatio = "16:9",
                    requiredInputsJson = "[\"tech_topic\"]",
                    isPremium = false
                ),
                TemplateEntity(
                    id = "tpl_luxury_product",
                    name = "Minimalist Luxury Product Ad",
                    description = "Studio pedestal product staging with floating water droplets, soft shadows, and clean pastel or obsidian backdrop.",
                    category = "Product Advertisement",
                    previewGradientColors = "#9D4EDD,#F1F5F9",
                    promptTemplate = "Commercial luxury studio advertisement photo of {product_name}, placed on a minimalist matte black geometric pedestal, soft rim lighting, subtle water mist droplets, macro lens, editorial aesthetic",
                    aspectRatio = "1:1",
                    requiredInputsJson = "[\"product_name\"]",
                    isPremium = false
                ),
                TemplateEntity(
                    id = "tpl_festival_diwali_durga",
                    name = "Durga Puja & Festive Grandeur",
                    description = "Majestic cultural festival visual with traditional dhak, glowing clay diyas, and divine ambient luminescence.",
                    category = "Festival",
                    previewGradientColors = "#FFB703,#FF007A",
                    promptTemplate = "Divine festive celebration of {festival_name}, glowing brass lamps, fragrant smoke swirls, golden ornaments, vibrant festive ambience, warm cinematic atmosphere, hyper-detailed",
                    aspectRatio = "9:16",
                    requiredInputsJson = "[\"festival_name\"]",
                    isPremium = false
                ),
                TemplateEntity(
                    id = "tpl_pro_headshot",
                    name = "Executive Studio Headshot",
                    description = "Crisp, professional corporate headshot with soft studio light box and blurred modern architectural interior.",
                    category = "Professional Headshot",
                    previewGradientColors = "#0077FF,#00E5FF",
                    promptTemplate = "Professional executive portrait of {profession_description}, confident warm smile, crisp tailored attire, 85mm f/1.4 lens bokeh, modern corporate office background, magazine cover grade",
                    aspectRatio = "3:4",
                    requiredInputsJson = "[\"profession_description\"]",
                    isPremium = false
                ),
                TemplateEntity(
                    id = "tpl_insta_aesthetic",
                    name = "Trendy Instagram Travel Story",
                    description = "Sun-kissed aesthetic travel snapshot with warm film grain and golden sunlight flare.",
                    category = "Instagram Post",
                    previewGradientColors = "#00FF9D,#00E5FF",
                    promptTemplate = "Aesthetic Instagram travel portrait in {destination}, golden hour sunlight flare, 35mm film grain aesthetic, relaxed effortless pose, pastel hues, organic lifestyle photography",
                    aspectRatio = "9:16",
                    requiredInputsJson = "[\"destination\"]",
                    isPremium = false
                ),
                TemplateEntity(
                    id = "tpl_neon_logo",
                    name = "Futuristic Cyber Vector Logo",
                    description = "Sleek glowing neon vector logo emblem on dark textured carbon background.",
                    category = "Logo Concept",
                    previewGradientColors = "#00E5FF,#9D4EDD",
                    promptTemplate = "Minimalist futuristic tech logo emblem representing {brand_concept}, glowing electric cyan and purple neon lines, vector iconography, centered, dark carbon fiber background, clean iconography",
                    aspectRatio = "1:1",
                    requiredInputsJson = "[\"brand_concept\"]",
                    isPremium = true
                )
            )
            templateDao.insertTemplates(initialTemplates)

            // Seed initial memories
            val defaultMemories = listOf(
                MemoryEntity(
                    id = "mem_lang_pref",
                    category = "language",
                    key = "Primary Languages",
                    value = "Bengali, Hindi, and English multilingual support enabled",
                    source = "system"
                ),
                MemoryEntity(
                    id = "mem_style_pref",
                    category = "style",
                    key = "Visual Aesthetic",
                    value = "Prefers futuristic cyber dark aesthetic with high contrast neon highlights",
                    source = "system"
                ),
                MemoryEntity(
                    id = "mem_creative_pref",
                    category = "preference",
                    key = "Creative Studio",
                    value = "Enable non-destructive image version branching and instant upscale",
                    source = "system"
                )
            )
            defaultMemories.forEach { memoryDao.insertMemory(it) }
        }
    }
}
