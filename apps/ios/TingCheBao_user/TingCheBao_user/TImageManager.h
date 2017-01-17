//
//  TImageManager.h
//  
//
//  Created by apple on 15/7/29.
//
//

#import <Foundation/Foundation.h>

@interface TImageManager : NSObject

+ (TImageManager*)share;

- (UIImage*)getnImageWithName:(NSString*)name;

- (void)saveImage:(UIImage *)image name:(NSString*)name;

- (void)saveImage:(UIImage *)image name:(NSString*)name Quality:(CGFloat)quality;

@end
