### 简介
Android平台类似Pinterest瀑布流展示效果的组件，支持多列和分页展示、预加载、下拉刷新。

### 已实现的特性
* 设置列数（默认3列）以及是否显示ScrollBar；
* 添加Header；
* 数据分页展示；
* 设置Item点击事件监听和滚动事件监听；
* 根据滚动状态自动回收和重载Item；
* 根据Activity生命周期手动回收和重载Item；
* 设置Item排列方式：默认（"Z"字排列）、高度最小的列优先；
* 下拉刷新和自动加载更多;
* 根据滚动方向，自动判断预加载方向；且预加载页数可设置：<BR/>
  offsetPageCount: 显示超出屏幕范围的页数（两个方向，始终有效，默认值1）；<BR/>
  preLoadPageCount：在上一步的基础上，根据滚动方向再次预加载的页数（仅在滚动停止后更新页面时有效，默认值1）；<BR/>

### 未来将添加的特性
* 切换屏幕后自动变换列数、重新排列页面内所有可见Item（不是全部刷新，效果见美丽说iPad版）;
* 像ListView、GridView等传统AdapterView一样通过更新数据自动刷新View。

### 使用注意
* 本项目中的下拉刷新特性引用了[Roid-lib-Rinc](https://github.com/RincLiu/roid-lib-rinc)项目中的[PullToRefreshScrollView](https://github.com/RincLiu/roid-lib-rinc/blob/master/src/com/rincliu/library/widget/view/pulltorefresh/PullToRefreshScrollView.java)及其他类，需要将该项目作为Library引用进来，当然也可以只将相关代码拷贝进来；
* 使用XML布局时，只能包含WaterfallView一个元素（即不能嵌套在任何其他组件或布局内，PullToRefreshScrollView的基类及其派生类都不允许指定parent）；
* 如果设置Item排列方式为SHORTEST_COLUMN_FIRST，则在初始化Item时，必须指定其View的LayoutParams，尤其是height，否则仍会按默认方式排列。
