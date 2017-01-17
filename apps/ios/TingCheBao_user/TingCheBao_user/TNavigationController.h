//
//  TNavigationController.h
//  TingCheBao_user
//
//  Created by apple on 15/3/10.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

//解决ios7上出现连续pop多次的问题
#import <UIKit/UIKit.h>

@interface TNavigationController : UINavigationController<UINavigationControllerDelegate>

@property(nonatomic, assign) BOOL shouldIgnorePushingViewControllers;

@end
