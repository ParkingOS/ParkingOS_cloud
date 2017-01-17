//
//  CVAPIRequestModel.m
//  TingCheBao
//
//  Created by apple on 14-7-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "CVAPIRequestModel.h"
#import "ReachabilityManager.h"
#import "TAPIUtility.h"
#import "NetworkView.h"


@interface CVAPIRequestModel ()

@property(nonatomic, retain) NSMutableSet* requests;

@end

@implementation CVAPIRequestModel

static NSInteger _requestCount = 0;

+ (void)increaseRequestCount {
    _requestCount++;
    [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
}

+ (void)decreaseRequestCount {
    _requestCount--;
    if (_requestCount <= 0)
        _requestCount = 0;
    if (_requestCount == 0)
        [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
}

- (id)init {
    if (self = [super init]) {
        _requests = [NSMutableSet set];
        _delegate = nil;
    }
    return self;
}

- (void)dealloc {
    [self cancel];
}

- (void)sendRequest:(CVAPIRequest*)request completion:(void (^)(NSDictionary* returnedData, NSError *error))handler {
    //check network connection
    if (![[ReachabilityManager sharedManager] isReachable]) {
        dispatch_async(dispatch_get_main_queue(), ^{
                        [request.hud removeFromSuperview];
                    });
        if (!_hideNetworkView) {
            [NetworkView showWithStatus:@"无法连接到网络，请稍后尝试!"];
        }

        if ([_delegate respondsToSelector:@selector(modelDidFailWithError:model:request:)]) {
            NSString *bundleIdentifier = [[NSBundle mainBundle] bundleIdentifier];
            NSDictionary *userInfo = @{NSHelpAnchorErrorKey: @"The network connection is poor.",
                                       NSURLErrorFailingURLStringErrorKey: [request.URL absoluteString]};
            
            NSError *error = [NSError errorWithDomain:bundleIdentifier code:2008 userInfo:userInfo];
            dispatch_async(dispatch_get_main_queue(), ^{
                [_delegate modelDidFailWithError:error model:self request:request];
            });
        }
        
        return;
    }
    
    [_requests addObject:request];
    [[self class] increaseRequestCount];
    
    NSOperationQueue *queue = [[NSOperationQueue alloc] init];
    
    [NSURLConnection sendAsynchronousRequest:request queue:queue completionHandler:^(NSURLResponse *response, NSData *data, NSError *error) {
        // update request count in main thread
        dispatch_async(dispatch_get_main_queue(), ^{
            [[self class] decreaseRequestCount];
            [_requests removeObject:request];
            
            //清除hud
            [request.hud removeFromSuperview];
        });

        // don't call completion handler if request got cancled
        if (!request.cancelled) {
//            NSLog(@"request--%@", request.URL.absoluteString);
            NSDictionary *returnedData = nil;
            //返回的数据是  GBK，需要先转化成 UTF8
            NSStringEncoding gbkEncoding =CFStringConvertEncodingToNSStringEncoding(kCFStringEncodingGB_18030_2000);
            NSString* temp = [[NSString alloc] initWithData:data encoding:gbkEncoding];
            
            //
            NSRange range = [temp rangeOfString:@"\n"];
            if (range.length > 0)
                temp = [temp stringByReplacingOccurrencesOfString:@"\n" withString:@"\\n"];
            
            data = [temp dataUsingEncoding:NSUTF8StringEncoding];
            
            if (error == nil && [data length] > 0) {
                returnedData = [NSJSONSerialization JSONObjectWithData:data options: NSJSONReadingMutableContainers error:nil];
                if (!returnedData) {
                    returnedData = @{@"info" : temp};
                }
                
                if (NULL != handler) {
                    // call completion handler in main thread
                    dispatch_async(dispatch_get_main_queue(), ^{
                        handler(returnedData, error);
                        NSLog(@"%@", returnedData);
                    });
                }
                if (_delegate && [_delegate respondsToSelector:@selector(modelDidFinishLoad:action:)]) {
                    [_delegate modelDidFinishLoad:self action:@""];
                }
            } else {
                if ([TAPIUtility isRequestTimeOut:error]) {
                    NSLog(@"加载超时--%@", request.URL.absoluteString);
                    if (!_hideNetworkView) {
                        [NetworkView showWithStatus:@"网络加载超时!"];
                    }
                }
                else {
                    NSLog(@"请求失败");
                    if (!_hideNetworkView) {
                        [NetworkView showWithStatus:@"网络请求失败!"];
                    }
                }
                if (_delegate && [_delegate respondsToSelector:@selector(modelDidFailWithError:model:request:)])
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [_delegate modelDidFailWithError:error model:self request:request];
                    });
            }
        } else {
            NSLog(@"cancel---%@", request.URL.absoluteString);
        }
    }];
}

- (void)cancel {
    _delegate = nil;
}

@end
