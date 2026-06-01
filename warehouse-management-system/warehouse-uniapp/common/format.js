export const STATUS_OPTIONS = ['全部', '待认领', '已认领', '已验收', '已上架', '已入库']

export function statusClass(status) {
  const map = {
    '待认领': 'status-waiting',
    '已认领': 'status-claimed',
    '已验收': 'status-accepted',
    '已上架': 'status-shelved',
    '已入库': 'status-stored'
  }
  return map[status] || ''
}

export function statusColor(status) {
  const map = {
    '待认领': '#F59E0B',
    '已认领': '#3B82F6',
    '已验收': '#8B5CF6',
    '已上架': '#06B6D4',
    '已入库': '#10B981'
  }
  return map[status] || '#94A3B8'
}

export function formatDateTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 19)
}

export function compactNumber(value) {
  if (value === null || value === undefined || value === '') return '0'
  const number = Number(value)
  if (Number.isNaN(number)) return String(value)
  return Number.isInteger(number) ? String(number) : String(Number(number.toFixed(3)))
}
