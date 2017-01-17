//
//  TUploadParkViewController.m
//  TingCheBao_user
//
//  Created by apple on 15/3/26.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TCheckParkViewController.h"
#import <BaiduMapAPI/BMKMapView.h>
#import <BaiduMapAPI/BMKLocationService.h>
#import "TAPIUtility.h"
#import "UIKeyboardViewController.h"
#import <BaiduMapAPI/BMKGeocodeSearch.h>
#import "TTicketHelpController.h"
#import "CVAPIRequestModel.h"
#import "TAPIUtility.h"

@interface TCheckParkViewController()<BMKMapViewDelegate, BMKLocationServiceDelegate, BMKGeoCodeSearchDelegate, MBProgressHUDDelegate>

//UI
@property(nonatomic, retain) BMKMapView* mapView;
@property(nonatomic, retain) UIView* chooseView;
@property(nonatomic, retain) UIImageView* chooseImgView;
@property(nonatomic, retain) UIImageView* borderImgView;
@property(nonatomic, retain) UILabel* promptLabel;

@property(nonatomic, retain) UIView* locationView;
@property(nonatomic, retain) UIImageView* locationImgView;
@property(nonatomic, retain) UILabel* addressLabel;
//@property(nonatomic, retain) UILabel* distanceLabel;
@property(nonatomic, retain) UILabel* nameHeadLabel;
@property(nonatomic, retain) UILabel* locationLabel;

@property(nonatomic, retain) UIView* white1View;
@property(nonatomic, retain) UIButton* moreButton;

@property(nonatomic, retain) UIView* white2View;
@property(nonatomic, retain) UILabel* payLabel;
@property(nonatomic, retain) UIButton* payButton;
@property(nonatomic, retain) UILabel* descriptionLabel;
@property(nonatomic, retain) UITextView* descriptionTextView;

@property(nonatomic, retain) UIButton* leftButton;
@property(nonatomic, retain) UIButton* rightButton;
@property(nonatomic, retain) UIButton* doneButton;

@property(nonatomic, retain) UIButton* img1Button;
@property(nonatomic, retain) UIButton* img2Button;
@property(nonatomic, retain) UIButton* img3Button;
@property(nonatomic, retain) UIButton* img4Button;


//-----
@property(nonatomic, retain) BMKLocationService* locationService;
@property(nonatomic, retain) BMKPointAnnotation* locationAnnotation;
@property(nonatomic, assign) BOOL firstLocation;

@property(nonatomic, retain) UIKeyboardViewController* keyboardViewController;
@property(nonatomic, retain) CVAPIRequest* request;

@property(nonatomic, retain) NSMutableArray* buttons;
@property(nonatomic, retain) NSDictionary* resultDic;
@property(nonatomic, retain) NSMutableArray* parkIds;
@property(nonatomic, assign) CLLocationCoordinate2D coordinate;
@end

@implementation TCheckParkViewController

- (id)init {
    if (self = [super init]) {
        
        _buttons = [NSMutableArray array];
        _resultDic = [NSDictionary dictionary];
        
        _locationService = [[BMKLocationService alloc] init];
        _locationService.delegate = self;
        
        _mapView = [[BMKMapView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height - 300)];
        _mapView.showsUserLocation = NO;
        _mapView.userTrackingMode = BMKUserTrackingModeNone;
        _mapView.maxZoomLevel = 19;
        _mapView.delegate = self;
        
        _chooseView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 165, 54)];
        _chooseView.hidden = YES;
        
        _borderImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"qipao"]];
        _borderImgView.frame = CGRectMake(0, 0, 165, 30);
        
        _promptLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, _chooseView.width, 30)];
        _promptLabel.text = @"名称";
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
        
        _white1View = [[UIView alloc] initWithFrame:CGRectMake(0, _locationView.bottom + 10, self.view.width, 90)];
        _white1View.backgroundColor = [UIColor whiteColor];
        
        _nameHeadLabel = [[UILabel alloc] initWithFrame:CGRectMake(20, 0, 100, 30)];
        _nameHeadLabel.text = @"车场名称准确";
        _nameHeadLabel.font = [UIFont systemFontOfSize:14];
        
        _img1Button = [self createImageButtonWithFrame:CGRectMake(self.view.width -  40, _nameHeadLabel.top, 40, 30)];
        
        _locationLabel = [[UILabel alloc] initWithFrame:CGRectMake(20, _nameHeadLabel.bottom, 100, 30)];
        _locationLabel.text = @"位置标注准确";
        _locationLabel.font = [UIFont systemFontOfSize:14];
        
        _img2Button = [self createImageButtonWithFrame:CGRectMake(self.view.width - 40, _locationLabel.top, 40, 30)];
        
        _payLabel = [[UILabel alloc] initWithFrame:CGRectMake(20, _locationLabel.bottom, 200, 30)];
        _payLabel.text = @"是否收费车场:";
        _payLabel.font = [UIFont systemFontOfSize:14];
        
        _img3Button = [self createImageButtonWithFrame:CGRectMake(self.view.width - 40, _payLabel.top, 40, 30)];
        
        [_white1View addSubview:_nameHeadLabel];
        [_white1View addSubview:_img1Button];
        [_white1View addSubview:_locationLabel];
        [_white1View addSubview:_img2Button];
        [_white1View addSubview:_payLabel];
        [_white1View addSubview:_img3Button];
        
        
        
        
        _moreButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _moreButton.tag = 200;
        
        UIImageView* imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ic_pull_down.png"]];
        imgView.frame = CGRectMake(6, 9, 12, 12);
        imgView.tag = 91;
        
        UILabel* label = [[UILabel alloc] initWithFrame:CGRectMake(20, 0, 80, 30)];
        label.text = @"更多信息";
        label.font = [UIFont systemFontOfSize:14];
        label.textColor = green_color;
        
        [_moreButton addSubview:imgView];
        [_moreButton addSubview:label];
        
        [_moreButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _moreButton.frame = CGRectMake(0, _white1View.bottom, 100, 30);
        
        
        
        //white2View---start
        
        _white2View = [[UIView alloc] initWithFrame:CGRectMake(0, _moreButton.bottom, self.view.width, 85)];
        _white2View.backgroundColor = [UIColor whiteColor];
        _white2View.hidden = YES;
        
        _descriptionLabel = [[UILabel alloc] initWithFrame:CGRectMake(20, 0, 120, 30)];
        _descriptionLabel.text = @"车场信息描述准确:";
        _descriptionLabel.font = [UIFont systemFontOfSize:14];
        
        _descriptionTextView = [[UITextView alloc] initWithFrame:CGRectMake(_descriptionLabel.right, 8, self.view.width - _descriptionLabel.width - 2*10 - 40, 70)];
        _descriptionTextView.font = [UIFont systemFontOfSize:14];
        _descriptionTextView.layer.cornerRadius = 3;
        _descriptionTextView.layer.borderColor = self.view.backgroundColor.CGColor;
        _descriptionTextView.layer.borderWidth = 1;
        _descriptionTextView.userInteractionEnabled = NO;
        
        _img4Button = [self createImageButtonWithFrame:CGRectMake(self.view.width - 40, _descriptionLabel.top, 40, 30)];
        
        [_white2View addSubview:_descriptionLabel];
        [_white2View addSubview:_descriptionTextView];
        [_white2View addSubview:_img4Button];
        
        //white2View---end
        
        _leftButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_leftButton setTitle:@"查看规则" forState:UIControlStateNormal];
        [_leftButton setTitleColor:green_color forState:UIControlStateNormal];
        [_leftButton setContentHorizontalAlignment:UIControlContentHorizontalAlignmentLeft];
        _leftButton.titleLabel.font = [UIFont systemFontOfSize:12];
        [_leftButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _leftButton.frame = CGRectMake(10, self.view.bottom - 30, 100, 30);
        
        _doneButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_doneButton setTitle:@"确认审核" forState:UIControlStateNormal];
        [_doneButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_doneButton setTitleColor:green_color forState:UIControlStateHighlighted];
        [_doneButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        [_doneButton setBackgroundImage:[TAPIUtility imageWithColor:[UIColor whiteColor]] forState:UIControlStateHighlighted];
        [_doneButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _doneButton.frame = CGRectMake(10, self.view.bottom - 70, self.view.width - 2*10, 40);
        _doneButton.layer.cornerRadius = 5;
        _doneButton.clipsToBounds = YES;
        
        _rightButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_rightButton setTitle:@"审核下一个" forState:UIControlStateNormal];
        [_rightButton setTitleColor:green_color forState:UIControlStateNormal];
        _rightButton.titleLabel.font = [UIFont systemFontOfSize:12];
        [_rightButton setContentHorizontalAlignment:UIControlContentHorizontalAlignmentRight];
        [_rightButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _rightButton.frame = CGRectMake(self.view.width - 10 - 100, self.view.bottom - 30, 100, 30);
        
        [self.view addSubview:_mapView];
        [self.view addSubview:_chooseView];
        [self.view addSubview:_locationView];
        [self.view addSubview:_white1View];
        [self.view addSubview:_moreButton];
        [self.view addSubview:_white2View];
        [self.view addSubview:_doneButton];
        [self.view addSubview:_leftButton];
        [self.view addSubview:_rightButton];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(237, 237, 237);
    self.titleView.text = @"审核停车场";
    _parkIds = [NSMutableArray array];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    [_mapView viewWillAppear];
    _mapView.delegate = self;
    _locationService.delegate = self;
    [_locationService startUserLocationService];
    
    //键盘弹出的视图
    _keyboardViewController = [[UIKeyboardViewController alloc] initWithControllerDelegate:self];
    [_keyboardViewController addToolbarToKeyboard];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    [_locationService stopUserLocationService];
    [_mapView viewWillDisappear];
    _mapView.delegate = nil;
    
    [_request cancel];
    
    //    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark private

- (UIButton*)createImageButtonWithFrame:(CGRect)frame {
    UIButton* button = [UIButton buttonWithType:UIButtonTypeCustom];
    button.tag = 100;
    [button addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
    button.frame = frame;
    UIImageView* selectImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"not_selected.png"]];
    selectImgView.frame = CGRectMake(10, 7, 15, 15);
    selectImgView.tag = 91;
    [button addSubview:selectImgView];
    
    [_buttons addObject:button];
    return button;
}

- (void)buttonTouched:(UIButton*)button {
    if ([_buttons containsObject:button]) {
        
        UIImageView* imgView = (UIImageView*)[button viewWithTag:91];
        imgView.image = [UIImage imageNamed:button.tag == 100 ? @"rechage_selected.png" : @"not_selected.png"];
        button.tag = button.tag == 100 ? 101 : 100;
        
    } else if (button == _doneButton) {
        
        [self requestSaveInfo];
        
    } else if (button == _leftButton) {
        
        TTicketHelpController* vc = [[TTicketHelpController alloc] initWithName:@"审核规则" url:[TAPIUtility getNetworkWithUrl:@"carinter.do?action=verifyrule"]];
        [self.navigationController pushViewController:vc animated:YES];
        
    } else if (button == _moreButton) {
        
        UIImageView* imgView = (UIImageView*)[_moreButton viewWithTag:91];
        imgView.image = [UIImage imageNamed:_moreButton.tag == 200 ? @"ic_pull_up.png" : @"ic_pull_down.png"];
        _white2View.hidden = _moreButton.tag == 200 ? NO : YES;
        _moreButton.tag = _moreButton.tag == 200 ? 201 : 200;
        
    } else if (button == _rightButton) {
        [self requestInfoWithLng:_coordinate.longitude lat:_coordinate.latitude];
    }
}

- (void)requestInfoWithLng:(float)lng lat:(float)lat {
    if (lng == 0 || lat == 0) {
        [TAPIUtility alertMessage:@"未能获得您的位置信息，请求失败"];
        return;
    } else if ([_parkIds count] > 30) {
        [TAPIUtility alertMessage:@"您的审核操作太过频繁，请稍后再试"];
        return;
    }
    
    NSMutableString* ids = [NSMutableString string];
    for (int i = 0; i < [_parkIds count]; i++) {
        if (i > 0) {
            [ids appendString:@","];
        }
        [ids appendString:[_parkIds objectAtIndex:i]];
    }
    NSString* apiPath = [NSString stringWithFormat:@"carinter.do?action=preverifypark&mobile=%@&lng=%lf&lat=%lf&ids=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone], lng, lat, ids];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
//        {
//            desc = "\U7ecf\U5386\U8fc7\U7d27\U5bc6\U6478\U6478";
//            id = 3654;
//            lat = "40.043947";
//            lng = "116.314052";
//            name = "\U7d27\U5bc6\U6478\U6478";
//            type = 1
//        }
//        {"id":"2232","company_name":"科技","resume":"bbb","longitude":"116.317564","latitude":"40.043024"}
        if ([[result objectForKey:@"id"] isEqualToString:@"-1"]) {
            
            //失败
            _doneButton.enabled = NO;
            [TAPIUtility alertMessage:@"您今日已审核过三个车场" toViewController:self];
            return;
            
        } else if ([[result objectForKey:@"id"] isEqualToString:@"-2"]) {
            
            //失败
            _doneButton.enabled = NO;
            [TAPIUtility alertMessage:@"您周围没有可审核的车场" toViewController:self];
            return;
            
        } else {
            
            //成功
            [_parkIds addObject:[result objectForKey:@"id"]];
            
            _resultDic = result;
            _promptLabel.text = [result objectForKey:@"name"];
            _descriptionTextView.text = [result objectForKey:@"desc"];
            
            [_doneButton setTitle:@"确认审核" forState:UIControlStateNormal];
            _doneButton.enabled = YES;
            
            _payLabel.text = [NSString stringWithFormat:@"是否%@车场", [[result objectForKey:@"type"] isEqualToString:@"0"] ? @"收费" : @"免费"];
            
            //地图移动
            [_mapView setCenterCoordinate:CLLocationCoordinate2DMake([[result objectForKey:@"lat"] floatValue], [[result objectForKey:@"lng"] floatValue]) animated:NO];
            _mapView.zoomLevel = 18;
            
            _chooseView.center = CGPointMake(_mapView.width/2, _mapView.height/2 - _chooseView.height/2);
            _chooseView.hidden = NO;
            
            //反检索
            BMKGeoCodeSearch* search = [[BMKGeoCodeSearch alloc] init];
            BMKReverseGeoCodeOption* option = [[BMKReverseGeoCodeOption alloc] init];
            option.reverseGeoPoint = CLLocationCoordinate2DMake([[result objectForKey:@"lat"] floatValue], [[result objectForKey:@"lng"] floatValue]);
            search.delegate = self;
            [search reverseGeoCode:option];
        }
        
    }];
}

- (void)requestSaveInfo {
    
    NSString* apiPath = [NSString stringWithFormat:@"carinter.do?action=verifypark&mobile=%@&id=%@&isname=%@&islocal=%@&ispay=%@&isresume=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone], [_resultDic objectForKey:@"id"], _img1Button.tag == 100 ? @"0" : @"1", _img2Button.tag == 100 ? @"0" : @"1", _img3Button.tag == 100 ? @"0" : @"1", _img4Button.tag == 100 ? @"0" : @"1"];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    NSLog(@"%@", _request);
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        BOOL success = [[result objectForKey:@"info"] isEqualToString:@"1"];
        if (success) {
            [_doneButton setTitle:@"已审核" forState:UIControlStateNormal];
            _doneButton.enabled = NO;
            
            [TAPIUtility alertMessage:@"审核成功"];
        } else {
            [TAPIUtility alertMessage:@"审核失败"];
        }
    }];
}


#pragma mark - BMKLocationServiceDelegate

- (void)didUpdateBMKUserLocation:(BMKUserLocation *)userLocation {
    if (userLocation.location.coordinate.latitude == 0 || userLocation.location.coordinate.longitude == 0) {
        NSLog(@"定位失败");
        return;
    }
    _coordinate = userLocation.location.coordinate;
    
    [_mapView updateLocationData:userLocation];
    if (!_firstLocation) {
        _firstLocation = YES;
        
        [self requestInfoWithLng:_coordinate.longitude lat:_coordinate.latitude];
    }
}

#pragma mark BMKGeoCodeSearchDelegate

- (void)onGetReverseGeoCodeResult:(BMKGeoCodeSearch *)searcher result:(BMKReverseGeoCodeResult *)result errorCode:(BMKSearchErrorCode)error {
    _addressLabel.text = result.address;
}


#pragma mark MBProgressHUDDelegate

- (void)hudWasHidden:(MBProgressHUD *)hud {
    [self.navigationController popViewControllerAnimated:YES];
}
/*
 #pragma mark - Navigation
 
 // In a storyboard-based application, you will often want to do a little preparation before navigation
 - (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
 // Get the new view controller using [segue destinationViewController].
 // Pass the selected object to the new view controller.
 }
 */

@end
