<h2>简介：</h2>
Android瀑布流组件，效果和Pinterest、美丽说等类似。<BR/>

<h2>已具备特性：</h2>
支持列数自定义，默认3列；<BR/>
支持添加Header；<BR/>
支持设置Item点击事件监听和滚动事件监听；<BR/>
支持Item自动回收；<BR/>
支持下拉刷新和自动加载更多。<BR/>

<h2>暂不支持特性：</h2>
不支持切换屏幕后自动变换列数、重新排列页面内所有可见Item（不是全部刷新，效果见美丽说iPad版）;<BR/>
不支持像ListView、GridView等传统AdapterView一样通过更新数据自动刷新View。<BR/>

<h2>使用注意：</h2>
1.本项目中的下拉刷新特性引用了<a target="_blank" href="https://github.com/RincLiu/roid-lib-rinc">roid-lib-rinc</a>项目中的<a target="_blank" href="https://github.com/RincLiu/roid-lib-rinc/blob/master/src/com/rincliu/library/widget/view/pulltorefresh/PullToRefreshScrollView.java">PullToRefreshScrollView</a>，需要将该项目作为Library引用进来，当然也可以只将相关代码拷贝进来；<BR/>
2.如果自定义列数或控制ScrollBar是否显示，需要通过XML布局，并且只能包含WaterfallView一个元素（即不能嵌套在任何其他组件或布局内，PullToRefreshScrollView的基类及其派生类都不允许指定parent），然后设置相关属性值。<BR/>
