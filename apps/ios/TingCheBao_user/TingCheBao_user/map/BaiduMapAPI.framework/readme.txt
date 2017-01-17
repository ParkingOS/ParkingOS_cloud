百度地图iOS SDK自v2.3.0起，采用可定制的形式为您提供开发包，当前开发包包含如下功能：
--------------------------------------------------------------------------------------
基础地图：包括基本矢量地图、卫星图、实时路况图和各种地图覆盖物，此外还包括各种与地图相关的操作和事件监听；
检索功能：包括POI检索，公交信息查询，路线规划，地理编码/反地理编码，在线建议查询，短串分享等；
LBS云检索：包括LBS云检索（周边、区域、城市内、详情）；
定位功能：获取当前位置信息；
计算工具：包括测距（两点之间距离）、坐标转换、调起百度地图等功能；
周边雷达：包含位置信息上传和检索周边相同应用的用户位置信息功能；


--------------------------------------------------------------------------------------
地图SDK功能介绍（全功能开发包）：

地图：提供地图展示和地图操作功能；
POI检索：支持周边检索、区域检索和城市内兴趣点检索；
地理编码：提供经纬度和地址信息相互转化的功能接口；
线路规划：支持公交、驾车、步行三种方式的线路规划；
覆盖物图层：支持在地图上添加覆盖物（标注、几何图形、热力图、地形图图层等），展示更丰富的LBS信息；
定位：获取当前位置信息，并在地图上展示（支持普通、跟随、罗盘三种模式）；
离线地图：使用离线地图可节省用户流量，提供更好的地图展示效果；
调启百度地图：利用SDK接口，直接在本地打开百度地图客户端或WebApp，实现地图功能；
周边雷达：利用周边雷达功能，开发者可在App内低成本、快速实现查找周边使用相同App的用户位置的功能；
LBS云检索：支持查询存储在LBS云内的自有数据；
特色功能：提供短串分享、Place详情检索、热力图等特色功能，帮助开发者搭建功能更加强大的应用；

--------------------------------------------------------------------------------------
较之v2.7.0，升级功能：

注：百度地图iOS SDK向广大开发者提供了配置更简单的 .framework形式的开发包，请开发者选择此种类型的开发包使用。自下个版本起，百度地图iOS SDK将不再提供 .a形式的开发包。

【 新  增 】
    周边雷达
	利用周边雷达功能，开发者可在App内低成本、快速实现查找周边使用相同App的用户位置的功能。
	    新增周边雷达管理类：BMKRadarManager
	    新增周边雷达protocol：BMKRadarManagerDelegate
	    1.提供单次位置信息上传功能；
		- (BOOL)uploadInfoRequest:(BMKRadarUploadInfo*) info;
	    2.提供位置信息连续自动上传功能；
		- (void)startAutoUpload:(NSTimeInterval) interval;//启动自动上传用户位置信息
		- (void)stopAutoUpload;//停止自动上传用户位置信息
	    3.提供周边位置信息检索功能；
		- (BOOL)getRadarNearbySearchRequest:(BMKRadarNearbySearchOption*) option;
	    4.提供清除我的位置信息功能
		- (BOOL)clearMyInfoRequest;
    基础地图
	1.新增折线多段颜色绘制能力；
	    1）BMKPolyline中新增接口：
		///纹理索引数组
		@property (nonatomic, strong) NSArray *textureIndex;
		/**
 		*分段纹理绘制，根据指定坐标点生成一段折线
 		*@param points 指定的直角坐标点数组
 		*@param count 坐标点的个数
 		*@paramtextureIndex纹理索引数组，成员为NSNumber,且为非负数，负数按0处理
 		*@return新生成的折线对象
 		*/
		+ (BMKPolyline *)polylineWithPoints:(BMKMapPoint *)points count:(NSUInteger)count textureIndex:(NSArray*) textureIndex;

		/**
 		*根据指定坐标点生成一段折线
		*@paramcoords指定的经纬度坐标点数组
		*@param count 坐标点的个数
		*@paramtextureIndex纹理索引数组，成员为NSNumber,且为非负数，负数按0处理
		*@return新生成的折线对象
		*/
		+ (BMKPolyline *)polylineWithCoordinates:(CLLocationCoordinate2D *)coords count:(NSUInteger)count textureIndex:(NSArray*) textureIndex;
	    2）BMKPolylineView新增接口
		/// 是否分段纹理绘制（突出显示）
		@property (nonatomic, assign) BOOL isFocus;
	2.可以修改BMKPolyline、BMKPolygon、BMKCircle、BMKArcline的端点数据了
	3.新增地图强制刷新功能：
	    BMKMapView新增接口：
		- (void)mapForceRefresh;//强制刷新mapview
    检索功能
	1.在线建议检索结果开放POI经纬度及UID信息；
	    BMKSuggestionResult新增接口：
		///poiId列表，成员是NSString
		@property (nonatomic, strong) NSArray* poiIdList;
		///pt列表，成员是：封装成NSValue的CLLocationCoordinate2D
		@property (nonatomic, strong) NSArray* ptList;
	2.更新检索状态码
	    BMKSearchErrorCode中新增：
		BMK_SEARCH_NETWOKR_ERROR,///网络连接错误
		BMK_SEARCH_NETWOKR_TIMEOUT,///网络连接超时
		BMK_SEARCH_PERMISSION_UNFINISHED,///还未完成鉴权，请在鉴权通过后重试
    计算工具
	1.新增调启百度地图客户端功能；
	    1）调起百度地图客户端 – poi调起
		新增调起百度地图poi管理类：BMKOpenPoi
		/**
		* 调起百度地图poi详情页面
		*@param option poi详情参数类（BMKOpenPoiDetailOption）
		*@return调起结果
		*/
		+ (BMKOpenErrorCode)openBaiduMapPoiDetailPage:(BMKOpenPoiDetailOption *) option;

		/**
		* 调起百度地图poi周边检索页面
		*@param option poi周边参数类（BMKOpenPoiNearbyOption）
		*@return调起结果
		*/
		+ (BMKOpenErrorCode)openBaiduMapPoiNearbySearch:(BMKOpenPoiNearbyOption *) option;
	    2）调起百度地图客户端 – 路线调起
		新增调起百度地图路线管理类类：BMKOpenRoute
		/**
		* 调起百度地图步行路线页面
		*@param option 步行路线参数类（BMKOpenWalkingRouteOption）
		*@return调起结果
		*/
		+ (BMKOpenErrorCode)openBaiduMapWalkingRoute:(BMKOpenWalkingRouteOption *) option;

		/**
		* 调起百度地图公交路线页面
		*@param option 公交路线参数类（BMKOpenTransitRouteOption）
		*@return调起结果
		*/
		+ (BMKOpenErrorCode)openBaiduMapTransitRoute:(BMKOpenTransitRouteOption *) option;

		/**
		* 调起百度地图驾车路线检索页面
		*@param option 驾车路线参数类（BMKOpenDrivingRouteOption）
		*@return调起结果
		*/
		+ (BMKOpenErrorCode)openBaiduMapDrivingRoute:(BMKOpenDrivingRouteOption *) option;
	2.新增本地收藏夹功能；
	    新增收藏点信息类：BMKFavPoiInfo
	    新增收藏点管理类：BMKFavPoiManager
	    新增接口：
		/**
		* 添加一个poi点
		* @paramfavPoiInfo点信息,in/out，输出包含favId和添加时间
		* @return -2:收藏夹已满，-1:重名或名称为空，0：添加失败，1：添加成功
		*/
		- (NSInteger)addFavPoi:(BMKFavPoiInfo*) favPoiInfo;

		/**
		* 获取一个收藏点信息
		* @paramfavId添加时返回的favId，也可通过getAllFavPois获取的信息中BMKFavPoiInfo的属性favId
		* @return收藏点信息,没有返回nil
		*/
		- (BMKFavPoiInfo*)getFavPoi:(NSString*) favId;

		/**
		* 获取所有收藏点信息
		* @return点信息数组
		*/
		- (NSArray*)getAllFavPois;

		/**
		* 更新一个收藏点
		* @paramfavId添加时返回的favId，也可通过getAllFavPois获取的信息中BMKFavPoiInfo的属性favId
		* @paramfavPoiInfo点信息,in/out，输出包含修改时间
		* @return成功返回YES，失败返回NO
		*/
		- (BOOL)updateFavPoi:(NSString*) favIdfavPoiInfo:(BMKFavPoiInfo*) favPoiInfo;

		/**
		* 删除一个收藏点
		* @paramfavId添加时返回的favId，也可通过getAllFavPois获取的信息中BMKFavPoiInfo的属性favId
		* @return成功返回YES，失败返回NO
		*/
		- (BOOL)deleteFavPoi:(NSString*) favId;

		/**
		* 清空所有收藏点
		* @return成功返回YES，失败返回NO
		*/
		- (BOOL)clearAllFavPois;
【 修  复 】
    1、修复setMinLevel、setMaxLevel生效的是整型的问题；
    2、修复setRegion精准度不高的问题；
    3、修复POI检索结果，pageNum不正确的问题；
    4、修复定位结果海拔始终为0的问题；
    5、修复反地理编码检索在特定情况下，收不到回调的问题；