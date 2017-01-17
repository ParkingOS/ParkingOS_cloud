//
//  TDetailViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-9-2.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TDetailViewController.h"
#import "CVAPIRequestModel.h"
#import "TDetailParkItem.h"
#import "UIImageView+WebCache.h"
#import "TAPIUtility.h"
#import "TLoginViewController.h"
#import "TPriceViewController.h"
#import "TCommentViewController.h"
#import "TParkMonthViewController.h"
#import <BaiduMapAPI/BMKLocationService.h>
#import "THomeViewController.h"
#import "TCollectorsListViewController.h"

#define padding 10
#define bg_color RGBCOLOR(230, 230, 230)
#define line_width 1

@interface TDetailViewController ()<BNNaviRoutePlanDelegate, BNNaviUIManagerDelegate, CLLocationManagerDelegate, BMKLocationServiceDelegate, UIScrollViewDelegate>

@property(nonatomic, retain) UIView* parkNumberView;
@property(nonatomic, retain) UILabel* parkNumberLabel;
@property(nonatomic, retain) UILabel* freeAndAllLabel;
@property(nonatomic, retain) UIView* supportView;
@property(nonatomic, retain) UIImageView* parkImgView;
@property(nonatomic, retain) UILabel* parkTypeLabel;
@property(nonatomic, retain) UIButton* priceButton;
@property(nonatomic, retain) UIView* centerView;
@property(nonatomic, retain) UILabel* addressLabel;
@property(nonatomic, retain) UIButton* phoneButton;
@property(nonatomic, retain) UIButton* agreeButton;
@property(nonatomic, retain) UIButton* projectButton;
@property(nonatomic, retain) UIButton* commentButton;
@property(nonatomic, retain) UILabel* descriptionLabel1;
@property(nonatomic, retain) UILabel* descriptionLabel;
@property(nonatomic, retain) UIView* bottomView;
@property(nonatomic, retain) UIButton* naviButton;
@property(nonatomic, retain) UIButton* payButton;
@property(nonatomic, retain) UIButton* monthButotn;

@property(nonatomic, retain) UIView* lineView1;
@property(nonatomic, retain) UIView* lineView2;
@property(nonatomic, retain) UIView* lineView3;
@property(nonatomic, retain) UIView* lineView4;
@property(nonatomic, retain) UIView* lineView5;
@property(nonatomic, retain) UIWindow* imgWindow;
@property(nonatomic, retain) UIScrollView* imgScrollView;
@property(nonatomic, retain) UIImageView* fullImgView;

@property(nonatomic, retain) TDetailParkItem* item;

@property(nonatomic, retain) BMKLocationService* locationService;

@property(nonatomic, retain) CVAPIRequest* request;
@property(nonatomic, retain) CVAPIRequest* payRequest;
@property(nonatomic, retain) CVAPIRequest* voteRequest;

@end

@implementation TDetailViewController

- (id)init {
    if (self = [super init]) {
        self.view.backgroundColor = bg_color;
        
        _parkNumberView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 200, 110)];
        _parkNumberView.backgroundColor = RGBCOLOR(82, 88, 102);
        
        _parkNumberLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 20, _parkNumberView.width, 25)];
        _parkNumberLabel.textColor = [UIColor whiteColor];
        _parkNumberLabel.textAlignment = NSTextAlignmentCenter;
        _parkNumberLabel.text = @"0/0";
        _parkNumberLabel.font = [UIFont boldSystemFontOfSize:32];
        _parkNumberLabel.numberOfLines = 2;
        
        _freeAndAllLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _parkNumberLabel.bottom, _parkNumberView.width, 20)];
        _freeAndAllLabel.font = [UIFont systemFontOfSize:13];
        _freeAndAllLabel.textColor = [UIColor grayColor];
        _freeAndAllLabel.textAlignment = NSTextAlignmentCenter;
        _freeAndAllLabel.text = @"空闲车位/总车位";
        
        [_parkNumberView addSubview:_parkNumberLabel];
        [_parkNumberView addSubview:_freeAndAllLabel];
        
        _parkImgView = [[UIImageView alloc] initWithFrame:CGRectMake(_parkNumberView.right, 0, self.view.width - _parkNumberView.width, _parkNumberView.height)];
        _parkImgView.backgroundColor = [UIColor blackColor];
//        _parkImgView.contentMode = UIViewContentModeScaleAspectFit;
        [_parkImgView addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapImgViewGesture:)]];
        _parkImgView.userInteractionEnabled = YES;
        
        _parkTypeLabel = [[UILabel alloc] initWithFrame:CGRectMake(_parkImgView.left, _parkImgView.bottom - 20, _parkImgView.width, 20)];
        _parkTypeLabel.textColor = [UIColor whiteColor];
        _parkTypeLabel.textAlignment = NSTextAlignmentCenter;
        _parkTypeLabel.backgroundColor = [UIColor blackColor];
        _parkTypeLabel.alpha = 0.6;
        _parkTypeLabel.text = @"停车场";
        _parkTypeLabel.font = [UIFont systemFontOfSize:15];
        
        _priceButton = [UIButton buttonWithType:UIButtonTypeCustom];
        
        NSString* title = @"0.0元/15分钟";
        NSMutableAttributedString* mutableTitle = [[NSMutableAttributedString alloc] initWithString:title];
        NSRange range = [title rangeOfString:@"元"];
        NSRange range1 = NSMakeRange(0, range.location);
        NSRange range2 = NSMakeRange(range.location, title.length - range.location);
        [mutableTitle addAttributes:@{NSFontAttributeName : [UIFont systemFontOfSize:19], NSForegroundColorAttributeName : [UIColor whiteColor]} range:range1];
        [mutableTitle addAttributes:@{NSFontAttributeName : [UIFont systemFontOfSize:14], NSForegroundColorAttributeName : bg_color} range:range2];
        
        [_priceButton setAttributedTitle:mutableTitle forState:UIControlStateNormal];
        [_priceButton setImage:[UIImage imageNamed:@"left_arrow_right.png"] forState:UIControlStateNormal];
        if(isIphoneNormal)
            _priceButton.imageEdgeInsets = UIEdgeInsetsMake(10, 290, 10, 10);
        else if (isIphone6)
            _priceButton.imageEdgeInsets = UIEdgeInsetsMake(10, 330, 10, 25);
        else if (isIphone6Plus)
            _priceButton.imageEdgeInsets = UIEdgeInsetsMake(10, 366, 10, 25);
        _priceButton.frame = CGRectMake(0, _parkNumberView.bottom, self.view.width, 40);
        [_priceButton setBackgroundImage:[TAPIUtility imageWithColor:RGBCOLOR(53, 60, 76)] forState:UIControlStateNormal];
        [_priceButton addTarget:self action:@selector(priceButtonTouched) forControlEvents:UIControlEventTouchUpInside];
        
        //centerView
        _centerView = [[UIView alloc] initWithFrame:CGRectMake(padding, _priceButton.bottom + padding, self.view.width - 2 * padding, 180)];
        _centerView.backgroundColor = [UIColor whiteColor];
        _centerView.layer.cornerRadius = 5;
        _centerView.clipsToBounds = YES;
        
        _addressLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, _centerView.width - 80, 50)];
        _addressLabel.text = @"";
        _addressLabel.backgroundColor = [UIColor whiteColor];
        _addressLabel.adjustsFontSizeToFitWidth = YES;
        
        _lineView1  = [[UIView alloc] initWithFrame:CGRectMake(0, _addressLabel.bottom, _addressLabel.width, line_width)];
        _lineView1.backgroundColor = bg_color;
        
        _phoneButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _phoneButton.frame = CGRectMake(_addressLabel.right, 0, _centerView.width - _addressLabel.width, _addressLabel.height + _lineView1.height);
        [_phoneButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        [_phoneButton setImage:[UIImage imageNamed:@"phone.png"] forState:UIControlStateNormal];
        [_phoneButton addTarget:self action:@selector(clickedPhoneButton) forControlEvents:UIControlEventTouchUpInside];
        
        _agreeButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _agreeButton.frame = CGRectMake(0, _lineView1.bottom, 110, 52);
        [_agreeButton setTitle:@"0" forState:UIControlStateNormal];
        [_agreeButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        _agreeButton.titleLabel.font = [UIFont systemFontOfSize:15];
        [_agreeButton setTitleEdgeInsets:UIEdgeInsetsMake(5, 0, 5, 0)];
        [_agreeButton setImage:[UIImage imageNamed:@"ic_praise.png"] forState:UIControlStateNormal];
        [_agreeButton setImageEdgeInsets:UIEdgeInsetsMake(10, 10, 10, _agreeButton.width - 40)];
        [_agreeButton setBackgroundImage:[TAPIUtility imageWithColor:[UIColor whiteColor]] forState:UIControlStateNormal];
        [_agreeButton addTarget:self action:@selector(clickedVoteButton:) forControlEvents:UIControlEventTouchUpInside];
        
        _lineView2 = [[UIView alloc] initWithFrame:CGRectMake(_agreeButton.right, _agreeButton.top, line_width, _agreeButton.height)];
        _lineView2.backgroundColor = bg_color;
        
        _projectButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _projectButton.frame = CGRectMake(_lineView2.right, _agreeButton.top, 110, _agreeButton.height);
        [_projectButton setTitle:@"0" forState:UIControlStateNormal];
        [_projectButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        _projectButton.titleLabel.font = [UIFont systemFontOfSize:15];
        [_projectButton setTitleEdgeInsets:UIEdgeInsetsMake(5, 0, 5, 0)];
        [_projectButton setImage:[UIImage imageNamed:@"ic_disparage.png"] forState:UIControlStateNormal];
        [_projectButton setImageEdgeInsets:UIEdgeInsetsMake(10, 10, 10, _projectButton.width - 40)];
        [_projectButton setBackgroundImage:[TAPIUtility imageWithColor:[UIColor whiteColor]] forState:UIControlStateNormal];
        [_projectButton addTarget:self action:@selector(clickedVoteButton:) forControlEvents:UIControlEventTouchUpInside];
        
        _lineView3 = [[UIView alloc] initWithFrame:CGRectMake(_projectButton.right, _projectButton.top, line_width, _projectButton.height)];
        _lineView3.backgroundColor = bg_color;
        
        _commentButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _commentButton.frame = CGRectMake(_lineView3.right, _agreeButton.top, _centerView.width - _lineView3.right, _agreeButton.height);
        [_commentButton setTitle:@"评论" forState:UIControlStateNormal];
        [_commentButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        _commentButton.titleLabel.font = [UIFont systemFontOfSize:15];
        [_commentButton setImage:[UIImage imageNamed:@"ic_arrow_grey.png"] forState:UIControlStateNormal];
        [_commentButton setTitleEdgeInsets:UIEdgeInsetsMake(0, -40, 0, 10)];
        [_commentButton setImageEdgeInsets:UIEdgeInsetsMake(15, _commentButton.width - 20, 15, 0)];
        [_commentButton setBackgroundImage:[TAPIUtility imageWithColor:[UIColor whiteColor]] forState:UIControlStateNormal];
        [_commentButton addTarget:self action:@selector(commentButtonTouched) forControlEvents:UIControlEventTouchUpInside];
        
        _lineView4 = [[UIView alloc] initWithFrame:CGRectMake(0, _agreeButton.bottom, _centerView.width, line_width)];
        _lineView4.backgroundColor = bg_color;
        
        _descriptionLabel1 = [[UILabel alloc] initWithFrame:CGRectMake(padding, _lineView4.bottom, 40, 67)];
        _descriptionLabel1.text = @"车场描述";
        _descriptionLabel1.numberOfLines = 2;
        
        _lineView5 = [[UIView alloc] initWithFrame:CGRectMake(60, _descriptionLabel1.top, line_width, _centerView.height - _descriptionLabel1.top)];
        _lineView5.backgroundColor = bg_color;
        
        _descriptionLabel = [[UILabel alloc] initWithFrame:CGRectMake(_lineView5.right + padding, _descriptionLabel1.top, _centerView.width - _lineView5.right - padding, _descriptionLabel1.height)];
        _descriptionLabel.text = @"本车场环境优雅，车位多，价格优惠！欢迎光临!";
        _descriptionLabel.numberOfLines = 2;
        _descriptionLabel.backgroundColor = [UIColor whiteColor];
        _descriptionLabel.font = [UIFont systemFontOfSize:15];
        
        [_centerView addSubview:_addressLabel];
        [_centerView addSubview:_phoneButton];
        [_centerView addSubview:_agreeButton];
        [_centerView addSubview:_projectButton];
        [_centerView addSubview:_commentButton];
        [_centerView addSubview:_descriptionLabel1];
        [_centerView addSubview:_descriptionLabel];
        [_centerView addSubview:_lineView1];
        [_centerView addSubview:_lineView2];
        [_centerView addSubview:_lineView3];
        [_centerView addSubview:_lineView4];
        [_centerView addSubview:_lineView5];
        
        //bottomView
        _bottomView = [[UIView alloc] initWithFrame:CGRectMake(padding, self.view.bottom - 60, _centerView.width, 50)];
        _bottomView.layer.cornerRadius = 5;
        _bottomView.clipsToBounds = YES;
        
        _naviButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_naviButton setTitle:@"导航" forState:UIControlStateNormal];
        [_naviButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_naviButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _naviButton.frame = CGRectMake(0, 0, _centerView.width/3, _bottomView.height);
        [_naviButton addTarget:self action:@selector(clickedNavButton:) forControlEvents:UIControlEventTouchUpInside];
        
        _payButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_payButton setTitle:@"付车费" forState:UIControlStateNormal];
        [_payButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_payButton setBackgroundImage:[TAPIUtility imageWithColor:blue_color] forState:UIControlStateNormal];
        _payButton.frame = CGRectMake(_naviButton.right, 0, _centerView.width/3, _bottomView.height);
        [_payButton addTarget:self action:@selector(clickedPayButton) forControlEvents:UIControlEventTouchUpInside];
        
        _monthButotn = [UIButton buttonWithType:UIButtonTypeCustom];
        [_monthButotn setTitle:@"月卡" forState:UIControlStateNormal];
        [_monthButotn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_monthButotn setBackgroundImage:[TAPIUtility imageWithColor:[UIColor orangeColor]] forState:UIControlStateNormal];
        _monthButotn.frame = CGRectMake(_payButton.right, 0, _centerView.width/3, _bottomView.height);
        [_monthButotn addTarget:self action:@selector(clickedMonthButton) forControlEvents:UIControlEventTouchUpInside];
        
        [_bottomView addSubview:_naviButton];
        [_bottomView addSubview:_payButton];
        [_bottomView addSubview:_monthButotn];
        
        //view
        [self.view addSubview:_parkNumberView];
        [self.view addSubview:_parkImgView];
        [self.view addSubview:_parkTypeLabel];
        [self.view addSubview:_priceButton];
        [self.view addSubview:_centerView];
        [self.view addSubview:_bottomView];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(237, 237, 237);
}


- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.titleView.text = _parkName;
    [self requestDetailInfo];
    
    //定位
    _locationService = [[BMKLocationService alloc] init];
    _locationService.delegate = self;
    [_locationService startUserLocationService];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_locationService stopUserLocationService];
    
    [_request cancel];
    [_payRequest cancel];
    [_voteRequest cancel];
}

#pragma request

- (void)requestDetailInfo {

    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=parkdetail&comid=%@&mobile=%@", _parkId, [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result || [result count] == 0)
            return;
        _item = [TDetailParkItem getItemFromDictionary:result];
        _parkNumberLabel.text = [NSString stringWithFormat:@"%@/%@", _item.freeSpace, _item.total];
        
        // placeHolderImg 延迟加载    SDWebImageDelayPlaceholder
        [_parkImgView sd_setImageWithURL:[NSURL URLWithString:_item.photoUrl] placeholderImage:[UIImage imageNamed:@"pic_park_ex.png"] options:SDWebImageDelayPlaceholder completed:^(UIImage *image, NSError *error, SDImageCacheType cacheType, NSURL *imageURL) {
            if (error == nil) {
                _parkImgView.tag = 1;
            } else {
                _parkImgView.tag = 0;
            }
        }];
        
        NSMutableAttributedString* mutableTitle = [[NSMutableAttributedString alloc] initWithString:_item.currentPrice];
        NSRange range = [_item.currentPrice rangeOfString:@"元"];
        NSRange range1 = NSMakeRange(0, range.location);
        NSRange range2 = NSMakeRange(range.location, _item.currentPrice.length - range.location);
        [mutableTitle addAttributes:@{NSFontAttributeName : [UIFont boldSystemFontOfSize:22], NSForegroundColorAttributeName : [UIColor whiteColor]} range:range1];
        [mutableTitle addAttributes:@{NSFontAttributeName : [UIFont systemFontOfSize:14], NSForegroundColorAttributeName : bg_color} range:range2];
        [_priceButton setAttributedTitle:mutableTitle forState:UIControlStateNormal];
        NSArray* types = @[@"地面停车场", @"地下停车场", @"路边停车场", @"地面/地下停车场"];
        _parkTypeLabel.text = [types objectAtIndex:[_item.parking_type integerValue]];
        _addressLabel.text = _item.address;
        _phoneButton.tag = [_item.mobile integerValue];
        [_agreeButton setTitle:_item.praiseNum forState:UIControlStateNormal];
        [_projectButton setTitle:_item.disparageNum forState:UIControlStateNormal];
        _descriptionLabel.text = _item.descri;
        [self updateSupportView];
        [self updateVoteState];
    }];
}

#pragma private

- (void)updateSupportView {
    NSMutableArray* images = [NSMutableArray array];
    if ([_item.nfc isEqualToString:@"1"]) {
        [images addObject:[UIImage imageNamed:@"ic_park_detail_nfc.png"]];
    }
    if ([_item.etc isEqualToString:@"1"]) {
        [images addObject:[UIImage imageNamed:@"ic_park_detail_etc.png"]];
    }
    if ([_item.book isEqualToString:@"1"]) {
        [images addObject:[UIImage imageNamed:@"ic_park_detail_booked.png"]];
    }
    if ([_item.navi isEqualToString:@"1"]) {
        [images addObject:[UIImage imageNamed:@"ic_park_detail_indoor_navi.png"]];
    }
    if ([_item.monthlyPay isEqualToString:@"1"]) {
        [images addObject:[UIImage imageNamed:@"ic_park_detail_monthly_pay.png"]];
    }
    int number = [images count];
    if (number == 0)
        return;
    float iconWidth = 30;
    _supportView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, iconWidth * number + (number - 1) * padding, 30)];
    for (int i = 0; i<number; i++) {
        UIImageView* imgView = [[UIImageView alloc] initWithImage:[images objectAtIndex:i]];
        imgView.frame = CGRectMake(i*(padding + iconWidth), 0, iconWidth, 30);
        [_supportView addSubview:imgView];
    }
    _supportView.center = CGPointMake(_parkNumberView.center.x, _freeAndAllLabel.bottom + _supportView.height / 2 + 4);
    [_parkNumberView addSubview:_supportView];
}

- (void)updateVoteState {
    if ([_item.hasPraise isEqualToString:@"1"]) {
        [_agreeButton setImage:[UIImage imageNamed:@"ic_praise_grey.png"] forState:UIControlStateNormal];
    } else if ([_item.hasPraise isEqualToString:@"0"]) {
        [_projectButton setImage:[UIImage imageNamed:@"ic_disparage_grey.png"] forState:UIControlStateNormal];
    }
}

- (void)clickedNavButton:(UIButton*)button {
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

- (void)priceButtonTouched {
    TPriceViewController* vc = [[TPriceViewController alloc] init];
    vc.parkId = _parkId;
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)clickedPayButton {
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        [TAPIUtility alertMessage:@"请先登录哦~"];
        return;
    }

    //=======当前订单有两个连接
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=currentorder&mobile=%@&comid=%@",[[NSUserDefaults standardUserDefaults] objectForKey:save_phone], _parkId];
    _payRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _payRequest.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    [self.model sendRequest:_payRequest completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        if ([result count]) {
            NSString* state = [result objectForKey:@"state"];
            if ([state isEqualToString:@"0"]) {
                [TAPIUtility alertMessage:@"请等待收费员结算您的订单!"];
            } else if ([state isEqualToString:@"1"]) {
                [self.navigationController popViewControllerAnimated:YES];
//                [[THomeViewController share] requestYuEBaoAndTickets:@{@"info" : result}];
            }
        } else {
            TCollectorsListViewController* vc = [[TCollectorsListViewController alloc] init];
            vc.parkId = _parkId;
            vc.parkName = _parkName;
            [self.navigationController pushViewController:vc animated:YES];
        }
    }];
}

- (void)clickedMonthButton {
    TParkMonthViewController* vc = [[TParkMonthViewController alloc] init];
    vc.parkIds =  @[@([_parkId integerValue])];
    vc.address = _item.address;
    vc.coordinate = _selfLocation;
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)commentButtonTouched {
    TCommentViewController* vc = [[TCommentViewController alloc] init];
    vc.parkId = _parkId;
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)clickedVoteButton:(UIButton*)button {
    if ([_item.hasPraise isEqualToString:@"1"] || [_item.hasPraise isEqualToString:@"0"]) {
        [TAPIUtility alertMessage:@"您已经投过票了,不能再投了哦～" success:NO toViewController:self];
        return;
    } else if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        //没登录的，先登录
        TLoginViewController* login = [[TLoginViewController alloc] init];
        [self.navigationController pushViewController:login animated:YES];
        return;
    }
    NSString* praise = @"";
    if (button == _agreeButton) {
        praise = @"1";
    } else if (button == _projectButton) {
        praise = @"0";
    }
    
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=praise&comid=%@&mobile=%@&praise=%@", _parkId, [[NSUserDefaults standardUserDefaults] objectForKey:save_phone], praise];
    _voteRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _voteRequest.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_voteRequest completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        if ([[result objectForKey:@"info"] isEqualToString:@"1"]) {
            int number = [_agreeButton.titleLabel.text integerValue];
            if ([praise isEqualToString:@"1"]) {
                [_agreeButton setTitle:[NSString stringWithFormat:@"%d", number+1] forState:UIControlStateNormal];
                _item.hasPraise = @"1";
            } else {
                [_projectButton setTitle:[NSString stringWithFormat:@"%d", number+1] forState:UIControlStateNormal];
                _item.hasPraise = @"0";
            }
            [self updateVoteState];
            [TAPIUtility alertMessage:@"投票成功" success:YES toViewController:self];
        } else {
            [TAPIUtility alertMessage:@"投票失败" success:NO toViewController:self];
        }
    }];
}

- (void)clickedPhoneButton {
    if ([_item.mobile isEqualToString:@""]) {
        [TAPIUtility alertMessage:@"该停车场暂未提供电话" success:NO toViewController:self];
        return;
    }
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", _item.mobile]]];
}

- (void)handleTapImgViewGesture:(UITapGestureRecognizer*)gesture {
    if (gesture.view == _parkImgView && _parkImgView.tag == 1) {
        [self showImageView:_parkImgView];
    } else if (gesture.view == _fullImgView) {
        [self hideImageView:_parkImgView];
    }
}

//显示全屏图片
- (void)showImageView :(UIImageView*)orgImgView{
    _imgWindow = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
    _imgWindow.backgroundColor = [UIColor blackColor];
    _imgWindow.alpha = 0;
    
    _imgScrollView = [[UIScrollView alloc] initWithFrame:_imgWindow.frame];
    _imgScrollView.contentSize = _imgScrollView.frame.size;
    _imgScrollView.backgroundColor = [UIColor clearColor];
    _imgScrollView.delegate = self;
    _imgScrollView.maximumZoomScale = 4.0;
    _imgScrollView.minimumZoomScale = 1.0;
    
    _fullImgView = [[UIImageView alloc] initWithImage:orgImgView.image];
    _fullImgView.backgroundColor = [UIColor blackColor];
//    _fullImgView.contentMode = UIViewContentModeScaleAspectFit;
    _fullImgView.frame = [self.view convertRect:orgImgView.frame toView:_imgWindow];
    [_fullImgView addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapImgViewGesture:)]];
    _fullImgView.userInteractionEnabled = YES;
    
    [_imgScrollView addSubview:_fullImgView];
    [_imgWindow addSubview:_imgScrollView];
    [_imgWindow makeKeyAndVisible];
    [UIView animateWithDuration:0.3 animations:^{
        _fullImgView.frame = _imgScrollView.frame;
        _imgWindow.alpha = 1;
    }];
}

//隐藏全屏图片
- (void)hideImageView :(UIImageView*)orgImgView{
    [UIView animateWithDuration:0.3 animations:^{
        _fullImgView.frame = [self.view convertRect:orgImgView.frame toView:_imgWindow];
        _imgWindow.alpha = 0;
    } completion:^(BOOL finished) {
        [_fullImgView removeFromSuperview];
        [_imgScrollView removeFromSuperview];
        _fullImgView = nil;
        _imgScrollView = nil;
        
        //清除 window
        NSMutableArray* windows = [NSMutableArray arrayWithArray:[UIApplication sharedApplication].windows];
        [windows removeObject:_imgWindow];
        //遍历window,选出正常的window
        [windows enumerateObjectsWithOptions:NSEnumerationReverse usingBlock:^(UIWindow* obj, NSUInteger idx, BOOL *stop) {
            if ([obj isKindOfClass:[UIWindow class]] && obj.windowLevel == UIWindowLevelNormal) {
                [obj makeKeyAndVisible];
                *stop = YES;
            }
        }];
    }];
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

#pragma mark UIScrollViewDelegate

- (UIView*)viewForZoomingInScrollView:(UIScrollView *)scrollView {
    return _fullImgView;
}
@end
