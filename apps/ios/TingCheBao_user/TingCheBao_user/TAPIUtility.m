//
//  TAPIUtility.m
//  TingCheBao
//
//  Created by apple on 14-7-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TAPIUtility.h"
#import "CVAPIRequestModel.h"
#include <sys/xattr.h>
#import "TSession.h"
#import <AVFoundation/AVFoundation.h>

#define user_token @"user_token"
#define user_name  @"user_name"
#define user_password  @"user_password"


#define main_url @"s.tingchebao.com"//正式地址
//#define main_url @"192.168.199.251"//测试地址 海祥239 yao 240 ronghui 251



#define TIMESTAMP_ERROR_VALUE 0
#define NUMBER_ERROR_VALUE [NSNumber numberWithInteger:0]

@implementation TAPIUtility

/**
 * @param mserver 取消息的时候为YES
 * @param download 下载图片时为YES
 */
+ (NSString*)getNetworkWithMserver:(BOOL)mserver downLoad:(BOOL)downLoad{
    NSMutableString* url = [[NSMutableString alloc] initWithString:@"http://"];
    if (downLoad) {
        [url appendString:@"d.tingchebao.com/"];
    } else {
        [url appendString:GL(save_server_url)];
        [url appendString:(mserver ? @"/mserver/" : @"/zld/")];
    }
    
    return url;
}

//baseUrl + url
+ (NSString*)getNetworkWithUrl:(NSString*)url {
    NSString* host = [TAPIUtility getNetworkWithMserver:NO downLoad:NO];
    return [NSString stringWithFormat:@"%@%@", host, url];
}

//+(void)alertMessage:(NSString*)message {
//    MBProgressHUD* hud = [MBProgressHUD showHUDAddedTo:[UIApplication sharedApplication].keyWindow animated:YES];
//    hud.mode = MBProgressHUDModeText;
//    hud.labelText = message;
//    hud.removeFromSuperViewOnHide = YES;
//    hud.color = [UIColor whiteColor];
//    hud.labelColor = [UIColor blackColor];
//    [hud hide:YES afterDelay:1];
//}
//
//+(void)alertSeriousMessage:(NSString *)message {
//    UIImageView* imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"alert.png"]];
//    imgView.frame = CGRectMake(0, 0, 40, 40);
//    MBProgressHUD* hud = [MBProgressHUD showHUDAddedTo:[UIApplication sharedApplication].keyWindow animated:YES];
//    hud.mode = MBProgressHUDModeCustomView;
//    hud.labelText = message;
//    hud.removeFromSuperViewOnHide = YES;
//    hud.customView = imgView;
//    [hud hide:YES afterDelay:1];
//}

+ (void)alertMessage:(NSString *)message {
//    UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"" message:message delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil];
//    [alert show];
    MBProgressHUD* hud = [MBProgressHUD showHUDAddedTo:[UIApplication sharedApplication].keyWindow animated:YES];
    hud.mode = MBProgressHUDModeText;
    hud.labelText = message;
    hud.removeFromSuperViewOnHide = YES;
    [hud hide:YES afterDelay:1];
}

+ (void)alertMessage:(NSString *)message afterDelay:(NSTimeInterval)delay {
    MBProgressHUD* hud = [MBProgressHUD showHUDAddedTo:[UIApplication sharedApplication].keyWindow animated:YES];
    hud.mode = MBProgressHUDModeText;
    hud.labelText = message;
    hud.removeFromSuperViewOnHide = YES;
    [hud hide:YES afterDelay:delay];
}

+ (void)alertMessage:(NSString *)message success:(BOOL)success toViewController:(UIViewController*)viewcontroller {
    MBProgressHUD* hud = [MBProgressHUD showHUDAddedTo:viewcontroller ? viewcontroller.view :[UIApplication sharedApplication].keyWindow animated:YES];
    hud.mode = MBProgressHUDModeCustomView;
    hud.delegate = viewcontroller;
    UIImageView* customView = [[UIImageView alloc] initWithImage:[UIImage imageNamed: success ? @"img_success.png" : @"img_fail.png"]];
    customView.frame = CGRectMake(0, 0, 27, success ? 30 : 27);
    hud.customView = customView;
    hud.labelText = message;
    [hud hide:YES afterDelay:1.0];
}

+ (void)alertMessage:(NSString *)message toViewController:(UIViewController*)viewcontroller {
    MBProgressHUD* hud = [[MBProgressHUD alloc] initWithView:viewcontroller.view];
    [viewcontroller.view addSubview:hud];
    hud.labelText = message;
    hud.delegate = viewcontroller;
    hud.removeFromSuperViewOnHide = YES;
    hud.mode = MBProgressHUDModeText;
    [hud show:YES];
    [hud hide:YES afterDelay:1.0];
}

+ (MBProgressHUD*)loaddingView:(UIView *)view {
    MBProgressHUD* hud = [MBProgressHUD showHUDAddedTo:view animated:YES];
//    _hud.labelText = @"加载...";
    return hud;
}

+(BOOL)isRequestTimeOut:(NSError*)error {
    if([error.domain isEqualToString:@"NSURLErrorDomain"] && error.code == -1001 )
        return YES;
    return NO;
}

+ (NSString*)colorWithFree:(double)free total:(double)total {
    if (free < 10 || free / total < 0.1) {
        return @"red";
    } else {
        return @"green";
    }
}

+(NSString*) getValidString:(NSString *)data {
    if (data == nil || [data isKindOfClass:[NSNull class]] || ![data isKindOfClass:[NSString class]])
        return @"";
    return data;
}

+(NSNumber*) getValidNumber:(NSObject *)data {
    if (data == nil || [data isKindOfClass:[NSNull class]])
        return NUMBER_ERROR_VALUE;
    if ([data isKindOfClass:[NSNumber class]])
        return (NSNumber*)data;
    if ([data isKindOfClass:[NSString class]])
        return [NSNumber numberWithInteger:[((NSString*)data) integerValue]];
    return NUMBER_ERROR_VALUE;
}

+(NSString*) getValidNumberString:(NSObject *)data {
    return [[TAPIUtility getValidNumber:data] stringValue];
}

+(double) getValidTimestamp:(NSObject*)timestamp {
    NSNumber* data = [self getValidNumber:timestamp];
    if ([data doubleValue] <= 0 || [data doubleValue]< 1000000000 || [data doubleValue] > 10000000000)
        return TIMESTAMP_ERROR_VALUE;
    return [data doubleValue];
}

+(NSArray *)getValidArray:(NSArray *)data {
    if (nil == data || ![data isKindOfClass:[NSArray class]]) {
        return @[];
    }
    
    return data;
}

//---------------------writeWithYang-------------------
+ (void)saveFirstInfo:(NSString *)info
{
    [[NSUserDefaults standardUserDefaults] setObject:info forKey:save_first_comeIn];
}
+ (BOOL)getFirstInfo
{
    NSString *string = [[NSUserDefaults standardUserDefaults] objectForKey:save_first_comeIn];
    if (string) {
        return NO;
    }
    return YES;
}

+ (void)saveCoordinate:(CLLocationCoordinate2D)coordinate
{
    NSArray *array = [[NSArray alloc] initWithObjects:[NSString stringWithFormat:@"%lf",coordinate.latitude],[NSString stringWithFormat:@"%lf",coordinate.longitude], nil];
    [[NSUserDefaults standardUserDefaults] setObject:array forKey:save_Coor];
}
+ (CLLocationCoordinate2D)getCoordinate
{
    NSArray *array = [[NSUserDefaults standardUserDefaults] objectForKey:save_Coor];
    CLLocationCoordinate2D coor;
    if ([array count]) {
        coor.latitude = [[array objectAtIndex:0] doubleValue];
        coor.longitude = [[array objectAtIndex:1] doubleValue];
    }else{
        coor.latitude = 0;
        coor.longitude = 0;
    }
    return coor;
}

+ (void)saveDistance:(NSString *)dis
{
    [[NSUserDefaults standardUserDefaults] setObject:dis forKey:save_disatuce];
}
+ (NSString *)getDistance
{
    return [[NSUserDefaults standardUserDefaults] objectForKey:save_disatuce];
}
//---------------------writeWithYang-------------------
+ (void)saveParks:(NSArray*)items {
    [NSKeyedArchiver archiveRootObject:items toFile:[TAPIUtility getParksPath]];
}

+ (NSArray*)getParks {
    return [NSKeyedUnarchiver unarchiveObjectWithFile:[TAPIUtility getParksPath]];
}

+ (NSString*)getParksPath {
    NSString* path = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject];
    path = [path stringByAppendingPathComponent:@"parkItems"];
    return path;
}

+ (NSString*)getHistoryPath {
    NSString* path = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject];
    path = [path stringByAppendingPathComponent:@"historyItems"];
    return path;
}

+ (BOOL)isValidOfCarNumber:(NSString*)number {
    if (number == nil) {
        return NO;
    }
    NSString *patternStr = [NSString stringWithFormat:@"^[\\u4e00-\\u9fa5]{1}[a-zA-Z_0-9]{5}[a-zA-Z_0-9_\\u4e00-\\u9fa5]$|^(WJ|wj)\\d{7}$"];
    NSRegularExpression *regularexpression = [[NSRegularExpression alloc]
                                              initWithPattern:patternStr
                                              options:NSRegularExpressionCaseInsensitive
                                              error:nil];
    NSUInteger numberofMatch = [regularexpression numberOfMatchesInString:number
                                                                  options:NSMatchingReportProgress
                                                                    range:NSMakeRange(0, number.length)];
    if(numberofMatch > 0)
    {
        return YES;
    }
    return NO;
}

+ (BOOL)isValidOfMoneyNumber:(NSString*)number {
    if (number == nil) {
        return NO;
    }
//    NSString *patternStr = [NSString stringWithFormat:@"^(\\d+)||(\\d+\\.\\d{1,2})$"];
    NSString *patternStr = [NSString stringWithFormat:@"^(([1-9]\\d*)(\\.\\d{1,2})?)$|^(0\\.0?([1-9]\\d?))$"];
    NSRegularExpression *regularexpression = [[NSRegularExpression alloc]
                                              initWithPattern:patternStr
                                              options:NSRegularExpressionCaseInsensitive
                                              error:nil];
    NSUInteger numberofMatch = [regularexpression numberOfMatchesInString:number
                                                                  options:NSMatchingReportProgress
                                                                    range:NSMakeRange(0, number.length)];
    if(numberofMatch > 0)
    {
        return YES;
    }
    return NO;
    
}

+ (BOOL)isValidOfPhone:(NSString*)number {
    if (number == nil) {
        return NO;
    }
//    NSString *patternStr = [NSString stringWithFormat:@"^(\\d+)||(\\d+\\.\\d{1,2})$"];
    NSString *patternStr = [NSString stringWithFormat:@"^[1][3,4,5,7,8][0-9]{9}$"];
    NSRegularExpression *regularexpression = [[NSRegularExpression alloc]
                                              initWithPattern:patternStr
                                              options:NSRegularExpressionCaseInsensitive
                                              error:nil];
    NSUInteger numberofMatch = [regularexpression numberOfMatchesInString:number
                                                                  options:NSMatchingReportProgress
                                                                    range:NSMakeRange(0, number.length)];
    if(numberofMatch > 0)
    {
        return YES;
    }
    return NO;

}

+ (UIImage *)imageWithColor:(UIColor *)color
{
    CGSize size = CGSizeMake(1, 1);
    CGRect rect = CGRectMake(0.0f, 0.0f, size.width, size.height);
    UIGraphicsBeginImageContext(rect.size);
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSetFillColorWithColor(context, [color CGColor]);
    CGContextFillRect(context, rect);
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return image;
}

+ (UIImage*)ajustImage:(UIImage*)image size:(CGSize)size {
    UIGraphicsBeginImageContextWithOptions(size, NO, UIScreen.mainScreen.scale);
    // draw scaled image into thumbnail context
    [image drawInRect:CGRectMake(0, 0, size.width, size.height)];
    UIImage *newThumbnail = UIGraphicsGetImageFromCurrentImageContext();
    // pop the context
    UIGraphicsEndImageContext();
    if(newThumbnail == nil)
        NSLog(@"could not scale image");
    return newThumbnail;
}

+(UIImage *) imageCompressForSize:(UIImage *)sourceImage targetSize:(CGSize)size{
    UIImage *newImage = nil;
    CGSize imageSize = sourceImage.size;
    CGFloat width = imageSize.width;
    CGFloat height = imageSize.height;
    CGFloat targetWidth = size.width;
    CGFloat targetHeight = size.height;
    CGFloat scaleFactor = 0.0;
    CGFloat scaledWidth = targetWidth;
    CGFloat scaledHeight = targetHeight;
    CGPoint thumbnailPoint = CGPointMake(0.0, 0.0);
    if(CGSizeEqualToSize(imageSize, size) == NO){
        CGFloat widthFactor = targetWidth / width;
        CGFloat heightFactor = targetHeight / height;
        if(widthFactor > heightFactor){
            scaleFactor = widthFactor;
        }
        else{
            scaleFactor = heightFactor;
        }
        scaledWidth = width * scaleFactor;
        scaledHeight = height * scaleFactor;
        if(widthFactor > heightFactor){
            thumbnailPoint.y = (targetHeight - scaledHeight) * 0.5;
        }else if(widthFactor < heightFactor){
            thumbnailPoint.x = (targetWidth - scaledWidth) * 0.5;
        }
    }
    
    UIGraphicsBeginImageContext(size);
    
    CGRect thumbnailRect = CGRectZero;
    thumbnailRect.origin = thumbnailPoint;
    thumbnailRect.size.width = scaledWidth;
    thumbnailRect.size.height = scaledHeight;
    [sourceImage drawInRect:thumbnailRect];
    newImage = UIGraphicsGetImageFromCurrentImageContext();
    
    if(newImage == nil){
        NSLog(@"scale image fail");
    }
    
    UIGraphicsEndImageContext();
    
    return newImage;
    
}

+ (UIImage *) imageCompressForWidth:(UIImage *)sourceImage targetWidth:(CGFloat)defineWidth{
    UIImage *newImage = nil;
    CGSize imageSize = sourceImage.size;
    CGFloat width = imageSize.width;
    CGFloat height = imageSize.height;
    CGFloat targetWidth = defineWidth;
    CGFloat targetHeight = height / (width / targetWidth);
    CGSize size = CGSizeMake(targetWidth, targetHeight);
    CGFloat scaleFactor = 0.0;
    CGFloat scaledWidth = targetWidth;
    CGFloat scaledHeight = targetHeight;
    CGPoint thumbnailPoint = CGPointMake(0.0, 0.0);
    if(CGSizeEqualToSize(imageSize, size) == NO){
        CGFloat widthFactor = targetWidth / width;
        CGFloat heightFactor = targetHeight / height;
        if(widthFactor > heightFactor){
            scaleFactor = widthFactor;
        }
        else{
            scaleFactor = heightFactor;
        }
        scaledWidth = width * scaleFactor;
        scaledHeight = height * scaleFactor;
        if(widthFactor > heightFactor){
            thumbnailPoint.y = (targetHeight - scaledHeight) * 0.5;
        }else if(widthFactor < heightFactor){
            thumbnailPoint.x = (targetWidth - scaledWidth) * 0.5;
        }
    }
    UIGraphicsBeginImageContext(size);
    CGRect thumbnailRect = CGRectZero;
    thumbnailRect.origin = thumbnailPoint;
    thumbnailRect.size.width = scaledWidth;
    thumbnailRect.size.height = scaledHeight;
    
    [sourceImage drawInRect:thumbnailRect];
    
    newImage = UIGraphicsGetImageFromCurrentImageContext();
    if(newImage == nil){
        NSLog(@"scale image fail");
    }
    
    UIGraphicsEndImageContext();
    return newImage;
}

+ (CGSize) sizeWithFont:(UIFont*) font size:(CGSize)size text:(NSString*)text {
    NSDictionary *attributes = @{NSFontAttributeName: font};
    if (getOS >= 7.0) {
        CGRect rect = [text boundingRectWithSize:size
                                     options:NSStringDrawingUsesLineFragmentOrigin
                                  attributes:attributes
                                     context:nil];
        return rect.size;
    } else {
        CGSize size = [text sizeWithFont:font constrainedToSize:size lineBreakMode:NSLineBreakByTruncatingTail];
        return size;
    }
}

+ (NSString*)getDuration:(int) durationSeconds {
    int hours = durationSeconds /(60*60);
    int leftSeconds = durationSeconds % (60*60);
    int minutes = leftSeconds / 60;
    int seconds = leftSeconds % 60;
    
    NSString* sBuffer = @"";
    NSString* hour = [self addZeroPrefix:hours];
    if (![hour isEqualToString:@"00"]) {
        sBuffer = [sBuffer stringByAppendingString:hour];
        sBuffer = [sBuffer stringByAppendingString:@"小时 "];
    }
    
    NSString* minute = [self addZeroPrefix:minutes];
    if (!([hour isEqualToString:@"00"] && [minute isEqualToString:@"00"])) {
        sBuffer = [sBuffer stringByAppendingString:minute];
        sBuffer = [sBuffer stringByAppendingString:@"分 "];
    }
    sBuffer = [sBuffer stringByAppendingString:[self addZeroPrefix:seconds]];
    sBuffer = [sBuffer stringByAppendingString:@"秒"];
    
    return sBuffer;
}

+ (NSString*) addZeroPrefix:(int)number{
    if(number < 10){
        return [NSString stringWithFormat:@"0%d", number];
    }else{
        return [NSString stringWithFormat:@"%d", number];
    }
}

+ (void)clearUserInfo {
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    [defaults removeObjectForKey:save_phone];
    [defaults removeObjectForKey:save_msg_id];
    [defaults setObject:@"10" forKey:save_low_recharge];
    [defaults setObject:@"0" forKey:save_account_money];
    [defaults removeObjectForKey:save_holiday_see_time];
    [defaults removeObjectForKey:save_Coor];
    [defaults removeObjectForKey:save_disatuce];
    
    [[TSession shared] removeAll];
}

+ (BOOL)isEnterpriseVersion {
    NSString* bundleID = [[[NSBundle mainBundle] infoDictionary] objectForKey:(NSString*)kCFBundleIdentifierKey];
    if ([bundleID isEqualToString:@"com.tingCheBao.enterprise"]) {
        return YES;
    } else {
        return NO;
    }
}

+ (void)sendDeviceToken {
    NSString* token = [[NSUserDefaults standardUserDefaults] objectForKey:save_device_token];
    if (!token || ![[NSUserDefaults standardUserDefaults] objectForKey:save_phone])
        return;
    
    if ([TAPIUtility isEnterpriseVersion]) {
        token = [@"E_" stringByAppendingString:token];
    }
    NSString* apiPath = [NSString stringWithFormat:@"carlogin.do?action=addcid&cid=%@&mobile=%@", token, [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    CVAPIRequest* request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    CVAPIRequestModel* model = [[CVAPIRequestModel alloc] init];
    model.hideNetworkView = YES;
    [model sendRequest:request completion:^(NSDictionary *result, NSError *error) {
    }];
}

+ (void)checkNotificationSetting {
    //是否打开通知提醒，只提醒 一次
    if ([[NSUserDefaults standardUserDefaults] objectForKey:save_phone] && ![[NSUserDefaults standardUserDefaults] boolForKey:save_open_notification] ) {
        UIAlertView* alert = [[UIAlertView alloc] initWithTitle:nil message:@"建议您在\"设置\"->\"通知中心\"中打开通知提醒，否则会影响手机支付功能哦～" delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil];
        if (getOS < 8.0 && ([[UIApplication sharedApplication] enabledRemoteNotificationTypes] == UIRemoteNotificationTypeNone)) {
            [alert show];
            [[NSUserDefaults standardUserDefaults] setBool:YES forKey:save_open_notification];
        } else if (getOS >= 8.0 && [[UIApplication sharedApplication] currentUserNotificationSettings].types == UIUserNotificationTypeNone) {
            [alert show];
            [[NSUserDefaults standardUserDefaults] setBool:YES forKey:save_open_notification];
        }
    }
}

+ (BOOL)isUpdateVersionWithOld:(NSString*)oldVersion new:(NSString*)newVersion {
    NSString *patternStr = [NSString stringWithFormat:@"^(\\d*)\\.(\\d*)\\.(\\d*)$"];
    NSRegularExpression *regularexpression = [[NSRegularExpression alloc]
                                              initWithPattern:patternStr
                                              options:NSRegularExpressionCaseInsensitive
                                              error:nil];
    NSArray* oldMatches = [regularexpression matchesInString:oldVersion options:NSMatchingReportProgress range:NSMakeRange(0, [oldVersion length])];
    NSArray* newMatches = [regularexpression matchesInString:newVersion options:NSMatchingReportProgress range:NSMakeRange(0, [newVersion length])];
    NSMutableArray* oldArray = [NSMutableArray array];
    NSMutableArray* newArray = [NSMutableArray array];
    
    if ([oldMatches count] && [newMatches count]) {
        for (NSTextCheckingResult *match in oldMatches) {
            NSInteger count = [match numberOfRanges];//匹配项
            for(NSInteger index = 1; index<count; index++){
                NSRange halfRange = [match rangeAtIndex:index];
                [oldArray addObject:[oldVersion substringWithRange:halfRange]];
            }
        }
        for (NSTextCheckingResult *match in newMatches) {
            NSInteger count = [match numberOfRanges];//匹配项
            for(NSInteger index = 1; index<count; index++){
                NSRange halfRange = [match rangeAtIndex:index];
                [newArray addObject:[newVersion substringWithRange:halfRange]];
            }
        }
        if ([oldArray count] == 3 && [newArray count] == 3) {
            for (int i = 0; i < 3; i++) {
                int a = [[newArray objectAtIndex:i] intValue];
                int b = [[oldArray objectAtIndex:i] intValue];
                if (a > b)
                    return YES;
                else if (a < b)
                    return NO;
                else if (a == b)
                    continue;
            }
        }
    }
    return NO;
}

+ (void)modifyBaiduDictory {
    NSArray *arrayOfPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsPath = [arrayOfPaths objectAtIndex:0];
    NSArray* files = @[@"baiduplist", @"cfg", @"log", @"navi", @"nmap", @"vmp"];
    NSArray* otherfiles = @[@"bdtts_license.dat", @"speech_stat.sqlite", @"speech_stat.sqlite-shm", @"speech_stat.sqlite-wal", @"parkItems", @"historyItems", @"sessions"];
    for (NSString* name in files) {
        NSString* nmapPath = [documentsPath stringByAppendingPathComponent: name];
        if ([nmapPath length] > 0)
        {
            if(![[NSFileManager defaultManager] fileExistsAtPath:nmapPath])
            {
                [[NSFileManager defaultManager] createDirectoryAtPath:nmapPath withIntermediateDirectories:YES attributes:nil error:NULL];
            }
            [self addFileSkipBackupAttribute:nmapPath];
        }
    }
    
    for (NSString* file in otherfiles)
    {
        NSString* path = [documentsPath stringByAppendingPathComponent:file];
        if (![[NSFileManager defaultManager] fileExistsAtPath:path]) {
            [[NSFileManager defaultManager] createFileAtPath:path contents:nil attributes:nil];
        }
        [TAPIUtility addFileSkipBackupAttribute:path];
    }
}

+ (int)addFileSkipBackupAttribute: (NSString*) filePath
{
    NSURL* url = [NSURL fileURLWithPath:filePath];
    const char* fileSysPath = [[url path] fileSystemRepresentation];
    
    const
    char* attrName = "com.apple.MobileBackup";
    u_int8_t attrValue = 1;
    
    setxattr(fileSysPath, attrName, &attrValue, sizeof(attrValue), 0, 0);
    //    int a = 0;
    //    attrName = (char*)malloc(30);
//    attrValue = 0;
//    ssize_t b = getxattr(fileSysPath, attrName, &attrValue, sizeof(attrValue), 0, 0);
//    NSLog(@"---%@---%d", filePath, attrValue);
    return 0;
}

//判断日期是今天，昨天 或者 其它日期
+(NSString *)getCommentTime:(NSDate *)date{
    
    NSTimeInterval secondsPerDay = 24 * 60 * 60;
    NSDate *today = [[NSDate alloc] init];
    NSDate *yesterday = [today dateByAddingTimeInterval: -secondsPerDay];
    
    // 10 first characters of description is the calendar date:
    NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
    formatter.dateFormat = @"yyyy-MM-dd";
    NSString * todayString = [formatter stringFromDate:today];//2015-05-12
    NSString * yesterdayString = [formatter stringFromDate:yesterday];
    NSString * dateString = [formatter stringFromDate:date];
    
    NSString* result = @"";
    if ([dateString isEqualToString:todayString])
    {
        result = @"今天";
        
    } else if ([dateString isEqualToString:yesterdayString])
    {
        result = @"昨天";
        
    } else {
        formatter.dateFormat = @"MM-dd";
        result = [formatter stringFromDate:date];
    }
    
    formatter.dateFormat = @" HH:mm";
    result = [result stringByAppendingString:[formatter stringFromDate:date]];
    return result;
}

+ (UIImage*)getLoactionImage {
    NSString* path = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) firstObject];
    path = [path stringByAppendingPathComponent:@"location_image"];
    NSData* data = [NSData dataWithContentsOfFile:path];
    UIImage * image = [UIImage imageWithData:data];
    return image;
}

+ (void)saveLocationImage:(UIImage *)image {
    NSString* path = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) firstObject];
    path = [path stringByAppendingPathComponent:@"location_image"];
    
    if (image) {
        NSData* data = UIImageJPEGRepresentation(image, 0.4);
        [data writeToFile:path atomically:YES];
    } else {
        [[NSFileManager defaultManager] removeItemAtPath:path error:nil];
    }
}

+ (UIImage*)rotateImage:(UIImage*)image degree:(CGFloat)degree {
    CGFloat width = CGImageGetWidth(image.CGImage);
    CGFloat height = CGImageGetHeight(image.CGImage);
    
    CGSize rotatedSize;
    
    rotatedSize.width = width;
    rotatedSize.height = height;
    
    UIGraphicsBeginImageContext(rotatedSize);
    CGContextRef bitmap = UIGraphicsGetCurrentContext();
    CGContextTranslateCTM(bitmap, rotatedSize.width/2, rotatedSize.height/2);
    CGContextRotateCTM(bitmap, degree * M_PI / 180);
    CGContextRotateCTM(bitmap, M_PI);
    CGContextScaleCTM(bitmap, -1.0, 1.0);
    CGContextDrawImage(bitmap, CGRectMake(-rotatedSize.width/2, -rotatedSize.height/2, rotatedSize.width, rotatedSize.height), image.CGImage);
    UIImage* newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext(); 
    return newImage;
}

+ (BOOL)checkPhotoAuthorization {
    //检测相机权限是否打开
    AVAuthorizationStatus authstatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
    if (authstatus ==AVAuthorizationStatusRestricted || authstatus ==AVAuthorizationStatusDenied) //用户关闭了权限
    {
        UIAlertView *alertView = [[UIAlertView alloc]initWithTitle:@"相机权限未开启" message:@"请在设置中允许打开相机" delegate:self cancelButtonTitle:@"确定" otherButtonTitles:nil];
        alertView.delegate =self;
        [alertView show];
        return NO;
    }
    return YES;
}

+ (NSString *)clearDoubleZero:(double)value fractionCount:(int)fractionCount
{
    if (fractionCount < 0) return nil;
    
    // 1.fmt ---> %.2f
    NSString *fmt = [NSString stringWithFormat:@"%%.%df", fractionCount];
    
    // 2.生成保留fractionCount位小数的字符串
    NSString *str = [NSString stringWithFormat:fmt, value];
    
    // 3.如果没有小数，直接返回
    if ([str rangeOfString:@"."].length == 0) {
        return str;
    }
    
    // 4.不断删除最后一个0 和 最后一个'.'
    int index = str.length - 1;
    unichar currentChar = [str characterAtIndex:index];
    
    while (currentChar == '0' ||  currentChar == '.') {
        if (currentChar == '.') {
            return [str substringToIndex:index];
        }
        
        index--;
        currentChar = [str characterAtIndex:index];
    }
    return [str substringToIndex:index + 1];
    //    unichar last = 0;
    //    while ( (last = [str characterAtIndex:str.length - 1]) == '0' ||
    //            last == '.') {
    //        str = [str substringToIndex:str.length - 1];
    //
    //        // 裁剪到'.'，直接返回
    //        if (last == '.') return str;
    //    }
}
@end
