interface VersionObj {
  key: string;
  [key: string]: any
}

/**
 * 版本比较
 * @param {va} 版本a  2.7.12
 * @param {vb} 版本b  2.7.8.1
 * @return {number} 大于0 表示版本a大于版本b, 等于0 表示版本相同, 小于0 表示版本a小于版本b
 */
export const compareVersion = (va: string|null, vb: string|null) => {
  const arr1 = (va || '').split('.');
  const arr2 = (vb || '').split('.');
  let i = 0;
  while (true) {
    const s1 = arr1[i];
    const s2 = arr2[i];
    i++;
    if (s1 === undefined || s2 === undefined) {
      return arr1.length - arr2.length;
    }
    if (s1 === s2) {
      continue;
    }
    return Number(s1) - Number(s2);
  }
}

/**
 * 版本列表从小到大排序
 * @param {versions} 版本String列表 ['2.7.12', '2.7.8.1']
 * @return {sortedVersions} 从小到大排序后的版本列表
 */
export const sortVersion = (versions: string[]) => {
  return versions.sort((a, b) => {
    const arr1 = a.split('.');
    const arr2 = b.split('.');
    let i = 0;
    while (true) {
      const s1 = arr1[i];
      const s2 = arr2[i];
      i++;
      if (s1 === undefined || s2 === undefined) {
        return arr1.length - arr2.length;
      }
      if (s1 === s2) {
        continue;
      }
      return Number(s1) - Number(s2);
    }
  })
}

/**
 * 版本列表从小到大排序
 * @param {versionObjs} 版本Object列表
 * @param {sortKey} 版本字段名
 * @return {sortedVersions} 从小到大排序后的版本列表
 */
export const sortVersionObj = (versionObjs: VersionObj[], sortKey?: string) => {
  return versionObjs.sort((a, b) => {
    const arr1 = sortKey ? (a[sortKey] || '').split('.') : (a.key || '').split('.');
    const arr2 = sortKey ? (b[sortKey] || '').split('.') : (b.key || '').split('.');
    let i = 0;
    while (true) {
      const s1 = arr1[i];
      const s2 = arr2[i];
      i++;
      if (s1 === undefined || s2 === undefined) {
        return arr1.length - arr2.length;
      }
      if (s1 === s2) {
        continue;
      }
      return Number(s1) - Number(s2);
    }
  })
}
