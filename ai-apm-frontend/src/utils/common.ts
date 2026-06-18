import Vue from 'vue';
import { Notification } from 'element-ui';

/**
 * Copy
 */
export const copy = (value: any, message?: string) => {
  const input = document.createElement('textarea');
  input.value = value;
  document.body.appendChild(input);
  input.select();
  if (document.execCommand('Copy')) {
    document.execCommand('Copy');
  }
  input.remove();
  Notification({
    title: '',
    message: message || '已复制！',
    duration: 1000,
    showClose: false,
    customClass: 'notification-copy success',
  });
};

/**
 * debounce
 */
export function debounce (this: any, func: () => void, wait: number, immediate?: boolean) {
  let timeout: any
  let args: any
  let context: any
  let timestamp: any
  let result: any

  const later = () => {
    // 据上一次触发时间间隔
    const last = +new Date() - timestamp

    // 上次被包装函数被调用时间间隔last小于设定时间间隔wait
    if (last < wait && last > 0) {
      timeout = setTimeout(later, wait - last)
    } else {
      timeout = null
      // 如果设定为immediate===true，因为开始边界已经调用过了此处无需调用
      if (!immediate) {
        result = func.apply(context, args)
        if (!timeout) {
          context = null
          args = []
        }
      }
    }
  }

  return ( ...params: any ) => {
    context = this
    timestamp = +new Date()
    const callNow = immediate && !timeout
    // 如果延时不存在，重新设定延时
    if (!timeout) {
      timeout = setTimeout(later, wait)
    }
    if (callNow) {
      result = func.apply(context, params)
      context = null
      params = null
    }

    return result
  }
}


/**
 * resetScreenSize
 */
export const resetScreenSize = (dw?: number, dh?: number) => {
  const init = () => {
    const _el = document.getElementById('app') as HTMLDivElement;
    const hScale = window.innerHeight / (dh || 1080);
    const wScale = window.innerWidth / (dw || 1920);
    // console.log(window.innerHeight, window.innerWidth)

    _el.style.transform = `scaleX(${wScale}) scaleY(${hScale})`;
    _el.style.overflowX = 'visible';
    _el.style.transformOrigin = 'left top';
    document.body.style.overflow = 'hidden';
  };

  let lazyFun: any = null;

  window.onresize = () => {
    clearTimeout(lazyFun);

    lazyFun = setTimeout(() => {
      init();
    }, 600);
  };

  init();
}

export const removeResetScreen = () => {
  const _el = document.getElementById('app') as HTMLDivElement;
  _el.style.transform = 'scaleX(1) scaleY(1)';
  _el.style.overflowX = 'auto';
  document.body.style.overflow = 'auto';

  window.onresize = null;
}

/**
 * StringIsEmpty
 */
export const StringIsEmpty = (unknown: any): boolean => {
  if (unknown === null || unknown === undefined || unknown === '') {
    return true
  } else {
    return false
  }
}
/**
 * MinNumZore
 */
export const MinNumZore = (unknown: any): number => {
  if (typeof unknown !== 'number' || unknown < 0) {
    return 0
  } else {
    return unknown
  }
}

/**
 * 异步方法处理
 * @param promise promise
 * @param dbApi 是否为接口api
 */
export const toAsyncWait = (promise: Promise<any>, dbApi: boolean = true) => {
  if (dbApi) {
    return promise.then((res) => {
      if (res.status === 200 && res.message.toLowerCase() === 'success') {
        return { error: null, result: res }
      } else {
        throw new Error(res.message)
      }
    }).catch((err) => ({ error: err, result: null }));
  } else {
    return promise.then((res) => ({ error: null, result: res })).catch((err) => ({ error: err, result: null }));
  }
}


/**
 * EventBus
 */
export const EventBus = new Vue();


/**
 * getTextWidth
 */
interface GTWOption {
  size?: number;
  family?: string;
}

export const getTextWidth: (text: string, option: GTWOption) => number = (text, options = {}) => {
  const { size = 14, family = 'Microsoft YaHei' } = options;
  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');
  ctx!.font = `${size}px ${family}`;
  const metrics = ctx!.measureText(text);
  return Math.abs(metrics.actualBoundingBoxLeft) + Math.abs(metrics.actualBoundingBoxRight);
}

/**
 * 按照A4纸张大小比例计算宽/高
 */
export const getA4Size = (width: number, height: number): { width: number, height: number } => {
  const a4Width = 794; // A4纸宽度，单位px
  const a4Height = 1123; // A4纸高度，单位px
  const aspectRatio = a4Width / a4Height;

  if (width) {
    // 宽度过大，按宽度缩放
    return { width, height: width / aspectRatio };
  } else if (height) {
    // 高度过大，按高度缩放
    return { width: height * aspectRatio, height };
  } else {
    // 高度过大，按高度缩放
    return { width: a4Width, height: a4Height };
  }
}

/**
 * 等待n秒
 */
export const waitForSomeSecond = (n: number, callback?: () => void): Promise<void> => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve();
      if (callback) {
        // console.log('waitForSomeSecond')
        callback();
      }
    }, n * 1000);
  });
}

/** Decode route query values that may have been encoded more than once. */
export function decodeRouteQuery (value: string | null | undefined): string {
  if (value == null || value === '') {
    return '';
  }
  let decoded = String(value);
  for (let i = 0; i < 3; i++) {
    try {
      const next = decodeURIComponent(decoded);
      if (next === decoded) {
        break;
      }
      decoded = next;
    } catch {
      break;
    }
  }
  return decoded;
}
