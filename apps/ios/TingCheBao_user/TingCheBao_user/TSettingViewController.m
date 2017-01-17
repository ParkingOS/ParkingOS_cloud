//
//  TSettingViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-9-12.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TSettingViewController.h"
#import "CVAPIRequestModel.h"
#import "TAPIUtility.h"
#import "TSettingItem.h"
#import "TAboutViewController.h"
#import "TOfflineMapViewController.h"
#import "MobClick.h"

#define padding 10

@interface TSettingViewController ()<UITableViewDataSource, UITableViewDelegate, UIAlertViewDelegate, UIActionSheetDelegate>

@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) UIAlertView* inputView;
@property(nonatomic, retain) TSettingItem* item;

@property(nonatomic, retain) UIActionSheet* autoPaySheet;
@property(nonatomic, retain) UIActionSheet* lowRechargeSheet;

@property(nonatomic, retain) NSArray* autoPayOptions;
@property(nonatomic, retain) NSArray* autoPayKeyOptions;
@property(nonatomic, retain) NSArray* lowRechargeOptions;
@property(nonatomic, retain) NSArray* lowRechargeKeyOptions;

@property(nonatomic, retain) CVAPIRequest* request;
@property(nonatomic, retain) CVAPIRequest* saveRequest;

@end

@implementation TSettingViewController

- (id)init {
    if (self = [super init]) {
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height) style:UITableViewStyleGrouped];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 10)];
        
        [self.view addSubview:_tableView];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.titleView.text = @"设置";
    
    _autoPayOptions = @[@"0元",
                        @"5元",
                        @"10元",
                        @"25元",
                        @"50元",
                        @"总是"];
    _autoPayKeyOptions = @[@"0",
                           @"5",
                           @"10",
                           @"25",
                           @"50",
                           @"-1"];
    _lowRechargeOptions = @[@"小于10元时",
                            @"小于25元时",
                            @"小于50元时",
                            @"小于100元时",
                            @"不提醒"];
    _lowRechargeKeyOptions = @[@"10",
                               @"25",
                               @"50",
                               @"100",
                               @"0"];
    
    [self requestSettingInfo];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
    [_saveRequest cancel];
}

#pragma mark private

//load
- (void)requestSettingInfo {
    
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=getprof&mobile=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        _item = [TSettingItem getItemFromDictionary:result];
        //如果原来用户设过，例如120 那就设为0
        if (![_lowRechargeKeyOptions containsObject:_item.low_recharge]) {
            _item.low_recharge = @"0";
        }
        [[NSUserDefaults standardUserDefaults] setObject:_item.low_recharge forKey:save_low_recharge];
        
        [self.tableView reloadData];
    }];
}

//save
- (void)requestSaveSettingInfo :(NSArray*)info {
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=setprof&mobile=%@&limit_money=%@&low_recharge=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone], info[0], info[1]];
    _saveRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _saveRequest.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_saveRequest completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        _item.auto_cash = [info objectAtIndex:0];
        _item.low_recharge = [info objectAtIndex:1];
        [self.tableView reloadData];
        [[NSUserDefaults standardUserDefaults] setObject:_item.low_recharge forKey:save_low_recharge];
    }];

}



#pragma mark UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    if (_item) {
        return 2;
    } else {
        return 0;
    }
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (!_item) {
        return 0;
    }
    if (section == 0) {
        return 3;
    } else {
        return 1;
    }
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"settingCell";
    UITableViewCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifier];
    }
    cell.backgroundColor = [UIColor whiteColor];
    if (indexPath.section == 0) {
        if (indexPath.row == 0) {
            NSMutableAttributedString* textAttribute = [[NSMutableAttributedString alloc] initWithString:@"自动结算 (仅限会员卡用户)"];
            [textAttribute addAttributes:@{NSForegroundColorAttributeName : orange_color,
                                           NSFontAttributeName : [UIFont systemFontOfSize:13]} range:NSMakeRange(5, 9)];
            cell.textLabel.attributedText = textAttribute;
            cell.detailTextLabel.text = [_autoPayOptions objectAtIndex:[_autoPayKeyOptions indexOfObject:_item.auto_cash]];
        } else if(indexPath.row == 1){
            cell.textLabel.text = @"最低充值提醒";
            cell.detailTextLabel.text = [_lowRechargeOptions objectAtIndex:[_lowRechargeKeyOptions indexOfObject:_item.low_recharge]];
        } else {
            cell.textLabel.text = @"离线地图";
        }
    } else {
        cell.textLabel.text = @"关于";
    }
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    return cell;
}

#pragma mark UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [_tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (indexPath.section == 0) {
        if (indexPath.row == 0) {
            UITableViewCell* cell = [_tableView cellForRowAtIndexPath:indexPath];
            _autoPaySheet = [[UIActionSheet alloc] initWithTitle:@"额度设置" delegate:self cancelButtonTitle:@"取消" destructiveButtonTitle:nil otherButtonTitles:nil];
            for (NSString* option in _autoPayOptions) {
                [_autoPaySheet addButtonWithTitle:option];
            }
            //cancle 是0，所以要加1
            _autoPaySheet.destructiveButtonIndex = [_autoPayOptions indexOfObject:cell.detailTextLabel.text] + 1;
            [_autoPaySheet showInView:self.view];
        } else if (indexPath.row == 1){
            UITableViewCell* cell = [_tableView cellForRowAtIndexPath:indexPath];
            _lowRechargeSheet = [[UIActionSheet alloc] initWithTitle:@"余额充值提醒" delegate:self cancelButtonTitle:@"取消" destructiveButtonTitle:nil otherButtonTitles:nil];
            for (NSString* option in _lowRechargeOptions) {
                [_lowRechargeSheet addButtonWithTitle:option];
            }
            _lowRechargeSheet.destructiveButtonIndex = [_lowRechargeOptions indexOfObject:cell.detailTextLabel.text] + 1;
            [_lowRechargeSheet showInView:self.view];
        } else {
            TOfflineMapViewController* vc = [TOfflineMapViewController new];
            [self.navigationController pushViewController:vc animated:YES];
        }
    } else {
        TAboutViewController* vc = [[TAboutViewController alloc] init];
        [self.navigationController pushViewController:vc animated:YES];
    }
}

#pragma mark UIActionSheetDelegate

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (buttonIndex != actionSheet.cancelButtonIndex && buttonIndex != actionSheet.destructiveButtonIndex) {
        NSString* title = [actionSheet buttonTitleAtIndex:buttonIndex];
        NSMutableArray* array = [NSMutableArray array];
        if (actionSheet == _autoPaySheet) {
            [array addObject: [_autoPayKeyOptions objectAtIndex:[_autoPayOptions indexOfObject:title]]];
            [array addObject:_item.low_recharge];
            
            //自定义事件
            [MobClick event:@"5" attributes:@{@"result" : [array objectAtIndex:0]}];
        } else {
            [array addObject:_item.auto_cash];
            [array addObject:[_lowRechargeKeyOptions objectAtIndex:[_lowRechargeOptions indexOfObject:title]]];
        }
        [self requestSaveSettingInfo:array];
    }
}

@end
