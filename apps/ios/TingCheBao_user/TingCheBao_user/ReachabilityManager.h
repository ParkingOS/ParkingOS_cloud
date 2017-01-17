//  ReachabilityManager.h
//  TingCheBao
//
//  Created by apple on 14-7-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Reachability.h"

@interface ReachabilityManager : NSObject{

    Reachability* hostReach;
    Reachability* internetReach;
    Reachability* wifiReach;
    
    int internetReachable;
    int wifiReachable;
    int hostReachable;
    
}
+ (id) sharedManager;

- (BOOL)isInternetReachable;
- (BOOL)isWifiReachable;
- (BOOL)isHostReachable;
- (BOOL)isReachableViaWWAN;
- (BOOL)isReachable;


- (void) resetHostReachability:(NSString*) host;

@end
