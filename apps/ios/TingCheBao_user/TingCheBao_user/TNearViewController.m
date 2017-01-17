//
//  TNearViewController.m
//  TingCheBao_user
//
//  Created by apple on 15/3/11.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TNearViewController.h"
#import "TNearCell.h"
#import "TNearCollectorCell.h"
#import "TAPIUtility.h"
#import <BaiduMapAPI/BMKLocationService.h>
#import "TDetailViewController.h"
#import "TUploadParkViewController.h"

typedef enum{
    page_mode_park = 0,
    page_mode_collector
} page_mode;

@interface TNearViewController ()<UITableViewDataSource, UITableViewDelegate, BNNaviRoutePlanDelegate, BMKLocationServiceDelegate, BNNaviUIManagerDelegate>

@property(nonatomic, retain) UIButton* parkButton;
@property(nonatomic, retain) UIView* lineView;
@property(nonatomic, retain) UIButton* collectorButton;

@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) UIButton* uploadButton;

@property(nonatomic, retain) UIView* bottomView;
@property(nonatomic, retain) UIButton* payButton;//可支付切换按钮
@property(nonatomic, retain) UIImageView* payImgView;
@property(nonatomic, retain) UILabel* payLabel;
@property(nonatomic, retain) UIButton* allButton;//显示所有车场按钮
@property(nonatomic, retain) UIImageView* allImgView;
@property(nonatomic, retain) UILabel* allLabel;


@property(nonatomic, retain) NSMutableArray* items;

@property(nonatomic, retain) NSMutableArray* selectedIndexArray;
@property(nonatomic, assign) page_mode mode;

@property(nonatomic, assign) CLLocationCoordinate2D parkLocation;
@property(nonatomic, assign) CLLocationCoordinate2D selfLocation;
@property(nonatomic, retain) BMKLocationService* locationService;

@end

@implementation TNearViewController

- (id)init {
    if (self = [super init]) {
        _selectedIndexArray = [NSMutableArray array];
        
        _parkButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_parkButton setTitle:@"停车场" forState:UIControlStateNormal];
        [_parkButton setTitleColor:green_color forState:UIControlStateNormal];
        [_parkButton setBackgroundImage:[UIImage imageNamed:@""] forState:UIControlStateNormal];
        [_parkButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _parkButton.frame = CGRectMake(0, 0, self.view.width/2, 40);
        
        _collectorButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_collectorButton setTitle:@"收费员" forState:UIControlStateNormal];
        [_collectorButton setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
        [_collectorButton setBackgroundImage:[UIImage imageNamed:@""] forState:UIControlStateNormal];
        [_collectorButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _collectorButton.frame = CGRectMake(_parkButton.right, 0, self.view.width/2, 40);
        
        _lineView = [[UIView alloc] initWithFrame:CGRectMake(_parkButton.right, 10, 1, 20)];
        _lineView.backgroundColor = green_color;
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, _parkButton.bottom, self.view.width, self.view.height - 40 - 70)];
        _tableView.backgroundColor = bg_view_color;
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
        _tableView.rowHeight = 65;
        
        _uploadButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_uploadButton setTitle:_mode == page_mode_park ? @"没有您要的车场? 点这里试试" : @"没有您要的收费员? 点这里试试" forState:UIControlStateNormal];
        [_uploadButton setTitleColor:green_color forState:UIControlStateNormal];
        _uploadButton.titleLabel.font = [UIFont systemFontOfSize:13];
        [_uploadButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _uploadButton.frame = CGRectMake(0, 0, self.view.width, 40);
        _tableView.tableFooterView = _uploadButton;
        
        //--------bottomView
        _bottomView = [[UIView alloc] initWithFrame:CGRectMake(0, _tableView.bottom, self.view.width, 70)];
        
        _payButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_payButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _payButton.tag = 1;
        
        _payImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@""]];
        
        _payLabel = [[UILabel alloc] init];
        _payLabel.text = @"可支付车场";
        _payLabel.textColor = green_color;
        _payLabel.textAlignment = NSTextAlignmentCenter;
        
        [_payButton addSubview:_payImgView];
        [_payButton addSubview:_payLabel];
        
        _allButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_allButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _allButton.tag = 0;
        
        _allImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@""]];
        
        _allLabel = [[UILabel alloc] init];
        _allLabel.text = @"全部车场";
        _allLabel.textColor = green_color;
        _allLabel.textAlignment = NSTextAlignmentCenter;
        
        [_allButton addSubview:_allImgView];
        [_allButton addSubview:_allLabel];
        
        [_bottomView addSubview:_payButton];
        [_bottomView addSubview:_allButton];
        //--------bottomView--end

        
        [self.view addSubview:_parkButton];
        [self.view addSubview:_collectorButton];
        [self.view addSubview:_lineView];
        [self.view addSubview:_tableView];
        [self.view addSubview:_bottomView];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.titleView.text = @"我的附近";
    _items = [NSMutableArray array];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    //定位
    _locationService = [[BMKLocationService alloc] init];
    _locationService.delegate = self;
    [_locationService startUserLocationService];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_locationService stopUserLocationService];
}

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    
    CGFloat width = self.view.width;
    _payButton.frame = CGRectMake((width - 100*2)/3, 10, 100, 50);
    CGFloat imgWidth = _payButton.tag == 1 ? 25 : 10;
    CGFloat labelHeight = 25;
    _payLabel.font = [UIFont systemFontOfSize:_payButton.tag == 1 ? 15 : 12];
    _payLabel.textColor = _payButton.tag == 1 ? green_color : [UIColor grayColor];
    _payImgView.image = [UIImage imageNamed:_payButton.tag == 1 ? @"park_pay.png" : @"park_circle.png"];
    _payImgView.frame = CGRectMake((100 - imgWidth)/2, 25-imgWidth, imgWidth, imgWidth);
    _payLabel.frame = CGRectMake(0, _payImgView.bottom, 100, labelHeight);
    
    _allButton.frame = CGRectMake(_payButton.right + (width - 100*2)/3, _payButton.top, _payButton.width, _payButton.height);
    imgWidth = _allButton.tag == 1 ? 25 : 10;
    _allLabel.font = [UIFont systemFontOfSize:_allButton.tag == 1 ? 15 : 12];
    _allLabel.textColor = _allButton.tag == 1 ? green_color : [UIColor grayColor];
    _allImgView.image = [UIImage imageNamed:_allButton.tag == 1 ? @"park_all.png" : @"park_circle.png"];
    _allImgView.frame = CGRectMake((100 - imgWidth)/2, 25-imgWidth, imgWidth, imgWidth);
    _allLabel.frame = CGRectMake(0, _allImgView.bottom, 100, labelHeight);
    
}

#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 5;
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (_mode == page_mode_park) {
        static NSString* identifier = @"cell1";
        TNearCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
        if (!cell) {
            cell = [[TNearCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifier];
        }
    //    [cell setItem:[_items objectAtIndex:indexPath.row]];
        return cell;
    } else {
        static NSString* identifier = @"cell2";
        TNearCollectorCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
        if (!cell) {
            cell = [[TNearCollectorCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifier];
        }
    //    [cell setItem:[_items objectAtIndex:indexPath.row]];
        return cell;
    }
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    BOOL flag = NO;
    for (NSNumber* index in _selectedIndexArray) {
        if ([index integerValue] == indexPath.row) {
            [_selectedIndexArray removeObject:index];
            flag = YES;
            break;
        }
    }
    if (flag == NO) {
        [_selectedIndexArray addObject:@(indexPath.row)];
    }
    
    [self.tableView reloadRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    BOOL flag = NO;
    for (NSNumber* index in _selectedIndexArray) {
        if ([index integerValue] == indexPath.row) {
            flag = YES;
            break;
        }
    }
    if (flag == NO) {
        return 65 + 8;
    } else {
        return 95 + 8;
    }
}

#pragma mark private

- (void)buttonTouched:(UIButton*)button {
    if (button == _parkButton) {
        
        [_parkButton setTitleColor:green_color forState:UIControlStateNormal];
        [_collectorButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        _mode = page_mode_park;
        [self.tableView reloadData];
        [_uploadButton setTitle:_mode == page_mode_park ? @"没有您要的车场? 点这里试试" : @"没有您要的收费员? 点这里试试" forState:UIControlStateNormal];
        _payLabel.text = @"可支付车场";
        _allLabel.text = @"全部车场";
    } else if (button == _collectorButton) {
        
        [_parkButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        [_collectorButton setTitleColor:green_color forState:UIControlStateNormal];
        _mode = page_mode_collector;
        [self.tableView reloadData];
        [_uploadButton setTitle:_mode == page_mode_park ? @"没有您要的车场? 点这里试试" : @"没有您要的收费员? 点这里试试" forState:UIControlStateNormal];
        _payLabel.text = @"显示长得帅的";
        _allLabel.text = @"全部";
        
    } else if (button == _uploadButton) {
        
        TUploadParkViewController* vc = [[TUploadParkViewController alloc] init];
        [self.navigationController pushViewController:vc animated:YES];
        
    } else {
        
        if (button == _payButton && button.tag != 1) {
            _payButton.tag = 1;
            _allButton.tag = 0;
        }
        if (button == _allButton && button.tag != 1) {
            _payButton.tag = 0;
            _allButton.tag = 1;
        }
        [self viewDidLayoutSubviews];
    }
}

- (void)clickedNavButton:(UIButton*)button {
    //TODO:wait _parkLocation
    if (_selfLocation.longitude == 0 || _selfLocation.latitude == 0) {
        [TAPIUtility alertMessage:@"定位失败，无法导航" success:NO toViewController:self];
        return;
    }
    NSMutableArray *nodesArray = [[NSMutableArray alloc]initWithCapacity:2];
    //起点 传入的是原始的经纬度坐标，若使用的是百度地图坐标，可以使用BNTools类进行坐标转化
    BNRoutePlanNode *startNode = [[BNRoutePlanNode alloc] init];
    startNode.pos = [[BNPosition alloc] init];
    startNode.pos.x = _selfLocation.longitude;
    startNode.pos.y = _selfLocation.latitude;
    startNode.pos.eType = BNCoordinate_BaiduMapSDK;
    [nodesArray addObject:startNode];
    
    //终点
    BNRoutePlanNode *endNode = [[BNRoutePlanNode alloc] init];
    endNode.pos = [[BNPosition alloc] init];
    endNode.pos.x = _parkLocation.longitude;
    endNode.pos.y = _parkLocation.latitude;
    endNode.pos.eType = BNCoordinate_BaiduMapSDK;
    [nodesArray addObject:endNode];
    
    [BNCoreServices_RoutePlan startNaviRoutePlan:BNRoutePlanMode_Recommend naviNodes:nodesArray time:nil delegete:self userInfo:nil];
}

- (void)clickedPayButton {
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        [TAPIUtility alertMessage:@"请先登录哦~"];
        return;
    }
    
    //TODO:wait
    //=======当前订单有两个连接
//    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=currentorder&mobile=%@&comid=%@",[[NSUserDefaults standardUserDefaults] objectForKey:save_phone], _parkId];
//    _payRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
//    _payRequest.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
//    [self.model sendRequest:_payRequest completion:^(NSDictionary *result, NSError *error) {
//        if (!result)
//            return;
//        if ([result count]) {
//            NSString* state = [result objectForKey:@"state"];
//            if ([state isEqualToString:@"0"]) {
//                [TAPIUtility alertMessage:@"请等待收费员结算您的订单!"];
//            } else if ([state isEqualToString:@"1"]) {
//                [self.navigationController popViewControllerAnimated:YES];
//                [[THomeViewController share] requestYuEBaoAndTickets:@{@"info" : result}];
//            }
//        } else {
//            TCollectorsListViewController* vc = [[TCollectorsListViewController alloc] init];
//            vc.parkId = _parkId;
//            vc.parkName = _parkName;
//            [self.navigationController pushViewController:vc animated:YES];
//        }
//    }];
}

- (void)clickedDetailButton {
    //TODO:wait
    TDetailViewController* vc = [[TDetailViewController alloc] init];
//    vc.parkId = item.parkId;
//    vc.parkName = item.name;
//    vc.selfLocation = _coordinate;
//    NSString* longitude = nil;
//    NSString* latitude = nil;
//    for (TParkItem* object in _allParkItems) {
//        if ([object.parkId isEqualToString:vc.parkId]) {
//            longitude = object.lng;
//            latitude = object.lat;
//            break;
//        }
//    }
//    vc.parkLocation = CLLocationCoordinate2DMake([latitude doubleValue], [longitude doubleValue]);
    [self.navigationController pushViewController:vc animated:YES];
}

#pragma mark - BMKLocationServiceDelegate

- (void)didUpdateBMKUserLocation:(BMKUserLocation *)userLocation {
    if (userLocation.location.coordinate.latitude == 0 || userLocation.location.coordinate.longitude == 0) {
        NSLog(@"定位失败");
        return;
    }
    _selfLocation = userLocation.location.coordinate;
    
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
    if ([error code] == BNRoutePlanError_LocationFailed) {
        NSLog(@"获取地理位置失败");
    }
    else if ([error code] == BNRoutePlanError_LocationServiceClosed)
    {
        NSLog(@"定位服务未开启");
    }
}

@end
