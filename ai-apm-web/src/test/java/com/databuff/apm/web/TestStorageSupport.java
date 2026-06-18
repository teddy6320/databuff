package com.databuff.apm.web;

import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.web.portal.GlobalTopologyQueryService;
import com.databuff.apm.web.config.ApmStorageProperties;
import com.databuff.apm.web.portal.BusinessPortalService;
import com.databuff.apm.web.portal.ServicePortalService;

public final class TestStorageSupport {

    private TestStorageSupport() {
    }

    public static ApmStorageProperties storage() {
        return new ApmStorageProperties("databuff", "databuff", "databuff");
    }

    public static GlobalTopologyQueryService globalTopologyQueryService(ApmReadRepository reader) {
        return new GlobalTopologyQueryService(reader, storage());
    }

    public static ServicePortalService servicePortalService(ApmReadRepository reader) {
        return new ServicePortalService(reader, storage(), globalTopologyQueryService(reader));
    }

    public static BusinessPortalService businessPortalService(ApmReadRepository reader) {
        return new BusinessPortalService(reader, storage(), globalTopologyQueryService(reader));
    }
}
