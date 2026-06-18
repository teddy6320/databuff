import * as types from './index.types';

// 格式化指标分类数据
export const formatMetricTypeData = (data: any[], attrs: string[] = []) => {
  type TreeNode = Record<string, any>
  const sortCompare = (a: string, b: string) => {
    if (a === '其他' || (a === '进程' && b === '主机')) {
      return 1
    } else if (b === '其他' || (a === '主机' && b === '进程')) {
      return -1
    }
    return a.localeCompare(b, 'zh-CN')
  }

  const needId = attrs.includes('id')
  const needLevel = attrs.includes('level')

  // 构建树形结构
  const tree = data.reduce((acc: TreeNode, item) => {
    const { type1, type2, type3 } = item || {}
    const updateNode = (obj: TreeNode, keys: string[]) => {
      return keys.reduce((nested, key, idx) => {
        if (!key) {
          return nested
        }
        if (idx === keys.length - 1) {
          nested[key] = key
        } else {
          nested[key] = { ...nested[key] }
        }
        return nested[key]
      }, obj)
    }

    updateNode(acc, [type1, type2, type3])
    return acc
  }, {})

  // 递归转换节点
  const transform = (node: TreeNode, level = 1, pValues: string[] = []): types.CascaderOptionItem[] => {
    return Object.entries(node)
      .sort(([a], [b]) => sortCompare(a, b))
      .map(([value, children]) => {
        const values = [...pValues, value]
        const item: types.CascaderOptionItem = {
          value,
          label: value,
          leaf: level === 3 || Object.keys(children).length === 0,
          children: level < 3 && Object.keys(children).length > 0
              ? transform(children, level + 1, values) : null
        }
        if (needId) {
          item.id = JSON.stringify({
            type1: values[0] || '',
            type2: values[1] || '',
            type3: values[2] || '',
          })
        }
        if (needLevel) {
          item.level = level
        }
        return item
      })
  }

  return transform(tree)
}
