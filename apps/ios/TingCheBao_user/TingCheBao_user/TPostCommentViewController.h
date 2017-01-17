//
//  TPostCommentViewController.h
//  TingCheBao_user
//
//  Created by apple on 15/5/12.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

//给车场的评论
#import "TBaseViewController.h"
#import "TRechargeWaysViewController.h"

@interface TPostCommentViewController : TBaseViewController

//从评论列表里进入，需要传
@property(nonatomic, retain) NSString* parkId;

@end
