//
//  ReachabilityManager.m
//  TingCheBao
//
//  Created by apple on 14-7-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "ReachabilityManager.h"

@interface ReachabilityManager ()

- (void) updateInterfaceWithReachability: (Reachability*) curReach;

@end


@implementation ReachabilityManager

static ReachabilityManager* _manager;

+ (id) sharedManager {
	
    if (_manager == nil) {
		_manager = [[ReachabilityManager alloc] init];
	}
    
	return _manager;
}


- (id)init
{
    self = [super init];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(reachabilityChanged:) name:kReachabilityChangedNotification object:nil];
        
        //Change the host name here to change the server your monitoring
        hostReach = [Reachability reachabilityWithHostName: @""];
        [hostReach startNotifier];
        [self updateInterfaceWithReachability: hostReach];
        
        internetReach = [Reachability reachabilityForInternetConnection];
        [internetReach startNotifier];
        [self updateInterfaceWithReachability: internetReach];
        
        wifiReach = [Reachability reachabilityForLocalWiFi];
        [wifiReach startNotifier];
        [self updateInterfaceWithReachability: wifiReach];
    }
    
    return self;
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}


- (void) configureTextField: (UITextField*) textField imageView: (UIImageView*) imageView reachability: (Reachability*) curReach
{
    NetworkStatus netStatus = [curReach currentReachabilityStatus];
    BOOL connectionRequired= [curReach connectionRequired];
    NSString* statusString= @"";
    switch (netStatus)
    {
        case NotReachable:
        {
            statusString = @"Access Not Available";
            imageView.image = [UIImage imageNamed: @"stop-32.png"] ;
            //Minor interface detail- connectionRequired may return yes, even when the host is unreachable.  We cover that up here...
            connectionRequired= NO;  
            break;
        }
            
        case ReachableViaWWAN:
        {
            statusString = @"Reachable WWAN";
            imageView.image = [UIImage imageNamed: @"WWAN5.png"];
            break;
        }
        case ReachableViaWiFi:
        {
            statusString= @"Reachable WiFi";
            imageView.image = [UIImage imageNamed: @"Airport.png"];
            break;
        }
    }
    if(connectionRequired)
    {
        statusString= [NSString stringWithFormat: @"%@, Connection Required", statusString];
    }
    textField.text= statusString;
}

- (void) updateInterfaceWithReachability: (Reachability*) curReach
{
    NetworkStatus netStatus = [curReach currentReachabilityStatus];
    if(curReach == hostReach)
	{   
        //NSLog(@"RH  %d",netStatus);
        hostReachable = netStatus;
       
    }
	if(curReach == internetReach){	//NSLog(@"RI  %d",netStatus);
        internetReachable = netStatus;
        //[self configureTextField: internetConnectionStatusField imageView: internetConnectionIcon reachability: curReach];
	}
	if(curReach == wifiReach){	//NSLog(@"RW  %d",netStatus);
        
        wifiReachable = netStatus;
		//[self configureTextField: localWiFiConnectionStatusField imageView: localWiFiConnectionIcon reachability: curReach];
	}
	
}

//Called by Reachability whenever status changes.
- (void) reachabilityChanged: (NSNotification* )note
{
	Reachability* curReach = [note object];
	NSParameterAssert([curReach isKindOfClass: [Reachability class]]);
	[self updateInterfaceWithReachability: curReach];
}

- (void) resetHostReachability:(NSString*) host{
    
    [hostReach stopNotifier];
    hostReach = [Reachability reachabilityWithHostName: host];
    [hostReach startNotifier];
    [self updateInterfaceWithReachability: hostReach];
}

- (BOOL)isReachable{
    BOOL reachable = YES;
    if (internetReachable == NotReachable && wifiReachable == NotReachable) {
        reachable = NO;
    }
    return reachable;
}

- (BOOL)isReachableViaWWAN{
    return (internetReachable == ReachableViaWWAN);
}

- (BOOL)isInternetReachable{
    return (internetReachable != NotReachable);
}
- (BOOL)isWifiReachable{
    return (wifiReachable != NotReachable);
}
- (BOOL)isHostReachable{
    return (hostReachable != NotReachable);
}

@end
