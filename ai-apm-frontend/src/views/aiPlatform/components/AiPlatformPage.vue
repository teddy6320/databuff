<template>
  <div class="ai-platform-page" v-loading="loading">
    <div class="page-header">
      <div class="page-header-main">
        <div class="page-icon" v-if="icon">
          <i :class="icon"></i>
        </div>
        <div class="page-header-text">
          <h1 class="page-title">{{ title }}</h1>
          <p v-if="subtitle" class="page-subtitle">{{ subtitle }}</p>
        </div>
      </div>
      <div v-if="$slots.stats" class="page-stats">
        <slot name="stats" />
      </div>
    </div>

    <div v-if="$slots.toolbar" class="page-toolbar">
      <slot name="toolbar" />
    </div>

    <div class="page-card">
      <slot />
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';

@Component
export default class AiPlatformPage extends Vue {
  @Prop({ required: true }) private title!: string;
  @Prop({ default: '' }) private subtitle!: string;
  @Prop({ default: '' }) private icon!: string;
  @Prop({ default: false }) private loading!: boolean;
}
</script>

<style lang="scss" scoped>
.ai-platform-page {
  flex: 1;
  min-height: 0;
  height: 100%;
  padding: 16px 18px 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background:
    radial-gradient(circle at 7% 8%, rgba(46, 139, 255, 0.14), transparent 30%),
    radial-gradient(circle at 90% 10%, rgba(23, 184, 144, 0.11), transparent 28%),
    linear-gradient(180deg, #f3f6ff 0%, #f7f9fc 46%, #f8fafc 100%);
  box-sizing: border-box;
  overflow: hidden;
}

.page-header {
  flex: none;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  min-height: 78px;
  padding: 16px 18px;
  border: 1px solid rgba(221, 228, 239, 0.95);
  border-radius: 12px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(250, 252, 255, 0.92)),
    #fff;
  box-shadow: 0 10px 26px rgba(43, 55, 86, 0.06);
  overflow: hidden;

  &::before,
  &::after {
    content: '';
    position: absolute;
    pointer-events: none;
    border-radius: 999px;
  }

  &::before {
    right: 260px;
    top: -88px;
    width: 190px;
    height: 190px;
    background: radial-gradient(circle, rgba(41, 98, 255, 0.12), rgba(41, 98, 255, 0));
  }

  &::after {
    right: -60px;
    bottom: -120px;
    width: 220px;
    height: 220px;
    border: 34px solid rgba(23, 184, 144, 0.06);
  }
}

.page-header-main {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  gap: 13px;
  min-width: 0;
  padding-top: 2px;
}

.page-icon {
  flex: none;
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #1f5eff;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(236, 243, 255, 0.92)),
    linear-gradient(135deg, rgba(41, 98, 255, 0.18), rgba(23, 184, 144, 0.12));
  box-shadow:
    inset 0 0 0 1px rgba(41, 98, 255, 0.16),
    0 12px 24px rgba(41, 98, 255, 0.14);
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.3;
  color: #111827;
}

.page-subtitle {
  margin: 5px 0 0;
  font-size: 14px;
  line-height: 1.6;
  color: #66758f;
}

.page-stats {
  flex: none;
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.page-toolbar {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
  min-height: 58px;
  padding: 11px 14px;
  border: 1px solid rgba(222, 229, 242, 0.92);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 10px 24px rgba(43, 55, 86, 0.05);

  :deep(.toolbar-left),
  :deep(.toolbar-right) {
    display: flex;
    align-items: center;
  }

  :deep(.toolbar-left) {
    gap: 12px;

    .el-button + .el-button {
      margin-left: 0;
    }
  }

  :deep(.toolbar-right) {
    justify-content: flex-end;
    gap: 12px;
    flex: 1;
    min-width: 420px;
    margin-left: auto;
  }

  :deep(.filter-input) {
    width: min(380px, 48vw);

    .el-input__inner {
      height: 36px;
      padding-left: 34px;
      border-radius: 11px;
      border-color: transparent;
      color: #25324a;
      background: #f7f9fd;
      box-shadow: inset 0 0 0 1px #e3eaf4;
      transition: all 0.18s ease;

      &:hover {
        background: #fff;
        box-shadow: inset 0 0 0 1px #d3ddec;
      }

      &:focus {
        border-color: transparent;
        background: #fff;
        box-shadow:
          inset 0 0 0 1px rgba(41, 98, 255, 0.44),
          0 0 0 3px rgba(41, 98, 255, 0.08);
      }
    }

    .el-input__prefix {
      left: 11px;
      color: #71809a;
    }
  }

  :deep(.segmented-filter) {
    flex: none;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    height: 36px;
    padding: 4px;
    border: 1px solid #e3eaf4;
    border-radius: 12px;
    background: #f7f9fd;
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72);

    .el-radio-button {
      display: inline-flex;
    }

    .el-radio-button__inner {
      height: 26px;
      min-width: 52px;
      padding: 6px 12px;
      border: none;
      border-radius: 9px !important;
      color: #55667f;
      background: transparent;
      box-shadow: none;
      line-height: 14px;
      transition: all 0.18s ease;

      &:hover {
        color: #1f5eff;
        background: #eef4ff;
      }
    }

    .el-radio-button__orig-radio:checked + .el-radio-button__inner {
      color: #1f5eff;
      background: #fff;
      box-shadow: 0 5px 12px rgba(41, 98, 255, 0.13);
    }
  }

  :deep(.el-button--primary) {
    height: 36px;
    padding: 0 16px;
    border: none;
    border-radius: 10px;
    background: linear-gradient(135deg, #2f6cff 0%, #1f57dc 100%);
    box-shadow: 0 8px 16px rgba(41, 98, 255, 0.2);
    transition: all 0.18s ease;

    &:hover,
    &:focus {
      transform: translateY(-1px);
      box-shadow: 0 10px 20px rgba(41, 98, 255, 0.24);
    }
  }

}

.page-card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 0;
  border: 1px solid rgba(222, 229, 242, 0.95);
  border-radius: 12px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(255, 255, 255, 0.94)),
    #fff;
  box-shadow: 0 14px 30px rgba(43, 55, 86, 0.07);
  overflow: hidden;

  :deep(.el-table) {
    flex: 1;
    min-height: 0;
    color: #24324b;
    background: transparent;

    &::before {
      display: none;
    }

    th.el-table__cell {
      padding: 12px 0;
      font-size: 13px;
      font-weight: 700;
      color: #41506a;
      background: #f5f8fd;
      border-bottom: 1px solid #e7edf7;
    }

    td.el-table__cell {
      padding: 14px 0;
      font-size: 13px;
      border-bottom: 1px solid #edf2f8;
    }

    .el-table__body tr:hover > td.el-table__cell {
      background: #f7fbff;
    }

    .el-table__empty-block {
      min-height: 200px;
    }
  }

  :deep(.el-table--border),
  :deep(.el-table--group) {
    border: none;

    .el-table__cell {
      border-right: none;
    }
  }

  :deep(.section-block) {
    display: flex;
    flex-direction: column;
    min-height: 0;
    flex: 1;

    & + .section-block {
      margin-top: 20px;
      padding-top: 20px;
      border-top: 1px solid #eef1f7;
    }
  }

  :deep(.section-head) {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  :deep(.section-title) {
    font-size: 15px;
    font-weight: 600;
    color: #24324b;
  }

  :deep(.section-desc) {
    font-size: 12px;
    color: #98a2b3;
  }

  :deep(.action-link) {
    display: inline-flex;
    align-items: center;
    height: 26px;
    padding: 0 7px;
    border-radius: 7px;
    color: #245bdb;
    cursor: pointer;
    font-size: 13px;
    transition: color 0.2s ease, background-color 0.2s ease;

    &:hover {
      color: #1747b8;
      background: #edf4ff;
    }

    &.danger {
      color: #ef4444;

      &:hover {
        color: #dc2626;
        background: #fff1f2;
      }
    }

    & + .action-link {
      margin-left: 3px;
    }
  }

  :deep(.resource-board) {
    flex: 1;
    min-height: 0;
    display: grid;
    grid-template-columns: 220px minmax(0, 1fr);
  }

  :deep(.resource-rail) {
    min-height: 0;
    padding: 16px 12px;
    border-right: 1px solid #edf2f8;
    background: linear-gradient(180deg, #fbfcff 0%, #f7f9fc 100%);
  }

  :deep(.rail-title) {
    padding: 0 10px 10px;
    font-size: 12px;
    font-weight: 700;
    color: #8290a5;
  }

  :deep(.rail-item) {
    width: 100%;
    height: 38px;
    margin-bottom: 6px;
    padding: 0 10px;
    border: 1px solid transparent;
    border-radius: 9px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #40516d;
    background: transparent;
    cursor: pointer;
    text-align: left;
    transition: all 0.18s ease;

    &:hover {
      background: #fff;
      border-color: #e1e8f4;
    }

    &.is-active {
      color: #1f5eff;
      background: #fff;
      border-color: rgba(41, 98, 255, 0.22);
      box-shadow: 0 8px 18px rgba(41, 98, 255, 0.08);
    }
  }

  :deep(.rail-count) {
    min-width: 26px;
    height: 20px;
    padding: 0 7px;
    border-radius: 999px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 700;
    color: #64748b;
    background: #eef3f9;
  }

  :deep(.rail-item.is-active .rail-count) {
    color: #1f5eff;
    background: #eaf1ff;
  }

  :deep(.resource-main) {
    min-width: 0;
    min-height: 0;
    padding: 14px 16px;
    display: flex;
    flex-direction: column;
  }

  :deep(.table-meta-bar) {
    flex: none;
    min-height: 38px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 10px;
  }

  :deep(.table-title) {
    font-size: 15px;
    font-weight: 700;
    color: #1f2937;
  }

  :deep(.table-desc) {
    margin-top: 3px;
    font-size: 12px;
    color: #7a889e;
  }

  :deep(.table-tools) {
    flex: none;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  :deep(.category-stack) {
    flex: 1;
    min-height: 0;
    overflow: auto;
    display: flex;
    flex-direction: column;
    gap: 14px;
    padding-right: 2px;
  }

  :deep(.category-section) {
    flex: none;
    border: 1px solid #edf2f8;
    border-radius: 12px;
    background: #fff;
    overflow: hidden;
  }

  :deep(.category-head) {
    min-height: 48px;
    padding: 12px 14px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    border-bottom: 1px solid #edf2f8;
    background: linear-gradient(180deg, #fbfdff, #f7faff);
  }

  :deep(.category-title) {
    font-size: 14px;
    font-weight: 700;
    color: #1f2937;
  }

  :deep(.category-desc) {
    margin-top: 2px;
    font-size: 12px;
    color: #7a889e;
  }

  :deep(.category-count) {
    flex: none;
    min-width: 30px;
    height: 24px;
    padding: 0 9px;
    border-radius: 999px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 700;
    color: #1f5eff;
    background: #eaf1ff;
  }

  :deep(.category-empty) {
    flex: 1;
    min-height: 180px;
    border: 1px dashed #dce5f2;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    color: #8390a5;
    background: #fbfdff;
  }

  :deep(.resource-table) {
    flex: 1;
    min-height: 0;
    border: 1px solid #edf2f8;
    border-radius: 10px;
    overflow: hidden;
  }

  :deep(.category-table) {
    border: none;
    border-radius: 0;
  }

  :deep(.primary-cell) {
    min-width: 0;
  }

  :deep(.primary-name) {
    margin-top: 5px;
    color: #6b7a91;
    font-size: 12px;
  }

  :deep(.uri-cell) {
    display: inline-flex;
    max-width: 100%;
    padding: 6px 8px;
    border-radius: 8px;
    background: #f6f8fb;
  }

  :deep(.status-tag) {
    display: inline-flex;
    align-items: center;
    height: 24px;
    padding: 0 10px 0 8px;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 700;

    &::before {
      content: '';
      width: 6px;
      height: 6px;
      margin-right: 6px;
      border-radius: 999px;
      background: currentColor;
      box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.72);
    }

    &.is-on {
      color: #16a34a;
      background: rgba(22, 163, 74, 0.12);
    }

    &.is-off {
      color: #64748b;
      background: #f1f5f9;
    }

    &.is-loaded {
      color: #2962ff;
      background: #eaefff;
    }
  }

  :deep(.source-tag) {
    display: inline-flex;
    align-items: center;
    height: 24px;
    padding: 0 9px;
    border: 1px solid rgba(218, 226, 239, 0.9);
    border-radius: 7px;
    font-size: 12px;
    color: #536780;
    background: #f8fbff;
  }

  :deep(.mono-text) {
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
    font-size: 12px;
    color: #314f7d;
    font-weight: 600;
  }

}

@media (max-width: 960px) {
  .ai-platform-page {
    padding: 14px;
  }

  .page-header {
    flex-direction: column;
    min-height: 0;
    padding: 18px;
  }

  .page-stats {
    width: 100%;
    justify-content: flex-start;
  }

  .page-toolbar {
    align-items: stretch;
  }

  .page-toolbar :deep(.toolbar-right) {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
    min-width: 0;
    margin-left: 0;
  }

  .page-toolbar :deep(.filter-input) {
    width: 100%;
  }

  .page-card :deep(.resource-board) {
    grid-template-columns: 1fr;
  }

  .page-card :deep(.resource-rail) {
    display: none;
  }
}
</style>

<style lang="scss">
.ai-platform-drawer {
  .el-drawer__header {
    margin-bottom: 0;
    padding: 18px 22px;
    border-bottom: 1px solid #eef1f7;
    font-size: 17px;
    font-weight: 600;
    color: #24324b;
  }

  .el-drawer__body {
    padding: 0;
    background: #f6f8fb;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }

  .drawer-footer {
    flex: none;
    margin-top: 0;
    padding: 14px 22px;
    border-top: 1px solid #eef1f7;
    background: rgba(255, 255, 255, 0.92);
    text-align: right;

    .el-button + .el-button {
      margin-left: 10px;
    }
  }

  .drawer-content {
    flex: 1;
    min-height: 0;
    padding: 18px 22px 22px;
    overflow: auto;
  }

  .form-section {
    padding: 16px;
    border: 1px solid #e6edf7;
    border-radius: 12px;
    background: #fff;

    & + .form-section {
      margin-top: 14px;
    }
  }

  .form-section-head {
    margin-bottom: 14px;
  }

  .form-section-title {
    font-size: 14px;
    font-weight: 700;
    color: #1f2937;
  }

  .form-section-desc {
    margin-top: 4px;
    font-size: 12px;
    line-height: 18px;
    color: #7a889e;
  }

  .form-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    column-gap: 14px;

    .is-wide {
      grid-column: 1 / -1;
    }
  }

  .el-form-item__label {
    color: #475a7d;
    font-weight: 500;
  }

  .el-input__inner,
  .el-textarea__inner {
    border-radius: 8px;
    border-color: #e2e8f0;
    background: #fbfdff;
  }

  .el-select,
  .el-input-number {
    width: 100%;
  }

  .el-form-item {
    margin-bottom: 16px;
  }

  .el-form-item:last-child {
    margin-bottom: 0;
  }
}
</style>
