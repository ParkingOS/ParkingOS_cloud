//
//  TMyRedPackageViewController.m
//  TingCheBao_user
//
//  Created by yangshaojin on 15-3-10.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TAPIUtility.h"
#import "TWeixin.h"
#import "WXApiObject.h"
#import "MobClick.h"
#import "SDWebImageDownloader.h"
#import "TTicketHelpController.h"
#import "TMyRedPackageViewController.h"

@interface TMyRedPackageViewController ()

@property (nonatomic, readwrite) UIButton *barRightButton;

@property (nonatomic, readwrite) CVAPIRequest* currentOrderReqeust;
@property (nonatomic, readwrite) CVAPIRequestModel* currentOrderModel;

@property (nonatomic, readwrite) NSArray *resultArray;
@property (nonatomic, readwrite) UITableView *tableView;

@property (nonatomic, readwrite) UILabel *titleLabel;

@end

@implementation TMyRedPackageViewController

- (instancetype)init
{
    if (self = [super init]) {
        
        UIBarButtonItem *right = [[UIBarButtonItem alloc] initWithCustomView:self.barRightButton];
        self.navigationItem.rightBarButtonItem = right;
        
        [self.view addSubview:self.titleLabel];
        [self.view addSubview:self.tableView];
        [self requseData];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.titleView.text = @"我的红包";
    self.view.backgroundColor = RGBCOLOR(236, 236, 236);
    // Do any additional setup after loading the view.
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [_currentOrderModel cancel];
    [_currentOrderReqeust cancel];
}

- (UILabel *)titleLabel
{
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, self.view.width, 30)];
        _titleLabel.backgroundColor = [UIColor clearColor];
        _titleLabel.textAlignment = NSTextAlignmentCenter;
        _titleLabel.text = @"你有0个停车券红包待领";
        _titleLabel.font = [UIFont boldSystemFontOfSize:15];
        _titleLabel.textColor = redpackage_color;
    }
    return _titleLabel;
}

- (void)requseData
{
     NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=bonusinfo&mobile=%@",[[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _currentOrderReqeust = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    
    _currentOrderModel = [[CVAPIRequestModel alloc] init];
    _currentOrderModel.delegate = self;
    [_currentOrderModel sendRequest:_currentOrderReqeust completion:^(NSDictionary *result, NSError *error) {
        if (result&&!error) {
            if ([result isKindOfClass:[NSArray class]]) {
                _resultArray = [self orderByResult:(NSArray*)result];
                int number = 0;
                for (int i=0; i<[_resultArray count]; i++) {
                    NSDictionary *dict = [_resultArray objectAtIndex:i];
                    if ([[dict objectForKey:@"state"] integerValue] == 1) {
                        number++;
                    }
                }
                self.titleLabel.text = [NSString stringWithFormat:@"你有%d个停车券红包待领",number];
                [self.tableView reloadData];
            }
        }
    }];
}

- (UIButton *)barRightButton
{
    if (!_barRightButton) {
        _barRightButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_barRightButton setBackgroundColor:[UIColor clearColor]];
        [_barRightButton setBackgroundImage:[UIImage imageNamed:@"help_deepgary.png"] forState:UIControlStateNormal];
        [_barRightButton addTarget:self action:@selector(barRightButtonClick:) forControlEvents:UIControlEventTouchUpInside];
        _barRightButton.frame = CGRectMake(0, 0, 25, 25);
    }
    return _barRightButton;
}

- (void)barRightButtonClick:(UIButton *)sender
{
    TTicketHelpController* helper = [[TTicketHelpController alloc] initWithName:@"停车券帮助" url:[TAPIUtility getNetworkWithUrl:@"ticket.jsp"]];
    [self.navigationController pushViewController:helper animated:YES];
}

- (UITableView *)tableView
{
    if (!_tableView) {
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, 30, self.view.width, self.view.height - 30) style:UITableViewStylePlain];
        _tableView.delegate   = self;
        _tableView.dataSource = self;
        _tableView.rowHeight = 100;
        _tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
        _tableView.backgroundColor = RGBCOLOR(236, 236, 236);
    }
    return _tableView;
}

#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.resultArray count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *cellName = @"RedPackage";
    TRedPackageCell *cell = [tableView dequeueReusableCellWithIdentifier:cellName];
    if (!cell) {
        cell = [[TRedPackageCell alloc] initWithStyle:UITableViewCellStyleValue2 reuseIdentifier:cellName];
    }
    cell.dict = [self.resultArray objectAtIndex:indexPath.row];
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    TRedPackageCell *cell = (TRedPackageCell *)[tableView cellForRowAtIndexPath:indexPath];
    if ([[cell.dict objectForKey:@"state"] integerValue] == 1) {
        TParkTicketPackageViewController *park = [[TParkTicketPackageViewController alloc] init];
        park.boundId = [cell.dict objectForKey:@"id"];
        UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:park];
        [self presentViewController:nav animated:YES completion:nil];
    }
}

- (NSMutableArray*)orderByResult:(NSArray*) items{
    NSArray* newItems = [items sortedArrayUsingComparator:^NSComparisonResult(NSDictionary* obj1, NSDictionary* obj2) {
        if ([[obj1 objectForKey:@"state"] isEqualToString:@"1"]) {
            if ([[obj2 objectForKey:@"state"] isEqualToString:@"1"]) {
                if ([[obj1 objectForKey:@"exptime"] doubleValue] >= [[obj2 objectForKey:@"exptime"] doubleValue]) {
                    return NSOrderedDescending;
                } else {
                    return NSOrderedAscending;
                }
            }
            return NSOrderedAscending;
        } else {
            return NSOrderedDescending;
        }
    }];
    return [NSMutableArray arrayWithArray:newItems];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
