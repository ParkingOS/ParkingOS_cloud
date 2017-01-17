//
//  CVAPIRequestModel.h
//  TingCheBao
//
//  Created by apple on 14-7-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CVAPIRequest.h"
#import "NSObject+CVJSON.h"

@class CVAPIRequestModel;

@protocol CVAPIModelDelegate <NSObject>

- (void)modelDidFinishLoad:(CVAPIRequestModel*)model action:(NSString*)action;

@optional

- (void)modelWillLoad:(CVAPIRequestModel *)model action:(NSString *)action;
- (void)modelDidSucceedWithResult:(NSDictionary*)result  model:(CVAPIRequestModel*)model action:(NSString*)action;
- (void)modelDidFailWithError:(NSError*)error model:(CVAPIRequestModel*)model request:(CVAPIRequest*)request;

@end


@interface CVAPIRequestModel : NSObject

@property(nonatomic, assign) BOOL hideNetworkView;

- (void)sendRequest:(CVAPIRequest*)request completion:(void (^)(NSDictionary*, NSError*))handler;
- (void)cancel;

/*  Methods for invoking the callbacks of View Controllers */

@property(nonatomic, unsafe_unretained) id<CVAPIModelDelegate> delegate;

@end
