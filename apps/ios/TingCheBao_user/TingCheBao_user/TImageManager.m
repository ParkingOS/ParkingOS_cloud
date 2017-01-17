//
//  TImageManager.m
//  
//
//  Created by apple on 15/7/29.
//
//

#import "TImageManager.h"

@implementation TImageManager

+ (TImageManager*)share {
    static dispatch_once_t onceToken;
    static TImageManager* manager = nil;
    dispatch_once(&onceToken, ^{
        manager = [[TImageManager alloc] init];
    });
    return manager;
}

- (id)init {
    if (self = [super init]) {
        
    }
    return self;
}

- (NSString*)getPathWithName:(NSString*)name {
    
    NSString* dirPath = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) firstObject];
    dirPath = [dirPath stringByAppendingPathComponent:@"imageMnager"];
    
    
    NSError* error;
    if (![[NSFileManager defaultManager] fileExistsAtPath:dirPath]) {

		if (![[NSFileManager defaultManager] createDirectoryAtPath:dirPath
									   withIntermediateDirectories:YES
														attributes:nil
															 error:&error])
		{
			NSLog(@"Failed to create folder '%@', error: %@", dirPath, error);
            return nil;
		}
	}
    dirPath = [dirPath stringByAppendingPathComponent:name];
    return dirPath;
}

- (UIImage*)getnImageWithName:(NSString*)name {
    NSData* data = [NSData dataWithContentsOfFile:[self getPathWithName:name]];
    UIImage * image = [UIImage imageWithData:data];
    return image;
}

- (void)saveImage:(UIImage *)image name:(NSString*)name{
    [self saveImage:image name:name Quality:1.0];
}

- (void)saveImage:(UIImage *)image name:(NSString*)name Quality:(CGFloat)quality {
    //如果image为nil,则删除
    if (image) {
        NSData* data = UIImageJPEGRepresentation(image, quality);
        [data writeToFile:[self getPathWithName:name] atomically:YES];
    } else {
        [[NSFileManager defaultManager] removeItemAtPath:[self getPathWithName:name] error:nil];
    }
}

@end
