//
//  TMyMonthViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-9-13.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TMyMonthViewController.h"
#import "CVAPIRequestModel.h"
#import "TMyMonthItem.h"
#import "TAPIUtility.h"
#import "TMyMonthCell.h"

@interface TMyMonthViewController ()<UITableViewDataSource, UITableViewDelegate>

@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) UILabel* alertLabel;
@property(nonatomic, retain) NSMutableArray* items;
@property(nonatomic, retain) CVAPIRequest* request;

@end

@implementation TMyMonthViewController

- (id)init {
    if (self = [super init]) {
        _items = [NSMutableArray array];
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height)];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 10)];
        _tableView.allowsSelection = NO;
        _tableView.rowHeight = 130;
        
        _alertLabel = [[UILabel alloc] initWithFrame:_tableView.frame];
        _alertLabel.backgroundColor = [UIColor clearColor];
        _alertLabel.text = @"没有包月卡哦~";
        _alertLabel.textAlignment = NSTextAlignmentCenter;
        _alertLabel.hidden = YES;

        [self.view addSubview:_tableView];
        [self.view addSubview:_alertLabel];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.titleView.text = @"我的包月卡";
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self requestMonthInfo];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
}

#pragma mark private

- (void)requestMonthInfo {
    
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=products&mobile=%@",[[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
//    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=products&mobile=%@",@"15801482643"];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        [_items removeAllObjects];
        for (NSDictionary *object in result) {
            TMyMonthItem* item = [TMyMonthItem getItemFromeDictionary:object];
            [_items addObject:item];
        }
        if ([_items count] == 0) {
            _alertLabel.hidden = NO;
        } else {
            _alertLabel.hidden = YES;
        }
        [_tableView reloadData];
    }];
}


#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [_items count];
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"settingCell";
    TMyMonthCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[TMyMonthCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifier];
    }
    [cell setItem:[_items objectAtIndex:indexPath.row]];
    return cell;
}


@end
