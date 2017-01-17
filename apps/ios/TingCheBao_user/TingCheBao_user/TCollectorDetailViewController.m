//
//  TCollectorDetailViewController.m
//  
//
//  Created by apple on 15/6/17.
//
//

#import "TCollectorDetailViewController.h"
#import "CVAPIRequestModel.h"
#import "TCollectorDetailItem.h"
#import "TAPIUtility.h"
#import "TParkCommentViewController.h"

@interface TCollectorDetailViewController ()<UITableViewDataSource, UITableViewDelegate>

@property(nonatomic, retain) UIImageView* photoImgView;
@property(nonatomic, retain) UILabel* colloctorNameLabel;
@property(nonatomic, retain) UILabel* parkNameLabel;
@property(nonatomic, retain) UIButton* phoneButton;

@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) UIButton* payButton;

@property(nonatomic, assign) BOOL firstLoad;
@property(nonatomic, retain) CVAPIRequest* request;

@property(nonatomic, retain) TCollectorDetailItem* item;


@end

@implementation TCollectorDetailViewController

- (id)init {
    if (self = [super init]) {
        _photoImgView = [[UIImageView alloc] initWithFrame:CGRectMake((self.view.width - 80)/2, 20, 80, 80)];
        _photoImgView.image = [UIImage imageNamed:@"collector.png"];
        
        CGFloat phoneWidth = 35;
        
        _colloctorNameLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, _photoImgView.bottom + 5, self.view.width - phoneWidth - 2*10, 30)];
        _colloctorNameLabel.text = @"";
        _colloctorNameLabel.textAlignment = NSTextAlignmentLeft;
        _colloctorNameLabel.font = [UIFont systemFontOfSize:17];
        
        _parkNameLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, _colloctorNameLabel.bottom, _colloctorNameLabel.width, 30)];
        _parkNameLabel.text = @"";
        _parkNameLabel.font = [UIFont systemFontOfSize:12];
        _parkNameLabel.textAlignment = NSTextAlignmentLeft;
        
        _phoneButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_phoneButton setImage:[UIImage imageNamed:@"green_phone.png"] forState:UIControlStateNormal];
        _phoneButton.frame = CGRectMake(self.view.width - 10 - phoneWidth, _colloctorNameLabel.top, phoneWidth, 30);
        [_phoneButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, _parkNameLabel.bottom + 10, self.view.width, 121) style:UITableViewStyleGrouped];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.rowHeight = 40;
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 1)];
        _tableView.tableHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 1, 0.5)];
        _tableView.hidden = YES;
        
        _payButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_payButton setTitle:@"向他付费" forState:UIControlStateNormal];
        [_payButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_payButton setTitleColor:[UIColor blackColor] forState:UIControlStateHighlighted];
        [_payButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _payButton.layer.cornerRadius = 5;
        _payButton.clipsToBounds = YES;
        _payButton.frame = CGRectMake(10, self.view.height - 60, self.view.width - 2*10, 40);
        [_payButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        [self.view addSubview:_photoImgView];
        [self.view addSubview:_colloctorNameLabel];
        [self.view addSubview:_parkNameLabel];
        [self.view addSubview:_phoneButton];
        [self.view addSubview:_tableView];
        [self.view addSubview:_payButton];
        
        
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(249, 249, 249);
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    _colloctorNameLabel.text = _collectorName;
    _parkNameLabel.text = _parkName;
    
    if (!_firstLoad) {
        [self requestCollectorsInfo];
        _firstLoad = YES;
    }
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
}

#pragma mark requeest

- (void)requestCollectorsInfo {
    //清除device token
    NSString* apiPath = [NSString stringWithFormat:@"carinter.do?action=puserdetail&uid=%@", _colletorId];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if([result count] == 0)
            return;
        _item = [TCollectorDetailItem getItemFromDic:result];
        
        _tableView.hidden = NO;
        [_tableView reloadData];
    }];
}

#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (_item) {
        return 3;
    } else {
        return 0;
    }
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell* cell  = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:@"cell"];
    cell.textLabel.font = [UIFont systemFontOfSize:16];
    cell.detailTextLabel.font = [UIFont systemFontOfSize:16];
    
    if (indexPath.row == 0) {
        cell.textLabel.text = [NSString stringWithFormat:@"服务次数: %@", _item.scount];
        cell.detailTextLabel.text = [NSString stringWithFormat:@"最近一周(%@次)", _item.wcount];
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        
    } else if (indexPath.row == 1) {
        
        cell.textLabel.text = [NSString stringWithFormat:@"收到打赏: %@笔", _item.rcount];
        cell.detailTextLabel.text = [NSString stringWithFormat:@"共%@元", _item.money];
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        
    } else if (indexPath.row == 2) {
        
        cell.textLabel.text = [NSString stringWithFormat:@"收到评价: %@", _item.ccount];
        cell.textLabel.textColor = green_color;
        
        
        UIView* accessoryView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 76, 40)];
        
        UILabel* label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 70, 40)];
        label.text = @"查看详情";
        label.textColor = green_color;
        label.font = [UIFont systemFontOfSize:16];
        UIImageView* imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"arrow_right_green.png"]];
        imgView.frame = CGRectMake(label.right, (40-12)/2, 6, 12);
        
        [accessoryView addSubview:label];
        [accessoryView addSubview:imgView];
        
        cell.accessoryView = accessoryView;
    }
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
    
    if (indexPath.row == 2) {
        TParkCommentViewController* vc = [[TParkCommentViewController alloc] init];
        vc.mode = Comment_mode_collector;
        vc.collectorId = _colletorId;
        
        [self.navigationController pushViewController:vc animated:YES];
    }
}

- (void)buttonTouched:(UIButton*)button {
    if (button == _payButton) {
        
        //退到上一个页面，就是输入金额页面
        [self.navigationController popViewControllerAnimated:YES];
        
    } else if (button == _phoneButton) {
        if (![_item.mobile isEqualToString:@""]) {
            
            [EMAlertView showAlertWithTitle:nil message:_item.mobile completionBlock:^(NSUInteger buttonIndex, EMAlertView *alertView) {
                if (buttonIndex != alertView.cancelButtonIndex)
                    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", _item.mobile]]];
            } cancelButtonTitle:@"取消" otherButtonTitles:@"确定", nil];
            
        } else {
            //没有手机号
            [TAPIUtility alertMessage:@"未提供电话号码"];
        }
    }
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
