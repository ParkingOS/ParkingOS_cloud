//
//  LeftSectionView.h
//  Dog
//
//  Created by apple on 14-7-24.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef enum {
    SETTING_TYPE = 0,
    TRASH_TYPE
} LeftSectionType;

@interface LeftSectionView : UIView

- (void)setText:(NSString*)text type:(LeftSectionType)type;
    
@end
