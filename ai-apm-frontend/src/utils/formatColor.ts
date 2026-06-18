// 是否为3位或6位十六进制颜色
export const isHexColor = (color: string) => {
  return /^#[0-9a-fA-F]{3}([0-9a-fA-F]{3})?$/.test(color)
}

// 十六进制颜色加透明度
export const formatToHexOpacity = (color: string, opacity = 1) => {
  if (/^#[0-9a-fA-F]{3}$/.test(color)) {
    color = color.replace(/#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])/, '#$1$1$2$2$3$3')
  }
  if (/^#[0-9a-fA-F]{6}$/.test(color) && opacity < 1) {
    opacity = opacity <= 0 ? 0 : opacity
    let hexOpacity = Math.round(opacity * 255).toString(16)
    hexOpacity = hexOpacity.length === 1 ? `0${hexOpacity}` : hexOpacity
    return `${color}${hexOpacity}`
  } else {
    return color
  }
}
