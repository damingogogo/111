const THEME_IMAGES = {
  default: '/static/theme/hero-care.png',
  screening: '/static/theme/screening.png',
  question: '/static/theme/screening.png',
  course: '/static/theme/course.png',
  progress: '/static/theme/course.png',
  avatar: '/static/theme/profile.png',
  employee: '/static/theme/profile.png',
  report: '/static/theme/report.png',
  intervention: '/static/theme/report.png',
  mood: '/static/theme/mood.png',
  policy: '/static/theme/policy.png',
  dept: '/static/theme/policy.png',
  setting: '/static/theme/policy.png'
}

const PLACEHOLDER = THEME_IMAGES.default

function themedImage(url) {
  const lower = String(url || '').toLowerCase()
  const matched = Object.keys(THEME_IMAGES).find((key) => lower.includes(`${key}-`) || lower.includes(`/${key}`))
  return matched ? THEME_IMAGES[matched] : PLACEHOLDER
}

export function imageUrl(url) {
  if (!url || typeof url !== 'string') {
    return PLACEHOLDER
  }
  if (url.startsWith('http://')) {
    return themedImage(url)
  }
  return url
}

export function usePlaceholder(target, key) {
  if (target && key) {
    target[key] = themedImage(target[key])
  }
}

export function themeImage(type = 'default') {
  return THEME_IMAGES[type] || PLACEHOLDER
}
