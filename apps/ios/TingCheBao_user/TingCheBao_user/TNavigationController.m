//
//  TNavigationController.m
//  TingCheBao_user
//
//  Created by apple on 15/3/10.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TNavigationController.h"

@interface TNavigationController()
{
}

@end

@implementation TNavigationController

-(instancetype)init {
    self = [super init];
    self.delegate=self;
    
    return self;
    
}

-(void)pushViewController:(UIViewController *)viewController animated:(BOOL)animated
{
    if (!_shouldIgnorePushingViewControllers)
    {
        [super pushViewController:viewController animated:animated];
    }
    
    _shouldIgnorePushingViewControllers = YES;
}

- (void)didShowViewController:(UIViewController *)viewController animated:(BOOL)animated
{
    _shouldIgnorePushingViewControllers = NO;
}

@end
