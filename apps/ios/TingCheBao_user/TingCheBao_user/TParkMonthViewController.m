//
//  TParkMonthViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-9-16.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TParkMonthViewController.h"
#import "CVAPIRequestModel.h"
#import "TAPIUtility.h"
#import "TParkMonthCell.h"
#import "TParkMonthItem.h"
#import "TParkAllMonthItem.h"
#import "TParkMonthHeaderView.h"
#import "TParkMonthDetailViewController.h"
#import "TParkMonthConfirmViewController.h"
#import "TLoginViewController.h"

#define padding  10

@interface TParkMonthViewController ()<UITableViewDataSource, UITableViewDelegate, TParkMonthCellDelegate>

//@property(nonatomic, retain) UILabel* addressLabel;
@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) UIImageView* imgView;
@property(nonatomic, retain) UILabel* alertLabel;
@property(nonatomic, retain) NSMutableArray* items;
@property(nonatomic, retain) CVAPIRequest* request;

@end

@implementation TParkMonthViewController

-(id)init {
    if (self = [super init]) {
        
//        _addressLabel = [[UILabel alloc] initWithFrame:CGRectMake(padding, 4, self.view.width - 2*padding, 40)];
//        _addressLabel.text = @"";
//        _addressLabel.backgroundColor = [UIColor clearColor];
//        _addressLabel.hidden = YES;
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, padding, self.view.width, self.view.height  - padding*2) style:UITableViewStylePlain];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 10)];
        _tableView.rowHeight = 126;
        _tableView.sectionHeaderHeight = 40;
        _tableView.backgroundColor = RGBCOLOR(249, 249, 249);
        if (getOS >= 7.0)
            _tableView.separatorInset = UIEdgeInsetsZero;
        if (getOS >= 8.0)
            _tableView.layoutMargins = UIEdgeInsetsZero;
//        UIView* bottomView = [[UIView alloc] initWithFrame:CGRectMake(padding, _tableView.top, _tableView.width, 1)];
//        bottomView.backgroundColor = light_white_color;
//        _tableView.tableFooterView = bottomView;
        _tableView.hidden = YES;
        
        _imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"img_page_null.png"]];
        _imgView.frame = CGRectMake((self.view.width - 200)/2, 100, 200, 100);
        _imgView.hidden = YES;
        
        _alertLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _imgView.bottom + 4, self.view.width, 30)];
        _alertLabel.backgroundColor = [UIColor clearColor];
        _alertLabel.text = @"暂未提供包月产品哦~";
        _alertLabel.textColor = noData_alert_color;
        _alertLabel.textAlignment = NSTextAlignmentCenter;
        _alertLabel.hidden = YES;
        
//        [self.view addSubview:_addressLabel];
        [self.view addSubview:_tableView];
        [self.view addSubview:_imgView];
        [self.view addSubview:_alertLabel];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(249, 249, 249);
    self.titleView.text = @"包月产品";
    _items = [NSMutableArray array];//不要写到viewWillAppeare里 否则如果加载失败，没有数据，点击cell会崩溃
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
//    _addressLabel.text = _address;
    [self requestParkMonthInfo];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
}

#pragma mark private

- (void)requestParkMonthInfo {
    NSString* apiPath = [NSString stringWithFormat:@"getpark.do?action=getproducts&lon=%lf&lat=%lf&parkids=%@&mobile=%@",  _coordinate.longitude, _coordinate.latitude, [_parkIds componentsJoinedByString:@","], [TAPIUtility getValidString:[[NSUserDefaults standardUserDefaults] objectForKey:save_phone]]];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        
        [_items removeAllObjects];
        for (NSDictionary *object in result) {
            TParkAllMonthItem* item = [TParkAllMonthItem getItmeFromDictionary:object];
            [_items addObject:item];
        }
        if ([_items count] == 0) {
//            _addressLabel.hidden = YES;
            _tableView.hidden = YES;
            _alertLabel.hidden = NO;
            _imgView.hidden = NO;
        } else {
//            _addressLabel.hidden = NO;
            _tableView.hidden = NO;
            _alertLabel.hidden = YES;
            _imgView.hidden = YES;
        }
        _tableView.hidden = NO;
        [_tableView reloadData];
    }];
}


#pragma mark UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return [_items count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    TParkAllMonthItem* item = [_items objectAtIndex:section];
    return [item.monthProducts count];
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"parkMonthCell";
    TParkMonthCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[TParkMonthCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifier];
    }
    cell.delegate = self;
    if (getOS >= 8.0)
        cell.layoutMargins = UIEdgeInsetsZero;
    TParkAllMonthItem* item = [_items objectAtIndex:indexPath.section];
    [cell setItem:[item.monthProducts objectAtIndex:indexPath.row]];
    return cell;
}

- (UIView*)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    TParkAllMonthItem* item = [_items objectAtIndex:section];
    TParkMonthHeaderView* view = [[TParkMonthHeaderView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, 40)];
    view.nameLabel.text = item.company_name;
    view.distanceLabel.text = [NSString stringWithFormat:@"%@km", item.distance];
    view.alpha = 0.9;
    view.backgroundColor = self.view.backgroundColor;
    return view;
}

#pragma mark UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [_tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    TParkMonthDetailViewController* vc = [[TParkMonthDetailViewController alloc] init];
    TParkAllMonthItem* item = [_items objectAtIndex:indexPath.section];
    vc.monthItem = [item.monthProducts objectAtIndex:indexPath.row];
    [self.navigationController pushViewController:vc animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 40;
}
//设成最少，但是不能为0
- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 1;
}

#pragma mark TParkMonthCellDelegate

- (void)buyButtonTouched:(TParkMonthItem *)item cell:(TParkMonthCell*)cell {
    //先登录
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        TLoginViewController* loginVc = [[TLoginViewController alloc] init];
        [self.navigationController pushViewController:loginVc animated:YES];
        return;
    }
    NSIndexPath* path = [_tableView indexPathForCell:cell];
    TParkMonthConfirmViewController* vc = [[TParkMonthConfirmViewController alloc] init];
    vc.parkId = [[_parkIds objectAtIndex:path.section] stringValue];
    vc.item = item;
    [self.navigationController pushViewController:vc animated:YES];
}
@end
