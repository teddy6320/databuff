# 许可证管理

> 页面: `/sysManage/license`
> 文件: `src/views/sysManage/license/index.vue`

## 页面职责

License 页用于查看当前授权信息，并上传新的 `.lic` 文件完成授权更新。

## 页面结构

- `License信息`: 产品名称、版本、序列号、授权状态、有效期
- `License更新`: 文件上传区

## 主要接口

- `getLicenseInfo`
- `logoutHandle`
- 上传通过 `el-upload action="/webapi/user/lisupload"` 直接提交

详细接口见:

- [User API](../../api/user.md)

## 关键行为

- 页面初始化时读取当前 License 信息
- 上传成功后会提示重新登录，并清理 token / cid 后刷新页面

## 注意事项

- 只接受 `.lic` 文件
- 上传成功会强制用户重新登录，这一点对运维操作影响较大
