//
//  TRecommendHistoryController.m
//  TingCheBao_user
//
//  Created by apple on 14/12/27.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TRecommendHistoryController.h"
#import "TAPIUtility.h"
#import "CVAPIRequestModel.h"
#import "TRecommendHistoryItem.h"
#import "TRecommendHistoryCell.h"
#import "TAccountViewController.h"

#define padding 15

@interface TRecommendHistoryController()<UITableViewDataSource, UITableViewDelegate>

@property(nonatomic, retain) UIImageView* moneyImgView;
@property(nonatomic, retain) UILabel* moneyLabel;
@property(nonatomic, retain) UILabel* queryLabel;
@property(nonatomic, retain) UITableView* tableView;

@property(nonatomic, retain) UILabel* noNormalLabel;
@property(nonatomic, retain) UIImageView* noNormalImgView;

@property(nonatomic, retain) NSMutableArray* items;
@property(nonatomic, retain) CVAPIRequest* request;

@end
@implementation TRecommendHistoryController

- (id)init {
    if (self = [super init]) {
        _moneyImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"recommend_money.png"]];
        _moneyImgView.frame = CGRectMake(self.view.width - 230, padding, 20, 25);
        
        _moneyLabel = [[UILabel alloc] initWithFrame:CGRectMake(_moneyImgView.right + 4, padding, 145, 25)];
        NSMutableAttributedString* moneyAttri = [[NSMutableAttributedString alloc] initWithString:@"共获得奖励 0 元"];
        [moneyAttri addAttributes:@{NSForegroundColorAttributeName : green_color} range:NSMakeRange(6, 1)];
        _moneyLabel.attributedText = moneyAttri;
        _moneyLabel.font = [UIFont systemFontOfSize:17];
        _moneyLabel.backgroundColor = [UIColor clearColor];
        _moneyLabel.textAlignment = NSTextAlignmentLeft;
        
        _queryLabel = [[UILabel alloc] initWithFrame:CGRectMake(self.view.width - 65, padding, 65, 25)];
        _queryLabel.backgroundColor = [UIColor clearColor];
        _queryLabel.font = [UIFont systemFontOfSize:17];
        NSAttributedString* attrString = [[NSAttributedString alloc] initWithString:@"去查看" attributes:@{NSUnderlineStyleAttributeName : @(NSUnderlineStyleSingle), NSForegroundColorAttributeName : RGBCOLOR(69, 150, 237)}];
        _queryLabel.attributedText = attrString;
        _queryLabel.userInteractionEnabled = YES;
        UITapGestureRecognizer* tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapGesture:)];
        [_queryLabel addGestureRecognizer:tapGesture];
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, _moneyLabel.bottom, self.view.width, self.view.height - _moneyLabel.height) style:UITableViewStyleGrouped];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.backgroundColor = RGBCOLOR(235, 235, 235);
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 1)];
        _tableView.allowsSelection = NO;
        
        _noNormalImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"img_page_null.png"]];
        _noNormalImgView.frame = CGRectMake((self.view.width - 200)/2, 150, 200, 100);
        
        _noNormalLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _noNormalImgView.bottom + 4, self.view.width, 25)];
        _noNormalLabel.text = @"您还没有推荐收费员,快去推荐吧!";
        _noNormalLabel.textColor = noData_alert_color;
        _noNormalLabel.font = [UIFont systemFontOfSize:14];
        _noNormalLabel.textAlignment = NSTextAlignmentCenter;
        
        //hide
        _noNormalLabel.hidden = _noNormalImgView.hidden = YES;
        
        [self.view addSubview:_moneyImgView];
        [self.view addSubview:_moneyLabel];
        [self.view addSubview:_queryLabel];
        
        [self.view addSubview:_tableView];
        
        [self.view addSubview:_noNormalImgView];
        [self.view addSubview:_noNormalLabel];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(235, 235, 235);
    _items = [NSMutableArray array];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.titleView.text = @"推荐记录";
    [self requestOrderInfo];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
}

#pragma mark request

- (void)requestOrderInfo {
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=recominfo&mobile=%@",[[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        [_items removeAllObjects];
        if ([result count] == 0) {
            _tableView.hidden = YES;
            _noNormalLabel.hidden = _noNormalImgView.hidden = NO;
        } else {
            _tableView.hidden = NO;
            _noNormalLabel.hidden = _noNormalImgView.hidden = YES;
            
            int i = 0;
            for (NSDictionary* dic in result) {
                TRecommendHistoryItem* item = [TRecommendHistoryItem getItemFromDic:dic];
                [_items addObject:item];
                if ([item.state isEqualToString:@"1"])
                    i += 30;
                else
                    i += 5;
            }
            NSString* money = [NSString stringWithFormat:@"%d", i];
            NSMutableAttributedString* moneyAttri = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"共获得奖励 %@ 元", money]];
            [moneyAttri addAttributes:@{NSForegroundColorAttributeName : green_color} range:NSMakeRange(6, money.length)];
            _moneyLabel.attributedText = moneyAttri;
            
            [_tableView reloadData];
        }
    }];
}

#pragma mark private 

- (void)handleTapGesture:(UITapGestureRecognizer*)gesture {
    TAccountViewController* vc = [[TAccountViewController alloc] init];
    [self.navigationController pushViewController:vc animated:YES];
}

#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return  [_items count];
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"cell";
    TRecommendHistoryCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
            cell = [[TRecommendHistoryCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier];
    }
    [cell setItem:[_items objectAtIndex:indexPath.row]];
    return cell;
}

@end
