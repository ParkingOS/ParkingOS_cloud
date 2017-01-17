//
//  TRechargeWaysCell.m
//  
//
//  Created by apple on 15/8/5.
//
//

#import "TRechargeWaysCell.h"

@implementation TRechargeWaysCell

- (void)layoutSubviews {
    [super layoutSubviews];
    
    self.textLabel.top = (self.contentView.height - self.textLabel.height)/2;
}
@end
