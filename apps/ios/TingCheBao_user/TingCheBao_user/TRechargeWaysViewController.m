//
//  TRechargeWaysViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-9-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TRechargeWaysViewController.h"
#import "TRechargeResultView.h"
#import "TAccountItem.h"
#import "TAPIUtility.h"
#import "CVAPIRequestModel.h"
#import "TAppDelegate.h"
#import "TCurrentOrderViewController.h"
#import "TMyMonthViewController.h"
#import "TAccountViewController.h"
#import "TViewController.h"
#import "TChooseTicketItem.h"
#import "TChooseTicketsView.h"
#import "MobClick.h"
#import "TShareItem.h"
#import "TShareView.h"
#import "UIImageView+WebCache.h"
#import <TencentOpenAPI/TencentOAuth.h>
#import <TencentOpenAPI/QQApi.h>
#import <TencentOpenAPI/QQApiInterface.h>
#import "TChooseTicketsController.h"
#import "TShakeView.h"
#import "TShareRedPackageView.h"
#import "TParkTicketPackageViewController.h"
#import "TTicketHelpController.h"
#import "THomeViewController.h"
#import "TPayResultView.h"
#import "TCommentToCollectorController.h"
#import "TCurrentOrderViewController.h"
#import "TRechargeWaysCell.h"

#import "TAlipay.h"
#import "TWeixin.h"

@interface TRechargeWaysViewController ()<UITableViewDataSource, UITableViewDelegate, UIAlertViewDelegate, CVAPIModelDelegate, TPayResultViewDelegate,TChooseTicketsViewDelegate, TAlipayDelegate, TWeixinDelegate, TShareViewDelegate, MBProgressHUDDelegate>

@property(nonatomic, retain) UIView* topView;
@property(nonatomic, retain) UILabel* nameHeadLabel;
@property(nonatomic, retain) UILabel* nameLabel;
@property(nonatomic, retain) UILabel* priceHeadLabel;
@property(nonatomic, retain) UILabel* priceLabel;
@property(nonatomic, retain) UIButton* rechargeButton;

@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) TChooseTicketsView* chooseTicketsView;
@property(nonatomic, retain) TShareRedPackageView *shareRedPackageView;


@property(nonatomic, retain) TAccountItem* item;
@property(nonatomic, retain) UIAlertView* alertView;

@property(nonatomic, retain) CVAPIRequestModel* notiRequestModel;

@property(nonatomic, retain) TPayResultView* resultView;

@property(nonatomic, retain) NSTimer* timer;
@property(nonatomic, retain) NSString* yuE;

@property(nonatomic, retain) NSString* ticketId;
@property(nonatomic, retain) NSString* needMoney;
@property(nonatomic, assign) BOOL isMoneyRech;
@property(nonatomic, assign) BOOL selectedZhifubao;
@property(nonatomic, retain) NSString* boundsId;

@property(nonatomic, retain) CVAPIRequest* request;
@property(nonatomic, retain) CVAPIRequest* payRequest;
@property(nonatomic, retain) CVAPIRequest* yuEAndTicketsRequest;
@property(nonatomic, retain) CVAPIRequest* yueRequest;

@property(nonatomic, assign) BOOL firstLoad;
@property(nonatomic, assign) BOOL allowTicket;

@property(nonatomic, retain) NSString* chooseTicketMoney;
@property(nonatomic, retain) NSString* mostTicketMoney;//券最多抵扣的数值
@property(nonatomic, retain) NSString* recommendTicketMoney;//推荐停车券值 最优值

@end

@implementation TRechargeWaysViewController

- (id)init {
    if (self = [super init]) {
        _topView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, 100)];
        _topView.backgroundColor = RGBCOLOR(239,239,244);
        
//        _nameHeadLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 4, 130, 30)];
//        _nameHeadLabel.backgroundColor = [UIColor clearColor];
//        _nameHeadLabel.text = @"产品名称:";
//        _nameHeadLabel.textColor = green_color;
//        _nameHeadLabel.textAlignment = NSTextAlignmentRight;
        
        _nameLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 20, self.view.width, 20)];
        _nameLabel.backgroundColor = [UIColor clearColor];
        _nameLabel.textAlignment = NSTextAlignmentCenter;
        _nameLabel.text = @"0.00元";
        _nameLabel.textColor = green_color;
        _nameLabel.numberOfLines = 1;
        _nameLabel.font = [UIFont systemFontOfSize:18];
        
//        _priceHeadLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 60 + 2, 130, 30)];
//        _priceHeadLabel.backgroundColor = [UIColor clearColor];
//        _priceHeadLabel.text = @"支付金额:";
//        _priceHeadLabel.textColor = green_color;
//        _priceHeadLabel.textAlignment = NSTextAlignmentRight;
        
        _priceLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _nameLabel.bottom + 10, self.view.width, 32)];
        _priceLabel.backgroundColor = [UIColor clearColor];
        _priceLabel.textAlignment = NSTextAlignmentCenter;
        _priceLabel.textColor = green_color;
        _priceLabel.font = [UIFont systemFontOfSize:36];
        
//        [_topView addSubview:_nameHeadLabel];
        [_topView addSubview:_nameLabel];
//        [_topView addSubview:_priceHeadLabel];
        [_topView addSubview:_priceLabel];
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, _topView.bottom, self.view.width, self.view.height - _topView.height - 60) style:UITableViewStyleGrouped];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 10)];
        
        _rechargeButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_rechargeButton setTitle:@"去支付" forState:UIControlStateNormal];
        [_rechargeButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_rechargeButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _rechargeButton.frame = CGRectMake(10, _tableView.bottom + 10, self.view.width - 2*10, 40);
        _rechargeButton.layer.cornerRadius = 5;
        _rechargeButton.clipsToBounds = YES;
        [_rechargeButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _resultView = [[TPayResultView alloc] initWithFrame:self.view.frame];
        _resultView.delegate = self;
        _resultView.hidden = YES;
        
//        [_resultView setObjectWithSucc:NO redPackge:NO];
//        _resultView.hidden = NO;
        
        _shareRedPackageView = [[TShareRedPackageView alloc] initWithFrame:self.view.frame];
        _shareRedPackageView.delegate = self;
        _shareRedPackageView.hidden = YES;
        
        [self.view addSubview:_topView];
        [self.view addSubview:_tableView];
        [self.view addSubview:_rechargeButton];
        [self.view addSubview:_resultView];
//        [self.view addSubview:_shareRedPackageView];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(239,239,244);
    _item = nil;
    _ticketItem = nil;
    _needMoney = @"0.00";
    _selectedZhifubao = YES;
    self.titleView.text = @"选择支付方式";
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    [self initInfo];
    if (_rechargeMode != RechargeMode_addMoney) {
        if (!_firstLoad) {
            [self requestYuEBaoAndTickets];
            _firstLoad = YES;
        }
        
    }
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
//    [_tableView reloadData];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(payReponse:) name: notification_msg_payResult object:nil];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
    [_request cancel];
    [_payRequest cancel];
    [_yuEAndTicketsRequest cancel];
    [_yueRequest cancel];
}

- (void)initInfo {
    _nameLabel.text = _name;
    CGSize size = [TAPIUtility sizeWithFont:_nameLabel.font size:CGSizeMake(_nameLabel.width, 60) text:_nameLabel.text];
    _nameLabel.height = size.height < 30 ? 30 : size.height;
    
    NSString* money = [NSString stringWithFormat:@"%@元", _price];
    _priceLabel.text = money;
}

#pragma mark request

- (void)requestYuEBaoAndTickets {
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=getaccount&mobile=%@&total=%@&orderid=%@&uid=%@&ptype=%@&utype=2", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone], _price, _orderId ? _orderId : @"", _collectorId ? _collectorId : @"", _isReward ? @"4" : (_rechargeMode == RechargeMode_buyTicket ? @"5" : @"")];
    _yuEAndTicketsRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _yuEAndTicketsRequest.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    CVAPIRequestModel* model = [[CVAPIRequestModel alloc] init];
    [model sendRequest:_yuEAndTicketsRequest completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        _yuE = [result objectForKey:@"balance"];
        if ([[result objectForKey:@"tickets"] count]) {
            _ticketItem = [TChooseTicketItem getItemFromDic:[[result objectForKey:@"tickets"] objectAtIndex:0]];
            //最多抵扣值
            _mostTicketMoney = _ticketItem.limit;
            //最优值
            _recommendTicketMoney = _ticketItem.money;
            //打赏时 券最多低扣mostTicketMoney元
            if ([_ticketItem.money floatValue] > [_mostTicketMoney floatValue]) {
                _ticketItem.moneyWhenBig2  = _ticketItem.money;
                _ticketItem.money = _mostTicketMoney;
            }
            _allowTicket = YES;
        } else {
            _ticketItem = nil;
            _allowTicket = NO;
        }
        [self updateData];
    }];
}

- (void)updateData {
    _isMoneyRech = [_price floatValue] > [_ticketItem.money floatValue] + [_yuE floatValue] ? NO : YES;
    if (_isMoneyRech)
        _needMoney = [NSString stringWithFormat:@"%.2f", MAX([_price floatValue] - [_ticketItem.money floatValue], 0.00)];
    else
        _needMoney = [NSString stringWithFormat:@"%.2f", MAX([_price floatValue] - [_ticketItem.money floatValue] - [_yuE floatValue] ,0.00)];
    
    [self.tableView reloadData];
}

- (void)payButtonTouched:(NSString*)text money:(NSString *)money ticketId:(NSString *)ticketId total:(NSString *)total collectorId:(NSString *)collectorId {
    
    money = _needMoney;
    ticketId = _ticketItem ? _ticketItem.ticketId : @"-1";
    total = _price;
    collectorId = _collectorId;
    
    NSString* name = _nameLabel.text;
    NSString* description = nil;
    if (_rechargeMode == RechargeMode_order)
        description = [NSString stringWithFormat:@"%@_2_%@_%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone], _orderId, ticketId];
    else if (_rechargeMode == RechargeMode_collector) {
        description = [NSString stringWithFormat:@"%@_%@_%@_%@_%@%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone], _isReward ? @"4" : @"3",  collectorId, total,  _isReward ? [NSString stringWithFormat:@"%@_", _orderId] : @"", ticketId];
        
    } else if (_rechargeMode == RechargeMode_buyTicket) {
        description = [NSString stringWithFormat:@"%@_5_%@_%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone], _buyTicketMoney, _buyTicketNumber];
    }
    
    if ([_price floatValue] == 0.00) {
        [TAPIUtility alertMessage:@"金额为0元，无需支付" success:NO toViewController:nil];
    } else {
        if (_isMoneyRech) {
            //停车宝余额支付
            NSString* apiPath = nil;
            if (_rechargeMode == RechargeMode_order) {
                NSDictionary* params =  @{@"action" : @"payorder",
                                          @"mobile" : [[NSUserDefaults standardUserDefaults] objectForKey:save_phone],
                                          @"subject" : name,
                                          @"money" : money,
                                          @"ptype" : @"2",
                                          @"orderid" : _orderId,
                                          @"ticketid" : ticketId,
                                          @"version" : @"2"
                                          };
                apiPath = [NSString stringWithFormat:@"carowner.do%@",[CVAPIRequest GETParamString:params]];
            } else if (_rechargeMode == RechargeMode_collector){
                if (_isReward == NO) {
                    apiPath = [NSString stringWithFormat:@"carowner.do?action=epay&mobile=%@&uid=%@&total=%@&ticketid=%@&version=2", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone], collectorId, total, ticketId];
                } else {
                    apiPath = [NSString stringWithFormat:@"carowner.do?action=puserreward&mobile=%@&orderid=%@&ticketid=%@&uid=%@&total=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone], _orderId, ticketId, _collectorId, total];
                }
            } else if (_rechargeMode == RechargeMode_buyTicket) {
                    apiPath = [NSString stringWithFormat:@"carowner.do?action=buyticket&mobile=%@&value=%@&number=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone], _buyTicketMoney, _buyTicketNumber];
            }
            
            
            _yueRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
            _yueRequest.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
            
            [self.model sendRequest:_yueRequest completion:^(NSDictionary *result, NSError *error) {
                if (!result)
                    return;
                if (_isReward) {
                    if ([[result objectForKey:@"result"] isEqualToString:@"1"]) {
                        [TAPIUtility alertMessage:@"打赏成功" success:YES toViewController:self];
                    } else {
                        [TAPIUtility alertMessage:[result objectForKey:@"errmsg"] success:NO toViewController:nil];
                    }
                    return;
                }
                _resultView.hidden = NO;
                _boundsId = [result objectForKey:@"tips"];
                
                //一种是普通余额支付，一种是直接支付
                if ([[result objectForKey:@"result"] isEqualToString:@"1"]) {//成功
                    if (_boundsId && ![_boundsId isEqualToString:@""]) {//红包
                        [_resultView setObjectWithSucc:YES redPackge:YES mode:_rechargeMode];
                    } else {
                        [_resultView setObjectWithSucc:YES redPackge:NO mode:_rechargeMode];
                    }
                    
                    //记下订单号
                    if (_rechargeMode == RechargeMode_collector) {
                        //直付返回的订单编号，订论的时候会用到
                        _orderId = [result objectForKey:@"errmsg"];
                    }
                    //因为没有result页面，变成了红包页面，所以没法控制关闭页面了，所以在这改成“已支付”防止用户再次支付
                    [_rechargeButton setTitle:@"已经支付" forState:UIControlStateNormal];
                    _rechargeButton.enabled = NO;
                    
                    //更新主页的订单状态
                    if (_rechargeMode == RechargeMode_order) {
                        [[THomeViewController share] updateOrderState:0];
                    }
                    
                } else {//失败
                    [_resultView setObjectWithSucc:NO redPackge:NO mode:_rechargeMode];
                }
            }];
        } else if (_selectedZhifubao == YES) {
            //支付宝
            TAlipay* alipay = [TAlipay getInstance];
            [alipay sendWithName:name price:money description:description delegate:self];
        } else if (_selectedZhifubao == NO) {
            //微信
            TWeixin* weixin = [TWeixin getInstance];
            [weixin sendWithName:name price:money description:description delegate:self];
        }
    }
}

#pragma mark private
- (void)buttonTouched:(UIButton*)button {
    
    if (button == _rechargeButton) {
        if (_rechargeMode != RechargeMode_addMoney) {
            
            //非充值
            [self payButtonTouched:nil money:nil ticketId:nil total:nil collectorId:nil];
        
        } else {
            
            //充值
            NSString* description = [NSString stringWithFormat:@"%@_0", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
            NSString* name = @"充值";
            if (_selectedZhifubao == YES) {
                //支付宝
                TAlipay* alipay = [TAlipay getInstance];
                [alipay sendWithName:name price:_price description:description delegate:self];
            } else if (_selectedZhifubao == NO) {
                //微信
                TWeixin* weixin = [TWeixin getInstance];
                [weixin sendWithName:name price:_price description:description delegate:self];
            }
        }
    }
}

//消息通知
- (void)payReponse:(NSNotification*)noti {
    //获取 余额和停车券
    NSString* state = [[noti.userInfo objectForKey:@"info"] objectForKey:@"state"];
    NSString* bonusid = [[noti.userInfo objectForKey:@"info"] objectForKey:@"bonusid"];
    NSString* result =[[noti.userInfo objectForKey:@"info"] objectForKey:@"result"];
    
    NSLog(@"notifi----%@", noti.userInfo);
    
    if ([state isEqualToString:@"2"]) {//===========成功
        
        if ([[noti.userInfo objectForKey:@"mtype"] intValue] == 9) {
            [[TShakeView getInstance] stop];
        }
        
        if (bonusid && ![bonusid isEqualToString:@""]) {//有红包
            _boundsId = bonusid;
            
            [_resultView setObjectWithSucc:YES redPackge:YES mode:_rechargeMode];
            _resultView.hidden = NO;
            
        } else {//没红包
            [_resultView setObjectWithSucc:YES redPackge:NO mode:_rechargeMode];
            _resultView.hidden = NO;
        }
        
        if (_rechargeMode == RechargeMode_collector) {
            //直付返回的订单编号，订论的时候会用到
            _orderId = [[noti.userInfo objectForKey:@"info"] objectForKey:@"orderid"];
        }
        //因为没有result页面，变成了红包页面，所以没法控制关闭页面了，所以在这改成“已支付”防止用户再次支付
        [_rechargeButton setTitle:@"已经支付" forState:UIControlStateNormal];
        _rechargeButton.enabled = NO;
        
        //更新主页的订单状态
        if (_rechargeMode == RechargeMode_order) {
            [[THomeViewController share] updateOrderState:0];
        }
        //保存消息
        [[NSUserDefaults standardUserDefaults] setObject:[noti.userInfo objectForKey:@"msgid"] forKey:save_msg_id];
        
    } else if ([state isEqualToString:@"-1"]) {//===============失败
        
        [_resultView setObjectWithSucc:NO redPackge:NO mode:_rechargeMode];
        _resultView.hidden = NO;
        
        //保存消息
        [[NSUserDefaults standardUserDefaults] setObject:[noti.userInfo objectForKey:@"msgid"] forKey:save_msg_id];
        
    } else if (result) {
        
        //打赏
        if (_isReward) {
            if ([result isEqualToString:@"1"])
                [TAPIUtility alertMessage:@"打赏成功" success:YES toViewController:self];
            else
                [TAPIUtility alertMessage:@"打赏失败" success:NO toViewController:nil];
            return;
        }
        
        //充值或者 包月产品
        if ([result isEqualToString:@"1"]) {
            NSString* bonusid = [[noti.userInfo objectForKey:@"info"] objectForKey:@"bonusid"];
            //是否有充值红包
            if (bonusid && ![bonusid isEqualToString:@""] && [bonusid integerValue] > 0) {
                _boundsId = bonusid;
                [_resultView setObjectWithRechargeRedpackage];
            } else {
                [_resultView setObjectWithSucc:YES redPackge:NO mode:_rechargeMode];
            }
        } else {
            [_resultView setObjectWithSucc:NO redPackge:NO mode:_rechargeMode];
        }
        _resultView.hidden = NO;
    }
}

- (UIImageView*)getSelectedView {
    UIImageView* imageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"rechage_selected.png"]];
    imageView.frame = CGRectMake(0, 0, 15, 15);
    return imageView;
}

#pragma mark UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    if (_rechargeMode == RechargeMode_addMoney || _rechargeMode == RechargeMode_buyTicket) {
        return 1;
    } else {
        return 3;
    }
    return 3;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSInteger number = 0;
    if (_rechargeMode == RechargeMode_addMoney) {
        number = 2;
        
    } else if (_rechargeMode == RechargeMode_buyTicket) {
        if (_isMoneyRech) {
            number = 1;
        } else {
            number = 3;
        }
        
    } else {
        if (!_isMoneyRech)
            number = [@[@(1), @(1), @(3)][section] integerValue];
        else
            number = [@[@(1), @(1), @(1)][section] integerValue];
    }
    return  number;
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"rechargeWaysCell";
    TRechargeWaysCell* cell = [[TRechargeWaysCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifier];
    cell.detailTextLabel.font = [UIFont systemFontOfSize:14];
    
    if (_rechargeMode == RechargeMode_addMoney) {
        
        cell.textLabel.text = [@[@"支付宝支付", @"微信支付"] objectAtIndex:indexPath.row];
        cell.imageView.image = [UIImage imageNamed:[@[@"zhifubao2.png", @"weixin2.png"] objectAtIndex:indexPath.row]];

        if ((indexPath.row == 0 && _selectedZhifubao == YES) || (indexPath.row == 1 && _selectedZhifubao == NO))
            cell.accessoryView = [self getSelectedView];
        
    } else if (_rechargeMode == RechargeMode_buyTicket) {
        cell.textLabel.text = @[@"余额支付", @"支付宝支付", @"微信支付"][indexPath.row];
        cell.imageView.image = [UIImage imageNamed:[@[@"yue.png", @"zhifubao2.png", @"weixin2.png"] objectAtIndex:indexPath.row]];
        if (indexPath.row == 0) {
            cell.detailTextLabel.text = [NSString stringWithFormat:@"¥%.2lf", MIN(MAX([_price floatValue] - [_ticketItem.money floatValue], 0), [_yuE floatValue])];
            cell.accessoryView = [self getSelectedView];
        } else if ((indexPath.row == 1 && _selectedZhifubao == YES) || (indexPath.row == 2 && _selectedZhifubao == NO)) {
            cell.detailTextLabel.text = [NSString stringWithFormat:@"¥%.2lf", [_needMoney floatValue]];
            cell.accessoryView = [self getSelectedView];
        }
        
    } else {
        if (indexPath.section == 0) {
            cell.textLabel.text = @"停车券";
            cell.imageView.image = [UIImage imageNamed:@"ticket.png"];
            if (_ticketItem == nil) {
                
                cell.detailTextLabel.text = _allowTicket ? @"不使用停车券" : @"无可用停车券";
                cell.userInteractionEnabled = _allowTicket;
                
            } else {
                
                //停车券最多抵扣2元,把值临时放在了moneyWhenBig2里
                if ([_ticketItem.moneyWhenBig2 floatValue]> [_ticketItem.money floatValue]) {
                    
//                    BOOL isRecommend = [_ticketItem.moneyWhenBig2 doubleValue] == [_recommendTicketMoney doubleValue];
                    BOOL isRecommend = [_ticketItem.isbuy boolValue];
                    
                    NSMutableAttributedString* attri = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"已选择%@元", _ticketItem.moneyWhenBig2] attributes:@{NSForegroundColorAttributeName : [UIColor grayColor]}];
                    NSAttributedString* attr2 = [[NSAttributedString alloc] initWithString:isRecommend ? @"购买券\n" : @"券\n" attributes:isRecommend ? @{NSForegroundColorAttributeName : green_color}: @{NSForegroundColorAttributeName : [UIColor grayColor]}];
                    NSAttributedString* attr3 = [[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"只可抵扣%@元", _ticketItem.money] attributes:@{NSForegroundColorAttributeName : red_color}];
                    [attri appendAttributedString:attr2];
                    [attri appendAttributedString:attr3];
                    cell.detailTextLabel.attributedText = attri;
                    cell.detailTextLabel.numberOfLines = 0;
                    
                } else {
                    
                    BOOL isRecommend = [_ticketItem.isbuy boolValue];
                    
//                    BOOL isRecommend = [_ticketItem.money doubleValue] == [_recommendTicketMoney doubleValue];
                    NSMutableAttributedString* attri = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"已选择%@元", _ticketItem.money] attributes:@{NSForegroundColorAttributeName : [UIColor grayColor]}];
                    NSAttributedString* attr2 = [[NSAttributedString alloc] initWithString:isRecommend ? @"购买券" : @"券" attributes:isRecommend ? @{NSForegroundColorAttributeName : green_color}: @{NSForegroundColorAttributeName : [UIColor grayColor]}];
                    [attri appendAttributedString:attr2];
//                    NSAttributedString* attr2 = [[NSAttributedString alloc] initWithString:isRecommend ? @"(最优)" : @"" attributes:@{NSForegroundColorAttributeName : green_color}];
//                    [attri appendAttributedString:attr2];
                    cell.detailTextLabel.attributedText = attri;
                }
                cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
                cell.userInteractionEnabled = YES;
            }
            
        } else if (indexPath.section == 1) {
            
            cell.textLabel.text = [NSString stringWithFormat:@"实付款:¥%.2lf", MAX([_price floatValue] - [_ticketItem.money floatValue], 0)];
            cell.detailTextLabel.text = @"";
            cell.userInteractionEnabled = NO;
            
        } else if (indexPath.section == 2) {
            cell.textLabel.text = @[@"余额支付", @"支付宝支付", @"微信支付"][indexPath.row];
            cell.imageView.image = [UIImage imageNamed:[@[@"yue.png", @"zhifubao2.png", @"weixin2.png"] objectAtIndex:indexPath.row]];
            if (indexPath.row == 0) {
                cell.detailTextLabel.text = [NSString stringWithFormat:@"¥%.2lf", MIN(MAX([_price floatValue] - [_ticketItem.money floatValue], 0), [_yuE floatValue])];
                cell.accessoryView = [self getSelectedView];
            } else if ((indexPath.row == 1 && _selectedZhifubao == YES) || (indexPath.row == 2 && _selectedZhifubao == NO)) {
                cell.detailTextLabel.text = [NSString stringWithFormat:@"¥%.2lf", [_needMoney floatValue]];
                cell.accessoryView = [self getSelectedView];
            }
        }
    }
    if (cell.imageView.image)
        cell.imageView.image = [TAPIUtility ajustImage:cell.imageView.image size:CGSizeMake(25, 25)];
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    UITableViewCell* cell = [tableView cellForRowAtIndexPath:indexPath];
    if ([cell.textLabel.text isEqualToString:@"支付宝支付"]) {
        _selectedZhifubao = YES;
    } else if ([cell.textLabel.text isEqualToString:@"微信支付"]) {
        _selectedZhifubao = NO;
    }
    if (_rechargeMode != RechargeMode_addMoney && indexPath.section == 0 && indexPath.row == 0) {
        //停车券
        TChooseTicketsController* vc = [[TChooseTicketsController alloc] init];
        vc.money = _price;
        if (_rechargeMode == RechargeMode_collector) {
            vc.collectorId = _collectorId;
        } else if (_rechargeMode == RechargeMode_order) {
            vc.orderId = _orderId;
        }
        if (_ticketItem)
            vc.ticketId = _ticketItem.ticketId;
        vc.isReward = _isReward;
        [self.navigationController pushViewController:vc animated:YES];
    }
    [self.tableView reloadData];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 1;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 10;
}

#pragma mark TPayResultViewDelegate

//点击 左button
- (void)resultLeftButtonTouched:(UIButton *)button {
    _resultView.hidden = _resultView.redPackage ? NO : YES;//红包时不关闭这个界面
    
    NSString* text = button.titleLabel.text;
    if ([text isEqualToString:@"去评论"]) {
        TCommentToCollectorController* vc = [[TCommentToCollectorController alloc] init];
        vc.orderid = _orderId;
        vc.completer = ^() {
            [self updateCommentState];
        };
        vc.needPushOrderPage = YES;
        
        [self.navigationController pushViewController:vc animated:YES];
    } else if ([text isEqualToString:@"重新支付"]) {
    } else if ([text isEqualToString:@"查看帐户"]) {
        TAccountViewController* vc = [[TAccountViewController alloc] init];
        TViewController* viewController = [TViewController share];
        BOOL flag = NO;
        for (UIViewController* object in viewController.centerController.viewControllers) {
            if ([object isKindOfClass:[TAccountViewController class]]) {
                [viewController.centerController popToViewController:object animated:YES];
                flag = YES;
                return;
            }
        }
        if (flag == NO)
            [viewController.centerController pushViewController:vc animated:NO];
    }
}


//点击 右button
- (void)resultRightButtonTouched:(UIButton *)button {
    //查看礼包
    TParkTicketPackageViewController *park = [[TParkTicketPackageViewController alloc] init];
    park.boundId = _boundsId;
    UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:park];
    [self presentViewController:nav animated:YES completion:nil];
}

#pragma mark TAlipayDelegate

- (void)alipayFail {
    _resultView.hidden = NO;
    [_resultView setObjectWithSucc:NO redPackge:NO mode:_rechargeMode];
}

#pragma mark TWeixinDelegate

- (void)weixinFail {
    _resultView.hidden = NO;
    [_resultView setObjectWithSucc:NO redPackge:NO mode:_rechargeMode];
}



- (void)setTicketItem:(TChooseTicketItem *)ticketItem {
    _ticketItem = ticketItem;
    
    //打赏时 券最多低扣mostTicketMoney元
    
//    if ([_ticketItem.money floatValue] > [_mostTicketMoney doubleValue]) {
    
//        if ([_ticketItem.type intValue] == 1 || [_ticketItem.isbuy intValue] == 1) {
//            _ticketItem.moneyWhenBig2 = _ticketItem.money;
//            _ticketItem.money = [NSString stringWithFormat:@"%d",[_price intValue] - 1];
//            
//        }else{
//            _ticketItem.moneyWhenBig2  = _ticketItem.money;
//            _ticketItem.money = _mostTicketMoney;
//        }
//    }
    
    _ticketItem.moneyWhenBig2 = _ticketItem.money;
    _ticketItem.money = _ticketItem.limit;
    
    [self updateData];
}


- (void)clickedLeftItem:(UIButton *)button {
    if (_rechargeButton.enabled == NO)
        [self.navigationController popToRootViewControllerAnimated:YES];
    else
        [super clickedLeftItem:button];
}

#pragma mark public

- (void)updateCommentState {
    if ([_resultView.leftButton.titleLabel.text isEqualToString:@"去评论"]) {
        _resultView.leftButton.enabled = NO;
        [_resultView.leftButton setTitle:@"已评论" forState:UIControlStateNormal];
    }
    
}


#pragma mark MBProgressHUDDelegate

- (void)hudWasHidden:(MBProgressHUD *)hud {
    if (_isReward) {
        for (UIViewController* vc in self.navigationController.viewControllers) {
            if ([vc isKindOfClass:[TCurrentOrderViewController class]]) {
                [self.navigationController popToViewController:vc animated:YES];
                break;
            }
        }
    } else {
        [self.navigationController popToRootViewControllerAnimated:YES];
    }
}

@end
