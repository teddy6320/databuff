package com.databuff.apm.web.ai.platform.skill;

/** Built-in skill packages live under {@code deploy/common/skills/} (see {@code apm.agent.builtin-skills-dir}). */
public final class DeployCommonSkills {

    public static final String URI_PREFIX = "deploy-common:";

    private DeployCommonSkills() {
    }

    public static String contentUri(String skillId) {
        return URI_PREFIX + skillId;
    }

    public static boolean isDeployCommonUri(String contentUri) {
        return contentUri != null && contentUri.startsWith(URI_PREFIX);
    }

    public static String skillIdFromUri(String contentUri) {
        if (!isDeployCommonUri(contentUri)) {
            return null;
        }
        String skillId = contentUri.substring(URI_PREFIX.length()).trim();
        return skillId.isEmpty() ? null : skillId;
    }
}
