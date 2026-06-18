import fonts from './db-iconfont.json';

const icons = fonts.glyphs.map((icon: any) => {
  return {
    name: icon.name,
    alias: icon.alias || [],
    unicode: String.fromCodePoint(icon.unicode_decimal), // `\\u${icon.unicode}`,
  };
});

export const getDbIcon = (type: string, _default: string = 'default') => {
  type = String(type || '').toLocaleLowerCase();
  _default = String(_default || '').toLocaleLowerCase() || 'default';
  const matchIcon = icons.find((icon) => icon.alias.includes(type));
  if (matchIcon) {
    return matchIcon.unicode;
  }
  const defaultIcon = icons.find((icon) => icon.alias.includes(_default));
  if (defaultIcon) {
    return defaultIcon.unicode;
  }
  return '\ue61e';
};
