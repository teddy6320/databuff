package com.databuff.apm.web.ai.platform.runtime;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpertRuntime {

    String expertId();

    long version();

    Mono<ExpertChatResult> chat(ExpertChatInput input);

    Flux<ExpertRuntimeEvent> stream(ExpertChatInput input);

    ExpertRuntimeStatus status();

    void close();
}
