//
//  CVAPIRequest.m
//  TingCheBao
//
//  Created by apple on 14-7-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "CVAPIRequest.h"
#import "NSString+CVURLEncoding.h"
#import "TAPIUtility.h"

#define REQUEST_TIMEOUT 10.0 //for development set it to 30.0, for product should revert it to 5.0
static NSString* kStringBoundary = @"3i2ndDfv2rTHiSisAbouNdArYfORhtTPEefj3q2f";

@implementation CVAPIRequest

- (id)initWithAPIPath:(NSString *)apiPath{
    return [self initWithAPIPath:apiPath mserver:NO downLoad:NO timeout:0];
}

- (id)initWithAPIPath:(NSString *)apiPath timeout:(double)timeout{
    return [self initWithAPIPath:apiPath mserver:NO downLoad:NO timeout:timeout];
}

- (id)initWithAPIPath:(NSString *)apiPath mserver:(BOOL)mserver{
    return [self initWithAPIPath:apiPath mserver:mserver downLoad:NO timeout:0];
}

- (id)initWithAPIPath:(NSString *)apiPath downLoad:(BOOL)downLoad{
    return [self initWithAPIPath:apiPath mserver:NO downLoad:downLoad timeout:0];
}

- (id)initWithAPIPath:(NSString *)apiPath mserver:(BOOL)mserver downLoad:(BOOL)downLoad timeout:(double)timeout{
    NSURL* apiURL = nil;
    if ([apiPath length] != 0) {
        apiPath = [[TAPIUtility getNetworkWithMserver:mserver downLoad:downLoad] stringByAppendingString:apiPath];
//        if ([apiPath rangeOfString:@"getpark.do?action=get2kpark"].length > 0) {
//            apiPath = @"http://www.google.com";
//            NSLog(@"google");
//        }
        NSLog(@"%@",apiPath);
        apiURL = [NSURL URLWithString:apiPath];
    }
    
    self = [super initWithURL:apiURL
                  cachePolicy:NSURLRequestReloadIgnoringCacheData
              timeoutInterval:timeout == 0 ? REQUEST_TIMEOUT : timeout];
    if (self) {
        [self setHTTPShouldHandleCookies:NO];
    }
    return self;
}

- (void)setParamString:(NSString *)paramString {
    [self addValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [self setHTTPMethod:@"POST"];
    [self setHTTPBody:[paramString dataUsingEncoding:NSUTF8StringEncoding]];
}

+ (NSString*)GETParamString:(NSDictionary *)parameters{
    
    NSMutableString* urlString =[NSMutableString string];
  
    NSArray* names = [parameters allKeys];
    
    for(NSString * name in names) {
        NSString* value = [parameters objectForKey:name];
        if([value isKindOfClass:[NSArray class]] || [value isKindOfClass:[NSMutableArray class]]) {
            for(NSString * item in (NSArray*)value) {
                [urlString appendFormat:@"&%@[]=%@", [name stringByUrlEncoding], [[item stringByUrlEncoding] stringByUrlEncoding]];
            }
        }
        else if([value isKindOfClass:[NSDictionary class]] || [value isKindOfClass:[NSMutableDictionary class]]) {
            NSArray* items = [(NSDictionary*)value allKeys];
            for(NSString * itemKey in items) {
                NSString* item = [(NSDictionary*)value objectForKey:itemKey];
                [urlString appendFormat:@"&%@.%@=%@", [name stringByUrlEncoding], [itemKey stringByUrlEncoding], [[item stringByUrlEncoding] stringByUrlEncoding]];
            }
        }
        else {
            [urlString appendFormat:@"&%@=%@", [name stringByUrlEncoding], [[value stringByUrlEncoding] stringByUrlEncoding]];
        }
    }
    [urlString replaceCharactersInRange:NSMakeRange(0,1) withString:@"?"];
    return urlString;
}

- (void)setPOSTParamString:(NSString *)paramString isJsonFormat:(BOOL)isJson {
    [self setHTTPMethod:@"POST"];
    if(isJson)
        [self addValue:@"application/json" forHTTPHeaderField:@"Content-type"];
    else
        [self addValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-type"];
    
    [self setHTTPBody:[paramString dataUsingEncoding:NSUTF8StringEncoding]];
}

- (void)setPUTParamString:(NSString *)paramString isJsonFormat:(BOOL)isJson{
    [self setHTTPMethod:@"PUT"];
    if(isJson)
        [self addValue:@"application/json" forHTTPHeaderField:@"Content-type"];
    else
        [self addValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-type"];
    [self setHTTPBody:[paramString dataUsingEncoding:NSUTF8StringEncoding]];
}

- (void)setHTTPMethod:(NSString *)method {
    [super setHTTPMethod:method];
}

- (void)setParam:(NSString *)value forKey:(NSString *)key {
    
}

- (void)setUploadFileParamString:(NSData*)data mimeType:(NSString*)mimeType fileName:(NSString*)fileName {
    [self setHTTPMethod:@"POST"];
    [self setValue:[NSString stringWithFormat:@"multipart/form-data; boundary=%@", kStringBoundary] forHTTPHeaderField:@"content-Type"];
    NSMutableData* body = [NSMutableData data];
    NSString* beginLine = [NSString stringWithFormat:@"--%@\r\n", kStringBoundary];
	NSString *endLine = @"\r\n";
    
    [body appendData:[beginLine dataUsingEncoding:NSUTF8StringEncoding]];
    [body appendData:[[NSString stringWithFormat:
                       @"Content-Disposition: form-data; name=\"%@\"; filename=\"%@\"\r\n",
                       fileName, fileName]
                      dataUsingEncoding:NSUTF8StringEncoding]];
    [body appendData:[[NSString stringWithFormat:@"Content-Length: %d\r\n", data.length]
                      dataUsingEncoding:NSUTF8StringEncoding]];
    [body appendData:[[NSString stringWithFormat:@"Content-Type: %@\r\n\r\n", mimeType]
                      dataUsingEncoding:NSUTF8StringEncoding]];
    [body appendData:data];
    [body appendData:[endLine dataUsingEncoding:NSUTF8StringEncoding]];
 
    [body appendData:[[NSString stringWithFormat:@"--%@--\r\n", kStringBoundary]
                      dataUsingEncoding:NSUTF8StringEncoding]];
    [self setHTTPBody:body];
}

- (void)setUploadFileParamString:(NSData*)data data2:(NSData*)data2 mimeType:(NSString*)mimeType {
    NSString* fileName = @"1.jpg";
    NSString* fileName2 = @"2.jpg";
    
    [self setHTTPMethod:@"POST"];
    [self setValue:[NSString stringWithFormat:@"multipart/form-data; boundary=%@", kStringBoundary] forHTTPHeaderField:@"content-Type"];
    NSMutableData* body = [NSMutableData data];
    NSString* beginLine = [NSString stringWithFormat:@"--%@\r\n", kStringBoundary];
    NSString *endLine = @"\r\n";
    
    //第一个
    [body appendData:[beginLine dataUsingEncoding:NSUTF8StringEncoding]];
    [body appendData:[[NSString stringWithFormat:
                       @"Content-Disposition: form-data; name=\"%@\"; filename=\"%@\"\r\n",
                       fileName, fileName]
                      dataUsingEncoding:NSUTF8StringEncoding]];
    [body appendData:[[NSString stringWithFormat:@"Content-Length: %d\r\n", data.length]
                      dataUsingEncoding:NSUTF8StringEncoding]];
    [body appendData:[[NSString stringWithFormat:@"Content-Type: %@\r\n\r\n", mimeType]
                      dataUsingEncoding:NSUTF8StringEncoding]];
    [body appendData:data];
    [body appendData:[endLine dataUsingEncoding:NSUTF8StringEncoding]];
    
    //第二个
    
    [body appendData:[beginLine dataUsingEncoding:NSUTF8StringEncoding]];
    
    [body appendData:[[NSString stringWithFormat:
                       @"Content-Disposition: form-data; name=\"%@\"; filename=\"%@\"\r\n",
                       fileName2, fileName2]
                      dataUsingEncoding:NSUTF8StringEncoding]];
    [body appendData:[[NSString stringWithFormat:@"Content-Length: %d\r\n", data2.length]
                      dataUsingEncoding:NSUTF8StringEncoding]];
    [body appendData:[[NSString stringWithFormat:@"Content-Type: %@\r\n\r\n", mimeType]
                      dataUsingEncoding:NSUTF8StringEncoding]];
    [body appendData:data2];
    [body appendData:[endLine dataUsingEncoding:NSUTF8StringEncoding]];
    
    
    //结尾
    [body appendData:[[NSString stringWithFormat:@"--%@--\r\n", kStringBoundary]
                      dataUsingEncoding:NSUTF8StringEncoding]];
    
    [self setHTTPBody:body];
}

- (void)cancel {
    _cancelled = YES;
}

@end
