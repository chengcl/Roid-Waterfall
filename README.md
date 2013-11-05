<h2>简介：</h2>
Android平台类似Pinterest瀑布流展示效果的组件，支持多列、分页和下拉刷新。<BR/>

<h2>已实现的特性：</h2>
1. 设置列数（默认3列）以及是否显示ScrollBar；<BR/>
2. 添加Header；<BR/>
3. 数据分页展示；<BR/>
4. 设置Item点击事件监听和滚动事件监听；<BR/>
5. 根据滚动状态自动回收和重载Item；<BR/>
6. 根据Activity生命周期手动回收和重载Item；<BR/>
7. 设置Item排列方式：默认（"Z"字排列）、高度最小的列优先；<BR/>
8. 下拉刷新和自动加载更多。<BR/>

<h2>未来将添加的特性：</h2>
1. 切换屏幕后自动变换列数、重新排列页面内所有可见Item（不是全部刷新，效果见美丽说iPad版）;<BR/>
2. 像ListView、GridView等传统AdapterView一样通过更新数据自动刷新View。<BR/>

<h2>使用注意：</h2>
1. 本项目中的下拉刷新特性引用了<a target="_blank" href="https://github.com/RincLiu/roid-lib-rinc">Roid-lib-Rinc</a>项目中的<a target="_blank" href="https://github.com/RincLiu/roid-lib-rinc/blob/master/src/com/rincliu/library/widget/view/pulltorefresh/PullToRefreshScrollView.java">PullToRefreshScrollView</a>及其他类，需要将该项目作为Library引用进来，当然也可以只将相关代码拷贝进来；<BR/>
2. 使用XML布局时，只能包含WaterfallView一个元素（即不能嵌套在任何其他组件或布局内，PullToRefreshScrollView的基类及其派生类都不允许指定parent）；<BR/>
3. 如果设置Item排列方式为SHORTEST_COLUMN_FIRST，则在初始化Item时，必须指定其View的LayoutParams，尤其是height，否则仍会按默认方式排列。<BR/>
