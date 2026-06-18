import { getDbIcon } from '@/assets/fonts/db-find-icon';

/**
 * 首字母大写
 */
export const FirstLetterCapital = (string: string) => {
  string = (string || '').toString();
  return string.charAt(0).toUpperCase() + string.slice(1);
}

/**
 * 图标unicode
 * @param {name} 图标别名
 * @param {defaultIcon} 默认图标别名，name不存在时使用
 * @return {icon} 图标unicode
 */
export const DbIconFilter = (name: string, defaultIcon: string = 'default') => {
  return getDbIcon(name, defaultIcon);
}
