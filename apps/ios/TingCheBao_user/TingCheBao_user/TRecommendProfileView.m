//
//  TRecommendProfileView.m
//  TingCheBao_user
//
//  Created by apple on 15/1/23.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TRecommendProfileView.h"

@interface TRecommendProfileView()

@property(nonatomic, retain) UILabel* titleLabel;
@property(nonatomic, retain) UILabel* contentLabel;

@end
@implementation TRecommendProfileView

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self.centerView.frame = CGRectMake(5, (self.height - 200)/2 - 20, self.width - 2*5, 200);
        
        self.closeButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [self.closeButton setBackgroundImage:[UIImage imageNamed:@"close_gray.png"] forState:UIControlStateNormal];
        self.closeButton.frame = CGRectMake(self.centerView.width - 37, 2, 35, 35);
        [self.closeButton addTarget:self action:@selector(closeButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(2, 20, self.centerView.width - 2*2, 30)];
        _titleLabel.textAlignment = NSTextAlignmentCenter;
        _titleLabel.text = @"收费员奖励";
        _titleLabel.textColor = orange_color;
        _titleLabel.font = [UIFont boldSystemFontOfSize:20];
        
        
        _contentLabel = [[UILabel alloc] initWithFrame:CGRectMake(4, _titleLabel.bottom, self.centerView.width - 8, 124)];
        _contentLabel.text = @"1、下载立得10元，停车收费一键查询\n2、手机收停车费，每单奖2元\n3、开通停车宝新会员，每个奖5元\n4、推荐收费员朋友，每个奖30元\n5、在线使用拿积分，每周积分换奖品，大奖等你拿";
        _contentLabel.numberOfLines = 6;
        
        [self.centerView addSubview:self.closeButton];
        [self.centerView addSubview:_titleLabel];
        [self.centerView addSubview:_contentLabel];
    }
    return self;
}

- (void)closeButtonTouched:(UIButton*)button {
    [self show:NO];
}

- (void)show:(BOOL)show {
    CGRect normalRect = CGRectMake(5, (self.height - 200)/2 - 20, self.width - 2*5, 200);
    CGRect litleRect = CGRectMake(normalRect.origin.x + normalRect.size.width/2, normalRect.origin.y + normalRect.size.height/2, 0, 0);
    if (show) {
        self.hidden = NO;
        self.centerView.frame = litleRect;
        [UIView animateWithDuration:0.3 animations:^{
            self.bgView.alpha = 0.7;
            self.centerView.frame = normalRect;
        }];
    } else {
        [UIView animateWithDuration:0.3 animations:^{
            self.bgView.alpha = 0;
            self.centerView.frame = litleRect;
        } completion:^(BOOL finished) {
            self.hidden = YES;
        }];
    }
}
@end
