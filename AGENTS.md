# AI Agents Specification — Claude for Galaxy

## 1. Overview

**Claude for Galaxy** implements a modular, multi-agent intelligence layer on top of Anthropic's Claude 3.5 Sonnet / 3.7 Sonnet (with fallback to Google Gemini and local built-in intelligence). Rather than presenting a single generic chat bot, the app provides specialized agents and quick message action transforms that adapt prompts, formatting, and outputs specifically for mobile productivity on Samsung Galaxy devices.

---

## 2. Multi-Agent Catalog

### 2.1 General Intelligence Agent (`agent_claude_core`)
* **Role**: Primary reasoning, dialog, brainstorming, and software development.
* **Model**: Claude 3.5 Sonnet / Claude 3.7 Sonnet (default).
* **System Prompt Strategy**:
  ```text
  You are Claude, a helpful, precise, and thoughtful AI assistant operating in a modern Samsung Galaxy mobile environment.
  Deliver direct, high-value responses formatted with markdown headings, lists, and code blocks where relevant.
  Avoid conversational filler and maintain a professional, friendly demeanor.
  ```

---

### 2.2 Web Search & Citation Agent (`agent_web_search`)
* **Role**: Real-time information retrieval, current events, weather, exchange rates, and technical documentation with cited sources.
* **Trigger**: Activated via the `🔍 Web Search` toggle chip or message action.
* **Output Format**:
  - Response body containing synthesized live facts.
  - Mandatory `[Sources]` section at the bottom citing domain references (e.g., `[1] reuters.com`, `[2] bbc.com`).
* **System Prompt**:
  ```text
  You are the Web Search Agent. You incorporate live external knowledge into your responses.
  Structure your reply clearly, and always append a verified [Sources] section with numbered domain citations.
  ```

---

### 2.3 Executive Summarizer Agent (`agent_summarizer`)
* **Role**: Condenses lengthy articles, research papers, email threads, or previous Claude answers into concise, actionable summaries.
* **Trigger**: One-tap `⚡ Summarize` chip or Message Action sheet.
* **Output Format**:
  - **Key Takeaway**: 1-sentence bottom-line summary.
  - **Core Points**: 3 to 5 bullet points.
* **Prompt Transformation**:
  ```text
  Summarize the following content into a clean executive summary suitable for a mobile phone screen:
  1. Key Takeaway (1 concise sentence)
  2. Core Points (3-5 bullet points)

  Content:
  {{USER_INPUT}}
  ```

---

### 2.4 Simplifier & ELI5 Agent (`agent_eli5`)
* **Role**: Takes dense jargon, complex math, legal clauses, or scientific papers and explains them using everyday analogies without dumbing down the truth.
* **Trigger**: `💡 Explain Simply (ELI5)` action.
* **Prompt Transformation**:
  ```text
  Explain the following concept or text simply (ELI5 format). Use clear analogies and plain language that anyone can easily understand:

  {{USER_INPUT}}
  ```

---

### 2.5 Polyglot Translator Agent (`agent_translator`)
* **Role**: Faithful, idiomatically accurate translations. Retains the original `emir/claude-s40` emphasis on Turkish and English, while expanding to Spanish, French, German, and Japanese.
* **Triggers**:
  - `🇹🇷 Translate to Turkish`
  - `🇬🇧 Translate to English`
  - `🌐 Multi-Language Translate`
* **Prompt Transformation**:
  ```text
  Translate the following message into natural, fluent {{TARGET_LANGUAGE}}.
  Preserve tone, nuances, and formatting accurately.

  Message:
  {{USER_INPUT}}
  ```

---

### 2.6 Task & Calendar Agent (`agent_task_extractor`)
* **Role**: Analyzes message threads, extracts commitments, deadlines, and tasks, and turns them into structured items in the app's Task & Calendar system.
* **Trigger**: `📋 Extract Tasks` or Message Action sheet.
* **Output Schema**:
  ```json
  [
    {
      "title": "Short task title",
      "details": "Context or instructions",
      "dateStr": "DD.MM.YYYY"
    }
  ]
  ```

---

## 3. Agent Execution Pipeline

```
 User Input / Action Tap
          │
          ▼
┌──────────────────────────┐
│  Quick Action / Router   │
│  - Selects Agent Role    │
│  - Wraps Context Prompt  │
└─────────┬────────────────┘
          │
          ▼
┌──────────────────────────┐
│ System Notes Injection   │  <-- Merges user personal notes from Settings
└─────────┬────────────────┘
          │
          ▼
┌──────────────────────────┐
│ Model API Dispatcher     │
│ (Claude 3.5 / Gemini)    │
└─────────┬────────────────┘
          │
          ▼
┌──────────────────────────┐
│ Output Parser & Formatter│
│ - Markdown formatting    │
│ - Code block highlight   │
│ - Web source card chips  │
└─────────┬────────────────┘
          │
          ▼
 Rendered in One UI Feed & Haptic Touch Feedback
```

---

## 4. Custom User Personas & Notes

Users can define personal notes in **Settings > Personal Notes for Claude**. This user instruction is systematically included with every agent prompt. Examples:
- *"I am a Kotlin software engineer; include concise code snippets."*
- *"Always format task recommendations with estimated duration."*
- *"Yanıtları daima akıcı bir Türkçe ile ver."*
