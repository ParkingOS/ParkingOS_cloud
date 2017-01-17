//
//  TIbeacon.h
//  TingCheBao_user
//
//  Created by apple on 14/12/29.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

@interface TIbeacon : NSObject

@property(nonatomic, assign) double LastTime;

+ (id)getInstance;

- (void)startScan;
- (void)stopScan;

@end
