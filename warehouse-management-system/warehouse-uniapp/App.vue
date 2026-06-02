<script>
export default {
  onLaunch() {
    this.closeSplashscreen()
    setTimeout(() => {
      this.requestAppPermissions()
    }, 1200)
  },
  methods: {
    closeSplashscreen() {
      // #ifdef APP-PLUS
      if (typeof plus === 'undefined' || !plus.navigator || !plus.navigator.closeSplashscreen) {
        return
      }
      setTimeout(() => {
        try {
          plus.navigator.closeSplashscreen()
        } catch (e) {}
      }, 800)
      // #endif
    },
    requestAppPermissions() {
      // #ifdef APP-PLUS
      if (typeof plus === 'undefined' || !plus.android || !plus.android.requestPermissions) {
        return
      }
      plus.android.requestPermissions(
        [
          'android.permission.READ_EXTERNAL_STORAGE',
          'android.permission.RECORD_AUDIO'
        ],
        () => {},
        () => {}
      )
      // #endif
    }
  }
}
</script>

<style>
/* ========== 全局基础 ========== */
page {
  min-height: 100%;
  background: #F8FAFC;
  color: #1E293B;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  -webkit-font-smoothing: antialiased;
}

view, text, input, textarea, button {
  box-sizing: border-box;
}

button {
  margin: 0;
  line-height: 1.2;
  border-radius: 16rpx;
}

button::after {
  border: 0;
}

/* ========== 页面容器 ========== */
.page {
  min-height: 100vh;
  padding: 24rpx;
  padding-bottom: 140rpx;
  background: #F8FAFC;
}

.page-nopad {
  min-height: 100vh;
  padding-bottom: 140rpx;
  background: #F8FAFC;
}

/* ========== 顶部区域 ========== */
.page-header {
  padding: 16rpx 8rpx 24rpx;
}

.page-header .title {
  font-size: 44rpx;
  font-weight: 800;
  color: #0F172A;
  line-height: 1.2;
}

.page-header .subtitle {
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #64748B;
}

/* ========== 搜索栏 ========== */
.search-wrap {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  height: 84rpx;
  padding: 0 24rpx;
  border-radius: 16rpx;
  background: #FFFFFF;
  border: 1rpx solid #E2E8F0;
}

.search-box .search-icon {
  font-size: 32rpx;
  color: #94A3B8;
  margin-right: 12rpx;
}

.search-box input {
  flex: 1;
  font-size: 28rpx;
  color: #1E293B;
  height: 100%;
}

.search-box input::placeholder {
  color: #CBD5E1;
}

/* ========== 卡片 ========== */
.card {
  margin-bottom: 20rpx;
  padding: 28rpx;
  border-radius: 20rpx;
  background: #FFFFFF;
  box-shadow: 0 2rpx 16rpx rgba(15, 23, 42, 0.04);
  border: 1rpx solid rgba(226, 232, 240, 0.6);
}

.card-flat {
  border: 1rpx solid #E2E8F0;
  box-shadow: none;
}

/* ========== 表单元素 ========== */
.field {
  margin-bottom: 28rpx;
}

.field:last-child {
  margin-bottom: 0;
}

.field-label {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 14rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: #334155;
}

.field-label .required::before {
  content: '*';
  color: #EF4444;
  margin-right: 4rpx;
}

.input,
.textarea,
.picker-box {
  width: 100%;
  min-height: 92rpx;
  padding: 0 24rpx;
  border: 1rpx solid #E2E8F0;
  border-radius: 16rpx;
  background: #FFFFFF;
  font-size: 28rpx;
  color: #1E293B;
  transition: border-color 0.2s;
}

.input:focus,
.textarea:focus {
  border-color: #4F46E5;
}

.textarea {
  height: 170rpx;
  padding-top: 20rpx;
  line-height: 1.5;
}

.picker-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #1E293B;
}

.picker-box .picker-arrow {
  font-size: 24rpx;
  color: #94A3B8;
}

/* ========== 按钮 ========== */
.btn-primary,
.btn-ghost,
.btn-danger,
.btn-success {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 92rpx;
  padding: 0 32rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
}

.btn-primary {
  color: #FFFFFF;
  background: linear-gradient(135deg, #4F46E5 0%, #6366F1 100%);
  box-shadow: 0 4rpx 16rpx rgba(79, 70, 229, 0.3);
}

.btn-primary:active {
  opacity: 0.9;
}

.btn-ghost {
  color: #4F46E5;
  border: 1rpx solid #C7D2FE;
  background: #EEF2FF;
}

.btn-danger {
  color: #EF4444;
  border: 1rpx solid #FECACA;
  background: #FEF2F2;
}

.btn-success {
  color: #10B981;
  border: 1rpx solid #A7F3D0;
  background: #ECFDF5;
}

.btn-sm {
  min-height: 64rpx;
  padding: 0 20rpx;
  font-size: 24rpx;
  border-radius: 12rpx;
}

.btn-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.btn-block {
  width: 100%;
}

.btn-voice {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 120rpx;
  min-height: 92rpx;
  border-radius: 16rpx;
  color: #4F46E5;
  border: 1rpx solid #C7D2FE;
  background: #EEF2FF;
  font-size: 24rpx;
}

/* ========== 布局工具 ========== */
.row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.row-between {
  justify-content: space-between;
}

.row-end {
  justify-content: flex-end;
}

.row-wrap {
  flex-wrap: wrap;
}

.col {
  flex: 1;
  min-width: 0;
}

.gap-xs { gap: 8rpx; }
.gap-sm { gap: 12rpx; }
.gap-md { gap: 20rpx; }
.gap-lg { gap: 32rpx; }

.mt-sm { margin-top: 12rpx; }
.mt-md { margin-top: 20rpx; }
.mt-lg { margin-top: 32rpx; }

.mb-sm { margin-bottom: 12rpx; }
.mb-md { margin-bottom: 20rpx; }
.mb-lg { margin-bottom: 32rpx; }

/* ========== 文字工具 ========== */
.text-xs { font-size: 22rpx; }
.text-sm { font-size: 24rpx; }
.text-base { font-size: 28rpx; }
.text-lg { font-size: 32rpx; }
.text-xl { font-size: 40rpx; }

.text-slate-400 { color: #94A3B8; }
.text-slate-500 { color: #64748B; }
.text-slate-600 { color: #475569; }
.text-slate-700 { color: #334155; }
.text-slate-800 { color: #1E293B; }
.text-slate-900 { color: #0F172A; }
.text-primary { color: #4F46E5; }
.text-success { color: #10B981; }
.text-danger { color: #EF4444; }
.text-warning { color: #F59E0B; }

.font-medium { font-weight: 500; }
.font-semibold { font-weight: 600; }
.font-bold { font-weight: 700; }
.font-extrabold { font-weight: 800; }

/* ========== 状态标签 ========== */
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  min-height: 48rpx;
  padding: 0 18rpx;
  border-radius: 24rpx;
  font-size: 22rpx;
  font-weight: 600;
}

.status-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
}

.status-waiting {
  color: #B45309;
  background: #FEF3C7;
}
.status-waiting .status-dot { background: #F59E0B; }

.status-claimed {
  color: #1D4ED8;
  background: #DBEAFE;
}
.status-claimed .status-dot { background: #3B82F6; }

.status-accepted {
  color: #6D28D9;
  background: #EDE9FE;
}
.status-accepted .status-dot { background: #8B5CF6; }

.status-shelved {
  color: #0E7490;
  background: #CFFAFE;
}
.status-shelved .status-dot { background: #06B6D4; }

.status-stored {
  color: #047857;
  background: #D1FAE5;
}
.status-stored .status-dot { background: #10B981; }

/* ========== 空状态 ========== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
}

.empty-state .empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.empty-state .empty-text {
  font-size: 28rpx;
  color: #94A3B8;
}

.empty-state .empty-hint {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #CBD5E1;
}

/* ========== 分割线 ========== */
.divider {
  height: 1rpx;
  background: #E2E8F0;
  margin: 20rpx 0;
}

/* ========== 固定底部 ========== */
.fixed-bottom-actions {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10;
  padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));
  background: #FFFFFF;
  border-top: 1rpx solid #E2E8F0;
  box-shadow: 0 -4rpx 20rpx rgba(15, 23, 42, 0.04);
}

/* ========== 弹窗遮罩 ========== */
.dialog-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
  background: rgba(15, 23, 42, 0.5);
}

.dialog-box {
  width: 100%;
  max-width: 600rpx;
  padding: 36rpx;
  border-radius: 24rpx;
  background: #FFFFFF;
  box-shadow: 0 20rpx 60rpx rgba(15, 23, 42, 0.15);
}

.dialog-title {
  margin-bottom: 24rpx;
  font-size: 32rpx;
  font-weight: 700;
  color: #0F172A;
  text-align: center;
}

.dialog-actions {
  margin-top: 28rpx;
  gap: 16rpx;
}

/* ========== 列表项通用 ========== */
.item-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #0F172A;
}

.item-code {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #64748B;
  font-family: "SF Mono", Monaco, monospace;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx 20rpx;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #F1F5F9;
  font-size: 24rpx;
  color: #475569;
}

.info-grid .info-label {
  color: #94A3B8;
}

.actions-bar {
  display: flex;
  justify-content: flex-end;
  gap: 12rpx;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #F1F5F9;
}

/* ========== 统计卡片 ========== */
.stat-card {
  position: relative;
  overflow: hidden;
  padding: 28rpx;
  border-radius: 20rpx;
  background: #FFFFFF;
  box-shadow: 0 2rpx 16rpx rgba(15, 23, 42, 0.04);
}

.stat-card .stat-icon {
  font-size: 48rpx;
  margin-bottom: 12rpx;
}

.stat-card .stat-value {
  font-size: 44rpx;
  font-weight: 800;
  color: #0F172A;
  line-height: 1.1;
}

.stat-card .stat-label {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #64748B;
}

/* ========== 快捷入口 ========== */
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 28rpx 16rpx;
  border-radius: 20rpx;
  background: #FFFFFF;
  box-shadow: 0 2rpx 16rpx rgba(15, 23, 42, 0.04);
  border: 1rpx solid rgba(226, 232, 240, 0.6);
}

.quick-item .quick-icon {
  font-size: 52rpx;
  margin-bottom: 12rpx;
}

.quick-item .quick-label {
  font-size: 24rpx;
  color: #475569;
  font-weight: 500;
}

/* ========== 顶部用户信息栏 ========== */
.user-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 8rpx 24rpx;
}

.user-bar .user-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.user-bar .user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #4F46E5, #818CF8);
  color: #FFFFFF;
  font-size: 28rpx;
  font-weight: 700;
}

.user-bar .user-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #0F172A;
}

.user-bar .user-greeting {
  font-size: 24rpx;
  color: #94A3B8;
  margin-top: 4rpx;
}

/* ========== 筛选器 ========== */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
  padding: 16rpx 20rpx;
  border-radius: 16rpx;
  background: #FFFFFF;
  border: 1rpx solid #E2E8F0;
}

.filter-bar .filter-icon {
  font-size: 28rpx;
  color: #64748B;
}

.filter-bar text {
  font-size: 26rpx;
  color: #475569;
}

/* ========== 顶部安全区适配 ========== */
.safe-top {
  padding-top: var(--status-bar-height);
}
</style>
