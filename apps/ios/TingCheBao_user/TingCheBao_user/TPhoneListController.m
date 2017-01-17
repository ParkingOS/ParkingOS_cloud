//
//  TPhoneListController.m
//  
//
//  Created by apple on 15/8/18.
//
//

#import "TPhoneListController.h"

@interface TPhoneListController()<UITableViewDataSource, UITableViewDelegate>

@property(nonatomic, retain) UITableView* tableView;

@end
@implementation TPhoneListController

- (id)init {
    if (self = [super init]) {
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height) style:UITableViewStylePlain];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 10)];
        
        [self.view addSubview:_tableView];
    }
    return self;
}

#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [GL(save_test_phones) count];
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"cell";
    UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier];
    }
    cell.textLabel.text = [GL(save_test_phones) objectAtIndex:indexPath.row];
    cell.accessoryType = [[GL(save_test_phones) objectAtIndex:indexPath.row] isEqualToString:GL(save_phone)] ? UITableViewCellAccessoryCheckmark : UITableViewCellAccessoryNone;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    SL([GL(save_test_phones) objectAtIndex:indexPath.row], save_phone);
    [_tableView reloadData];
}

@end
