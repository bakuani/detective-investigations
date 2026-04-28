package com.detective.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class AiGenerationService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, Long> modelCooldownUntilMs = new ConcurrentHashMap<>();
    private static final long RATE_LIMIT_COOLDOWN_MS = 120_000L;

    public AiGenerationService(ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> generateCaseTemplate(String theme, String difficulty, Integer cluesCount) {
        String safeTheme = theme == null || theme.isBlank() ? "РґРµС‚РµРєС‚РёРІ" : theme.trim();
        String safeDifficulty = difficulty == null || difficulty.isBlank() ? "medium" : difficulty.trim();
        int safeClues = cluesCount != null && cluesCount > 0 ? cluesCount : 5;

        String promptText = buildPrompt(safeTheme, safeDifficulty, safeClues);
        List<ModelPlan> plans = modelPlansByDifficulty(safeDifficulty);

        for (ModelPlan plan : plans) {
            long now = System.currentTimeMillis();
            long cooldownUntil = modelCooldownUntilMs.getOrDefault(plan.model(), 0L);
            if (cooldownUntil > now) {
                log.info("Skipping model '{}' due to cooldown ({}s left)",
                        plan.model(), (cooldownUntil - now) / 1000);
                continue;
            }

            try {
                String rawResponse = callModelWithSpringAi(plan, promptText);
                Map<String, Object> parsed = parseAndNormalizeTemplate(rawResponse, safeTheme, safeDifficulty, safeClues);
                log.info("AI generation succeeded with model '{}'", plan.model());
                return parsed;
            } catch (Exception e) {
                if (isRateLimitError(e)) {
                    long nextTryAt = System.currentTimeMillis() + RATE_LIMIT_COOLDOWN_MS;
                    modelCooldownUntilMs.put(plan.model(), nextTryAt);
                    log.warn("Model '{}' is rate-limited (429). Cooldown for {}s. Reason: {}",
                            plan.model(), RATE_LIMIT_COOLDOWN_MS / 1000, e.getMessage());
                }
                log.warn("Model '{}' failed. Trying next model. Reason: {}", plan.model(), e.getMessage());
            }
        }

        log.warn("All models failed. Using text fallback. theme='{}', difficulty='{}', cluesCount={}",
                safeTheme, safeDifficulty, safeClues);
        return buildFallbackTemplate(safeTheme, safeDifficulty, safeClues);
    }

    private String callModelWithSpringAi(ModelPlan plan, String promptText) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(plan.model())
                .withTemperature((float) plan.temperature())
                .withTopP((float) plan.topP())
                .withMaxTokens(plan.maxTokens())
                .build();

        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("You are a strict JSON generator. Return only valid JSON object, no markdown."),
                        new UserMessage(promptText)
                ),
                options
        );

        var response = chatClient.call(prompt);
        if (response == null
                || response.getResult() == null
                || response.getResult().getOutput() == null
                || response.getResult().getOutput().getContent() == null
                || response.getResult().getOutput().getContent().isBlank()) {
            throw new IllegalStateException("Empty or invalid Spring AI response content");
        }
        return response.getResult().getOutput().getContent();
    }

    private String buildPrompt(String theme, String difficulty, int cluesCount) {
        return String.format("""
                РЎРѕР·РґР°Р№ РґРµС‚РµРєС‚РёРІРЅРѕРµ РґРµР»Рѕ РЅР° СЂСѓСЃСЃРєРѕРј СЏР·С‹РєРµ Рё РІРµСЂРЅРё РўРћР›Р¬РљРћ JSON-РѕР±СЉРµРєС‚.
                Р‘РµР· markdown Рё Р±РµР· РїРѕСЏСЃРЅРµРЅРёР№ РІРЅРµ JSON.

                Р¤РѕСЂРјР°С‚:
                {
                  "title": "РѕСЂРёРіРёРЅР°Р»СЊРЅРѕРµ РЅР°Р·РІР°РЅРёРµ",
                  "intro": "СЃРѕРґРµСЂР¶Р°С‚РµР»СЊРЅР°СЏ Р·Р°РІСЏР·РєР° РґРµР»Р° СЃ РјРµСЃС‚РѕРј, СЃРѕР±С‹С‚РёРµРј Рё РєРѕРЅС‚РµРєСЃС‚РѕРј",
                  "groundTruth": "РєС‚Рѕ Рё РїРѕС‡РµРјСѓ РІРёРЅРѕРІРµРЅ",
                  "tags": ["tag1", "tag2"],
                  "suspects": [{"name":"...","description":"...","alibi":"..."}],
                  "clues": [{"content":"...","type":"text","importanceRank":1}]
                }

                РўСЂРµР±РѕРІР°РЅРёСЏ:
                - title: РѕСЂРёРіРёРЅР°Р»СЊРЅС‹Р№, Р±РµР· С€Р°Р±Р»РѕРЅР° "РўР°Р№РЅР°: ..."
                - intro: РєРѕРЅРєСЂРµС‚РЅР°СЏ РёСЃС‚РѕСЂРёСЏ, 3-6 РїСЂРµРґР»РѕР¶РµРЅРёР№
                - clues: СЃС‚СЂРѕРіРѕ %d С€С‚СѓРє
                - importanceRank С‚РѕР»СЊРєРѕ 1, 2 РёР»Рё 3
                - РјРёРЅРёРјСѓРј РїРѕ РѕРґРЅРѕР№ СѓР»РёРєРµ РєР°Р¶РґРѕРіРѕ СЂР°РЅРіР° (1, 2, 3)
                - РІСЃРµ С‚РµРєСЃС‚РѕРІС‹Рµ Р·РЅР°С‡РµРЅРёСЏ вЂ” РЅР° СЂСѓСЃСЃРєРѕРј СЏР·С‹РєРµ

                РўРµРјР°: %s
                РЎР»РѕР¶РЅРѕСЃС‚СЊ: %s
                Seed: %s
                """, cluesCount, theme, difficulty, UUID.randomUUID());
    }

    private List<ModelPlan> modelPlansByDifficulty(String difficulty) {
        double baseTemperature = "hard".equalsIgnoreCase(difficulty) ? 0.85 : 0.7;
        int baseTokens = "hard".equalsIgnoreCase(difficulty) ? 2200 : 1600;

        List<ModelPlan> plans = new ArrayList<>();
        plans.add(new ModelPlan("tencent/hy3-preview:free", baseTemperature, 0.95, Math.min(baseTokens, 1600)));
        plans.add(new ModelPlan("minimax/minimax-m2.5:free", Math.max(0.6, baseTemperature - 0.05), 0.9, Math.max(baseTokens, 1800)));
        plans.add(new ModelPlan("openai/gpt-oss-120b:free", Math.max(0.55, baseTemperature - 0.1), 0.9, Math.max(baseTokens, 1800)));
        return plans;
    }

    private Map<String, Object> parseAndNormalizeTemplate(String rawResponse, String theme, String difficulty, int cluesCount) throws Exception {
        String jsonString = extractJsonObject(rawResponse);
        Map<String, Object> parsed = objectMapper.readValue(jsonString, Map.class);
        return normalizeTemplate(parsed, theme, difficulty, cluesCount);
    }

    private String extractJsonObject(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new IllegalArgumentException("AI response is empty");
        }

        String cleaned = rawResponse.trim();
        if (cleaned.startsWith("```")) {
            int firstNewLine = cleaned.indexOf('\n');
            if (firstNewLine >= 0) {
                cleaned = cleaned.substring(firstNewLine + 1).trim();
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.lastIndexOf("```")).trim();
            }
        }

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalArgumentException("AI response does not contain JSON object");
        }

        return cleaned.substring(start, end + 1);
    }

    private Map<String, Object> normalizeTemplate(Map<String, Object> parsed, String theme, String difficulty, int cluesCount) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("title", getString(parsed.get("title"), buildDynamicFallbackTitle(theme)));
        normalized.put("intro", getString(parsed.get("intro"), buildDynamicFallbackIntro(theme, difficulty)));
        normalized.put("groundTruth", getString(parsed.get("groundTruth"), "РџСЂРµСЃС‚СѓРїРЅРёРє РёСЃРїРѕР»СЊР·РѕРІР°Р» Р»РѕР¶РЅС‹Рµ Р°Р»РёР±Рё Рё РїРѕРґРјРµРЅСѓ С„Р°РєС‚РѕРІ, РЅРѕ РґРѕРїСѓСЃС‚РёР» РѕС€РёР±РєСѓ РІРѕ РІСЂРµРјРµРЅРё СЃРѕР±С‹С‚РёР№."));
        normalized.put("tags", normalizeTags(parsed.get("tags")));
        normalized.put("suspects", normalizeSuspects(parsed.get("suspects")));
        normalized.put("clues", normalizeClues(parsed.get("clues"), cluesCount));
        return normalized;
    }

    private List<String> normalizeTags(Object rawTags) {
        List<String> tags = new ArrayList<>();
        if (rawTags instanceof List<?> rawList) {
            for (Object item : rawList) {
                String value = getString(item, null);
                if (value != null && !value.isBlank()) {
                    tags.add(value);
                }
            }
        }
        if (tags.isEmpty()) {
            tags.add("СѓР»РёРєРё");
            tags.add("Р»РѕР¶РЅРѕРµ Р°Р»РёР±Рё");
            tags.add("РЅРµРѕР¶РёРґР°РЅРЅС‹Р№ РјРѕС‚РёРІ");
        }
        return tags;
    }

    private List<Map<String, Object>> normalizeSuspects(Object rawSuspects) {
        List<Map<String, Object>> suspects = new ArrayList<>();
        if (rawSuspects instanceof List<?> rawList) {
            for (Object item : rawList) {
                if (item instanceof Map<?, ?> rawMap) {
                    Map<String, Object> suspect = new LinkedHashMap<>();
                    suspect.put("name", getString(rawMap.get("name"), "РќРµРёР·РІРµСЃС‚РЅС‹Р№ РїРѕРґРѕР·СЂРµРІР°РµРјС‹Р№"));
                    suspect.put("description", getString(rawMap.get("description"), "РЎРєСЂС‹РІР°РµС‚ РІР°Р¶РЅС‹Рµ РґРµС‚Р°Р»Рё Рё РёР·Р±РµРіР°РµС‚ РїСЂСЏРјС‹С… РѕС‚РІРµС‚РѕРІ."));
                    suspect.put("alibi", getString(rawMap.get("alibi"), "РђР»РёР±Рё Р·РІСѓС‡РёС‚ СѓР±РµРґРёС‚РµР»СЊРЅРѕ, РЅРѕ РїРѕРґС‚РІРµСЂР¶РґРµРЅРёР№ РјР°Р»Рѕ."));
                    suspects.add(suspect);
                }
            }
        }

        if (suspects.isEmpty()) {
            suspects.add(buildSuspect("РђР»РµРєСЃРµР№ Р’РѕСЂРѕРЅРѕРІ", "РҐР»Р°РґРЅРѕРєСЂРѕРІРµРЅ Рё СЃР»РёС€РєРѕРј СѓРІРµСЂРµРЅ РІ РґРµС‚Р°Р»СЏС…, РєРѕС‚РѕСЂС‹Рµ РЅРµ РґРѕР»Р¶РµРЅ Р·РЅР°С‚СЊ.", "РЈС‚РІРµСЂР¶РґР°РµС‚, С‡С‚Рѕ РІРµСЃСЊ РІРµС‡РµСЂ Р±С‹Р» РІ Р°СЂС…РёРІРµ РѕРґРёРЅ."));
            suspects.add(buildSuspect("РњР°СЂРёРЅР° Р›РµР±РµРґРµРІР°", "РќРµСЂРІРЅРёС‡Р°РµС‚ РїСЂРё СѓС‚РѕС‡РЅСЏСЋС‰РёС… РІРѕРїСЂРѕСЃР°С… Рё РјРµРЅСЏРµС‚ С„РѕСЂРјСѓР»РёСЂРѕРІРєРё.", "Р“РѕРІРѕСЂРёС‚, С‡С‚Рѕ РЅРµ РѕС‚С…РѕРґРёР»Р° РѕС‚ С‚РµР»РµС„РѕРЅР°."));
            suspects.add(buildSuspect("РРіРѕСЂСЊ РЎРѕРєРѕР»РѕРІ", "РџС‹С‚Р°РµС‚СЃСЏ РІС‹РіР»СЏРґРµС‚СЊ РїРѕР»РµР·РЅС‹Рј СЃР»РµРґСЃС‚РІРёСЋ, РЅРѕ СѓС…РѕРґРёС‚ РѕС‚ РєРѕРЅРєСЂРµС‚РёРєРё.", "РЎРѕРѕР±С‰Р°РµС‚, С‡С‚Рѕ РїСЂРёС€РµР» СѓР¶Рµ РїРѕСЃР»Рµ РёРЅС†РёРґРµРЅС‚Р°."));
        }
        return suspects;
    }

    private List<Map<String, Object>> normalizeClues(Object rawClues, int cluesCount) {
        List<Map<String, Object>> clues = new ArrayList<>();
        if (rawClues instanceof List<?> rawList) {
            int idx = 1;
            for (Object item : rawList) {
                if (item instanceof Map<?, ?> rawMap) {
                    Map<String, Object> clue = new LinkedHashMap<>();
                    clue.put("content", getString(rawMap.get("content"), "РћР±РЅР°СЂСѓР¶РµРЅР° СѓР»РёРєР°, С‚СЂРµР±СѓСЋС‰Р°СЏ РґРѕРїРѕР»РЅРёС‚РµР»СЊРЅРѕР№ РїСЂРѕРІРµСЂРєРё."));
                    clue.put("type", getString(rawMap.get("type"), "text"));
                    clue.put("importanceRank", parseRank(rawMap.get("importanceRank"), idx));
                    clues.add(clue);
                    idx++;
                }
            }
        }

        while (clues.size() < cluesCount) {
            int next = clues.size() + 1;
            clues.add(buildClue("РЈР»РёРєР° " + next + ": РїРѕРєР°Р·Р°РЅРёСЏ СЃРІРёРґРµС‚РµР»РµР№ СЂР°СЃС…РѕРґСЏС‚СЃСЏ СЃРѕ РІСЂРµРјРµРЅРµРј РЅР° РІРёРґРµРѕР·Р°РїРёСЃРё.", ((next - 1) % 3) + 1));
        }
        if (clues.size() > cluesCount) {
            clues = new ArrayList<>(clues.subList(0, cluesCount));
        }

        for (int i = 0; i < clues.size(); i++) {
            clues.get(i).put("importanceRank", (i % 3) + 1);
        }
        return clues;
    }

    private Map<String, Object> buildFallbackTemplate(String theme, String difficulty, int cluesCount) {
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("title", buildDynamicFallbackTitle(theme));
        fallback.put("intro", buildDynamicFallbackIntro(theme, difficulty));
        fallback.put("groundTruth", "РџСЂРµСЃС‚СѓРїРЅРёРє Р·Р°СЂР°РЅРµРµ РїРѕРґРіРѕС‚РѕРІРёР» Р»РѕР¶РЅСѓСЋ С†РµРїРѕС‡РєСѓ СЃРѕР±С‹С‚РёР№ Рё РїРѕРґСЃС‚Р°РІРЅС‹Рµ СѓР»РёРєРё, РЅРѕ РїСЂРѕСЃС‡РёС‚Р°Р»СЃСЏ РІ РґРµС‚Р°Р»СЏС… С‚Р°Р№РјР»Р°Р№РЅР°.");
        fallback.put("tags", new ArrayList<>(List.of("РёРЅС‚СЂРёРіР°", "СѓР»РёРєРё", "Р»РѕР¶РЅРѕРµ Р°Р»РёР±Рё", "СЃРєСЂС‹С‚С‹Р№ РјРѕС‚РёРІ")));

        List<Map<String, Object>> suspects = new ArrayList<>();
        suspects.add(buildSuspect("РђР»РµРєСЃРµР№ Р’РѕСЂРѕРЅРѕРІ", "РЎР»РёС€РєРѕРј С…РѕСЂРѕС€Рѕ РѕСЂРёРµРЅС‚РёСЂСѓРµС‚СЃСЏ РІ РѕР±СЃС‚РѕСЏС‚РµР»СЊСЃС‚РІР°С… РґРµР»Р° Рё СЃС‚Р°СЂР°РµС‚СЃСЏ РІРµСЃС‚Рё Р±РµСЃРµРґСѓ.", "РЈС‚РІРµСЂР¶РґР°РµС‚, С‡С‚Рѕ Р·Р°РЅРёРјР°Р»СЃСЏ РґРѕРєСѓРјРµРЅС‚Р°РјРё РІ РѕС‚РґРµР»СЊРЅРѕРј РєР°Р±РёРЅРµС‚Рµ."));
        suspects.add(buildSuspect("РњР°СЂРёРЅР° Р›РµР±РµРґРµРІР°", "Р­РјРѕС†РёРѕРЅР°Р»СЊРЅР° Рё РїСѓС‚Р°РµС‚СЃСЏ РІ РґРµС‚Р°Р»СЏС…, РєРѕРіРґР° СЂРµС‡СЊ Р·Р°С…РѕРґРёС‚ Рѕ РІСЂРµРјРµРЅРё РїСЂРѕРёСЃС€РµСЃС‚РІРёСЏ.", "Р“РѕРІРѕСЂРёС‚, С‡С‚Рѕ РІСЃРµ РІСЂРµРјСЏ Р±С‹Р»Р° РЅР° СЃРІСЏР·Рё РїРѕ С‚РµР»РµС„РѕРЅСѓ."));
        suspects.add(buildSuspect("РРіРѕСЂСЊ РЎРѕРєРѕР»РѕРІ", "Р”РµСЂР¶РёС‚СЃСЏ СѓРІРµСЂРµРЅРЅРѕ, РЅРѕ РёР·Р±РµРіР°РµС‚ РїСЂСЏРјС‹С… РѕС‚РІРµС‚РѕРІ Рѕ РјР°СЂС€СЂСѓС‚Рµ РїРµСЂРµРґ РёРЅС†РёРґРµРЅС‚РѕРј.", "РЎРѕРѕР±С‰Р°РµС‚, С‡С‚Рѕ РїСЂРёР±С‹Р» СѓР¶Рµ РїРѕСЃР»Рµ СЃР»СѓС‡РёРІС€РµРіРѕСЃСЏ."));
        fallback.put("suspects", suspects);

        List<Map<String, Object>> clues = new ArrayList<>();
        for (int i = 1; i <= cluesCount; i++) {
            clues.add(buildClue("РЈР»РёРєР° " + i + ": РІС‹СЏРІР»РµРЅРѕ РЅРµСЃРѕРѕС‚РІРµС‚СЃС‚РІРёРµ РјРµР¶РґСѓ РїРѕРєР°Р·Р°РЅРёСЏРјРё Рё Р·Р°С„РёРєСЃРёСЂРѕРІР°РЅРЅС‹Рј РІСЂРµРјРµРЅРµРј СЃРѕР±С‹С‚РёР№.", ((i - 1) % 3) + 1));
        }
        fallback.put("clues", clues);

        return fallback;
    }

    private String buildDynamicFallbackTitle(String theme) {
        String normalized = theme.toLowerCase();
        if (normalized.contains("РјСѓР·РµР№") || normalized.contains("РіР°Р»РµСЂРµ")) return "РЁРµРїРѕС‚ РїРѕРґ СЃС‚РµРєР»СЏРЅРЅС‹Рј РєСѓРїРѕР»РѕРј";
        if (normalized.contains("РїРѕРµР·Рґ") || normalized.contains("РІРѕРєР·Р°Р»") || normalized.contains("РјРµС‚СЂРѕ")) return "РџРѕСЃР»РµРґРЅРёР№ СЂРµР№СЃ Р±РµР· СЃРІРёРґРµС‚РµР»РµР№";
        if (normalized.contains("С‚РµР°С‚СЂ") || normalized.contains("СЃС†РµРЅР°") || normalized.contains("РѕРїРµСЂР°")) return "РђРїР»РѕРґРёСЃРјРµРЅС‚С‹ РїРѕСЃР»Рµ РІС‹СЃС‚СЂРµР»Р°";
        if (normalized.contains("РѕСЃРѕР±РЅСЏРє") || normalized.contains("РїРѕРјРµСЃС‚") || normalized.contains("СѓСЃР°Рґ")) return "РќР°СЃР»РµРґСЃС‚РІРѕ СЃ Р·Р°РїР°С…РѕРј РїРѕСЂРѕС…Р°";
        if (normalized.contains("СѓРЅРёРІРµСЂСЃ") || normalized.contains("Р°РєР°РґРµРј")) return "РЎРµРјРёРЅР°СЂ, РєРѕС‚РѕСЂС‹Р№ РЅРёРєС‚Рѕ РЅРµ РїРµСЂРµР¶РёР»";
        if (normalized.contains("РєРѕСЂРїРѕСЂР°С‚") || normalized.contains("РѕС„РёСЃ") || normalized.contains("Р±РёР·РЅРµСЃ")) return "РџСЂРѕС‚РѕРєРѕР» СЃРѕРІРµС‚Р° РґРёСЂРµРєС‚РѕСЂРѕРІ";

        List<String> titles = List.of(
                "Р“РѕСЂРѕРґ, РіРґРµ РјРѕР»С‡Р°С‚ РєР°РјРµСЂС‹",
                "РџРµРїРµР» РІ РєР°СЂРјР°РЅРµ СЃРІРёРґРµС‚РµР»СЏ",
                "РўСЂРµС‚РёР№ РєР»СЋС‡ РѕС‚ Р·Р°РєСЂС‹С‚РѕР№ РєРѕРјРЅР°С‚С‹",
                "РќРѕС‡СЊ, РєРѕРіРґР° Р°Р»РёР±Рё РЅРµ СЃС…РѕРґСЏС‚СЃСЏ",
                "Р”РµР»Рѕ Рѕ С‚РµРЅРё РІ РІРёС‚СЂРёРЅРµ"
        );
        return titles.get(Math.abs(theme.hashCode()) % titles.size());
    }

    private String buildDynamicFallbackIntro(String theme, String difficulty) {
        String normalized = theme.toLowerCase();
        String pressure;
        if ("hard".equalsIgnoreCase(difficulty)) {
            pressure = "Р’СЂРµРјРµРЅРё РїРѕС‡С‚Рё РЅРµС‚: РїСЂРѕРјРµРґР»РµРЅРёРµ РїСЂРёРІРµРґРµС‚ Рє РїРѕС‚РµСЂРµ РєР»СЋС‡РµРІС‹С… СѓР»РёРє.";
        } else if ("easy".equalsIgnoreCase(difficulty)) {
            pressure = "Р”РµР»Рѕ РІС‹РіР»СЏРґРёС‚ РїСЂРѕСЃС‚С‹Рј С‚РѕР»СЊРєРѕ РЅР° РїРѕРІРµСЂС…РЅРѕСЃС‚Рё: РіР»Р°РІРЅР°СЏ РѕС€РёР±РєР° СЃРїСЂСЏС‚Р°РЅР° РІ РјРµР»РѕС‡Р°С….";
        } else {
            pressure = "Р§РµРј РіР»СѓР±Р¶Рµ РІС‹ Р°РЅР°Р»РёР·РёСЂСѓРµС‚Рµ РјР°С‚РµСЂРёР°Р»С‹, С‚РµРј СЏСЃРЅРµРµ, С‡С‚Рѕ СЌС‚Рѕ Р±С‹Р»Р° Р·Р°СЂР°РЅРµРµ СЃРїР»Р°РЅРёСЂРѕРІР°РЅРЅР°СЏ Р»РµРіРµРЅРґР°.";
        }

        if (normalized.contains("РјСѓР·РµР№") || normalized.contains("РіР°Р»РµСЂРµ")) {
            return "РќРѕС‡СЊСЋ РёР· Р·Р°РєСЂС‹С‚РѕРіРѕ РєСЂС‹Р»Р° РіРѕСЂРѕРґСЃРєРѕРіРѕ РјСѓР·РµСЏ РёСЃС‡РµР· СЌРєСЃРїРѕРЅР°С‚, РєРѕС‚РѕСЂС‹Р№ СѓС‚СЂРѕРј РґРѕР»Р¶РЅС‹ Р±С‹Р»Рё РїРѕРєР°Р·Р°С‚СЊ РјРµР¶РґСѓРЅР°СЂРѕРґРЅРѕР№ РєРѕРјРёСЃСЃРёРё. РЎР»РµРґРѕРІ РІР·Р»РѕРјР° РЅРµС‚, СЃРёРіРЅР°Р»РёР·Р°С†РёСЏ РјРѕР»С‡Р°Р»Р°, Р° РѕС…СЂР°РЅР° СѓС‚РІРµСЂР¶РґР°РµС‚, С‡С‚Рѕ РЅРё РѕРґРёРЅ РїРѕСЃС‚ РЅРµ РїРѕРєРёРґР°Р»Рё. РќР° РјРµСЃС‚Рµ РЅР°Р№РґРµРЅ С‚РѕР»СЊРєРѕ СЃР»СѓР¶РµР±РЅС‹Р№ РїСЂРѕРїСѓСЃРє Сѓ Р°РІР°СЂРёР№РЅРѕРіРѕ РІС‹С…РѕРґР° Рё СЃС‚СЂР°РЅРЅР°СЏ Р·Р°РїРёСЃСЊ РІ Р¶СѓСЂРЅР°Р»Рµ СЂРµСЃС‚Р°РІСЂР°С†РёРё. " + pressure;
        }
        if (normalized.contains("РїРѕРµР·Рґ") || normalized.contains("РІРѕРєР·Р°Р»") || normalized.contains("РјРµС‚СЂРѕ")) {
            return "РќР° РєРѕРЅРµС‡РЅРѕР№ СЃС‚Р°РЅС†РёРё РЅРѕС‡РЅРѕРіРѕ РјР°СЂС€СЂСѓС‚Р° РІ РїСѓСЃС‚РѕРј РІР°РіРѕРЅРµ РЅР°Р№РґРµРЅРѕ С‚РµР»Рѕ РєСѓСЂСЊРµСЂР°, РєРѕС‚РѕСЂС‹Р№ РїРµСЂРµРІРѕР·РёР» РґРёРїР»РѕРјР°С‚РёС‡РµСЃРєРёР№ РєРµР№СЃ. РЎР°Рј РєРµР№СЃ РёСЃС‡РµР·, Р° С‡Р°СЃС‚СЊ РєР°РјРµСЂ РЅР° РїР»Р°С‚С„РѕСЂРјРµ РѕРєР°Р·Р°Р»Р°СЃСЊ РЅРµРґРѕСЃС‚СѓРїРЅР° РёРјРµРЅРЅРѕ РІ РЅСѓР¶РЅС‹Р№ РїСЂРѕРјРµР¶СѓС‚РѕРє РІСЂРµРјРµРЅРё. РџР°СЃСЃР°Р¶РёСЂС‹ РґР°СЋС‚ РїСЂРѕС‚РёРІРѕСЂРµС‡РёРІС‹Рµ РїРѕРєР°Р·Р°РЅРёСЏ Рѕ С‡РµР»РѕРІРµРєРµ, РІС‹С€РµРґС€РµРј Р·Р° РјРёРЅСѓС‚Сѓ РґРѕ РѕСЃС‚Р°РЅРѕРІРєРё. " + pressure;
        }
        if (normalized.contains("С‚РµР°С‚СЂ") || normalized.contains("СЃС†РµРЅР°") || normalized.contains("РѕРїРµСЂР°")) {
            return "Р’Рѕ РІСЂРµРјСЏ РіРµРЅРµСЂР°Р»СЊРЅРѕР№ СЂРµРїРµС‚РёС†РёРё РІРµРґСѓС‰РёР№ Р°РєС‚РµСЂ СѓРїР°Р» РЅР° СЃС†РµРЅРµ Р·Р° РЅРµСЃРєРѕР»СЊРєРѕ РјРёРЅСѓС‚ РґРѕ РїСЂРµРјСЊРµСЂС‹. Р’ Р·Р°Р»Рµ РЅРµ Р±С‹Р»Рѕ Р·СЂРёС‚РµР»РµР№, РЅРѕ Р·Р° РєСѓР»РёСЃР°РјРё РЅР°С…РѕРґРёР»РёСЃСЊ РІСЃРµ РєР»СЋС‡РµРІС‹Рµ С„РёРіСѓСЂС‹ РїРѕСЃС‚Р°РЅРѕРІРєРё: СЂРµР¶РёСЃСЃРµСЂ, РґСѓР±Р»РµСЂ, РїСЂРѕРґСЋСЃРµСЂ Рё РіСЂРёРјРµСЂ. Р”РѕРєСѓРјРµРЅС‚С‹ С‚РµР°С‚СЂР° Рё СЂР°СЃРїРёСЃР°РЅРёРµ СЂРµРїРµС‚РёС†РёР№ РЅР°РјРµРєР°СЋС‚ РЅР° РєРѕРЅС„Р»РёРєС‚, РєРѕС‚РѕСЂС‹Р№ РґР°РІРЅРѕ РІС‹С€РµР» Р·Р° СЂР°РјРєРё С‚РІРѕСЂС‡РµСЃС‚РІР°. " + pressure;
        }
        if (normalized.contains("РѕСЃРѕР±РЅСЏРє") || normalized.contains("РїРѕРјРµСЃС‚") || normalized.contains("СѓСЃР°Рґ")) {
            return "РџРѕСЃР»Рµ СЃРµРјРµР№РЅРѕРіРѕ СѓР¶РёРЅР° РІ Р·Р°РіРѕСЂРѕРґРЅРѕРј РѕСЃРѕР±РЅСЏРєРµ С…РѕР·СЏРёРЅР° РґРѕРјР° РЅР°С€Р»Рё РјРµСЂС‚РІС‹Рј РІ Р·Р°РїРµСЂС‚РѕРј РєР°Р±РёРЅРµС‚Рµ. РћРєРЅР° Р·Р°РєСЂС‹С‚С‹ РёР·РЅСѓС‚СЂРё, РєР°РјРµСЂС‹ РЅР° СЌС‚Р°Р¶Рµ РѕС‚СЃСѓС‚СЃС‚РІСѓСЋС‚, Р° РіРѕСЃС‚Рё СѓРІРµСЂСЏСЋС‚, С‡С‚Рѕ РЅРёРєС‚Рѕ РЅРµ РїРѕРєРёРґР°Р» СЃС‚РѕР»РѕРІСѓСЋ. РћРґРЅР°РєРѕ РЅРµР·Р°РґРѕР»РіРѕ РґРѕ С‚СЂР°РіРµРґРёРё Р±С‹Р»Рѕ РёР·РјРµРЅРµРЅРѕ Р·Р°РІРµС‰Р°РЅРёРµ, Рё РїРѕС‡С‚Рё Сѓ РєР°Р¶РґРѕРіРѕ Р·Р° СЃС‚РѕР»РѕРј РїРѕСЏРІРёР»СЃСЏ РјРѕС‚РёРІ. " + pressure;
        }

        return "Р’ РіРѕСЂРѕРґРµ РїСЂРѕРёР·РѕС€Р»Рѕ РґРµР»Рѕ, РєРѕС‚РѕСЂРѕРµ СЃР»РёС€РєРѕРј Р°РєРєСѓСЂР°С‚РЅРѕ РІС‹РґР°СЋС‚ Р·Р° РЅРµСЃС‡Р°СЃС‚РЅС‹Р№ СЃР»СѓС‡Р°Р№. Р’ РјР°С‚РµСЂРёР°Р»Р°С… СЃР»РµРґСЃС‚РІРёСЏ РІРёРґРЅС‹ РїСЂРѕР±РµР»С‹: РґРѕРєСѓРјРµРЅС‚С‹ РїРѕСЏРІРёР»РёСЃСЊ Р·Р°РґРЅРёРј С‡РёСЃР»РѕРј, СЃРІРёРґРµС‚РµР»Рё РїРѕРІС‚РѕСЂСЏСЋС‚ РѕРґРёРЅР°РєРѕРІС‹Рµ С„РѕСЂРјСѓР»РёСЂРѕРІРєРё, Р° С‚РµС…РЅРёС‡РµСЃРєРёРµ Р»РѕРіРё РЅРµ СЃРѕРІРїР°РґР°СЋС‚ СЃ РѕС„РёС†РёР°Р»СЊРЅС‹Рј РІСЂРµРјРµРЅРµРј РїСЂРѕРёСЃС€РµСЃС‚РІРёСЏ. Р’Р°Рј РїСЂРµРґСЃС‚РѕРёС‚ СЃРѕР±СЂР°С‚СЊ СЂРµР°Р»СЊРЅС‹Р№ С‚Р°Р№РјР»Р°Р№РЅ, РѕС‚СЃРµСЏС‚СЊ РїРѕРґСЃС‚Р°РІРЅС‹Рµ СѓР»РёРєРё Рё РЅР°Р№С‚Рё С‚РѕРіРѕ, РєС‚Рѕ РїСЂРѕСЃС‡РёС‚Р°Р» СЂР°СЃСЃР»РµРґРѕРІР°РЅРёРµ РЅР° РЅРµСЃРєРѕР»СЊРєРѕ С€Р°РіРѕРІ РІРїРµСЂРµРґ. " + pressure;
    }

    private Map<String, Object> buildSuspect(String name, String description, String alibi) {
        Map<String, Object> suspect = new LinkedHashMap<>();
        suspect.put("name", name);
        suspect.put("description", description);
        suspect.put("alibi", alibi);
        return suspect;
    }

    private Map<String, Object> buildClue(String content, int rank) {
        Map<String, Object> clue = new LinkedHashMap<>();
        clue.put("content", content);
        clue.put("type", "text");
        clue.put("importanceRank", rank);
        return clue;
    }

    private Integer parseRank(Object rawRank, int fallback) {
        if (rawRank instanceof Number number) return clampRank(number.intValue(), fallback);
        if (rawRank instanceof String s) {
            try {
                return clampRank(Integer.parseInt(s), fallback);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private int clampRank(int rank, int fallback) {
        if (rank < 1 || rank > 3) return Math.max(1, Math.min(3, fallback));
        return rank;
    }

    private String getString(Object value, String fallback) {
        if (value instanceof String s && !s.isBlank()) return s.trim();
        return fallback;
    }

    private boolean isRateLimitError(Exception e) {
        String message = e.getMessage();
        if (message == null) return false;
        String lower = message.toLowerCase();
        return lower.contains("429")
                || lower.contains("rate-limit")
                || lower.contains("rate limit")
                || lower.contains("temporarily rate-limited");
    }

    private record ModelPlan(String model, double temperature, double topP, int maxTokens) {}
}
