//
//  TParkInfoView.h
//  TingCheBao_user
//
//  Created by apple on 15/3/7.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

//当点击地图上标注的弹出这个view
#import <UIKit/UIKit.h>
#import "TParkItem.h"

@protocol TParkInfoViewDelegate <NSObject>

- (void)parkInfoViewWithItem:(TParkItem*)item text:(NSString*)text;

@end
@interface TParkInfoView : UIView

@property(nonatomic, retain) TParkItem* item;
@property(nonatomic, unsafe_unretained) id<TParkInfoViewDelegate>delegate;

@end
