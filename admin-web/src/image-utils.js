const THEME_IMAGES = {
  default: '/theme/hero-care.png',
  admin_users: '/theme/profile.png',
  departments: '/theme/policy.png',
  employees: '/theme/profile.png',
  screenings: '/theme/screening.png',
  screening_questions: '/theme/screening.png',
  screening_reports: '/theme/report.png',
  intervention_plans: '/theme/report.png',
  courses: '/theme/course.png',
  course_progress: '/theme/course.png',
  consultants: '/theme/avatar-consultant.png',
  appointments: '/theme/consult.png',
  mood_logs: '/theme/mood.png',
  policies: '/theme/policy.png',
  system_settings: '/theme/policy.png',
  upload_files: '/theme/hero-care.png',
  avatar: '/theme/profile.png',
  employee: '/theme/profile.png',
  dept: '/theme/policy.png',
  screening: '/theme/screening.png',
  question: '/theme/screening.png',
  report: '/theme/report.png',
  intervention: '/theme/report.png',
  course: '/theme/course.png',
  progress: '/theme/course.png',
  consultant: '/theme/avatar-consultant.png',
  appointment: '/theme/consult.png',
  mood: '/theme/mood.png',
  policy: '/theme/policy.png',
  setting: '/theme/policy.png'
}

function themeByUrl(url) {
  const lower = String(url || '').toLowerCase()
  const key = Object.keys(THEME_IMAGES).find((item) => lower.includes(`${item}-`) || lower.includes(`/${item}`))
  return key ? THEME_IMAGES[key] : null
}

export function fallbackImage(table = 'default', column = '') {
  if (column === 'avatar_url') return THEME_IMAGES.avatar
  if (column === 'chart_image_url') return THEME_IMAGES.report
  return THEME_IMAGES[table] || THEME_IMAGES.default
}

export function displayImageUrl(url, table = 'default', column = '') {
  if (!url || typeof url !== 'string') {
    return fallbackImage(table, column)
  }
  if (url.includes('/deng/mock/') || url.includes('/mock/')) {
    return themeByUrl(url) || fallbackImage(table, column)
  }
  return url
}

export function handleImageError(event, table = 'default', column = '') {
  const img = event?.target
  if (!img) return
  const fallback = fallbackImage(table, column)
  if (!img.dataset.fallbackApplied) {
    img.dataset.fallbackApplied = 'true'
    img.src = fallback
  }
}
