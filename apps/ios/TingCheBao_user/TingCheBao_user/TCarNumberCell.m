//
//  TCarNumberCell.m
//  
//
//  Created by apple on 15/7/13.
//
//

#import "TCarNumberCell.h"

@interface TCarNumberCell()

@property(nonatomic, retain) UIImageView* statusView;

@end

@implementation TCarNumberCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        _statusView = [[UIImageView alloc] init];
                       //[UIImage imageNamed:[NSString stringWithFormat:@"carStatus_%@", _item.state]]];
        
        self.textLabel.font = [UIFont systemFontOfSize:15];
        self.detailTextLabel.font = [UIFont systemFontOfSize:12];
        
        [self.contentView addSubview:_statusView];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    CGSize size = T_TEXTSIZE(self.textLabel.text, self.textLabel.font);
    self.textLabel.left = self.imageView.right + 4;
    self.textLabel.width = size.width;
    self.separatorInset = UIEdgeInsetsMake(0, self.textLabel.left, 0, 0);
    _statusView.frame = CGRectMake(self.textLabel.right + 3, (self.contentView.height - 20)/2 + 1, [_item.is_auth isEqualToString:@"-1"] ? 63 : ([_item.is_auth isEqualToString:@"-2"] ? 55 : 45), 20);
    
}


- (void)setItem:(TCarNumberItem *)item {
    _item = item;
    self.imageView.image = [UIImage imageNamed:_item ? @"carNumber.png" : @"add_green.png"];
    self.imageView.image = [TAPIUtility ajustImage:self.imageView.image size:CGSizeMake(20, 20)];
    
    self.textLabel.text = _item ? _item.car_number : @"点击添加车牌";
    self.statusView.image = [UIImage imageNamed:[NSString stringWithFormat:@"carStatus_%@", _item.is_auth]];
    
    
    //is_auht:0未认证，1已认证 2认证中 -1审核不通过 -2异常
    if ([_item.is_auth isEqualToString:@"1"]) {
        //认证通过
        self.detailTextLabel.text = @"可以使用信用额度";
    } else if ([_item.is_auth isEqualToString:@"2"]){
        //在审核
        self.detailTextLabel.text = @"1-3天";
    } else if ([_item.is_auth isEqualToString:@"0"]){
        //未认证
        self.detailTextLabel.text = @"认证可获得信用额度";//如果字太多 放不下
    } else if ([_item.is_auth isEqualToString:@"-1"]){
        //认证失败
        self.detailTextLabel.text = @"请重新认证";
    } else if ([_item.is_auth isEqualToString:@"-2"]){
        //车牌异常
        self.detailTextLabel.text = @"请联系客服";
    } else {
        self.detailTextLabel.text = @"";
    }
    
    if ([_item.is_auth isEqualToString:@"0"] || _item == nil || [_item.is_auth isEqualToString:@"-1"]) {
        self.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
        self.userInteractionEnabled = YES;
    } else {
        self.accessoryType = UITableViewCellAccessoryNone;
        self.userInteractionEnabled = NO;
    }
}
@end
