//
//  TCarNumberListViewController.m
//  
//
//  Created by apple on 15/7/13.
//
//

#import "TCarNumberListViewController.h"
#import "CVAPIRequestModel.h"
#import "TCarNumberItem.h"
#import "TCarNumberCell.h"
#import "TCarNumberAddController.h"
#import "TSession.h"

@interface TCarNumberListViewController ()<UITableViewDataSource, UITableViewDelegate>


@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) NSMutableArray* items;

@property(nonatomic, retain) CVAPIRequest* request;

@end

@implementation TCarNumberListViewController

- (id)init {
    if (self = [super init]) {
        _items = [NSMutableArray array];
        
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height) style:UITableViewStyleGrouped];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        UILabel* label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, self.view.width, 30)];
        label.text = @"最多支持一人三车";
        label.textColor = [UIColor grayColor];
        label.textAlignment = NSTextAlignmentCenter;
        label.font = [UIFont systemFontOfSize:12];
        _tableView.tableFooterView = label;
        _tableView.backgroundColor = RGBCOLOR(249, 249, 249);
        
        _tableView.hidden = YES;
        
        [self.view addSubview:_tableView];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.titleView.text = @"编辑车牌";
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self requestInfo];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    [_request cancel];
}

- (void)requestInfo {
    //每次都网络请求
    NSString* apiPath = [NSString stringWithFormat:@"carinter.do?action=getcarnumbs&mobile=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    CVAPIRequestModel* model = [[CVAPIRequestModel alloc] init];
    model.hideNetworkView = YES;
    [model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        _tableView.hidden = NO;
        
        [_items removeAllObjects];
        NSMutableArray* carNumbers = [NSMutableArray array];
        
        for (NSDictionary* dic in result) {
            TCarNumberItem* item = [TCarNumberItem getItemFromeDictionary:dic];
            [_items addObject:item];
            [carNumbers addObject:item.car_number];
        }
        
        //保存本地车牌号
        [[TSession shared] setCarNumbers:carNumbers];
        [_tableView reloadData];
    }];
}
#pragma mark UITableViewDelegate

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return MIN([_items count] + 1, 3);
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"cell";
    TCarNumberCell* cell = [tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[TCarNumberCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifier];
    }
    if (indexPath.row == [_items count]) {
        cell.item = nil;
    } else {
        cell.item = [_items objectAtIndex:indexPath.row];
    }
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [_tableView deselectRowAtIndexPath:indexPath animated:YES];
    TCarNumberItem* item = nil;
    if (indexPath.row < [_items count])
        item = [_items objectAtIndex:indexPath.row];
    NSString* number = item.car_number;
    TCarNumberAddController* vc = [[TCarNumberAddController alloc] initWithCarNumber:number];
    [self.navigationController pushViewController:vc animated:YES];
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
