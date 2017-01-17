//
//  TAccountViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-9-12.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//¥

#import "TAccountViewController.h"
#import "CVAPIRequestModel.h"
#import "TAPIUtility.h"
#import "TAccountItem.h"
#import "TAccountDetailViewController.h"
#import "THomeViewController.h"
#import "TViewController.h"
#import "TMyMonthViewController.h"
#import "TRechargeViewController.h"
#import "TTicketViewController.h"
#import "TSession.h"
#import "TMyRedPackageViewController.h"
#import "TCarNumberListViewController.h"
#import "TTicketHelpController.h"

@interface TAccountViewController ()<UITableViewDataSource, UITableViewDelegate>

@property(nonatomic, retain) UIView* topView;
@property(nonatomic, retain) UILabel* creditLabel;
@property(nonatomic, retain) UIButton* helpButton;
@property(nonatomic, retain) UILabel* moneyLabel;
@property(nonatomic, retain) UIButton* chargeButton;
@property(nonatomic, retain) UILabel* hurryLabel;
@property(nonatomic, retain) UITableView* tableView;

@property(nonatomic, retain) UIAlertView* inputView;
@property(nonatomic, retain) TAccountItem* item;

@property(nonatomic, retain) NSString* tmpNumber;

@property(nonatomic, retain) CVAPIRequest* request;
@property(nonatomic, retain) CVAPIRequest* exitRequest;
@property(nonatomic, retain) CVAPIRequest* modifyRequest;

@end

@implementation TAccountViewController

- (id)init {
    if (self = [super init]) {
        _topView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, 110)];
        _topView.backgroundColor = [UIColor whiteColor];
        
        NSString* credit = @"信用额度 0/0";
        CGSize creditSize = T_TEXTSIZE(credit, [UIFont systemFontOfSize:15]);
        _creditLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, 8, creditSize.width, 20)];
        _creditLabel.backgroundColor = [UIColor clearColor];
        _creditLabel.text = credit;
        _creditLabel.textColor = green_color;
        _creditLabel.font = [UIFont systemFontOfSize:15];
        _creditLabel.textAlignment = NSTextAlignmentLeft;
        
        _helpButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_helpButton setBackgroundImage:[UIImage imageNamed:@"help_green.png"] forState:UIControlStateNormal];
        _helpButton.frame = CGRectMake(_creditLabel.right + 4, _creditLabel.top , 20, 20);
        [_helpButton addTarget:self action:@selector(helpButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _moneyLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 30, self.view.width - 45, 72)];
        _moneyLabel.backgroundColor = [UIColor clearColor];
        _moneyLabel.text = @"余额";
//        _moneyLabel.font = [UIFont systemFontOfSize:22];
        _moneyLabel.textAlignment = NSTextAlignmentCenter;
        NSString* money = [NSString stringWithFormat:@"余额 ¥%.2f", [[[NSUserDefaults standardUserDefaults] objectForKey:save_account_money] floatValue]];
        NSMutableAttributedString* moneyAttribute = [[NSMutableAttributedString alloc] initWithString:money];
        [moneyAttribute addAttributes:@{NSForegroundColorAttributeName : RGBCOLOR(153, 153, 153), NSForegroundColorAttributeName : [UIFont systemFontOfSize:22]} range:NSMakeRange(0, 3)];
        [moneyAttribute addAttributes:@{NSForegroundColorAttributeName : RGBCOLOR(102, 102, 102), NSFontAttributeName : [UIFont boldSystemFontOfSize:40]} range:NSMakeRange(3, money.length - 3)];
        _moneyLabel.attributedText = moneyAttribute;
        
        
        
        _chargeButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _chargeButton.frame = CGRectMake(_topView.width - 45, 15, 45, _topView.height - 15);
        [_chargeButton addTarget:self action:@selector(chargeButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
        UILabel* label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 25, _topView.height)];
        label.text = @"充值";
        label.textColor = green_color;
        label.font = [UIFont systemFontOfSize:12];
        UIImageView* arrowImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"arrow_right_green.png"]];
        arrowImageView.frame = CGRectMake(label.right + 4, (_topView.height - 12)/2, 6, 12);
        [_chargeButton addSubview:label];
        [_chargeButton addSubview:arrowImageView];
        
        _hurryLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 88 , self.view.width, 20)];
        _hurryLabel.backgroundColor = [UIColor clearColor];
        _hurryLabel.text = @"";
        _hurryLabel.textColor = RGBCOLOR(255, 127, 0);
        _hurryLabel.font = [UIFont systemFontOfSize:13];
        _hurryLabel.textAlignment = NSTextAlignmentCenter;
        
        [_topView addSubview:_creditLabel];
        [_topView addSubview:_helpButton];
        [_topView addSubview:_moneyLabel];
        [_topView addSubview:_chargeButton];
        [_topView addSubview:_hurryLabel];
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, _topView.bottom, self.view.width, self.view.height - _topView.height) style:UITableViewStyleGrouped];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        UIView* bottomView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, 80)];
        UIButton* button = [UIButton buttonWithType:UIButtonTypeCustom];
        [button setTitle:@"退出登录" forState:UIControlStateNormal];
        [button setTitleColor:green_color forState:UIControlStateNormal];
        [button setBackgroundImage:[TAPIUtility imageWithColor:[UIColor whiteColor]] forState:UIControlStateNormal];
        button.frame = CGRectMake(10, 40, self.view.width - 2*10, 40);
        [button addTarget:self action:@selector(loginOutButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
        button.layer.borderColor = green_color.CGColor;
        button.layer.borderWidth = 1;
        button.layer.cornerRadius = 5;
        [bottomView addSubview:button];
        _tableView.tableFooterView = bottomView;
        _tableView.backgroundColor = RGBCOLOR(249, 249, 249);
        
        [self.view addSubview:_topView];
        [self.view addSubview:_tableView];

    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(249, 249, 249);
    self.titleView.text = @"我的帐户";
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self requestAccountInfo];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
    [_exitRequest cancel];
    [_modifyRequest cancel];
}

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    
    CGSize creditSize = T_TEXTSIZE(_creditLabel.text, _creditLabel.font);
    _creditLabel.width = creditSize.width;
    _helpButton.left = _creditLabel.right + 4;
}

#pragma mark private

- (void)loginOutButtonTouched:(id)sender {
    //清除device token
    NSString* apiPath = [NSString stringWithFormat:@"carlogin.do?action=addcid&cid=%@&mobile=%@", @"", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _exitRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _exitRequest.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    [self.model sendRequest:_exitRequest completion:^(NSDictionary *result, NSError *error) {
        //清除用户数据
        [TAPIUtility clearUserInfo];
        
        TViewController* viewController = [TViewController share];
        [viewController.leftController selectHomePage];
    }];
}

- (void)chargeButtonTouched:(UIButton*)button {
    TRechargeViewController* vc = [[TRechargeViewController alloc] init];
    //如果信用额度过低
    if ([_item.limit doubleValue] > 0 &&  [_item.limit_balan doubleValue] < [_item.limit_warn doubleValue]) {
        vc.lackMoney = [NSString stringWithFormat:@"%lf", [_item.limit doubleValue] - [_item.limit_balan doubleValue]];
    }
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)helpButtonTouched:(UIButton*)button {
    TTicketHelpController* vc = [[TTicketHelpController alloc] initWithName:@"信用额度说明" url:@"http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208427120&idx=1&sn=6cb6719bf1520ef5a72097fe5c7fe56a#rd"];
    [self.navigationController pushViewController:vc animated:YES];
}

#pragma mark request

- (void)requestAccountInfo {
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=detail&mobile=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result || [result count] == 0)
            return;
        _item = [TAccountItem getItemFromeDictionary:result];
        
        //信用额度
        NSString* credit = [NSString stringWithFormat:@"信用额度 %@/%@", _item.limit_balan, _item.limit];
        NSMutableAttributedString* creditAttribute = [[NSMutableAttributedString alloc] initWithString:credit];
        [creditAttribute addAttributes:@{NSForegroundColorAttributeName : green_color, NSForegroundColorAttributeName : [UIFont systemFontOfSize:15]} range:NSMakeRange(0, credit.length)];
        if ([_item.limit doubleValue] > 0 &&  [_item.limit_balan doubleValue] < [_item.limit_warn doubleValue]) {        [creditAttribute addAttributes:@{NSForegroundColorAttributeName : red_color, NSForegroundColorAttributeName : [UIFont systemFontOfSize:15]} range:NSMakeRange(5, _item.limit_balan.length)];
        }
        _creditLabel.attributedText = creditAttribute;
        
        //余额
        NSString* money = [NSString stringWithFormat:@"余额 ¥%.2f", [_item.balance floatValue]];
        NSMutableAttributedString* moneyAttribute = [[NSMutableAttributedString alloc] initWithString:money];
        [moneyAttribute addAttributes:@{NSForegroundColorAttributeName : RGBCOLOR(153, 153, 153), NSForegroundColorAttributeName : [UIFont systemFontOfSize:22]} range:NSMakeRange(0, 3)];
        [moneyAttribute addAttributes:@{NSForegroundColorAttributeName : RGBCOLOR(102, 102, 102), NSFontAttributeName : [UIFont boldSystemFontOfSize:40]} range:NSMakeRange(3, money.length - 3)];
        _moneyLabel.attributedText = moneyAttribute;
        
        
        //充值提醒 如果不足10元
        if ([[[NSUserDefaults standardUserDefaults] objectForKey:save_low_recharge] floatValue] > [_item.balance floatValue]) {
            NSString* low_recharge = [[NSUserDefaults standardUserDefaults] objectForKey:save_low_recharge];
            _hurryLabel.text = [NSString stringWithFormat:@"充值提醒:不足%d，请即时充值...", [low_recharge intValue]];
            _hurryLabel.hidden = NO;
        } else {
            _hurryLabel.text = @"";
            _hurryLabel.hidden = YES;
        }
        [_tableView reloadData];
        [self viewDidLayoutSubviews];
        //保存金额
        [[NSUserDefaults standardUserDefaults] setObject:_item.balance ?: @"0" forKey:save_account_money];
   }];
}

#pragma mark UITableViewDelegate

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (section == 0) {
        return 4;
    } else {
        return 2;
    }
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"accountCell";
    UITableViewCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifier];
    }
    cell.backgroundColor = [UIColor whiteColor];
    if (indexPath.section == 0) {
        if (indexPath.row == 0) {
            cell.textLabel.text = @"我的停车券";
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
            cell.imageView.image = [UIImage imageNamed:@"ticket.png"];
        }else if (indexPath.row == 1) {
            cell.textLabel.text = @"我的红包";
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
            cell.imageView.image = [UIImage imageNamed:@"ic_account_redpacket.png"];
            cell.detailTextLabel.text = @"";
        } else if (indexPath.row == 2) {
            cell.textLabel.text = @"帐户明细";
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
            cell.imageView.image = [UIImage imageNamed:@"mingxi.png"];
        } else {
            cell.textLabel.text = @"我的包月卡";
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
            cell.imageView.image = [UIImage imageNamed:@"month.png"];
        }
        
    } else {
        if (indexPath.row == 0) {
            cell.textLabel.text = @"车牌号码";
//            cell.detailTextLabel.text = _item ? _item.carNumber : @"";
//            cell.detailTextLabel.textColor = [UIColor lightGrayColor];
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
            cell.imageView.image = [UIImage imageNamed:@"carNumber.png"];
            
            UIView* accessoryView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, [_item.state isEqualToString:@"-1"] ? 81 : 63, 40)];
            UIImageView* statusView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:[NSString stringWithFormat:@"carStatus_%@", _item.state]]];
            statusView.frame = CGRectMake(0, 10, [_item.state isEqualToString:@"-1"] ? 63 : 45, 20);
            
            UIImageView* imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"arrow_apple.png"]];
            imgView.frame = CGRectMake(statusView.right + 10, (40-13)/2, 7, 13);
            
            [accessoryView addSubview:statusView];
            [accessoryView addSubview:imgView];
            
            cell.accessoryView = accessoryView;
            
        } else {
            cell.textLabel.text = @"电话";
            cell.detailTextLabel.text = _item ? _item.mobile : @"";
            cell.detailTextLabel.textColor = [UIColor lightGrayColor];
            cell.selectionStyle = UITableViewCellSelectionStyleNone;
            cell.imageView.image = [UIImage imageNamed:@"phoneNumber.png"];
        }
    }
    cell.imageView.image = [TAPIUtility ajustImage:cell.imageView.image size:CGSizeMake(25, 25)];
    return cell;
}

- (NSString*)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    if (section == 0) {
        return @"";
    } else {
        return @"个人信息";
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 30;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 1;
}

#pragma mark UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [_tableView deselectRowAtIndexPath:indexPath animated:YES];
    UIViewController* vc = nil;
    if (indexPath.section == 0) {
        switch (indexPath.row) {
            case 0:
                vc = [[TTicketViewController alloc] init];
                [self.navigationController pushViewController:vc animated:YES];
                return;
                break;
            case 1:
                vc = [[TMyRedPackageViewController alloc] init];
                [self.navigationController pushViewController:vc animated:YES];
                break;
            case 2:
                vc = [[TAccountDetailViewController alloc] init];
                [self.navigationController pushViewController:vc animated:YES];
                return;
                break;
            case 3:
                vc = [[TMyMonthViewController alloc] init];
                [self.navigationController pushViewController:vc animated:YES];
                return;
                break;
            default:
                break;
        }
    } else {
        if (indexPath.row == 0) {
            TCarNumberListViewController* vc = [[TCarNumberListViewController alloc] init];
            [self.navigationController pushViewController:vc animated:YES];
        }
    }
}

@end
