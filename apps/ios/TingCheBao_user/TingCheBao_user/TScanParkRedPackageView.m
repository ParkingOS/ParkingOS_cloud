//
//  TScanParkRedPackageView.m
//  
//
//  Created by apple on 15/7/22.
//
//

#import "TScanParkRedPackageView.h"

@interface TScanParkRedPackageView()

@property(nonatomic, retain) UILabel* successLabel;
@property(nonatomic, retain) UIImageView* imgView;
@property(nonatomic, retain) UILabel* moneyHeadLabel;
@property(nonatomic, retain) UILabel* moneyLabel;
@property(nonatomic, retain) UILabel* parkHeadLabel;
@property(nonatomic, retain) UILabel* parkLabel;
@property(nonatomic, retain) UIButton* detailButton;

@end
@implementation TScanParkRedPackageView

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self.centerView.frame = CGRectMake(20, (self.height - 410)/2, self.width - 20*2, 410);
        self.closeButton.frame = CGRectMake(self.centerView.right - 20, self.centerView.top - 20, 40, 40);
        
        _successLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 20, self.centerView.width, 30)];
        _successLabel.text = @"领取车场专用券成功";
        _successLabel.textColor = gray_color;
        _successLabel.textAlignment = NSTextAlignmentCenter;
        _successLabel.font = [UIFont systemFontOfSize:18];
        
        _imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ticket_park.png"]];
        _imgView.frame = CGRectMake((self.centerView.width - 180)/2, _successLabel.bottom + 10, 180, 190);
        
        _moneyHeadLabel = [[UILabel alloc] initWithFrame:CGRectMake(isIphoneNormal ? 55 : 65, _imgView.bottom + 30, 100, 20)];
        _moneyHeadLabel.text = @"专用券金额:";
        _moneyHeadLabel.textColor = gray_color;
        _moneyHeadLabel.font = [UIFont systemFontOfSize:15];
        
        _moneyLabel = [[UILabel alloc] initWithFrame:CGRectMake(_moneyHeadLabel.right, _moneyHeadLabel.top, self.centerView.width - _moneyHeadLabel.right, 20)];
        _moneyLabel.text = @"0元";
        _moneyLabel.textColor = red_color;
        _moneyLabel.font = [UIFont systemFontOfSize:15];
        
        _parkHeadLabel = [[UILabel alloc] initWithFrame:CGRectMake(_moneyHeadLabel.left, _moneyHeadLabel.bottom, _moneyHeadLabel.width, 20)];
        _parkHeadLabel.text = @"可使用停车场:";
        _parkHeadLabel.textColor = gray_color;
        _parkHeadLabel.font = [UIFont systemFontOfSize:15];
        
        _parkLabel = [[UILabel alloc] initWithFrame:CGRectMake(_parkHeadLabel.right, _parkHeadLabel.top, _moneyLabel.width, 40)];
        _parkLabel.text = @"";
        _parkLabel.textColor = red_color;
        _parkLabel.font = [UIFont systemFontOfSize:15];
        _parkLabel.numberOfLines = 0;
        
        _detailButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_detailButton setTitle:@"代金券详情" forState:UIControlStateNormal];
        [_detailButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_detailButton setBackgroundImage:[TAPIUtility imageWithColor:red_color] forState:UIControlStateNormal];
        _detailButton.frame = CGRectMake(10, _parkLabel.bottom + 10, self.centerView.width - 2*10, 40);
        [_detailButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _detailButton.layer.cornerRadius = 5;
        _detailButton.clipsToBounds = YES;
        
        [self.centerView addSubview:_successLabel];
        [self.centerView addSubview:_imgView];
        [self.centerView addSubview:_moneyHeadLabel];
        [self.centerView addSubview:_moneyLabel];
        [self.centerView addSubview:_parkHeadLabel];
        [self.centerView addSubview:_parkLabel];
        [self.centerView addSubview:_detailButton];
        
    }
    return self;
}

#pragma mark super 重写
- (void)closeButtonTouched:(UIButton*)button {
    [self show:NO];
}

#pragma mark private

- (void)buttonTouched:(UIButton*)button {
    [self show:NO];
    if (_completeHandle) {
        _completeHandle();
    }
}

#pragma mark public

- (void)setItem:(TScanParkRedPackageItem *)item {
    _item = item;
    _successLabel.text = [NSString stringWithFormat:@"领取%@(%@)的车场专用券成功", _item.collectorName, _item.collectorId];
    _moneyLabel.text = [NSString stringWithFormat:@"%@元", _item.money];
    _parkLabel.text = _item.cname;
    CGSize size = [TAPIUtility sizeWithFont:_parkLabel.font size:CGSizeMake(_parkLabel.width, 40) text:_parkLabel.text];
    _parkLabel.height = size.height;
}
@end
