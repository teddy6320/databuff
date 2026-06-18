import G6, { IEdge } from '@antv/g6';
import { getDbIcon } from '@/assets/fonts/db-find-icon';

// 多行文本
export const multipleFittingString = (str: string, maxWidth: number, fontSize: number, maxLine: number) => {
  let currentWidth = 0;
  let splitIdx = 0;
  const splits: number[] = [];
  const pattern = new RegExp('[\u4E00-\u9FA5]+'); // distinguish the Chinese charactors and letters
  str.split('').forEach((letter, i) => {
    if (currentWidth > maxWidth) {
      splitIdx = i;
      splits.push(splitIdx);
      currentWidth = 0;
    };
    if (pattern.test(letter)) {
      // Chinese charactors
      currentWidth += fontSize;
    } else {
      // get the width of single letter according to the fontSize
      currentWidth += G6.Util.getLetterWidth(letter, fontSize);
    }
    
  });
  if (str && !splits.length) {
    splits.push(str.length);
  }
  let result: string[] = [];
  splits.forEach((idx, i) => {
    if (i === 0) {
      result.push(str.slice(0, idx));
    } else {
      result.push(str.slice(splits[i - 1], idx));
    }
    if (i === splits.length - 1) {
      result.push(str.slice(idx));
    }
  });
  result = result.filter( i => i);
  const rst = result.splice(0, maxLine);
  if (result.length > rst.length) {
    rst[rst.length - 1] = `${rst[rst.length - 1]}...`
  }
  return {
    text: rst.join('\n'),
    line: rst.length
  }
};

// 根据宽度截取字符串
export const getNameByWith = (name: string, width: number, fontsize: number, needLine: boolean = false) => {
  const scale = fontsize / 12
  const _width = width / scale
  const span = document.createElement('span')
  span.style.visibility = 'hidden'
  span.style.whiteSpace = 'nowrap'
  span.style.fontFamily = 'sans-serif'
  span.style.fontSize = '12px'
  document.body.appendChild(span)

  const getText = (str: string, reverse?: boolean, prefix?: string) => {
    let text = ''
    if (reverse) {
      str = str.split('').reverse().join('')
    }
    for (let j = 0; j < str.length; j++) {
      span.innerText = (prefix || '') + text + str[j]
      if (span.offsetWidth <= _width) {
        text += str[j]
      } else {
        break
      }
    }
    if (reverse) {
      text = text.split('').reverse().join('')
    }
    return (prefix || '') + text
  }

  let showName = getText(name)
  let line = 1;
  if (showName.length < name.length) {
    const _name = name.substring(showName.length)
    const _sn = getText(_name)
    if (_sn === _name) {
      showName = `${showName}\n${_sn}`
    } else {
      showName = `${showName}\n${getText(_name, true, '...')}`
    }
    line = 2;
  }
  document.body.removeChild(span)
  return needLine ? {
    line, showName
  } : showName
}

export const getIcon = getDbIcon;
