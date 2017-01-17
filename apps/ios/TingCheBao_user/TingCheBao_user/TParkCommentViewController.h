//
//  TParkCommentViewController.h
//  TingCheBao_user
//
//  Created by apple on 15/4/17.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

//车场评论的列表
#import "TBaseViewController.h"

typedef enum {
    Comment_mode_park = 0,
    Comment_mode_collector
} Comment_mode;


@interface TParkCommentViewController : TBaseViewController

@property(nonatomic, assign) Comment_mode mode;
@property(nonatomic, retain) NSString* parkId;
@property(nonatomic, retain) NSString* collectorId;

@end
