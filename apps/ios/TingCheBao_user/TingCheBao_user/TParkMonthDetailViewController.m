//
//  TParkMonthDetailViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-9-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TParkMonthDetailViewController.h"
#import "TAPIUtility.h"
#import "CVAPIRequestModel.h"
#import "TParkMonthDetailItem.h"
#import "TRechargeWaysViewController.h"
#import "TParkMonthDetailTopCell.h"
#import "TParkMonthDetailSectionView.h"
#import "TParkMonthDetailBottomCell.h"
#import "TCommentViewController.h"
#import "TParkMonthConfirmViewController.h"
#import "TLoginViewController.h"

@interface TParkMonthDetailViewController ()<UITableViewDataSource, UITableViewDelegate, TParkMonthDetailBottomCellDelegate, TParkMonthDetailSectionViewDelegate>

@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) CVAPIRequest* request;

@end

@implementation TParkMonthDetailViewController

- (id)init {
    if (self = [super init]) {
        _tableView = [[UITableView alloc] initWithFrame:self.view.frame];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 10)];
        _tableView.allowsSelection = NO;
        _tableView.backgroundColor = RGBCOLOR(225, 225, 225);
        _tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
        
        [self.view addSubview:_tableView];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = light_white_color;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.titleView.text = @"包月详情";
    
    [self requestDetailInfo];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
}

#pragma mark request

- (void)requestDetailInfo {
//        NSString* apiPath = [NSString stringWithFormat:@"getpark.do?action=getproducts&lon=%lf&lat=%lf&parkids=%@&mobile=%@",  _coordinate.longitude, _coordinate.latitude, [_parkIds componentsJoinedByString:@","], [TAPIUtility getValidString:[[NSUserDefaults standardUserDefaults] objectForKey:save_phone]]];
    NSString* apiPath = [NSString stringWithFormat:@"getpark.do?action=getpdetail&pid=%@&mobile=%@", _monthItem.productId, [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        _detailItem = [TParkMonthDetailItem getItemFromDictionary:result];
        [_tableView reloadData];
    }];
}

#pragma mark private

- (void)buyButtonTouched {
    return;
    TRechargeWaysViewController* vc = [[TRechargeWaysViewController alloc] init];
    vc.productId = _monthItem.productId;
//    vc.name = [NSString stringWithFormat:@"%@ %@ %@", _parkName, _item.name, @"一个月"];
//    vc.price = _item.price;
//    vc.startDate = _startDate;
//    vc.longTime = _longTime;
    [self.navigationController pushViewController:vc animated:YES];
}

#pragma mark UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 1;
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.section == 0) {
        static NSString* identifier = @"topCell";
        TParkMonthDetailTopCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
        if (!cell) {
            cell = [[TParkMonthDetailTopCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifier];
        }
        if (_monthItem)
            cell.item = _monthItem;
        return cell;
    } else if (indexPath.section == 1){
        static NSString* identifier = @"bottomCell";
        TParkMonthDetailBottomCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
        if (!cell) {
            cell = [[TParkMonthDetailBottomCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifier];
        }
        cell.delegate = self;
        if (_detailItem && _monthItem)
            [cell setItem:_detailItem monthItem:_monthItem];
        return cell;
    }
    return nil;
}

- (UIView*)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    TParkMonthDetailSectionView* sectionView = [[TParkMonthDetailSectionView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, 40)];
    sectionView.delegate = self;
    sectionView.item = _monthItem;
    return sectionView;
}

#pragma mark UITableViewDelegate
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.section == 0)
        return 200;
    else
        return 630;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    if (section == 0)
        return 0;
    else
        return 40;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.row == 0) {
        
    } else {
    }
}

#pragma mark TParkMonthDetailBottomCellDelegate 

- (void)cellCommentTouched {
    if (!_detailItem.parkId) {
        NSLog(@"parkid is no");
        return;
    }
    TCommentViewController* vc = [[TCommentViewController alloc] init];
    vc.parkId = _detailItem.parkId;
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)sectionBuyButtonTouched {
    //先登录 
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        TLoginViewController* loginVc = [[TLoginViewController alloc] init];
        [self.navigationController pushViewController:loginVc animated:YES];
        return;
    }
    TParkMonthConfirmViewController* vc = [[TParkMonthConfirmViewController alloc] init];
    vc.parkId = _detailItem.parkId;
    vc.item = _monthItem;
    [self.navigationController pushViewController:vc animated:YES];
}

@end
