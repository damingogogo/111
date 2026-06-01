function chineseIntegerToNumber(text) {
  const digitMap = {
    零: 0,
    〇: 0,
    一: 1,
    二: 2,
    两: 2,
    三: 3,
    四: 4,
    五: 5,
    六: 6,
    七: 7,
    八: 8,
    九: 9
  }
  const unitMap = {
    十: 10,
    百: 100,
    千: 1000,
    万: 10000
  }

  let total = 0
  let section = 0
  let number = 0
  for (const char of text) {
    if (digitMap[char] !== undefined) {
      number = digitMap[char]
      continue
    }
    const unit = unitMap[char]
    if (!unit) continue
    if (unit === 10000) {
      section = (section + number) * unit
      total += section
      section = 0
    } else {
      section += (number || 1) * unit
    }
    number = 0
  }
  return total + section + number
}

export function parseNumberFromText(text) {
  if (!text) return null
  const cleaned = String(text).replace(/\s+/g, '')
  const digitalText = cleaned.replace(/负/g, '-').replace(/点/g, '.')
  const digital = digitalText.match(/-?\d+(\.\d+)?/)
  if (digital) return Number(digital[0])

  const chinese = cleaned.match(/负?[零〇一二两三四五六七八九十百千万点]+/)
  if (!chinese) return null

  const value = chinese[0]
  const negative = value.indexOf('负') === 0
  const pure = value.replace('负', '').replace(/点/g, '.')
  const parts = pure.split('.')
  const integer = chineseIntegerToNumber(parts[0])
  let result = integer
  if (parts[1]) {
    const digitText = parts[1]
    const decimal = digitText
      .split('')
      .map(char => chineseIntegerToNumber(char))
      .join('')
    result = Number(`${integer}.${decimal}`)
  }
  return negative ? -result : result
}

export function startVoiceNumber(label) {
  return new Promise((resolve, reject) => {
    // #ifdef APP-PLUS
    if (typeof plus !== 'undefined' && plus.speech && plus.speech.startRecognize) {
      uni.showToast({ title: `请说出${label || '数字'}`, icon: 'none' })
      plus.speech.startRecognize(
        { punctuation: false },
        result => {
          const number = parseNumberFromText(result)
          if (number === null || Number.isNaN(number)) {
            uni.showToast({ title: '没有识别到数字', icon: 'none' })
            reject(new Error('没有识别到数字'))
            return
          }
          resolve(number)
        },
        error => {
          reject(error)
        }
      )
      return
    }
    // #endif

    uni.showToast({ title: '当前环境暂不支持语音输入', icon: 'none' })
    reject(new Error('当前环境暂不支持语音输入'))
  })
}

export const MODEL_VOICE_NOTE = '大模型识别接口预留在 common/voice.js，客户提供接口和费用后可替换 startVoiceNumber。'
