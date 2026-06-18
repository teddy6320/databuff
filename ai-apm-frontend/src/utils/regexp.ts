import i18n from '@/i18n';
export const ipReg = new RegExp(/^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$/);
const netRegStr =
  '^(?:(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}' +
  '(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\/([1-9]|[1-2]\\d|3[0-2])$';
const networkSegmentReg = new RegExp(netRegStr);
export const ipRangeCheck = (rule: any, value: any, callback: any) => {
  // 中文检查
  const zhReg = new RegExp('[\\u4E00-\\u9FFF]+', 'g');
  if (zhReg.test(value)) {
    callback(new Error(i18n.t('modules.utils.regexp.ts.s_335cb4e2') as string));
  }
  // 英文检查
  const enReg = new RegExp('[a-zA-Z]+', 'g');
  if (enReg.test(value)) {
    callback(new Error(i18n.t('modules.utils.regexp.ts.s_88aacd96') as string));
  }
  // 中文全角逗号检查
  const dotReg = new RegExp(/\，/, 'g');
  if (dotReg.test(value)) {
    callback(new Error(i18n.t('modules.utils.regexp.ts.s_543c051a') as string));
  }
  // 非空检查
  if (!value && rule.field !== 'white_ip') {
    callback(new Error(i18n.t('modules.utils.regexp.ts.s_f6c73adf') as string));
  }
  const trimValue = value.replace(/\s+/g, '');
  const ips = trimValue.split(',').filter((item: any) => item);
  // const emptys = ips.filter(item => !item)
  // if (emptys.length > 0) {
  //   callback(new Error(i18n.t('modules.utils.regexp.ts.s_7c214531') as string))
  // }
  for (let i = 0, len = ips.length; i < len; i++) {
    if (ips[i].indexOf('/') !== -1) {
      // ip网段校验
      if (!networkSegmentReg.test(ips[i])) {
        callback(new Error(i18n.t('modules.utils.regexp.ts.s_f27a26f9') as string));
      }
    } else if (ips[i].indexOf('-') !== -1) {
      const [startIp, endIp] = ips[i].split('-');
      if (!ipReg.test(startIp) || !ipReg.test(endIp)) {
        callback(new Error(i18n.t('modules.utils.regexp.ts.s_ae1f1b54') as string));
      }
    } else {
      if (!ipReg.test(ips[i])) {
        callback(new Error(i18n.t('modules.utils.regexp.ts.s_a77b4d5d') as string));
      }
    }
  }
  callback();
};


// 标点符号和空格校验
export const characterReg = new RegExp(/[#\s\\\[\]\{\}!,，。！？；<>%^&*?]/)

export const htmlTagReg = new RegExp(/<(\w+)[^>]*>(.*?<\/\1>)?/)

export const htmlAnnotaionReg = new RegExp(/<!--[\s\S]*?-->/g)

export const xssRegTest = (params: any, keys?: []) => {
  let result = true;
  const paramsType = Object.prototype.toString.call(params)
  switch (paramsType) {
    case '[object Object]':
      const _keys = keys || Object.keys(params)
      _keys.forEach((key) => {
        const keyValue = params[key]
        if (Object.prototype.hasOwnProperty.call(params, key) && typeof keyValue === 'string') {
          const testResult = !(htmlAnnotaionReg.test(keyValue))
          if (!testResult) {
            result = false
          }
        }
      })
      break;
    case '[object Array]':
      if (params && params.length) {
        params.forEach((value: any) => {
          if (typeof value === 'string') {
            const testResult = !(htmlAnnotaionReg.test(value))
            if (!testResult) {
              result = false
            }
          }
        })
      }
      break;
    case '[object String]':
      if (params) {
        const testResult = !(htmlAnnotaionReg.test(params))
        if (!testResult) {
          result = false
        }
      }
      break;
  }
  return result
}

// 中文、字母、数字、下划线和横杠 {4, 100}
export const serviceNameReg = new RegExp(/^(?:[\u3400-\u4DB5\u4E00-\u9FEA\uFA0E\uFA0F\uFA11\uFA13\uFA14\uFA1F\uFA21\uFA23\uFA24\uFA27-\uFA29]|[\uD840-\uD868\uD86A-\uD86C\uD86F-\uD872\uD874-\uD879][\uDC00-\uDFFF]|\uD869[\uDC00-\uDED6\uDF00-\uDFFF]|\uD86D[\uDC00-\uDF34\uDF40-\uDFFF]|\uD86E[\uDC00-\uDC1D\uDC20-\uDFFF]|\uD873[\uDC00-\uDEA1\uDEB0-\uDFFF]|\uD87A[\uDC00-\uDFE0]|[a-zA-Z0-9_\-]){4,100}$/);
// 中文、字母、数字、下划线、横杠、@、|、#、&、小括号、中括号 {4, 100}
export const serviceNameNewReg = new RegExp(/^(?:[\u3400-\u4DB5\u4E00-\u9FEA\uFA0E\uFA0F\uFA11\uFA13\uFA14\uFA1F\uFA21\uFA23\uFA24\uFA27-\uFA29]|[\uD840-\uD868\uD86A-\uD86C\uD86F-\uD872\uD874-\uD879][\uDC00-\uDFFF]|\uD869[\uDC00-\uDED6\uDF00-\uDFFF]|\uD86D[\uDC00-\uDF34\uDF40-\uDFFF]|\uD86E[\uDC00-\uDC1D\uDC20-\uDFFF]|\uD873[\uDC00-\uDEA1\uDEB0-\uDFFF]|\uD87A[\uDC00-\uDFE0]|[a-zA-Z0-9_\-\@\|\｜\#\&\(\)\[\]]){4,100}$/);
// 中文、字母、数字、下划线、横杠、@、|、# {4, 100}
export const serviceNameNewReg2 = new RegExp(/^(?:[\u3400-\u4DB5\u4E00-\u9FEA\uFA0E\uFA0F\uFA11\uFA13\uFA14\uFA1F\uFA21\uFA23\uFA24\uFA27-\uFA29]|[\uD840-\uD868\uD86A-\uD86C\uD86F-\uD872\uD874-\uD879][\uDC00-\uDFFF]|\uD869[\uDC00-\uDED6\uDF00-\uDFFF]|\uD86D[\uDC00-\uDF34\uDF40-\uDFFF]|\uD86E[\uDC00-\uDC1D\uDC20-\uDFFF]|\uD873[\uDC00-\uDEA1\uDEB0-\uDFFF]|\uD87A[\uDC00-\uDFE0]|[a-zA-Z0-9_\-\@\|\｜\#]){4,100}$/);

export const LensVersionReg = new RegExp(/^[ulh](\d+\.){2}\d+$/);
