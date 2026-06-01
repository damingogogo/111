<script>
export default {
  onLaunch() {}
}
</script>

<style>
page {
  min-height: 100%;
  background: #f7fbff;
  color: #121a33;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

.page {
  padding: 28rpx 24rpx 36rpx;
  background:
    linear-gradient(180deg, #f5f1ff 0%, rgba(247, 251, 255, 0) 280rpx),
    linear-gradient(180deg, #f7fbff 0%, #fbfcff 100%);
}

.hero {
  position: relative;
  min-height: 214rpx;
  padding: 38rpx 32rpx;
  padding-right: 250rpx;
  border-radius: 16rpx;
  color: #121a33;
  overflow: hidden;
  background: linear-gradient(135deg, #efe8ff 0%, #f8fbff 52%, #e8f6ff 100%);
  box-shadow: 0 18rpx 46rpx rgba(69, 92, 168, 0.12);
}

.hero-visual {
  position: absolute;
  right: -18rpx;
  bottom: -18rpx;
  width: 260rpx;
  height: 170rpx;
  border-radius: 22rpx;
  opacity: 0.92;
}

.hero-title {
  font-size: 42rpx;
  font-weight: 800;
  line-height: 1.2;
}

.hero-sub {
  margin-top: 16rpx;
  color: #6f7690;
  font-size: 26rpx;
  line-height: 1.7;
}

.section-title {
  margin: 34rpx 0 18rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 32rpx;
  font-weight: 800;
}

.card {
  margin-bottom: 18rpx;
  padding: 24rpx;
  border: 1rpx solid #edf1fb;
  border-radius: 14rpx;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 14rpx 38rpx rgba(78, 97, 145, 0.08);
}

.row {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.cover {
  width: 148rpx;
  height: 112rpx;
  flex: 0 0 auto;
  border-radius: 12rpx;
  background: #eef5ff;
}

.title {
  font-size: 30rpx;
  font-weight: 700;
}

.desc {
  margin-top: 10rpx;
  color: #747b92;
  font-size: 24rpx;
  line-height: 1.6;
}

.tag {
  display: inline-flex;
  margin-top: 12rpx;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: #eef5ff;
  color: #3e6ff1;
  font-size: 22rpx;
}

.btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 78rpx;
  border-radius: 12rpx;
  background: linear-gradient(90deg, #3f72f5 0%, #9a5ce4 100%);
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 700;
  box-shadow: 0 12rpx 24rpx rgba(76, 97, 223, 0.18);
}

.btn.secondary {
  background: #f0f5ff;
  color: #3e5b99;
  box-shadow: none;
}

.input {
  min-height: 76rpx;
  padding: 0 20rpx;
  border: 1rpx solid #e4eaf7;
  border-radius: 12rpx;
  background: rgba(255, 255, 255, 0.94);
}
</style>
