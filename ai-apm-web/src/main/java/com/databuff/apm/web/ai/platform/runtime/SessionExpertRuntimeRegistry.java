package com.databuff.apm.web.ai.platform.runtime;

import com.databuff.apm.web.ai.InMemoryLlmProviderStore;
import com.databuff.apm.web.ai.OpenAiCompatibleChatClient;
import com.databuff.apm.web.ai.platform.expert.AiExpertDefinition;
import com.databuff.apm.web.ai.platform.expert.BrainRoutingCatalog;
import com.databuff.apm.web.ai.platform.skill.AiSkillDefinition;
import com.databuff.apm.web.ai.platform.skill.SkillManagementService;
import com.databuff.apm.web.ai.platform.tool.AiToolDefinition;
import com.databuff.apm.web.ai.platform.tool.ExpertToolResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Keeps one AgentScope runtime per chat session for the brain expert so async
 * continuations reuse the same agent instance created at session start.
 */
@Service
@Lazy
public class SessionExpertRuntimeRegistry {

    private static final Logger log = LoggerFactory.getLogger(SessionExpertRuntimeRegistry.class);
    private static final String BRAIN_EXPERT_ID = "brain";

    private final SkillManagementService skillManagementService;
    private final InMemoryLlmProviderStore llmProviderStore;
    private final ExpertToolResolver expertToolResolver;
    private final AgentScopeRuntimeAdapter runtimeAdapter;
    private final BrainRoutingCatalog brainRoutingCatalog;
    private final ConcurrentMap<String, CachedSessionRuntime> runtimes = new ConcurrentHashMap<>();

    public SessionExpertRuntimeRegistry(
            SkillManagementService skillManagementService,
            InMemoryLlmProviderStore llmProviderStore,
            ExpertToolResolver expertToolResolver,
            AgentScopeRuntimeAdapter runtimeAdapter,
            BrainRoutingCatalog brainRoutingCatalog) {
        this.skillManagementService = skillManagementService;
        this.llmProviderStore = llmProviderStore;
        this.expertToolResolver = expertToolResolver;
        this.runtimeAdapter = runtimeAdapter;
        this.brainRoutingCatalog = brainRoutingCatalog;
    }

    public ExpertRuntime getOrCreate(String sessionId, AiExpertDefinition expert) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId is required for session-scoped runtime");
        }
        if (expert == null || !BRAIN_EXPERT_ID.equals(expert.expertId())) {
            throw new IllegalArgumentException("session-scoped runtime only supports brain expert");
        }
        if (!expert.enabled()) {
            throw new IllegalStateException("expert is disabled: " + expert.expertId());
        }
        RuntimeCacheKey expectedKey = computeCacheKey(expert);
        String normalizedSessionId = sessionId.trim();
        CachedSessionRuntime cached = runtimes.get(normalizedSessionId);
        if (cached != null && cached.cacheKey.fingerprint().equals(expectedKey.fingerprint())) {
            return cached.runtime;
        }
        synchronized (lockFor(normalizedSessionId)) {
            cached = runtimes.get(normalizedSessionId);
            if (cached != null && cached.cacheKey.fingerprint().equals(expectedKey.fingerprint())) {
                return cached.runtime;
            }
            if (cached != null) {
                cached.runtime.close();
            }
            ExpertRuntime runtime = runtimeAdapter.buildSessionRuntime(expert, normalizedSessionId);
            RuntimeCacheKey actualKey = runtime instanceof AgentScopeExpertRuntime scoped
                    ? scoped.cacheKey()
                    : expectedKey;
            runtimes.put(normalizedSessionId, new CachedSessionRuntime(actualKey, runtime));
            log.info("Created session-scoped brain runtime for session {} with cache key {}",
                    normalizedSessionId, actualKey.fingerprint());
            return runtime;
        }
    }

    public void release(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        CachedSessionRuntime removed = runtimes.remove(sessionId.trim());
        if (removed != null) {
            removed.runtime.close();
            log.info("Released session-scoped brain runtime for session {}", sessionId.trim());
        }
    }

    public void releaseAll() {
        for (String sessionId : runtimes.keySet()) {
            release(sessionId);
        }
    }

    private RuntimeCacheKey computeCacheKey(AiExpertDefinition expert) {
        List<AiToolDefinition> tools = expertToolResolver.resolve(expert);
        java.util.List<AiSkillDefinition> skills = expert.skillIds().stream()
                .map(skillManagementService::find)
                .flatMap(java.util.Optional::stream)
                .filter(AiSkillDefinition::enabled)
                .toList();
        OpenAiCompatibleChatClient.ResolvedLlmProvider provider = runtimeAdapter.resolveProvider(expert)
                .orElse(null);
        String providerCode = provider == null ? "default" : provider.providerCode();
        long providerVersion = provider == null ? 0L : llmProviderStore.providerVersion(providerCode);
        String routingCatalogHash = brainRoutingCatalog.routableExpertsFingerprint();
        return RuntimeCacheKey.of(expert, tools, skills, providerCode, providerVersion, routingCatalogHash);
    }

    private Object lockFor(String sessionId) {
        return ("session-expert-runtime-lock:" + sessionId).intern();
    }

    private record CachedSessionRuntime(RuntimeCacheKey cacheKey, ExpertRuntime runtime) {
    }
}
