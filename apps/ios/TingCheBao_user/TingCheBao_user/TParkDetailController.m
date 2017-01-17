//
//  TParkDetailController.m
//  TingCheBao_user
//
//  Created by apple on 15/4/16.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TParkDetailController.h"
#import "TParkDetailItem.h"
#import "TAPIUtility.h"
#import "CVAPIRequestModel.h"
#import "UIImageView+WebCache.h"
#import <BaiduMapAPI/BMKLocationService.h>
#import "TParkDetailCell.h"
#import "TPriceViewController.h"
#import "TCommentViewController.h"
#import "TParkCommentViewController.h"
#import "TFullImageView.h"
#import "TCollectorsListViewController.h"

@interface TParkDetailController ()<UITableViewDataSource, UITableViewDelegate, UIAlertViewDelegate, BMKLocationServiceDelegate, BNNaviRoutePlanDelegate, BNNaviUIManagerDelegate>

@property(nonatomic, retain) TFullImageView* imageView;
@property(nonatomic, retain) UILabel* freeNumLabel;
@property(nonatomic, retain) UITableView* tableView;

@property(nonatomic, retain) UIButton* commentButton;
@property(nonatomic, retain) UIButton* naviButton;


@property(nonatomic, retain) TParkDetailItem* item;

@property(nonatomic, retain) CVAPIRequest* request;
@property(nonatomic, retain) BMKLocationService* locationService;
@property(nonatomic, assign) CLLocationCoordinate2D selfLocation;

@property(nonatomic, assign) BOOL firstLoad;

@end

@implementation TParkDetailController

- (id)init {
    if (self = [super init]) {
        //        _imageView = [[TFullImageView alloc] initWithImage:nil];
        _imageView = [[TFullImageView alloc] init];
        _imageView.frame = CGRectMake(0, 0, self.view.width, 200);
        _imageView.backgroundColor = [UIColor blackColor];
        _imageView.contentMode = UIViewContentModeScaleAspectFit;
        
        _freeNumLabel = [[UILabel alloc] initWithFrame:CGRectMake(self.view.width - 10 - 90, _imageView.height - 10 - 30, 90, 30)];
        _freeNumLabel.text = @"车位 6/100";
        _freeNumLabel.backgroundColor = [UIColor blackColor];
        _freeNumLabel.textAlignment = NSTextAlignmentCenter;
        _freeNumLabel.alpha = 0.8;
        _freeNumLabel.layer.cornerRadius = 5;
        _freeNumLabel.clipsToBounds = YES;
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, _imageView.bottom, self.view.width, self.view.height - 40 - 20 - 40- _imageView.height) style:UITableViewStyleGrouped];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 1)];
        
        _commentButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_commentButton setTitle:@"点击展开评论" forState:UIControlStateNormal];
        _commentButton.titleLabel.font = [UIFont systemFontOfSize:14];
        [_commentButton setTitleColor:green_color forState:UIControlStateNormal];
        [_commentButton setImage:[UIImage imageNamed:@""] forState:UIControlStateNormal];
        _commentButton.frame = CGRectMake((self.view.width - 130)/2, _tableView.bottom, 130, 40);
        [_commentButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _naviButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_naviButton setTitle:@"到这去" forState:UIControlStateNormal];
        [_naviButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_naviButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _naviButton.frame = CGRectMake(10, _commentButton.bottom, self.view.width - 2*10, 40);
        [_naviButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _naviButton.layer.cornerRadius = 5;
        _naviButton.clipsToBounds = YES;
        
        [self.view addSubview:_imageView];
        [self.view addSubview:_freeNumLabel];
        [self.view addSubview:_tableView];
        [self.view addSubview:_commentButton];
        [self.view addSubview:_naviButton];
        
        _imageView.hidden = _freeNumLabel.hidden = _tableView.hidden = _naviButton.hidden = _commentButton.hidden = YES;
    }
    
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(245, 245, 245);
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    //标题
    self.titleView.text = _parkName ? _parkName : @"车场详情";
    
    //右上角 打电话
    UIButton* button = [UIButton buttonWithType:UIButtonTypeCustom];
    button.frame = CGRectMake(0, 0, 20, 20);
    [button setBackgroundImage:[UIImage imageNamed:@"detail_phone.png"] forState:UIControlStateNormal];
    [button addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
    
    UIBarButtonItem* photoItem = [[UIBarButtonItem alloc] initWithCustomView:button];
    self.navigationItem.rightBarButtonItem = photoItem;
    
    //定位
    _locationService = [[BMKLocationService alloc] init];
    _locationService.delegate = self;
    [_locationService startUserLocationService];
    
    if (!_firstLoad) {
        [self requestDetailInfo];
        _firstLoad = YES;
    }
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_locationService stopUserLocationService];
    
    [_request cancel];
}

#pragma request

- (void)requestDetailInfo {
    
    NSString* apiPath = [NSString stringWithFormat:@"carinter.do?action=parkinfo&comid=%@", _parkId];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result || [result count] == 0)
            return;
        //显示页面
        _imageView.hidden = _freeNumLabel.hidden = _tableView.hidden = _naviButton.hidden = _commentButton.hidden = NO;
        
        _item = [TParkDetailItem getItemFromDictionary:result];
        
        //更新freeNum
        NSMutableAttributedString* attrString = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"车位:%@/%@", _item.free, _item.total] attributes:@{NSForegroundColorAttributeName : [UIColor whiteColor], NSFontAttributeName : [UIFont systemFontOfSize:14]}];
        [attrString addAttributes:@{NSForegroundColorAttributeName : green_color} range:NSMakeRange(3, _item.free.length)];
        _freeNumLabel.attributedText = attrString;
        CGSize size = [TAPIUtility sizeWithFont:[UIFont systemFontOfSize:14] size:CGSizeMake(200, 30) text:attrString.string];
        size = CGSizeMake(size.width + 20, size.height);
        _freeNumLabel.frame = CGRectMake(self.view.width - 10 - size.width, _imageView.height - 10 - 30, size.width, 30);
        
        
        
        // placeHolderImg 延迟加载    SDWebImageDelayPlaceholder
        [_imageView sd_setImageWithURL:[NSURL URLWithString:_item.photo_url] placeholderImage:[UIImage imageNamed:@"detail_default.jpg"] options:SDWebImageDelayPlaceholder completed:^(UIImage *image, NSError *error, SDImageCacheType cacheType, NSURL *imageURL) {
            if (error == nil) {
                _imageView.tag = 1;
            } else {
                _imageView.tag = 0;
            }
        }];
        [_tableView reloadData];
    }];
}

#pragma mark UITableViewDelegate

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    if ([_item.epay isEqualToString:@"1"])
        return 3;
    else
        return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (section == 0) {
        return 2;
    } else if (section == 1) {
        return 1;
    } else if (section == 2) {
        return 1;
    }
    return 1;
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifer = @"cell";
    TParkDetailCell* cell = [tableView dequeueReusableCellWithIdentifier:identifer];
    if (!cell) {
        cell = [[TParkDetailCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifer];
    }
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    if (indexPath.row == 0 && indexPath.section == 0) {
        cell.leftLabel.text = @"地址:";
        cell.leftLabel.textColor = RGBCOLOR(148, 148, 148);
        
        //        CGSize size = [TAPIUtility sizeWithFont:[UIFont systemFontOfSize:17] size:CGSizeMake(230, 600) text:_item.addr];
        //        size.height += 10;
        //        if (size.height < 44) {
        //            size.height = 44;
        //        }
        cell.rightLabel.text = _item.addr;
        cell.rightLabel.numberOfLines = 0;//地址可能很长，分成二行
        
        //        cell.detailTextLabel.text = @"纠错";
        cell.accessoryType = UITableViewCellAccessoryNone;
        cell.userInteractionEnabled = NO;
    } else if (indexPath.row == 1 && indexPath.section == 0) {
        cell.leftLabel.text = @"价格:";
        cell.leftLabel.textColor = RGBCOLOR(148, 148, 148);
        
        cell.rightLabel.text = [_item.price isEqualToString:@"-1"] ? @"免费" : _item.price;
        cell.accessoryType = [_item.price isEqualToString:@"-1"] ? UITableViewCellAccessoryNone : UITableViewCellAccessoryDisclosureIndicator;
        cell.userInteractionEnabled = [_item.price isEqualToString:@"-1"] ? NO : YES;
    } else if (indexPath.row == 0 && indexPath.section == 1) {
        
        cell.leftLabel.text = @"描述:";
        cell.leftLabel.textColor = RGBCOLOR(148, 148, 148);
        
        cell.rightLabel.text = _item.desc;
        cell.rightLabel.numberOfLines = 0;
        cell.accessoryType = UITableViewCellAccessoryNone;
        cell.userInteractionEnabled = NO;
        
        cell.detailTextLabel.text = @"";
    } else if (indexPath.row == 0 && indexPath.section == 2) {
        cell.leftLabel.text = @"付费:";
        cell.leftLabel.textColor = green_color;
        
        cell.rightLabel.text = @"选择收费员可直接付费";
    }
    
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 10;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 1;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    //计算描述的高度，可能字数很多
    if (indexPath.section == 1) {
        CGSize size = [TAPIUtility sizeWithFont:[UIFont systemFontOfSize:17] size:CGSizeMake(230, 600) text:_item.desc];
        size.height += 10;
        if (size.height < 44) {
            size.height = 44;
        }
        return size.height;
    }
    return 44;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
    
    
    if (indexPath.section == 0 && indexPath.row == 1) {
        TPriceViewController* vc = [[TPriceViewController alloc] init];
        //        TCommentViewController* vc = [[TCommentViewController alloc] init];
        vc.parkId = _parkId;
        [self.navigationController pushViewController:vc animated:YES];
    } else if (indexPath.section == 2 && indexPath.row == 0) {
        TCollectorsListViewController* vc = [[TCollectorsListViewController alloc] init];
        vc.parkId = _item.parkId;
        vc.parkName = _item.name;
        [self.navigationController pushViewController:vc animated:YES];
    }
    
}
#pragma mark private

- (void)buttonTouched:(UIButton*)button {
    if (button == _commentButton) {
        
        TParkCommentViewController* vc = [[TParkCommentViewController alloc] init];
        //        TCommentViewController* vc = [[TCommentViewController alloc] init];
        vc.parkId = _parkId;
        vc.mode = Comment_mode_park;
        [self.navigationController pushViewController:vc animated:YES];
        
    } else if (button == _naviButton) {
        
        //导航
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
        endNode.pos.x = [_item.lng doubleValue];
        endNode.pos.y = [_item.lat doubleValue];
        endNode.pos.eType = BNCoordinate_BaiduMapSDK;
        [nodesArray addObject:endNode];
        
        [BNCoreServices_RoutePlan startNaviRoutePlan:BNRoutePlanMode_Recommend naviNodes:nodesArray time:nil delegete:self userInfo:nil];
        
    } else {
        
        //打电话
        UIAlertView* alert = nil;
        if (_item.phone && ![_item.phone isEqualToString:@""])
            alert = [[UIAlertView alloc] initWithTitle:nil message:_item.phone delegate:self cancelButtonTitle:@"取消" otherButtonTitles:@"确定", nil];
        else
            alert = [[UIAlertView alloc] initWithTitle:nil message:@"暂未提供车场电话" delegate:nil cancelButtonTitle:nil otherButtonTitles:@"确定", nil];
        [alert show];
        
    }
}

#pragma mark UIAlertViewDelegate


- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (buttonIndex != alertView.cancelButtonIndex) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", _item.phone]]];
    }
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
