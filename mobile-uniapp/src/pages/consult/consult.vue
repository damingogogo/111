<template>
  <view class="page">
    <view class="hero">
      <view class="hero-title">心理支持资源</view>
      <view class="hero-sub">提供情绪调节、压力识别和求助路径说明，帮助你整理当前状态与下一步行动。</view>
      <image class="hero-visual" src="/static/theme/consult.png" mode="aspectFill" />
    </view>

    <view class="card intro-card">
      <view class="title">使用说明</view>
      <view class="desc">以下内容用于个人自助参考。若出现持续低落、焦虑、睡眠严重受影响等情况，请及时联系企业 HR、线下医疗机构或当地心理援助热线。</view>
    </view>

    <view v-for="item in supportItems" :key="item.title" class="card support-card">
      <view class="tag">{{ item.tag }}</view>
      <view class="title">{{ item.title }}</view>
      <view class="desc">{{ item.desc }}</view>
      <view class="steps">
        <view v-for="step in item.steps" :key="step" class="step">{{ step }}</view>
      </view>
      <view class="btn secondary support-btn" @tap="copyItem(item)">复制指引</view>
    </view>

    <view class="section-title">更多支持</view>
    <view class="card support-card">
      <view class="title">企业内部支持</view>
      <view class="desc">如需进一步帮助，可通过企业内部公开渠道联系 HR 或员工关怀负责人。</view>
    </view>
  </view>
</template>

<script setup>
const supportItems = [
  {
    tag: '情绪调节',
    title: '三分钟呼吸练习',
    desc: '适合紧张、烦躁或注意力难以集中时使用。',
    steps: ['找一个安静位置坐稳', '吸气 4 秒，停顿 2 秒，呼气 6 秒', '重复 6 到 8 轮后记录当前感受']
  },
  {
    tag: '压力识别',
    title: '把压力拆成一件小事',
    desc: '把当前最重的事情拆成今天可以完成的第一步。',
    steps: ['写下最困扰的一件事', '标出可控和不可控部分', '只选择一个 15 分钟内能完成的动作']
  },
  {
    tag: '求助路径',
    title: '需要帮助时的处理顺序',
    desc: '当情绪持续影响睡眠、饮食或工作时，优先寻求现实支持。',
    steps: ['先联系可信任的家人或同事', '同步企业 HR 或员工关怀负责人', '必要时前往正规医疗机构获取专业帮助']
  }
]

function copyItem(item) {
  uni.setClipboardData({
    data: `${item.title}\n${item.steps.join('\n')}`
  })
}
</script>

<style scoped>
.intro-card,
.support-card {
  display: block;
}

.steps {
  display: grid;
  gap: 12rpx;
  margin-top: 18rpx;
}

.step {
  padding: 16rpx 18rpx;
  border-radius: 12rpx;
  background: #f0f5ff;
  color: #315ed6;
  font-size: 25rpx;
  font-weight: 700;
  line-height: 1.5;
}

.support-btn {
  height: 68rpx;
  margin-top: 18rpx;
  font-size: 24rpx;
}
</style>
