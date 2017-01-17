//
//  TUploadParkViewController.m
//  TingCheBao_user
//
//  Created by apple on 15/3/26.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TUploadParkViewController.h"
#import <BaiduMapAPI/BMKMapView.h>
#import <BaiduMapAPI/BMKLocationService.h>
#import "TAPIUtility.h"
#import "UIKeyboardViewController.h"
#import <BaiduMapAPI/BMKGeocodeSearch.h>
#import "TTicketHelpController.h"
#import "CVAPIRequestModel.h"
#import "TAPIUtility.h"
#import "TCheckParkViewController.h"
#import "TLoginViewController.h"

@interface TUploadParkViewController ()<BMKMapViewDelegate, BMKLocationServiceDelegate, BMKGeoCodeSearchDelegate>

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
@property(nonatomic, retain) UITextField* nameTextField;

@property(nonatomic, retain) UIView* white1View;
@property(nonatomic, retain) UIButton* moreButton;
@property(nonatomic, retain) UIButton* checkButton;

@property(nonatomic, retain) UIView* white2View;
@property(nonatomic, retain) UILabel* payLabel;
@property(nonatomic, retain) UIButton* payButton;
@property(nonatomic, retain) UILabel* descriptionLabel;
@property(nonatomic, retain) UITextView* descriptionTextView;

@property(nonatomic, retain) UIButton* doneButton;
@property(nonatomic, retain) UIButton* goodButton;

//-----
@property(nonatomic, retain) BMKLocationService* locationService;
@property(nonatomic, retain) BMKPointAnnotation* locationAnnotation;
@property(nonatomic, assign) BOOL firstLocation;

@property(nonatomic, retain) UIKeyboardViewController* keyboardViewController;
@property(nonatomic, retain) CVAPIRequest* request;

@property(nonatomic, retain) BMKGeoCodeSearch* mapSearch;

@end

@implementation TUploadParkViewController

- (id)init {
    if (self = [super init]) {
        
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
        
        _white1View = [[UIView alloc] initWithFrame:CGRectMake(0, _locationView.bottom + 10, self.view.width, 60)];
        _white1View.backgroundColor = [UIColor whiteColor];
        
        _nameHeadLabel = [[UILabel alloc] initWithFrame:CGRectMake(20, _locationView.bottom + 10, 100, 30)];
        _nameHeadLabel.text = @"停车场全名:";
        _nameHeadLabel.font = [UIFont systemFontOfSize:14];
        
        _nameTextField = [[UITextField alloc] initWithFrame:CGRectMake(_nameHeadLabel.right, _nameHeadLabel.top, self.view.width - _nameHeadLabel.width - 2*10 - 10, 30)];
        _nameTextField.placeholder = @"其它车友审核通过后会在车场端显示";
        _nameTextField.clearButtonMode = UITextFieldViewModeWhileEditing;
        _nameTextField.font = [UIFont systemFontOfSize:14];
//        _nameTextField.borderStyle = UITextBorderStyleRoundedRect;
        _nameTextField.backgroundColor = [UIColor whiteColor];
        _nameTextField.layer.cornerRadius = 2;
        
        _payLabel = [[UILabel alloc] initWithFrame:CGRectMake(20, _nameHeadLabel.bottom, 150, 30)];
        _payLabel.text = @"是否收费车场:";
        _payLabel.font = [UIFont systemFontOfSize:14];
        
        _payButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _payButton.tag = 100;
        [_payButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _payButton.frame = CGRectMake(self.view.width - 60 - 10, _payLabel.top, 50, 30);
        UILabel* label2 = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 30, 30)];
        label2.text = @"收费";
        label2.textColor = [UIColor blackColor];
        label2.font = [UIFont systemFontOfSize:14];
        UIImageView* selectImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"not_selected.png"]];
        selectImgView.frame = CGRectMake(label2.right, 7, 15, 15);
        selectImgView.tag = 91;
        [_payButton addSubview:label2];
        [_payButton addSubview:selectImgView];
        
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
        _moreButton.frame = CGRectMake(0, _payLabel.bottom, 100, 30);
        
        
        //white2View---start
        
        _white2View = [[UIView alloc] initWithFrame:CGRectMake(0, _moreButton.bottom, self.view.width, 85)];
        _white2View.backgroundColor = [UIColor whiteColor];
        _white2View.hidden = YES;
        
        _descriptionLabel = [[UILabel alloc] initWithFrame:CGRectMake(20, 0, 100, 30)];
        _descriptionLabel.text = @"车场信息描述:";
        _descriptionLabel.font = [UIFont systemFontOfSize:14];

        _descriptionTextView = [[UITextView alloc] initWithFrame:CGRectMake(_descriptionLabel.right, 8, self.view.width - _descriptionLabel.width - 2*10 - 10, 70)];
        _descriptionTextView.font = [UIFont systemFontOfSize:14];
        _descriptionTextView.layer.cornerRadius = 3;
        _descriptionTextView.layer.borderColor = self.view.backgroundColor.CGColor;
        _descriptionTextView.layer.borderWidth = 1;

        [_white2View addSubview:_descriptionLabel];
        [_white2View addSubview:_descriptionTextView];
        
        //white2View---end
        
        
        _checkButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_checkButton setTitle:@"我也要审核" forState:UIControlStateNormal];
        [_checkButton setTitleColor:green_color forState:UIControlStateNormal];
        _checkButton.titleLabel.font = [UIFont systemFontOfSize:12];
//        _checkButton.contentHorizontalAlignment = UIControlContentHorizontalAlignmentRight;
        [_checkButton setImage:[TAPIUtility imageWithColor:[UIColor clearColor]] forState:UIControlStateNormal];
        [_checkButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _checkButton.frame = CGRectMake(self.view.width - 10 - 90, _white2View.bottom + 10, 90, 30);
        _checkButton.layer.borderColor = green_color.CGColor;
        _checkButton.layer.borderWidth = 1;
        _checkButton.layer.cornerRadius = 5;
        _checkButton.clipsToBounds = YES;
        
        _doneButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_doneButton setTitle:@"上传车场" forState:UIControlStateNormal];
        [_doneButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_doneButton setTitleColor:green_color forState:UIControlStateHighlighted];
        [_doneButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        [_doneButton setBackgroundImage:[TAPIUtility imageWithColor:[UIColor whiteColor]] forState:UIControlStateHighlighted];
        [_doneButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _doneButton.frame = CGRectMake(10, self.view.bottom - 70, self.view.width - 2*10, 40);
        _doneButton.layer.cornerRadius = 5;
        _doneButton.clipsToBounds = YES;
        
        _goodButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_goodButton setTitle:@"上传车场的好处?" forState:UIControlStateNormal];
        [_goodButton setTitleColor:green_color forState:UIControlStateNormal];
        _goodButton.titleLabel.font = [UIFont systemFontOfSize:12];
        [_goodButton setImage:[TAPIUtility imageWithColor:[UIColor clearColor]] forState:UIControlStateNormal];
        [_goodButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _goodButton.frame = CGRectMake(self.view.width - 10 - 100, self.view.bottom - 30, 100, 30);
        
        [self.view addSubview:_mapView];
        [self.view addSubview:_chooseView];
        [self.view addSubview:_locationView];
        [self.view addSubview:_white1View];
        [self.view addSubview:_nameHeadLabel];
        [self.view addSubview:_nameTextField];
        [self.view addSubview:_payLabel];
        [self.view addSubview:_payButton];
        [self.view addSubview:_moreButton];
        [self.view addSubview:_checkButton];
        [self.view addSubview:_white2View];
        [self.view addSubview:_doneButton];
        [self.view addSubview:_goodButton];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(237, 237, 237);
    self.titleView.text = @"上传停车场";
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
    _mapSearch.delegate = nil;
    [_request cancel];
    
//    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)mapView:(BMKMapView *)mapView regionDidChangeAnimated:(BOOL)animated {
    _mapSearch = [[BMKGeoCodeSearch alloc] init];
    BMKReverseGeoCodeOption* option = [[BMKReverseGeoCodeOption alloc] init];
    option.reverseGeoPoint = _mapView.centerCoordinate;
    _mapSearch.delegate = self;
    [_mapSearch reverseGeoCode:option];
}

#pragma mark BMKGeoCodeSearchDelegate

- (void)onGetReverseGeoCodeResult:(BMKGeoCodeSearch *)searcher result:(BMKReverseGeoCodeResult *)result errorCode:(BMKSearchErrorCode)error {
    _addressLabel.text = result.address;
}

#pragma mark private

- (void)buttonTouched:(UIButton*)button {
    if (button == _payButton) {
        UIImageView* imgView = (UIImageView*)[_payButton viewWithTag:91];
        imgView.image = [UIImage imageNamed:_payButton.tag == 100 ? @"rechage_selected.png" : @"not_selected.png"];
        _payButton.tag = _payButton.tag == 100 ? 101 : 100;
    } else if (button == _checkButton) {
        
        TCheckParkViewController* vc = [[TCheckParkViewController alloc] init];
        [self.navigationController pushViewController:vc animated:YES];
        
    } else if (button == _doneButton) {
        if ([_nameTextField.text isEqualToString:@""]) {
            [TAPIUtility alertMessage:@"请输入车场名称"];
            return;
        }
        [self requestSaveInfo];
        
    } else if (button == _goodButton) {
        TTicketHelpController* vc = [[TTicketHelpController alloc] initWithName:@"上传车场好处" url:[TAPIUtility getNetworkWithUrl:@"carinter.do?action=upfine"]];
        [self.navigationController pushViewController:vc animated:YES];
    } else if (button == _moreButton) {
        UIImageView* imgView = (UIImageView*)[_moreButton viewWithTag:91];
        imgView.image = [UIImage imageNamed:_moreButton.tag == 200 ? @"ic_pull_up.png" : @"ic_pull_down.png"];
        _white2View.hidden = _moreButton.tag == 200 ? NO : YES;
        _moreButton.tag = _moreButton.tag == 200 ? 201 : 200;
    }
}

- (void)requestSaveInfo {
    //先登陆
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        [self.navigationController pushViewController:[[TLoginViewController alloc] init] animated:YES];
        return;
    }
    
    NSDictionary* dic = @{@"action" : @"uppark",
                          @"mobile" : [[NSUserDefaults standardUserDefaults] objectForKey:save_phone],
                          @"parkname" : _nameTextField.text,
                          @"desc" : _descriptionTextView.text,
                          @"lng" : [NSString stringWithFormat:@"%lf", _mapView.centerCoordinate.longitude],
                          @"lat" : [NSString stringWithFormat:@"%lf", _mapView.centerCoordinate.latitude],
                          @"type" : _payButton.tag == 101 ? @"0" : @"1"};
    NSString* apiPath = [NSString stringWithFormat:@"carinter.do%@", [CVAPIRequest GETParamString:dic]];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        BOOL success = [[result objectForKey:@"info"] isEqualToString:@"1"];
        if (success) {
            //清空信息
            _nameTextField.text = @"";
            _descriptionTextView.text = @"";
            
            UIImageView* imgView = (UIImageView*)[_payButton viewWithTag:91];
            imgView.image = [UIImage imageNamed:@"not_selected.png"];
            _payButton.tag = 100;
        }
        [TAPIUtility alertMessage:success ? @"感谢您上传车场" : ([[result objectForKey:@"info"] isEqualToString:@"-2"] ? @"抱歉，该位置已经有其它用户上传" : @"您今天上传车场数量已达上限")];
    }];
}


#pragma mark - BMKLocationServiceDelegate

- (void)didUpdateBMKUserLocation:(BMKUserLocation *)userLocation {
    if (userLocation.location.coordinate.latitude == 0 || userLocation.location.coordinate.longitude == 0) {
        NSLog(@"定位失败");
        return;
    }
    
    [_mapView updateLocationData:userLocation];
    if (!_firstLocation) {
        _firstLocation = YES;
        
        [_mapView updateLocationData:userLocation];
        [_mapView setCenterCoordinate:userLocation.location.coordinate animated:NO];
        _mapView.zoomLevel = 18;
        
        _chooseView.center = CGPointMake(_mapView.width/2, _mapView.height/2 - _chooseView.height/2);
        _chooseView.hidden = NO;
    }
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
