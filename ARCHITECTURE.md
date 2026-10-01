# System Architecture — Claude for Galaxy

## 1. Overview & Vision

**Claude for Galaxy** is a native Android application built specifically for modern Samsung Galaxy devices (e.g. Samsung Galaxy A53 5G). It ports and modernizes the core capabilities of [emir/claude-s40](https://github.com/emir/claude-s40)—Anthropic Claude intelligence, multi-action transformations, paginated document reading, memory card file saving, and web search—while replacing retro constraints with a pure **Samsung One UI** design language and Jetpack Compose.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Samsung Galaxy A53 UI                           │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │               One UI Viewing Area Header (26sp)                  │  │
│  ├──────────────────────────────────────────────────────────────────┤  │
│  │         One UI Agent Segmented Control (Pill Switcher)           │  │
│  │     [🤖 Claude] [🔍 Web] [⚡ Summarize] [💡 ELI5] [🌐 Tr] [⋮]      │  │
│  ├──────────────────────────────────────────────────────────────────┤  │
│  │           One UI Squircle Chat Bubbles (120Hz Feed)              │  │
│  │    User: primaryContainer / Assistant: surfaceVariant           │  │
│  ├──────────────────────────────────────────────────────────────────┤  │
│  │             One UI Quick Action Chips [Web, Tr, ...]             │  │
│  ├──────────────────────────────────────────────────────────────────┤  │
│  │               Modern Pill Composer (30dp Squircle)               │  │
│  ├──────────────────────────────────────────────────────────────────┤  │
│  │             Samsung One UI Bottom Navigation Bar                 │  │
│  │        [Chat]  [History]  [Agents]  [Notes]  [Settings]          │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ StateFlow / Events
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                          Presentation Layer                            │
│                         MultiAgentViewModel                            │
│  • Agent State Machine          • Segmented Control & Bottom Sheet     │
│  • Specialized Agent Logic      • Haptic Tactile Engine Feedback       │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Coroutines / Flow
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                            Domain Layer                                │
│                     Agent Orchestration Pipeline                       │
│     • General Assistant • Web Search Agent • Summarizer Agent          │
│     • Simplifier Agent  • Translator Agent • Task & Calendar Agent     │
└───────────────────┬────────────────────────────────┬───────────────────┘
                    │                                │
                    ▼                                ▼
┌──────────────────────────────────────┐  ┌──────────────────────────────┐
│           Repository Layer           │  │         Network Layer        │
│        ClaudeS40Repository           │  │       ClaudeApiService       │
│  • SharedPreferences / JSON Store    │  │  • Anthropic API (Claude 3.5)│
│  • Conversations & Message Threads   │  │  • Google Gemini API         │
│  • Saved .TXT Documents              │  │  • Custom S40 Go Proxy       │
│  • Calendar & To-Do Memos            │  │  • Built-in Smart Engine     │
└──────────────────────────────────────┘  └──────────────────────────────┘
```

---

## 2. MultiAgentViewModel Architecture

### 2.1 State Contracts

The state is managed using an immutable unidirectional data flow pattern (`MultiAgentUiState`):

```kotlin
data class AgentInteractionState(
    val id: String,
    val name: String,
    val tag: String,
    val description: String,
    val placeholder: String,
    val accentColorHex: Long,
    val suggestionChips: List<String>,
    val defaultPrompt: String
)

data class MultiAgentUiState(
    val currentTab: GalaxyNavTab = GalaxyNavTab.CHAT,
    val activeConversationId: String = "default-chat",
    val activeConversationTitle: String = "Chat with Claude",
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val selectedMessageForAction: ChatMessage? = null,
    val isActionSheetOpen: Boolean = false,
    val isAgentBottomSheetOpen: Boolean = false,
    val statusNotice: String? = null,
    val selectedAgentId: String = "claude_core",
    val activeAgent: String = "Claude 3.5 Sonnet",
    val currentAgentState: AgentInteractionState = ...
)
```

### 2.2 Segmented Control & Agent Interaction Logic

The `MultiAgentViewModel` mediates all interactions between the UI components:

1. **Segmented Control Interaction**:
   - `selectAgent(agentId: String)`:
     - Updates `selectedAgentId` and the active `AgentInteractionState`.
     - Automatically adjusts the composer placeholder (e.g. *"Search live web with Web Searcher..."*, *"Paste text to summarize..."*).
     - Auto-configures agent prerequisites (e.g. enables `webSearchEnabled = true` when `web_search` is activated).
     - Dispatches tactile haptic pulse (`feedbackManager.playKeyClick()`).

2. **Agent Switcher Bottom Sheet**:
   - `openAgentBottomSheet()` / `closeAgentBottomSheet()`:
     - Toggles the Material3 `ModalBottomSheet` displaying the catalog of 6 specialized agents with capabilities, category chips, and starter prompts.
   - `selectAgentPrompt(agent: AgentDefinition)`:
     - Pre-fills the composer with tailored starter prompts and focuses the chat feed.

3. **Agent-Specific Message Pipeline**:
   - When `sendMessage()` is executed, `MultiAgentViewModel` dynamically inspects `selectedAgentId` and transforms the dispatch:
     - **Web Searcher (`web_search`)**: Enforces real-time search context and mandates cited source URL collection.
     - **Summarizer (`summarizer`)**: Routes action type `"SHORTEN"` to structure responses as key takeaways and core bullet points.
     - **Simplifier (`eli5`)**: Routes action type `"SIMPLIFY"` to decompose dense jargon into intuitive analogies.
     - **Translator (`translator`)**: Routes action type `"TRANSLATE_TR"` or `"TRANSLATE_EN"` preserving conversational tone.
     - **Task Manager (`task_planner`)**: Routes action type `"TODO"` and synchronizes actionable commitments directly into the calendar manager.

---

## 3. Samsung One UI Design System

### 3.1 Signature Squircle Corner Radii

Samsung One UI is characterized by organic, generous squircle curvature rather than sharp rectangular corners:

- **Tokens (`OneUiShapes.kt`)**:
  - `extraSmall`: `8.dp` (badges and inner chips)
  - `small`: `14.dp` (capsule tags and quick action chips)
  - `medium`: `20.dp` (segmented control items, buttons)
  - `large`: `26.dp` (One UI cards and dialog containers)
  - `extraLarge`: `30.dp` (One UI floating composers and bottom sheets)

### 3.2 Custom Material3 Chat Bubble Composables (`OneUiChatBubbles.kt`)

Chat messages are rendered using dedicated Samsung One UI composables:

- **Asymmetric Squircle Curvature**:
  - `OneUiUserSquircle`: `RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 6.dp)` (subtle tail on the right).
  - `OneUiAssistantSquircle`: `RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 6.dp, bottomEnd = 24.dp)` (subtle tail on the left).
- **One UI Material3 Color Role Mapping**:
  - **User Bubble**:
    - Light: `MaterialTheme.colorScheme.primary` (Samsung Galaxy Blue `#1B64F2`).
    - Dark: `MaterialTheme.colorScheme.primaryContainer` (`#0D2554` / `#4C8DFF`).
    - Text: `MaterialTheme.colorScheme.onPrimaryContainer` / `Color.White`.
  - **Assistant Bubble**:
    - Light: `MaterialTheme.colorScheme.surface` (`#FFFFFF`).
    - Dark: `MaterialTheme.colorScheme.surfaceVariant` (`#1C1E26`).
    - Border: `MaterialTheme.colorScheme.outlineVariant` (`#282B36`).
    - Text: `MaterialTheme.colorScheme.onSurface` (`#F8FAFC`).
  - **Agent Accent Badges**:
    - Visual indicators for each agent: Emerald (Web), Amber (Summarizer), Cyan (ELI5), Rose (Translator), Indigo (Tasks), Violet (Claude).
  - **Live Web Sources Citation Cards**:
    - Embedded inner squircle cards (`14.dp`) with favicon and domain link references.

---

## 4. AMOLED Battery & Screen Optimization

- The Galaxy A53 features a 6.5" 120Hz Super AMOLED display.
- Default Dark Mode uses **Pure Pitch Black (`#000000`)** as the background surface. This completely turns off organic LED pixels, resulting in significant battery savings and infinite contrast ratio.
- Smooth 120Hz scrolling is achieved by keying all list items (`items(messages, key = { it.id })`) to avoid recomposition thrashing.
