<template>
  <view class="page">
    <view class="hero">
      <view class="hero-title">一对一专业咨询</view>
      <view class="hero-sub">员工自主选择平台认证咨询师，可预约线上视频或语音咨询，记录全程加密保密。</view>
      <image class="hero-visual" src="/static/theme/consult.png" mode="aspectFill" />
    </view>
    <view class="card booking-form">
      <view class="title">预约设置</view>
      <picker mode="date" :value="form.date" @change="form.date = $event.detail.value">
        <view class="input picker">日期：{{ form.date }}</view>
      </picker>
      <picker mode="selector" :range="times" @change="form.time = times[$event.detail.value]">
        <view class="input picker">时间：{{ form.time }}</view>
      </picker>
      <picker mode="selector" :range="methods" @change="method = methods[$event.detail.value]">
        <view class="input picker">方式：{{ method }}</view>
      </picker>
      <textarea class="textarea" v-model="form.notes" placeholder="想和咨询师沟通的问题（可选）" />
    </view>

    <view v-for="item in consultants" :key="item.id" class="card">
      <view class="row">
        <image class="avatar" :src="imageUrl(item.avatar_url)" mode="aspectFill" @error="usePlaceholder(item, 'avatar_url')" />
        <view>
          <view class="title">{{ item.name }}</view>
          <view class="desc">{{ item.title }} · {{ item.speciality }}</view>
        </view>
      </view>
      <view class="btn" @tap="book(item)">预约 {{ form.date }} {{ form.time }}</view>
    </view>

    <view class="section-title">我的预约</view>
    <view class="tabs">
      <view v-for="item in appointmentTabs" :key="item.value" class="tab" :class="{ active: appointmentFilter === item.value }" @tap="appointmentFilter = item.value">{{ item.label }}</view>
    </view>
    <view v-if="visibleAppointments.length === 0" class="card">
      <view class="desc">还没有预约记录。</view>
    </view>
    <view v-for="item in visibleAppointments" :key="item.id" class="card appointment">
      <view class="title">{{ item.consultant_name }} · {{ item.method }}</view>
      <view class="desc">{{ item.appointment_time }}</view>
      <view class="tag">{{ item.status }}</view>
      <view class="desc" v-if="item.notes">{{ item.notes }}</view>
      <view class="appointment-actions" v-if="item.status !== '已取消'">
        <view class="btn secondary action-btn" @tap="reschedule(item)">按上方时间改约</view>
        <view class="btn danger action-btn" @tap="cancel(item)">取消预约</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { computed, reactive, ref } from 'vue'
import { imageUrl, usePlaceholder } from '../../utils/images.js'
import { request, requireEmployee } from '../../utils/request.js'

const consultants = ref([])
const appointments = ref([])
const appointmentFilter = ref('active')
const methods = ['视频', '语音']
const times = ['09:30:00', '10:00:00', '14:00:00', '15:30:00', '19:00:00']
const appointmentTabs = [
  { label: '有效', value: 'active' },
  { label: '已取消', value: 'canceled' },
  { label: '全部', value: 'all' }
]
const visibleAppointments = computed(() => {
  if (appointmentFilter.value === 'all') return appointments.value
  if (appointmentFilter.value === 'canceled') return appointments.value.filter((item) => item.status === '已取消')
  return appointments.value.filter((item) => item.status !== '已取消')
})
const method = ref('视频')
const form = reactive({
  date: nextDate(),
  time: '10:00:00',
  notes: ''
})

function nextDate() {
  const value = new Date(Date.now() + 24 * 60 * 60 * 1000)
  return value.toISOString().slice(0, 10)
}

async function loadData() {
  const id = requireEmployee()
  if (!id) return
  consultants.value = await request('/mobile/consultants')
  appointments.value = await request(`/mobile/appointments?employeeId=${id}`)
}

async function book(item) {
  const id = requireEmployee()
  if (!id) return
  await request('/mobile/appointments', {
    method: 'POST',
    data: {
      employeeId: id,
      consultantId: item.id,
      appointmentTime: `${form.date} ${form.time}`,
      method: method.value,
      notes: form.notes || '小程序预约'
    }
  })
  uni.showToast({ title: '预约已提交' })
  form.notes = ''
  await loadData()
}

async function reschedule(item) {
  const id = requireEmployee()
  if (!id) return
  await request(`/mobile/appointments/${item.id}/reschedule`, {
    method: 'PUT',
    data: {
      employeeId: id,
      appointmentTime: `${form.date} ${form.time}`,
      method: method.value,
      notes: form.notes || item.notes || '小程序改约'
    }
  })
  uni.showToast({ title: '已改约' })
  await loadData()
}

async function cancel(item) {
  const id = requireEmployee()
  if (!id) return
  uni.showModal({
    title: '取消预约',
    content: `确认取消 ${item.consultant_name} 的咨询预约吗？`,
    success: async (res) => {
      if (!res.confirm) return
      await request(`/mobile/appointments/${item.id}/cancel`, {
        method: 'PUT',
        data: { employeeId: id }
      })
      uni.showToast({ title: '已取消' })
      await loadData()
    }
  })
}

onShow(loadData)
</script>

<style scoped>
.avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: #eef5ff;
  box-shadow: 0 8rpx 22rpx rgba(78, 97, 145, 0.1);
}

.picker {
  display: flex;
  align-items: center;
  margin: 22rpx 0 16rpx;
}

.textarea {
  width: 100%;
  min-height: 150rpx;
  padding: 18rpx;
  border: 1rpx solid #e4eaf7;
  border-radius: 12rpx;
  background: #fbfcff;
  box-sizing: border-box;
}

.booking-form {
  margin-top: 20rpx;
}

.tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14rpx;
  margin: 0 0 18rpx;
}

.tab {
  height: 66rpx;
  display: grid;
  place-items: center;
  border-radius: 12rpx;
  background: rgba(255, 255, 255, 0.94);
  border: 1rpx solid #edf1fb;
  color: #747b92;
  font-size: 24rpx;
  font-weight: 800;
}

.tab.active {
  background: linear-gradient(90deg, #3f72f5 0%, #9a5ce4 100%);
  color: #ffffff;
}

.appointment {
  display: block;
}

.appointment-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14rpx;
  margin-top: 18rpx;
}

.action-btn {
  height: 68rpx;
  font-size: 24rpx;
}

.danger {
  background: #fff0f0;
  color: #d64242;
  box-shadow: none;
}
</style>
