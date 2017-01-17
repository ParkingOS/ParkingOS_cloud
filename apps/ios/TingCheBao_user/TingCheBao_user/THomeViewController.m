//
//  THomeViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-8-19.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//
#import "NetworkView.h"
#import "THomeViewController.h"
#import "TTicketHelpController.h"
#import <BaiduMapAPI/BMKMapView.h>
#import <BaiduMapAPI/BMKPointAnnotation.h>
#import <BaiduMapAPI/BMKPinAnnotationView.h>
#import "MyAnnotation.h"
#import <BaiduMapAPI/BMKSuggestionSearch.h>
#import <BaiduMapAPI/BMKPoiSearch.h>
#import <BaiduMapAPI/BMKRouteSearch.h>
#import "CVAPIRequestModel.h"
#import "TParkItem.h"
#import "MyAnnotation.h"
#import "TAPIUtility.h"

#import "TPayCollectorViewController.h"
#import "TRechargeViewController.h"

#import "BNCoreServices.h"
#import "TLoginViewController.h"

#import "TAppDelegate.h"
#import "TRechargeWaysViewController.h"
#import "TPush.h"
#import "TTicketItem.h"
#import "TAccountItem.h"
#import "TAlipay.h"
#import "TWeixin.h"
#import "TMyMonthViewController.h"
#import "TAccountViewController.h"
#import "TCurrentOrderViewController.h"
#import "TViewController.h"
#import "TVersionAlertView.h"
#import "TReaderViewController.h"
#import "TCurrentOrderItem.h"
#import "TCollectorsListViewController.h"
#import "WXApiObject.h"
#import "THolidayView.h"
#import "TShareView.h"
#import "UIImageView+WebCache.h"
#import "TIbeacon.h"
#import <TencentOpenAPI/TencentOAuth.h>
#import <TencentOpenAPI/QQApi.h>
#import <TencentOpenAPI/QQApiInterface.h>
#import "MobClick.h"
#import "TSession.h"
#import "TParkInfoView.h"
#import "TSearchView.h"
#import "TCurrentOrderView.h"
#import "TParkDetailController.h"
#import "TUploadParkViewController.h"
#import "TShareRedPackageView.h"
#import "TPromptImageView.h"
#import <BaiduMapAPI/BMKGeocodeSearch.h>
#import "TPostCommentViewController.h"
#import "TLocationViewController.h"
#import "TLocationInfoViewController.h"
#import <MobileCoreServices/MobileCoreServices.h>
#import "TAccountItem.h"
#import "TRechargeViewController.h"
#import "TScanParkRedPackageView.h"
#import "TTicketViewController.h"
#import "TMyRedPackageViewController.h"
#import "TCollectorsListViewController.h"
#import "TTicketGameViewController.h"

#define annotation_height 65
#define annotation_width  50
#define bar_height        60
#define padding       4
#define zoom_lever    16

typedef enum {
    SearchModeHistory = 0,
    SearchModeResutl
}SearchMode;

@interface MyRootSearch: BMKRouteSearch

@property(nonatomic, retain)BMKDrivingRoutePlanOption* option;

@end

@implementation MyRootSearch


@end

@interface THomeViewController ()<BMKMapViewDelegate, BMKLocationServiceDelegate, UITableViewDataSource, UITableViewDelegate, UISearchBarDelegate, BMKSuggestionSearchDelegate, BMKPoiSearchDelegate, BNNaviRoutePlanDelegate, BNNaviUIManagerDelegate, UISearchDisplayDelegate, UIScrollViewDelegate, BMKRouteSearchDelegate, TAlipayDelegate, TWeixinDelegate, CVAPIModelDelegate, THolidayViewDelegate, TShareViewDelegate, TParkInfoViewDelegate, TSearchViewDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate, BMKGeoCodeSearchDelegate>

@property(nonatomic, retain) UIView* topBar;
@property(nonatomic, retain) UISearchBar* searchBar;
@property(nonatomic, retain) UIBarButtonItem* searchItem;
@property(nonatomic, retain) UIButton* cancelButton;
@property(nonatomic, retain) UITableView* searchTableView;
@property(nonatomic, assign) SearchMode searchMode;
@property(nonatomic, retain) BMKMapView* mapView;
@property(nonatomic, retain) TSearchView* bottomView;
@property(nonatomic, retain) BMKSuggestionSearch* suggestionSearch;
@property(nonatomic, retain) BMKSuggestionSearchOption* searchOption;
@property(nonatomic, retain) NSArray* searchResult;
@property(nonatomic, retain) NSArray* searchResultKeys;
@property(nonatomic, retain) NSArray* historyResult;
@property(nonatomic, retain) NSArray* historyResultKeys;
@property(nonatomic, retain) NSString* selectedKeyword;
@property(nonatomic, retain) BMKPointAnnotation* searchAnnotation;
@property(nonatomic, retain) BMKPointAnnotation* locationAnnotation;
@property(nonatomic, retain) BMKAnnotationView* locationAnnotationView;
@property(nonatomic, retain) BMKPointAnnotation* carAnnotation;
@property(nonatomic, retain) BMKAnnotationView* carAnnotationView;
@property(nonatomic, retain) BMKActionPaopaoView* locationPaopaoView;
@property(nonatomic, retain) BMKActionPaopaoView* infoPaopaoView;
@property(nonatomic, retain) BMKActionPaopaoView* carPaopaoView;

@property(nonatomic, retain) BMKPoiSearch* poiSearch;
@property(nonatomic, retain) BMKCitySearchOption* poiSearchOption;

@property(nonatomic, retain) UIButton* locationSelfButton;
@property(nonatomic, retain) UIButton* locationCarButton;
@property(nonatomic, retain) UIButton* priceButton;
@property(nonatomic, retain) UIButton* queryCurrOrder;
//用户的坐标
@property(nonatomic, assign) CLLocationCoordinate2D coordinate;
//请求周围车场的坐标
@property(nonatomic, assign) CLLocationCoordinate2D requestCoordinate;
//所有的车场信息
@property(nonatomic, retain) NSMutableArray* allParkItems;
@property(nonatomic, retain) NSMutableArray* payParkItems;
//请求时需要它 目前没用到
@property(nonatomic, retain) NSString* pearpareIDs;
@property(nonatomic, retain) BMKAnnotationView* selectedAnnotationView;
@property(nonatomic, retain) NSString* selectedParkId;

@property(nonatomic, retain) NSMutableArray* annotionaViews;

@property(nonatomic, assign) BOOL fisrtLocation;
@property(nonatomic, assign) BOOL firstChangeRegon;
@property(nonatomic, assign) BOOL firstLoad;

//支付请求返回前 需保留
@property(nonatomic, retain) NSString* orderPrice;
@property(nonatomic, retain) NSString* orderId;
//记录当前所在城市,默认北京
@property(nonatomic, retain) NSString* cityName;

@property(nonatomic, retain) THolidayView* holidayView;//节日

@property(nonatomic, retain) CVAPIRequestModel* currentOrderModel;
@property(nonatomic, retain) CVAPIRequestModel* requestAllParksModel;

@property(nonatomic, retain) CVAPIRequest* currentOrderReqeust;
@property(nonatomic, retain) CVAPIRequest* allParksReqeust;
@property(nonatomic, retain) CVAPIRequest* zhifuRequest;
@property(nonatomic, retain) CVAPIRequest* switchRequest;
@property(nonatomic, retain) CVAPIRequest* recommendRequest;
@property(nonatomic, retain) CVAPIRequest* regionReqeust;
@property(nonatomic, retain) CVAPIRequest* yueRequest;
@property(nonatomic, retain) CVAPIRequest* priceRequest;
@property(nonatomic, retain) CVAPIRequest* checkVersionRequest;
@property(nonatomic, retain) CVAPIRequest* checkHolidayRequest;
@property(nonatomic, retain) CVAPIRequest* checkAccountRequest;
@property(nonatomic, retain) CVAPIRequest* shareInfoRequest;
@property(nonatomic, retain) CVAPIRequest* yuEAndTicketsRequest;
@property(nonatomic, retain) CVAPIRequest* redPackageRequest;

//---------------------writeWithYang-------------------
@property(nonatomic, retain) CVAPIRequest* getDistanceRequest;
@property(nonatomic, retain) NSString* boundsId;//红包id
@property(nonatomic, assign) BOOL isLack;//周围车场是否非常少，由服务器返回
@property(nonatomic, retain) NSString* lackMoney;//信用欠费金额
@property(nonatomic, assign) BOOL onlyShowPayParks;

@property(nonatomic, readwrite) CBCentralManager *centralManager;

@property(nonatomic, readwrite) CLBeacon* beacon;
@property (nonatomic, readwrite) CLBeaconRegion *myBeaconRegion;
@property (nonatomic, readwrite) CLLocationManager *locationManager;

@property (nonatomic, readwrite) double oldTime;

@property(nonatomic, retain) AVAudioPlayer* audioPlayer;

@property(nonatomic, readwrite) NSTimer *timer;

//停车后照相
@property(nonatomic, retain) UIImagePickerController* locationImagePicker;


@end

@implementation THomeViewController

+ (THomeViewController*)share {
    static dispatch_once_t once;
    static THomeViewController *home;
    dispatch_once(&once, ^ { home = [[THomeViewController alloc] init];});
    return home;
}

- (id)init {
    if (self = [super init]) {
        _searchResult = [NSArray array];
        _searchResultKeys = [NSArray array];
        _historyResult = [NSArray array];
        _historyResultKeys = [NSArray array];
        _fisrtLocation = YES;
        _firstChangeRegon = YES;
        _firstLoad = YES;
        _allParkItems = [NSMutableArray array];
        _payParkItems = [NSMutableArray array];
        _pearpareIDs = @"";
        _annotionaViews = [NSMutableArray array];
        _isLack = NO;
        _onlyShowPayParks = YES;
        _cityName = @"北京";
        _checkCityName = YES;
        
        _suggestionSearch = [[BMKSuggestionSearch alloc] init];
        _suggestionSearch.delegate = self;
        
        _searchOption = [[BMKSuggestionSearchOption alloc] init];
        
        _poiSearch = [[BMKPoiSearch alloc] init];
        _poiSearch.delegate = self;
        
        _locationService = [[BMKLocationService alloc] init];
        _locationService.delegate = self;
        
        //++++++++++++++++++
        
        _searchTableView = [[UITableView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height)];
        _searchTableView.delegate = self;
        _searchTableView.dataSource = self;
        _searchTableView.hidden = YES;
        _searchTableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 1)];
        
        _mapView = [[BMKMapView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height)];
        _mapView.showsUserLocation = NO;
        _mapView.userTrackingMode = BMKUserTrackingModeNone;
        _mapView.maxZoomLevel = 19;
        _mapView.delegate = self;
        
        _bottomView = [[TSearchView alloc] initWithFrame:CGRectMake(0, self.view.height - 115, self.view.width, 215)];
        _bottomView.delegate = self;
        
        _locationSelfButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_locationSelfButton setImage:[UIImage imageNamed:@"location.png"] forState:UIControlStateNormal];
        [_locationSelfButton addTarget:self action:@selector(clickedLocationButton:) forControlEvents:UIControlEventTouchUpInside];
        _locationSelfButton.frame = CGRectMake(2, self.view.height - 140.5 - 40, 35, 35);
        
        _locationCarButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_locationCarButton setImage:[UIImage imageNamed:@"car_find.png"] forState:UIControlStateNormal];
        [_locationCarButton addTarget:self action:@selector(clickedLocationCarButton) forControlEvents:UIControlEventTouchUpInside];
        _locationCarButton.frame = CGRectMake(2, self.view.height - 140.5 - 2*40, 35, 35);
        
        _queryCurrOrder = [UIButton buttonWithType:UIButtonTypeCustom];
        [_queryCurrOrder setBackgroundImage:[UIImage imageNamed:@"search_order_green.png"] forState:UIControlStateNormal];
//        _queryCurrOrder.alpha = 1.0;
//        _queryCurrOrder.layer.cornerRadius = 5;
//        _queryCurrOrder.clipsToBounds = YES;
        [_queryCurrOrder addTarget:self action:@selector(clickedQueryCurrOrderButton) forControlEvents:UIControlEventTouchUpInside];
        _queryCurrOrder.frame = CGRectMake(_mapView.right - 62, self.view.height - 140.5 - 65, 55, 55);
        
//---------------------writeWithYang-------------------
        self.locationManager = [[CLLocationManager alloc] init];
        self.locationManager.delegate = self;
//        [self.locationManager setDesiredAccuracy: kCLLocationAccuracyBest];
        
        if ([_locationManager respondsToSelector:@selector(requestAlwaysAuthorization)]) {
            //必须是永远定位，否则后台时 无法检查ibeacon
            [_locationManager requestAlwaysAuthorization];
        }
        
//        self.centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
        
        NSUUID *uuid = [[NSUUID alloc] initWithUUIDString:@"FDA50693-A4E2-4FB1-AFCF-C6EB07647825"];
        self.myBeaconRegion = [[CLBeaconRegion alloc] initWithProximityUUID:uuid identifier:@"com.mybeacon.region"];
        
        [[NSUserDefaults standardUserDefaults] removeObjectForKey:save_Coor];
        [[NSUserDefaults standardUserDefaults] removeObjectForKey:save_disatuce];
        
//---------------------writeWithYang-------------------
        _holidayView = [[THolidayView alloc] initWithFrame:self.view.frame];
        _holidayView.delegate = self;
        _holidayView.hidden = YES;
        
        [self.view addSubview:_mapView];
        [self.view addSubview:_bottomView];
        [self.view addSubview:_searchTableView];
        [self.view addSubview:_locationSelfButton];
        [self.view addSubview:_locationCarButton];
        [self.view addSubview:_priceButton];
        [self.view addSubview:_queryCurrOrder];
        
        [self.view addSubview:_holidayView];

//---------------------writeWithYang-------------------
        [self.view addSubview:[TShakeView getInstance]];
        [TShakeView getInstance].hidden = YES;
//---------------------writeWithYang-------------------
        //每一个要检查版本 延迟5秒 以防地图页面卡死
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, NSEC_PER_SEC * 5),
            dispatch_get_main_queue(), ^{
                [self checkVersion];
                //检查holiday
                [self checkHoliday];
                //查检是否打开通知
                [TAPIUtility checkNotificationSetting];
        });
    }
    return self;
}

//---------------------writeWithYang-------------------
#pragma mark -
#pragma mark yaoyiyao
- (BOOL)canBecomeFirstResponder
{
    return YES;// default is NO
}
- (void)motionBegan:(UIEventSubtype)motion withEvent:(UIEvent *)event
{
    NSLog(@"开始摇动手机");
    if ([[NSUserDefaults standardUserDefaults] objectForKey:save_phone]&&[[TAPIUtility getDistance] floatValue] < 200) {
        [self playSound];
    }
}
- (void)motionEnded:(UIEventSubtype)motion withEvent:(UIEvent *)event
{
    NSLog(@"stop");
//    if ([[TShakeView getInstance] start:![[_resultDict objectForKey:@"inout"] boolValue]]) {
    if ([[TAPIUtility getDistance] floatValue] > 200) {
        
    }else if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        
    }else if(self.centralManager.state == CBCentralManagerStatePoweredOff){
        
        [self alertShow];

    }else if (_beacon) {
        
            [[TShakeView getInstance] moso];
        
            [self sendRequestIncomeOrOutParket:^(NSDictionary *dict){
                if ([[dict objectForKey:@"inout"] integerValue] == 0 &&[[TShakeView getInstance] start:YES]) {
                    
                    [self sendRequest:@"addorder" withDict:dict];
                    
                }else if ([[dict objectForKey:@"inout"] integerValue] == 1&&[[TShakeView getInstance] start:NO]){
                    
                    [self sendRequest:@"doorder" withDict:dict];
                    
                }else if ([[dict objectForKey:@"inout"] integerValue] == 2){
                    
                    if ([[dict objectForKey:@"orderid"] intValue] > 0) {
                        [[TShakeView getInstance] start:NO];
                        [self sendRequest:@"doorder" withDict:dict];
                    }else{
                        [[TShakeView getInstance] start:YES];
                        [self sendRequest:@"addorder" withDict:dict];
                    }
                }
            } withMoto:YES];
        }else{
            [[TShakeView getInstance] fail];
        }
//    }
}
- (void)motionCancelled:(UIEventSubtype)motion withEvent:(UIEvent *)event
{
    NSLog(@"取消");
}


- (void)sendRequest:(NSString *)income withDict:(NSDictionary *)dict{
    //    NSLog(@"send---------");
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        return;
    }
    NSString *urlString = nil;
    if ([[dict objectForKey:@"orderid"] intValue] > 0) {
        urlString = [NSString stringWithFormat:@"ibeaconhandle.do?major=%@&minor=%@&action=%@&mobile=%@&uid=%@&orderid=%@", _beacon.major, _beacon.minor,income, [[NSUserDefaults standardUserDefaults] objectForKey:save_phone],[dict objectForKey:@"uid"],[dict objectForKey:@"orderid"]];
    }else{
        urlString = [NSString stringWithFormat:@"ibeaconhandle.do?major=%@&minor=%@&action=%@&mobile=%@&uid=%@", _beacon.major, _beacon.minor,income, [[NSUserDefaults standardUserDefaults] objectForKey:save_phone],[dict objectForKey:@"uid"]];
    }
    
    CVAPIRequest* request = [[CVAPIRequest alloc] initWithAPIPath:urlString timeout:6.00];
    CVAPIRequestModel* model = [[CVAPIRequestModel alloc] init];
    model.delegate = self;
//    model.hideNetworkView = YES;
    [model sendRequest:request completion:^(NSDictionary *result, NSError *error) {
//        {"result":"0","info":""}
//        result 0失败，1成功 ,2已结算过
        if (result) {
            NSString* state = [result objectForKey:@"result"];
            if ([state integerValue] == 1) {
                
                if ([[dict objectForKey:@"orderid"] integerValue] == 0) {
                    [[TShakeView getInstance] stop];
                    [self clickedQueryCurrOrderButton];
                }else{
                    [_timer invalidate];
                    _timer = nil;
                    _timer = [NSTimer scheduledTimerWithTimeInterval:5 target:self selector:@selector(pushOrderType) userInfo:nil repeats:YES];
                }
                
            }else if ([state integerValue] == 0){
                [[TShakeView getInstance] stop];
                UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"提醒通知"
                                                                message:[result objectForKey:@"info"]
                                                               delegate:self
                                                      cancelButtonTitle:@"知道了"
                                                      otherButtonTitles:nil];
                [alert show];
            }else if ([state integerValue] == 2){
                [[TShakeView getInstance] stop];
                UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"error"
                                                                message:@"订单已结算过"
                                                               delegate:self
                                                      cancelButtonTitle:@"知道了"
                                                      otherButtonTitles:nil];
                [alert show];
            }else if ([state integerValue] == 3){
                [[TShakeView getInstance] stop];
                UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"提醒通知"
                                                                message:@"余额不足"
                                                               delegate:self
                                                      cancelButtonTitle:@"充值"
                                                      otherButtonTitles:@"查看订单",nil];
                alert.tag = 1000;
                [alert show];
            }
            
        }else if(error)
        {
            [[TShakeView getInstance] fail];
        }
    }];
}

- (void)pushOrderType
{
    TPush* push = [TPush getInstance];
    [push requestMsg:nil];
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (alertView.tag == 1000) {
        if (buttonIndex == 0) {
            UIViewController* controller = [[TRechargeViewController alloc] init];
            [[TViewController share].centerController pushViewController:controller animated:NO];
        }else{
            [self clickedQueryCurrOrderButton];
        }
    }
}

- (void)playSound {
    // Get the main bundle for the app
    [self.audioPlayer prepareToPlay];
    [self.audioPlayer play];
}

- (AVAudioPlayer*)audioPlayer {
    if (!_audioPlayer) {
        NSBundle* mainBundle = [NSBundle mainBundle];
        NSString* filePath = [mainBundle pathForResource:@"5018" ofType:@"mp3"];
        NSData* data = [NSData dataWithContentsOfFile:filePath];
        NSError* error = nil;
        
        //后台播放音频设置
        AVAudioSession *session = [AVAudioSession sharedInstance];
        
        [session setActive:YES error:nil]; // 静音下也可播放
        
        _audioPlayer = [[AVAudioPlayer alloc] initWithData:data error:&error];
        _audioPlayer.delegate = self;
        _audioPlayer.volume = 0.8;
        _audioPlayer.numberOfLoops = 0;
    }
    return _audioPlayer;
}

//---------------------writeWithYang-------------------

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.view.backgroundColor = RGBCOLOR(237, 237, 237);
    self.titleView.text = @"停车宝";
    
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleMessage:) name:notification_msg_prepare_payOrder object:nil];
//    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(notificationRecevied:) name:notification_msg_payResult object:nil];
    
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    static int first = 0;
    //初始化 navigation items
    [self initNavigationItems:NO];
    [self showHistoryView:NO];
    
    [_mapView viewWillAppear];
    _mapView.delegate = self;
    _locationService.delegate = self;
    [_locationService startUserLocationService];
    
    //获取被遗漏的消息
    TPush* push = [TPush getInstance];
    [push requestMsg:nil];
    if (first++ == 0) {
        //ibeacon
        TIbeacon* ibeacon = [TIbeacon getInstance];
        [ibeacon startScan];
    }
    
    //更新订单状态
    [self checkCurrentOrder];
    //是否欠费
    [self checkAccount:nil];
    //查看是否有未领的红包，在底部提示
    [self checkRedpackage];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    [self becomeFirstResponder];
    
    [self showCarAnnotation];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self resignFirstResponder];
    
    [_locationService stopUserLocationService];
    [_mapView viewWillDisappear];
    _mapView.delegate = nil;
    _currentOrderModel.delegate = nil;
    _requestAllParksModel.delegate = nil;
    
    [_currentOrderReqeust cancel];
    [_allParksReqeust cancel];
    [_zhifuRequest cancel];
    [_switchRequest cancel];
    [_recommendRequest cancel];
    [_regionReqeust cancel];
    [_yueRequest cancel];
    [_priceRequest cancel];
    [_checkVersionRequest cancel];
    [_checkHolidayRequest cancel];
    [_checkAccountRequest cancel];
    [_shareInfoRequest cancel];
    [_yueRequest cancel];
    [_getDistanceRequest cancel];
    [_redPackageRequest cancel];
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidAppear:animated];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillChangeFrameNotification object:nil];
    //隐藏搜索记录
    [self showHistoryView:NO];
}

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    _locationSelfButton.frame = CGRectMake(2, _bottomView.top - 45, 35, 35);
    _locationCarButton.frame = CGRectMake(2, _bottomView.top - 45 - 40, 35, 35);
    _queryCurrOrder.frame = CGRectMake(_mapView.right - 58, _bottomView.top - 58, 55, 55);
}

//init navigation items
- (void)initNavigationItems:(BOOL)showSearch{
    self.titleView.hidden = showSearch;
    self.titleView.textColor = [UIColor grayColor];
    
    //把searchBar放在navigation bar  上
    CGFloat width = isIphone6Plus ? self.view.width - 124 : self.view.width - 120;
    _searchBar = [[UISearchBar alloc] initWithFrame:CGRectMake(0, 0, width, 40)];
    _searchBar.barTintColor = RGBCOLOR(237, 237, 237);
    _searchBar.delegate = self;
    _searchBar.placeholder = @"输入地址";
    UIBarButtonItem* searchItem = [[UIBarButtonItem alloc] initWithCustomView:_searchBar];
    
    [self updateRightItem:NO];
    if (showSearch)
        self.navigationItem.leftBarButtonItems = @[self.leftItem, searchItem];
    else
        self.navigationItem.leftBarButtonItems = @[self.leftItem];
}

- (void)handleMessage:(NSNotification*)noti {
    NSString* state = [[noti.userInfo objectForKey:@"info"] objectForKey:@"state"];
    if ([state isEqualToString:@"1"]) {//===========未结算或待支付
        //结算待支付
        [self updateOrderState:2];
        [self clickedQueryCurrOrderButton:nil];
    } else if ([state isEqualToString:@"0"]) {
        //有订单未结算
        [self updateOrderState:1];
        [self clickedQueryCurrOrderButton:nil];
    }
    [[NSUserDefaults standardUserDefaults] setObject:[noti.userInfo objectForKey:@"msgid"] forKey:save_msg_id];
}

#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (_searchMode == SearchModeHistory) {
        return [_historyResult count];
    } else {
        return [_searchResult count];
    }
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSString* identifier =  @"historyCell";
//    NSLog(@"indexPath: %d-%d", [_searchResult count], indexPath.row);
    UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier];
    }
    cell.textLabel.text = [(_searchMode == SearchModeHistory ? _historyResult : _searchResult) objectAtIndex:indexPath.row];
    if (![cell.textLabel.text isEqualToString:@"清除历史记录"]) {
        cell.textLabel.textColor = [UIColor blackColor];
        cell.imageView.image = [TAPIUtility ajustImage:[UIImage imageNamed:@"ic_search_history.png"] size:CGSizeMake(15, 15)];
    } else {
        cell.textLabel.textColor = [UIColor grayColor];
        cell.imageView.image = [TAPIUtility ajustImage:[TAPIUtility imageWithColor:[UIColor clearColor]] size:CGSizeMake(15, 15)];
    }
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    
    _selectedKeyword = [(_searchMode == SearchModeHistory ? _historyResultKeys : _searchResultKeys) objectAtIndex:indexPath.row];
    if ([_selectedKeyword isEqualToString:@"清除历史记录"]) {
        [NSKeyedArchiver archiveRootObject:[NSArray array] toFile:[TAPIUtility getHistoryPath]];
        //重新初始化
        [self showHistoryView:YES];
    } else {
        _poiSearchOption = [[BMKCitySearchOption alloc] init];
        _poiSearchOption.keyword = _selectedKeyword;
        _poiSearchOption.city = _cityName;
        [_poiSearch poiSearchInCity:_poiSearchOption];
        
        [self showHistoryView:NO];
        
        //自定义事件
        [MobClick event:@"2"];
    }
}

#pragma mark UIScrollViewDelegate

- (void)scrollViewDidScroll:(UIScrollView *)scrollView {
    [_searchBar endEditing:YES];
}

#pragma mark BMKSuggestionSearchDelegate

- (void)onGetSuggestionResult:(BMKSuggestionSearch *)searcher result:(BMKSuggestionResult *)result errorCode:(BMKSearchErrorCode)error {
    if (error == BMK_SEARCH_NO_ERROR) {
        NSMutableArray* array = [NSMutableArray array];
        for (int i=0; i<[result.keyList count]; i++) {
            NSString* item = [NSString stringWithFormat:@"%@   %@%@", [result.keyList objectAtIndex:i], [result.cityList objectAtIndex:i], [result.districtList objectAtIndex:i]];
            [array addObject:item];
        }
        _searchResultKeys = result.keyList;
        _searchResult = [NSArray arrayWithArray:array];
        [_searchTableView reloadData];
    } else {
        _searchResult = [NSArray array];
        _searchResultKeys = [NSArray array];
        [_searchTableView reloadData];
    }
}
#pragma mark UISearchBarDelegate

- (void)searchBarSearchButtonClicked:(UISearchBar *)searchBar {
    _selectedKeyword = searchBar.text;
    
    _poiSearchOption = [[BMKCitySearchOption alloc] init];
    _poiSearchOption.keyword = searchBar.text;
    _poiSearchOption.city = _cityName;
    [_poiSearch poiSearchInCity:_poiSearchOption];
    
    [self showHistoryView:NO];
}

- (void)searchBarTextDidBeginEditing:(UISearchBar *)searchBar {
    if ([_searchBar.text isEqualToString:@""]) {
        _searchBar.placeholder = @"输入地址";
        [self showHistoryView:YES];
    }
}

- (void)searchBar:(UISearchBar *)searchBar textDidChange:(NSString *)searchText {
    if (![searchText isEqualToString:@""]) {
        //隐藏 历史记录
        _searchMode = SearchModeResutl;
        
        _searchOption.cityname = _cityName;
        _searchOption.keyword = searchBar.text;
        [_suggestionSearch suggestionSearch:_searchOption];
    } else {
        //空时，显示 历史记录
        [self showHistoryView:YES];
    }
}

// show 历史记录
- (void)showHistoryView:(BOOL)show {
    if (show) {
        [self updateRightItem:YES];
        NSMutableArray*tempArray = [NSMutableArray arrayWithArray:[NSKeyedUnarchiver unarchiveObjectWithFile:[TAPIUtility getHistoryPath]]];
        if ([tempArray count]) {
            [tempArray addObject:@"清除历史记录"];
        }
        _historyResultKeys = [NSArray arrayWithArray:tempArray];
        _historyResult = [NSArray arrayWithArray:tempArray];
        
        _searchTableView.hidden = NO;
        _searchMode = SearchModeHistory;
        [_searchTableView reloadData];
        [self.view bringSubviewToFront:_searchTableView];
    } else {
        _searchMode = SearchModeHistory;
        _searchBar.text = @"";
        [_searchBar endEditing:YES];
        _searchTableView.hidden = YES;
        
        //重置navigation
        [self initNavigationItems:NO];
    }
}
#pragma mark BMKPoiSearchDelegate

- (void)onGetPoiResult:(BMKPoiSearch *)searcher result:(BMKPoiResult *)poiResult errorCode:(BMKSearchErrorCode)errorCode {
    if (errorCode == BMK_SEARCH_NO_ERROR) {
        BMKPoiInfo* info = [poiResult.poiInfoList firstObject];
//        NSLog(@"poi %f %f", info.pt.latitude, info.pt.longitude);

        _searchBar.placeholder = _selectedKeyword;
        //保存history记录
        NSMutableArray* array = [NSMutableArray arrayWithArray:[NSKeyedUnarchiver unarchiveObjectWithFile:[TAPIUtility getHistoryPath]]];
        if ([array containsObject:_selectedKeyword])
            [array removeObject:_selectedKeyword];
        [array insertObject:_selectedKeyword atIndex:0];
        [NSKeyedArchiver archiveRootObject:array toFile:[TAPIUtility getHistoryPath]];
        
        //放大移动到搜索位置
        [self moveMapToCenter:info.pt];
        //显示标注
        if (_searchAnnotation)
            [_mapView removeAnnotation:_searchAnnotation];
        _searchAnnotation = [[BMKPointAnnotation alloc] init];
        _searchAnnotation.coordinate = info.pt;
        _searchAnnotation.title = nil;
        [_mapView addAnnotation:_searchAnnotation];
        //请求recommend(先算路)
        [self requestAllParks:_searchAnnotation.coordinate];
    } else {
        [TAPIUtility alertMessage:@"抱歉，未找到结果" success:NO toViewController:self];
    }
}

#pragma mark BMKMapViewDelegate
//点击 车位
- (void)mapView:(BMKMapView *)mapView didSelectAnnotationView:(BMKAnnotationView *)view {
    if ([view.annotation isKindOfClass:[MyAnnotation class]]) {
        [self didSelectAnnotationView:view];
        //paopao 在边界的时候，移动屏幕
        CGFloat newX = _mapView.center.x;
        CGFloat newY = _mapView.center.y;
        //115,106 是paopaoView的宽高的一半
        if (view.center.x + 115 > self.view.width) {
            newX = _mapView.center.x + (view.center.x + 115 - _mapView.width) + 10;
        }
        if (view.center.x - 115 < 0) {
            newX = _mapView.center.x - (115 - view.center.x) - 10;
        }
        if (view.top - 106 < 0) {
            newY = _mapView.center.y - (106 - view.top) - 10;
        }
        if (newX != _mapView.center.x || newY != _mapView.center.y) {
            CGPoint newPoint = CGPointMake(newX, newY);
            CLLocationCoordinate2D c = [_mapView convertPoint:newPoint toCoordinateFromView:_mapView];
                [_mapView setCenterCoordinate:c animated:YES];
        }
        
        [_locationAnnotationView setSelected:NO animated:YES];
        [_carAnnotationView setSelected:NO animated:YES];
        [_locationAnnotationView.paopaoView removeFromSuperview];
        [_carAnnotationView.paopaoView removeFromSuperview];
        
    } else if (_locationAnnotationView == view) {
        [_selectedAnnotationView setSelected:NO animated:YES];
        _selectedParkId = nil;
        [_carAnnotationView setSelected:NO animated:YES];
        
        [_selectedAnnotationView.paopaoView removeFromSuperview];
        [_carAnnotationView.paopaoView removeFromSuperview];
        
    } else if (_carAnnotationView == view) {
        [_selectedAnnotationView setSelected:NO animated:YES];
        _selectedParkId = nil;
        [_locationAnnotationView setSelected:NO animated:YES];
        
        [_selectedAnnotationView.paopaoView removeFromSuperview];
        [_locationAnnotationView.paopaoView removeFromSuperview];
    }
}

//车位视图
- (BMKAnnotationView *)mapView:(BMKMapView *)view viewForAnnotation:(id <BMKAnnotation>)annotation {
    if (annotation == _searchAnnotation) {
        BMKPinAnnotationView* annotationView = [[BMKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"searchAnnotation"];
        return annotationView;
    } else if (annotation == _locationAnnotation) {
        BMKAnnotationView* annotationView = [[BMKAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"locationAnnotation"];
        annotationView.canShowCallout = YES;
        annotationView.paopaoView = self.locationPaopaoView;
        annotationView.image = [UIImage imageNamed:@"blue_circle.png"];
        annotationView.centerOffset = CGPointMake(0, -1*annotationView.height/2);
        static int firtLoad = 0;
        if (firtLoad == 0 && ![TAPIUtility getLoactionImage]) {
            [annotationView setSelected:YES];
            [_selectedAnnotationView setSelected:NO animated:YES];
            _selectedParkId = nil;
            [_carAnnotationView setSelected:NO animated:YES];
            [_selectedAnnotationView.paopaoView removeFromSuperview];
            [_carAnnotationView.paopaoView removeFromSuperview];
            firtLoad = 1;
        }
        
        _locationAnnotationView = annotationView;
        return annotationView;
        
    } else if (annotation == _carAnnotation) {
        BMKAnnotationView* annotationView = [[BMKAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"carAnnotation"];
        annotationView.canShowCallout = YES;
        annotationView.paopaoView = self.carPaopaoView;
        annotationView.image = [UIImage imageNamed:@"green_car.png"];
        annotationView.centerOffset = CGPointMake(0, -1*annotationView.height/2);
        if ([TAPIUtility getLoactionImage]) {
            [annotationView setSelected:YES];
            [_selectedAnnotationView setSelected:NO animated:YES];
            _selectedParkId = nil;
            [_locationAnnotationView setSelected:NO animated:YES];
            [_selectedAnnotationView.paopaoView removeFromSuperview];
            [_locationAnnotationView.paopaoView removeFromSuperview];
        }
        _carAnnotationView = annotationView;
        return annotationView;
    } else {
        BMKAnnotationView* annotationView = [[BMKAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"annotation"];
        annotationView.canShowCallout = YES;
        annotationView.paopaoView = [self getInfoPaopaoView:((MyAnnotation*)annotation).item];
        annotationView.image = [self getImageFromViewWithItem:((MyAnnotation*)annotation).item selected:[((MyAnnotation*)annotationView.annotation).item.parkId isEqualToString:_selectedParkId]];
        [annotationView setSelected:[((MyAnnotation*)annotationView.annotation).item.parkId isEqualToString:_selectedParkId] ? YES : NO];
        if ([((MyAnnotation*)annotationView.annotation).item.parkId isEqualToString:_selectedParkId]) {
            //其实暂时不会执行这，因为新添加的annotaion是不会被选中的
            _selectedAnnotationView = annotationView;
            
            [_locationAnnotationView setSelected:NO animated:YES];
            [_carAnnotationView setSelected:NO animated:YES];
            [_locationAnnotationView.paopaoView removeFromSuperview];
            [_carAnnotationView.paopaoView removeFromSuperview];
        }
        return annotationView;
    }
    return nil;
}

- (void)mapView:(BMKMapView *)mapView regionWillChangeAnimated:(BOOL)animated {
}

- (void)mapView:(BMKMapView *)mapView regionDidChangeAnimated:(BOOL)animated {
}

#pragma mark BMKLocationServiceDelegate

- (void)willStartLocatingUser {
//    NSLog(@"start location");
}

- (void)didStopLocatingUser {
//    NSLog(@"stop location");
}

- (void)didUpdateUserHeading:(BMKUserLocation *)userLocation {
//    [_mapView updateLocationData:userLocation];
}

- (void)didUpdateBMKUserLocation:(BMKUserLocation *)userLocation {
    CLLocationCoordinate2D newCoordinate = userLocation.location.coordinate;
    if (newCoordinate.latitude == 0 || newCoordinate.longitude == 0) {
        NSLog(@"定位失败");
        return;
    }
//---------------------writeWithYang-------------------
    
    if (self.centralManager.state == CBCentralManagerStatePoweredOff || !self.centralManager) {
        
        if ([TAPIUtility getDistance] == nil || [[TAPIUtility getDistance] floatValue] > 200) {
            
            if ([TAPIUtility getCoordinate].latitude == 0) {
                [TAPIUtility saveCoordinate:newCoordinate];
                [self iBeaconParkDisatuceWithLocation:newCoordinate completion:^{
                    if (self.centralManager) {
                        [self alertShow];
                    }else{
                       self.centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
                    }
                }];
            }else if(![TAPIUtility getDistance])
            {
                [self iBeaconParkDisatuceWithLocation:newCoordinate completion:^{
                    if (self.centralManager) {
                        [self alertShow];
                    }else{
                        self.centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
                    }
                }];
            }else{
                if ([self disatuceFromCoordinate:[TAPIUtility getCoordinate] to:newCoordinate]) {
                    
                    [TAPIUtility saveCoordinate:newCoordinate];
                    [self iBeaconParkDisatuceWithLocation:newCoordinate completion:^{
                        if (self.centralManager) {
                            [self alertShow];
                        }else{
                            self.centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
                        }
                    }];
                }
            }
        }
    }
//---------------------writeWithYang-------------------
    
    if (fabs(_coordinate.latitude - newCoordinate.latitude) > 0.0001 || fabs(_coordinate.longitude - newCoordinate.longitude) > 0.0001) {
        _coordinate = newCoordinate;
        
        //切换到对应的城市
        [self switchCity];
        
        [_mapView updateLocationData:userLocation];
        
        if (_fisrtLocation) {
            _fisrtLocation = NO;
            //这两行代码不要动！animate不要设YES
            [_mapView setCenterCoordinate:_coordinate animated:NO];
            _mapView.zoomLevel = zoom_lever;
            
            //获取所有停车场信息
            [self requestAllParks:_coordinate];
        }
        
        //中心小车图标
        if (_locationAnnotation)
            [_mapView removeAnnotation:_locationAnnotation];
        _locationAnnotation = [[BMKPointAnnotation alloc] init];
        _locationAnnotation.coordinate = _coordinate;
        _locationAnnotation.title = @"";
        [_mapView addAnnotation:_locationAnnotation];
        BMKAnnotationView* annotionView = [_mapView viewForAnnotation:_locationAnnotation];
        //这行必须加，不然不能在最前面,但是要在paopao下面
        for (UIView* view in annotionView.superview.subviews) {
            if ([view isKindOfClass:[BMKActionPaopaoView class]]) {
                [annotionView.superview insertSubview:annotionView belowSubview:view];
            }
        }
    }
    
}
//---------------------writeWithYang-------------------
- (void)iBeaconParkDisatuceWithLocation:(CLLocationCoordinate2D)coor completion:(void(^)(void))handler
{
    NSString* apiPath = [NSString stringWithFormat:@"carservice.do?action=scanibeacon&lng=%lf&lat=%lf",coor.longitude,coor.latitude];
    _currentOrderReqeust = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    
    _currentOrderModel = [[CVAPIRequestModel alloc] init];
    _currentOrderModel.delegate = self;
    _currentOrderModel.hideNetworkView = YES;
    [_currentOrderModel sendRequest:_currentOrderReqeust completion:^(NSDictionary *result, NSError *error) {
        if (!error && result) {
            [TAPIUtility saveDistance:[result objectForKey:@"info"]];
            if ([[TAPIUtility getDistance] floatValue] < 200.0 && handler) {
                handler();
            }
        }
    }];
}

- (BOOL)disatuceFromCoordinate:(CLLocationCoordinate2D)coorA to:(CLLocationCoordinate2D)coorB
{
    CLLocation *orig=[[CLLocation alloc] initWithLatitude:coorA.latitude  longitude:coorA.longitude];
    CLLocation* dist=[[CLLocation alloc] initWithLatitude:coorB.latitude longitude:coorB.longitude];
    
    CLLocationDistance kilometers=[orig distanceFromLocation:dist];
    
    if (kilometers >= [[TAPIUtility getDistance] doubleValue]) {
        return YES;
    }
    return NO;
}

- (void)centralManagerDidUpdateState:(CBCentralManager *)central
{
    [TAPIUtility saveDistance:nil];
    switch (central.state) {
        case CBCentralManagerStatePoweredOff:
            [self.locationManager stopRangingBeaconsInRegion:self.myBeaconRegion];
            break;
        case CBCentralManagerStatePoweredOn:
            [self.locationManager startRangingBeaconsInRegion:self.myBeaconRegion];
            break;
        default:
            break;
    }
}

- (void)alertShow
{
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"请先打开蓝牙"
                                                    message:@"快速通行,先人一步"
                                                   delegate:self
                                          cancelButtonTitle:@"知道了"
                                          otherButtonTitles: nil];
    [alert show];
}

#pragma locationManagerDelegate

- (void)locationManager:(CLLocationManager *)manager
rangingBeaconsDidFailForRegion:(CLBeaconRegion *)region
              withError:(NSError *)error
{
    _beacon = nil;
}

- (void)locationManager:(CLLocationManager *)manager
        didRangeBeacons:(NSArray *)beacons
               inRegion:(CLBeaconRegion *)region
{
    
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        return;
    }
    
    if ([beacons count]) {
        _beacon = nil;
        for (CLBeacon* beacon in beacons) {
            if (!_beacon || beacon.proximity <= _beacon.proximity) {
                _beacon = beacon;
            }
        }
    }else{
        _beacon = nil;
    }
   
    if (_beacon && [[NSUserDefaults standardUserDefaults] objectForKey:save_phone] &&[[TAPIUtility getDistance] floatValue] < 200.0 &&[[TAPIUtility getDistance] floatValue]) {
        double nowTime = [[NSDate date] timeIntervalSince1970];
        if (nowTime - _oldTime > 5*60) {
            _oldTime = nowTime;
            // 弹框
            if ([TAPIUtility getFirstInfo]) {
                [TAPIUtility saveFirstInfo:@"1"];
                
                TPromptImageView *prompt = [[TPromptImageView alloc] initWithFrame:CGRectMake(20, 20, mainScreenSize.width - 40, mainScreenSize.height/3*2)];
                [self.view addSubview:prompt];
            }else{
                [self sendRequestIncomeOrOutParket:nil withMoto:NO];
            }
        }
    }
}

- (void)sendRequestIncomeOrOutParket:(void(^)(NSDictionary *dict))handle withMoto:(BOOL)moto {
    //    NSLog(@"send---------");
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        return;
    }
    CVAPIRequest* request = [[CVAPIRequest alloc] initWithAPIPath:[NSString stringWithFormat:@"ibeaconhandle.do?major=%@&minor=%@&action=ibcincom&mobile=%@", _beacon.major, _beacon.minor, [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]] timeout:6.00];
    CVAPIRequestModel* model = [[CVAPIRequestModel alloc] init];
    model.delegate = self;
//    model.hideNetworkView = YES;
    [model sendRequest:request completion:^(NSDictionary *result, NSError *error) {
//        inout:入场/出场，0入口，1出口 -1通道不存在
//    uid:收费员编号，不处理，生成或结算订单时传回
        if (result) {
            NSString* state = [result objectForKey:@"inout"];
            if ([state integerValue] == 0) {
                
                if ([[result objectForKey:@"orderid"] intValue] > 0) {
                    if (moto) {
                        [[TShakeView getInstance] stop];
                        [self clickedQueryCurrOrderButton];
                    }
                }else {
                    
                    //进场
                    [[TShakeView getInstance] normal:YES];
                    
                    if (moto) {
                        if (handle) {
                            handle(result);
                        }
                    }
                }
            }else if ([state integerValue] == 1){
                //出场
                if ([[result objectForKey:@"orderid"] intValue] == 0) {
                    if (moto) {
                        
                        if ([[result objectForKey:@"uid"] intValue] == -1) {
                            [[TShakeView getInstance] normalWithText:@"该出口没有收费员在岗"];
                        }else{
                            [[TShakeView getInstance] stop];
                            TPayCollectorViewController* controller = [[TPayCollectorViewController alloc] init];
                            controller.parkName      = [result objectForKey:@"parkname"];
                            controller.collectorName = [result objectForKey:@"name"];
                            controller.collectorId   = [result objectForKey:@"uid"];
                            
                            [[TViewController share].centerController pushViewController:controller animated:NO];
                        }
                    }
                }else {

                    [[TShakeView getInstance] normal:NO];
                    
                    if (moto) {
                        if (handle) {
                            handle(result);
                        }
                    }
                }
            }else if ([state integerValue] == 2){
                //综合一体
                if ([[result objectForKey:@"orderid"] intValue] > 0) {
                    [[TShakeView getInstance] normal:NO];
                    if (moto) {
                        if (handle) {
                            handle(result);
                        }
                    }
                }else {
                    
                    [[TShakeView getInstance] normal:YES];
                    if (moto) {
                        if (handle) {
                            handle(result);
                        }
                    }
                }
            }else if ([state integerValue] == -1){
                [[TShakeView getInstance] normalWithText:@"未知车场"];
            }
            
        }else{
            [[TShakeView getInstance] fail];
        }
    }];
}

//---------------------writeWithYang-------------------

- (void)didFailToLocateUserWithError:(NSError *)error {
    NSLog(@"未定位成功！");
}

#pragma mark - BNNaviRoutePlanDelegate
//算路成功回调
-(void)routePlanDidFinished:(NSDictionary *)userInfo
{
    NSLog(@"算路成功");
    
    //路径规划成功，开始导航
    [BNCoreServices_UI showNaviUI:BN_NaviTypeReal delegete:self isNeedLandscape:NO];
}

//算路失败回调
- (void)routePlanDidFailedWithError:(NSError *)error andUserInfo:(NSDictionary *)userInfo
{
    NSLog(@"算路失败");
}

//算路取消回调
-(void)routePlanDidUserCanceled:(NSDictionary*)userInfo {
    NSLog(@"算路取消");
}

#pragma mark - BNNaviUIManagerDelegate

//退出导航回调
-(void)onExitNaviUI:(NSDictionary*)extraInfo
{
    NSLog(@"退出导航");
}

//退出导航声明页面回调
- (void)onExitexitDeclarationUI:(NSDictionary*)extraInfo
{
    NSLog(@"退出导航声明页面");
}

//算路

#pragma mark request

- (void)requestAllParks {
    if (_requestCoordinate.latitude != 0 && _requestCoordinate.longitude != 0)
        [self requestAllParks:_requestCoordinate];
}

- (void)requestAllParks:(CLLocationCoordinate2D) coordinate{
    _requestCoordinate = coordinate;
    
    _allParksReqeust = [[CVAPIRequest alloc] initWithAPIPath:[NSString stringWithFormat:@"getpark.do?action=get2kpark&lng=%lf&lat=%lf", coordinate.longitude, coordinate.latitude]];
    _allParksReqeust.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    _requestAllParksModel = [[CVAPIRequestModel alloc] init];
    _requestAllParksModel.delegate = self;
    [_requestAllParksModel sendRequest:_allParksReqeust completion:^(NSDictionary *result, NSError *error) {
        if (result) {
            //先移除所有的停车场信息
            [self removedAnnotations:_allParkItems];
            [_allParkItems removeAllObjects];
            [_payParkItems removeAllObjects];
            
            
            //update bottomView UI
            _isLack = [[result objectForKey:@"lack"] isEqualToString:@"1"];
            if (_isLack) {
                //显示上传
                // 上传模式优先级小于充值模式
                [self checkAccount:^(BOOL isNeedRecharge) {
                    //如果不需要充值提醒 则显示 上传
                    if (isNeedRecharge == NO) {
                        [_bottomView setMode:SearchViewMode_upload];
                    }
                }];
            } else {
                //不显示上传
                if (_bottomView.mode == SearchViewMode_upload) {
                    [_bottomView setMode:SearchViewMode_normal];
                }
            }
            
            
            _selectedParkId = [result objectForKey:@"suggid"];
            
            for (NSDictionary* object in [result objectForKey:@"data"]) {
                TParkItem* item = [TParkItem getItemFromDictionary:object];
                [_allParkItems addObject:item];
                if ([item.epay isEqualToString:@"1"]) {
                    [_payParkItems addObject:item];
                }
            }
            //更新并显示可支付车场
            [self addAnnotations:_onlyShowPayParks ? _payParkItems : _allParkItems];
            
            
//            //去掉 "正在加载..." 提示
//            [_mapView viewForAnnotation:_locationAnnotation].canShowCallout = NO;
//            [[_mapView viewForAnnotation:_locationAnnotation].paopaoView removeFromSuperview];
        }
    }];
}


#pragma mark private

- (TCurrentOrderView*)currentOrderView {
    if (!_currentOrderView) {
        _currentOrderView = [[TCurrentOrderView alloc] initWithFrame:[UIScreen mainScreen].bounds];
        __weak __typeof__ (self) wself = self;
        _currentOrderView.scanBlock = ^{
            __strong __typeof (wself) sself = wself;
            TReaderViewController* vc = [[TReaderViewController alloc] init];
            [sself.navigationController pushViewController:vc animated:YES];
            
            [sself.currentOrderView show:NO];
        };
        _currentOrderView.closeBlock = ^{
            __strong __typeof (wself) sself = wself;
            sself.currentOrderModel.delegate = nil;
            [sself.currentOrderReqeust cancel];
        };
        _currentOrderView.photoBlock = ^{
            __strong __typeof (wself) sself = wself;
            
            if (![TAPIUtility checkPhotoAuthorization]) {
                return;
            }
            
            UIImagePickerController* picker = [[UIImagePickerController alloc] init];
            picker.sourceType = UIImagePickerControllerSourceTypeCamera;
            picker.mediaTypes = @[(NSString*)kUTTypeImage];
            picker.delegate = sself;
            [sself presentViewController:picker animated:YES completion:nil];
            
            //隐藏
            [sself.currentOrderView show:NO];
            
        };
        _currentOrderView.payBlock = ^(TCurrentOrderItem* item){
            if ([item.total floatValue] == 0)
                [TAPIUtility alertMessage:@"金额为0元，无需支付"];
            else {
                
                __strong __typeof (wself) sself = wself;
                
                TRechargeWaysViewController* vc = [[TRechargeWaysViewController alloc] init];
                vc.orderId = item.orderid;
                vc.name = @"停车费";
                vc.price = item.total;
                vc.rechargeMode = RechargeMode_order;
                [sself.navigationController pushViewController:vc animated:YES];
                
                //隐藏
                [sself.currentOrderView show:NO];
            }
        };
    }
    return _currentOrderView;
}

- (void)updateRightItem:(BOOL)showHistory {
    _cancelButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [_cancelButton setTitleColor:green_color forState:UIControlStateNormal];
    [_cancelButton addTarget:self action:@selector(cancleItemTouched:) forControlEvents:UIControlEventTouchUpInside];
    [_cancelButton setContentHorizontalAlignment:UIControlContentHorizontalAlignmentRight];
    if (showHistory) {
        [_cancelButton setTitle:@"取消" forState:UIControlStateNormal];
        _cancelButton.frame = CGRectMake(0, 0, 40, 30);
        [_cancelButton setBackgroundImage:nil forState:UIControlStateNormal];
    } else {
        [_cancelButton setTitle:@"" forState:UIControlStateNormal];
        [_cancelButton setBackgroundImage:[UIImage imageNamed:@"scan_reader.png"] forState:UIControlStateNormal];
        _cancelButton.frame = CGRectMake(0, 0, 25, 25);
    }
    UIBarButtonItem* cancleItem = [[UIBarButtonItem alloc] initWithCustomView:_cancelButton];
    self.navigationItem.rightBarButtonItem = cancleItem;
}

- (BMKActionPaopaoView*)locationPaopaoView {
//    if (!_locationPaopaoView) {
        UIView* view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 170, 50)];
        UIImageView* imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"green_paopao.png"]];
        imgView.frame = view.frame;
        imgView.userInteractionEnabled = YES;
  
        UILabel* label = [[UILabel alloc] initWithFrame:CGRectMake(10, 5, 140, 20)];
        label.backgroundColor = [UIColor clearColor];
        label.text = ![TAPIUtility getLoactionImage] ? @"停车之后，拍一张" : @"拍照记录新位置";
        label.font = [UIFont systemFontOfSize:13];
        label.textColor = [UIColor whiteColor];
        label.textAlignment = NSTextAlignmentLeft;
        label.tag = 1;
    
        UILabel* label2 = [[UILabel alloc] initWithFrame:CGRectMake(label.left, 20, label.width, label.height)];
        label2.backgroundColor = [UIColor clearColor];
        label2.text = ![TAPIUtility getLoactionImage] ? @"回头找车，快一点" : @"同时清空旧位置";
        label2.font = [UIFont systemFontOfSize:11];
        label2.textColor = [UIColor whiteColor];
        label2.textAlignment = NSTextAlignmentLeft;
        label2.tag = 2;
        
        UIButton* button = [UIButton buttonWithType:UIButtonTypeCustom];
        button.frame = view.frame;
        [button addTarget:self action:@selector(takePictureButtonTouched) forControlEvents:UIControlEventTouchUpInside];
        
        [view addSubview:imgView];
        [view addSubview:label];
        [view addSubview:label2];
        [view addSubview:button];
        
        _locationPaopaoView = [[BMKActionPaopaoView alloc] initWithCustomView:view];
//    }
    return _locationPaopaoView;
}

- (BMKActionPaopaoView*)carPaopaoView {
    if (!_carPaopaoView) {
        UIView* view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 100, 36)];
        UIImageView* imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"green_bg_paopao.png"]];
        imgView.frame = view.frame;
        imgView.userInteractionEnabled = YES;
        
        UILabel* label = [[UILabel alloc] initWithFrame:CGRectMake(0, 6, view.width - 5, 20)];
        label.backgroundColor = [UIColor clearColor];
        label.text = @"车停在这了";
        label.font = [UIFont systemFontOfSize:12];
        label.textColor = [UIColor whiteColor];
        label.textAlignment = NSTextAlignmentCenter;
        
        UIImageView* imgView2 = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"white_right_arrow.png"]];
        imgView2.frame = CGRectMake(view.width - 14, 12, 7, 10);
        
        UIButton* button = [UIButton buttonWithType:UIButtonTypeCustom];
        button.frame = view.frame;
        [button addTarget:self action:@selector(showPictureInfo) forControlEvents:UIControlEventTouchUpInside];
        
        [view addSubview:imgView];
        [view addSubview:label];
        [view addSubview:button];
        [view addSubview:imgView2];
        
        _carPaopaoView = [[BMKActionPaopaoView alloc] initWithCustomView:view];
    }
    return _carPaopaoView;
}

- (BMKActionPaopaoView*)getInfoPaopaoView:(TParkItem*)item {
    UIView* view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 230, 106)];
    TParkInfoView* parkView = [[TParkInfoView alloc] initWithFrame:view.frame];
    parkView.delegate = self;
    parkView.item = item;
    [view addSubview:parkView];
        
    _infoPaopaoView = [[BMKActionPaopaoView alloc] initWithCustomView:view];
    return _infoPaopaoView;
}


- (void)moveMapToCenter:(CLLocationCoordinate2D)coordinate {
    [_mapView setCenterCoordinate:coordinate animated:YES];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, NSEC_PER_SEC * 0.5),
        dispatch_get_main_queue(), ^{
            [_mapView setZoomLevel:zoom_lever];
    });
}

- (void)addAnnotations:(NSArray*) parkItems {
    NSMutableArray* annotations = [NSMutableArray array];
    for (TParkItem* item in parkItems) {
        
        MyAnnotation* annotation = [[MyAnnotation alloc] init];
//        annotation.title = [NSString stringWithFormat:@"%@,%@", item.latitude, item.longitude];
        annotation.item = item;
        annotation.coordinate = CLLocationCoordinate2DMake([item.lat doubleValue], [item.lng doubleValue]);
        [annotations addObject:annotation];
    }
    [_mapView addAnnotations:annotations];
}

- (void)removedAnnotations:(NSArray*) parkItems {
    NSMutableArray* annotations = [NSMutableArray array];
    for (TParkItem* item in parkItems) {
        for (MyAnnotation* annotation in _mapView.annotations) {
            if ([annotation isKindOfClass:[MyAnnotation class]] && [annotation.item.parkId isEqualToString:item.parkId]) {
                [annotations addObject:annotation];
                break;
            }
        }
    }
    [_mapView removeAnnotations:annotations];
}



- (void)cancleItemTouched:(UIButton*)button {
    if ([button.titleLabel.text isEqualToString:@"取消"])
        [self showHistoryView:NO];
    else if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]){
        [self.navigationController pushViewController:[[TLoginViewController alloc] init] animated:YES];
    } else {
        //扫描二维码
        TReaderViewController* vc = [[TReaderViewController alloc] init];
        [self.navigationController pushViewController:vc animated:YES];
        //自定义事件
        [MobClick event:@"6"];
    }
}

- (void)rightItemTouched:(UIBarButtonItem*)item {
    [[TViewController share] showOrHideRightMenu];
}


//更改 选中 annotation 的大小
- (void) didSelectAnnotationView:(BMKAnnotationView *)view {
    if (![view.annotation isKindOfClass:[MyAnnotation class]]) {
        return;
    }
    
    if (_selectedAnnotationView) {
        id annotation = _selectedAnnotationView.annotation;
        _selectedAnnotationView.image = [self getImageFromViewWithItem:((MyAnnotation*)annotation).item selected:NO];
        
        [_selectedAnnotationView setSelected:NO animated:YES];
    }
    _selectedAnnotationView = view;
    id annotation = _selectedAnnotationView.annotation;
    
    _selectedAnnotationView.image = [self getImageFromViewWithItem:((MyAnnotation*)annotation).item selected:YES];
    [_selectedAnnotationView setSelected:YES animated:NO];
    
    _selectedParkId = ((MyAnnotation*)view.annotation).item.parkId;
    
    [[view superview] bringSubviewToFront:view];
    //这行必须加，不然不能在最前面,但是要在paopao下面
    if (_locationAnnotation) {
        BMKAnnotationView* recommendView = [_mapView viewForAnnotation:_locationAnnotation];
        for (UIView* view in recommendView.superview.subviews) {
            if ([view isKindOfClass:[BMKActionPaopaoView class]]) {
                [recommendView.superview insertSubview:recommendView belowSubview:view];
            }
        }
    }
}

//还原被中的停车场大小(当附近没有推荐车场时 调用)
- (void)deSelectAnnotationView {
    if (_selectedAnnotationView) {
        id annotation = _selectedAnnotationView.annotation;
        _selectedAnnotationView.image = [self getImageFromViewWithItem:((MyAnnotation*)annotation).item selected:NO];
        
        [_selectedAnnotationView setSelected:NO animated:YES];
    }
    _selectedAnnotationView = nil;
    _selectedParkId = nil;
}


-(UIImage *)getImageFromViewWithItem:(TParkItem*)item selected:(BOOL)selected{
    NSString* way = @"";
    if ([item.price intValue] < 0) {
        way = @"free";
    } else if ([item.epay intValue] == 1) {
        way = @"pay";
    } else {
        way = @"normal";
    }
    NSString* imgName = [NSString stringWithFormat:@"park_%@_%@", way, [TAPIUtility colorWithFree:[item.free doubleValue] total:[item.total doubleValue]]];
    if (selected) {
        imgName = [imgName stringByAppendingString:@"_select"];
    }
    imgName = [imgName stringByAppendingString:@".png"];
    
    UIView *viewForImage=[[UIView alloc]initWithFrame:CGRectMake(0, 0, isIphone6Plus ? annotation_width*1.5 : annotation_width, isIphone6Plus ? annotation_height*1.5 : annotation_height)];
    UIImageView *imageview=[[UIImageView alloc]initWithFrame:viewForImage.frame];
    [imageview setImage:[UIImage imageNamed:imgName]];
    [viewForImage addSubview:imageview];
    
    UIGraphicsBeginImageContext(viewForImage.bounds.size);
    [viewForImage.layer renderInContext:UIGraphicsGetCurrentContext()];
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return image;
}

- (void)clickedLocationButton:(id)sender {
    //animate 不要设为 YES
   [_mapView setCenterCoordinate:_coordinate animated:NO];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, NSEC_PER_SEC * 0.5),
                   dispatch_get_main_queue(), ^{
                       [_mapView setZoomLevel:zoom_lever];
                   });
    //清空，nearView name 会用到这个值
    _selectedKeyword = @"";
    [self requestAllParks:_coordinate];
//    [self requestRecommendParks:_coordinate hour:0 minite:0 name:@"我的位置" showPrompt:YES prompt:nil];
}

- (void)clickedLocationCarButton {
    //animate 不要设为 YES
    CLLocationCoordinate2D coodinate = CLLocationCoordinate2DMake([GL(save_location_lat) doubleValue], [GL(save_location_log) doubleValue]);
    
    [_mapView setCenterCoordinate:coodinate animated:NO];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, NSEC_PER_SEC * 0.5),
                   dispatch_get_main_queue(), ^{
                       [_mapView setZoomLevel:zoom_lever];
                   });
    //弹出car的弹框
    [self showCarAnnotation];
}

- (void)clickedQueryCurrOrderButton:(TCurrentOrderItem*)item {
    //先登录
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]){
        [self.navigationController pushViewController:[[TLoginViewController alloc] init] animated:YES];
        return;
    }
    
    [self.currentOrderView show:YES];
    [self.currentOrderView updateView:currentOrderMode_loading];
    
    if (!item) {
        [self.currentOrderView.loaddingView startAnimating];
        NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=currentorder&mobile=%@",[[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
        _currentOrderReqeust = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
        
        _currentOrderModel = [[CVAPIRequestModel alloc] init];
        _currentOrderModel.delegate = self;
        [_currentOrderModel sendRequest:_currentOrderReqeust completion:^(NSDictionary *result, NSError *error) {
            if (!result)
                return;
            TCurrentOrderItem* item = nil;
            if ([result count])
                item = [TCurrentOrderItem getItemFromDictionary:result];
            
            if (item == nil) {
                [self updateOrderState:0];
            } else if ([item.state isEqualToString:@"0"]) {
                [self updateOrderState:1];
            } else {
                [self updateOrderState:2];
            }
            
            [self.currentOrderView setItem:item];
        }];
    } else {
        if (item == nil) {
            [self updateOrderState:0];
        } else if ([item.state isEqualToString:@"0"]) {
            [self updateOrderState:1];
        } else {
            [self updateOrderState:2];
        }
        [self.currentOrderView setItem:item];
    }
}

- (void)clickedQueryCurrOrderButton {
    [self clickedQueryCurrOrderButton:nil];
}

- (void)checkCurrentOrder {
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone])
        return;
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=currentorder&mobile=%@",[[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _currentOrderReqeust = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    
    _currentOrderModel = [[CVAPIRequestModel alloc] init];
    _currentOrderModel.delegate = self;
    [_currentOrderModel sendRequest:_currentOrderReqeust completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        TCurrentOrderItem* item = nil;
        if ([result count])
            item = [TCurrentOrderItem getItemFromDictionary:result];
        if (item == nil) {
            [self updateOrderState:0];
        } else if ([item.state isEqualToString:@"0"]) {
            [self updateOrderState:1];
        } else {
            [self updateOrderState:2];
        }
    }];
}

- (void)takePictureButtonTouched {
    //查检相机权限
    if (![TAPIUtility checkPhotoAuthorization]) {
        return;
    }
    
    BMKAnnotationView* annotationView = [_mapView viewForAnnotation:_locationAnnotation];
    [annotationView.paopaoView removeFromSuperview];
    [annotationView setSelected:NO animated:NO];
    
    _locationImagePicker = [[UIImagePickerController alloc] init];
    _locationImagePicker.sourceType = UIImagePickerControllerSourceTypeCamera;
    _locationImagePicker.mediaTypes = @[(NSString*)kUTTypeImage];
    _locationImagePicker.delegate = self;
    [self presentViewController:_locationImagePicker animated:YES completion:nil];
}

- (void)showPictureInfo {
    BMKAnnotationView* annotationView = [_mapView viewForAnnotation:_carAnnotation];
    [annotationView.paopaoView removeFromSuperview];
    [annotationView setSelected:NO animated:NO];
    
    TLocationInfoViewController* vc = [[TLocationInfoViewController alloc] init];
    [self.navigationController pushViewController:vc animated:YES];
}

//检查最新版本
- (void)checkVersion {
    NSString* apiPath = [NSString stringWithFormat:@"update/user/ios_user_%@update.txt", [TAPIUtility isEnterpriseVersion] ? @"enterprise_" : @""];
    _checkVersionRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath downLoad:YES];
    
    CVAPIRequestModel* model = [[CVAPIRequestModel alloc] init];
    model.hideNetworkView = YES;
    [model sendRequest:_checkVersionRequest completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        NSString* newVersion  = [result objectForKey:@"info"];
        NSString* nowVersion = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"];
        
        if ([TAPIUtility isUpdateVersionWithOld:nowVersion new:newVersion] && [[result objectForKey:@"alertUpdate"] isEqualToString:@"1"]) {
            TVersionAlertView* versionAlert = [[TVersionAlertView alloc] init];
            [versionAlert setVersion:[result objectForKey:@"info"]];
            versionAlert.appStoreUrl = [result objectForKey:@"url"];
            [versionAlert show];
        } else {
        }
    }];
}

//检查是否有节日活动
- (void)checkHoliday {
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone])
        return;
    if ([[NSUserDefaults standardUserDefaults] objectForKey:save_holiday_see_time]) {
        NSDate* lastSeeDate = [NSDate dateWithTimeIntervalSince1970:[[[NSUserDefaults standardUserDefaults] objectForKey:save_holiday_see_time] doubleValue]];
        NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
        [formatter setDateFormat:@"yyyy-MM-dd"];
        NSString* now = [formatter stringFromDate:lastSeeDate];
        NSString* last = [formatter stringFromDate:[NSDate date]];
        if ([now isEqualToString:last])
            return;
    }
    
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=hbonus&mobile=%@&version=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone],[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleShortVersionString"]];
    _checkHolidayRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    
    CVAPIRequestModel* model = [[CVAPIRequestModel alloc] init];
    model.hideNetworkView = YES;
    [model sendRequest:_checkHolidayRequest completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        if ([result count] > 0) {
            [[SDWebImageDownloader sharedDownloader] downloadImageWithURL:[NSURL URLWithString:[TAPIUtility getNetworkWithUrl:[result objectForKey:@"imgurl"]]] options:0 progress:nil completed:^(UIImage *image, NSData *data, NSError *error, BOOL finished) {
    //            [_holidayView.holidayButton setImage:[UIImage imageNamed:@"test2.jpg"] forState:UIControlStateNormal];
                if (image) {
                    _holidayView.hidden = NO;
                    [_holidayView.holidayButton setImage:image forState:UIControlStateNormal];
                    if ([[result objectForKey:@"sharable"] isEqualToString:@"1"])
                        _holidayView.holidayButton.userInteractionEnabled = YES;
                    
                    [[NSUserDefaults standardUserDefaults] setObject:@([[NSDate date] timeIntervalSince1970]) forKey:save_holiday_see_time];
                } else {
                }
            }];
        }
            
    }];
}

//检查信用额度是否不够
- (void)checkAccount:(void(^)(BOOL isNeedRecharge))handle {
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        if (handle) {
            handle(NO);
        }
        return;
    }
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=detail&mobile=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _checkAccountRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    [self.model sendRequest:_checkAccountRequest completion:^(NSDictionary *result, NSError *error) {
        if (!result || [result count] == 0)
            return;
        TAccountItem* item = [TAccountItem getItemFromeDictionary:result];
        
        if ([item.limit doubleValue] > 0) {
            _lackMoney = [NSString stringWithFormat:@"%lf", [item.limit doubleValue] - [item.limit_balan doubleValue]];
        }
        
        if ([item.limit doubleValue] > 0 &&  [item.limit_balan doubleValue] < [item.limit_warn doubleValue]) {
            //欠费
            [_bottomView setMode:SearchViewMode_money];
            
            //回调返回结果
            if (handle) {
                handle(YES);
            }
        } else {
            //未欠费
            if (_bottomView.mode == SearchViewMode_money) {
                _bottomView.mode = SearchViewMode_normal;
            }
            //回调返回结果
            if (handle) {
                handle(NO);
            }
        }
    }];
}

- (void)checkRedpackage
{
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=bonusinfo&mobile=%@",[[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _redPackageRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    
    CVAPIRequestModel* model = [[CVAPIRequestModel alloc] init];
    model.delegate = self;
    model.hideNetworkView = YES;
    [model sendRequest:_redPackageRequest completion:^(NSDictionary *result, NSError *error) {
        if (result&&!error) {
            if ([result isKindOfClass:[NSArray class]]) {
                int number = 0;
                for (int i=0; i<[result count]; i++) {
                    NSDictionary *dict = [(NSArray*)result objectAtIndex:i];
                    if ([[dict objectForKey:@"state"] integerValue] == 1) {
                        number++;
                    }
                }
                _bottomView.redNumber = @(number);
            }
        }
    }];
}

//
//#pragma mark TChooseCityViewDelegate
//
//- (void)chooseCityViewWithIndex:(int)index {
//    if (index == 0) {
//       [_mapView setZoomLevel:11];
//        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, NSEC_PER_SEC * 0.5),
//                       dispatch_get_main_queue(), ^{
//                           //以 天安门为中心
//                           [_mapView setCenterCoordinate:CLLocationCoordinate2DMake(39.915168, 116.403875) animated:YES];
//                       });
//    } else {
//    }
//    _chooseCityView.hidden = YES;
//}

- (void)showCarAnnotation {
    //小车停车图标
    //annotationView setSelected:YES 无效，只能删了，重新绘制; setselected:NO虽然管用，但是paopao会悬浮，所以必须要paopaoView removeFromSuper
    //弹出car的弹框 只能删了重新创建一个新的
    if (_carAnnotation)
        [_mapView removeAnnotation:_carAnnotation];
    if (GL(save_location_lat) && GL(save_location_log)) {
        _locationCarButton.hidden = NO;
        
        _carAnnotation = [[BMKPointAnnotation alloc] init];
        _carAnnotation.coordinate = CLLocationCoordinate2DMake([GL(save_location_lat) doubleValue], [GL(save_location_log) doubleValue]);
        _carAnnotation.title = @"";
        [_mapView addAnnotation:_carAnnotation];
    } else {
        _locationCarButton.hidden = YES;
    }
    
    
    //paopao内容可能会变化，目前有两种状态 已拍照和未拍照
    if (_locationAnnotation && _locationAnnotationView.selected == NO) {
        BMKAnnotationView* annotationView = [_mapView viewForAnnotation:_locationAnnotation];
        annotationView.paopaoView = self.locationPaopaoView;
    }
    
}

#pragma mark THolidayViewDelegate

- (void)holidayShareTouched {
    _holidayView.hidden = YES;
    
    [self requestShareInfoWithIsHoliday:YES];
}

- (void)requestShareInfoWithIsHoliday:(BOOL)isHoliday {
    NSString* action = isHoliday ? @"hbparms" : @"obparms";
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=%@&mobile=%@", action, [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    if (!isHoliday) {
        apiPath = [apiPath stringByAppendingFormat:@"&bid=%@", _boundsId];
    }
    _shareInfoRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    
    CVAPIRequestModel* model = [[CVAPIRequestModel alloc] init];
    [model sendRequest:_shareInfoRequest completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        TShareItem* item = [TShareItem getItemFromDic:result];
        
        TShareView* shareView = [[TShareView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height)];
        shareView.delegate = self;
        shareView.item = item;
        [self.view addSubview:shareView];
        shareView.centerView.top = shareView.bottom;
        [UIView animateWithDuration:0.3 animations:^{
            shareView.centerView.top = shareView.height - 170;
        }];
    }];
}

- (void)holidayCloseTouched {
    _holidayView.hidden = YES;
}

#pragma mark TShareViewDelegate

- (void)shareViewTouched:(TShareView *)shareView index:(NSInteger)index {
    
    TShareItem* item = shareView.item;
    int a = 0;
    NSLog(@"%d", a);
    [[SDWebImageDownloader sharedDownloader] downloadImageWithURL:[NSURL URLWithString:[TAPIUtility getNetworkWithUrl:item.imgurl]] options:0 progress:nil completed:^(UIImage *image, NSData *data, NSError *error, BOOL finished) {
        if (image) {
            //自定义事件
            [MobClick event:@"8"];
//            192.168.10.240
            WXWebpageObject* webObj = [WXWebpageObject object];
            webObj.webpageUrl = [TAPIUtility getNetworkWithUrl:item.url];
            if (_boundsId) {
                webObj.webpageUrl = [webObj.webpageUrl stringByAppendingString:[NSString stringWithFormat:@"&id=%@", _boundsId]];
            }
            NSLog(@"weburl----%@", webObj.webpageUrl);
            
            WXMediaMessage* message = [WXMediaMessage message];
            message.title = item.title;
            message.description = item.descri;
            message.mediaObject = webObj;
            UIImage* thumbImg = [TAPIUtility ajustImage:image size:CGSizeMake(30, 30)];
            message.thumbData = UIImageJPEGRepresentation(thumbImg, 1);
            
            SendMessageToWXReq* req = [[SendMessageToWXReq alloc] init];
            if (index == 0)
                req.scene = WXSceneSession;//聊天界面
            else if (index == 1)
                req.scene = WXSceneTimeline;
            req.bText = NO;
            req.message = message;
            [WXApi sendReq:req];
        } else {
            NSLog(@"+++++++NO image");
        }
    }];
}

#pragma mark keyboard
- (void)willShowKeyboard:(NSNotification*)notification {
    NSDictionary *notificationInfo = [notification userInfo];
    
    // Get the end frame of the keyboard in screen coordinates.
    CGRect finalKeyboardFrame = [[notificationInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
    
    // Convert the finalKeyboardFrame to view coordinates to take into account any rotation
    // factors applied to the window’s contents as a result of interface orientation changes.
    finalKeyboardFrame = [self.currentOrderView convertRect:finalKeyboardFrame fromView:_currentOrderView.window];
    
    
    // Get the animation curve and duration
    UIViewAnimationCurve animationCurve = (UIViewAnimationCurve) [[notificationInfo objectForKey:UIKeyboardAnimationCurveUserInfoKey] integerValue];
    NSTimeInterval animationDuration = [[notificationInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] doubleValue];
    
    // Animate view size synchronously with the appearance of the keyboard.
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationDuration:animationDuration];
    [UIView setAnimationCurve:animationCurve];
    [UIView setAnimationBeginsFromCurrentState:YES];
//    CGFloat height = _currentOrderView.bottom - finalKeyboardFrame.origin.y;
//    self.currentOrderView.blurView.bottom = MIN(([UIScreen mainScreen].bounds.size.height + 215)/2, finalKeyboardFrame.origin.y);
    [UIView commitAnimations];
    
}

- (void)modelDidFailWithError:(NSError *)error model:(CVAPIRequestModel *)model request:(CVAPIRequest *)request {
    
//    if ([[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
//        [[TShakeView getInstance] fail];
//    }
    
    if (model == _currentOrderModel) {
        if (_currentOrderView)
            [self.currentOrderView.loaddingView stopAnimating];
    } else if (model == _requestAllParksModel) {
        //更新北京全部的空闲车位数
    }
}

#pragma mark TParkInfoViewDelegate

- (void)parkInfoViewWithItem:(TParkItem *)item text:(NSString *)text {
    if ([text isEqualToString:@"详情"]) {
        //自定义事件
        [MobClick event:@"9"];

        TParkDetailController* vc = [[TParkDetailController alloc] init];
        vc.parkId = item.parkId;
        vc.parkName = item.name;
        [self.navigationController pushViewController:vc animated:YES];
    } else if ([text isEqualToString:@"到这去"]) {
        NSString* longitude = item.lng;
        NSString* latitude = item.lat;
        NSLog(@"%@---%@", latitude, longitude);
        if (!longitude) {
            [TAPIUtility alertMessage:@"定位失败" success:NO toViewController:self];
            NSLog(@"longtude为空");
            return;
        }
        NSMutableArray *nodesArray = [[NSMutableArray alloc]initWithCapacity:2];
        //起点 传入的是原始的经纬度坐标，若使用的是百度地图坐标，可以使用BNTools类进行坐标转化
        BNRoutePlanNode *startNode = [[BNRoutePlanNode alloc] init];
        startNode.pos = [[BNPosition alloc] init];
        startNode.pos.x = _coordinate.longitude;
        startNode.pos.y = _coordinate.latitude;
        startNode.pos.eType = BNCoordinate_BaiduMapSDK;
        [nodesArray addObject:startNode];
        
        //终点
        BNRoutePlanNode *endNode = [[BNRoutePlanNode alloc] init];
        endNode.pos = [[BNPosition alloc] init];
        endNode.pos.x = [longitude doubleValue];
        endNode.pos.y = [latitude doubleValue];
        endNode.pos.eType = BNCoordinate_BaiduMapSDK;
        [nodesArray addObject:endNode];
        
        [BNCoreServices_RoutePlan startNaviRoutePlan:BNRoutePlanMode_Recommend naviNodes:nodesArray time:nil delegete:self userInfo:nil];
        
        //自定义事件
        [MobClick event:@"3"];
    } else if ([text isEqualToString:@"付车费"]) {
        if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
            [TAPIUtility alertMessage:@"请先登录哦~"];
            return;
        }
        //=======先检查是否有未结算的订单
        NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=currentorder&mobile=%@&comid=%@",[[NSUserDefaults standardUserDefaults] objectForKey:save_phone], item.parkId];
        _zhifuRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
        _zhifuRequest.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
        
        [self.model sendRequest:_zhifuRequest completion:^(NSDictionary *result, NSError *error) {
            if (!result)
                return;
            if ([result count]) {
                NSString* state = [result objectForKey:@"state"];
                if ([state isEqualToString:@"0"]) {
                    [TAPIUtility alertMessage:@"请等待收费员结算您的订单!"];
                }
            } else {
                TCollectorsListViewController* vc = [[TCollectorsListViewController alloc] init];
                vc.parkId = item.parkId;
                vc.parkName = item.name;
                [self.navigationController pushViewController:vc animated:YES];
            }
        }];
    }
}

#pragma mark TSearchViewDelegate

- (void)searchViewClicked:(NSInteger)index {
    //8 9 10 需要先登陆
    if ((index == 2 || index == 10 || index == 8 || index == 9) && ![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        TLoginViewController* vc = [[TLoginViewController alloc] init];
        [self.navigationController pushViewController:vc animated:YES];
        return;
    }
    
    if (index == 0) {
        
        [self removedAnnotations:_allParkItems];
        [self addAnnotations:_payParkItems];
        _onlyShowPayParks = YES;
        
    } else if (index == 1) {
        
        [self removedAnnotations:_allParkItems];
        [self addAnnotations:_allParkItems];
        _onlyShowPayParks = NO;
        
    } else if (index == 2) {
        TUploadParkViewController* vc = [[TUploadParkViewController alloc] init];
        [self.navigationController pushViewController:vc animated:YES];
        
    } else if (index == 3) {
        [self initNavigationItems:YES];
        
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, NSEC_PER_SEC * 0.2),
                       dispatch_get_main_queue(), ^{
                           [_searchBar becomeFirstResponder];
                       });
    } else if (index == 5) {
        TTicketHelpController* vc = [[TTicketHelpController alloc] initWithName:@"上传车场好处" url:[TAPIUtility getNetworkWithUrl:@"carinter.do?action=upfine"]];
        [self.navigationController pushViewController:vc animated:YES];
    } else if (index == 6) {
        //去充值
        TRechargeViewController* vc = [[TRechargeViewController alloc] init];
        vc.lackMoney = _lackMoney;//欠费的金额，充值必须大于等于这个值
        [self.navigationController pushViewController:vc animated:YES];
        
    } else if (index == 7) {
        [UIView animateWithDuration:0.2 animations:^{
            self.bottomView.top += (_bottomView.addButton.tag == 1 ? -100 : 100);
            [self.bottomView resetOrginTop];
            [self viewDidLayoutSubviews];
        } completion:nil];
        
    } else if (index == 8) {
        TMyRedPackageViewController* vc = [[TMyRedPackageViewController alloc] init];
        [self.navigationController pushViewController:vc animated:YES];
        
    } else if (index == 9) {
        TTicketGameViewController* vc = [[TTicketGameViewController alloc] initWithName:@"打灰机" url:[TAPIUtility getNetworkWithUrl:[@"flygame.do?action=pregame&mobile=" stringByAppendingString:[[NSUserDefaults standardUserDefaults] objectForKey:save_phone]]]];
        vc.goBackAfterShare = YES;
        [self.navigationController pushViewController:vc animated:YES];
        
    } else if (index == 10) {
        TCollectorsListViewController* vc = [[TCollectorsListViewController alloc] init];
        vc.mode = TCollectorsListMode_recent;
        
        [self.navigationController pushViewController:vc animated:YES];
    }
}

#pragma mark UIImagePickerControllerDelegate 

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
    UIImage* image = (UIImage*) [info objectForKey:UIImagePickerControllerOriginalImage];
    if (picker != _locationImagePicker) {
        //下面这三行 应该不会用到
        [picker dismissViewControllerAnimated:YES completion:nil];
        [self.currentOrderView show:YES];
        [self.currentOrderView captureImage:image];
    } else {
        [picker dismissViewControllerAnimated:NO completion:^{
            //先清除所有信息
            [TAPIUtility saveLocationImage:nil];
            SL(nil, save_location_level);
            SL(nil, save_location_time);
            SL(nil, save_location_note);
            SL(nil, save_location_lat);
            SL(nil, save_location_log);
            SL(nil, save_location_right);
            
            
            NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
            formatter.dateFormat = @"yyyy-MM-dd HH:mm";
            NSString* time = [formatter stringFromDate:[NSDate new]];
            
            //保存在本地
            [TAPIUtility saveLocationImage:image];
            SL(time, save_location_time);
            SL(@(_coordinate.latitude), save_location_lat);
            SL(@(_coordinate.longitude), save_location_log);
            
            TLocationInfoViewController* vc = [[TLocationInfoViewController alloc] init];
            [self.navigationController pushViewController:vc animated:YES];
        }];
    }
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    //[self showBars:YES animated:NO];
    [picker dismissViewControllerAnimated:YES completion:nil];
    if (picker != _locationImagePicker) {
        [self.currentOrderView show:YES];
    } else {
        
    }
}

#pragma mark BMKGeoCodeSearchDelegate

//切换到对应的城市

- (void)switchCity {
    if (_checkCityName) {
        BMKGeoCodeSearch* search = [[BMKGeoCodeSearch alloc] init];
        BMKReverseGeoCodeOption* option = [[BMKReverseGeoCodeOption alloc] init];
        option.reverseGeoPoint = _coordinate;
        search.delegate = self;
        [search reverseGeoCode:option];
        
        _checkCityName = NO;
    }
}
- (void)onGetReverseGeoCodeResult:(BMKGeoCodeSearch *)searcher result:(BMKReverseGeoCodeResult *)result errorCode:(BMKSearchErrorCode)error {
    _cityName = result.addressDetail.city;
}

#pragma mark public

- (void)updateOrderState:(int)state {
    //state 0 没有， 1 有但没结算 2结算待支付
    UIImage* image = [UIImage imageNamed:[@[@"search_order_green.png", @"search_order_yellow.png", @"search_order_red.png"] objectAtIndex:state]];
    [_queryCurrOrder setImage:image forState:UIControlStateNormal];
}

- (void)showCollectorRedPackageView:(TScanParkRedPackageItem *)item {
    TScanParkRedPackageView* redView = [[TScanParkRedPackageView alloc] initWithFrame:[UIScreen mainScreen].bounds];
    redView.item = item;
    redView.completeHandle = ^{
        TTicketViewController* ticketVc = [[TTicketViewController alloc] init];
        [self.navigationController pushViewController:ticketVc animated:YES];
    };
    [redView show:YES];
    return;
}
@end
