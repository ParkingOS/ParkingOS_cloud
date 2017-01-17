//
//  TCurrentOrderViewController.m
//  TingCheBao_user
//
//  Created by apple on 14/10/18.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TCurrentOrderViewController.h"
#import "CVAPIRequestModel.h"
#import "TAPIUtility.h"
#import "TCurrentOrderItem.h"
#import "TParkDetailController.h"
#import "TParkItem.h"
#import "TParkTicketPackageViewController.h"
#import "TRewardViewController.h"
#import "TCommentToCollectorController.h"

#define padding 10

#define unenabel_color [UIColor grayColor]

@interface TCurrentOrderViewController ()<UITableViewDataSource, UITableViewDelegate, UIAlertViewDelegate>

@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) UIButton* commentButton;
@property(nonatomic, retain) UIButton* photoButton;
@property(nonatomic, retain) UIButton* redBadgeButton;

@property(nonatomic, retain) UIView* bottomView;
@property(nonatomic, retain) UIView* lineView;
@property(nonatomic, retain) UILabel* rewardLabel;
@property(nonatomic, retain) UIButton* rewardButton;


@property(nonatomic, retain) TCurrentOrderItem* item;
@property(nonatomic, retain) CVAPIRequest* request;
@property(nonatomic, assign) BOOL firstLoad;
@end

@implementation TCurrentOrderViewController

- (id)init {
    if (self = [super init]) {
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height) style:UITableViewStyleGrouped];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.backgroundColor = self.view.backgroundColor = RGBCOLOR(249, 249, 249);
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 1)];
        _tableView.scrollEnabled = NO;
        
        //----bottomView
        
        
        _bottomView = [[UIView alloc] initWithFrame:CGRectMake(0, self.view.bottom - 85, self.view.width, 85)];
        _bottomView.backgroundColor = [UIColor whiteColor];
        
        _lineView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, 1)];
        _lineView.backgroundColor = [UIColor grayColor];
        _lineView.layer.shadowOffset = CGSizeMake(0, 2);
        
        _rewardLabel = [[UILabel alloc] initWithFrame:CGRectMake(padding, 10, self.view.width - 2*padding, 20)];
        _rewardLabel.text = @"您可以使用停车券给收费员打赏";
        _rewardLabel.font = [UIFont systemFontOfSize:14];
        _rewardLabel.textColor = [UIColor grayColor];
        
        _rewardButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_rewardButton setTitle:@"去打赏" forState:UIControlStateNormal];
        [_rewardButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _rewardButton.layer.cornerRadius = 5;
        _rewardButton.clipsToBounds = YES;
        [_rewardButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _rewardButton.frame = CGRectMake(padding, _rewardLabel.bottom + 4, self.view.width - 2*padding, 40);
        [_rewardButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
//        [_bottomView addSubview:_lineView];
        [_bottomView addSubview:_rewardLabel];
        [_bottomView addSubview:_rewardButton];
        //---end bottomView
        
        _redBadgeButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_redBadgeButton setBackgroundImage:[UIImage imageNamed:@"redBadge.png"] forState:UIControlStateNormal];
        _redBadgeButton.frame = CGRectMake(self.view.width - 50 - 5, isIphone4s ? _bottomView.top - 13 : _bottomView.top - 55, 50, 50);
        [_redBadgeButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        [self.view addSubview:_tableView];
        [self.view addSubview:_bottomView];
        [self.view addSubview:_redBadgeButton];
        
        _tableView.hidden = YES;
        _bottomView.hidden = YES;
        _redBadgeButton.hidden = YES;
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(249, 249, 249);
}


- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.titleView.text = @"订单详情";
    [self requestOrderInfo];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
}

#pragma mark request

- (void)requestOrderInfo {
    NSString* apiPath = @"";
    apiPath = [NSString stringWithFormat:@"carowner.do?action=orderdetail&orderid=%@&mobile=%@",_historyOrderid, [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        
        _item = [TCurrentOrderItem getItemFromDictionary:result];
        
        //更新页面
        _tableView.hidden = NO;
        _bottomView.hidden = NO;
        _redBadgeButton.hidden = ![_item.bonusid isEqualToString:@""] ? NO : YES;
        
        [_rewardButton setTitle:[_item.reward isEqualToString:@"1"] ? @"已打赏" : @"去打赏" forState:UIControlStateNormal];
        [_rewardButton setTitleColor:[_item.reward isEqualToString:@"1"] ? unenabel_color : [UIColor whiteColor] forState:UIControlStateNormal];
        [_rewardButton setBackgroundImage:[TAPIUtility imageWithColor:[_item.reward isEqualToString:@"1"] ? [UIColor whiteColor] : green_color] forState:UIControlStateNormal];
        _rewardButton.layer.borderColor = unenabel_color.CGColor;
        _rewardButton.layer.borderWidth = [_item.reward isEqualToString:@"1"] ? 1 : 0;
        _rewardButton.userInteractionEnabled = [_item.reward isEqualToString:@"1"] ? NO : YES;
        
        [_tableView reloadData];
    }];
}

- (void)buttonTouched:(UIButton*)button {
    if (button == _commentButton) {
        
        TCommentToCollectorController* comment = [[TCommentToCollectorController alloc] init];
        comment.orderid = _item.orderid;
        [self.navigationController pushViewController:comment animated:YES];
        
    } else if (button == _photoButton) {
        
        if (_item.collectorMobile && ![_item.collectorMobile isEqualToString:@""]) {
            UIAlertView* alert = [[UIAlertView alloc] initWithTitle:nil message:_item.collectorMobile delegate:self cancelButtonTitle:@"取消" otherButtonTitles:@"确定", nil];
            alert.delegate = self;
            [alert show];
        } else {
            [TAPIUtility alertMessage:@"未提供电话号码"];
        }
        
    } else if (button == _redBadgeButton) {
        
        //红包
        TParkTicketPackageViewController *park = [[TParkTicketPackageViewController alloc] init];
        park.boundId = _item.bonusid;
        UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:park];
        [self presentViewController:nav animated:YES completion:nil];
        
    } else if (button == _rewardButton) {
        //打赏
        TRewardViewController* vc = [[TRewardViewController alloc] init];
        vc.collectorId = _item.collectorId;
        vc.orderId = _item.orderid;
        [self.navigationController pushViewController:vc animated:YES];
    }
}

#pragma mark UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (section == 0)
        return 5;
    else
        return 1;
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"orderCell";
    UITableViewCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:identifier];
    }
    if (indexPath.section == 0) {
        NSArray* images = @[@"order_home.png", @"ic_order_id.png", @"order_alarm.png", @"order_stop.png"];
        if (indexPath.row < [images count])
            cell.imageView.image = [TAPIUtility ajustImage:[UIImage imageNamed:[images objectAtIndex:indexPath.row]] size:CGSizeMake(25, 25)];
        cell.textLabel.textColor = [UIColor grayColor];
        cell.textLabel.font = [UIFont systemFontOfSize:15];
        
        if (indexPath.row == 0) {
            cell.textLabel.text = _item.parkname;
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
        } else if (indexPath.row == 1) {
            cell.textLabel.text = [NSString stringWithFormat:@"订单号: %@", _item.orderid];
            
            cell.selectionStyle = UITableViewCellSelectionStyleNone;
        } else if (indexPath.row == 2) {
            cell.textLabel.text = @"入场时间";
            if ([_item.ctype isEqualToString:@"4"]) {
                cell.textLabel.text = @"直接付费";
            } else {
                NSDate* date = [NSDate dateWithTimeIntervalSince1970:[_item.btime integerValue]];
                NSDate* eDate = [NSDate dateWithTimeIntervalSince1970:[_item.etime integerValue]];
                NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
                [formatter setDateFormat:@"MM/dd HH:mm"];
                
                cell.textLabel.text = [NSString stringWithFormat:@"%@ — %@", [formatter stringFromDate:date], [formatter stringFromDate:eDate]];
            }
            cell.selectionStyle = UITableViewCellSelectionStyleNone;
        } else if (indexPath.row == 3) {
            NSString* duration = [TAPIUtility getDuration:[_item.etime intValue] - [_item.btime intValue]];
            cell.textLabel.text = [NSString stringWithFormat:@"已停%@", duration];
            cell.selectionStyle = UITableViewCellSelectionStyleNone;
        } else if (indexPath.row == 4) {
            NSMutableAttributedString* attri = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"停车费: ¥%@", _item.total] attributes:@{NSForegroundColorAttributeName : [UIColor grayColor], NSFontAttributeName : [UIFont systemFontOfSize:14]}];
            [attri addAttributes:@{NSForegroundColorAttributeName: green_color, NSFontAttributeName : [UIFont systemFontOfSize:24]} range:NSMakeRange(5, attri.string.length - 5)];
            cell.textLabel.attributedText = attri;
            cell.selectionStyle = UITableViewCellSelectionStyleNone;
        }
    } else if (indexPath.section == 1) {
        cell.textLabel.textColor = green_color;
        cell.textLabel.font = [UIFont systemFontOfSize:15];
        cell.textLabel.text = [NSString stringWithFormat:@"%@: %@", _item.collectorId, _item.collectorName];
        
        
        //comment
        BOOL allowComment = [_item.comment isEqualToString:@"1"] ? NO : YES;
        UIView* accessoryView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, allowComment ? 80 : 90, 30)];
        
        _commentButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_commentButton setTitle: !allowComment ? @"已评价" : @"评价" forState:UIControlStateNormal];
        [_commentButton setTitleColor:allowComment ? green_color : unenabel_color forState:UIControlStateNormal];
        _commentButton.titleLabel.font = [UIFont systemFontOfSize:13];
        _commentButton.layer.borderColor = allowComment ? green_color.CGColor : unenabel_color.CGColor;
        _commentButton.layer.borderWidth = 1;
        _commentButton.layer.cornerRadius = 3;
        _commentButton.frame = CGRectMake(0, 0, allowComment ? 35 : 45, 30);
        _commentButton.clipsToBounds = YES;
        [_commentButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _commentButton.userInteractionEnabled = allowComment ? YES : NO;
        
        
        
        _photoButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_photoButton setBackgroundImage:[UIImage imageNamed:@"green_phone.png"] forState:UIControlStateNormal];
        _photoButton.frame = CGRectMake(accessoryView.width - 35, 0, 35, 30);
        [_photoButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        [accessoryView addSubview:_commentButton];
        [accessoryView addSubview:_photoButton];
        
        cell.accessoryView = accessoryView;
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
    }
    return cell;
}

//- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
//}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [_tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (indexPath.row == 0 && indexPath.section == 0) {
        TParkDetailController* vc = [[TParkDetailController alloc] init];
        vc.parkId = _item.parkid;
        vc.parkName = _item.parkname;
        [self.navigationController pushViewController:vc animated:YES];
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 30;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 1;
}

- (NSString*)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    if (section == 1) {
        return @"车场收款人";
    }
    return nil;
}



- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (buttonIndex != alertView.cancelButtonIndex) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", _item.collectorMobile]]];
    }
}
@end
