
/**
 * Copyright (c) 2015-present, Viro, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 * @providesModule ViroAmbientLight
 * @flow
 */
'use strict';

import { requireNativeComponent, View, StyleSheet, Platform } from 'react-native';
import React, { Component } from 'react';
import normalizeColor from "react-native/Libraries/StyleSheet/normalizeColor"
var NativeMethodsMixin = require('react/lib/NativeMethodsMixin');
var NativeModules = require('react-native').NativeModules;
var PropTypes = require('react/lib/ReactPropTypes');
var ColorPropType = require('react-native').ColorPropType;


/**
 * Used to render a ViroAmbientLight
 */
var ViroAmbientLight = React.createClass({
  mixins: [NativeMethodsMixin],

  propTypes: {
    ...View.propTypes,
    color: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
  },

  render: function() {
      let nativeProps = Object.assign({}, this.props);
      nativeProps.style=[this.props.style];
      if (this.props.color) {
        let rgba = normalizeColor(this.props.color);
        let argb = ((rgba & 0xff) << 24) | (rgba >> 8);
        // iOS takes color in the form rgba, Android takes argb
        if (Platform.OS === 'ios') {
          nativeProps.color = rgba;
        } else if (Platform.OS === 'android') {
          nativeProps.color = argb;
        }
      }

      return (
        <VRTAmbientLight
          {...nativeProps}
        />
      );
  }
});

var VRTAmbientLight = requireNativeComponent(
  'VRTAmbientLight',
  ViroAmbientLight
);

module.exports = ViroAmbientLight;