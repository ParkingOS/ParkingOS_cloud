//
//  TUploadParkViewController.m
//  TingCheBao_user
//
//  Created by apple on 15/3/26.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TLocationViewController.h"
#import <BaiduMapAPI/BMKMapView.h>
#import <BaiduMapAPI/BMKLocationService.h>
#import "TAPIUtility.h"
#import "UIKeyboardViewController.h"
#import <BaiduMapAPI/BMKGeocodeSearch.h>
#import <BaiduMapAPI/BMKPolyline.h>
#import <BaiduMapAPI/BMKRouteSearch.h>
#import <BaiduMapAPI/BMKPolylineView.h>
#import "CVAPIRequestModel.h"
#import "TAPIUtility.h"

@interface TLocationViewController ()<BMKMapViewDelegate, BMKLocationServiceDelegate, BMKGeoCodeSearchDelegate, MBProgressHUDDelegate, BMKRouteSearchDelegate>

//UI
@property(nonatomic, retain) BMKMapView* mapView;
@property(nonatomic, retain) UIView* chooseView;
@property(nonatomic, retain) UIImageView* chooseImgView;
@property(nonatomic, retain) UIImageView* borderImgView;
@property(nonatomic, retain) UILabel* promptLabel;

@property(nonatomic, retain) UIView* locationView;
@property(nonatomic, retain) UIImageView* locationImgView;
@property(nonatomic, retain) UILabel* addressLabel;
@property(nonatomic, retain) UILabel* nameHeadLabel;
@property(nonatomic, retain) UITextField* nameTextField;

@property(nonatomic, retain) UIButton* doneButton;

//-----
@property(nonatomic, retain) BMKLocationService* locationService;
@property(nonatomic, retain) BMKPointAnnotation* locationAnnotation;
@property(nonatomic, assign) BOOL firstLocation;

@property(nonatomic, assign) CLLocationCoordinate2D coordiante;
@property(nonatomic, assign) BOOL firstLoad;
@property(nonatomic, retain) BMKGeoCodeSearch* mapSearch;

@end

@implementation TLocationViewController

- (id)initWithMode:(location_mode)mode {
    if (self = [super init]) {
        
        _mode = mode;
        
        _locationService = [[BMKLocationService alloc] init];
        _locationService.delegate = self;
        
        _mapView = [[BMKMapView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height - 100)];
        _mapView.showsUserLocation = NO;
        _mapView.userTrackingMode = BMKUserTrackingModeNone;
        _mapView.maxZoomLevel = 19;
        _mapView.delegate = self;
        
        _chooseView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 165, 54)];
        _chooseView.hidden = YES;
        
        _borderImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"qipao"]];
        _borderImgView.frame = CGRectMake(0, 0, 165, 30);
        
        _promptLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, _chooseView.width, 30)];
        _promptLabel.text = @"拖动地图,选择车场位置";
        _promptLabel.font = [UIFont systemFontOfSize:14];
        _promptLabel.textAlignment = NSTextAlignmentCenter;
        
        _chooseImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"park_normal_green_select"]];
        _chooseImgView.frame = CGRectMake((_chooseView.width - 20)/2, 30, 20, 24);
        
        [_chooseView addSubview:_borderImgView];
        [_chooseView addSubview:_promptLabel];
        [_chooseView addSubview:_chooseImgView];
        
        //------locationView------
        _locationView = [[UIView alloc] initWithFrame:CGRectMake(0, _mapView.bottom - 30, self.view.width, 30)];
        _locationView.backgroundColor = [UIColor whiteColor];
        _locationView.alpha = 0.9;
        
        _locationImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"gray_location.png"]];
        _locationImgView.frame = CGRectMake(6, 7, 12, 16);
        
        _addressLabel = [[UILabel alloc] initWithFrame:CGRectMake(20, 0, self.locationView.width, 30)];
        _addressLabel.text = @"定位中...";
        _addressLabel.textColor = green_color;
        _addressLabel.font = [UIFont systemFontOfSize:14];
        
        [self.locationView addSubview:_locationImgView];
        [self.locationView addSubview:_addressLabel];
        
        //------locationView---end-----
        
        
        _doneButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_doneButton setTitle:@"完成" forState:UIControlStateNormal];
        [_doneButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_doneButton setTitleColor:green_color forState:UIControlStateHighlighted];
        [_doneButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        [_doneButton setBackgroundImage:[TAPIUtility imageWithColor:[UIColor whiteColor]] forState:UIControlStateHighlighted];
        [_doneButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _doneButton.frame = CGRectMake(10, self.view.bottom - 70, self.view.width - 2*10, 40);
        _doneButton.layer.cornerRadius = 5;
        _doneButton.clipsToBounds = YES;
        
        [self.view addSubview:_mapView];
        
        //mode_show只显示地图一个控件
        if (_mode == location_mode_choose) {
            [self.view addSubview:_chooseView];
            [self.view addSubview:_locationView];
            [self.view addSubview:_doneButton];
        } else {
            _mapView.height = self.view.height;
        }
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(237, 237, 237);
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    self.titleView.text = (_mode == location_mode_choose) ? @"选择停车位置" : @"停车位置";
    
    [_mapView viewWillAppear];
    _mapView.delegate = self;
    _locationService.delegate = self;
    [_locationService startUserLocationService];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    [_locationService stopUserLocationService];
    [_mapView viewWillDisappear];
    _mapView.delegate = nil;
    _mapSearch.delegate = nil;
}

- (void)showSearchWarking {
    BMKPlanNode *fromeNode = [[BMKPlanNode alloc] init];
    fromeNode.pt = _coordiante;
    
    BMKPlanNode *toNode = [[BMKPlanNode alloc] init];
    toNode.pt = CLLocationCoordinate2DMake([GL(save_location_lat) doubleValue], [GL(save_location_log) doubleValue]);
    BMKRouteSearch* search = [[BMKRouteSearch alloc] init];
    search.delegate = self;
    BMKWalkingRoutePlanOption* option = [[BMKWalkingRoutePlanOption alloc] init];
    option.from = fromeNode;
    option.to = toNode;
    [search walkingSearch:option];
}

- (void)mapView:(BMKMapView *)mapView regionDidChangeAnimated:(BOOL)animated {
    if (_mode == location_mode_choose) {
        _mapSearch = [[BMKGeoCodeSearch alloc] init];
        BMKReverseGeoCodeOption* option = [[BMKReverseGeoCodeOption alloc] init];
        option.reverseGeoPoint = _mapView.centerCoordinate;
        _mapSearch.delegate = self;
        [_mapSearch reverseGeoCode:option];
    }
}

#pragma mark BMKGeoCodeSearchDelegate

- (void)onGetReverseGeoCodeResult:(BMKGeoCodeSearch *)searcher result:(BMKReverseGeoCodeResult *)result errorCode:(BMKSearchErrorCode)error {
    _addressLabel.text = result.address;
}

#pragma mark private

//事件
- (void)buttonTouched:(UIButton*)button {
    if (button == _doneButton) {
        [self requestSaveInfo];
    }
}

//保存数据
- (void)requestSaveInfo {
    if (_mapView.centerCoordinate.longitude == 0 || _mapView.centerCoordinate.latitude == 0) {
        [TAPIUtility alertMessage:@"无法取得您的位置信息" success:NO toViewController:nil];
        return;
    }
    SL(@(_mapView.centerCoordinate.latitude), save_location_lat);
    SL(@(_mapView.centerCoordinate.longitude), save_location_log);
    SL(@"1", save_location_right);
    
    [TAPIUtility alertMessage:@"设置成功" success:YES toViewController:self];
}


#pragma mark - BMKLocationServiceDelegate
//定位
- (void)didUpdateBMKUserLocation:(BMKUserLocation *)userLocation {
    if (userLocation.location.coordinate.latitude == 0 || userLocation.location.coordinate.longitude == 0) {
        NSLog(@"定位失败");
        return;
    }
    
    [_mapView updateLocationData:userLocation];
    _coordiante = userLocation.location.coordinate;
    
    if (!_firstLocation) {
        _firstLocation = YES;
        
        [_mapView updateLocationData:userLocation];
        [_mapView setCenterCoordinate:userLocation.location.coordinate animated:NO];
        _mapView.zoomLevel = 18;
        
        if (_mode == location_mode_choose) {
            //让图标居中
            _chooseView.center = CGPointMake(_mapView.width/2, _mapView.height/2 - _chooseView.height/2);
            _chooseView.hidden = NO;
        } else {
            //显示步行路线
            [self showSearchWarking];
        }
    }
    
    if (_mode == location_mode_show) {
        //定位图标
        if (_locationAnnotation)
            [_mapView removeAnnotation:_locationAnnotation];
        _locationAnnotation = [[BMKPointAnnotation alloc] init];
        _locationAnnotation.coordinate = _coordiante;
        _locationAnnotation.title = @"";
        [_mapView addAnnotation:_locationAnnotation];
        BMKAnnotationView* annotionView = [_mapView viewForAnnotation:_locationAnnotation];
        //这行必须加，不然不能在最前面
        [annotionView.superview bringSubviewToFront:annotionView];
    }
}

#pragma mark BMKRouteSearchDelegate

//算路
- (void)onGetWalkingRouteResult:(BMKRouteSearch *)searcher result:(BMKWalkingRouteResult *)result errorCode:(BMKSearchErrorCode)error {
    BMKWalkingRouteLine* plan = (BMKWalkingRouteLine*)[result.routes objectAtIndex:0];
    NSInteger size = [plan.steps count];
    int planPointCounts = 0;
    for (int i = 0; i < size; i++) {
        BMKWalkingStep* transitStep = [plan.steps objectAtIndex:i];
        if(i==0){
            BMKPointAnnotation* item = [[BMKPointAnnotation alloc]init];
            item.coordinate = plan.starting.location;
            item.title = @"起点";
            [_mapView addAnnotation:item]; // 添加起点标注
            
        }else if(i==size-1){
            BMKPointAnnotation* item = [[BMKPointAnnotation alloc]init];
            item.coordinate = plan.terminal.location;
            item.title = @"终点";
            [_mapView addAnnotation:item]; // 添加起点标注
            
        }
        //添加annotation节点
        BMKPointAnnotation* item = [[BMKPointAnnotation alloc]init];
        item.coordinate = transitStep.entrace.location;
        item.title = [NSString stringWithFormat:@"direction%d", transitStep.direction * 30 - 90];
        [_mapView addAnnotation:item];
        
        //轨迹点总数累计
        planPointCounts += transitStep.pointsCount;
    }
    
    //轨迹点
    BMKMapPoint* temppoints = malloc(sizeof(CLLocationCoordinate2D) * planPointCounts);
//    BMKMapPoint * temppoints = new BMKMapPoint[planPointCounts];
    int i = 0;
    for (int j = 0; j < size; j++) {
        BMKWalkingStep* transitStep = [plan.steps objectAtIndex:j];
        int k=0;
        for(k=0;k<transitStep.pointsCount;k++) {
            temppoints[i].x = transitStep.points[k].x;
            temppoints[i].y = transitStep.points[k].y;
            i++;
        }
    }
    // 通过points构建BMKPolyline
    BMKPolyline* polyLine = [BMKPolyline polylineWithPoints:temppoints count:planPointCounts];
    [_mapView addOverlay:polyLine]; // 添加路线overlay
//    delete []temppoints;
}

//绘制路线
- (BMKOverlayView*)mapView:(BMKMapView *)mapView viewForOverlay:(id<BMKOverlay>)overlay {
    if ([overlay isKindOfClass:[BMKPolyline class]]) {
        BMKPolylineView* polylineView = [[BMKPolylineView alloc] initWithOverlay:overlay];
        polylineView.fillColor = [[UIColor cyanColor] colorWithAlphaComponent:1];
        polylineView.strokeColor = RGBCOLOR(87, 80, 255);
        polylineView.lineWidth = 5.0;
        return polylineView;
    } 
    return nil;
}

//绘制anntation
- (BMKAnnotationView*)mapView:(BMKMapView *)mapView viewForAnnotation:(id<BMKAnnotation>)annotation {
    
    BMKAnnotationView* annotationView = [[BMKAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"locationAnnotation"];
    UIImage* img = nil;
    if ([annotation.title isEqualToString:@"起点"]) {
        img = [UIImage imageNamed:@"location_start.png"];
    } else if ([annotation.title isEqualToString:@"终点"]) {
        img = [UIImage imageNamed:@"location_end.png"];
    } else if ([annotation.title rangeOfString:@"direction"].length > 0) {
        img = [UIImage imageNamed:@"direction.png"];
        int degree = [[annotation.title substringFromIndex:9] intValue];
        NSLog(@"%d", degree);
        img = [TAPIUtility rotateImage:img degree:degree];
    } else {
        img = [UIImage imageNamed:@"blue_circle.png"];
    }
    annotationView.image = img;
    
    if ([annotation.title isEqualToString:@"起点"] || [annotation.title isEqualToString:@"终点"]) {
        annotationView.centerOffset = CGPointMake(0, -1*annotationView.height/2);
    }
    
    return annotationView;
}

#pragma mark MBProgressHUDDelegate

- (void)hudWasHidden:(MBProgressHUD *)hud {
    [self.navigationController popViewControllerAnimated:YES];
}

@end
