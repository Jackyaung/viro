//
//  VRO360ImageManager.m
//  React
//
//  Created by Vik Advani on 3/3/16.
//  Copyright © 2016 Viro Media. All rights reserved.
//

#import "VRO360ImageManager.h"
#import "VRT360Image.h"
#import "RCTImageSource.h"

@implementation VRO360ImageManager

RCT_EXPORT_MODULE()
RCT_EXPORT_VIEW_PROPERTY(source, RCTImageSource)
RCT_EXPORT_VIEW_PROPERTY(onLoadStartViro, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onLoadEndViro, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(rotation, NSNumberArray)

- (VRT360Image *)view
{
  return [[VRT360Image alloc] initWithBridge:self.bridge];
}

@end