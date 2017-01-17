//
//  CVAPIRequest.h
//  TingCheBao
//
//  Created by apple on 14-7-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MBProgressHUD.h"

@interface CVAPIRequest : NSMutableURLRequest

@property(nonatomic, assign) BOOL isReady;
@property(nonatomic, retain) MBProgressHUD* hud;

@property(nonatomic, readonly, assign) BOOL cancelled;

- (id)initWithCommand:(NSString *)command andApiPath:(NSString*)apiPath;
- (void)setParamString:(NSString*)paramString;

- (id)initWithAPIPath:(NSString *)apiPath;
- (id)initWithAPIPath:(NSString *)apiPath timeout:(double)timeout;
- (id)initWithAPIPath:(NSString *)apiPath mserver:(BOOL)mserver;
- (id)initWithAPIPath:(NSString *)apiPath downLoad:(BOOL)downLoad;
- (void)setHTTPMethod:(NSString *)method;
- (void)setPOSTParamString:(NSString *)paramString isJsonFormat:(BOOL)isJson;
- (void)setPUTParamString:(NSString *)paramString isJsonFormat:(BOOL)isJson;
- (void)setUploadFileParamString:(NSData*)data mimeType:(NSString*)mimeType fileName:(NSString*)fileName;
- (void)setUploadFileParamString:(NSData*)data data2:(NSData*)data2 mimeType:(NSString*)mimeType;

// for GET api, will encode some characters that stringByAddingPercentEscapes does not handle
+ (NSString*)GETParamString:(NSDictionary *)parameters;
- (id)initWithAPIUrlString:(NSString *)apiPath;

// cancel

- (void)cancel;

@end
