# SRecyclerView

RecyclerView重新封装,实现通用的数据适配器，上拉加载，下拉刷新和加载进度界面，空数据界面，异常界面等的实现

# 引入

## Step 1. Add the JitPack repository to your build file

* maven { url '<https://jitpack.io>' }

## Step 2. Add the dependency

* api 'com.github.shengwang520:SRecyclerView:1.0.28'

## Changelog

### Version:1.0.28

* 升级编译环境

### Version:1.0.27

* 优化第1页数据不足时，自动触发下一页数据请求
* 移除数据模型需要实现的接口唯一id方法，采用重新equals实现

### Version:1.0.26

* 优化数据刷新

### Version:1.0.25

* 优化数据刷新逻辑

### Version:1.0.24

* 优化下拉刷新逻辑
* 触发下拉刷新，需要在刷新结束时，手动添加停止刷新方法

### Version:1.0.21

* 优化阿拉伯语适配

### Version:1.0.20

* 新增卡片分割线适配阿拉伯语

### Version:1.0.19

* 优化卡片分割线

### Version:1.0.18

* 新增卡片分割线

### Version:1.0.17

* 修复删除数据偶现崩溃问题

### Version:1.0.16

* 优化数据刷新逻辑

### Version:1.0.15

* 优化数据新增逻辑显示没有数据

### Version:1.0.14

* 优化数据刷新逻辑

### Version:1.0.13

* 优化数据刷新时的比较逻辑

### Version:1.0.12

* 调整数据刷新判断逻辑方法

### Version:1.0.09

* 添加数据更新参数，确认数据刷新位置是否变动

### Version:1.0.08

* 开放方法权限

### Version:1.0.07

* 优化空数据刷新逻辑

### Version:1.0.06

* 优化空数据布局显示

### Version:1.0.05

* 优化空数据显示逻辑

### Version:1.0.04

* 修复空布局不显示问题

### Version:1.0.03

* 新增设置空数据布局方法

### Version:1.0.02

* 新增GridLayoutManager头部和底部布局显示效果
* 优化列表底部布局操作方法

### Version:1.0.01

* 首次发布
